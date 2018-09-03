# Welcome to the Rapid Inquiry Facility Documentation Site
{:.no_toc}

1. Contents
{:toc}

# What is the RIF?

An overview of the RIF, along with details of its purpose and its creators  [can be found here](/introduction/what-is-the-RIF).

# Licensing

The RIF is licensed under the [GNU LESSER GENERAL PUBLIC LICENSE](https://www.gnu.org/licenses/lgpl-3.0.en.html) and is freely available
from the [RIF GitHub](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/) repository.

# Building and installation

[See this document](/introduction/building-and-installation) for details on how to build and install the RIF.

# Other Manuals

The RIF has the following manuals:

- [RIF 4.0 Manual](/standalone/RIF_v40_Manual.pdf)
- [RIF 4.0 Data Loader Manual](/standalone/RIF_Data_Loader_Manual.pdf)
- [RIF Web Application and Middleware Installation](/rifWebApplication/Readme)
- [Windows Postgres Install using pg_dump and scripts](/rifDatabase/Postgres/production/windows_install_from_pg_dump)
- [SQL Server Production Database Installation](/rifDatabase/SQLserver/production/INSTALL)
- [Manual data loading](/rifDatabase/DataLoaderData/DataLoading)
- [Tile maker Manual](/rifNodeServices/tileMaker)
- [Database Management Manual](/rifDatabase/databaseManagementManual)

# For Developers

## RIF Architecture

![RIF Architecture](/development/RIF_architecture.png)

This section is a series of links to developer documentation

* [Coding Standards](/development/coding-standards) -- see this for our preferences for code formatting, and so on.
* [RIF design documentation on Sourceforge](http://rapidinquiryfacility.sourceforge.net/index.html)
* [Database Design](/development/Database-design)
* [Database test harness](/rifDatabase/TestHarness/db_test_harness)
* [General RIF Installation Notes for the Middleware and the Data Loader Tool](/rifDatabase/General-RIF-Installation-Notes-for-the-Middleware-and-the-Data-Loader-Tool)
* [Opening the RIF front end in NetBeans8 for editing and debugging](/development/Opening-the-RIF-front-end-in-NetBeans8-for-editing---debugging)
* [Opening the RIF repository in Eclipse for editing](/development/Opening-the-RIF-repository-in-Eclipse-for-editing)
* [Setting up JRI in Eclipse](/development/Setting-up-JRI-in-Eclipse)
* [RIF Front End Description](/development/RIF-front-end-description)
* [Creating a new restful web service](/development/Creating-a-new-restful-web-service)

## To-do Lists and “Roadmaps”

None of these is close to an actual product roadmap, but most of them contain something useful. They are shown with the latest change date where we know that.

* [RIF to-do List](/development/TODO) (March 2018) -- This has the most useful details of these files. Some things are genuinely still to be done. Some are done and marked as such. Some are done and not marked. We’ll turn the ones that are still relevant into issues on GitHub.
* [Node Services TODO](/rifNodeServices/TODO) (October 2017) -- Some things here are probably still valid. Need to discuss with Peter.
* [RIF Re-development](/development/The-RIF-re-development) -- This is a high-level overview, and out of date.
* [Kev’s Suggested Road Map with the Middleware](/development/Kevs-Suggested-Road-Map-with-the-Middleware) -- Some OK suggestions here, notably about reworking the web services and creating an ICD-9 service. Also tells us the name of the main class in the data loader.
* [Middleware Code Road Map](/development/Kev-Code-Road-Map) -- Parts of this provide a decent overview of the project structure within the RIF. It’s out of date regarding the package structure and the way the database-related classes are split up, but it has some value. Ignore the coding conventions, though.
* [Middleware Handover Notes](/development/Kev-Handover-Notes) -- This is essentially all out of date. The notes on the taxonomy services might be useful.
