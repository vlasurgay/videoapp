param(
    [switch]$AI
)

$ErrorActionPreference = "Stop"

if ($AI) {
    $env:AI_ENABLED = "true"
    $env:WORKER_BUILD_TARGET = "videoapp-worker-ai"
}
else {
    $env:AI_ENABLED = "false"
    $env:WORKER_BUILD_TARGET = "videoapp-worker"
}

docker compose up -d --build
if ($LASTEXITCODE -ne 0) {
    throw "docker compose failed"
}

docker compose ps