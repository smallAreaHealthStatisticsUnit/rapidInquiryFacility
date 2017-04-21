

-- Assumes we have created a database called tmp_sahsu_db

-- =====================================================================
-- Create system tables needed by the Data Loader Tool
-- =====================================================================

IF OBJECT_ID('data_set_configurations', 'U') IS NOT NULL DROP TABLE data_set_configurations;
CREATE TABLE data_set_configurations (
	id INT IDENTITY(1,1),
	core_data_set_name VARCHAR(50) NOT NULL,
	version VARCHAR(30) NOT NULL,
	creation_date DATE NOT NULL DEFAULT GETDATE(),
	current_workflow_state VARCHAR(20) NOT NULL DEFAULT 'start');
GO
	
IF OBJECT_ID('rif_change_log', 'U') IS NOT NULL DROP TABLE rif_change_log;
CREATE TABLE rif_change_log (
	data_set_id INT NOT NULL,
	row_number INT NOT NULL,
	field_name VARCHAR(30) NOT NULL,
	old_value VARCHAR(30) NOT NULL,
	new_value VARCHAR(30) NOT NULL,
	time_stamp DATE NOT NULL DEFAULT GETDATE());
GO

IF OBJECT_ID('rif_failed_val_log', 'U') IS NOT NULL DROP TABLE rif_failed_val_log;
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

IF OBJECT_ID('dbo.clean_date', 'FN') IS NOT NULL DROP FUNCTION dbo.clean_date
GO
CREATE FUNCTION dbo.clean_date(
	@date_value VARCHAR(30),
	@date_format VARCHAR(30))
RETURNS VARCHAR(30) 
AS 
BEGIN
       -- This function probably won't do what KGARWOOD intended.
       -- BUT is seems to do as much as the postgres version which seems to be a stub
   DECLARE @result varchar(30)
   DECLARE @tempDate as datetime
   SET @tempDate  = CAST(@date_value AS datetime)
   SET @result = CONVERT(nvarchar(30), @tempDate, CAST(@date_format AS int)) 
   RETURN @result;
END
GO

--SELECT date_matches_format('05/20/1995', 'MM/DD/YYYY');

IF OBJECT_ID('dbo.clean_year', 'FN') IS NOT NULL DROP FUNCTION dbo.clean_year
GO
CREATE FUNCTION dbo.clean_year(
	@year_value VARCHAR(30))
RETURNS VARCHAR(30) 
AS 
BEGIN

       DECLARE @tmpDateStr as VARCHAR(30)
       DECLARE @tmpDate as date
       DECLARE @retStr as VARCHAR(30)

       SET @tmpDateStr = @year_value + '.01.01'
       IF (len(@year_value) = 2) 
              SET @tmpDate = CONVERT(date, @tmpDateStr, 2)
       ELSE
              SET @tmpDate = CONVERT(date, @tmpDateStr, 102)

       SET @retStr = CAST(YEAR(@tmpDate) AS VARCHAR(30))
       RETURN @retStr
END
GO

--SELECT date_matches_format('05/20/1995', 'MM/DD/YYYY');

IF OBJECT_ID('dbo.clean_age', 'FN') IS NOT NULL DROP FUNCTION dbo.clean_age;
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

IF OBJECT_ID('dbo.clean_icd_code', 'FN') IS NOT NULL DROP FUNCTION dbo.clean_icd_code
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

IF OBJECT_ID('dbo.clean_uk_postal_code', 'FN') IS NOT NULL DROP FUNCTION dbo.clean_uk_postal_code
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

IF OBJECT_ID('dbo.clean_icd', 'FN') IS NOT NULL DROP FUNCTION dbo.clean_icd
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


IF OBJECT_ID('dbo.date_matches_format', 'FN') IS NOT NULL DROP FUNCTION dbo.date_matches_format
GO
CREATE FUNCTION dbo.date_matches_format(
	@date_value VARCHAR(30),
	@date_format VARCHAR(30))
	RETURNS INT AS 

BEGIN
       --Date formats are defined as integers in SQL SERVER, so assume an integer is passed in

       --this function should really use TRY can CATCH which isn't so easy in functions
       --It will break if an invalid date fromat is given
       DECLARE @DateFormatInt AS INT
       DECLARE @res AS INT

       SET @DateFormatInt = CAST(@date_format as int)

       IF (@date_value = NULL OR @date_format = NULL)
              RETURN 0
   
              SET  @res = 
              CASE
                     WHEN CONVERT(VARCHAR, CONVERT(date, @date_value, @DateFormatInt), @DateFormatInt) = @date_value THEN
                     1
                     ELSE
                     0
              END
       RETURN @res

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
 
IF OBJECT_ID('dbo.is_numeric', 'FN') IS NOT NULL DROP FUNCTION dbo.is_numeric
GO
CREATE FUNCTION dbo.is_numeric(
	@original_value VARCHAR(30))
