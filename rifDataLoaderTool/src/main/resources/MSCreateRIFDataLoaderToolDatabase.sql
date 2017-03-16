

-- Assumes we have created a database called tmp_sahsu_db

-- =====================================================================
-- Create system tables needed by the Data Loader Tool
-- =====================================================================

DROP TABLE IF EXISTS data_set_configurations;
CREATE TABLE data_set_configurations (
	id INT IDENTITY(1,1),
	core_data_set_name VARCHAR(50) NOT NULL,
	version VARCHAR(30) NOT NULL,
	creation_date DATE NOT NULL DEFAULT GETDATE(),
	current_workflow_state VARCHAR(20) NOT NULL DEFAULT 'start');
GO
	
DROP TABLE IF EXISTS rif_change_log;
CREATE TABLE rif_change_log (
	data_set_id INT NOT NULL,
	row_number INT NOT NULL,
	field_name VARCHAR(30) NOT NULL,
	old_value VARCHAR(30) NOT NULL,
	new_value VARCHAR(30) NOT NULL,
	time_stamp DATE NOT NULL DEFAULT GETDATE());
GO

DROP TABLE IF EXISTS rif_failed_val_log;
CREATE TABLE rif_failed_val_log (
	data_set_id INT NOT NULL,
	row_number INT NOT NULL,
	field_name VARCHAR(30) NOT NULL,
	invalid_value VARCHAR(30) NOT NULL,
	time_stamp DATE NOT NULL DEFAULT GETDATE());
GO

-- =====================================================================
-- Default Data Cleaning Functions
-- =====================================================================

DROP FUNCTION IF EXISTS dbo.clean_date
GO
CREATE FUNCTION dbo.clean_date(
	@date_value VARCHAR(30),
	@date_format VARCHAR(30))
RETURNS VARCHAR(30) 
AS 
BEGIN
	DECLARE 
		@result AS VARCHAR(30);
	SELECT @result = @date_value;
	RETURN @result;
END
GO

--SELECT date_matches_format('05/20/1995', 'MM/DD/YYYY');

DROP FUNCTION IF EXISTS dbo.clean_year
GO
CREATE FUNCTION dbo.clean_year(
	@year_value VARCHAR(30))
RETURNS VARCHAR(30) 
AS 
BEGIN

	DECLARE 
		@result AS VARCHAR(30);

   	SET @result = @year_value;

    RETURN @result;
END
GO

--SELECT date_matches_format('05/20/1995', 'MM/DD/YYYY');

DROP FUNCTION IF EXISTS dbo.clean_age;
GO
CREATE FUNCTION dbo.clean_age(
	@original_age VARCHAR(30))
RETURNS VARCHAR(30) 
AS 
BEGIN
	DECLARE 
		@cleaned_age VARCHAR(30);
	--This is just a stub FUNCTION dbo.to demonstrate functionality.

	SET @cleaned_age = @original_age;
	RETURN @cleaned_age;
END
GO

DROP FUNCTION IF EXISTS dbo.clean_icd_code
GO
CREATE FUNCTION dbo.clean_icd_code(
	@original_icd_code VARCHAR(30))
RETURNS VARCHAR(30) 
AS 
BEGIN
	DECLARE 
		@cleaned_icd_code VARCHAR(30)

	SET @cleaned_icd_code = REPLACE(@original_icd_code, '.', '');
	RETURN @cleaned_icd_code;
END
GO

DROP FUNCTION IF EXISTS dbo.uk_postal_code
GO
CREATE FUNCTION dbo.clean_uk_postal_code(
	@original_uk_postal_code VARCHAR(30))
RETURNS VARCHAR(30) 
AS 
BEGIN
	DECLARE 
		@cleaned_uk_postal_code VARCHAR(30);

	SET @cleaned_uk_postal_code = UPPER(REPLACE(@original_uk_postal_code, ' ', ''));
	RETURN @cleaned_uk_postal_code;
END
GO

DROP FUNCTION IF EXISTS dbo.clean_icd
GO
CREATE FUNCTION dbo.clean_icd(
	@original_icd_code VARCHAR(30))
RETURNS VARCHAR(30) 
AS 
BEGIN
	DECLARE 
		@cleaned_icd_code VARCHAR(30);

	SET @cleaned_icd_code = REPLACE(@original_icd_code, '.', '');
	RETURN @cleaned_icd_code;
END
GO

-- =====================================================================
-- Default Validating Functions
-- =====================================================================

GO
DROP FUNCTION IF EXISTS dbo.date_matches_format
GO
CREATE FUNCTION dbo.date_matches_format(
	@date_value VARCHAR(30),
	@date_format VARCHAR(30))
	RETURNS INT AS 

BEGIN
	DECLARE 
		@result INT;

	RETURN 1;

