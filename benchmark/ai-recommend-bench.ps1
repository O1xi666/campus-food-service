# AI 推荐接口压测：对比「绕过缓存」与「命中缓存」的响应时间
#
# 用法：先设置 SKY_CACHE_ENABLED 并启动应用，再运行：
#   $env:SKY_CACHE_ENABLED="false"   # 或 "true"，然后启动应用
#   .\ai-recommend-bench.ps1 -Token <用户token> -Hits 6  -Tag off            # 绕过缓存
#   .\ai-recommend-bench.ps1 -Token <用户token> -Hits 12 -Tag on -Warmup     # 命中缓存
param(
    [Parameter(Mandatory=$true)][string]$Token,
    [int]$Hits = 6,
    [ValidateSet("on","off")][string]$Tag = "off",
    [string]$Preference = "spicy",
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$Warmup
)

$ErrorActionPreference = "Stop"
$outFile = Join-Path $PSScriptRoot "ai-recommend-$Tag.txt"
$tmp = Join-Path $env:TEMP "ai-rec.tmp"

# 预热：先真实跑一次，让缓存进入"已缓存"状态（仅 -Warmup 时执行）
if ($Warmup) {
    Write-Host "预热中（未命中，会真实调用大模型）..."
    curl.exe -s -G "$BaseUrl/user/ai/recommend" --data-urlencode "preference=$Preference" -H "token: $Token" -o $tmp
}

$times = @()
for ($i = 1; $i -le $Hits; $i++) {
    $t = curl.exe -s -G "$BaseUrl/user/ai/recommend" --data-urlencode "preference=$Preference" -H "token: $Token" -w "%{time_total}" -o $tmp
    $times += [double]$t
    Write-Host ("第 " + $i + " 次：" + $t + " s")
}

$times | Set-Content $outFile -Encoding UTF8
$avg = ($times | Measure-Object -Average).Average
Write-Host ("采样 " + $times.Count + " 次，平均 " + [math]::Round($avg, 4) + " s，结果写入 " + $outFile)
