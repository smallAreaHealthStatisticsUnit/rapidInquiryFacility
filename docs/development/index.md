---
layout: default
title: Developer Documentation
---

## RIF Architecture

![RIF Architecture]({{ site.baseurl }}/development/RIF_architecture.png){:width="100%"}

## General Developer Documentation

* [Coding Standards]({{ site.baseurl }}/development/coding-standards) -- see this for our preferences for code formatting, and so on.
* [RIF design documentation on Sourceforge](http://rapidinquiryfacility.sourceforge.net/index.html)
* [Database Design]({{ site.baseurl }}/development/Database-design)
* [Database test harness]({{ site.baseurl }}/rifDatabase/TestHarness/db_test_harness/README)
* [General RIF Installation Notes for the Middleware and the Data Loader Tool]({{ site.baseurl }}/rifDatabase/General-RIF-Installation-Notes-for-the-Middleware-and-the-Data-Loader-Tool)
* [Opening the RIF front end in NetBeans8 for editing and debugging]({{ site.baseurl }}/development/Opening-the-RIF-front-end-in-NetBeans8-for-editing---debugging)
* [Opening the RIF repository in Eclipse for editing]({{ site.baseurl }}/development/Opening-the-RIF-repository-in-Eclipse-for-editing)
* [Setting up JRI in Eclipse]({{ site.baseurl }}/development/Setting-up-JRI-in-Eclipse)
* [RIF Front End Description]({{ site.baseurl }}/development/RIF-front-end-description)
* [Creating a new restful web service]({{ site.baseurl }}/development/Creating-a-new-restful-web-service)

## To-do Lists and Roadmaps

## Consolidated

I've put together a partial [consolidated to-do list]({{ site.baseurl }}/development/consolidated-to-do-list) based on what is still useful from the documents below. It still requires work, specifically discussion with the rest of the team. And the various points will then be extracted and turned into [Issues on GitHub](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues).

### Still Valid or Useful

These contain at least potentially useful information.

* [RIF to-do List]({{ site.baseurl }}/development/TODO) (Last changed March 2018) -- This has the most useful details of any of these files. Some things are genuinely still to be done. Some are done and marked as such. Some are done and not marked.
* [Node Services TODO]({{ site.baseurl }}/rifNodeServices/TODO) (Last changed October 2017) -- Some things here are probably still valid. Need to discuss with Peter.

### Obsolete

These documents are largely outdated, though they have some information about the history of the project and hints for future improvements.

* [RIF Re-development]({{ site.baseurl }}/development/The-RIF-re-development) -- This is a high-level overview, and out of date; though it does include some notes that explain some of the original plans.
* [Kev’s Suggested Road Map with the Middleware]({{ site.baseurl }}/development/Kevs-Suggested-Road-Map-with-the-Middleware) -- Some OK suggestions here, notably about reworking the web services and creating an ICD-9 service. Also tells us the name of the main class in the data loader.
* [Middleware Code Road Map]({{ site.baseurl }}/development/Kev-Code-Road-Map) -- Parts of this provide a decent overview of the project structure within the RIF. It’s out of date regarding the package structure and the way the database-related classes are split up, but it has some value. Ignore the coding conventions, though.
* [Middleware Handover Notes]({{ site.baseurl }}/development/Kev-Handover-Notes) -- This is essentially all out of date. The notes on the taxonomy services might be useful.
