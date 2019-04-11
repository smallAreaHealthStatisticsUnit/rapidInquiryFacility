1. Create roles and accounts in Oracle using SQL*Plus and SYSTEM (Sn4k3B1teBlack$), and test:

* RIF_USER role. You could create more project specific roles
  ```
  CREATE ROLE rif_user;
  ```
  
* RIF User (peter)
  ```
  CREATE USER peter IDENTIFIED BY Orz1cTentac1e$; 
  GRANT CONNECT, rif_user TO peter;
  
* For explain plan to work logon as SYS with SYSDBA 
  ```
  GRANT SELECT ON v_$sql TO rif_user;
  GRANT SELECT ON v_$sql_plan TO rif_user;
  ```
  
* RIF Health Data (rif_data)

  ```  
CREATE USER rif_health_data IDENTIFIED BY Culiembro2001#;
GRANT CONNECT, RESOURCE TO rif_health_data;
ALTER USER rif_health_data QUOTA UNLIMITED ON users;

* Logon as rif_health_data, create a dummy table:
  ```
  CREATE TABLE my_all_tables AS SELECT owner, table_name, tablespace_name, num_rows, last_analyzed FROM all_tables;
  GRANT SELECT ON my_all_tables TO rif_user;
  ```
  
* Logon as RIF User (peter), check access to dummy table:
  ```
  DESC rif_health_data.my_all_tables
     Name                                      Null?    Type
   ----------------------------------------- -------- ----------------------------  
   OWNER                                     NOT NULL VARCHAR2(128) 
   TABLE_NAME                                NOT NULL VARCHAR2(128)
   TABLESPACE_NAME                                    VARCHAR2(30)
   NUM_ROWS                                           NUMBER
   LAST_ANALYZED                                      DATE

  SELECT * FROM rif_health_data.my_all_tables;
   owner  |           table_name           | tablespace_name | num_rows | last_analyzed
  --------+--------------------------------+-----------------+----------+---------------
   SYS    | DUAL                           | SYSTEM          |        1 | 2018-03-30
   SYS    | SYSTEM_PRIVILEGE_MAP           | SYSTEM          |      257 | 2019-02-21
   SYS    | TABLE_PRIVILEGE_MAP            | SYSTEM          |       26 | 2019-02-21
   SYS    | USER_PRIVILEGE_MAP             | SYSTEM          |        4 | 2019-02-21
  ...
  ```
  
2. Setup Oracle FDW as Postgres using psql

* Create Oracle_fdw extension:
  ```
  CREATE EXTENSION oracle_fdw;
  CREATE SERVER orcl FOREIGN DATA WRAPPER oracle_fdw OPTIONS (dbserver '//155.198.41.211/ORCL');
  GRANT USAGE ON FOREIGN SERVER orcl TO rif_user, rif40;  
  ```
* Foreach RIF user needing remote tabler access create a user mapping  
  ```
  DROP USER MAPPING IF EXISTS FOR postgres SERVER orcl;
  CREATE USER MAPPING FOR postgres SERVER orcl OPTIONS (user 'peter', password 'Orz1cTentac1e$');
  ```
* Do **NOT** use LDAP. Oracle uses a non standard LDAP library which will interact badly with the Postgres standard library.
  
3. Setup 

* As rif40 create a foreign table:
  ```
  CREATE FOREIGN TABLE my_all_tables (
	OWNER                                    VARCHAR(128),
	TABLE_NAME                               VARCHAR(128),
	TABLESPACE_NAME                          VARCHAR(30),
	NUM_ROWS                                 INTEGER,
	LAST_ANALYZED                            DATE
  ) SERVER orcl OPTIONS (schema 'RIF_HEALTH_DATA', table 'MY_ALL_TABLES', readonly 'true');

  \dS+ my_all_tables
                                                 Foreign table "peter.my_all_tables"
       Column      |          Type          | Collation | Nullable | Default | FDW options | Storage  | Stats target | Description
  -----------------+------------------------+-----------+----------+---------+-------------+----------+--------------+-------------
   owner           | character varying(128) |           |          |         |             | extended |              |
   table_name      | character varying(128) |           |          |         |             | extended |              | 
   tablespace_name | character varying(30)  |           |          |         |             | extended |              |
   num_rows        | integer                |           |          |         |             | plain    |              |
   last_analyzed   | date                   |           |          |         |             | plain    |              |
  Server: orcl
  FDW options: (schema 'RIF_HEALTH_DATA', "table" 'MY_ALL_TABLES', readonly 'true')
  ```
