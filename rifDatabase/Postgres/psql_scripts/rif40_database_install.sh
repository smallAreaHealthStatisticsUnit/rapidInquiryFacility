#! /usr/bin/env bash

# ************************************************************************
#
# Description:
#
# Rapid Enquiry Facility (RIF) - RIF40 create production Postgres database from backup
#								   One directory (production) version
#
# This is a manual rewrite in Bash of the rif40_database_install.bat
# Windows script. At the time of writing (2018-03-22) it still has 
# a certain amount of hardcoding. In particular it doesn't prompt the user
# for anything.
#
# ************************************************************************

# Fail if anything goes wrong.
set -e 

NEWUSER=martin
NEWDB=sahsuland
NEWPW=martin
PGPASSWORD=postgres
RIF40PW=rif40


echo Creating production RIF Postgres database
PG_SYSCONFDIR=$(pg_config --sysconfdir)
echo PG_SYSCONFDIR=$PG_SYSCONFDIR

# Create the config dir if it doesn't exist
if [ ! -d "$PG_SYSCONFDIR" ]
then
	mkdir $PG_SYSCONFDIR
fi

# Copy in the config file if it's not already there.
if [ ! -f "$PG_SYSCONFDIR/psqlrc" ]
then
	if [ -f psqlrc ]
	then
		cp psqlrc "$PG_SYSCONFDIR"
	fi
fi

# Set some defaults
if [ "$NEWUSER" = "" ]
then
	NEWUSER=martin
fi

if [ "$NEWDB" = "" ]
then
	NEWDB=sahsuland
fi

echo "------------------------------------------------------------------------------------------"
echo "-"
echo "- WARNING! this script will the drop and create the RIF40 %NEWDB% Postgres database."
echo "-"
echo "- Test user: $NEWUSER; password: $NEWPW"
echo "- Postgres password:       $PGPASSWORD"
echo "- Schema (rif40) password: $RIF40PW"
echo "- PG password directory:   $PGPASSDIR"
echo "- PG sysconfig directory:  $PG_SYSCONFDIR"
echo "-"
echo "------------------------------------------------------------------------------------------"
echo 
read -p "Continue? " -n 1 -r
echo    
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    echo Database creation aborted
    exit 0
fi

psql -U postgres -d postgres -h localhost -w -e -P pager=off \
	-v testuser=$NEWUSER -v newdb=$NEWDB -v newpw=$®∏ \
	-v verbosity=terse \
	-v debug_level=1 \
	-v echo=all \
	-v postgres_password=$PGPASSWORD \
	-v rif40_password=$RIF40PW \
	-v tablespace_dir= \
	-v pghost=localhost \
	-v os=macos \
	-f db_create.sql | tee db_create.log

ECHO db_create.sql built $NEWDB OK

if [ -f sahsuland.sql ]
then
	psql -d $NEWDB -U postgres -f sahsuland.sql
else
	pg_restore -d $NEWDB -U postgres sahsuland_dev.dump
fi



