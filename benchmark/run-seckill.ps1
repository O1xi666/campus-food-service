<#
  秒杀下单链路压测（JMeter 非 GUI 模式）

  场景：单机、本机 MySQL、固定并发，对比 sky.seckill.mode=redis / direct-db 两种模式。
  被测接口：POST /user/order/seckill
  用法示例：见 README「复现方式」。

  注意：脚本只负责跑压测和统计，不改应用配置，也不重置库存。
  跑之前请自行把被测菜品库存重置到目标值（MySQL；redis 模式还要删掉 dish:stock:<id> 这个 key）。
#>
param(
    [Parameter(Mandatory = $true)][string]$Label,
    [Parameter(Mandatory = $true)][string]$Token,
    [int]$Threads = 200,
    [int]$Loops = 5,
    [int]$DishId = 68,
    [int]$AddressBookId = 4,
    [string]$Jmeter = "E:\JMter\apache-jmeter-5.6.3\bin\jmeter.bat",
    [string]$ServerHost = "localhost",
    [int]$Port = 8080,
    [string]$MySqlExe = "E:\mysql\bin\mysql.exe",
    [string]$MySqlUser = "root",
    [string]$MySqlPassword = "",
    [string]$Database = "sky_take_out"
)

$jtl = Join-Path $PSScriptRoot "seckill-$Label.jtl"

function Invoke-Sql([string]$query) {
    $sqlArgs = @("-u$MySqlUser", "-N", "-B", "-e", $query, $Database)
    if ($MySqlPassword) { $sqlArgs = @("-u$MySqlUser", "-p$MySqlPassword") + $sqlArgs }
    return (& $MySqlExe @sqlArgs 2>$null)
}

function Get-Status([string]$name) {
    return [int]((Invoke-Sql "SHOW GLOBAL STATUS LIKE '$name'") -split "`t")[1]
}

# 计数器差值：Com_* 看数据库实际承受的语句量，Innodb_row_lock_* 看行锁竞争
$counterNames = @("Com_select", "Com_insert", "Com_update", "Innodb_row_lock_waits", "Innodb_row_lock_time")
Invoke-Sql "FLUSH STATUS" | Out-Null
$before = @{}
foreach ($c in $counterNames) { $before[$c] = Get-Status $c }

Write-Host "==> 运行秒杀压测 [$Label]  并发=$Threads  轮次=$Loops  总请求=$($Threads * $Loops)"
& $Jmeter -n -t (Join-Path $PSScriptRoot "seckill.jmx") -l $jtl "-Jhost=$ServerHost" "-Jport=$Port" "-Jthreads=$Threads" "-Jloops=$Loops" "-Jtoken=$Token" "-JdishId=$DishId" "-JaddressBookId=$AddressBookId" 2>&1 | Select-String -Pattern "summary" | Select-Object -Last 2

$after = @{}
foreach ($c in $counterNames) { $after[$c] = Get-Status $c }
$maxConn = Get-Status "Max_used_connections"

# ---- 接口侧指标 ----
$rows = Import-Csv $jtl
if (-not $rows) { Write-Host "没有采集到样本"; exit 1 }
$elapsed = $rows | ForEach-Object { [double]$_.elapsed } | Sort-Object
$n = $elapsed.Count
$stamps = $rows | ForEach-Object { [double]$_.timeStamp }
$durMs = (($stamps | Measure-Object -Maximum).Maximum + $elapsed[$n - 1]) - ($stamps | Measure-Object -Minimum).Minimum
$durSec = $durMs / 1000.0
$ifQps = [Math]::Round($n / $durSec, 1)

function Pct([double[]]$sorted, [double]$p) {
    $idx = [Math]::Ceiling($p * $sorted.Count) - 1
    if ($idx -lt 0) { $idx = 0 }
    return $sorted[$idx]
}

$sum = 0.0
foreach ($e in $elapsed) { $sum += $e }

$dbTotal = 0
foreach ($c in @("Com_select", "Com_insert", "Com_update")) { $dbTotal += ($after[$c] - $before[$c]) }
$dbQps = [Math]::Round($dbTotal / $durSec, 1)

$result = [ordered]@{
    Label             = $Label
    Threads           = $Threads
    Loops             = $Loops
    Samples           = $n
    Errors            = ($rows | Where-Object { $_.success -ne "true" }).Count
    IF_QPS            = $ifQps
    Avg_ms            = [Math]::Round($sum / $n, 2)
    P50_ms            = [Math]::Round((Pct $elapsed 0.50), 2)
    P95_ms            = [Math]::Round((Pct $elapsed 0.95), 2)
    P99_ms            = [Math]::Round((Pct $elapsed 0.99), 2)
    Max_ms            = [Math]::Round($elapsed[$n - 1], 2)
    Duration_s        = [Math]::Round($durSec, 2)
    Com_select        = $after["Com_select"] - $before["Com_select"]
    Com_insert        = $after["Com_insert"] - $before["Com_insert"]
    Com_update        = $after["Com_update"] - $before["Com_update"]
    DB_Statements     = $dbTotal
    DB_QPS            = $dbQps
    RowLockWaits      = $after["Innodb_row_lock_waits"] - $before["Innodb_row_lock_waits"]
    RowLockTime_ms    = $after["Innodb_row_lock_time"] - $before["Innodb_row_lock_time"]
    MaxUsedConn       = $maxConn
}
$result.GetEnumerator() | ForEach-Object { "{0,-18} {1}" -f $_.Key, $_.Value }
$result | ConvertTo-Json | Set-Content (Join-Path $PSScriptRoot "summary-seckill-$Label.json") -Encoding UTF8