* Grant access as required. This can be to all RIF users as in this case, a project role or a specific user
  ```
  GRANT SELECT ON my_all_tables TO rif_user;
  ```
 * Check the schema owner has no access to the health data.   
  ``` 
  SELECT * FROM  my_all_tables LIMIT 5;
  ERROR:  user mapping not found for "rif40"
  ``` 
  Note that the *postgres* superuser will also not have access unless it has a user mapping!
 * Or for batch work use the import foreign schema command

  ```
  DROP FOREIGN TABLE IF EXISTS my_all_tables;
  IMPORT FOREIGN SCHEMA 'RIF_HEALTH_DATA' LIMIT TO ('MY_ALL_TABLES', 'USER_TABLES') 
  FROM orcl OPTIONS (case 'lower', readonly 'true');
  ```
  
* As RIF user (peter)
  ```
  \dS+ rif40.my_all_tables                                              Foreign table "rif40.my_all_tables"
  
       Column      |          Type          | Collation | Nullable | Default | FDW options | Storage  | Stats target | Description
  -----------------+------------------------+-----------+----------+---------+-------------+----------+--------------+-------------
   owner           | character varying(128) |           |          |         |             | extended |              |
   table_name      | character varying(128) |           |          |         |             | extended |              | 
   tablespace_name | character varying(30)  |           |          |         |             | extended |              |
   num_rows        | integer                |           |          |         |             | plain    |              |
   last_analyzed   | date                   |           |          |         |             | plain    |              |
  Server: orcl
  FDW options: (schema 'RIF_HEALTH_DATA', "table" 'MY_ALL_TABLES', readonly 'true')
  
  SELECT * FROM  my_all_tables LIMIT 5; 
   owner |      table_name       | tablespace_name | num_rows | last_analyzed
  -------+-----------------------+-----------------+----------+---------------
   SYS   | DUAL                  | SYSTEM          |        1 | 2018-03-30
   SYS   | SYSTEM_PRIVILEGE_MAP  | SYSTEM          |      257 | 2019-02-21
   SYS   | TABLE_PRIVILEGE_MAP   | SYSTEM          |       26 | 2019-02-21
   SYS   | USER_PRIVILEGE_MAP    | SYSTEM          |        4 | 2019-02-21
   SYS   | STMT_AUDIT_OPTION_MAP | SYSTEM          |      337 | 2018-03-30
  (5 rows)
  ```

4. Diagnsotics
 
The information will also be avaiable in various *pg_* views.
 
* To check the Oracle_fdw version:
  ``` 
   \dx  
                          List of installed extensions
       Name    | Version |  Schema  |              Description
   ------------+---------+----------+----------------------------------------
    oracle_fdw | 1.1     | postgres | foreign data wrapper for Oracle access
  (1 row)
  ```
  
\des+
\det+
\deu+

* To see the user mappings. You can see your own password, the superuser *postgres* can see all passwords
sahsu=> \deu+
                      List of user mappings
 Server | User name |                 FDW options
--------+-----------+---------------------------------------------
 orcl   | peter     | ("user" 'peter', password 'Orz1cTentac1e$')
 orcl   | postgres  |
(2 rows)
\dew+
* To use the connection diagnostic *oracle_diag()*; as the user postgres. Be careful; this will also grant the Postgres user access to the data.
  ```
  CREATE USER MAPPING FOR peter SERVER orcl OPTIONS (user 'peter', password 'Orz1cTentac1e$');
  SELECT oracle_diag('orcl'::Text);
                                        oracle_diag
  ---------------------------------------------------------------------------------------
   oracle_fdw 2.1.0, PostgreSQL 10.5, Oracle client 18.3.0.0.0, Oracle server 18.0.0.0.0
  (1 row)
  ```
  
5. HES Example

* In Oracle as SYSTEM or RIF grant user access to data:

```
GRANT SELECT ON rif.rif_201617_apr2019 TO peter;
```

