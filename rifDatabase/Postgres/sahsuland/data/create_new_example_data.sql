--
-- Use sahsuland_dev_old on laptop
--
DROP TABLE pop_population_pop;
CREATE TABLE pop_population_pop AS
WITH a AS (
	SELECT year, age_sex_group, b.low_age, b.high_age,
		  CASE TRUNC(age_sex_group::integer/ 100) WHEN 1 THEN 'M' WHEN 2 THEN 'F' ELSE 'U' END AS sex, 
		  CASE 
				WHEN b.low_age = b.high_age THEN b.low_age::INT
				WHEN b.high_age = 255 THEN (RANDOM()*(100-b.low_age)+b.low_age)::INT
				ELSE (RANDOM()*(b.high_age-b.low_age)+b.low_age)::INT
		  END AS age, 
		  level1, level2, level3, level4, total
	FROM sahsuland_pop a
			LEFT OUTER JOIN rif40_age_groups b ON (b.age_group_id = 1 AND b.offset = MOD(a.age_sex_group::integer, 100))
)
SELECT a.*,
      CASE
			WHEN a.age BETWEEN 0 AND 4 THEN 
				CASE WHEN sex = 'M' THEN (100+a.age::INT) 
					 WHEN sex = 'F' THEN (200+a.age::INT) 
					 ELSE                (300+a.age::INT) END
			WHEN a.age BETWEEN 5 AND 84 THEN 
				CASE WHEN sex = 'M' THEN (100+TRUNC(a.age::INT/5)+4) 
					 WHEN sex = 'F' THEN (200+TRUNC(a.age::INT/5)+4)
					 ELSE                (300+TRUNC(a.age::INT/5)+4) END
			WHEN a.age BETWEEN 85 AND 150 THEN 
				CASE WHEN sex = 'M' THEN  121
			     	 WHEN sex = 'F' THEN  221
					 ELSE                 321 END
			ELSE 99
		END AS new_age_sex_group
  FROM a;
ALTER TABLE pop_population_pop ADD CONSTRAINT pop_sahsuland_pop_pk PRIMARY KEY(year,age_sex_group,level4);	
\dS+ pop_population_pop
SELECT sex, SUM(total) AS total
  FROM pop_population_pop
 GROUP BY sex
 ORDER BY sex;  
SELECT age, SUM(total) AS total
  FROM pop_population_pop
 WHERE age NOT BETWEEN low_age AND high_age
 GROUP BY age
 ORDER BY age;  
SELECT new_age_sex_group, SUM(total) AS total
  FROM pop_population_pop
 WHERE new_age_sex_group != age_sex_group
 GROUP BY new_age_sex_group
 ORDER BY new_age_sex_group;  
SELECT age, SUM(total) AS total
  FROM pop_population_pop
 GROUP BY age
 ORDER BY age; 	
SELECT year, sex, age, level1, level2, level3, level4, total FROM pop_population_pop LIMIT 4;
--\copy (SELECT year, sex, age, level1, level2, level3, level4, total FROM pop_population_pop ORDER BY year, sex, age, level4) TO pop_population_pop.csv WITH (HEADER true, FORMAT csv)
DROP TABLE pop_population_pop;

DROP TABLE num_sahsuland_cancer;
CREATE TABLE num_sahsuland_cancer AS
SELECT year, age_sex_group, b.low_age, b.high_age,
      CASE TRUNC(age_sex_group::integer/ 100) WHEN 1 THEN 'M' WHEN 2 THEN 'F' ELSE 'U' END AS sex, 
	  CASE 
			WHEN b.low_age = b.high_age THEN b.low_age::INT
			WHEN b.high_age = 255 THEN (RANDOM()*(100-b.low_age)+b.low_age)::INT
			ELSE (RANDOM()*(b.high_age-b.low_age)+b.low_age)::INT
	  END AS age, 
	  level1, level2, level3, level4, icd, total
FROM rif_data.sahsuland_cancer a
		LEFT OUTER JOIN rif40_age_groups b ON (b.age_group_id = 1 AND b.offset = MOD(a.age_sex_group::integer, 100));
ALTER TABLE num_sahsuland_cancer ADD CONSTRAINT num_sahsuland_cancer_pk PRIMARY KEY(year,age_sex_group,level4,icd);	
\dS+ num_sahsuland_cancer
SELECT sex, SUM(total) AS total
  FROM num_sahsuland_cancer
 GROUP BY sex
 ORDER BY sex;  
SELECT age, SUM(total) AS total
  FROM num_sahsuland_cancer
 WHERE age NOT BETWEEN low_age AND high_age
 GROUP BY age
 ORDER BY age;  
SELECT age, SUM(total) AS total
  FROM num_sahsuland_cancer
 GROUP BY age
 ORDER BY age; 	
SELECT year, sex, age, level1, level2, level3, level4, icd, total FROM num_sahsuland_cancer LIMIT 4;
--\copy (SELECT year, sex, age, level1, level2, level3, level4, icd, total FROM num_sahsuland_cancer ORDER BY year, sex, age, icd, level4) TO num_sahsuland_cancer.csv WITH (HEADER true, FORMAT csv)
DROP TABLE num_sahsuland_cancer;

--
-- Eof