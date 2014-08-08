#
# Helper script to run a windows command using Powershell. 
#
# Parameters:
# 1. Log file name
# 2. Command
# 3+. Args
#
# Stdout and stderr are tee to the log
#
# Returns the exit status of the command
#

Param(
[string]$log,
[string]$cmd
)

Write-Host "Log: $log"
Write-Host "Command: $args"
#
# CD up one directory
#
Set-Location ..

#
# Clean up log files
#
If (Test-Path $log".err"){
	Remove-Item $log".err" -verbose
}
If (Test-Path $log){
	Remove-Item $log -verbose
}

Try {
#	Invoke-expression -command "$cmd $args 2>&1 | tee $log" | Out-Null
	$process=(Start-Process $cmd -ArgumentList $args -NoNewWindow -verbose -PassThru -Wait) 2>&1 | tee $log
}
Catch {
	Write-Host "Error in Invoke-expression"
	$error[0]
	If (Test-Path $log){
		rename-item -path $log -Newname $log".err" -force -verbose
	}
	exit 2
}

if ($process.ExitCode -ne 0) {
	Write-Host "Error in command execution"
	$error[0]
	sleep 1
	rename-item -path $log -Newname $log".err" -force -verbose
	exit 1
}
else {
	Write-Host "Command $cmd ran OK."
	exit 0
}
#
# Eof