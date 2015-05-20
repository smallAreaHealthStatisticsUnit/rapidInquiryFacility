


Create trigger tr_STUDY_AREAS
on [dbo].[T_RIF40_STUDY_AREAS]
for insert, update 
as

Declare  @xtype varchar(5)

	IF EXISTS (SELECT * FROM DELETED)
	BEGIN
		SET @XTYPE = 'D'
	END
	IF EXISTS (SELECT * FROM INSERTED)
		BEGIN
			IF (@XTYPE = 'D')
		BEGIN
			SET @XTYPE = 'U'
		END
	ELSE
		BEGIN
			SET @XTYPE = 'I'
		END
------------------------------------------------
--When Transaction is an Update  then rollback 
-------------------------------------------------
	IF (@XTYPE = 'U')
		BEGIN
		 	Raiserror (50100, 16,1 );
			rollback tran
		  end 
END



EXEC sp_addmessage 50100, 16, 
   N'Table_name: [dbo].[T_RIF40_STUDY_AREAS] , Update not allowed';
GO

