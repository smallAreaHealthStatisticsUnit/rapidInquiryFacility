---
layout: default
title: RIF Taxonomy Services
---

1. Contents
{:toc}

# Introduction

The RIF has a separate taxonomy service. It is intended to support a variety of ontologies; the first
class of ontology being international classification of disease (ICD). It is anticipated that over time
further ontologies will be added; for instance:

* ICD Oncology
* HES A&E codes
* HES Oper codes

## Configuration

The file `TaxonomyServicesConfiguration.xml` in `rapidInquiryFacility/rifServices/src/main/resources` specifies the configuration for one or more taxonomy services. The parameter for the ICD 10 service is `icd10_ClaML_file`. It is shipped with a value of  `ExampleClaMLICD10Codes.xml`. That file contains a cut-down version of the standard WHO file which needs to be licensed directly from the WHO.

For a full ICD10 listing add the following SAHSU supplied files (in *Taxonomy services configuration files*) to:
*%CATALINA_HOME%\conf* and restart tomcat

* *icdClaML2016ens.xml* [WHO ICD 10 2016 data from: [WHO Classifications Download Area](http://apps.who.int/classifications/apps/icd/ClassificationDownloadNR/login.aspx?ReturnUrl=%2fclassifications%2fapps%2ficd%2fClassificationDownload%2fdefault.aspx).
  You will need to agree to a non commercial WHO license, declare your usage and create an account.
* *TaxonomyServicesConfiguration.xml* [The configuration file]
* *ClaML.dtd* [The XML specification used to parse *icdClaML2016ens.xml* supplied with it]

## Troubleshooting Startup Issues

The taxonomy services does not startup until the first first user logon. It has to load and process a
number of different data sources, it is possible if you are quick to get the error "The system for supporting the
taxonomy services has not yet been initialised" from the front end:

* Investigations tab: Searching ICD codes;
* Export: Save completed study
* Export: Export study tables

![Taxonomy Services warning]({{ site.baseurl }}/rifWebApplication/taxonomy_sevice_warning.png){:width="100%"}

These will go away if you wait!

Errors, which are normally caused by configuration issues are displayed as:

![Taxonomy Services error]({{ site.baseurl }}/rifWebApplication/taxonomy_sevice_error.png){:width="100%"}

Look in the startup log. This is the normal case:

```
08:03:17.131 [http-nio-8080-exec-8] INFO  rifGenericLibrary.util.TaxonomyLogger : [TaxonomyLogger]: Created TaxonomyLogger: rifGenericLibrary.util.TaxonomyLogger
08:03:17.144 [http-nio-8080-exec-8] INFO  rifGenericLibrary.util.TaxonomyLogger : [TaxonomyLogger]: Set java.util.logging.manager=org.apache.logging.log4j.jul.LogManager
08:03:17.145 [http-nio-8080-exec-8] INFO  rifGenericLibrary.util.TaxonomyLogger : [taxonomyServices.RIFTaxonomyWebServiceApplication]:
!!!!!!!!!!!!!!!!!!!!! RIFTaxonomyWebServiceApplication !!!!!!
08:03:18.747 [http-nio-8080-exec-8] INFO  rifGenericLibrary.util.TaxonomyLogger : [taxonomyServices.ICD10TaxonomyTermParser]:
ICD10TaxonomyTermParser 2
08:03:39.623 [http-nio-8080-exec-8] INFO  rifGenericLibrary.util.TaxonomyLogger : [org.sahsu.taxonomyservices.ClaMlTaxonomyService]:
icd101/1TaxonomyParser: ICD Taxonomy Service read: "C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\icdClaML2016ens.xml".
08:03:39.623 [http-nio-8080-exec-8] INFO  rifGenericLibrary.util.TaxonomyLogger : [org.sahsu.taxonomyservices.ClaMlTaxonomyService]:
icd101/1TaxonomyParser: ICD Taxonomy Service initialised: ICD 10 is a classification of diseases..
```

Check the taxonomy services configuration file *TaxonomyServicesConfiguration.xml*:

```
<?xml version="1.0" encoding="UTF-8"?>
<taxonomy_services>
	<taxonomy_service>
		<identifier>icd10</identifier>
		<name>ICD Taxonomy Service</name>
		<description>International classification of diseases and related health problems 10th revision (2016 version).</description>
		<version>1.0</version>
		<ontology_service_class_name>org.sahsu.taxonomyservices.ClaMlTaxonomyService</ontology_service_class_name>
		<parameters>
			<parameter>
				<name>icd10_ClaML_file</name>
				<value>icdClaML2016ens.xml</value>
			</parameter>
		</parameters>
	</taxonomy_service>
</taxonomy_services>
```

##  How it works

The configuration file is read and parsed and for each listed taxonomy service the `<ontology_service_class_name>` is loaded (e.g. `org.sahsu.taxonomyservices.ClaMlTaxonomyService`) and the `initialise()` method called.

The class then loads and parses the file specified in the `<ontology_service_class_name>` element of the configuration file, and sets a taxonomy services manager. For ICD 10, for example, the code for this is in `ICD10TaxonomyTermParser.java` in the `rifGenericLibrary.taxonomyServices` package. The taxonomy services manager is used by the taxonomy service REST calls and then the front end.

### Taxonomy service REST calls

* http://localhost:8080/taxonomies/initialiseService
  - Is the service initialized?
  - Returns: true/false
* http://localhost:8080/taxonomies/getTaxonomyServiceProviders
  - Get taxonomy services listing
  - Returns:
    ```json
	[{
			"identifier": "icd10",
			"name": "ICD Taxonomy Service",
			"description": "ICD 10 is a classification of diseases."
		}
	]
	```
* http://localhost:8080/taxonomies/getMatchingTerms?taxonomy_id=icd10&search_text=J22&is_case_sensitive=false
  - Get matching terms to use query (J22)
  - Returns:
    ```json
	[{
			"identifier": "J20-J22-icd10",
			"label": "J20-J22",
			"description": "\n\t\t\tOther acute lower respiratory infections\n\t\t",
			"isTopLevelTerm": null
		}, {
			"identifier": "J22-icd10",
			"label": "J22",
			"description": "\n\t\t\tUnspecified acute lower respiratory infection\n\t\t",
			"isTopLevelTerm": null
		}
	]
    ```

The following do not appear to be in use:

* http://localhost:8080/taxonomies/getImmediateChildTerms
* http://localhost:8080/taxonomies/getParentTerm
* http://localhost:8080/taxonomies/getRootTerms

# Adding ICD 9 and ICD 11 support

* ICD 11 is in beta tested and is expected to be released in June 2018.
* ICD 9 codes were downloaded from: https://raw.githubusercontent.com/drobbins/ICD9/master/icd9.txt;
  these codes are themselves reformatted
  from: https://www.cms.gov/ICD9ProviderDiagnosticCodes/downloads/cmsv29_master_descriptions.zip

## ICD 9

To create an ICD 9 service, you would make a class along the lines of
*org.sahsu.taxonomyservices.ClaMlTaxonomyService* and ensure it implemented the interface
rifGenericLibrary.taxonomyServices.TaxonomyServiceAPI.  Note that most of the code used to support taxonomy
services is generic and does not rely on RIF concepts - this is why it is found in the
rifGenericLibrary.taxonomyServices package.

This would need to parse the CSV file into a similar structure to that used by the CLaML parser and implement
the required REST service callbacks.

## ICD 11

Support for ICD 11 has been added to the ICDTaxonomyService; the parameter file is called *icd11_ClaML_file*.
The following configuration would support both ICD10 and ICD11.

```
<?xml version="1.0" encoding="UTF-8"?>
<taxonomy_services>
	<taxonomy_service>
		<identifier>icd10</identifier>
		<name>ICD Taxonomy Service</name>
		<description>International classification of diseases and related health problems 10th revision (2016 version).</description>
		<version>1.0</version>
		<ontology_service_class_name>org.sahsu.taxonomyservices.ClaMlTaxonomyService</ontology_service_class_name>
		<parameters>
			<parameter>
				<name>icd10_ClaML_file</name>
				<value>icdClaML2016ens.xml</value>
			</parameter>
		</parameters>
	</taxonomy_service>

	<taxonomy_service>
		<identifier>icd10</identifier>
		<name>ICD Taxonomy Service</name>
		<description>International classification of diseases and related health problems 11th revision (2018 version).</description>
		<version>1.0</version>
		<ontology_service_class_name>org.sahsu.taxonomyservices.ClaMlTaxonomyService</ontology_service_class_name>
		<parameters>
			<parameter>
				<name>icd11_ClaML_file</name>
				<value>icd11ClaML2018ens.xml</value>
			</parameter>
		</parameters>
	</taxonomy_service>
</taxonomy_services>
```

There are a number of inter related assumptions here that may not be correct:

* The same CLaML data type description is used by ICD 10 and 11
* The ICD 10 parser can parse the ICD 11 CLaML
* ICD 11 codes will be set in the same way as ICD 10 (is the histology separate or part of the code or both
  and therefore data provider dependent)

Whilst it would be useful for the RIF to support ICD 11 as a training and familiarization; medical data containing
ICD 11 code is probably a decade away.

# Adding new ontologies

Adding a new service can be very simple. [See here for how to add new services](./adding-a-new-taxonomy).

# Issues

The taxonomy services has the following issues:

* Initialization: remove annoying messages caused by initialization by run the initialization in a
  service context listener, to the taxonomy service is initialized on service start; not first call;
* List contains ICD 10 chapters. These should be removed from the list as they contain no identifiers,
  e.g. Chapter II Neoplasms contains an identifier of *II-NULL* which is not convertible to a database query);
* Range queries, e.g. A15-A19 Tuberculosis (identifier A15-A19-ICD10) are not converted to a workable
  database query.

These are intended to be resolved in the period May-September 2018 when ICD 9 support is added.
