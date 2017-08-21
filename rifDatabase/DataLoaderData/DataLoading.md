RIF Data Loading
================

# Contents

- [1. Overview](#1-overview)
  - [1.1 RIF Capabilities](#11-rif-capabilities)
  - [1.2 Limitations](#12-limitations)
  - [1.3 Data Loader Tool])#13-data-loader-tool)
- [2. RIF Data Loading Prerequistes](#2-rif-data-loading-prerequistes)
  - [2.1 Postgres](#21-postgres)
  - [2.2 SQL Server](#22-sql-server)
  - [2.3 Data Structure](#23-data-structure)
- [3. Load Processing](#3-load-processing)
  - [3.1 Numerator](#31-numerator)
  - [3.2 Denominator](#32-denominator)
  - [3.3 Covariates](#33-covariates)
  - [3.4 Administrative Geography](#34-administrative-geography)
- [4. Information Governance](#4-information-governance)
 - [4.1 Auditing](#41-auditing)

# 1. Overview

This document details both the manual process for the loading of data into the RIF and the automated process 
using the data loader tool. The RIF requires the following types of data:

* Numerator data. This may be in indivual record form or aggregated to a suitable administrative geography. 
  Indvidual records are assumed to have been de-indentified into a suitable pseudonymous form; all this is
  not a requirement.
* Denominator data. This is in aggregate form.
* Covariate data. This is again in aggregate form and has been quantilised
* Administrative geography. This is created by the Tile-Maker tool; this document details the loading of 
  this data and the geocoding requirements of the RIF. See [Add tile-maker manual] for how to create
  RIF administrative geographiues.
  
# 1.1 RIF Capabilities
  
# 1.2 Limitations

Limiations in the current RIF

* No support form covariates embedded in numerator or denominator data. Data must be extracted into a separate 
  covariate table. Must covariates must be merged into a single table. It is not planned to remove these 
  restrictions.
* Covariates must be quantilised. Support on the fly quantilisation may be added in future releases.
* Denomionator data is always used in direct standardisation. There is no support currently in the RIF for
  indirect standardisation using stand populations. This was supported in previous versions and will be put 
  back if required.
* Numarators are currently limited to ICD 10 coding only. Support will be progressively added for:
  * ICD 9
  * ICD 11
  * ICD oncology O(ICD-O-1)
  * UK HES oper and A+E codes
  * User specified conditions, e.g. Low birthweight, complex groups of ICD codes
* The RIF currently lack complex support for Information Governance beyond having strong role based permissions.
  
  **This document will detail how to audit access to the data.**. This is envisaged as being part of a separate
  information governance tool. When this tool is operatoon the RIF will allow users to extract more complex 
  data than just the extracton required to calculate the results.
  
1.3 Data Loader Tool
  
The data loader tool in its current prototype form is severly limited ins it ability to transform data by:

* Aggregation
* Re-gecoding data
* Performing complex cleaning ad remapping of data (e.g. chosing between provisiomnal and final causes of death)

It is envisaged that the data loader will become a broiwser based tool like the RIF of the RIF toolchain.
 
# 2. RIF Data Loading Prerequistes

The following steps need to be carried out to process the data:

* Separation of data into numerator, denominator and covariates. 
* Collation of covariates into a single covariate source
* Geo-coding of data to joing to the adminstrative dataset.

## 2.1 Postgres

## 2.2 SQL Server

## 2.3 Data Structure

# 3. Load Processing 

## 3.1 Numerator

## 3.2 Denominator

## 3.3 Covariates

## 3.4 Administrative Geography

# 4. Information Governance

## 4.1 Auditing

Peter Hambly
17th August 2017