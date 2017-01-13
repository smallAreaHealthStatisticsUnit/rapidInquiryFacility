/*
 * ==============================================================================
 * HEALTH CODE FREQUENCY ANALYSIS SUITE 
 *    by Kevin Garwood
 * ====================================
 * This program tries shows an example of applying simple database operations 
 * to analyse the frequency with which health and operation codes appear alone 
 * and in combination within routinely collected hospital records.  The 
 * inspiration for this idea comes both from my own experience dealing with 
 * routinely collected health data and from taking Imperial College's Short 
 * Course on Big Data in Health.  Here we don't use real data but data sets that 
 * can be roughly modelled on the kinds of data you would find in records found 
 * in the UK's Hospital Episode Statistics (HES).  The goal of this script is to 
 * show how applying some basic informatics analysis can help researchers and 
 * hospital administrators do something very practical: to study the trends in 
 * the way health codes appear co-located within the same health record that 
 * charts a patient's journey through the healthcare system.
 *
 * This script is released as part of a wider body of work called the Rapid 
 * Inquiry Facility(RIF), and we hope its users may benefit from this tool to build 
 * taxonomy services that relate terms based on the frequency with which they 
 * appear in practice rather than how their concepts are related with respect 
 * to one another.  
 *
 * The RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see http://www.gnu.org/licenses.
 */

/*
 * To run this demo:
 * -----------------
 * (1) Download and install PostgreSQL v9.3 or later.
 * (2) Create a blank database (eg: 'healthcare_db')
 * (3) Jump ahead in the script to where you see #RUN_PROGRAM
 *     Uncomment one of the code lines indicated in the
 *     instructions shown there.
 * (3) You can run it in one of two ways: command line or 
 *     using PostgreSQL's built-in pgAdmin tool.  
 *     PostgreSQL so you can run it from command line
 * Command:
 * psql -U postgres -d healthcare_db 
 * -f C:\test_icd\HealthCodeFrequencyAnalyser.sql
 * 
 * or
 * 
 * (4) Open pgadmin and open the database you created 
 *     (eg: double-click on 'healthcare_db' you created).
 * (5) Click on the "Tools" menu, then "Query Tool".  You should see a dialog
 *     appear.  Copy and paste all of this script into the window and
 *     click the "Query" menu item, followed by "Execute".
 */

/*
 * Overview of Patient Record Data Set we Imagine
 * ==============================================
 * One of my goals here is not to show you clever code, but to show you very 
 * basic code that can be used to support a practical purpose.  We begin with 
 * an understanding of our example data set, which is a very simple example 
 * based on data sets such as HES.  If you want to see how closely the example 
 * models a more realistic example, look up the "HES Inpatient Data Dictionary".
 * You can find it at:
 * 
 * The HES data dictionary defines dozens of fields that may appear in a single 
 * episode of health care for a patient.  Although there is a lot of information 
 * in the records, our example here is only concerned with the following 
 * concepts:
 * - date of event: (eg: admidate, or a year field may be good enough)
 * - health care provider: (eg: procodet, or other fields which refer to 
 *   organisation or 
 *   administrative area)
 * - health codes: (eg: ICD codes described in columns diag_01 to diag_20 
 *   inclusively)
 * - operation codes (eg: OPCS operation codes oper_01 to oper_24 inclusively)
 *
 * That's it! Here we don't even care about the patient's identifier or 
 * demographics.  Our concern is limited to analysing how codes (the health and 
 * operation codes) are collocated with another and how frequently they appear.
 *
 * In our example, we're going to just have these fields:
 * - year: the year of the health episode
 * - healthcare_provider: a code that represents some kind of healthcare 
 *   facility. Assume this is an 
 *   organisation of some kind.
 * - diag_01, diag_02, diag_03: to simulate the first few diagnostic codes you 
 *   would find in a real
 *   data dump of episode records.
 * - oper_01, oper_02: operation codes that describe what kind of operations 
 *   were 
 *   done to a patient.
 *
 * Our "patient_records" table will look like this:
 *
 * year health_care_provider diag_01 diag_02 diag_03 oper_01 oper_02
 * ...  ...                  ...     ...     ...     ...     ...
 */

