--
-- Postgres specific parameters
--
-- Usage: psql -w -e -f pg_%1.sql
-- Connect flags if required: -U <username> -d <Postgres database name> -h <host> -p <port>
--
\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing
