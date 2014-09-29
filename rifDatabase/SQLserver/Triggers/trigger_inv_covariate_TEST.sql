--TEST [dbo].[T_RIF40_INV_COVARIATES] TRIGGER
---------------------------------------------------------------------------------

select ic.*, c.type 
FROM  [dbo].[T_RIF40_INV_COVARIATES] ic ,rif40_covariates c 
              WHERE ic.[geography]=c.[GEOGRAPHY] and 
			  c.geolevel_name=ic.study_geolevel_name and
			 --c.covariate_name=ic.covariate_name and -- can comment it out to test 
			  c.type=2 
			  --3790 join is retirning duplicates 
----------------------------------------------------------------------------------------
		select ic.* 
		from  [T_RIF40_INV_COVARIATES] ic 
		where EXISTS (SELECT 1 FROM [dbo].rif40_covariates c 
              WHERE ic.[geography]=c.[GEOGRAPHY] and 
			  c.geolevel_name=ic.study_geolevel_name and
			-- c.covariate_name=ic.covariate_name and -- can comment it out to test 
			  c.type=2) 
			  --359

			  
SELECT ic.*
 FROM  [dbo].[T_RIF40_INV_COVARIATES]   ic
 WHERE   EXISTS (SELECT 1 FROM [dbo].rif40_covariates c 
              WHERE ic.[geography]=c.[GEOGRAPHY] and 
			  c.geolevel_name=ic.study_geolevel_name and
			 c.covariate_name=ic.covariate_name and -- can comment it out to test 
					   IC.MAX=C.MAX)

------------------------------------------------------------------
select * from rif40_covariates
where type=1 and GEOGRAPHY='EW01' AND GEOLEVEL_NAME='SOA2001'
ORDER BY MAX

select * from rif40_covariates
where type=1 and GEOGRAPHY='EW01' AND GEOLEVEL_NAME='SOA2001'
ORDER BY MIN