* Find and describe the table:
  ```
  SQL> select table_name from all_tables where owner = 'RIF';

	TABLE_NAME
	--------------------------------------------------------------------------------
	RIF_201617_APR2019

  SQL> desc rif.RIF_201617_APR2019
	 Name                                      Null?    Type
	 ----------------------------------------- -------- ----------------------------
	 SAHSU_ID                                           NUMBER
	 AGE_SEX_GROUP                                      NUMBER
	 EXTRACT_HESID                                      VARCHAR2(32)
	 DIAG_01                                            VARCHAR2(6)
	 DIAG_02                                            VARCHAR2(6)
	 DIAG_03                                            VARCHAR2(6)
	 DIAG_04                                            VARCHAR2(6)
	 DIAG_05                                            VARCHAR2(6)
	 DIAG_06                                            VARCHAR2(6)
	 DIAG_07                                            VARCHAR2(6)
	 DIAG_08                                            VARCHAR2(6)
	 DIAG_09                                            VARCHAR2(6)
	 DIAG_10                                            VARCHAR2(6)
	 DIAG_11                                            VARCHAR2(6)
	 DIAG_12                                            VARCHAR2(6)
	 DIAG_13                                            VARCHAR2(6)
	 DIAG_14                                            VARCHAR2(6)
	 DIAG_15                                            VARCHAR2(6)
	 DIAG_16                                            VARCHAR2(6)
	 DIAG_17                                            VARCHAR2(6)
	 DIAG_18                                            VARCHAR2(6)
	 DIAG_19                                            VARCHAR2(6)
	 DIAG_20                                            VARCHAR2(6)
	 OPERTN_01                                          VARCHAR2(4)
	 OPERTN_02                                          VARCHAR2(4)
	 OPERTN_03                                          VARCHAR2(4)
	 OPERTN_04                                          VARCHAR2(4)
	 OPERTN_05                                          VARCHAR2(4)
	 OPERTN_06                                          VARCHAR2(4)
	 OPERTN_07                                          VARCHAR2(4)
	 OPERTN_08                                          VARCHAR2(4)
	 OPERTN_09                                          VARCHAR2(4)
	 OPERTN_10                                          VARCHAR2(4)
	 OPERTN_11                                          VARCHAR2(4)
	 OPERTN_12                                          VARCHAR2(4)
	 OPERTN_13                                          VARCHAR2(4)
	 OPERTN_14                                          VARCHAR2(4)
	 OPERTN_15                                          VARCHAR2(4)
	 OPERTN_16                                          VARCHAR2(4)
	 OPERTN_17                                          VARCHAR2(4)
	 OPERTN_18                                          VARCHAR2(4)
	 OPERTN_19                                          VARCHAR2(4)
	 OPERTN_20                                          VARCHAR2(4)
	 OPERTN_21                                          VARCHAR2(4)
	 OPERTN_22                                          VARCHAR2(4)
	 OPERTN_23                                          VARCHAR2(4)
	 OPERTN_24                                          VARCHAR2(4)
	 EPISTART                                           DATE
	 YEAR                                               NUMBER
	 RIFCOUNTRY2011                                     CHAR(9)
	 COA11                                              VARCHAR2(9)
	 GOR11                                              VARCHAR2(9)
	 LAD11                                              VARCHAR2(9)
	 LSOA11                                             VARCHAR2(9)
	 MSOA11                                             VARCHAR2(9)
   ```
* Create indexes and analyze as required:
  ```
  CREATE UNIQUE INDEX rif.rif_201617_apr2019_uk ON rif.rif_201617_apr2019(sahsu_id);
  CREATE BITMAP INDEX rif.rif_201617_apr2019_yr ON rif.rif_201617_apr2019(year);
  CREATE BITMAP INDEX rif.rif_201617_apr2019_asg ON rif.rif_201617_apr2019(age_sex_group);
  CREATE BITMAP INDEX rif.rif_201617_apr2019_rifcountry2011 ON rif.rif_201617_apr2019(rifcountry2011);
  CREATE BITMAP INDEX rif.rif_201617_apr2019_coa11 ON rif.rif_201617_apr2019(coa11);
  CREATE BITMAP INDEX rif.rif_201617_apr2019_gor11 ON rif.rif_201617_apr2019(gor11);
  CREATE BITMAP INDEX rif.rif_201617_apr2019_lad11 ON rif.rif_201617_apr2019(lad11);
  CREATE BITMAP INDEX rif.rif_201617_apr2019_lsoa11 ON rif.rif_201617_apr2019(lsoa11);
  CREATE BITMAP INDEX rif.rif_201617_apr2019_msoa11 ON rif.rif_201617_apr2019(msoa11);
  ANALYZE TABLE rif.rif_201617_apr2019 ESTIMATE STATISTICS;
  ```
