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
# Rapid Enquiry Facility (RIF) - Helper script to zip a directory in windows 
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
# Args 0: Zip file
# Args 1: Directory to be zipped, must exist 
#
Param(
	[ValidateNotNullOrEmpty()][string]$zipFile,
	[ValidateNotNullOrEmpty()][string]$zipDir
)
#
# Setup log if required
#
if (!$log) {
	$log=$(get-location).Path + "\install.log"
}

#
# Clean up log files
#
If (Test-Path $log".err"){
	Remove-Item $log".err" -verbose
}
If (Test-Path $log){
	Remove-Item $log -verbose
}

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
    $msg | Out-File $log -Append -Width 180;
}

function DoZip {
	Write-Feedback "zip.ps1 log: $Log"	
	Write-Feedback "zip.ps1 errors: $Err" 		
	Write-Feedback "zip.ps1 zipFile: $zipFile" 
	Write-Feedback "zip.ps1 zipDir: $zipDir"
#	
	if(-not (test-path($zipFile))) {
		set-content $zipFile ("PK" + [char]5 + [char]6 + ("$([char]0)" * 18)) 
		(dir $zipFile).IsReadOnly = $false    		
	}
	if(-not (test-path($zipFile))) {
			Throw "zip.ps1: ERROR! Cannot create $zipFile"
	}

	$shellApplication=new-object -com shell.application -verbose -ErrorAction Stop

 	$Zip = $shellApplication.NameSpace($zipFile)
	$7zip = "C:\Program Files\7-Zip\7z.exe"
	if (-not ($Zip)) {
		if (test-path($7zip)) {
			Write-Feedback "zip.ps1: Using 7zip"
		}
		else {
			Throw "zip.ps1: ERROR! No ZIP program installed; install 7-zip"
		}
	}		
	Get-ChildItem $zipDir | foreach {
		$file = $_.fullname
		Write-Feedback "zip.ps1: Add: $file"
		if ($Zip) {
			$Zip.CopyHere($_.fullname)
		}
		elseif (test-path($7zip)) {
			$args = "a $zipFile $file"
			$process=(Start-Process $7zip -ArgumentList $args -NoNewWindow -verbose -PassThru -Wait) 2>&1 | tee $log
			if ($process.ExitCode -ne 0) {
				Throw "zip.ps1: ERROR! $process.ExitCode in $7zip $args"
			}
		}
	} 
}

#
# Main Try/Catch block
#
Try {
	DoZip
}
Catch {
    write-ErrorFeedback "zip.ps1: ERROR! in DoZip(); Caught an exception:" 
    write-ErrorFeedback "Exception Type: $($_.Exception.GetType().FullName)" 
    write-ErrorFeedback "Exception Message: $($_.Exception.Message)" 
	If (Test-Path $log){
		rename-item -path $log -Newname $log".err" -force -verbose
	}	
#
	exit 1
}

#
# Eof