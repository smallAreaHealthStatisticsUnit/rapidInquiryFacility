#!/bin/sh

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