END
GO

/*
 * Function: is_numeric
 * --------------------
 * Uses a regular expression to check whether a piece of text is numeric
 * or not.
 * Input: a text value
 *RETURNS: 
 *    true if the text value represents a number or
 *    false if the text value is not a number
 */
 
GO
DROP FUNCTION IF EXISTS dbo.is_numeric
GO
CREATE FUNCTION dbo.is_numeric(
	@original_value VARCHAR(30))
RETURNS INT 
AS 
BEGIN
	DECLARE 
		@result INT;

	IF @original_value IS NULL
		RETURN 0;

	RETURN 1;
END
GO

DROP FUNCTION IF EXISTS dbo.is_valid_age
GO
CREATE FUNCTION dbo.is_valid_age(
	@candidate_age VARCHAR(30))
	RETURNS INT AS 

BEGIN

	RETURN 1;
	
END
GO

DROP FUNCTION IF EXISTS dbo.is_valid_uk_postal_code
GO
CREATE FUNCTION dbo.is_valid_uk_postal_code(
	@candidate_postal_code VARCHAR(30))
	RETURNS INT AS 

BEGIN

	RETURN 1;

END
GO

DROP FUNCTION IF EXISTS dbo.is_valid_double
GO
CREATE FUNCTION dbo.is_valid_double(
	@candidate_double VARCHAR(20))
	RETURNS INT AS 

BEGIN
	DECLARE 
		@double_value AS DOUBLE PRECISION;

	SELECT @double_value = cast(@candidate_double as double precision);
		
	RETURN 1;

END
GO


DROP FUNCTION IF EXISTS dbo.is_valid_integer
GO
CREATE FUNCTION dbo.is_valid_integer(
	@candidate_integer VARCHAR(20))
	RETURNS INT AS 

BEGIN
		
	RETURN 1;

END
GO


-- =====================================================================
-- Data Transformation Functions
-- =====================================================================

--Login to PostgreSQL
-- ensure that a database tmp_sahsu_db exists
--Copy and paste this entire file into PG Admin's query editor
--Press enter.  It should create all the tables and load all the functions 
--that should appear in the demo.

GO
DROP FUNCTION IF EXISTS dbo.convert_age_sex;
GO
CREATE FUNCTION dbo.convert_age_sex(
	@age INT,
	@sex INT)
	RETURNS INT AS 

BEGIN
	DECLARE 
		@age_sex_code INT;

	IF @age IS NULL OR @sex IS NULL
	   RETURN -1;
	
	SET @age_sex_code = (@sex * 100) + @age;
	RETURN @age_sex_code;
END
GO


GO
DROP FUNCTION IF EXISTS dbo.map_age_to_rif_age_group;
GO
CREATE FUNCTION dbo.map_age_to_rif_age_group(
	@original_age VARCHAR(30))
RETURNS VARCHAR(30) 
AS 
BEGIN
	DECLARE 
		@age INT,
		@result VARCHAR(30);

	SET @age = CAST(@original_age AS INT);

	IF @age=0
		SET @result = '0';
	ELSE IF @age=1
		SET @result = '1';
	ELSE IF @age=2
		SET @result = '2';
	ELSE IF @age=3
		SET @result = '3';
	ELSE IF @age=4
		SET @result = '4';
	ELSE IF @age BETWEEN 5 AND 9
		SET @result = '5';
	ELSE IF @age BETWEEN 10 AND 14
		SET @result = '6';
	ELSE IF @age BETWEEN 15 AND 19 
		SET @result = '7';
	ELSE IF @age BETWEEN 20 AND 24 
		SET @result = '8';
	ELSE IF @age BETWEEN 25 AND 29
		SET @result = '9';
	ELSE IF @age BETWEEN 30 AND 34
		SET @result = '10';
	ELSE IF @age BETWEEN 35 AND 39
		SET @result = '11';
	ELSE IF @age BETWEEN 40 AND 44
		SET @result = '12';		
	ELSE IF @age BETWEEN 45 AND 49
		SET @result = '13';
	ELSE IF @age BETWEEN 50 AND 54
		SET @result = '14';
	ELSE IF @age BETWEEN 55 AND 59
		SET @result = '15';
	ELSE IF @age BETWEEN 60 AND 64
		SET @result = '16';
	ELSE IF @age BETWEEN 65 AND 69
		SET @result = '17';
	ELSE IF @age BETWEEN 70 AND 74 
		SET @result = '18';
	ELSE IF @age BETWEEN 75 AND 79
		SET @result = '19';
	ELSE IF @age BETWEEN 80 AND 84
		SET @result = '20';
	ELSE IF @age >= 85
		SET @result = '21';
	ELSE
		SET @result = NULL;
		
	RETURN @result;
END
GO

