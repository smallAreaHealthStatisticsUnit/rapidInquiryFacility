--check table/view existance ,compare with rif40_tables---

SELECT A.TABLE_NAME as Existing_table, A.TABLE_CATALOG AS Database_name , a.TABLE_TYPE as Existing_TableView,

b.TABLE_NAME as Expected_table,b.THEME as Expected_table_theme, b.DESCRIPTION as Expected_table_decription

FROM INFORMATION_SCHEMA.TABLES A

full outer JOIN [dbo].[RIF40_TABLES] B ON

A.TABLE_NAME=B.TABLE_NAME

WHERE TABLE_SCHEMA = 'dbo'

order by b.TABLE_NAME desc



-- check table/view existance , compare with rif40_tables_and_views---

SELECT A.TABLE_NAME as Existing_table, A.TABLE_CATALOG AS Database_name , a.TABLE_TYPE as Existing_TableView,

b.f2 as Expected_table,b.f1 as Expected_table_type, b.f3 as Expected_table_decription

FROM INFORMATION_SCHEMA.TABLES A

full outer JOIN [dbo].[rif40_tables_and_views] B ON

A.TABLE_NAME=B.F2

WHERE TABLE_SCHEMA = 'dbo' 



--- check column existance --

SELECT t.name as Existing_TableName , c.name as Existing_ColumnName, 

d.f2 as Expected_column_name,d.f1 as Expected_table_name

from sys.columns c 

inner join sys.tables t 

on c.object_id=t.object_id 

full outer join [dbo].[rif40_columns] d

on c.name=d.f2
