INSERT INTO %1(%2, areaname, gid)
SELECT areaid, areaname, ROW_NUMBER() OVER(ORDER BY areaid) AS gid
  FROM %2
 ORDER BY 1