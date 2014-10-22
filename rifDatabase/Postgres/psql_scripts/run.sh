#!/bin/sh
#
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
# Rapid Enquiry Facility (RIF) - Helper script to run a Unix command using bash. 
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
# Stdout and stderr are tee to the log
#
# Returns the exit status of the command
#
LOG=$1
WD=$2
CMD=$3
shift;shift;shift
ARGS=$*
MODULE=`basename $0`
#
# cmd() function
#
cmd() {
	echo "UNIX [`pwd`]> $CMD $ARGS"
	$CMD $ARGS 
	cmd_status=$?
	if [ $cmd_status != "0" ]; then
		echo "$MODULE: $CMD failed with status: $cmd_status"
	fi		
	echo $cmd_status > $LOG.status
}

#
# Check working directory exists, cd to it
#
if [ ! -d $WD ]; then	
	echo "$MODULE: run directory $WD not found"
	exit 2
fi
cd $WD

#
# Remove old log files, create
#
if [ -f $LOG ]; then
	cmd rm -f $LOG 
}
if [ -f $LOG.err ]; then
	cmd rm -f $LOG.err 
}
if [ -f $LOG.status ]; then
	cmd rm -f $LOG.status 
}
touch $LOG
if [ ! -w $LOG ]; then	
	echo "$MODULE: log file $LOG not writeable"
	exit 3
fi

#
# Run command
#
(cmd) | 2>&1 tee -a $LOG
#
# Check status file
#
if [ ! -f $LOG.status ]; then	
	echo "$MODULE: run status file $LOG.status not found" >> $LOG
	exit 4
fi
if [ ! -s $LOG.status ]; then	
	echo "$MODULE: run status file $LOG.status zero sized" >> $LOG
	exit 5
fi

#
# Extract status
#
EXIT_STATUS=`cat $LOG.status`
EXIT_STATUS=${EXIT_STATUS:=9999}
if [ $EXIT_STATUS = "9999" ] then
	echo "$MODULE: run status file $LOG.status not found/zero sized" >> $LOG
elif [ $EXIT_STATUS != "0" ] then
	mv -f $LOG $LOG.err
	exit 6
fi
rm -f $LOG.status
exit 0
#
# Eof