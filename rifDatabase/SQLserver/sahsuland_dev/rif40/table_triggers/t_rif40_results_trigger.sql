/*
Check - USER exists.
Check - USER is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.
Check - relative_risk, smoothed_relative_risk, posterior_probability, posterior_probability_lower95, posterior_probability_upper95,
  	smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95, residual_relative_risk, residual_rr_lower95, residual_rr_upper95
	are NULL for directly standardised results
*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_studies_checks')
BEGIN
	DROP TRIGGER [rif40].[tr_studies_checks]
END
GO


CREATE TRIGGER [tr_result_checks] 
on [rif40].[t_rif40_results]
for insert , update 
as

 DECLARE @Standard nvarchar(MAX) =
		(
		SELECT 
        cast(ic.[DIRECT_STANDARDISATION] as varchar(20))+ ' '
		FROM inserted ic 
		where ic.[DIRECT_STANDARDISATION]=1 and 
			(
			ic.relative_risk IS NOT NULL OR
			ic.smoothed_relative_risk IS NOT NULL OR
			ic.posterior_probability IS NOT NULL OR
			ic.posterior_probability_lower95 IS NOT NULL OR
			ic.posterior_probability_upper95 IS NOT NULL OR
			ic.smoothed_smr IS NOT NULL OR
			ic.smoothed_smr_lower95 IS NOT NULL OR
			ic.smoothed_smr_upper95 IS NOT NULL OR
			ic.residual_relative_risk IS NOT NULL OR
			ic.residual_rr_lower95 IS NOT NULL OR
			ic.residual_rr_upper95 IS NOT NULL
			)
        FOR XML PATH('')
		 );

	IF @Standard IS NOT NULL
		BEGIN
			RAISERROR('Expecting NULL relative_risk with direct standardised results: %s', 16, 1, @Standard) with log;
		END;
GO