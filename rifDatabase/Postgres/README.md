# README file for the RIF Database layer Postgres port.

This README assumes a knowledge of how V3.1 RIF works.

See docs\BUILD.md for installation instructions. The original instructions in Install.docx are no longer maintained and the contents have been moved to 
the port specific markdown files. 

WARNING: The RIF requires Postgres 9.3 or 9.4 to work. 9.1 and 9.2 will not work. In particular PL/pgsql GET STACKED DIAGNOSTICS is used which 
is a post 9.2 option. 

The new V4.0 RIF uses either Postgres or Microsoft SQL server as a database backend.

## Development History

See also the [development log] 
(https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/Database%20Development%20Log.md)

Considerable work was done on improving RIF extract performance for the SAHSU Environment and Health Atlas; which the current RIF could not 
realistically cope  with as studies were taking days to run. After some experiment a single denominator driven extract was decided on, 
supporting multiple investigations (hence numerators) and covariates. The table structure is similar to the SAHSU population tables and 
suffers from the same performance problems. Essentialy, Oracle will do a full table scan of the population table unless it is forced not 
to use IOTs and common table expressions. The RIF extract tables will behave in the same manner; and Postgres behaves in the same way. The 
root cause of this is high speed of sequential disk scans, Oracle and Postgres both assume than it is always quicker to read the whole table 
in and not use the indexes. Using an Oracle 10053 trace to look at the cost based optimiser decison tree shows that it was close, but wrong. 
Using an Oracle Index organised table (cluster in Postgres land) forces the use of the index and the queries speeds up many, many times faster 
(20 minimum normally). As stated before Postgres behaves exactly the same. Adjusting the balance between sequential and random IO would help; 
but always run the risk that out of date table and index statistics or loosing the adjusting the balance between sequential and random IO will 
cause a sever performance problem. Clustering is far simpler. This wil be the subject of a blog. Final Oracle performance - Engand and Wales at 
Census output area level 1974-2009, 5 investigations, 58 million rows (2.2 GB data as a CSV) in 23 mins at a parallelisation of 2. Postgres will 
be at least 30% slower on similar hardware until the current parallelisation development is implemented.

The version 4.0 (V4.0) RIF database was created from version 3.1 in Oracle (itself a port of the V3.0 Access version). At this point primary, foreign 
keys and triggers were added and a number of improvements:

* Full support for multiple users (this was added via a column default to version 3.1). T_ tables have an associated view so the user can 
  only see their own data, or data shared to them by the RIF_MANAGER role
* Support for RIF_USER, RIF_MANAGER and RIF_STUDENT (restricted geolevels, low cell count suppression) roles added
* Investigation conditions and covariates were normalised
* Age, sex field support was added
* ICD9/10 support was made configurable and ICD oncology, UK HES operational codes added
* Dummy geospatial support added (i.e. the columns only)
* Automatic denominator/numerator support was added
* Basic auditing support
* Support for multiple denominators accross a study was discontinued. A study may use multiple numerators accross it investigations but 
  only one denominator.

The V3.0 data was then imported into V4.0 to test the triggers.

The Postgres port was created using ora2pg; which initally created foreign data wrapper tables for all the Oracle tables. This is the migration 
database sahsuland_v3_v4. The data was then dumped to CSV files.

The full V4.0 RIF schema was created using ora2pg and scripts. The triggers were ported and the basic PL/pgsql support added. The data 
was then imported. 

The new RIF database is being targeted as PostGres 9.3 or later; and will use features requiring this version.

Windows and most Linux distributions have pre built packages for Postgres. Building from source is also covered in the Linux build notes 

The RIF database design was reverse engineered, intially using the Oracle database modeler, and with the current Github release using 
[pgmodeler](https://github.com/pgmodeler/pgmodeler).

Geospatial support was then added; initially merely imported. Support was progressively added for geoJSON, simplification and geolevel 
intersection. A simple study extract was performed.

## Postgres Setup

Postgres is usually setup in one of three ways:
 
* Standalone mode on a Windows firewalled laptop. This uses local database MD5 passwords and no SSL and is not considered secure for network use.
* Secure mode on a Windows server and Active directory network. This uses remote database connections using SSL; with SSPI (Windows GSS 
  connectivity) for psql and secure LDAP for Java connectivity.
* Secure mode on a Linux server and Active directory network. This uses remote database connections using SSL; with GSSAPI/Kerberos for 
  psql and secure LDAP for Java connectivity.

Postgres can proxy users (see ident.conf examples in ????). Typically this is used to allow remote postgres administrator user authentication 
and to logon as the schema owner (rif40).



 

