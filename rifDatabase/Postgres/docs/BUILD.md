# RIF40 Postgres database build from Github

## Database development environment

Current two databases are used: *sahsuland* and *sahsuland_dev*. The installer database *sahsuland* is kept stable for long periods, 
*sahsuland_dev* is the working database. Both *sahsuland* and *sahsuland_dev* are built with the completed alter 
scripts. Instructions to build the *sahsuland_dev* database to include the alter scripts under development are included.

After the move from the SAHSU private network to the main 
Imperial network the database structure is only modified by alter scripts so that the SAHSU Private network 
and Microsoft SQL Server Ports can be kept up to date. Alter scripts are designed to be run more than once without 
side effects, although they must be run in numeric order.

Once the RIF is in production alter scripts will only be able to complete successfully once and cannot then 
be re-run.

The directory structure is rifDatabase\Postgres\:

* conf            - Exmaples of varaious configuration files
* etc             - Postgres psql logon script (.psqlrc)
* example_data    - Example extracts and results creating as part of SAHSUland build
* logs            - Logs from psql_scripts runs
* PLpgsql         - PL/pgsql scripts
* psql_scripts    - PSQL scripts; **Postgres database build directory**
* sahsuland       - SAHSUland creation psql scripts
* sahsuland\data  - SAHSUland data
* shapefiles      - Postgres psql SQL scripts derived from shapefiles, creation scripts
* shapefiles\data - Shapefiles

The databases *sahsuland* and *sahsuland_dev* are built using make and Node.js. 

## Tool chain 

On Windows:

* Install MinGW with the full development environment and add *c:\MinGW\msys\1.0\bin* to path
* Install Python 2.7 **not Python 3.n**
* Install a C/C++ compiler, suitable for use by Node.js. I used Microsoft Visual Studio 2012 
  under an Academic licence. The dependency on Node.js will be reduced by replacing the current 
  program with Node.js web services available for use by the wider RIF community.

On Windows, Linux and MacOS

* Install Postgres 9.3.5+ or 9.4.1+ and PostGIS 2.1.3+. Building will fail on Postgres 9.2 or 
  below as PL/pgsql GET STACKED DIAGNOSTICS is used; PostGIS before 2.1.3 has bugs that will cause 
  shapefile simplification to fail.
* Install 64 bit Node.js. The 32 bit node will run into memory limitation problems will vary large 
  shapefiles. node.js is required to build to geoJSON to topoJSON converter by Mike Bostock at: 
  (https://github.com/mbostock/topojson/wiki/Installation). Node.js is available from: (http://nodejs.org/)
* Install R; integrate R into Postgres [optional R integration](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/plr.md)

### Pre build tests

Run up a command tool, *not* in the Postgres build directory (psql_scripts):

* Type *make*. Check make works correctly:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres>make
make: *** No targets specified and no makefile found.  Stop.
```
* Check logon to psql with a password for:
  * postgres
* Check Node.js is correctly installed; *cd GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node*; *make install topojson*. 
  The [Node.js install dialog]
(https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/Node/Node%20Install.md) is provided as an example.
  **If the toposjon node.js does not run correctly then node is not properly installed**. 
* Check R is integrated into Postgres as PLR (See the PLR build instructions).

### Build control using make

The RIF database is built using GNU make for ease of portability. The RIF is built from the directory 
*GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts*.

#### Principal targets

* Create sahsuland_dev and sahusland:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node> make db_setup
```
   **Note that this does not apply the alter scripts under development**
* To build or rebuild sahsuland_dev:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node> make clean sahsuland_dev
```
   **Note that this does not apply the alter scripts under development**
* To build or rebuild sahsuland_dev:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node> make clean sahsuland_dev
```
#### Configuring make

**Do not edit the Makefile directly; subsequent git changes will cause conflicts and you may loose 
your changes.** Instead copy Makefile.local.example to Makefile.local and edit Makefile.local. Beware 
of your choice of editor on especially Windows. The RIF is developed on Windows and Linux at the same time so 
you will have files with both Linux <CR> and Windows <CRLF> semantics. Windows Notepad in particular 
does not understand Linux files.



#### Porting limitations

Makefiles have the following limitations:

* Full dependency tracking for SQL scripts has not yet been implemented; you are advised to do a *make clean* 
  or *make devclean* before building as in the below examples or nothing much may happen.
* A fully working version of Node.js that can compile is required or you will not be able to generate the 
  topoJSON tiles data.

#### Help

The Makefile has [help](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/psql_scripts/Make%20Help.md):

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>make help
```
 
