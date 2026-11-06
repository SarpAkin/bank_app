# Runs WithdrawalTest against every mutant in mutation\mutants and prints KILLED or SURVIVED.
# Run it from the project root in PowerShell:
#   powershell -ExecutionPolicy Bypass -File mutation\run-mutants.ps1
# If "mvn" is not on PATH, give the Maven that comes with IntelliJ:
#   powershell -ExecutionPolicy Bypass -File mutation\run-mutants.ps1 -Mvn "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.3\plugins\maven\lib\maven3\bin\mvn.cmd"
# The original Withdrawal.java is restored at the end, even if the script is interrupted.
param([string]$Mvn = "mvn")

$target = "src\main\java\com\bankapp\operation\Withdrawal.java"
$backup = [System.IO.Path]::GetTempFileName()
Copy-Item $target $backup -Force
$killed = 0
$total = 0
try {
    $mutants = Get-ChildItem "mutation\mutants" -Directory | Where-Object { $_.Name -like "M*" } | Sort-Object Name
    foreach ($dir in $mutants) {
        Copy-Item (Join-Path $dir.FullName "Withdrawal.java") $target -Force
        $log = & $Mvn -q -B test "-Dtest=WithdrawalTest" "-Djacoco.skip=true" 2>&1 | Out-String
        $code = $LASTEXITCODE
        $total++
        if ($log -match "COMPILATION ERROR") {
            $status = "COMPILE ERROR"
        } elseif ($code -ne 0) {
            $status = "KILLED"
            $killed++
        } else {
            $status = "SURVIVED"
        }
        Write-Host ("{0}  {1}" -f $dir.Name, $status)
        $log -split "`r?`n" | Where-Object { $_ -match "^\[ERROR\]   WithdrawalTest" } |
            ForEach-Object { Write-Host ("      " + $_.Substring(10)) }
    }
} finally {
    Copy-Item $backup $target -Force
    Remove-Item $backup
}
Write-Host "Mutation score: $killed / $total killed"
