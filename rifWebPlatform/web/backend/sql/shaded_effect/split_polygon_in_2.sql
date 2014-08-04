create table halftest as
WITH RECURSIVE
ref(geom, env) AS (
SELECT geom,
ST_Envelope(geom) As env,
ST_Area(geom)/2 As targ_area,
1000 As nit
FROM e_wards
WHERE gid = 3
),

T(n,overlap) AS (
VALUES (CAST(0 As Float),CAST(0 As Float))
UNION ALL
SELECT n + nit, ST_Area(ST_Intersection(geom, ST_Translate(env, n+nit, 0)))
FROM T CROSS JOIN ref
WHERE ST_Area(ST_Intersection(geom, ST_Translate(env, n+nit, 0)))> ref.targ_area
) ,  

bi(n) AS
(SELECT n
FROM T
ORDER BY n DESC LIMIT 1)  

SELECT /*bi.n,*/
ST_Difference(geom, ST_Translate(ref.env, n,0)) As geom_part1/*,
ST_Intersection(geom, ST_Translate(ref.env, n,0)) As geom_part2*/
FROM bi CROSS JOIN ref