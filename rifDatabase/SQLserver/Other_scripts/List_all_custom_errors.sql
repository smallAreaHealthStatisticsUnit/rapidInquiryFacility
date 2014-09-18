------------------------------------------
--list all registred custom error MSGs
------------------------------------------

select * 
FROM SYS.messages 
WHERE message_id>50000
