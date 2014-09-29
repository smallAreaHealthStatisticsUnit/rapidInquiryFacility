/****** table outcome triger 

Check current_version_start_year (if not NULL) BETWEEN rif40_tables.year_start AND rif40_tables.year_stop: 
NEED CLARIFICATION SHOULD THE TABLE NAME MATCH TO COMPARE A PARTICULAR START , STOP YEAR ? NUMER_TAB MATCHES WITH RIF40_TABLES TABLE_NAME
Check numer_tab is a numerator :NEED CLARIFICATION 


 *****/
 -- current version start year 
 CREATE alter TRIGGER TR_TABLE_OUTCOME
 ON [dbo].[RIF40_TABLE_OUTCOMES]
 FOR INSERT ,  UPDATE 
 AS
 BEGIN 

 DECLARE @table  nvarchar(MAX) =
		(
		SELECT 
        concat(a.OUTCOME_GROUP_NAME , a.numer_tab)+ ' '
		FROM inserted a
        where a.NUMER_TAB not in (select TABLE_NAME from rif40_tables )
		
        FOR XML PATH('')
		 );

IF @table is NOT NULL
BEGIN
    RAISERROR('table does not exist %s', 16, 1, @table) with log;
END;

 -------check current start year  

	   DECLARE @STARTYEAR nvarchar(MAX) =
		(
		SELECT 
        concat(a.OUTCOME_GROUP_NAME , a.[CURRENT_VERSION_START_YEAR])+ ' '
		FROM inserted a
		inner join rif40_tables b
        on a.NUMER_TAB=b.TABLE_NAME
		where a.CURRENT_VERSION_START_YEAR between b.YEAR_START and b.YEAR_STOP and a.CURRENT_VERSION_START_YEAR is not null 
        FOR XML PATH('')
		 );

IF @STARTYEAR is NOT NULL
BEGIN
    RAISERROR('Current version start date didnt match for %s', 16, 1, @STARTYEAR) with log;
END;
END 











-----------------------------------
-- testing something 
-----------------------------------

-- declare @row int 
 if  exists (
 select * from [RIF40_TABLE_OUTCOMES] a
 inner join rif40_tables b
 on a.NUMER_TAB=b.TABLE_NAME
 where a.CURRENT_VERSION_START_YEAR between b.YEAR_START and b.YEAR_STOP
 )
  BEGIN
	--@row = @@rowcount
    Raiserror ('Matched recs found',16,1)
    print 'There are ' +  cast(@@rowcount as varchar(20)) + ' matched rows'
 END


 ----------------------------------------------------------------

 DECLARE @rowcount INT
SET @rowcount = (select COUNT(*) from [rto] a
    inner join rt b
    on a.NUM=b.TABLE_NAME
    where a.START_YEAR between b.YEAR_START and b.YEAR_STOP)
IF @rowcount > 0
BEGIN
    Raiserror ('Matched recs found',16,1)
    print 'There are ' +  cast(@rowcount as varchar(20)) + ' matched rows'
END

--------------does the same thing -------------------------
select * from [RIF40_TABLE_OUTCOMES] a
where exists ( select * from [RIF40_TABLE_OUTCOMES]
 inner join rif40_tables b
 on a.NUMER_TAB=b.TABLE_NAME
 where a.CURRENT_VERSION_START_YEAR between b.YEAR_START and b.YEAR_STOP)

 	SELECT 
        concat(a.OUTCOME_GROUP_NAME , a.[CURRENT_VERSION_START_YEAR])+ ' '
		FROM [RIF40_TABLE_OUTCOMES] a
		inner join rif40_tables b
        on a.NUMER_TAB=b.TABLE_NAME
		where a.CURRENT_VERSION_START_YEAR between b.YEAR_START and b.YEAR_STOP
        FOR XML PATH('')