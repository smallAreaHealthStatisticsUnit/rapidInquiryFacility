/*
 * SQL statement name: 	insert_adjacency.sql
 * Type:				Microsoft SQL Server SQL
 * Parameters:
 *						1: adjacency table; e.g. adjacency_cb_2014_us_500k
 *						2: geometry table; e.g. geometry_cb_2014_us_500k
 *						3: Max zoomlevel
 *
 * Description:			Create insert statement into adjacency table
 * Note:				%%%% becomes %% after substitution
 */

IF EXISTS(SELECT * FROM sys.indexes WHERE object_id =OBJECT_ID('geometry_ews2011', 'U') AND NAME = 'geometry_ews2011_geolevel_id_zoomlevel')
	DROP INDEX geometry_ews2011_geolevel_id_zoomlevel ON geometry_ews2011;
	
CREATE NONCLUSTERED INDEX [geometry_ews2011_geolevel_id_zoomlevel]
ON [geometry_ews2011] ([geolevel_id],[zoomlevel])
INCLUDE ([areaid],[geom]);
 
CREATE TABLE #temp_a (
	geolevel_id     INT, 
	areaid          VARCHAR(200), 
	adjacent_areaid VARCHAR(200));

DECLARE @max_geolevel_id AS int;
DECLARE c1 CURSOR FOR
	SELECT MAX(geolevel_id) AS max_geolevel_id
	  FROM %2
	 WHERE zoomlevel = %3;
DECLARE @cnt INT = 1;
OPEN c1;
FETCH NEXT FROM c1 INTO @max_geolevel_id
WHILE @cnt <= @max_geolevel_id
BEGIN
	WITH a AS (
			SELECT a.geolevel_id, a.areaid, b.areaid AS adjacent_areaid
			  FROM %2 a, %2 b
			 WHERE a.zoomlevel                 = %3
			   AND b.zoomlevel                 = %3
			   AND a.geolevel_id               = b.geolevel_id
			   AND a.geolevel_id               = @cnt
			   AND a.geom.STIntersects(b.geom) = 1
			   AND a.areaid                    != b.areaid
	)
	INSERT INTO #temp_a(geolevel_id, areaid, adjacent_areaid)
	SELECT * FROM a
	 ORDER BY geolevel_id, a.areaid;
	PRINT 'Created adjacency_list for geolevel_id: ' + CAST(@cnt AS VARCHAR) + '; rows: ' +  CAST(@@ROWCOUNT AS VARCHAR);
    SET @cnt = @cnt + 1;
END;
DEALLOCATE c1;

CREATE INDEX #temp_a_pk ON #temp_a(geolevel_id, areaid);

WITH b AS (
		SELECT a.geolevel_id, a.areaid,
				   c.adjacency_list
		  FROM #temp_a a OUTER APPLY (
				SELECT STUFF(( SELECT ',' + b.adjacent_areaid
				   FROM #temp_a AS b
				  WHERE a.areaid      = b.areaid
					AND a.geolevel_id = b.geolevel_id
				  ORDER BY b.adjacent_areaid
						FOR XML PATH('') ), 1,1,'') AS adjacency_list) AS c
)
INSERT INTO %1(geolevel_id, areaid, num_adjacencies, adjacency_list)
SELECT DISTINCT geolevel_id, areaid, LEN(adjacency_list)-LEN(REPLACE(adjacency_list, ',', ''))+1 AS num_adjacencies, adjacency_list
  FROM b
 ORDER BY 1, 2;

DROP TABLE #temp_a