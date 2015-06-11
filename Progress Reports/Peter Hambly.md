# Peter Hambly Progress Report RIF 4.0

Principal Work Area: Postgres Database

## 2015
### May
#### 10th-15 May

Alter script #7

Support for taxonomies/ontologies (e.g. ICD9, 10); removed previous table based support.
Modify t_rif40_inv_conditions to remove SQL injection risk

Done:

* rif40_outcomes - list of ontologies to remain - remove all field except for:
	outcome_type, outcome_description, current_version, current_sub_version, previous_version
* Add new outcome_group to rif40_outcome_groups for SAHUSLAND_CANCER
* Fix rif40_tables, rif40_table_outcomes, rif40_outcome_groups join for SAHUSLAND_CANCER
  - Add view: rif40_numerator_outcome_columns
  - Add checks: to rif40_inv_conditions, rif40_num_denom, rif40_num_denom_errors
* Drop existing ontology tables (keep icd9/10 until new ontology middleware is ready)
* Modify t_rif40_inv_conditions to remove SQL injection risk:
  - Rename column condition to min_condition
  - Add columns: max_condition, predefined_group_name, outcome_group_name
  - Add foreign key constraint on rif40_predefined_groups(predefined_group_name)
  - Add foreign key constraint on rif40_outcome_groups(outcome_group_name)
  - Add check constraints: 
    1. min_condition or predefined_group_name
    2. max_condition may be null, but if set != min_condition
* Rebuild rif40_inv_conditions:
  - Add back condition, derive from: min_condition, max_condition, predefined_group_name, outcome_group_name
  - Add numer_tab, field_name, column_exists and column_comments fields for enhanced information
* Load new rif40_create_disease_mapping_example()
 
#### 18th-22nd May

* rif40_GetMapAreas() fix - the following query works at LEVEL4 but returns NULL at other levels

```sql
SELECT SUBSTRING(rif40_xml_pkg.rif40_GetMapAreas(
			'SAHSU' 	/* Geography */, 
			'LEVEL3' 	/* geolevel view */, 
			55.5268097::REAL /* y_max */, -4.88653803 /* x_max */, 52.6875343 /* y_min */, -7.58829451 /* x_min - Bounding box - from cte */)::Text
				FROM 1 FOR 160) AS json 
LIMIT 4;
```

* topojson_convert.js - add name, gid and area_id as properties; validated link

#### 26th-29th May

Alter script #7 continued.

Clean up rif40_columns, rif40_tables_and_views, rif40_triggers. This is so the DDL tests now pass

Done:

* DDL test suite fixes:

	1. Tidy up RIF40_TABLES_AND_VIEWS table_or_view column, drop column TABLE_OR_VIEW_NAME_HREF, add primary key and check constraint
	2. Tidy up RIF40_COLUMNS, drop columns: TABLE_OR_VIEW_NAME_HREF, COLUMN_NAME_HREF; add primary and foreign key and check constraint 
	3. Remove existing ontology tables (keep icd9/10 until new ontology middleware is ready)
	4. Remove columns dropped from RIF40_OUTCOMES and T_RIF40_INV_CONDITIONS
	5. Add new view: RIF40_NUMERATOR_OUTCOME_COLUMNS 
	6. Add new columns for: RIF40_INV_CONDITIONS and T_RIF40_INV_CONDITIONS
    7. RIF40_OUTCOMES trigger removed from rif40_triggers
    8. Add primary and foreign keys to rif40_triggers
	
* Regression tested OK. Still expecting some problems on wpea-rif1. Still no support for old studies
* Improved make targets - made database aware
* RIF startup procedure: added application name support, no check mode (for middleware testing) 

#### 1st-5th June

* Full regression test of database build
* Port to MacOS, regression test
* Install Tomcat 8 on Windows, MacOS, Tomcat 6 on Redhat Enterprise Linux 5. Checked Tomcat 6 will work.
* Started work on unified build using make in rifBuild tree
* Tested Middleware WAR build from github, dependency issues found 

#### 8th-12th June

* Middleware WAR build now OK (mainly thanks to Kev)
* Integrated with make using Powershell on Windows
* Install.ps1 created that gains the elevated privileges required. Install RIF middleware and test WAR unpack.

#### 15th-19th June

Immediate TODO list: 

* Improve test harness to check all return data, build trigger test harness to test for error conditions
* Modify alter 7 so that existing conditions become rif40_error(...); /* Existing condition */ e.g.

SELECT 1 AS x
 WHERE rif40_error(-20199, 'Dummy error: %', 'Test'::VARCHAR);
 
Raises an exception!

* Fix pernicious mixed content issues in JS frontend
* Test wpea-darwin remotely; using JS on httpd (i.e. minimise reverse proxy traffic) 

Next few weeks:

* RIF batch integration
* Complete R integration
* Build and integrate node middleware server:
  * GeoJSON to TopoJSON conversion; converted node program to using HTTP POST methods
  * Secure logons using session_ids, time stamps and eliptic curve cryptography (public/private keys)
  * Shapefile conversion to WKT (Well known test) format later 
* Secure wpea-darwin httpd by jailing it; re-penetration test; add caching. Shields to be lowered when 
  secure logons are integrated into the middleware and the JS frontend