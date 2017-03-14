--
-- Use sahsuland_dev_old on laptop
--
DROP VIEW pop_population_pop;
CREATE VIEW pop_population_pop AS
SELECT year, age_sex_group, b.low_age, b.high_age,
      CASE TRUNC(age_sex_group::integer/ 100) WHEN 1 THEN 'M' WHEN 2 THEN 'F' ELSE 'U' END AS sex, 
	  CASE 
			WHEN b.low_age = b.high_age THEN b.low_age
			WHEN b.high_age = 255 THEN (RANDOM()*(100-b.low_age)+b.low_age)::INT
			ELSE (RANDOM()*(b.high_age-b.low_age)+b.low_age)::INT
	  END AS age, 
	  level1, level2, level3, level4, total
FROM sahsuland_pop a
		LEFT OUTER JOIN rif40_age_groups b ON (b.age_group_id = 1 AND b.offset = MOD(a.age_sex_group::integer, 100));
SELECT sex, SUM(total) AS total
  FROM pop_population_pop
 GROUP BY sex
 ORDER BY sex;  
SELECT age, SUM(total) AS total
  FROM pop_population_pop
 WHERE age NOT BETWEEN low_age AND high_age
 GROUP BY age
 ORDER BY age;  
SELECT age, SUM(total) AS total
  FROM pop_population_pop
 GROUP BY age
 ORDER BY age; 	
SELECT year, sex, age, level1, level2, level3, level4, total FROM pop_population_pop LIMIT 4;
\copy (SELECT year, sex, age, level1, level2, level3, level4, total FROM pop_population_pop ORDER BY year, sex, age, level4) TO pop_population_pop.csv WITH (HEADER true, FORMAT csv)


DROP VIEW num_sahsuland_cancer;
CREATE VIEW num_sahsuland_cancer AS
SELECT year, age_sex_group, b.low_age, b.high_age,
      CASE TRUNC(age_sex_group::integer/ 100) WHEN 1 THEN 'M' WHEN 2 THEN 'F' ELSE 'U' END AS sex, 
	  CASE 
			WHEN b.low_age = b.high_age THEN b.low_age
			WHEN b.high_age = 255 THEN (RANDOM()*(100-b.low_age)+b.low_age)::INT
			ELSE (RANDOM()*(b.high_age-b.low_age)+b.low_age)::INT
	  END AS age, 
	  level1, level2, level3, level4, icd, total
FROM sahsuland_cancer a
		LEFT OUTER JOIN rif40_age_groups b ON (b.age_group_id = 1 AND b.offset = MOD(a.age_sex_group::integer, 100));
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
\copy (SELECT year, sex, age, level1, level2, level3, level4, icd, total FROM num_sahsuland_cancer ORDER BY year, sex, age, icd, level4) TO num_sahsuland_cancer.csv WITH (HEADER true, FORMAT csv)

--
-- Eof