* In Postgres as rif40 create a foreign table, converting the datatype to Postgres; grant access as required:
  ```
  CREATE FOREIGN TABLE rif_201617_apr2019 (
	 SAHSU_ID                                           NUMERIC,
	 AGE_SEX_GROUP                                      NUMERIC,
	 EXTRACT_HESID                                      VARCHAR(32),
	 DIAG_01                                            VARCHAR(6),
	 DIAG_02                                            VARCHAR(6),
	 DIAG_03                                            VARCHAR(6),
	 DIAG_04                                            VARCHAR(6),
	 DIAG_05                                            VARCHAR(6),
	 DIAG_06                                            VARCHAR(6),
	 DIAG_07                                            VARCHAR(6),
	 DIAG_08                                            VARCHAR(6),
	 DIAG_09                                            VARCHAR(6),
	 DIAG_10                                            VARCHAR(6),
	 DIAG_11                                            VARCHAR(6),
	 DIAG_12                                            VARCHAR(6),
	 DIAG_13                                            VARCHAR(6),
	 DIAG_14                                            VARCHAR(6),
	 DIAG_15                                            VARCHAR(6),
	 DIAG_16                                            VARCHAR(6),
	 DIAG_17                                            VARCHAR(6),
	 DIAG_18                                            VARCHAR(6),
	 DIAG_19                                            VARCHAR(6),
	 DIAG_20                                            VARCHAR(6),
	 OPERTN_01                                          VARCHAR(4),
	 OPERTN_02                                          VARCHAR(4),
	 OPERTN_03                                          VARCHAR(4),
	 OPERTN_04                                          VARCHAR(4),
	 OPERTN_05                                          VARCHAR(4),
	 OPERTN_06                                          VARCHAR(4),
	 OPERTN_07                                          VARCHAR(4),
	 OPERTN_08                                          VARCHAR(4),
	 OPERTN_09                                          VARCHAR(4),
	 OPERTN_10                                          VARCHAR(4),
	 OPERTN_11                                          VARCHAR(4),
	 OPERTN_12                                          VARCHAR(4),
	 OPERTN_13                                          VARCHAR(4),
	 OPERTN_14                                          VARCHAR(4),
	 OPERTN_15                                          VARCHAR(4),
	 OPERTN_16                                          VARCHAR(4),
	 OPERTN_17                                          VARCHAR(4),
	 OPERTN_18                                          VARCHAR(4),
	 OPERTN_19                                          VARCHAR(4),
	 OPERTN_20                                          VARCHAR(4),
	 OPERTN_21                                          VARCHAR(4),
	 OPERTN_22                                          VARCHAR(4),
	 OPERTN_23                                          VARCHAR(4),
	 OPERTN_24                                          VARCHAR(4),
	 EPISTART                                           DATE,
	 YEAR                                               NUMERIC,
	 RIFCOUNTRY2011                                     VARCHAR(9),
	 COA11                                              VARCHAR(9),
	 GOR11                                              VARCHAR(9),
	 LAD11                                              VARCHAR(9),
	 LSOA11                                             VARCHAR(9),
	 MSOA11                                             VARCHAR(9)
  ) SERVER orcl OPTIONS (schema 'RIF', table 'RIF_201617_APR2019', readonly 'true');
  
  GRANT SELECT ON rif_201617_apr2019 TO rif_user;

  \dS+ rif_201617_apr2019
											   Foreign table "rif40.rif_201617_apr2019"
		 Column     |         Type          | Collation | Nullable | Default | FDW options | Storage  | Stats target | Description
	----------------+-----------------------+-----------+----------+---------+-------------+----------+--------------+-------------
	 sahsu_id       | numeric               |           |          |         |             | main     |              |
	 age_sex_group  | numeric               |           |          |         |             | main     |              |
	 extract_hesid  | character varying(32) |           |          |         |             | extended |              |
	 diag_01        | character varying(6)  |           |          |         |             | extended |              |
	 diag_02        | character varying(6)  |           |          |         |             | extended |              |
	 diag_03        | character varying(6)  |           |          |         |             | extended |              |
	 diag_04        | character varying(6)  |           |          |         |             | extended |              |
	 diag_05        | character varying(6)  |           |          |         |             | extended |              |
	 diag_06        | character varying(6)  |           |          |         |             | extended |              |
	 diag_07        | character varying(6)  |           |          |         |             | extended |              |
	 diag_08        | character varying(6)  |           |          |         |             | extended |              |
	 diag_09        | character varying(6)  |           |          |         |             | extended |              |
	 diag_10        | character varying(6)  |           |          |         |             | extended |              |
	 diag_11        | character varying(6)  |           |          |         |             | extended |              |
	 diag_12        | character varying(6)  |           |          |         |             | extended |              |
	 diag_13        | character varying(6)  |           |          |         |             | extended |              |
	 diag_14        | character varying(6)  |           |          |         |             | extended |              |
	 diag_15        | character varying(6)  |           |          |         |             | extended |              |
	 diag_16        | character varying(6)  |           |          |         |             | extended |              |
	 diag_17        | character varying(6)  |           |          |         |             | extended |              |
	 diag_18        | character varying(6)  |           |          |         |             | extended |              |
	 diag_19        | character varying(6)  |           |          |         |             | extended |              |
	 diag_20        | character varying(6)  |           |          |         |             | extended |              |
	 opertn_01      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_02      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_03      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_04      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_05      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_06      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_07      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_08      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_09      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_10      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_11      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_12      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_13      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_14      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_15      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_16      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_17      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_18      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_19      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_20      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_21      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_22      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_23      | character varying(4)  |           |          |         |             | extended |              |
	 opertn_24      | character varying(4)  |           |          |         |             | extended |              |
	 epistart       | date                  |           |          |         |             | plain    |              |
	 year           | numeric               |           |          |         |             | main     |              |
	 rifcountry2011 | character varying(9)  |           |          |         |             | extended |              |
	 coa11          | character varying(9)  |           |          |         |             | extended |              |
	 gor11          | character varying(9)  |           |          |         |             | extended |              |
	 lad11          | character varying(9)  |           |          |         |             | extended |              |
	 lsoa11         | character varying(9)  |           |          |         |             | extended |              |
	 msoa11         | character varying(9)  |           |          |         |             | extended |              |
	Server: orcl
	FDW options: (schema 'RIF', "table" 'RIF_201617_APR2019', readonly 'true')  
  ```

