INSERT INTO lookup_%1(%1, areaname)
SELECT areaid, areaname
  FROM %1
 ORDER BY 1