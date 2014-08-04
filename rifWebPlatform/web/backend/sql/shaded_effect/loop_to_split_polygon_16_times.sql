--drop table gid1;
--create table gid1 as

drop table if exists cuts;
create table cuts(geom geometry,portions varchar);

DO $$
DECLARE
	half record;
	quarter record;
	octave record;
	sixteenth record;
	thirtysecond record;
BEGIN
	drop table if exists halves;
	select halfpolygonline(geom) as geom into half from e_wards where gid = 3;
	
	create temp table halves as select (st_dump(st_split(geom , halfpolygonline(geom)))).geom as geom from e_wards where gid = 3;
	insert into cuts(geom,portions) select halfpolygonline(geom), 'half' from halves;
	
	FOR half IN select * from halves LOOP

	    drop table if exists quarters;	
	    create temp table quarters as select  (st_dump(st_split(half.geom,halfpolygonline(half.geom)))).geom as geom;
	    insert into cuts(geom,portions) select geom,'quarters' from quarters;
	   /*FOR quarter IN select * from quarters  LOOP 	

		drop table if exists octaves;	
	        create temp table octaves as select  halfpolygon(quarter.geom) as geom;

	        FOR octave IN select * from octaves  LOOP 	
	        
			drop table if exists sixteenths;	
			create temp table sixteenths as select  halfpolygon(octave.geom) as geom;

			FOR sixteenth IN select * from sixteenths  LOOP 	
	        
				drop table if exists thirtyseconds;	
				create temp table thirtyseconds as select  halfpolygon(sixteenth.geom) as geom;
				insert into cuts (geom) select * from 	thirtyseconds;
			
			END LOOP;
			
		END LOOP; 

           END LOOP;  */
           
                 
         END LOOP;
	

END;
$$language plpgsql;