* In Postgres as the end user (*peter*) check access to the table

	```
	\timing

	EXPLAIN VERBOSE SELECT year, COUNT(*) AS total 
	  FROM rif40.rif_201617_apr2019
	 GROUP BY year 
	 ORDER BY year;

										  QUERY PLAN                                                                                                                                                                                               
	-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 GroupAggregate  (cost=10000.00..20005.01 rows=1 width=40)
	   Output: year, count(year)
	   Group Key: rif_201617_apr2019.year
	   ->  Foreign Scan on rif40.rif_201617_apr2019  (cost=10000.00..20000.00 rows=1000 width=32)
			 Output: sahsu_id, age_sex_group, extract_hesid, diag_01, diag_02, diag_03, diag_04, diag_05, diag_06, diag_07, diag_08, diag_09, diag_10, diag_11, diag_12, diag_13, diag_14, diag_15, diag_16, diag_17, diag_18, diag_19, diag_20, opertn_01, opertn_02, opertn_03, opertn_04, opertn_05, opertn_06, opertn_07, opertn_08, opertn_09, opertn_10, opertn_11, opertn_12, opertn_13, opertn_14, opertn_15, opertn_16, opertn_17, opertn_18, opertn_19, opertn_20, opertn_21, opertn_22, opertn_23, opertn_24, epistart, year, rifcountry2011, coa11, gor11, lad11, lsoa11, msoa11
			 Oracle query: SELECT /*5f97a9d8ac019cfe71bac2fa042bb7a1*/ r1."YEAR" FROM "RIF"."RIF_201617_APR2019" r1 ORDER BY r1."YEAR" ASC NULLS LAST
			 Oracle plan: SELECT STATEMENT
			 Oracle plan:   BITMAP CONVERSION TO ROWIDS
			 Oracle plan:     BITMAP INDEX FULL SCAN RIF_201617_APR2019_YR
	(9 rows)
	 
	SELECT year, COUNT(year) AS total 
	  FROM rif40.rif_201617_apr2019
	 GROUP BY year 
	 ORDER BY year;
	 year |  total
	------+----------
	 1978 |        1
	 1983 |        1
	 1985 |        1
	 1987 |        1
	 1994 |        2
	 1996 |        1
	 1999 |        2
	 2000 |        1
	 2001 |        1
	 2003 |        2
	 2004 |        1
	 2005 |       11
	 2006 |        8
	 2007 |       16
	 2008 |       13
	 2009 |       49
	 2010 |       62
	 2011 |       72
	 2012 |      172
	 2013 |      375
	 2014 |      924
	 2015 |     5777
	 2016 | 15227773
	 2017 | 20395164
	 2018 |  4993288
	(25 rows)
	 
	``` 

* In Postgres as the end user (*peter*) test the linkage to EWS2011 geography:
  ```
  CREATE TABLE rif_201617_apr2019_geolink AS
  SELECT rifcountry2011, coa11, gor11, lad11, lsoa11, msoa11, COUNT(rifcountry2011) AS total 
    FROM rif40.rif_201617_apr2019
   GROUP BY rifcountry2011, coa11, gor11, lad11, lsoa11, msoa11;
  SELECT 192414
  Time: 814798.213 ms (13:34.798)
  
  SELECT COUNT(a.coa11) AS total, COUNT(b.coa2011) AS linked_total
    FROM rif_201617_apr2019_geolink a
		LEFT OUTER JOIN rif_data.hierarchy_ews2011 b ON (a.coa11 = b.coa2011);
   total  | linked_total
  --------+--------------
   192413 |       192410
  (1 row)  
  
  SELECT coa11 FROM rif_201617_apr2019_geolink
  EXCEPT
  SELECT coa2011 FROM rif_data.hierarchy_ews2011
  ORDER BY 1;
       coa11
   -----------
   E00014344
   S00103709
   S00116416

  (4 rows)
  ```
