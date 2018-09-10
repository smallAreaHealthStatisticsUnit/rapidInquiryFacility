---
layout: default
title: What is the RIF?
---

1. Contents
{:toc}

The Rapid Inquiry Facility (RIF) is a freely available software application that supports two types of environmental health activities:
disease mapping studies and risk analysis studies. It facilitates interrogation of databases containing geographical, health, population
and risk factor (e.g. deprivation) data to produce estimates of disease risk in specified areas and to conduct disease mapping in
those areas using advanced statistical techniques. The RIF software is unique and only available from SAHSU. It was designed to help
epidemiologists and public health researchers to rapidly investigate potential environmental hazards, especially those related to
industrial sites. The tool uses health, environmental, socio-economic, population and geographic data to calculate risks in relation
to sources of exposure and to generate maps.

## Study Aims

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

## References

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

# What does the RIF do?

The RIF automatically generates contextual maps showing the area under study. A report is generated summarising the study details, and
reporting the crude and adjusted rates and risks for each health outcome investigated. Graphs comparing the age, gender and
socio-economic (or other covariate) structure of the study, and comparison populations, are provided to aid interpretation.

As well calculating the rates and relative risks (and associated 95% confidence intervals) for each exposure group or distance band,
the RIF also runs Chi-square tests for homogeneity and linear trends to test the global association between distance/exposure covariate
and disease risk. In disease mapping analyses, maps showing crude, adjusted and smoothed risks by area are also displayed.
Demonstrations of the current RIF are available. These are large Windows Media Video (.wmv) files, and must first be unzipped
(use WinZip on Windows), and then they can be viewed using Windows Media Player.

[Old V3 RIF Disease mapping example video LINK BROKEN - FIX](http://www.sahsu.org/Disease%20Mapping%20Demo.zip)

## Screen Shots

### Screenshot of the old RIF 3.x

The old RIF was an embedded plug-in for ArcGIS 9 and used Oracle or access as a database backend. It was written in VBA

![RIF 3.0 screenshot](http://www.sahsu.org/sites/impc_sahsu/files/rif%203%20screenshot.png)

### Screenshots of the new RIF 4.0

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

![RIF 4.0 login screen]({{ site.baseurl }}/Screenshots/RIF_login.png)

To create a RIF disease map:

* Study area
* Comparision area
* The disease to the mapped (the investigation)
* The statistical outputs required

Study area  section. Comparision are selection is identical.

![RIF 4.0 study area selection]({{ site.baseurl }}/Screenshots/RIF_studyarea.png)

This shows an investigation being setup.

![RIF 4.0 investigation setup]({{ site.baseurl }}/Screenshots/RIF_investigation.png)

This box shows the choosing of the statistical methods.

![RIF 4.0 Statistics Dialog]({{ site.baseurl }}/Screenshots/RIF_statistical_methods.png)

The study is then submitted. It is checked and then saved in the database and a procedure called to extract the required data. This is then
passed to R to perform the required statistics, with the results saved to the database.

![RIF 4.0 study submission]({{ site.baseurl }}/Screenshots/RIF_submission.png)

When the study is complete the user is informed and the data can be viewed or mapped. The data can additionally be exported to the user as a zip file
for further analysis.

![RIF 4.0 data viewer]({{ site.baseurl }}/Screenshots/RIF_viewer.png)

The maps displayed here use the synthetic test dataset, *SAHSULAND* are are using the classification scheme from the
[THE ENVIRONMENT AND HEALTH ATLAS FOR ENGLAND AND WALES](http://www.envhealthatlas.co.uk/homepage/)

![RIF 4.0 mapping]({{ site.baseurl }}/Screenshots/RIF_mapping.png)

Finally, the user can export the study data for further analysis and inclusion in papers and reports.

![RIF 4.0 data export]({{ site.baseurl }}/Screenshots/RIF_export.png)

# Authors

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

# Funders

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

## Collaborators

* The [US Centers for Disease Control and Prevention (CDC)](https://www.cdc.gov/)
* The [National Institute for Health Research](https://www.nihr.ac.uk/)

## Other Contributors

* The [European Commission](https://ec.europa.eu/commission/index_en/) has supported previous RIF versions
* [Public Health England (PHE)](https://www.gov.uk/government/organisations/public-health-england/) is the principal funder of SAHSU.
