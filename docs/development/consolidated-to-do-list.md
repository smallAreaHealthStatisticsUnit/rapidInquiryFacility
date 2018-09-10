---
layout: default
title: Consolidated To-do List
---

**Created 2018-09-03** by Martin McCallion, from various documents relating to Kev’s departure a year or more ago, and existing `TODO.md` documents. I intend to turn everything  here into issues in the GitHub project, as that will be a better way of managing the project into the future.

## The Original Documents

The original documents are referred to below as:
* [redev -- "The RIF Redevelopment"]({{ site.baseurl }}/development/The-RIF-re-development)
* [kevroad - "Kev’s Suggested Road Map with the Middleware"]({{ site.baseurl }}/development/Kevs-Suggested-Road-Map-with-the-Middleware)
* [todo -- "RIF to-do List"]({{ site.baseurl }}/development/TODO).


## The List

Links to the original documents are in brackets. My comments are in bold.

- Optimise performance on very large datasets ([redev]({{ site.baseurl }}/development/The-RIF-re-development))
- Audit trail (kind of already there, in that SQL statements and similar are put into the log) ([redev]({{ site.baseurl }}/development/The-RIF-re-development))
- “New technical features will include enhancement of flexibility by clearly defined XML interfaces, giving the RIF a batch mode for the first time and allowing for the export of the data into other tools. Statistical processing will be built in modular manner so it can be easily extended. Additionally, there are plans to integrate RIF risk analysis with the BREEZE AERMOD / ISC new generation air quality modelling system (http://www.breeze-software.com/aermod/). It is also hoped to support Wind roses.” ([redev]({{ site.baseurl }}/development/The-RIF-re-development))
- “Existing RIF statistical functionality such as Satscan (for cluster dectection), INLA and LinBUGS/WinBUGS (for Bayesian Smoothing) will continue to be supported” -- **INLA is certainly used; not sure about the rest**. ([redev]({{ site.baseurl }}/development/The-RIF-re-development))
- Data Loader project ([kevroad]({{ site.baseurl }}/development/Kevs-Suggested-Road-Map-with-the-Middleware))
- “Eliminate `HealthOutcomeManager`” -- **I think this is out of date, as that class is actually used now**. ([kevroad]({{ site.baseurl }}/development/Kevs-Suggested-Road-Map-with-the-Middleware))
- ICD 9 (various) -- **This will be needed at some point**.
- Improve logging ([todo]({{ site.baseurl }}/development/TODO)) -- **I also want to do this; todo says “PH done September 2017”, so it’s talking about something else. I’d like to modernise the whole thing, switching to SLF4J  & Logback; and also vastly improve the internal handling**.
- “Issues with log4j log rotation” ([todo]({{ site.baseurl }}/development/TODO)) -- **Should be fixed by the above, or can be addressed separately**.
- Rengine not being shutdown correctly on reload of service ([todo]({{ site.baseurl }}/development/TODO)) -- **investigate**.
- **Review all non-completed front-end items in ([todo]({{ site.baseurl }}/development/TODO)) with Peter**
- “Missing information not stored in the database: Retrieve information on a completed study. Used in the info button in disease mapping and data viewer. The database cannot return all the required information. This requires changes to both the backend and middleware. [Planned PH]” -- **discuss with Peter** ([todo]({{ site.baseurl }}/development/TODO)).
- Setting various web headers ([todo]({{ site.baseurl }}/development/TODO))
