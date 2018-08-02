# PL/R on Windows

PL/R is a loadable procedural language that enables you to write PostgreSQL functions and triggers in the R programming 
language. PL/R offers most (if not all) of the capabilities a function writer has in the R language.

PL/R install notes can be found at: (http://www.bostongis.com/PrinterFriendly.aspx?content_name=postgresql_plr_tut01)

## Installation instructions:

* Extract plr.dll/sql from ZIP to:
  * plr*.sql, plr.control in: C:\Program Files\PostgreSQL\9.3\share\extension
  * plr.dll in: C:\Program Files\PostgreSQL\9.3\lib
* Set R_HOME;  e.g. R_HOME=C:\Program Files\R\R-2.15.3
* Add R 64 bit bin to path, e.g.: C:\Program Files\R\R-2.15.3\bin\x64
* Create test extension in Postgres database:
```
sahsuland=# create extension plr;
CREATE EXTENSION
```
### Common errors:
* No control file
```
sahsuland=# create extension plr;
ERROR:  could not open extension control file "C:/Program Files/PostgreSQL/9.1/share/extension/plr.control": No such file or directory
```
   **Rename plr--8.3.0.14.sql to plr.sql**
* Missing version specific extension creation script
```
sahsuland=# create extension plr;
ERROR:  could not stat file "C:/Program Files/PostgreSQL/9.1/share/extension/plr--8.3.0.14.sql": No such file or directory
```
  **Rename plr.sql to plr--8.3.0.14.sql**
* Cannot find R DLL
```
ERROR: could not load library "C:/Program Files/PostgreSQL/9.3/lib/plr.dll": unknown error 126 
SQL state: 58P01
```
  **Add R_HOME and 32/64 bit R bin directory (as appropriate) to system path**
