/*
 * SQL statement name: 	generate_series.sql
 * Type:				MS SQL Server SQL statement
 * Parameters:			None
 * Description:			Generate a series of values, from start to stop with a step size of step
 *						Original by: Simon Greener, Independent GeoSpatial Solutions Architect
 *						http://www.spatialdbadvisor.com/sql_server_blog/86/generate_series-for-sql-server-2008
 */
CREATE FUNCTION generate_series ( @p_start INT, @p_end INT, @p_step INT=1 )
RETURNS @Integers TABLE ( [IntValue] INT )
AS
BEGIN
    DECLARE
      @v_i                 INT,
      @v_step              INT,
      @v_terminating_value INT;
    BEGIN
      SET @v_i = CASE WHEN @p_start IS NULL THEN 1 ELSE @p_start END;
      SET @v_step  = CASE WHEN @p_step IS NULL OR @p_step = 0 THEN 1 ELSE @p_step END;
      SET @v_terminating_value =  @p_start + CONVERT(INT,ABS(@p_start-@p_end) / ABS(@v_step) ) * @v_step;
      -- Check for impossible combinations
      IF NOT ( ( @p_start > @p_end AND SIGN(@p_step) = 1 )
               OR
               ( @p_start < @p_end AND SIGN(@p_step) = -1 ))
      BEGIN
        -- Generate values
        WHILE ( 1 = 1 )
        BEGIN
           INSERT INTO @Integers ( [IntValue] ) VALUES ( @v_i )
           IF ( @v_i = @v_terminating_value )
              BREAK
           SET @v_i = @v_i + @v_step;
        END;
      END;
    END;
    RETURN
END