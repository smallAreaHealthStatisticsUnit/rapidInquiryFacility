--
-- Test case a)
--	
DO LANGUAGE plpgsql $$
DECLARE
	errors INTEGER:=0;
--
	error_message VARCHAR;
--
	v_sqlstate 	VARCHAR;
	v_context	VARCHAR;
	v_detail 	VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN	
	IF NOT (rif40_sql_pkg.rif40_sql_test(
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
		'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][]
		/* Use defaults */)) THEN
        errors:=errors+1;
    END IF;		
--
-- DELIBERATE FAIL: level 3: 01.015.016900 removed
--
	IF (rif40_sql_pkg.rif40_sql_test(
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016200'') ORDER BY level4',
		'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200 [DELIBERATE FAIL TEST]',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3} 
		,{01,01.015,01.015.016200,01.015.016200.4} 
		,{01,01.015,01.015.016900,01.015.016900.1} 
		,{01,01.015,01.015.016900,01.015.016900.2} 
		,{01,01.015,01.015.016900,01.015.016900.3} 
		}'::Text[][],
		'1224' /* Expected SQLCODE */, 
		FALSE /* Do not RAISE EXCEPTION on failure */)) THEN
        errors:=errors+1;
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_sql_test', 
			'DELIBERATE FAIL TEST PASSED.');
    END IF;		

	IF NOT (rif40_sql_pkg.rif40_sql_test(	
		'INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #1'', ''EXTRACT_TRIGGER_TEST'', ''MAP_TRIGGER_TEST'', 1 /* Diease mapping */, ''LEVEL1'', ''LEVEL4'', ''SAHSULAND_POP'', NULL /* FAIL HERE */)',
		'TRIGGER TEST #1: rif40_studies.suppression_value IS NULL',
		NULL::Text[][] 	/* No results for trigger */,
		'23502' 		/* Expected SQLCODE */, 
		FALSE 			/* Do not RAISE EXCEPTION on failure */)) THEN
		errors:=errors+1;
    END IF;	
--	
	IF errors = 0 THEN
		RAISE NOTICE 'No test harness errors.';		
	ELSE
		RAISE EXCEPTION 'Test harness errors: %', errors;
	END IF;
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		GET STACKED DIAGNOSTICS v_sqlstate = RETURNED_SQLSTATE;
		GET STACKED DIAGNOSTICS v_context = PG_EXCEPTION_CONTEXT;
		error_message:='Test harness caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||
			'Detail: '||v_detail::VARCHAR||E'\n'||
			'Context: '||v_context::VARCHAR||E'\n'||
			'SQLSTATE: '||v_sqlstate::VARCHAR;
		RAISE EXCEPTION '1: %', error_message;
END;
$$;
  
--
-- Testing stop
--
--DO LANGUAGE plpgsql $$
--BEGIN
--	RAISE EXCEPTION 'Stop processing';
--END;
--$$;

--
-- Eof