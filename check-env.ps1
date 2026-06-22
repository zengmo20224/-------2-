Set-Location 'F:\trae code\新建文件夹 (2)'
Write-Host '=== .env 文件内容（前 8 行）==='
Get-Content .env -Encoding UTF8 | Select-Object -First 8
Write-Host ''
Write-Host '=== 文件编码检查 ==='
$bytes = [System.IO.File]::ReadAllBytes('.env')
Write-Host ("文件大小: " + $bytes.Length + " bytes")
Write-Host ("前 3 字节 (查 BOM): " + ($bytes[0..2] | ForEach-Object { '{0:X2}' -f $_ }))
$content = [System.Text.Encoding]::UTF8.GetString($bytes)
Write-Host ''
Write-Host '=== JWT_SECRET 行 ==='
$jwtLine = ($content -split "`n") | Where-Object { $_ -match 'JWT_SECRET=' }
Write-Host $jwtLine
$jwtVal = ($jwtLine -replace 'JWT_SECRET=', '' -replace "`r", '')
Write-Host ("值长度: " + $jwtVal.Length)
$valBytes = [System.Text.Encoding]::UTF8.GetBytes($jwtVal)
$nonAscii = $valBytes | Where-Object { $_ -gt 127 }
Write-Host ("非 ASCII 字节数: " + $nonAscii.Count)
Write-Host ''
Write-Host '=== 工作目录路径 ==='
$pwd
Write-Host ("路径含中文: " + ($PWD.Path -match '[^\x00-\x7F]'))
Write-Host ("路径含空格: " + ($PWD.Path -match ' '))
