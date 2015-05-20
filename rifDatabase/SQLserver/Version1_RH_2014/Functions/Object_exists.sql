CREATE FUNCTION dbo.ObjectExists(@Object VARCHAR(100), @Type VARCHAR(2)) RETURNS BIT
AS
BEGIN
  DECLARE @Exists BIT
  IF EXISTS(SELECT 1 FROM sys.objects 
  WHERE object_id = OBJECT_ID(@Object) AND type = (@Type))
    SET @Exists = 1
  ELSE
    SET @Exists = 0
  RETURN @Exists
END