RETURNS INT 
AS 
BEGIN
	return ISNUMERIC(@original_value)
END
GO

IF OBJECT_ID('dbo.is_valid_age', 'FN') IS NOT NULL DROP FUNCTION dbo.is_valid_age
GO
CREATE FUNCTION dbo.is_valid_age(
	@candidate_age VARCHAR(30))
	RETURNS INT AS 

BEGIN
       -- Declare the return variable here
       DECLARE @age_value integer;
       SET @age_value = @candidate_age;
       IF (@age_value < 0) OR (@age_value > 120) 
              RETURN 0;
       ELSE
              RETURN 1;
              
RETURN 1;
	
END
GO

IF OBJECT_ID('dbo.is_valid_uk_postal_code', 'FN') IS NOT NULL DROP FUNCTION dbo.is_valid_uk_postal_code
GO
CREATE FUNCTION dbo.is_valid_uk_postal_code(
	@candidate_postal_code VARCHAR(30))
	RETURNS INT AS 

BEGIN

	RETURN 1;

END
GO

IF OBJECT_ID('dbo.is_valid_double', 'FN') IS NOT NULL DROP FUNCTION dbo.is_valid_double
GO
CREATE FUNCTION dbo.is_valid_double(
	@candidate_double VARCHAR(20))
	RETURNS INT AS 

BEGIN
	   return ISNUMERIC(@candidate_double)

END
GO


IF OBJECT_ID('dbo.is_valid_integer', 'FN') IS NOT NULL DROP FUNCTION dbo.is_valid_integer
GO
CREATE FUNCTION dbo.is_valid_integer(
	@candidate_integer VARCHAR(20))
	RETURNS INT AS 

BEGIN
       IF (@candidate_integer LIKE '%[^0-9]%')  
              RETURN 0;
       ELSE
              RETURN 1;

              
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
IF OBJECT_ID('dbo.convert_age_sex', 'FN') IS NOT NULL DROP FUNCTION dbo.convert_age_sex;
GO
CREATE FUNCTION dbo.convert_age_sex(
	@age INT,
	@sex INT)
	RETURNS INT AS 

BEGIN
       IF (@age IS NULL OR @sex IS NULL) BEGIN
          RETURN -1;
       END
       
       IF (@age >= 0 AND @age <= 4) BEGIN
              IF (@sex = 1) 
                     RETURN(100 + @age);
              ELSE IF (@sex = 2)
                           RETURN(200 + @age);
              ELSE
                     RETURN(300 + @age);
              
       END 
       
       IF (@age >= 5 AND @age <= 84) BEGIN
              IF (@sex = 1)
                     RETURN(100+ (@age/5) + 4);
              ELSE IF (@sex = 2)
                     RETURN(200+ (@age/5) + 4);
              ELSE
                     RETURN(300+ (@age/5) + 4);
              
       END
       
       IF (@age >= 85 AND @age <=150) BEGIN
              IF (@sex = 1)
                     RETURN 121;
              ELSE IF (@sex = 2) 
                     RETURN 221;
              ELSE
                     RETURN 321;
       END
       
       RETURN 99;
END
GO


IF OBJECT_ID('dbo.map_age_to_rif_age_group', 'FN') IS NOT NULL DROP FUNCTION dbo.map_age_to_rif_age_group;
GO
CREATE FUNCTION dbo.map_age_to_rif_age_group(
	@original_age VARCHAR(30))
RETURNS VARCHAR(30) 
AS 
BEGIN
       DECLARE @age INT;
       DECLARE @result INT;

       SET @age = CAST(@original_age AS int)

       IF (@age = 0) SET @result = '0';
       IF (@age = 1) SET @result = '1'
       IF (@age = 2) SET @result = '2'
       IF (@age = 3) SET @result = '3'
       IF (@age = 4) SET @result = '4'
       IF (@age >=5 and @age <= 9) SET @result = '5'
       IF (@age >=10 and @age <= 14) SET @result = '6'
       IF (@age >=15 and @age <= 19) SET @result = '7'
       IF (@age >=20 and @age <= 24) SET @result = '8'
       IF (@age >=25 and @age <= 29) SET @result = '9'
       IF (@age >=30 and @age <= 34) SET @result = '10'
       IF (@age >=35 and @age <= 39) SET @result = '11'
       IF (@age >=40 and @age <= 44) SET @result = '12'
       IF (@age >=45 and @age <= 49) SET @result = '13'
       IF (@age >=50 and @age <= 54) SET @result = '14'
       IF (@age >=55 and @age <= 59) SET @result = '15'
       IF (@age >=60 and @age <= 64) SET @result = '16'
       IF (@age >=65 and @age <= 69) SET @result = '17'
       IF (@age >=70 and @age <= 74) SET @result = '18'
       IF (@age >=75 and @age <= 79) SET @result = '19'
       IF (@age >=80 and @age <= 84) SET @result = '20'
       IF (@age >=85) SET @result = '21'

       RETURN @result
END
GO

