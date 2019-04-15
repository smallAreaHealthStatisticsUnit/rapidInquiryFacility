---
layout: default
title: Handover Notes for the Suspension of Development, April 2019
---

1. Contents
{:toc}

# Martin

## Status of Recent Work

Everything is up to date. The installer has been tested on Windows and Mac with both databases and works reliably. Multiple covariates work.

### Completed, but not in `master`

There is a branch called `installer_list_amend_scripts_in_single_place` which is not merged into `master`. It improves the installer, in that the current version in `master` has the various alter scripts listed in two places. The new branch will find any such scripts and run them as required, with no need to list them specifically.

We did not have time to test the changes as fully as we would like, so we have left the branch unmerged.

## Things I wanted to get done

There are [52 open issues](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues) at the time of writing. Some of them are mentioned below, but all are things that we could do with fixing, building, or improving.

### Making the Installer Handle Updates

The installer creates a brand-new database. It would be good to make it also handle upgrades from one version to the next. See [issue #146](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/146).

### Local Caching of Base Maps

See [issue #89](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/89). Support for using maps when no internet connection is available.

### Better Database Schema Management

See [issue #147](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/147). At present we maintain two sets of scripts for creating and updating the database schemas -- one for each platform. We'd like to move to using a single set of scripts to the extent that is possible, and Liquibase is a useful tool to help with that.

### Improving Database Access

I'd like to restructure the code so that the database access was all in classes that are intended for that. At present things are a bit of a mess, with SQL spread through various parts of the Java code.

Ideally, I think, I would move the whole database access to a separate microservice. This isn't strictly necessary, and it may be splitting things up further than is strictly needed, but it would be a good way to enforce the separation of concerns.

### The Roadmap

Not forgetting everything on [the RIF Roadmap](https://trello.com/b/CTTtyxJR/the-rif-roadmap).

## Contacting Me

If you need me to look at anything you can email me at <martin@devilgate.org>. I'm on Twitter at [@devilgate](https://twitter.com/devilgate). Also [LinkedIn](https://www.linkedin.com/in/martinmccallion/?originalSubdomain=uk), [Facebook](http://www.facebook.com/martin.mccallion).

# Peter

For really fine detail about everything that happens, see the older 
[Building and Installing the RIF section](../introduction/building-and-installation).

## Current State of the Development

   * [Risk analysis specific D3 graphs](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/127). Done;
   * [Multiple (used in statistical calculations) and additional (for use outside of the RIF) covariate support](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/124). One primary 
      covariate, multiple additional covariates, No support for multiple covariates in the calculation or results 
     (this reduce resource risk). Multiple additional covariates available in the extract. Done;
   * [Pooled or individual analysis for multiple risk analysis points/shapes (e.g COMARE postcodes)](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/129).
     Currently the RIF analyses there in N bands with all the sites as on. It is proposed to extend the RIF to support:
     * Individual site analysis;
     * Pooled analysis (1 or more groups of sites). Groups would be defined from a categorisation variable in the shapefile. Would require 
	   changes to:
       * Shapefile load screen and controller;
       * JSON study definition format;  
       * Study extract and result tables;
	   * R risk analysis code.
	 Front end complete with database changes (11/4/2019). Middleware and R support TODO;
	 
	 [Pooled or individual analysis for multiple risk analysis points/shapes (e.g COMARE postcodes)](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/129) 
     also fixes sizing problems study/comparison area map table (to the left of the map). It cannot be re-sized because
     of a bug in UI-grid this is issue [#154](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/154)

   * [Oracle interconnect](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/126). 
     Support linking RIF DB tables to Oracle master DB; done; documented in 
	 [Remote Data links](https://smallareahealthstatisticsunit.github.io/rapidInquiryFacility/rifDatabase/DataLoaderData/DataLoading#remote-data-links).
	
## RIF Test system

* The Statistics Service does not support TLS connections from the rifServices middleware 
  [issue #155](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/155)
* All scripts used on the private network are in *C:\RIFDEMO* on the RIF server and *G:\RIF\RIFDEMO* on the
  network;
* The RIF40_STARTUP stored procedure had to be edited slightly to run on the private network;
* Two years of HES data have been added (April 2016 to April 2018) using a materialized view 
  (V_HES_201617_APR2019) of a Postgres foreign data wrapper to an Oracle database;
* The Population model was extended from 2015 to 2018 using the 2014 population;
* Multiple covariates were enabled;
* Tests showed that the RIF does not check that the numerator and denominator are valid for the study period. No 
  denominator will result in an extract error;
* The Heathrow data was loaded into Oracle as: 
  * HEATHROW_COA2011;
  * RIF_TOTAL_CVD;
  * RIF_TOTAL_CHD;
  * RIF_TOTAL_STROKE;
* The RIF test system is Postgres only because of 
  [Issue #128 SQL Server SAHSU Database not linked to geography](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/128). 
  SQL Server SAHSU Database not linked to geography. This is a column length issue (i.e. there is a spurious space or two). Postgres is 
  fully functional [PH];
  
## Database Improvements needed before the Data Loader is built

The current scripts for creating a RIF database rely on a core set of scripts, and up to 14 alter scripts. The 
alter script in particular cause dependency issues when they modify trigger SQL. This needs to be resolved so 
that the RIF can manage its own data structures and to remove dependency issue using Git version control and 
dynamic triggers:

* Convert the RIF to use [Liquibase](https://www.liquibase.org/) to manage the existing database and the 
  data loading. This will require a Java console program to be run as the RIF40 schema owner and version 
  checks between the Front end, middleware and Liquibase change set;
* Move the existing state machine PL/pgSQL and T-SQL code that extracts a study into Java;
* Make the trigger code column name dynamic (requires *hstore* for Postgres, SQL Server can do it natively)

This is estimated at about 3 months work.

## Front End

The use of Angular.js is discussed in [RIF Front End](../rifWebApplication/rifFrontEnd.html).

## Contacting Me

If you need me to look at anything you can email me at: <phambly@fastmail.co.uk>. 