* This shows there are three junk COA2011 codes in the SAHSU HES data (probably new codes created after the census); and the performance of the remote 
  link is such that it must be materialized:
  * *oracle_fdw* uses the remote indexes but brings backs all the columns for all the rows requested; not the column required;
  * Aggregation is done locally;
  * Queries on tables that only return a few rows where **ALL** the predicates are indexed will be fast;
  * Aggregation queries (typical for the RIF) will always be slow because all the columns are returned;

* In Postgres as the end user (*peter*) materialize a LOCAL COPY of the HES data:
  ```
  CREATE MATERIALIZED VIEW peter.hes_201617_apr2019
  AS 
  SELECT sahsu_id, 
		 b.coa2011, b.lsoa2011, b.msoa2011, b.ladua2011, b.gor2011, b.scntry2011, b.cntry2011, 
		 age_sex_group, extract_hesid, 
         diag_01, diag_02, diag_03, diag_04, diag_05, diag_06, diag_07, diag_08, diag_09, diag_10, diag_11, diag_12, diag_13, diag_14, diag_15, diag_16, diag_17, diag_18, diag_19, diag_20, 
		 opertn_01, opertn_02, opertn_03, opertn_04, opertn_05, opertn_06, opertn_07, opertn_08, opertn_09, opertn_10, opertn_11, opertn_12, opertn_13, opertn_14, opertn_15, opertn_16, opertn_17, opertn_18, opertn_19, opertn_20, opertn_21, opertn_22, opertn_23, opertn_24, 
		 epistart, year, coa11
    FROM rif40.rif_201617_apr2019 a
		LEFT OUTER JOIN rif_data.hierarchy_ews2011 b ON (a.coa11 = b.coa2011)
  ORDER BY 1;
  SELECT 40623718
  Time: 2320353.069 ms (38:40.353)
  ```  
  This can be refreshed by ```REFRESH MATERIALIZED VIEW peter.hes_201617_apr2019;```.
* In Postgres as the end user (*peter*) index materialized view and analyze:
  ``` 
  CREATE UNIQUE INDEX hes_201617_apr2019_uk ON peter.hes_201617_apr2019(sahsu_id);
  CREATE INDEX hes_201617_apr2019_yr ON peter.hes_201617_apr2019(year);
  CREATE INDEX hes_201617_apr2019_asg ON peter.hes_201617_apr2019(age_sex_group);
  CREATE INDEX hes_201617_apr2019_rifcountry2011 ON peter.hes_201617_apr2019(rifcountry2011);
  CREATE INDEX hes_201617_apr2019_coa11 ON peter.hes_201617_apr2019(coa2011);
  CREATE INDEX hes_201617_apr2019_gor11 ON peter.hes_201617_apr2019(gor2011);
  CREATE INDEX hes_201617_apr2019_lad11 ON peter.hes_201617_apr2019(lad2011);
  CREATE INDEX hes_201617_apr2019_lsoa11 ON peter.hes_201617_apr2019(lsoa2011);
  CREATE INDEX hes_201617_apr2019_msoa11 ON peter.hes_201617_apr2019(msoa2011);
  CREATE INDEX hes_201617_apr2019_diag_01 ON peter.hes_201617_apr2019(diag_01);
  
  ANALYZE VERBOSE peter.hes_201617_apr2019;
  
  -- Data MUST appear in the rif_data schema and be a view
  CREATE OR REPLACE VIEW rif_data.v_hes_201617_apr2019
  AS SELECT * FROM peter.hes_201617_apr2019;
  
  EXPLAIN VERBOSE SELECT year, 
                         COUNT(year) AS total, COUNT(coa2011) AS total_coa2011, COUNT(coa11) AS total_coa, COUNT(coa11) -  COUNT(coa2011) AS unlinked, 
						 COUNT(sahsu_id) AS total  
	 FROM peter.hes_201617_apr2019
	GROUP BY year 
	ORDER BY year;
                                                           QUERY PLAN                                                   
--------------------------------------------------------------------------------------------------------------------------------
 Finalize GroupAggregate  (cost=1431012.80..1431013.06 rows=5 width=45)
   Output: year, count(year), count(coa2011), count(coa11), (count(coa11) - count(coa2011)), count(sahsu_id)
   Group Key: hes_201617_apr2019.year
   ->  Sort  (cost=1431012.80..1431012.82 rows=10 width=37)
         Output: year, (PARTIAL count(year)), (PARTIAL count(coa2011)), (PARTIAL count(coa11)), (PARTIAL count(sahsu_id))
         Sort Key: hes_201617_apr2019.year
         ->  Gather  (cost=1431011.58..1431012.63 rows=10 width=37)
               Output: year, (PARTIAL count(year)), (PARTIAL count(coa2011)), (PARTIAL count(coa11)), (PARTIAL count(sahsu_id))
               Workers Planned: 2
               ->  Partial HashAggregate  (cost=1430011.58..1430011.63 rows=5 width=37)
                     Output: year, PARTIAL count(year), PARTIAL count(coa2011), PARTIAL count(coa11), PARTIAL count(sahsu_id)
                     Group Key: hes_201617_apr2019.year
                     ->  Parallel Seq Scan on peter.hes_201617_apr2019  (cost=0.00..1218508.48 rows=16920248 width=37)
                           Output: year, coa2011, coa11, sahsu_id
(14 rows)	
  SELECT year, 
		 COUNT(year) AS total, COUNT(coa2011) AS total_coa2011, COUNT(coa11) AS total_coa, COUNT(coa11) -  COUNT(coa2011) AS unlinked, 
		 COUNT(sahsu_id) AS total 
		 FROM peter.hes_201617_apr2019
	GROUP BY year 
	ORDER BY year;	
 year |  total   | total_coa2011 | total_coa | unlinked |  total
------+----------+---------------+-----------+----------+----------
 1978 |        1 |             0 |         0 |        0 |        1
 1983 |        1 |             1 |         1 |        0 |        1
 1985 |        1 |             1 |         1 |        0 |        1
 1987 |        1 |             1 |         1 |        0 |        1
 1994 |        2 |             2 |         2 |        0 |        2
 1996 |        1 |             1 |         1 |        0 |        1
 1999 |        2 |             2 |         2 |        0 |        2
 2000 |        1 |             1 |         1 |        0 |        1
 2001 |        1 |             1 |         1 |        0 |        1
 2003 |        2 |             2 |         2 |        0 |        2
 2004 |        1 |             1 |         1 |        0 |        1
 2005 |       11 |            11 |        11 |        0 |       11
 2006 |        8 |             8 |         8 |        0 |        8
 2007 |       16 |            16 |        16 |        0 |       16
 2008 |       13 |            13 |        13 |        0 |       13
 2009 |       49 |            49 |        49 |        0 |       49
 2010 |       62 |            62 |        62 |        0 |       62
 2011 |       72 |            72 |        72 |        0 |       72
 2012 |      172 |           172 |       172 |        0 |      172
 2013 |      375 |           375 |       375 |        0 |      375
 2014 |      924 |           924 |       924 |        0 |      924
 2015 |     5777 |          5777 |      5777 |        0 |     5777
 2016 | 15227773 |      15227707 |  15227773 |       66 | 15227773
 2017 | 20395164 |      20395112 |  20395164 |       52 | 20395164
 2018 |  4993288 |       4993280 |   4993288 |        8 |  4993288
(25 rows)


Time: 11739.462 ms (00:11.739)
  ```

