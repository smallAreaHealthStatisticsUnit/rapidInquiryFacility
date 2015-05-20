----TRIGGER FOR RESULTS 

/******

Check - USER exists.
Check - USER is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.

Check - relative_risk, smoothed_relative_risk, posterior_probability, posterior_probability_lower95, posterior_probability_upper95,
  	smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95, residual_relative_risk, residual_rr_lower95, residual_rr_upper95
	are NULL for directly standardised results

*********/

insert into [dbo].[T_RIF40_RESULTS]
(USERNAME, STUDY_ID, INV_ID, BAND_ID, GENDERS, ADJUSTED, DIRECT_STANDARDISATION, OBSERVED, EXPECTED, LOWER95, UPPER95, RELATIVE_RISK, SMOOTHED_RELATIVE_RISK, POSTERIOR_PROBABILITY, POSTERIOR_PROBABILITY_LOWER95, POSTERIOR_PROBABILITY_UPPER95, RESIDUAL_RELATIVE_RISK, RESIDUAL_RR_LOWER95, RESIDUAL_RR_UPPER95, SMOOTHED_SMR, SMOOTHED_SMR_LOWER95, SMOOTHED_SMR_UPPER95)
values()

-- NEED EXAMPLE DATA to test trigger 


-------------------------------------
--------create trigger 
-------------------------------------

CREATE TRIGGER tr_result_checks 
on [dbo].[T_RIF40_RESULTS]
for insert , update 
as

 DECLARE @Standard nvarchar(MAX) =
		(
		SELECT 
        ic.[STUDY_ID],cast(ic.[DIRECT_STANDARDISATION] as varchar(20))+ ' '
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