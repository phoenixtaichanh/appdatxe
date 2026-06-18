param([string]$SqlFile = "$PSScriptRoot\database.sql")

$ErrorActionPreference='SilentlyContinue'
if (-not (Get-Command mysql -ErrorAction SilentlyContinue)) {
    Write-Host "Không tìm thấy lệnh 'mysql'. Bạn cần cài MySQL Client hoặc dùng MySQL Workbench." -ForegroundColor Red
    exit 1
}

$mysqlPassword = Read-Host "Nhập mật khẩu MySQL cho user root"
if ([string]::IsNullOrWhiteSpace($mysqlPassword)) { $mysqlPassword = '' }

& mysql -u root -p$mysqlPassword doan3_db < $SqlFile
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$correctHash = & node -e "const bcrypt=require('bcryptjs'); process.stdout.write(bcrypt.hashSync('password123',10));"
$updateSql = @"
UPDATE users SET password = '$correctHash'
WHERE email IN (
  'passenger@test.com','passenger@gmail.com',
  'driver@test.com','driver1@test.com','driver1@gmail.com','driver@gmail.com',
  'driver2@test.com','driver3@test.com'
);
"@

$updateSql | & mysql -u root -p$mysqlPassword doan3_db
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "`nDa seed va fix password xong. Tai khoan login duoc:" -ForegroundColor Green
& mysql -u root -p$mysqlPassword -e "SELECT id,email,user_type FROM doan3_db.users WHERE email LIKE '%@test.com' OR email LIKE '%@gmail.com';"
