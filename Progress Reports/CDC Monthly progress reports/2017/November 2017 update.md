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

  
 

  