/*
 * The Results We're Trying to Get
 * ===============================
 * In this section, we go over what we want to achieve from the program.
 * Our incoming table will look like this:
 * Table: "patient_records"
 * year health_care_provider diag_01 diag_02 diag_03 oper_01 oper_02
 * ...  ...                  ...     ...     ...     ...     ...
 *
 * At a minimum, we want to produce the following tables, which are ordered 
 * by decreasing frequency.
 *
 * 1. Frequency of codes, regardless of year or healthcare provider. Ordered 
 *    by descending frequency
 * -------------------------------------------------------------------------
 * Table: code_frequencies_general
 * code frequency
 * ...  ...
 *
 * 2. Frequency of codes based on year. Ordered first by descending 
 *    year, then descending frequency.
 * ----------------------------------------------------------------
 * Table: code_frequencies_year
 * year code num_occurrences
 * ...  ...  ...
 * 
 * 3. Frequency of codes based on healthcare provider. Ordered first by 
 *    ascending healthcare, then by descending frequency.
 * --------------------------------------------------------------------
 * Table: code_frequencies_provider
 * health_care_provider code frequency
 * ...                  ...  ... 
 * 
 * 4. Frequency of codes based on year and healthcare provider.  Ordered 
 *    first by ascending healthcare provider, second by descending year 
 *    and third by descending frequency
 * --------------------------------------------------------------------
 * Table: code_frequencies_provider_year
 * healthcare_provider year frequency
 * ...                 ...  ...
 * 
 * 5. Frequency of co-located codes in general.  Ordered by ascending 
 *    code and decreasing frequency.
 * -----------------------------------------------------------------------
 * Table: coloc_code_general
 * code other_code frequency 
 * ...  ...        ...
 *
 * 6. Frequency of co-located codes by year.  Ordered by descending year,
 *    then descending frequency
 * ----------------------------------------------------------------------
 * Table: coloc_code_year
 * year code other_code frequency
 * ...  ...  ...        ...
 * 
 * 7. Frequency of co-located codes by healthcare provider. Ordered by
 * ascending healthcare provider, then decreasing frequency.
 * -------------------------------------------------------------------
 * Table: coloc_code_provider
 * healthcare_provider code other_code frequency
 * ...                 ...  ...        ...
 *
 * 8. Frequency of co-located codes by healthcare provider and year.  
 *    Ordered by ascending healthcare provider, then by descending year, then 
 *    by descending frequency.
 * --------------------------------------------------------------------------
 * Table: coloc_provider_year
 * healthcare_provider year code other_code frequency
 * ...                 ...  ...  ...        ...
 */
 
/*
 * Overview of Algorithm for Creating Code Frequency Analyses
 * ==========================================================
 * For many, this algorithm will not appear to be anything advanced or
 * sophisticated. It also hasn't necessarily been designed with performance
 * in mind.  However, it is simple and therefore amenable to being adapted
 * by others using basic SQL skills and free open source tools.
 *
 * Step 1: Obtain patient record data in table with expected fields.
 *
 * Step 2: In one table, create a collection of codes that appear anywhere
 *         in the coding fields.  It will likely contain duplicate records
 *         and have the following fields:
 *         year healthcare_provider code
 * 
 * Step 3: Create reports for frequency analysis of individual codes.  For
 *         this part of the analysis, we don't care what other terms appear
 *         beside them and in some cases they may appear alone.
 * 
 * Step 4: Begin work to support co-located term analysis. Create a new table
 *         which removes the duplicates in Step 2, to produce a table of 
 *         unique codes.  It will unique row entries that have the fields:
 *         year, healthcare_provider, code.
 *
 * Step 5: In another table, merge all of the coding fields into a single 
 *         text phrase, where terms are separated by a delimiter (eg: comma).
 * 
 * Step 6: Combine the table in Step 2 with the table in Step 3 based on year,
 *         healthcare provider, and whether a unique term in Step 2 appears in 
 *         a single text phrase in Step 3.  We will have a table with these 
 *         fields:
 *         year, health_provider, code, single_coding_phrase.
 *
 * Step 7: Using the combined table from Step 4, explode the single text 
 *         phrase back into individual terms and make a row for each combination 
 *         of a unique code and one of the terms.  We will now have a table that
 *         has the fields year, health_provider, code, other_code.  'code'
 *         comes from 'code' in Step 4 and each value of other_code will have 
 *         been parsed from a single_coding_phrase.
 * 
 * Step 8: Remove occurrences where code appears by an empty other_code entry.
 * 
 * Step 9: Remove occurrences where code and other_code are identical.  By now
 *         we will have a table that serves as the basis for all other analyses
 *         relating to co-located codes. It will have the fields: 
 *         year health_provider code other_code.
 *
 * Step 10: Aggregate rows in Table 7 to suit different reports about 
 *          frequencies of co-located codes.
 *
 * The steps will be described in the following sections
 */

/*
 * =============================================================================
 * Step 1: Obtaining the Example Patient Data Set (Hard Coded Values)
 * ------------------------------------------------------------------
 * The routines in this section create the example patient record data set, 
 * either through adding hard-coded data found in this script or by importing 
 * the data from CSV files.  The program that does frequency analysis does not 
 * attach meaning to any of the codes and even if it did, it probably shouldn't. 
 * Data entry habits can vary depending on year, the practices of healthcare 
 * providers or the preferences of individual hospital staff members.  From the 
 * program's point of view, they're all just codes and it doesn't care why one 
 * code is related to another, or whether those combinations even make sense 
 * from a healthcare or epidemiological perspective.
 * =============================================================================
*/

-- Create the patient record table by using hard-coded record data that are
-- added to it through individual insertion statements.  We use this by default 
-- in case you don't have any CSV files you want to import.
CREATE OR REPLACE FUNCTION create_hard_coded_patient_records()
	RETURNS void AS 
$$
DECLARE
	

