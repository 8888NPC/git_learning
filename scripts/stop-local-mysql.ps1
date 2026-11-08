param([int]$Port = 3306)

$ErrorActionPreference = 'Stop'
$mysqladmin = 'D:\mysql\bin\mysqladmin.exe'
if (-not (Test-Path -LiteralPath $mysqladmin)) {
    throw "找不到 mysqladmin.exe：$mysqladmin。请先运行 start-local-mysql.ps1。"
}

& $mysqladmin --no-defaults --protocol=TCP --host=127.0.0.1 "--port=$Port" --user=root shutdown
if ($LASTEXITCODE -ne 0) {
    throw "停止 MySQL 失败。请确认本地 MySQL 正在监听端口 $Port。"
}
Write-Output '本地 MySQL 已停止。'
