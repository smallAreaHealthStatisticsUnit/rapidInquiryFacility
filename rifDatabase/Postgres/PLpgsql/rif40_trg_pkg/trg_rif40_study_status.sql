
-- ************************************************************************
--
-- GIT Header
--
-- $Format:Git ID: (%h) %ci$
-- Version hash: $Format:%H$
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Postgres triggers
--
-- Trigger code will be split up on a per table basis
--
-- This is currently run only when the database is built from scripts
-- Ideally it should be refactored into many bits so it can be run easily in
-- alter scripts
--
-- Copyright:
--
-- The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
-- that rapidly addresses epidemiological and public health questions using 
-- routinely collected health and population data and generates standardised 
-- rates and relative risks for any given health outcome, for specified age 
-- and year ranges, for any given geographical area.
--
-- Copyright 2014 Imperial College London, developed by the Small Area
-- Health Statistics Unit. The work of the Small Area Health Statistics Unit 
-- is funded by the Public Health England as part of the MRC-PHE Centre for 
-- Environment and Health. Funding for this project has also been received 
-- from the Centers for Disease Control and Prevention.  
--
-- This file is part of the Rapid Inquiry Facility (RIF) project.
-- RIF is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Lesser General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- RIF is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
-- GNU Lesser General Public License for more details.
--
-- You should have received a copy of the GNU Lesser General Public License
-- along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
-- to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
-- Boston, MA 02110-1301 USA
--
-- Author:
--
-- Peter Hambly, SAHSU
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C20900: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

DROP TRIGGER IF EXISTS trg_rif40_study_status ON rif40_study_status CASCADE;
CREATE OR REPLACE FUNCTION rif40_trg_pkg.trg_rif40_study_status() RETURNS trigger AS $BODY$
DECLARE
	c1ststat CURSOR (l_study_id INTEGER) FOR
		SELECT COUNT(a.study_id)+1 AS ith_update
		  FROM t_rif40_study_status a
 		 WHERE a.study_id = COALESCE(l_study_id, currval('rif40_study_id_seq'::regclass)::integer); 
	c1_rec RECORD;
BEGIN
--
	IF TG_OP = 'INSERT' THEN
		OPEN c1ststat(NEW.study_id);
		FETCH c1ststat INTO c1_rec;
		CLOSE c1ststat;
--
-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT
--
		IF (USER = NEW.username OR NEW.username IS NULL /* Will be defaulted */) 
				AND rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
			INSERT INTO t_rif40_study_status (
				username,
				study_id,
				study_state,
				creation_date,
				ith_update,
				message)
			VALUES(
				COALESCE(NEW.username, "current_user"()),
				COALESCE(NEW.study_id, (currval('rif40_study_id_seq'::regclass))::integer),
				NEW.study_state /* no default value */,
				COALESCE(NEW.creation_date, current_timestamp),
				COALESCE(NEW.ith_update, c1_rec.ith_update),
				NEW.message /* no default value */);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_study_status',
				'Cannot INSERT: User % must have rif_user or rif_manager role, NEW.username (%) must be USER or NULL', 
				USER::VARCHAR, NEW.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'UPDATE' THEN
		OPEN c1ststat(OLD.study_id);
		FETCH c1ststat INTO c1_rec;
		CLOSE c1ststat;
	
--
-- Check USER = OLD.username and NEW.username = OLD.username; if OK UPDATE
--
		IF USER = OLD.username AND NEW.username = OLD.username THEN
			UPDATE t_rif40_study_status
			   SET username=NEW.username,
			       study_id=NEW.study_id,
			       study_state=NEW.study_state,
			       creation_date=NEW.creation_date,
			       ith_update=COALESCE(NEW.ith_update, c1_rec.ith_update),
			       message=NEW.message
			 WHERE study_id=OLD.study_id
			   AND study_state=OLD.study_state;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_study_status',
				'Cannot UPDATE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'DELETE' THEN
	
--
-- Check USER = OLD.username; if OK DELETE
--
		IF USER = OLD.username THEN
			DELETE FROM t_rif40_study_sql
			 WHERE study_id=OLD.study_id
			   AND study_state=OLD.study_state;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_study_status',
				'Cannot DELETE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
		END IF;
		RETURN NULL;
	END IF;
	RETURN NEW;
END;
$BODY$
LANGUAGE 'plpgsql';
COMMENT ON FUNCTION rif40_trg_pkg.trg_rif40_study_status() IS 	
	'INSTEAD OF trigger for view T_RIF40_STUDY_STATUS to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted.'; 

--
-- Eof