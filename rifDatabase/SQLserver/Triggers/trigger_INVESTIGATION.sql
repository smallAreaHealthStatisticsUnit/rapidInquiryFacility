USE [RIF40]
GO

/****** Object:  Trigger [dbo].[tr_investigations]    Script Date: 29/09/2014 12:13:24 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

create trigger [dbo].[tr_investigations]
on [dbo].[T_RIF40_INVESTIGATIONS]
for insert , update 
as
begin 
declare @investigation_state_only_flag int -- MIGHT NOT WORK FOR BATCH INSERTS 
	IF exists (
			select ic.username  
		    from inserted ic , [T_RIF40_INVESTIGATIONS] iv
			where   ic.username= iv.username AND
 					ic.inv_name = iv.inv_name AND
 					ic.inv_description = iv.inv_description AND
 					ic.year_start = iv.year_start AND
 					ic.year_stop = iv.year_stop AND
 					ic.max_age_group = iv.max_age_group AND
 					ic.min_age_group = iv.min_age_group AND
 					ic.genders = iv.genders AND
 					ic.numer_tab = iv.numer_tab AND
 					ic.geography = iv.geography AND
 					ic.study_id = iv.study_id AND
 					ic.inv_id = iv.inv_id AND
 					ic.classifier = iv.classifier AND
 					ic.classifier_bands = iv.classifier_bands AND
 					ic.mh_test_type = iv.mh_test_type  AND
 					ic.investigation_state <> iv.investigation_state
					) 
			begin 
				set @investigation_state_only_flag = 1
			end 


	-- this needs instread of trigger
	--INSERT MyTable(col1, [other columns])
 --   SELECT UPPER(i.col1)
 --       , i.[other columns]
 --   FROM Inserted i

-----------------------------
---define #t2
-----------------------------
select t.*
into #t2
from #t1 t
inner join  inserted ic 
on t.TABLE_NAME = ic.NUMER_TAB


-----------------------------
---define #t3
-----------------------------
select t.*
into #t3
from [dbo].[T_RIF40_STUDIES] s 
inner join inserted ic 
on s.STUDY_ID=ic.STUDY_ID
inner join #t1 t
on s.DENOM_TAB = t.TABLE_NAME


-----------------------------
---define #t4
-----------------------------
select t.*
into #t4
from [dbo].[T_RIF40_STUDIES] s 
inner join inserted  ic 
on s.STUDY_ID=ic.STUDY_ID
inner join #t1 t
on s.DIRECT_STAND_TAB = t.TABLE_NAME

DECLARE @numer_tab NVARCHAR(MAX)= 
( 
SELECT TABLE_NAME
FROM	rif40_tables a 
WHERE	table_name in (select [NUMER_TAB] from [dbo].[T_RIF40_INVESTIGATIONS] where ISNUMERATOR <>1)
 FOR XML PATH('') 
);
BEGIN TRY
IF @numer_tab IS NOT NULL 
	BEGIN 
		RAISERROR(50050,16,1,@numer_tab); 
		rollback 
	END;
END TRY
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 

-----------------------
--year start
-----------------------
DECLARE @year_start NVARCHAR(MAX)= 
( 
SELECT concat(TABLE_NAME ,' / ', b.YEAR_START)  + ' , '
FROM	rif40_tables a 
inner join [dbo].[T_RIF40_INVESTIGATIONS] b
on a.TABLE_NAME=b.NUMER_TAB
where b.YEAR_START is not null and 
b.YEAR_START < a.YEAR_START
 FOR XML PATH('') 
);
BEGIN TRY
IF @year_start IS NOT NULL 
	BEGIN 
		RAISERROR(50051,16,1,@year_start); 
		rollback 
	END;
END TRY
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 


-----------------------
--year stop
-----------------------
DECLARE @year_stop NVARCHAR(MAX)= 
( 
SELECT concat(TABLE_NAME ,' / ', b.YEAR_STOP)  + ' , '
FROM	rif40_tables a 
inner join [dbo].[T_RIF40_INVESTIGATIONS] b
on a.TABLE_NAME=b.NUMER_TAB
where b.YEAR_START is not null and 
b.YEAR_START > a.YEAR_STOP
 FOR XML PATH('') 
);
BEGIN TRY
IF @year_stop IS NOT NULL 
	BEGIN 
		RAISERROR(50052,16,1,@year_stop); 
		rollback 
	END;
END TRY
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 

---------------
-- MIN AGE 
---------------

SELECT a.table_name, a.year_start, a.year_stop, a.isindirectdenominator, a.isdirectdenominator, a.isnumerator,
	   a.age_sex_group_field_name, a.sex_field_name, a.age_group_field_name, a.total_field, a.AGE_GROUP_ID,
       MIN(g.offset) min_age_group, MAX(g.offset) max_age_group
into #t1
FROM rif40_tables a
LEFT OUTER JOIN rif40_age_groups g ON (g.age_group_id  = a.age_group_id)
--WHERE table_name = 'UK91_DEATHS'
GROUP BY a.year_start, a.year_stop, a.isindirectdenominator, a.isdirectdenominator, a.isnumerator,
		 a.age_sex_group_field_name, a.sex_field_name, a.age_group_field_name, a.total_field,a.table_name , a.AGE_GROUP_ID


DECLARE @min_age NVARCHAR(MAX)= 
( 
select * from #t1 a 
inner join [dbo].[T_RIF40_INVESTIGATIONS] b 
on a.table_name = b.numer_tab and 
a.min_age_group is not null and
b.MIN_AGE_GROUP<a.min_age_group
 FOR XML PATH('') 
)
BEGIN TRY
IF @min_age IS NOT NULL 
	BEGIN 
		RAISERROR(50053,16,1,@min_age); 
		rollback 
	END;
END TRY
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 

---------------
-- Max AGE 
---------------
DECLARE @no_age_group NVARCHAR(MAX)= 
( 
select concat(a.min_age_group ,' / ', a.max_age_group)  + ' , '
from #t1 a 
inner join [dbo].[T_RIF40_INVESTIGATIONS] b 
on a.table_name = b.numer_tab and 
a.max_age_group is  null or
a.min_age_group is  null 
 FOR XML PATH('') 
)
BEGIN TRY
IF @no_age_group IS NOT NULL 
	BEGIN 
		RAISERROR(50054,16,1,@no_age_group); 
		rollback 
	END;
END TRY
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 

-----------------------------------------------
--min >max age group: just for inserted table 
--------------------- --------------------------
DECLARE @min_max NVARCHAR(MAX)= 
( 
select a.min_age_group + ' , '
from inserted a 
where a.min_age_group> a.max_age_group
FOR XML PATH('') 
)
BEGIN TRY
IF @min_max IS NOT NULL 
	BEGIN 
		RAISERROR(50055,16,1,@min_max); 
		rollback 
	END;
END TRY
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 

-----------------------------------------------
--year start > year stop : just for inserted table 
--------------------- --------------------------
DECLARE @year NVARCHAR(MAX)= 
( 
select a.YEAR_START + ' , '
from inserted a 
where a.YEAR_START> a.YEAR_STOP
FOR XML PATH('') 
)
BEGIN TRY
IF @year IS NOT NULL 
	BEGIN 
		RAISERROR(50055,16,1,@year); 
		rollback 
	END;
END TRY
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 

--------------------------------
-- verify column existence  
----------------------------------

DECLARE @sex_field_name nvarchar(MAX) =
(
	select a.SEX_FIELD_NAME+ ' , '
	from #t1 a
	inner join inserted b
	on a.TABLE_NAME=b.NUMER_TAB 
	where a.SEX_FIELD_NAME is not null and 
	a.SEX_FIELD_NAME not in (SELECT c.name 
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =b.NUMER_TAB)
   FOR XML PATH('')

);
BEGIN TRY 
IF @sex_field_name IS NOT NULL
	BEGIN
		RAISERROR(50057, 16, 1, @sex_field_name) with log;
		ROLLBACK
	END;
END TRY 
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 


DECLARE @age_group_field_name nvarchar(MAX) =
(
	select a.AGE_GROUP_FIELD_NAME+ ' , '
	from #t1 a
	inner join inserted b
	on a.TABLE_NAME=b.NUMER_TAB 
	where a.SEX_FIELD_NAME is not null and 
	a.AGE_GROUP_FIELD_NAME not in (SELECT c.name 
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =b.NUMER_TAB)
   FOR XML PATH('')

);
BEGIN TRY 
IF @age_group_field_name IS NOT NULL
	BEGIN
		RAISERROR(50058, 16, 1, @age_group_field_name) with log;
		ROLLBACK
	END;
END TRY 
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 


DECLARE @age_sex_group_field_name nvarchar(MAX) =
(
	select a.AGE_SEX_GROUP_FIELD_NAME+ ' , '
	from #t1 a
	inner join inserted b
	on a.TABLE_NAME=b.NUMER_TAB 
	where a.SEX_FIELD_NAME is not null and 
	a.AGE_SEX_GROUP_FIELD_NAME not in (SELECT c.name 
							from sys.columns c 
							inner join  sys.tables t 
							on c.object_id=t.object_id 
							where t.name =b.NUMER_TAB)
   FOR XML PATH('')

);
BEGIN TRY 
IF @age_sex_group_field_name IS NOT NULL
	BEGIN
		RAISERROR(50059, 16, 1, @age_sex_group_field_name) with log;
		ROLLBACK
	END;
END TRY 
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 


--#t2 = c1a_rec
--#t3 = c1b_rec
--#t4 = c1c_rec


-------------------------------------------------------------
-- verify field names are the same : batch insert wont work 
-------------------------------------------------------------
DECLARE @age_group_id nvarchar(MAX) =
(
	select t.AGE_GROUP_ID
	from #t1 t 
	where t.AGE_GROUP_ID <> (select t2.AGE_GROUP_ID from #t2 t2 ) or 
	t.AGE_GROUP_ID <> (select t3.AGE_GROUP_ID from #t3 t3)
	FOR XML PATH('')

);
BEGIN TRY 
IF @age_group_id IS NOT NULL
	BEGIN
		RAISERROR(50060, 16, 1, @age_group_id) with log;
		ROLLBACK
	END;
END TRY 
BEGIN CATCH
		EXEC [ErrorLog_proc]
END CATCH 

end 
GO


