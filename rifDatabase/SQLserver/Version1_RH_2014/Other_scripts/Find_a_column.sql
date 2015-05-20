-------------------------------------
-- find a column like  'direct%'
-------------------------------------


SELECT t.name AS table_name,
 SCHEMA_NAME(schema_id) AS schema_name,
 c.name AS column_name
 FROM sys.tables AS t
 INNER JOIN sys.columns c ON t.OBJECT_ID = c.OBJECT_ID 
 where c.name like 'direct%'
ORDER BY schema_name, table_name;
