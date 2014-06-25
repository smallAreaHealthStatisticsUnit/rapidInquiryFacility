DO $$DECLARE
 agegroup integer :=  1;
 gids CURSOR FOR SELECT gid FROM e_wards ORDER BY gid;
 gid integer;
BEGIN
 FOR gid IN gids LOOP
    agegroup := 1;	
  WHILE agegroup <= 18 LOOP
    insert into e_wards_agegroups values (2014, trunc(random() * 10000 + 1),agegroup,2,gid.gid );	
    agegroup := agegroup + 1;
  END LOOP;
 END LOOP; 
END;
$$;			 