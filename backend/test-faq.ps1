$ErrorActionPreference = "SilentlyContinue"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000/api/faq" -Method GET -TimeoutSec 5
    Write-Host "Status: $($response.StatusCode)"
    Write-Host "Content (first 500 chars): $($response.Content.Substring(0, [Math]::Min(500, $response.Content.Length)))"
} catch {
    Write-Host "Error: $_"
}
