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
# Rapid Enquiry Facility (RIF) - Helper script to create a directory in windows 
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
# Args 0: directory with FULL path, will be created if it does not exist 
#
Param(
	[ValidateNotNullOrEmpty()][string]$directory,
	[string]$log,
	[string]$err
)

#
# Logging functions
#
function Write-Feedback(){
    param
    (
        [Parameter(Position=0,ValueFromPipeline=$true)]
        [string]$msg,
        [string]$BackgroundColor = "Black",
        [string]$ForegroundColor = "Yellow"
    )

    Write-Host -BackgroundColor $BackgroundColor -ForegroundColor $ForegroundColor $msg;
    $msg | Out-File $log -Append -Width 180;
}
function Write-ErrorFeedback(){
    param
    (
        [Parameter(Position=0,ValueFromPipeline=$true)]
        [string]$msg,
        [string]$BackgroundColor = "Black",
        [string]$ForegroundColor = "Red"
    )

    Write-Host -BackgroundColor $BackgroundColor -ForegroundColor $ForegroundColor $msg;
    $msg | Out-File $err -Append -Width 180;
}

#
# Setup log if required
#
if (!$log) {
	$log=$(get-location).Path + "\mkdir.log"
}
if (!$err) {
	$err=$(get-location).Path + "\mkdir.err"
}

#
# Check directory exists
#
If (Test-Path $directory) { # Destination, assumed a directory	
	Write-Feedback "mkdir.ps1: directory: $directory already exists"
	exit 0
}
	
If (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {   

#
# If no Administrative rights, it will display a popup window asking user for Admin rights
#
# Get our script path, set log
#
	$ScriptPath = (Get-Variable MyInvocation).Value.MyCommand.Path

#
# Clean up log files
#
	If (Test-Path $log".err"){
		Remove-Item $log".err" -verbose
	}
	If (Test-Path $log){
		Remove-Item $log -verbose
	}
	If (Test-Path $err){
		Remove-Item $err -verbose
	}
#	
# Build relaunch string
#
	$RelaunchArgs = '-ExecutionPolicy Unrestricted -command ' + $ScriptPath + ' \"' + $directory + '\" \"' + $log + '\" \"' + $err + '\"'
	
# DO NOT USE: -NoNewWindow -Wait
	Write-Host "mkdir.ps1 log: $Log"
	Write-Host "mkdir.ps1 arguments: $RelaunchArgs"
	Try {
		$process = Start-Process "$PsHome\PowerShell.exe" -Verb RunAs -ArgumentList $RelaunchArgs -PassThru -Verbose
	}
	Catch {
		Write-Error "mkdir.ps1: ERROR! in Start-Process"
		write-Error "Exception Type: $($_.Exception.GetType().FullName)" 
		write-Error "Exception Message: $($_.Exception.Message)" 
		If (Test-Path $log){
			Write-Host "mkdir.ps1: log: $log >>>"
			get-content -Path $log		
			Write-Host "<<< End of log."
			rename-item -path $log -Newname $log".err" -force -verbose
		}
		exit 2
	}
#
# Wait until the elevated process terminates
#
    while (!($process.HasExited)) {
		Start-Sleep -Seconds 1
    }
#
# End of program
#
	[string]$my_status=$process.ExitCode; # BUG in powershell!
	If (Test-Path $err){
		Write-Host "mkdir.ps1: ERROR! in command execution: $err >>>" -ForegroundColor Red
		$err_msg=get-content -Path $err | Out-String
		Write-Host $err_msg -ForegroundColor Red
		Write-Host "<<< End of error trace." -ForegroundColor Red	
		
		If (Test-Path $log){
			Write-Host "mkdir.ps1: log: $log >>>"
			$log_msg=get-content -Path $log	| Out-String	
			Write-Host $log_msg -ForegroundColor Yellow
			Write-Host "<<< End of log."
			rename-item -path $log -Newname $log".err" -force -verbose
		}
		else {
			Write-Warning "mkdir.ps1: WARNING! no log: $log"	
		}		
		exit 1
	}
	else {
		If (Test-Path $log){
			Write-Host "mkdir.ps1: log: $log >>>"
			$log_msg=get-content -Path $log	| Out-String	
			Write-Host $log_msg -ForegroundColor Yellow	
			Write-Host "<<< End of log."
		}
		else {
			Write-Warning "mkdir.ps1: WARNING! no log: $log"	
		}	
		Write-Host "mkdir.ps1 Command $arguments ran OK."
		exit 0
	}
}
#
# After user clicked Yes on the popup, your file will be reopened with Admin rights
#

function DoElevatedOperations {
	Write-Feedback "mkdir.ps1 Running PRIVILEGED"
	Write-Feedback "mkdir.ps1 log: $Log"	
	Write-Feedback "mkdir.ps1 errors: $Err" 		
	Write-Feedback "mkdir.ps1 directory: $directory" 
#	
	If (-not (Test-Path $directory) ) { # Destination, assumed a directory
		New-Item $directory -type directory -verbose -ErrorAction Stop
		If (-not (Test-Path $directory) ) { # Destination, assumed a directory
			Throw "mkdir.ps1: directory: $directory not created"
		}
		else {	
			Write-Feedback "mkdir.ps1: directory: $directory created"
		}		
	}
	else {	
		Write-Feedback "mkdir.ps1: directory: $directory already exists"
	}
}

#
# Main Try/Catch block
#
Try {
	DoElevatedOperations
}
Catch {
    write-ErrorFeedback "mkdir.ps1: ERROR! in DoElevatedOperations(); Caught an exception:" 
    write-ErrorFeedback "Exception Type: $($_.Exception.GetType().FullName)" 
    write-ErrorFeedback "Exception Message: $($_.Exception.Message)" 
#
	Start-Sleep -Seconds 2
	exit 0
}

#
# Eof