* In Postgres as RIF40 setup materialized view as numerator  
  ``` 
  	
  
   INSERT INTO rif40.rif40_health_study_themes(theme , description) 
   VALUES ('HES', 'England Hospital Inpatients');
   
	INSERT INTO rif40.rif40_tables (
	   theme,
	   table_name,
	   description,
	   year_start,
	   year_stop,
	   total_field,
	   isindirectdenominator,
	   isdirectdenominator,
	   isnumerator,
	   automatic,
	   sex_field_name,
	   age_group_field_name,
	   age_sex_group_field_name,
	   age_group_id) 
	SELECT 
	   'HES',				/* theme */
	   'HES_201617_APR2019',	/* table_name */
	   'England 2011 Census boundaries HES Inpatients data 2016-2017',				/* description */
	   2016,				/* year_start */
	   2018,				/* year_stop */
	   NULL,				/* total_field */
	   0,					/* isindirectdenominator */
	   0,					/* isdirectdenominator */
	   1,					/* isnumerator */
	   1,					/* automatic */
	   NULL,				/* sex_field_name */
	   NULL,				/* age_group_field_name */
	   'AGE_SEX_GROUP',		/* age_sex_group_field_name */
	   1					/* age_group_id */;
	   
	INSERT INTO rif40.rif40_tables (
	   theme,
	   table_name,
	   description,
	   year_start,
	   year_stop,
	   total_field,
	   isindirectdenominator,
	   isdirectdenominator,
	   isnumerator,
	   automatic,
	   sex_field_name,
	   age_group_field_name,
	   age_sex_group_field_name,
	   age_group_id) 
	SELECT 
	   'HES',				/* theme */
	   'V_HES_201617_APR2019',	/* table_name */
	   'England 2011 Census boundaries HES Inpatients view 2016-2017',				/* description */
	   2016,				/* year_start */
	   2018,				/* year_stop */
	   NULL,				/* total_field */
	   0,					/* isindirectdenominator */
	   0,					/* isdirectdenominator */
	   1,					/* isnumerator */
	   1,					/* automatic */
	   NULL,				/* sex_field_name */
	   NULL,				/* age_group_field_name */
	   'AGE_SEX_GROUP',		/* age_sex_group_field_name */
	   1					/* age_group_id */;
	--
	-- Setup ICD field (UK_ICD_SAHSU_01) 
	--
	INSERT INTO rif40.rif40_outcome_groups(
	   outcome_type, outcome_group_name, outcome_group_description, field_name, multiple_field_count)
	SELECT
	   'ICD' AS outcome_type,
	   'HES_DIAG' AS outcome_group_name,
	   'UK diag_01' AS outcome_group_description,
	   'diag_01' AS field_name,
	   0 AS multiple_field_count
	WHERE NOT EXISTS (SELECT outcome_group_name FROM  rif40.rif40_outcome_groups WHERE outcome_group_name = 'HES_DIAG');

	INSERT INTO rif40.rif40_table_outcomes (
	   outcome_group_name,
	   numer_tab,
	   current_version_start_year) 
	SELECT 
	   'HES_DIAG',
	   'HES_201617_APR2019',
	   2016; 
  ```