BEGIN

	DROP TABLE IF EXISTS patient_records;
	CREATE TABLE patient_records (
	   patient_id TEXT,
	   year INT,
	   healthcare_provider TEXT,
	   diag_01 TEXT,
	   diag_02 TEXT,
	   diag_03 TEXT,
	   oper_01 TEXT,
	   oper_02 TEXT
	);

	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2001, 'hospitalA', '302', '405', '406', null, null);
	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2001, 'hospitalA', '405', '408', '412', 'AAB', 'QJQ');
	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2002, 'hospitalA', '405', null, null, null, null);
	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2002, 'hospitalA', '252', '392', null, null, null);
	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2003, 'hospitalA', '152', null, null, null, null);
	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2004, 'hospitalA', '666', '392', '102', null, null);
	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2004, 'hospitalA', '454', null, null, null, null);

	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2004, 'hospitalA', '405', '408', '666', null, null);

	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2005, 'hospitalA', '679', '777', null, null, null);

	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2006, 'hospitalA', '754', '755', '777', null, null);

	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2006, 'hospitalA', '754', '755', '777', null, null);

	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2006, 'hospitalA', '754', '755', '777', null, null);

	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2006, 'hospitalA', '75', '74', '73', null, null);

	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2001, 'hospitalB', '302', '405', '406', null, null);
	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2001, 'hospitalB', '405', '408', '412', 'KTX', 'LPR');
	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2002, 'hospitalB', '405', null, null, null, null);
	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2002, 'hospitalB', '252', '392', null, null, null);
	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2003, 'hospitalB', '152', null, null, null, null);
	
	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2003, 'hospitalC', '666', '392', '102', null, null);
	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2003, 'hospitalC', '454', null, null, null, null);

	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2004, 'hospitalD', '405', '408', '666', null, null);

	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2005, 'hospitalD', '679', '777', null, null, null);

	INSERT INTO patient_records(year, healthcare_provider, diag_01,
		diag_02, diag_03, oper_01, oper_02) 
	VALUES (2006, 'hospitalD', '754', '755', '777', null, null);



END;

$$   LANGUAGE plpgsql;


/*
 * ============================================================================
 * Step 1: Obtaining the Example Patient Data Set (Loaded from CSV File)
 * ---------------------------------------------------------------------
 * We present two alternative ways of doing Step 1.  We can either use hard 
 * coded values, or we can load data from a CSV file. Hard coding patient 
 * records is useful if we just want to run the program without having to 
 * create or locate CSV data files.  But in practice, the patient records would 
 * almost certainly be imported from some form of CSV file.
 * ============================================================================
*/
CREATE OR REPLACE FUNCTION load_patient_records_from_csv_file(
	patient_record_file TEXT)
	RETURNS void AS 
$$
DECLARE

