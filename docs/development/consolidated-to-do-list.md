---
layout: default
title: Consolidated To-do List
---

**Created 2018-09-03** by Martin McCallion, from various documents relating to Kev’s departure a year or more ago, and existing `TODO.md` documents. I intend to turn everything  here into issues in the GitHub project, as that will be a better way of managing the project into the future.

The original docs are referred to below as [redev](/development/The-RIF-re-development), [kevroad](/development/Kevs-Suggested-Road-Map-with-the-Middleware), [hand](/development/Kev-Handover-Notes), and [todo](/development/TODO).

- Optimise performance on very large datasets (redev)
- Audit trail (kind of already there, in that SQL statements and similar are put into the log) (redev)
- “New technical features will include enhancement of flexibility by clearly defined XML interfaces, giving the RIF a batch mode for the first time and allowing for the export of the data into other tools. Statistical processing will be built in modular manner so it can be easily extended. Additionally, there are plans to integrate RIF risk analysis with the BREEZE AERMOD / ISC new generation air quality modelling system (http://www.breeze-software.com/aermod/). It is also hoped to support Wind roses.” (redev)
- “Existing RIF statistical functionality such as Satscan (for cluster dectection), INLA and LinBUGS/WinBUGS (for Bayesian Smoothing) will continue to be supported” -- **INLA is certainly used; not sure about the rest**. (redev)
- Data Loader project (kevroad)
- “Eliminate `HealthOutcomeManager`” -- **I think this is out of date, as that class is actually used now**. (kevroad)
- ICD 9 (various) -- **This will be needed at some point**.
- Improve logging (todo) -- **I also want to do this; todo says “PH done September 2017”, so it’s talking about something else. I’d like to modernise the whole thing, switching to SLF4J  & Logback; and also vastly improve the internal handling**.
- “[I]ssues with log4j log rotation” (todo) -- **Should be fixed by the above, or can be addressed separately**.
- Rengine not being shutdown correctly on reload of service (todo) -- **investigate**.
- **Review all non-completed front-end items in (todo) with Peter**
- “Missing information not stored in the database: Retrieve information on a completed study. Used in the info button in disease mapping and data viewer. The database cannot return all the required information. This requires changes to both the backend and middleware. [Planned PH]” -- **discuss with Peter** (todo).
- Setting various web headers (todo)
