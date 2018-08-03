Welcome to the Rapid Inquiry Facility Documentation Site
========================================
# Contents

- [1. What is the RIF?](#1-what-is-the-rif)
  - [1.1 Study Aims](#11-study-aims)
  - [1.2 References](#12-references)
- [2. What does the RIF do?](#2-what-does-the-rif-do)
  - [2.1 Screen Shots](#21-screen-shots)
    - [2.1.1 Screenshot of the old RIF 3.x](#211-screenshot-of-the-old-rif-3x)
    - [2.1.2 Screenshots of the new RIF 4.0](#212-screenshots-of-the-new-rif-40)
- [3. Who has authored the RIF?](#3-authors)
- [4. Who funds the development?](#4-funders)
  - [4.1 Funders](#4-funders)
  - [4.2 Other Contributors](#4-other-contributors)
- [5. What are the licensing terms?](#5-licensing)
- [6. How do you build and install the RIF?](#6-build-and-install)
  - [6.1 Postgres](#61-postgres)
    - [6.1.1 Building the RIF Postgres Database](#611-building-the-rif-postgres-database)
    - [6.1.2 Installing the RIF Postgres Database](#612-installing-the-rif-postgres-database)
  - [6.2 Microsoft SQL Server](#62-microsoft-sql-server)
    - [6.2.1 Building the RIF SQL Server Database](#621-building-the-rif-sql-server-database)
    - [6.2.2 Installing the RIF SQL Server Database](#622-installing-the-rif-sql-server-database)
  - [6.3 Web Services](#63-web-services)
    - [6.3.1 Taxonomy Services](#631-taxonomy-services)
  - [6.4 Data Loading](#64-data-loading)
    - [6.4.1 Manual Data Loading](#641-manual-data-loading)
    - [6.4.2 Using the Data Loading Tool](#642-using-the-data-Loading-tool)
- [7. Are there manuals available?](#7-manuals)
- [8. Are there resources for developers?](#8-for-developers)
- [9. What is left TODO?](#9-todo-list)

# 1. What is the RIF?

The Rapid Inquiry Facility (RIF) is a freely available software application that supports two types of environmental health activities:
disease mapping studies and risk analysis studies. It facilitates interrogation of databases containing geographical, health, population
and risk factor (e.g. deprivation) data to produce estimates of disease risk in specified areas and to conduct disease mapping in
those areas using advanced statistical techniques. The RIF software is unique and only available from SAHSU. It was designed to help
epidemiologists and public health researchers to rapidly investigate potential environmental hazards, especially those related to
industrial sites. The tool uses health, environmental, socio-economic, population and geographic data to calculate risks in relation
to sources of exposure and to generate maps.

## 1.1 Study Aims

Disease mapping studies are used to visualise mortality or morbidity rates and risks across an area.  They are used to explore spatial
patterns of health outcomes; identify potential issues regarding data quality by geographical area; and identify areas which need
additional study.

Risk analysis studies are used to provide an initial investigation into whether a suspected source of some particular exposure is having
an impact on health in a local population.  The tool can generate standardised rates and relative risks for a set of health outcomes.
The RIF allows for full flexibility in the selection of a range of ages; a time frame specifying an era for data set collections; and
a geographic area.

The software application was originally developed in the late 1990s by SAHSU staff. The most widely used RIF distribution
(version 3.x) an embedded plug-in for ArcGIS 9.x has been employed by many institution and public health practitioners around the world
to both automatically generate disease maps and asses disease risk in proximity to known source of pollution.

## 1.2 References

* P Aylin, R Maheswaran, J Wakefield, S Cockings, L Jarup, R Arnold, G Wheeler, P Elliott.
  A national facility for small area disease mapping and rapid initial assessment of apparent disease clusters around a point source:
  the UK Small Area Health Statistics Unit.
  J Public Health 1999; 21(3): 289-298. doi:10.1093/pubmed/21.3.289
* L Jarup.
  Health and Environment Information Systems for Exposure and Disease Mapping, and Risk Assessment.
  Environ Health Perspect 2004; 112(9): 995–997. doi:  10.1289/ehp.6736.
* Juhasz A, Nagy C, Paldy A, Beale, L, 2009,
  Development of Deprivation Index and its relation to premature mortality due to diseases of circulatory system in Hungary, 1998-2004.
  Social Science & Medicine, Elsevier, vol. 70(9), pages 1342-1349.
* Holowaty EJ, Norwood TA, Wanigaratne S, Abellan JJ, Beale L, 2010,
  International Feasibility and utility of mapping disease risk at the neighbourhood level within a Canadian public health unit: an
  ecological study.
  International Journal of Health Geographics, 9:21.
* L Beale, S Hodgson, JJ Abellan, S LeFevre, L Jarup.
  Evaluation of Spatial Relationships between Health and the Environment: The Rapid Inquiry Facility.
  Environ Health Perspect 2010;118:1306-1312.

# 2. What does the RIF do?

The RIF automatically generates contextual maps showing the area under study. A report is generated summarising the study details, and
reporting the crude and adjusted rates and risks for each health outcome investigated. Graphs comparing the age, gender and
socio-economic (or other covariate) structure of the study, and comparison populations, are provided to aid interpretation.

As well calculating the rates and relative risks (and associated 95% confidence intervals) for each exposure group or distance band,
the RIF also runs Chi-square tests for homogeneity and linear trends to test the global association between distance/exposure covariate
and disease risk. In disease mapping analyses, maps showing crude, adjusted and smoothed risks by area are also displayed.
Demonstrations of the current RIF are available. These are large Windows Media Video (.wmv) files, and must first be unzipped
(use WinZip on Windows), and then they can be viewed using Windows Media Player.

[Old V3 RIF Disease mapping example video LINK BROKEN - FIX](http://www.sahsu.org/Disease%20Mapping%20Demo.zip)

## 2.1. Screen Shots

### 2.1.1 Screenshot of the old RIF 3.x

The old RIF was an embedded plug-in for ArcGIS 9 and used Oracle or access as a database backend. It was written in VBA

![RIF 3.0 screenshot](http://www.sahsu.org/sites/impc_sahsu/files/rif%203%20screenshot.png)

### 2.1.2 Screenshots of the new RIF 4.0

The Rapid Inquiry Facility (RIF) is a freely available software application that supports two types of environmental health activities:
disease mapping studies and risk analysis studies.

It was designed to help epidemiologists and public health researchers to rapidly investigate potential environmental hazards, especially
those related to industrial sites. The tool uses health, environmental, socio-economic, population and geographic data to calculate risks
in relation to sources of exposure and to generate maps.

The RIF 4.0 is an open source, freely accessible web platform which can be used off and on-line. It uses a three layers framework in which
all data can be stored locally or remotely on a spatially enabled database, PostGIS. This is  directly linked to a Java middleware whose
role is to check, validate and secure all communications between the graphical user interface, a JavaScript/HTML5 platform, and the database.

The RIF 4.0 integrates advanced methods in statistics, exposure assessment and data visualization. It is integrated with R and offers
linkage to external software for the assessment  of air pollution exposure and noise. Each block has been developed independently using
a mixture of technologies which are connected together using an abstract interface (Middle layer).

Oncde the RIF is set up the user can logon to the SQL Server or Postgres database:

![RIF 4.0 login screen](/Screenshots/RIF_login.png)

To create a RIF disease map:

* Study area
* Comparision area
* The disease to the mapped (the investigation)
* The statistical outputs required

Study area  section. Comparision are selection is identical.

![RIF 4.0 study area selection](/Screenshots/RIF_studyarea.png)

This shows an investigation being setup.

![RIF 4.0 investigation setup](/Screenshots/RIF_investigation.png)

This box shows the choosing of the statistical methods.

![RIF 4.0 Statistics Dialog](/Screenshots/RIF_statistical_methods.png)

The study is then submitted. It is checked and then saved in the database and a procedure called to extract the required data. This is then
passed to R to perform the required statistics, with the results saved to the database.

![RIF 4.0 study submission](/Screenshots/RIF_submission.png)

When the study is complete the user is informed and the data can be viewed or mapped. The data can additionally be exported to the user as a zip file
for further analysis.

![RIF 4.0 data viewer](/Screenshots/RIF_viewer.png)

The maps displayed here use the synthetic test dataset, *SAHSULAND* are are using the classification scheme from the
[THE ENVIRONMENT AND HEALTH ATLAS FOR ENGLAND AND WALES](http://www.envhealthatlas.co.uk/homepage/)

![RIF 4.0 mapping](/Screenshots/RIF_mapping.png)

Finally, the user can export the study data for further analysis and inclusion in papers and reports.

![RIF 4.0 data export](/Screenshots/RIF_export.png)

# 3. Authors

The RIF was developed by the [The Small Area Health Statistics Unit](http://www.sahsu.org/) at
[Imperial College London](http://www.imperial.ac.uk/). SAHSU is part of the
[MRC-PHE Centre for Environment and Health](http://www.environment-health.ac.uk)

## MRC-PHE Centre for Environment and Health

The national Centre for Environment and Health is funded jointly by the Medical Research Council and Public Health England, with five
years funding 2009-2014, renewed w to mid 2019. The Centre is held jointly between Imperial College London (lead institution) and
King’s College London. It brings together a multidisciplinary research team, existing research programmes and a supportive research
environment. The Director is Professor Paul Elliott (Imperial) and the Deputy Director is Professor Frank Kelly (King’s). The Centre
incorporates the UK Small Area Health Statistics Unit (SAHSU) at Imperial and the Environmental Research Group (ERG) at King’s College
London. It brings together leading researchers from four divisions of the Faculty of Medicine at Imperial College, and researchers at
King’s College with St George’s University of London.

The Centre is an international centre of excellence for research and training on the health effects of environmental pollutants and the translation of this knowledge to inform national and international policies to improve health.  The Centre aims to integrate individual-level and small-area analyses of environmental exposures and health, combined with experimental data, biomarker and mechanistic studies, and analyses of large population cohorts, to tackle environmental health problems of public health and scientific importance. There is a particular focus in environmental epidemiology.  There is an extensive training programme with PhD studentships, Masters courses and short courses.

## UK Small Area Health Statistics Unit (SAHSU)

Following the identification of a ‘cluster’ of childhood leukaemia near the Sellafield nuclear plant in 1983, the Government established
SAHSU in 1987 with a remit that includes the investigation of disease near sources of environmental pollution and the study more generally
of disease variation across small areas in relation to environmental pollutants.

SAHSU’s work therefore includes substantive epidemiological enquiries of environmental health problems and methodological research.
SAHSU studies may involve (as necessary) environmental modelling and monitoring as well as biomarkers (of exposure, effects and genetic
susceptibility) in addition to the analysis of routine health statistics, to aid interpretation of causal inference.

Recently published SAHSU studies include health effects associated with noise and air pollution exposures near Heathrow Airport,
disease mapping studies and The Environment and Health Atlas for England and Wales. Other areas of research have included analysis
of congenital anomalies and cancer incidence near landfill sites, adverse birth outcomes in relation to chlorinated disinfection
by-products in drinking water, and kidney disease mortality near chemical plants emitting heavy metals. Ongoing studies include a
major study of traffic related air pollution in London and national studies investigating health impacts of large-scale waste
treatment facilities on health.

A major and unique feature of SAHSU is its range of national postcoded databases, which are used for environmental public health
analyses and surveillance. The data held include individual records of all births (from 1981) in England and Wales, deaths
(from 1981), cancer registrations (from 1974), congenital anomalies (from 1983) as well as hospital admissions (from 1989),
which include the postcode or address of residence (there are currently ~1.75 million residential postcodes in use in the UK).
This allows small area geographically-based analyses. The database holds in excess of ten million health event records per year.
Data on populations and socio-economic status (SES) are also held for small areas (enumeration districts: 147,000, super output
areas: 34,000 and census output areas: 175,000) from the 1981, 1991 and 2001 censuses. Maintenance and updating of the databases
(on health, population, SES and selected environmental data) is conducted by the SAHSU database manager and supporting staff.

# 4. Funders

The software application was originally developed in the late 1990s by SAHSU staff.  The RIF was further developed as part of the
[EUROHEIS and EUROHEIS2](http://www.euroheis.org/) projects, funded by the European Commission’s Directorate for General Health and
Consumer Protection. The aim of these projects was to improve health information and analysis in order to assess relationships between
environmental pollution and disease.

The RIF also received funding from:

* The US Center for Disease Control (CDC) as part of the [US] [Environment and Health Public Tracking
  Program](https://www.cdc.gov/nceh/tracking/).  As an example, the State of Utah Department of Health used the RIF to investigate the
  perceived excess of leukaemia in relation to oil refineries in Utah [reference 3].  The CDC support enabled modifications to the RIF to increase
  its functionality, ease of use and versatility.
* The [National Institute for Health Research](https://www.nihr.ac.uk/) which funds a Health Protection Research Unit into
  [Health Impact of Environmental Hazards](http://hieh.hpru.nihr.ac.uk/our-research/research-themes/theme-1-project-3-cluster-guidelinesrapid-inquiry-facility/)

## 4.1 Collaborators

* The [US Centers for Disease Control and Prevention (CDC)](https://www.cdc.gov/)
* The [National Institute for Health Research](https://www.nihr.ac.uk/)

## 4.2 Other Contributors

* The [European Commission](https://ec.europa.eu/commission/index_en/) has supported previous RIF versions
* [Public Health England (PHE)](https://www.gov.uk/government/organisations/public-health-england/) is the principal funder of SAHSU.

## 5. Licensing

The RIF is licensed by the [GNU LESSER GENERAL PUBLIC LICENSE](https://www.gnu.org/licenses/lgpl-3.0.en.html) and is freely available
from the [RIF GitHub](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/) repository.

# 6. Build and Install

## 6.1 Postgres

This section is a series of links to howto guides on the Postgres databas

### 6.1.1 Building the RIF Postgres database

See: [Building the RIF Postgres database from github](/rifDatabase/Postgres/docs/BUILD.md)

Port specific install notes:

  * [Windows install notes](/rifDatabase/Postgres/docs/windows.md)
  * [Linux install from repository notes](/rifDatabase/Postgres/docs/linux_repo.md)
  * [MACoS install from repository notes](/rifDatabase/Postgres/docs/macos_repo.md)
  * [Linux install from source notes](/rifDatabase/Postgres/docs/linux_source.md)

Detailed build instructions for Postgres:

  * [Postgres build process detail](/rifDatabase/Postgres/docs/BUILD.md)

### 6.1.2 Installing the RIF Postgres Database

See: [Installing a Windows RIF database from a SAHSU supplied pg_dump (Postgres database dump) file](/rifDatabase/Postgres/production/windows_install_from_pg_dump.md)

## 6.2 Microsoft SQL Server

### 6.2.1 Building the RIF SQL Server Database

See: [Building the RIF SQL Server database from github](/rifDatabase/SQLserver/installation/README.md)

### 6.2.2 Installing the RIF SQL Server Database

See: [Installing the RIF SQL Server database from a SAHSU supplied database export](/rifDatabase/SQLserver/production/INSTALL.md)

## 6.3 Web Services

See: [Building RIF web services from github and installing securely in Tomcat](/rifWebApplication/Readme.md)

### 6.3.1 Taxonomy Services

See: [RIF Taxonomy Services](/Taxonomy-Services)

## 6.4 Data Loading

### 6.4.1 Manual Data Loading

See: [Manual data loading](/rifDatabase/DataLoaderData/DataLoading.md)

### 6.4.2 Using the Data Loading Tool

See: [RIF Data Loader Manual](/Documentation/RIF%20Data%20Loader%20Manual.pdf)

# 7. Manuals

Currently there are eight manuals:

- [RIF 4.0 Manual](/standalone/RIF_v40_Manual.pdf)
- [RIF 4.0 Data Loader Manual](/standalone/RIF_Data_Loader_Manual.pdf)
- [RIF Web Application and Middleware Installation](/rifWebApplication/Readme.md)
- [Windows Postgres Install using pg_dump and scripts](/rifDatabase/Postgres/production/windows_install_from_pg_dump.md)
- [SQL Server Production Database Installation](/rifDatabase/SQLserver/production/INSTALL.md)
- [Manual data loading](/rifDatabase/DataLoaderData/DataLoading.md)
- [Tile maker Manual](/rifNodeServices/tileMaker.md)
- [Database Management Manual](/rifDatabase/databaseManagementManual.md)

# 8. For Developers

## RIF Architecture

![RIF Architecture](/RIF_architecture.png)

This section is a series of links to developer documentation

* [Coding Standards](development/coding-standards) -- see this for our preferences for code formatting, and so on.
* [RIF Re-development](/The-RIF-re-development)
* [RIF design documentation on Sourceforge](http://rapidinquiryfacility.sourceforge.net/index.html)
* [Database Design](/Database-design)
* [Database test harness](/rifDatabase/TestHarness/db_test_harness)
* [General RIF Installation Notes for the Middleware and the Data Loader Tool](/General-RIF-Installation-Notes-for-the-Middleware-and-the-Data-Loader-Tool)
* [Kevs Suggested Road Map with the Middleware](/Kevs-Suggested-Road-Map-with-the-Middleware)
* [Middleware Code Road Map](/Kev-Code-Road-Map)
* [Middleware Handover Notes](/Kev-Handover-Notes)
* [Opening the RIF front end in NetBeans8 for editing and debugging](/Opening-the-RIF-front-end-in-NetBeans8-for-editing---debugging)
* [Opening the RIF repository in Eclipse for editing](/Opening-the-RIF-repository-in-Eclipse-for-editing)
* [Setting up JRI in Eclipse](/Setting-up-JRI-in-Eclipse)
* [RIF Front End Description](/RIF-front-end-description)
* [Creating a new restful web service](/Creating-a-new-restful-web-service)

# 9. TODO list

* [To Do List](/TODO)