BEGIN

	DROP TABLE IF EXISTS patient_records;
	CREATE TABLE patient_records (
	   patient_id TEXT,
	   year INT,
	   healthcare_provider TEXT,
	   diag_01 TEXT,
	   diag_02 TEXT,
	   diag_03 TEXT,
	   oper_01 TEXT,
	   oper_02 TEXT
	);

	EXECUTE format ('
	COPY patient_records (	
		patient_id,
		year,
		healthcare_provider,
		diag_01,
		diag_02,
		diag_03,
		oper_01,
		oper_02) 
	FROM 
		%L
	(FORMAT CSV, HEADER)', patient_record_file);

END;

$$   LANGUAGE plpgsql;

--SELECT "load_csv_file"('C:\test', 'test_patient_data_file.csv');


/*
 * ============================================================================
 * Step 2: Collect all coding field values. Perform single code frequency 
 * analyses
 * ----------------------------------------------------------------------------
 * We begin the main part of processing by gathering all the code values across
 * all coding fields into a single table 'all_codes_with_duplicates', that 
 * contains the following fields:
 * 
 * year healthcare_provider code
 * ...  ...                 ...
 *
 * This table will contain duplicate entries and these will be used to assess 
 * the frequency with which codes appear at all in the patient_records table. 
 * Given this table, various result tables will be generated
 * ============================================================================
*/
CREATE OR REPLACE FUNCTION combine_all_coding_values()
	RETURNS void AS 
$$
DECLARE
	test_patient_data_file TEXT;

BEGIN
	
	-- The following code is a big union statement that searches
	-- each coding field for non-null field values and collects 
	-- them into a single table with the fields:
	--
	-- year healthcare_provider code
	-- ...  ...                 ...

	-- #ADAPT_CODE: This is one of the few parts of the code you will
	-- have to adapt.  Use the format here to work for whatever 
	-- diagnostic and/or operation codes that interest you.  In 
	-- something like HES, you would need diag_01 ... diag_20 and
	-- oper_01 ... oper_20.  If you're working with a data extract
	-- from a specific hospital, you may not find you need as many
	-- fields.
	DROP TABLE IF EXISTS all_codes_with_duplicates;
	CREATE TABLE all_codes_with_duplicates AS
	SELECT DISTINCT
		year,
		healthcare_provider,		
		diag_01 AS code
	FROM
		patient_records
	WHERE
		diag_01 IS NOT NULL
	UNION
	SELECT DISTINCT
		year,
		healthcare_provider,		
		diag_02 AS code
	FROM
		patient_records
	WHERE
		diag_02 IS NOT NULL
	UNION		
	SELECT DISTINCT
		year,
		healthcare_provider,		
		diag_03 AS code
	FROM
		patient_records
	WHERE
		diag_03 IS NOT NULL		
	UNION		
	SELECT DISTINCT
		year,
		healthcare_provider,		
		oper_01 AS code
	FROM
		patient_records
	WHERE
		oper_01 IS NOT NULL		
	UNION		
	SELECT DISTINCT
		year,
		healthcare_provider,		
		oper_02 AS code
	FROM
		patient_records
	WHERE
		oper_02 IS NOT NULL;
   	
   	DROP INDEX IF EXISTS ind_tmp_all_codes_duplicates;   	
	CREATE INDEX  ind_tmp_all_codes_duplicates ON 
		all_codes_with_duplicates(year, healthcare_provider, code);
		

END;

$$   LANGUAGE plpgsql;


-- Creates reports based on counting duplicate code values based on year,
-- healthcare provider, or a combination of the two.  If the output_directory
-- is not null, it attempts to export the reports to CSV files.
CREATE OR REPLACE FUNCTION run_single_code_analyses(output_directory TEXT)
	RETURNS void AS 
$$
DECLARE

	date_phrase TEXT;	
	results_single_code_provider_year_file TEXT;
	results_single_code_provider_file TEXT;
	results_single_code_year_file TEXT;		
	
BEGIN
		
	--Here we create the first of our 'final' tables that will
	--be used to produce analyses	
	DROP TABLE IF EXISTS final_single_code_provider_year;
	CREATE TABLE final_single_code_provider_year AS
	SELECT
		healthcare_provider,
		year,
		code,
		COUNT(code) AS frequency
	FROM
		all_codes_with_duplicates
	GROUP BY
		healthcare_provider,
		year,
		code
	ORDER BY
		healthcare_provider ASC,
		year DESC,
		COUNT(code) DESC;

	-- Adding a primary key here isn't necessary but I use it more as a 
	-- convention that can help catch simple errors. It's more important to 
	-- know when it fails rather than when it succeeds.
   	ALTER TABLE final_single_code_provider_year 
   		ADD PRIMARY KEY (healthcare_provider, year, code);

	DROP TABLE IF EXISTS final_single_code_provider;
	CREATE TABLE final_single_code_provider AS
	SELECT
		healthcare_provider,
		code,
		COUNT(code) AS frequency
	FROM
		all_codes_with_duplicates
	GROUP BY
		healthcare_provider,
		code
	ORDER BY
		healthcare_provider ASC,
		COUNT(code) DESC;
	
   	ALTER TABLE final_single_code_provider 
   		ADD PRIMARY KEY (healthcare_provider, code);
	
	DROP TABLE IF EXISTS final_single_code_year;
	CREATE TABLE final_single_code_year AS
	SELECT
		year,
		code,
		COUNT(code) AS frequency
	FROM
		all_codes_with_duplicates
	GROUP BY
		year,
		code
	ORDER BY
		year DESC,
		COUNT(code) DESC;
   	ALTER TABLE final_single_code_year ADD PRIMARY KEY (year, code);

	--Optionally write these tables to CSV files
	IF output_directory IS NOT NULL THEN
		RAISE NOTICE 'Exporting single code frequency analysis reports';
		
		date_phrase :=
			(SELECT to_char(current_timestamp, 'YYYY-Mon-DD-HH24-MI'));

		-- Writing out the single code frequency analysis by
		-- provider and year
		results_single_code_provider_year_file :=
			output_directory ||
			'\results_single_code_provider_year' ||
			date_phrase ||
			'.csv';			
		EXECUTE format ('
		COPY final_single_code_provider_year
		TO
			%L
		(FORMAT CSV, HEADER)', 
		results_single_code_provider_year_file);			

		-- Writing out the single code frequency analysis by
		-- provider
		results_single_code_provider_file :=
			output_directory ||
			'\results_single_code_provider' ||
			date_phrase ||
			'.csv';			
		EXECUTE format ('
		COPY final_single_code_provider
		TO
			%L
		(FORMAT CSV, HEADER)', 
		results_single_code_provider_file);			

		-- Writing out the single code frequency analysis by
		-- year
		results_single_code_year_file :=
			output_directory ||
			'\results_single_code_year' ||
			date_phrase ||
			'.csv';			
		EXECUTE format ('
		COPY final_single_code_year
		TO
			%L
		(FORMAT CSV, HEADER)', 
		results_single_code_year_file);			
	
	END IF;
	
END;

$$   LANGUAGE plpgsql;

--Testing...
--SELECT "run_single_code_analyses"()



/*
 * ===========================================================================
 * Part III: Merge Coding Fields into a Single Delimited and Searchable Phrase
 * ---------------------------------------------------------------------------
 * In the previous part we created a collection of unique codes that appear 
 * anywhere in any of the coding fields (ie: diag_01, diag_02, diag_03, 
 * oper_01, oper_02). In this part, we combine all of the terms into a single 
 * phrase.  
 * So a line like year healthcare_provider diag_01 diag_02 diag_03 oper_01 
 * oper_02
 * ...  ...                 ...     ...     ...     ...     ...
 * 2009 XYZ                 345     346     353     B101    B102
 *
 * would end up in another table looking like:
 * year healthcare_provider whole_coding_phrase
 * ...  ...                 ...
 * 2009 XYZ                 ,345,346,353,B101,B102
 *
 * The reason we're doing this is to prepare a data field where we can easily 
 * try to find one of the unique terms in the phrase of all terms combined.
 * ===========================================================================
*/

-- We're using this routine to help us condense the meaningful contributions of 
-- coding fields, ignoring empty field values.  In practice, the lower the code
-- number, the more frequently it will have a value in a health record.  For
-- example, in HES records diag_01 will be populated far far more than diag_20.
-- The same trend often appears in operation codes.  This the first part of 
-- dealing with the sparse matrix problem: extracting the field values that 
-- matter from a matrix which may leave a lot of entries blank.
CREATE OR REPLACE FUNCTION combine_codes(
	diag_01 TEXT,
	diag_02 TEXT,
	diag_03 TEXT,
	oper_01 TEXT,
	oper_02 TEXT)
	RETURNS TEXT AS 
$$
DECLARE
	concatenated_result TEXT;

BEGIN
	
	concatenated_result := ',';
	
	IF diag_01 IS NOT NULL THEN
		concatenated_result := concatenated_result || diag_01 || ',';
	END IF;
		
	IF diag_02 IS NOT NULL THEN
		concatenated_result := concatenated_result || diag_02 || ',';
	END IF;

	IF diag_03 IS NOT NULL THEN
		concatenated_result := concatenated_result || diag_03 || ',';
	END IF;

	IF oper_01 IS NOT NULL THEN
		concatenated_result := concatenated_result || oper_01 || ',';
	END IF;

	IF oper_02 IS NOT NULL THEN
		concatenated_result := concatenated_result || oper_02 || ',';
	END IF;

	RETURN concatenated_result;
END;

$$   LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_colocated_code_frequencies()
	RETURNS void AS 
$$
DECLARE
	test_patient_data_file TEXT;

BEGIN

	-- We want to create a collection of unique coding values and then
	-- try to find them in a table where all the coding values in each
	-- patient record have been merged into a single phrase.

	-- Reduce the number of columns we care about in the coding value matrix...
	
	-- #ADAPT_CODE: This is the last part of the code you have to adapt.
	-- You may end up having to modify combine_codes with far more fields
	-- but once you've done that, from now on the algorithm doesn't care
	-- how many coding fields were used or what they were called in 
	-- patient_records.
	DROP TABLE IF EXISTS concatenated_coding_fields;
	CREATE TABLE concatenated_coding_fields AS
	SELECT
		year,
		healthcare_provider,
		combine_codes(
			diag_01, 
			diag_02, 
			diag_03, 
			oper_01, 
			oper_02) AS whole_coding_phrase
	FROM
		patient_records;
	CREATE INDEX  ind_tmp_concat_fields ON 
		concatenated_coding_fields(year, healthcare_provider, whole_coding_phrase);

	-- Reduce the number of rows we care about in the coding value matrix...
	
	-- create a collection of unique coding terms.  We do this by using
	-- the 'DISTINCT' SQL phrase.	
	DROP TABLE IF EXISTS unique_coding_values;
	CREATE TABLE unique_coding_values AS
	SELECT DISTINCT
		year,
		healthcare_provider,		
		code
	FROM
		all_codes_with_duplicates;
   	ALTER TABLE unique_coding_values 
   		ADD PRIMARY KEY (year, healthcare_provider, code);

	-- Now try to find each term in unique_coding_values in the 
	-- 'whole_coding_phrase' field of 'concatenated_coding_fields'.  In the 
	-- phrase, we're trying to make sure we're looking for the exact phrase 
	-- surrounded by ' characters. For example, C34 can be found in the codes C34 
	-- and C349 and in the case of ICD codes, C349 would be a more specific 
	-- instance of C34.   But we want to be precise so that that C34 will only be 
	-- matched if it finds 'C34' rather than anything that contains or starts 
	-- with it.
	DROP TABLE IF EXISTS tmp_associations1;
	CREATE TABLE tmp_associations1 AS 
	SELECT
		a.year,
		a.healthcare_provider,
		a.code,		
		b.whole_coding_phrase
	FROM
		unique_coding_values a,
		concatenated_coding_fields b
	WHERE
		b.whole_coding_phrase LIKE '%' || ',' || a.code || ',' || '%' AND
		a.year = b.year AND
		a.healthcare_provider = b.healthcare_provider;

	-- tmp_associations1 may now have entries that look like this:
	-- year healthcare_provider code whole_coding_phrase
	-- 2009 XYZ                 63   ,63,234,154,202
	-- ...  ...                 ...  ...
	--
	-- We want to do two things.  First, we want to 'explode' the 
	-- whole_coding_phrase field to create a table row for each combination
	-- of code and one of the terms in the phrase:
	--
	-- year healthcare_provider code other_code
	-- 2009 XYZ                 63   ''
	-- 2009 XYZ                 63   63	
	-- 2009 XYZ                 63   234
	-- 2009 XYZ                 63   154
	-- 2009 XYZ                 63   202
	-- ...  ...                 ...  ...
	
	-- Second, we want to eliminate any rows where a code is the same as 
	-- other_code and where other_code may have blank values.
	-- year healthcare_provider code other_code	
	-- 2009 XYZ                 63   '' (DELETE)
	-- 2009 XYZ                 63   63	(DELETE)
	-- 2009 XYZ                 63   234
	-- 2009 XYZ                 63   154
	-- 2009 XYZ                 63   202
	-- ...  ...                 ...  ...
	--
	-- This table will contain duplicate entries that we'll use to assess
	-- the frequencies with which other_code appears with code in the same
	-- patient record.

	DROP TABLE IF EXISTS colocated_codes_with_duplicates;
	CREATE TABLE colocated_codes_with_duplicates AS
	WITH exploded_list AS 
		(SELECT
			year,
			healthcare_provider,
			code,
			UNNEST(regexp_split_to_array(whole_coding_phrase, ',')) 
				AS other_code
		FROM
			tmp_associations1)
	SELECT
		healthcare_provider,
		year,
		code, 
		other_code
	FROM
		exploded_list
	WHERE
		code != other_code AND
		other_code != '';

   	DROP INDEX IF EXISTS ind_coloc_codes_duplicates;
	CREATE INDEX  ind_coloc_codes_duplicates ON 
		colocated_codes_with_duplicates(year, healthcare_provider, code);
	
END;

$$   LANGUAGE plpgsql;

-- Creates reports based on counting duplicate code values based on year,
-- healthcare provider, or a combination of the two.  If the output_directory
-- is not null, it attempts to export the reports to CSV files.

CREATE OR REPLACE FUNCTION run_colocated_code_analyses(output_directory TEXT)
	RETURNS void AS 
$$
DECLARE

	date_phrase TEXT;	
	results_coloc_code_provider_year_file TEXT;
	results_coloc_code_provider_file TEXT;
	results_coloc_code_year_file TEXT;		
	
BEGIN
		
	DROP TABLE IF EXISTS final_coloc_frequency_provider_year;
	CREATE TABLE final_coloc_frequency_provider_year AS
	SELECT
		healthcare_provider,
		year,
		code,
		other_code,
		COUNT(healthcare_provider || year || code || other_code) 
			AS total_occurrences
	FROM
		colocated_codes_with_duplicates
	GROUP BY
		healthcare_provider,
		year,
		code,
		other_code
	ORDER BY
		healthcare_provider ASC,
		year DESC,
		COUNT(healthcare_provider || year || code || other_code) DESC;
   	ALTER TABLE final_coloc_frequency_provider_year 
   		ADD PRIMARY KEY (healthcare_provider, year, code, other_code);

	DROP TABLE IF EXISTS final_coloc_frequency_provider;
	CREATE TABLE final_coloc_frequency_provider AS
	SELECT
		healthcare_provider,
		code,
		other_code,
		COUNT(healthcare_provider || code || other_code) 
			AS total_occurrences
	FROM
		colocated_codes_with_duplicates
	GROUP BY
		healthcare_provider,
		code,
		other_code
	ORDER BY
		healthcare_provider,
		COUNT(healthcare_provider || code || other_code) DESC;
   	ALTER TABLE final_coloc_frequency_provider 
   		ADD PRIMARY KEY (healthcare_provider, code, other_code);

	DROP TABLE IF EXISTS final_coloc_frequency_year;
	CREATE TABLE final_coloc_frequency_year AS
	SELECT
		year,
		code,
		other_code,
		COUNT(year || code || other_code) AS total_occurrences
	FROM
		colocated_codes_with_duplicates
	GROUP BY
		year,
		code,
		other_code
	ORDER BY
		year DESC,
		COUNT(year || code || other_code) DESC;
   	ALTER TABLE final_coloc_frequency_year 
   		ADD PRIMARY KEY (year, code, other_code);

	--Optionally write these tables to CSV files
	IF output_directory IS NOT NULL THEN
		RAISE NOTICE 'Exporting co-located frequency analysis reports';
		
		date_phrase :=
			(SELECT to_char(current_timestamp, 'YYYY-Mon-DD-HH24-MI'));

		-- Writing out the co-located code frequency analysis by
		-- provider and year
		results_coloc_code_provider_year_file :=
			output_directory ||
			'\results_coloc_frequency_provider_year' ||
			date_phrase ||
			'.csv';			
		EXECUTE format ('
		COPY final_coloc_frequency_provider_year
		TO
			%L
		(FORMAT CSV, HEADER)', 
		results_coloc_code_provider_year_file);
		
		-- Writing out the co-located code frequency analysis by
		-- provider
		results_coloc_code_provider_file :=
			output_directory ||
			'\results_coloc_frequency_provider' ||
			date_phrase ||
			'.csv';			
		EXECUTE format ('
		COPY final_coloc_frequency_provider
		TO
			%L
		(FORMAT CSV, HEADER)', 
		results_coloc_code_provider_file);
		
		-- Writing out the co-located code frequency analysis by
		-- year
		results_coloc_code_year_file :=
			output_directory ||
			'\results_coloc_frequency_year' ||
			date_phrase ||
			'.csv';			
		EXECUTE format ('
		COPY final_coloc_frequency_year
		TO
			%L
		(FORMAT CSV, HEADER)', 
		results_coloc_code_year_file);
				
	END IF;
	
END;

$$   LANGUAGE plpgsql;

/*
 * ==============================================================================
 * Bonus Features
 * --------------
 * I've included a couple of extra routines that analyse how sparse the table
 * data are for diagnostic codes, operation codes and both.  For example, the
 * routines count the number of non-null field values that are specified in
 * the diagnostic code section.  Frequencies of non-null values are then
 * aggregated by health provider, year and both.  The same is done for 
 * operation codes
 * ==============================================================================
 *
 */

-- #ADAPT_CODE: You'd have to expand the parameters to match the number of 
-- diagnostic codes you had in your patient_record file.
CREATE OR REPLACE FUNCTION count_num_filled_diagnostic_codes(
	diag_01 TEXT,
	diag_02 TEXT,
	diag_03 TEXT)
	RETURNS INT AS 
$$
DECLARE

	non_null_field_count INT;
	
BEGIN
	non_null_field_count := 0;
	
	IF diag_01 IS NOT NULL THEN
		non_null_field_count := non_null_field_count + 1;
	END IF;
		
	IF diag_02 IS NOT NULL THEN
		non_null_field_count := non_null_field_count + 1;
	END IF;

	IF diag_03 IS NOT NULL THEN
		non_null_field_count := non_null_field_count + 1;
	END IF;

	RETURN non_null_field_count;
END;

$$   LANGUAGE plpgsql;


-- #ADAPT_CODE: You'd have to expand the parameters to match the number of 
-- diagnostic codes you had in your patient_record file.
CREATE OR REPLACE FUNCTION count_num_filled_operation_codes(
	oper_01 TEXT,
	oper_02 TEXT)
	RETURNS INT AS 
$$
DECLARE

	non_null_field_count INT;
	
BEGIN
	non_null_field_count := 0;
	
	IF oper_01 IS NOT NULL THEN
		non_null_field_count := non_null_field_count + 1;
	END IF;
		
	IF oper_02 IS NOT NULL THEN
		non_null_field_count := non_null_field_count + 1;
	END IF;

	RETURN non_null_field_count;
END;

$$   LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION run_sparseness_analyses(
	output_directory TEXT)
	RETURNS void AS 
$$
DECLARE

	date_phrase TEXT;	
	results_sparse_diag_provider_year_file TEXT;
	results_sparse_diag_provider_file TEXT;
	results_sparse_diag_year_file TEXT;		

	results_sparse_oper_provider_year_file TEXT;
	results_sparse_oper_provider_file TEXT;
	results_sparse_oper_year_file TEXT;		
	
BEGIN

	DROP TABLE IF EXISTS tmp_sparse_diag_analysis1;
	CREATE TABLE tmp_sparse_diag_analysis1 AS
	SELECT
		year,
		healthcare_provider,
		count_num_filled_diagnostic_codes(
			diag_01, 
			diag_02, 
			diag_03) AS num_filled
	FROM
		patient_records;

	CREATE INDEX ind_tmp_sparse_diag_analysis1 ON 
		tmp_sparse_diag_analysis1(year, 
		healthcare_provider, 
		num_filled);
	
	DROP TABLE IF EXISTS final_sparse_diag_provider_year;
	CREATE TABLE final_sparse_diag_provider_year AS
	SELECT
		healthcare_provider,
		year,
		num_filled,
		COUNT(num_filled) AS frequency			
	FROM
		tmp_sparse_diag_analysis1
	GROUP BY
		healthcare_provider,
		year,
		num_filled
	ORDER BY
		healthcare_provider ASC,
		year DESC,
		num_filled DESC;
		
	DROP TABLE IF EXISTS final_sparse_diag_provider;
	CREATE TABLE final_sparse_diag_provider AS
	SELECT
		healthcare_provider,
		num_filled,
		COUNT(num_filled) AS frequency			
	FROM
		tmp_sparse_diag_analysis1
	GROUP BY
		healthcare_provider,
		num_filled
	ORDER BY
		healthcare_provider ASC,
		num_filled DESC;

	DROP TABLE IF EXISTS final_sparse_diag_year;
	CREATE TABLE final_sparse_diag_year AS
	SELECT
		year,
		num_filled,
		COUNT(num_filled) AS frequency			
	FROM
		tmp_sparse_diag_analysis1
	GROUP BY
		year,
		num_filled		
	ORDER BY
		year DESC,
		num_filled DESC;


	-- Do the same for operation codes
	DROP TABLE IF EXISTS tmp_sparse_oper_analysis1;
	CREATE TABLE tmp_sparse_oper_analysis1 AS
	SELECT
		year,
		healthcare_provider,
		count_num_filled_operation_codes(
			oper_01, 
			oper_02) AS num_filled
	FROM
		patient_records;

	CREATE INDEX ind_tmp_sparse_oper_analysis1 ON 
		tmp_sparse_oper_analysis1(year, 
		healthcare_provider, 
		num_filled);

	DROP TABLE IF EXISTS final_sparse_oper_provider_year;
	CREATE TABLE final_sparse_oper_provider_year AS
	SELECT
		healthcare_provider,
		year,
		num_filled,
		COUNT(num_filled) AS frequency			
	FROM
		tmp_sparse_oper_analysis1
	GROUP BY
		healthcare_provider,
		year,
		num_filled
	ORDER BY
		healthcare_provider ASC,
		year DESC,
		num_filled DESC;
		
	DROP TABLE IF EXISTS final_sparse_oper_provider;
	CREATE TABLE final_sparse_oper_provider AS
	SELECT
		healthcare_provider,
		num_filled,
		COUNT(num_filled) AS frequency			
	FROM
		tmp_sparse_oper_analysis1
	GROUP BY
		healthcare_provider,
		num_filled
	ORDER BY
		healthcare_provider ASC,
		num_filled DESC;

	DROP TABLE IF EXISTS final_sparse_oper_year;
	CREATE TABLE final_sparse_oper_year AS
	SELECT
		year,
		num_filled,		
		COUNT(num_filled) AS frequency			
	FROM
		tmp_sparse_oper_analysis1
	GROUP BY
		year,
		num_filled
	ORDER BY
		year DESC,
		num_filled DESC;

	--Optionally write these tables to CSV files
	IF output_directory IS NOT NULL THEN
		RAISE NOTICE 'Exporting co-located frequency analysis reports';
		
		date_phrase :=
			(SELECT to_char(current_timestamp, 'YYYY-Mon-DD-HH24-MI'));


		-- Writing out the frequency of non-empty diagnostic fields
		-- provider and year
		results_sparse_diag_provider_year_file :=
			output_directory ||
			'\results_sparse_diag_provider_year' ||
			date_phrase ||
			'.csv';			
		EXECUTE format ('
		COPY final_sparse_diag_provider_year
		TO
			%L
		(FORMAT CSV, HEADER)', 
		results_sparse_diag_provider_year_file);

		-- Writing out the frequency of non-empty diagnostic fields
		-- provider
		results_sparse_diag_provider_file :=
			output_directory ||
			'\results_sparse_diag_provider' ||
			date_phrase ||
			'.csv';			
		EXECUTE format ('
		COPY final_sparse_diag_provider
		TO
			%L
		(FORMAT CSV, HEADER)', 
		results_sparse_diag_provider_file);

		-- Writing out the frequency of non-empty diagnostic fields
		-- year
		results_sparse_diag_year_file :=
			output_directory ||
			'\results_sparse_diag_year' ||
			date_phrase ||
			'.csv';			
		EXECUTE format ('
		COPY final_sparse_diag_year
		TO
			%L
		(FORMAT CSV, HEADER)', 
		results_sparse_diag_year_file);

		-- Writing out the frequency of non-empty operation fields
		-- provider and year
		results_sparse_oper_provider_year_file :=
			output_directory ||
			'\results_sparse_oper_provider_year' ||
			date_phrase ||
			'.csv';			
		EXECUTE format ('
		COPY final_sparse_oper_provider_year
		TO
			%L
		(FORMAT CSV, HEADER)', 
		results_sparse_oper_provider_year_file);

		-- Writing out the frequency of non-empty operation fields
		-- provider
		results_sparse_oper_provider_file :=
			output_directory ||
			'\results_sparse_oper_provider' ||
			date_phrase ||
			'.csv';			
		EXECUTE format ('
		COPY final_sparse_oper_provider
		TO
			%L
		(FORMAT CSV, HEADER)', 
		results_sparse_oper_provider_file);

		-- Writing out the frequency of non-empty operation fields
		-- year
		results_sparse_oper_year_file :=
			output_directory ||
			'\results_sparse_oper_year' ||
			date_phrase ||
			'.csv';			
		EXECUTE format ('
		COPY final_sparse_oper_year
		TO
			%L
		(FORMAT CSV, HEADER)', 
		results_sparse_oper_year_file);

	END IF;


END;

$$   LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION main_program(
	patient_record_file TEXT, 
	output_directory TEXT)
	RETURNS void AS 
$$
DECLARE

BEGIN
	
	IF patient_record_file IS NULL THEN
		PERFORM create_hard_coded_patient_records();	
	ELSE
		PERFORM load_patient_records_from_csv_file(
			patient_record_file);
	END IF;

	PERFORM combine_all_coding_values();
	PERFORM run_single_code_analyses(output_directory);

	PERFORM create_colocated_code_frequencies();
	PERFORM run_colocated_code_analyses(output_directory);
	PERFORM run_sparseness_analyses(output_directory);
END;

$$   LANGUAGE plpgsql;

-- #RUN_PROGRAM:
-- Uncomment one of these sections below.  Then either run the script from 
-- command line or copy all of it into pgAdmin's Query Tool window.  If you 
-- run it with a CSV file, make sure it has the columns:
-- year, healthcare_provider, diag_01, diag_02, diag_03, oper_01, oper_02.
-- And make sure your output directory exists.

-- Run the program with a CSV file you provide, but don't write out results
--SELECT "main_program"('C:\test_icd\test_patient_data_file.csv', NULL)

-- Run the program with a CSV file you provide and write results to files
--SELECT "main_program"('C:\test_icd\test_patient_data_file.csv', 'C:\test_icd')

-- Run the program without a CSV input file.  It will use hard-coded patient
-- records instead.  Write results to files.
--SELECT "main_program"(NULL, 'C:\test_icd')

-- Run the program without a CSV input file.  It will use hard-coded patient
-- records instead.  Do not write results to files.
SELECT "main_program"(NULL, NULL)

