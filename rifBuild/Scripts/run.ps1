# ************************************************************************
#
# GIT Header
#
# $Format:Git ID: (%h) %ci$
# $Id: e96a6b0aa1ba85325e1b7b0e57163d2b7707440b $
# Version hash: $Format:%H$
#
# Description:
#
# Rapid Enquiry Facility (RIF) - Helper script to run a windows command using Powershell. 
#
# Copyright:
#
# The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
# that rapidly addresses epidemiological and public health questions using 
# routinely collected health and population data and generates standardised 
# rates and relative risks for any given health outcome, for specified age 
# and year ranges, for any given geographical area.
#
# Copyright 2014 Imperial College London, developed by the Small Area
# Health Statistics Unit. The work of the Small Area Health Statistics Unit 
# is funded by the Public Health England as part of the MRC-PHE Centre for 
# Environment and Health. Funding for this project has also been received 
# from the Centers for Disease Control and Prevention.  
#
# This file is part of the Rapid Inquiry Facility (RIF) project.
# RIF is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# RIF is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
# to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
# Boston, MA 02110-1301 USA
#
# Author:
#
# Peter Hambly, SAHSU
#
# Helper script to run a windows command using Powershell. 
#
# Parameters:
# 1. Log file name
# 2. Working directory 
# 3. Command
# 4+. Args
#
# Stdout and stderr are tee'd to the log
#
# Returns the exit status of the command
#

Param(
[ValidateNotNullOrEmpty()][string]$log,
[ValidateNotNullOrEmpty()][string]$curdir,
[ValidateNotNullOrEmpty()][string]$cmd
)

Write-Host "Log: $log"
Write-Host "Working directory: $curdir"
Write-Host "Command: $cmd"
Write-Host "Arguments: $args"
#
# CD to working directory
#
Set-Location $curdir

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
	Write-Error "run.ps1: ERROR! in Invoke-expression"
    write-Error "Exception Type: $($_.Exception.GetType().FullName)" 
    write-Error "Exception Message: $($_.Exception.Message)" 
	If (Test-Path $log){
		rename-item -path $log -Newname $log".err" -force -verbose
	}
	exit 2
}

if ($process.ExitCode -ne 0) {
	Write-Error "run.ps1: ERROR! $process.ExitCode in command execution"
	sleep 1
	rename-item -path $log -Newname $log".err" -force -verbose
	exit 1
}
else {
	Write-Host "run.ps1 Command $cmd ran OK."
	exit 0
}
#
# Eof