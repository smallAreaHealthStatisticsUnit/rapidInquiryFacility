CDC Update - November 2017
==========================

# Highlights

* SQL Server port now has exactly the same functionality as the Postgres port;
* Data export of data extract and results now works; user can download a ZIP file and re-run
  the R side of the analysis;
* A SEER test dataset has been created for both ports in preparation for SAHSU internal testing;
* SAHSU is about to test both the SQL Server and Postgres ports internally;
* About to start security testing;
* A new Java developer to replace is being interview for **UPDATE**;
* David has now left (to work for TomTom in Belgium);
* Peter is now working on the Front End having taking over from David;
* Logging using Log4j has been implemented by Peter and the middleware now correctly reports errors to
  the front end. Log4j was chosen as the CDC use it. It is now much easier to trace errors (even 
  in R!);
* The SAHSUland test data has been improved;
* Documentation has been overhauled (partly in the handovers from Kev and David, partly in preparation for 
  the new JAva developer)
  
# Next tasks

* Complete security testing;
* Get CDC test system up to date;
* Prepare for SAHSU internal testing.

# Risk Analysis

* Work needs to be carried out principally in the Middleware. This will be the task of the new Java 
  developer unless we fail to hire. In this case Peter will start this instad of the maps generation;
* Some work in envsaiged in the smoothing functions and in the database; we are intending to test
  cluster anaysis using:
  * BayesSTDetect using Winbugs called from R 
  * SatScan or Scanstatistics; both called from R 

# Maps generation

* It is intended to add a map generator to the export ZIP file to create high quality outputs suitable
  for journals;
* The RIF will remember the users choices in the data and map viewer panels;  
* The export zip generator will build the maps;
* Results in shapefile form will probably be added;
* Users will be able to regenerate the maps outside of the RIF.

# SEER Test data

| cb_2014_us_state_500k |  areaname   | start | end  |  cases  |
|-----------------------|-------------|-------|------|---------|
| 01785533              | Alaska      |  1992 | 2013 |    8080 |
| 01779778              | California  |  1973 | 2013 | 3185854 |
| 01779780              | Connecticut |  1973 | 2013 |  753261 |
| 01705317              | Georgia     |  1975 | 2013 |  810910 |
| 01779782              | Hawaii      |  1973 | 2013 |  201078 |
| 01779785              | Iowa        |  1973 | 2013 |  632565 |
| 01779786              | Kentucky    |  2000 | 2013 |  366513 |
| 01629543              | Louisiana   |  2000 | 2013 |  340659 |
| 01779789              | Michigan    |  1973 | 2013 |  854021 |
| 01779795              | New Jersey  |  2000 | 2013 |  752599 |
| 00897535              | New Mexico  |  1973 | 2013 |  267661 |
| 01455989              | Utah        |  1973 | 2013 |  260674 |
| 01779804              | Washington  |  1974 | 2013 |  743088 |

## Test case 1004 

File: 

* Exclude Alaska and Hawaii so the maps are focused on the 49 "mainland" states (i.e. they look better)
* Lung cancer defined: C33,C340,C341,C342,C343,C348,C349
* Period 2000 to 2013 (i.e. the maximum allowed for the data)
* Covariate: head of house median income quintile

Run times:

* Postgres (constrained to 50M RAM): 7:43
* SQL Server:
* R with BYM model: 30s

 

  