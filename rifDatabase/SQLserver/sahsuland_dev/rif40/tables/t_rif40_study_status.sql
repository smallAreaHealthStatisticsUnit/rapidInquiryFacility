/*
removed foreign key temporarily
*/


--drop if already exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_study_status]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_study_status]
END
GO

--table definition

CREATE TABLE [rif40].[t_rif40_study_status](
	[username] 		[varchar](90) NOT NULL DEFAULT (user_name()),
	[study_id] 		[integer] NOT NULL CONSTRAINT t_rif40_study_status_study_id_seq DEFAULT ([rif40].[rif40_sequence_current_value](N'rif40.rif40_study_id_seq')),
	[study_state] 	VARCHAR(1) NOT NULL CONSTRAINT [check_study_state]
			CHECK (study_state IN ('C', 'V', 'E', 'G', 'R', 'R', 'S', 'F', 'W')),
	[creation_date] DATETIME NOT NULL DEFAULT getdate(),
	[ith_update] 	INTEGER NOT NULL,
	[message] 		NVARCHAR(MAX),
	[trace] 		NVARCHAR(MAX)
 CONSTRAINT [t_rif40_study_status_pk] PRIMARY KEY CLUSTERED 
(
	[study_id], [study_state]
)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) 
ON [PRIMARY],
CONSTRAINT [t_rif40_studystatus_study_id_fk] FOREIGN KEY ([study_id])
      REFERENCES [rif40].[t_rif40_studies] ([study_id])
      ON UPDATE NO ACTION ON DELETE NO ACTION
) ON [PRIMARY]
GO

--permissions


--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Status events for a given study.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_status'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_status', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_status', @level2type=N'COLUMN',@level2name=N'study_id'
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
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',
	@level1name=N't_rif40_study_status', @level2type=N'COLUMN',@level2name=N'study_state'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Creation date', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',
	@level1name=N't_rif40_study_status', @level2type=N'COLUMN',@level2name=N'creation_date'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Update number (for ordering)', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',
	@level1name=N't_rif40_study_status', @level2type=N'COLUMN',@level2name=N'ith_update'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Status message', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',
	@level1name=N't_rif40_study_status', @level2type=N'COLUMN',@level2name=N'message'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Trace message; includes exception where relevant', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',
	@level1name=N't_rif40_study_status', @level2type=N'COLUMN',@level2name=N'trace'
GO

--indices
CREATE INDEX t_rif40_study_status_uname
  ON [rif40].[t_rif40_study_status](username)
GO

