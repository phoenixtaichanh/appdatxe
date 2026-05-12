Add-Type -AssemblyName Microsoft.Office.Interop.Word
$word = New-Object -ComObject Word.Application
$word.Visible = $false

$files = @(
    "D:\laptrinhdidong\DoAn3\Research\D? Cuong Chi Ti?t.docx",
    "D:\laptrinhdidong\DoAn3\Research\KHMT - B?o C?o D? An 3 - LE DANG KHOA - TRAN NGUYEN TUAN ANH.docx"
)

$outputFile = "D:\laptrinhdidong\DoAn3\Research\wiki\docs_raw.txt"
$sb = [System.Text.StringBuilder]::new()
[void]$sb.AppendLine("=" * 80)
[void]$sb.AppendLine("FILE 1: D? Cuong Chi Ti?t.docx")
[void]$sb.AppendLine("=" * 80)

$doc1 = $word.Documents.Open("D:\laptrinhdidong\DoAn3\Research\D? Cuong Chi Ti?t.docx")
[void]$sb.AppendLine($doc1.Content.Text)
$doc1.Close($false)

[void]$sb.AppendLine("")
[void]$sb.AppendLine("=" * 80)
[void]$sb.AppendLine("FILE 2: KHMT - Bao Cao Du An 3")
[void]$sb.AppendLine("=" * 80)

$doc2 = $word.Documents.Open("D:\laptrinhdidong\DoAn3\Research\KHMT - Bao Cao Du An 3 - LE DANG KHOA - TRAN NGUYEN TUAN ANH.docx")
[void]$sb.AppendLine($doc2.Content.Text)
$doc2.Close($false)

$word.Quit()
[System.Runtime.Interopservices.Marshal]::ReleaseComObject($word) | Out-Null

$sb.ToString() | Out-File -FilePath $outputFile -Encoding UTF8
Write-Host "Done: $outputFile"
