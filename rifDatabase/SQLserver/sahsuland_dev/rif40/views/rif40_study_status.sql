
--drop view if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_study_status]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_study_status]
END
GO

--view definition
CREATE VIEW [rif40].[rif40_study_status] AS 
SELECT c.username,
    c.study_id,
    c.study_state,
    c.creation_date,
	c.ith_update,
	c.message,
	c.trace
   FROM [rif40].[t_rif40_study_status] c
     LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
 GO
 
 --permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_status] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_status] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Status events for a given study.' , 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_status'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_status', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_status', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Study state: 
C: created, not verified; 
V: verified, but no other work done; 
E: extracted imported or created, but no results or maps created; 
G: Extract failure, extract, results or maps not created;
R: initial results population, create map table; 
S: R success;
F: R failure, R has caught one or more exceptions [depends on the exception handler design]
W: R warning.', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',
	@level1name=N'rif40_study_status', @level2type=N'COLUMN',@level2name=N'study_state'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Creation date', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',
	@level1name=N'rif40_study_status', @level2type=N'COLUMN',@level2name=N'creation_date'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Update number (for ordering)', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',
	@level1name=N'rif40_study_status', @level2type=N'COLUMN',@level2name=N'ith_update'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Status message', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',
	@level1name=N'rif40_study_status', @level2type=N'COLUMN',@level2name=N'message'
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Trace message; includes exception where relevant', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',
	@level1name=N'rif40_study_status', @level2type=N'COLUMN',@level2name=N'trace'
GO