--
-- Exmaple user creation script
--
-- In psql as postgres:
--
CREATE ROLE mdouglas LOGIN
  NOSUPERUSER INHERIT NOCREATEDB CREATEROLE NOREPLICATION;
GRANT rif_manager TO mdouglas;
GRANT rif_user TO mdouglas;
CREATE SCHEMA mdouglas
  AUTHORIZATION mdouglas;
GRANT ALL ON SCHEMA mdouglas TO mdouglas;
/*
Edit 

1) pg_ident.conf for user mappings

#
sahsuland	mdouglas		pop
sahsuland	mdouglas		gis
sahsuland	mdouglas		rif40
sahsuland	mdouglas		mdouglas
#
sahsuland_dev	mdouglas		pop
sahsuland_dev	mdouglas		gis
sahsuland_dev	mdouglas		rif40
sahsuland_dev	mdouglas		mdouglas
sahsuland_dev	mdouglas		postgres

2) pg_hba.conf to add user remote client (wpea-mdouglas in this case)

hostssl	sahsuland	all	 	146.179.138.8 		255.255.255.255	sspi 	map=sahsuland
hostssl	sahsuland_dev	all	 	146.179.138.8 		255.255.255.255	sspi 	map=sahsuland_dev

 */

--
-- Eof