* In Postgres as the end user (*peter*)   
  ```
  SELECT numerator_table, denominator_table, geography FROM rif40_num_denom;   numerator_table    | denominator_table  | geography
  ----------------------+--------------------+-----------
   COMARE_CANCER        | EWS2011_POPULATION | EWS2011
   EWS2011_CANCER       | EWS2011_POPULATION | EWS2011
   V_HES_201617_APR2019 | EWS2011_POPULATION | EWS2011
   NUM_SAHSULAND_CANCER | POP_SAHSULAND_POP  | SAHSULAND
   SEER_CANCER          | SEER_POPULATION    | USA_2014
  (5 rows)
    SELECT * FROM rif40_num_denom_errors
   WHERE numerator_table LIKE '%HES_201617_APR2019' 
     AND denominator_table = 'EWS2011_POPULATION' 
	 AND geography = 'EWS2011';
   geography | numerator_owner |   numerator_table    | is_numerator_resolvable | n_num_denom_validated |                    numerator_description                     | denominator_owner | denominator_table  | is_denominator_resolvable | d_num_denom_validated |       denominator_description        | automatic | auto_indirect_error_flag | auto_indirect_error | n_fdw_create_status | n_fdw_error_message | n_fdw_date_created | n_fdw_rowtest_passed
  -----------+-----------------+----------------------+-------------------------+-----------------------+--------------------------------------------------------------+-------------------+--------------------+---------------------------+-----------------------+--------------------------------------+-----------+--------------------------+---------------------+---------------------+---------------------+--------------------+----------------------
   EWS2011   | peter           | V_HES_201617_APR2019 |                       1 |                     1 | England 2011 Census boundaries HES Inpatients view 2016-2017 | rif_data          | EWS2011_POPULATION |                         1 |                     1 | UK 2011 Census Population 1981-2018. |         1 |                        0 |                     |                     |                     |                    |
   EWS2011   |                 | HES_201617_APR2019   |                       0 |                     0 | England 2011 Census boundaries HES Inpatients data 2016-2017 | rif_data          | EWS2011_POPULATION |                         1 |                     1 | UK 2011 Census Population 1981-2018. |         1 |                        0 |                     |                     |                     |                    |
  (2 rows)	 
  ```
  As can be seen, the view is resolvable, the materialized view is not. Cursor *c2* in *rif40_sql_pkg.rif40_is_object_resolvable()* needs to have 
  support for materialized view added:
  ```
  c2 CURSOR(l_schema VARCHAR, l_table VARCHAR) FOR
		SELECT schemaname, tablename
		  FROM pg_tables
		 WHERE schemaname = l_schema
		   AND tablename  = LOWER(l_table)
		 UNION
		SELECT schemaname, viewname AS tablename
		  FROM pg_views
		 WHERE schemaname = l_schema
		   AND viewname   = LOWER(l_table)
	 	 UNION
		SELECT n.nspname schemaname, a.relname tablename				/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE b.ftrelid = a.oid
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND n.nspname  = USER
		   AND n.nspname  = l_schema
		   AND a.relname  = LOWER(l_table);
  ```

Bug, when using multiple health outcomes. probably fixed
```  
XML Parsing Error: no element found
Location: https://localhost:8080/rifServices/studySubmission/getAgeGroups?userID=peter&geographyName=EWS2011&numeratorTableName=[object%20Object]
Line Number 1, Column 1: getAgeGroups:1:1
XML Parsing Error: no element found
Location: https://localhost:8080/rifServices/studySubmission/getYearRange?userID=peter&geographyName=EWS2011&numeratorTableName=[object%20Object]
Line Number 1, Column 1: getYearRange:1:1 
```