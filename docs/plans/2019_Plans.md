---
layout: default
title: 2019 Plans
---

1. Contents
{:toc}

# Introduction

[RIF Roadmap](https://trello.com/b/CTTtyxJR/the-rif-roadmap) or as a graphic: ![2019 RIF Roadmap]({{ site.baseurl }}/plans/2019_RIF_Roadmap.png)

# Bug Fixes

## High priority

* SQL Server SAHSU Database not linked to geography;

## To be allocated - low priority

* Issue #79 Unicode area names (SQL Server only);
* Issue #75 IE support;
* Issue #57 front end mapping synchronisation (map auto draw disabled, #issue #67 print state support depends);
* Issue #56 error loading studty from database generated JSON;
* Issue #113 refresh logs you off;.

# Enhancements

## Now (Q1 2019)

### Make the RIF Usable within SAHSU

* Issue #? risk analysis maps;
* Issue #126 Oracle interconnect;
* Issue #68 risk analysis selection at high resolution does not work acceptably:
  * PNG tile support in test;
  * Mouseover support required (so you can see the area names) - issue #66;
* Issue #121 Add priors;
* Issue #67 print state support for saving user print selection;
* Issue #63 extend PNG tile support and restructed mapping code to mapping;
* Issue #80 optimise performance on large datasets;

### Improve the Installation Process

* Issue #118 Improve the installation process;

### Additional Covariates

* Issue #124 Multiple covariates (first part for March 2019);

## Next (Q2-Q4 2019)

### Support Multiple Covariates

* Issue #124 Multiple covariates (first part for March 2019);

### Information 

* Issue #85 IG tool;

### Logging and AuditingGovernance

* Issue #81 improve the audit trail;
* Issue #86 logging;

### Data Loading Improvements

### Data Extract Improvements

* Data extract ZIP:
  1. Scripts - issue #88;
  2. US Maps - issue #92;
  3. Printing - issue #93;
  4. Allow use of more fields - issue #94;
  5. Changing resolution - issue #95;
  6. Additional items - issue #96;
* Issue #85 local caching of basemaps;

### Further making the RIF Usable within SAHSU

* Issue #65 sort mapping info boxes;
* Issue #46 exception handling requires improvement to make errors clearer to user and avoid multiple nested errors in the log;
* Issue #123 improve search in taxonomies;
* Issue #117 Add JavaDoc;
* Issue #110 Re-start RIF services;
* Issue #97 Add additional information to circles and shapes (number of intersections, distance to nearest source);
