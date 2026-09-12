<#
  菜品列表接口压测脚本（JMeter 非 GUI 模式）

  场景：单机、本机 MySQL、固定并发，对比 sky.cache.enabled=true / false 两种情况。
  被测接口：GET /user/dish/list

  用法示例：
    .\run-benchmark.ps1 -Label cache-on
    .\run-benchmark.ps1 -Label cache-off

  注意：压测前需要先用对应的 SKY_CACHE_ENABLED 启动应用（见项目根 README）。
  脚本只负责跑压测和统计，不改应用配置。
#>
param(
    [Parameter(Mandatory = $true)][string]$Label,
    [string]$Jmeter = "E:\JMter\apache-jmeter-5.6.3\bin\jmeter.bat",
    [string]$ServerHost = "localhost",
    [int]$Port = 8080,
    [int]$Threads = 50,
    [int]$Loops = 20,
    [string]$Plan
)

if (-not $Plan) { $Plan = Join-Path $PSScriptRoot "dish-list.jmx" }
$jtl = Join-Path $PSScriptRoot "results-$Label.jtl"

Write-Host "==> 运行压测 [$Label]  $ServerHost`:$Port  并发=$Threads  轮次=$Loops  总请求=$($Threads * $Loops)"
& $Jmeter -n -t $Plan -l $jtl "-Jhost=$ServerHost" "-Jport=$Port" "-Jthreads=$Threads" "-Jloops=$Loops" 2>&1 |
    Select-String -Pattern "summary|Err|error|Running|Waiting" | Select-Object -Last 6

$rows = Import-Csv $jtl
$elapsed = $rows | ForEach-Object { [double]$_.elapsed } | Sort-Object
$n = $elapsed.Count
if ($n -eq 0) { Write-Host "没有采集到样本"; exit 1 }

function Pct([double[]]$sorted, [double]$p) {
    $idx = [Math]::Ceiling($p * $sorted.Count) - 1
    if ($idx -lt 0) { $idx = 0 }
    return $sorted[$idx]
}

$errors = ($rows | Where-Object { $_.success -ne "true" }).Count
$stamps = $rows | ForEach-Object { [double]$_.timeStamp }
$durMs = (($stamps | Measure-Object -Maximum).Maximum + $elapsed[$n - 1]) - ($stamps | Measure-Object -Minimum).Minimum
$throughput = [Math]::Round($n / ($durMs / 1000.0), 1)

$sum = 0.0; foreach ($e in $elapsed) { $sum += $e }

$result = [ordered]@{
    Label        = $Label
    Samples      = $n
    Threads      = $Threads
    Errors       = $errors
    ErrorRate    = "{0:N2}%" -f (100.0 * $errors / $n)
    Avg_ms       = [Math]::Round($sum / $n, 2)
    Min_ms       = [Math]::Round($elapsed[0], 2)
    P50_ms       = [Math]::Round((Pct $elapsed 0.50), 2)
    P90_ms       = [Math]::Round((Pct $elapsed 0.90), 2)
    P95_ms       = [Math]::Round((Pct $elapsed 0.95), 2)
    P99_ms       = [Math]::Round((Pct $elapsed 0.99), 2)
    Max_ms       = [Math]::Round($elapsed[$n - 1], 2)
    Throughput   = "$throughput req/s"
    Jtl          = $jtl
}
$result.GetEnumerator() | ForEach-Object { "{0,-12} {1}" -f $_.Key, $_.Value }
$result | ConvertTo-Json | Set-Content (Join-Path $PSScriptRoot "summary-$Label.json") -Encoding UTF8