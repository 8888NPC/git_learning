param(
    [string]$InstallDirectory,
    [string]$DataDirectory = 'D:\mysql-data-task-api',
    [int]$Port = 3306
)

$ErrorActionPreference = 'Stop'
$asciiHome = 'D:\mysql'

function Test-MySqlPort {
    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $connection = $client.ConnectAsync('127.0.0.1', $Port)
        return $connection.Wait(500) -and $client.Connected
    } catch {
        return $false
    } finally {
        $client.Dispose()
    }
}

if (-not $InstallDirectory) {
    $softwareRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
    $candidate = Get-ChildItem -LiteralPath $softwareRoot -Directory -Filter 'mysql-*' |
        ForEach-Object { Get-ChildItem -LiteralPath $_.FullName -Directory -Filter '*-winx64' -ErrorAction SilentlyContinue } |
        Select-Object -First 1
    if (-not $candidate) {
        throw "没有找到 MySQL ZIP 解压目录。请传入 -InstallDirectory，例如 D:\后端软件\mysql-8.4.13\mysql-8.4.13-winx64。"
    }
    $InstallDirectory = $candidate.FullName
}

$mysqld = Join-Path $InstallDirectory 'bin\mysqld.exe'
if (-not (Test-Path -LiteralPath $mysqld)) {
    throw "找不到 mysqld.exe：$mysqld"
}

if (-not (Test-Path -LiteralPath $asciiHome)) {
    New-Item -ItemType Junction -Path $asciiHome -Target $InstallDirectory | Out-Null
} elseif (-not (Test-Path -LiteralPath (Join-Path $asciiHome 'bin\mysqld.exe'))) {
    throw "$asciiHome 已存在但不是可用的 MySQL 目录。请检查该路径后重试。"
}

if (-not (Test-Path -LiteralPath (Join-Path $DataDirectory 'mysql'))) {
    if (Test-Path -LiteralPath $DataDirectory) {
        $existingFiles = Get-ChildItem -LiteralPath $DataDirectory -Force
        if ($existingFiles.Count -gt 0) {
            throw "数据目录已存在但未初始化，脚本不会覆盖其中内容：$DataDirectory"
        }
    } else {
        New-Item -ItemType Directory -Path $DataDirectory | Out-Null
    }
    & $mysqld --no-defaults '--basedir=D:/mysql' "--datadir=$($DataDirectory.Replace('\','/'))" --initialize-insecure
    if ($LASTEXITCODE -ne 0) {
        throw 'MySQL 初始化失败。'
    }
}

$serverListening = Test-MySqlPort
$mysqldProcess = $null
if (-not $serverListening) {
    $arguments = @(
        '--no-defaults',
        '--basedir=D:/mysql',
        "--datadir=`"$($DataDirectory.Replace('\','/'))`"",
        '--bind-address=127.0.0.1',
        "--port=$Port"
    )
    $errorLog = Join-Path $DataDirectory 'mysqld-startup.err'
    $outputLog = Join-Path $DataDirectory 'mysqld-startup.out'
    $mysqldProcess = Start-Process -FilePath (Join-Path $asciiHome 'bin\mysqld.exe') `
        -ArgumentList $arguments -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput $outputLog -RedirectStandardError $errorLog
}

$ready = $false
for ($attempt = 0; $attempt -lt 60; $attempt++) {
    Start-Sleep -Milliseconds 500
    $serverListening = Test-MySqlPort
    if ($serverListening) {
        $ready = $true
        break
    }
    if ($null -ne $mysqldProcess -and $mysqldProcess.HasExited) {
        break
    }
}
if (-not $ready) {
    $startupError = Join-Path $DataDirectory 'mysqld-startup.err'
    $details = if (Test-Path -LiteralPath $startupError) { Get-Content -Raw -LiteralPath $startupError } else { '没有生成启动日志。' }
    throw "MySQL 未能在端口 $Port 启动。$details"
}

& (Join-Path $asciiHome 'bin\mysql.exe') --no-defaults --protocol=TCP --host=127.0.0.1 "--port=$Port" --user=root '--execute=CREATE DATABASE IF NOT EXISTS task_api CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;'
if ($LASTEXITCODE -ne 0) {
    throw 'MySQL 已启动，但创建 task_api 数据库失败。'
}

Write-Output "MySQL 已就绪：127.0.0.1:$Port，数据库 task_api，数据目录 $DataDirectory"
