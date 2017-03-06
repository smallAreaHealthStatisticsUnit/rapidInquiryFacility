/*
Draft version with no logging or error handling

Function: 	rif40_auto_indirect_checks()
Parameters:	table name
Returns: 	NULL if not found or not an indirect denominator, geography and counts
Description:	Automatic (Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). 
		A user specific T_RIF40_NUM_DENOM is supplied for other combinations. 
		Cannot be applied to direct standardisation denominator) is restricted to 1 denominator per geography.
 */

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_auto_indirect_checks]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP FUNCTION [rif40].[rif40_auto_indirect_checks]
GO 


CREATE FUNCTION [rif40].[rif40_auto_indirect_checks](@l_table_name VARCHAR(max))
	RETURNS varchar AS
BEGIN

	DECLARE @msg VARCHAR(max), @dmsg VARCHAR(max);
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF SUSER_SNAME() <> 'rif40' AND  IS_MEMBER(N'[rif_manager]') = 0 AND IS_MEMBER(N'[rif_user]')=0
		RETURN 0;

--		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_auto_indirect_checks', 'User % must be rif40 or have rif_user or rif_manager role', 
	
--
-- If inputs are NULL return NULL 
--
	IF @l_table_name IS NULL 
		RETURN NULL;

--
-- automatic indirect denominator checks
--
	DECLARE @isindirectdenominator int, @isnumerator int, @automatic int;
	
	SELECT @isindirectdenominator=isindirectdenominator, 
		@isnumerator=isnumerator, 
		@automatic=automatic
	FROM [rif40].[rif40_tables]
	WHERE table_name=@l_table_name;
	
	IF @automatic = 0 OR @isindirectdenominator != 1 OR @isnumerator = 1 
		RETURN NULL;
--		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_auto_indirect_checks', 'table: % is not an automatic indirect denominator', l_table_name::VARCHAR);
		
--
-- Check object is resolvable
--
	IF [rif40].[rif40_is_object_resolvable](@l_table_name) = 0
		RETURN NULL;
		--	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_auto_indirect_checks', 'table: % not resolvable', l_table_name::VARCHAR);
	

	IF @isindirectdenominator = 1 
	BEGIN
		DECLARE @geography VARCHAR(max), @total_denominators int;
		DECLARE @geo_curs CURSOR;
		
		SET @geo_curs = CURSOR FOR 	
			WITH valid_geog AS (
				SELECT g.geography
				FROM [rif40].[rif40_geographies] g
				WHERE [rif40].[rif40_num_denom_validate](g.geography, @l_table_name) = 1
			)
			SELECT valid_geog.geography geography, COUNT(t.table_name) total_denominators
			FROM [rif40].[rif40_tables] t, valid_geog 
			WHERE t.table_name            != @l_table_name
			AND t.isindirectdenominator = 1
			AND t.automatic             = 1
			AND [rif40].[rif40_num_denom_validate](valid_geog.geography, t.table_name) = 1
			GROUP BY valid_geog.geography;
	
		OPEN @geo_curs;
		FETCH NEXT FROM @geo_curs INTO @geography, @total_denominators;
		
		WHILE @@FETCH_STATUS = 0
		BEGIN
			IF @total_denominators > 0
			BEGIN
				IF (@msg IS NULL or @msg = '')
					SET @msg = '\n'+@geography+' '+convert(varchar,@total_denominators);
				ELSE
					SET @msg = @msg+', '+@geography+' '+convert(varchar,@total_denominators);
							
				DECLARE @ok_table_name	VARCHAR(max);
				DECLARE @check_tab_curs CURSOR;
				SET @check_tab_curs = CURSOR FOR
					SELECT t.table_name 
					FROM [rif40].[rif40_tables] t 
					WHERE t.table_name != @l_table_name
					AND t.isindirectdenominator = 1
					AND t.automatic             = 1
					AND [rif40].[rif40_num_denom_validate](@geography, t.table_name) = 1;
				FETCH NEXT FROM @check_tab_curs INTO @ok_table_name;
				
				WHILE @@FETCH_STATUS = 0
				BEGIN
					IF (@dmsg IS NULL or @dmsg = '')
						SET @dmsg = ' ('+@ok_table_name;
					ELSE
						SET @dmsg = @dmsg + ', '+@ok_table_name;
					FETCH NEXT FROM @check_tab_curs INTO @ok_table_name;
				END;
				IF @dmsg IS NOT NULL
					SET @dmsg = @dmsg +')';
				CLOSE @check_tab_curs;
				DEALLOCATE @check_tab_curs;
				--to log:
--					PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_auto_indirect_checks', 'table[%]: %, geography: %; total_denominators = %; %', 
--					j::VARCHAR, l_table_name::VARCHAR, c2_rec.geography::VARCHAR, c2_rec.total_denominators::VARCHAR, dmsg::VARCHAR);
			END
	--		ELSE
		--to log
--				PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_auto_indirect_checks', 'table[%]: %, geography: %; total_denominators = 0', 
--					j::VARCHAR, l_table_name::VARCHAR, c2_rec.geography::VARCHAR);
			FETCH NEXT FROM @geo_curs INTO @geography, @total_denominators;
		END;
		CLOSE @geo_curs;
		DEALLOCATE @geo_curs;
	END;
	RETURN @msg;
END;
GO

/*
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_auto_indirect_checks(character varying) TO public;
GRANT EXECUTE ON FUNCTION rif40_sql_pkg.rif40_auto_indirect_checks(character varying) TO rif40;
COMMENT ON FUNCTION rif40_sql_pkg.rif40_auto_indirect_checks(character varying) IS 'Function: 	rif40_auto_indirect_checks()
Parameters:	table name
Returns: 	NULL if not found or not an indirect denominator, geography and counts
Description:	Automatic (Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). 
		A user specific T_RIF40_NUM_DENOM is supplied for other combinations. 
		Cannot be applied to direct standardisation denominator) is restricted to 1 denominator per geography.';
*/