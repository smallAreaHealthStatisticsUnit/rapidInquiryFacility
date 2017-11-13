CDC Update - November 2017
==========================

# Highlights

* SQL Server port now has exactly the same functionality as the Postgres port;
* Data export of data extract and results now works; user can download a ZIP file and re-run
  the R side of the analysis;
* A SEER test dataset has been created for both ports in preparation for SAHSU internal testing. SAHSU 
  is about to test both the SQL Server and Postgres ports internally so we can start to use it in the 
  new year;
* Security testing completed;
* A new Java developer to replace is Kev being interviewed 9/11;
* David has now left (to work for TomTom in Belgium). Peter is now working on the Front End having taking over from David;
* Logging using Log4j has been implemented by Peter and the middleware now correctly reports errors to
  the front end. Log4j was chosen as the CDC use it. It is now much easier to trace errors (even 
  in R!). The alert logging in the front end has also been improved and a log list tab will be provided at some point.
  Front end logs for the moment go to the browser console if it is enabled;
* The SAHSUland test data has been improved;
* Documentation has been overhauled (partly in the handovers from Kev and David, partly in preparation for 
  the new Java developer)
  
# Next tasks

## November

* Get CDC test system up to date;
* Security fixes;
* CDC security testing;
* Prepare for SAHSU internal testing.

## December

* Key front end faults related to multi-geography support:

  * Map synchronisation issues. A change in geography from one study to another causes chaos in the data viewer and disease 
	mapping tabs. The best solution is to set both tabs to the same geography then set up the maps
	and finally zoom to map extent. This will fix the map to the correct location;
  * Null zoomlevel error, appears when moving between the data viewer and the disease mapper. 
  * Memory leaks
* Fix missing information not stored in the database, re-create JSON study configuration file in the middleware
* Mapping support prototypes

## Q1 2018

* Risk analysis
* Mapping support

# Security Testing

205 unique URLs were tested using OWASP ZAP (https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project) Ajax Spider

The report is at: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Testing/owasp_zap_test1.md and the URL list 
tested is at: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Testing/url_list2.txt. 

One medium and three low medium isses were highlighted for fixing.

## Medium Issues

1. X-Frame-Options Header Not Set

   X-Frame-Options header is not included in the HTTP response to protect against 'ClickJacking' attacks.

## Low Medium Issues

1. Incomplete or No Cache-control and Pragma HTTP Header Set

   The cache-control and pragma HTTP header have not been set properly or are missing allowing the browser and proxies to cache content.

2. X-Content-Type-Options Header Missing

   The Anti-MIME-Sniffing header X-Content-Type-Options was not set to 'nosniff'. This allows older versions of Internet Explorer and Chrome 
   to perform MIME-sniffing on the response body, potentially causing the response body to be interpreted and displayed as a content type other 
   than the declared content type. Current (early 2014) and legacy versions of Firefox will use the declared content type (if one is set), 
   rather than performing MIME-sniffing.

3. Web Browser XSS Protection Not Enabled

   Web Browser XSS Protection is not enabled, or is disabled by the configuration of the 'X-XSS-Protection' HTTP response header 
   on the web server

# Risk Analysis

THis will be the key work package for the first few months of next year.

* Work needs to be carried out principally in the Middleware. This will be the task of the new Java 
  developer unless we fail to hire. In this case Peter will start this instead of the maps generation;
* Some work in envsaiged in the smoothing functions and in the database; we are intending to test
  cluster anaysis using:
  * BayesSTDetect using Winbugs called from R 
  * SatScan or Scanstatistics; both called from R 

# Maps generation

Current maps (see examples below) are not suitable for inclusion in Scientific publications.

* It is intended to add a map generator to the export ZIP file to create high quality outputs suitable
  for journals;
* The RIF will remember the users choices in the data and map viewer panels;  
* The export zip generator will build the maps;
* Results in shapefile form will probably be added;
* Users will be able to regenerate the maps outside of the RIF.

# TODO List

There is a detailed TODO list which I am updating as I test.

* [To Do List](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/wiki/TODO)

# SEER Test data

A SEER test dataset has been created so the RIF can do some real Science.

| cb_2014_us_state_500k |  areaname   | start | end  |  cases  |
|-----------------------|-------------|-------|------|---------|
| 01785533              | Alaska      |  1992 | 2013 |    8080 |
| 01779778              | California  |  1973 | 2013 | 3185854 |
| 01779780              | Connecticut |  1973 | 2013 |  753261 |
| 01705317              | Georgia     |  1975 | 2013 |  810910 |
| 01779782              | Hawaii      |  1973 | 2013 |  201078 |
| 01779785              | Iowa        |  1973 | 2013 |  632565 |
| 01779786              | Kentucky    |  2000 | 2013 |  366513 |
| 01629543              | Louisiana   |  2000 | 2013 |  340659 |
| 01779789              | Michigan    |  1973 | 2013 |  854021 |
| 01779795              | New Jersey  |  2000 | 2013 |  752599 |
| 00897535              | New Mexico  |  1973 | 2013 |  267661 |
| 01455989              | Utah        |  1973 | 2013 |  260674 |
| 01779804              | Washington  |  1974 | 2013 |  743088 |

The SEER cancer data has 9,176,963 rows and requires 800MB for the data and 1.3GB for the indexes.

## Test case 1004 

I have created a test case, 1004. File: *1004 SEER 2000-13 lung cancer HH income mainland states.json*
   
The test study *1004 SEER 2000-13 lung cancer HH income mainland states.json*:

* States were chosen so they mapped compactly (Alaska and Hawaii excluded):

  * California
  * Connecticut
  * Georgia
  * Iowa
  * Kentucky
  * Louisiana
  * Michigan
  * New Jersey
  * New Mexico
  * Utah
  * Washington;
 
* Years: 2000-2013 (the maximum period available across these states);
* Covariate: median head of household income, quintilised;
* Both sexes;
* All ages;
* Lung cancer: 

  * C33: Malignant neoplasm of trachea
  * C340: Main bronchus
  * C342: Middle lobe, bronchus or lung
  * C341: Upper lobe, bronchus or lung
  * C343: Lower lobe, bronchus or lung
  * C348: Overlapping lesion of bronchus and lung
  * C349: Bronchus or lung, unspecified
 
* Full Bayesian smoothing
   
Run times:

* Postgres (constrained to 50M RAM): 7:43 ***I am about to tune this!!!!**
* SQL Server (using 1.3G RAM): 1:21 (desktop) 34.5S (laptop)
* R with BYM model: 30s

## Example maps

![Loiusiana/Kentucky/Georgia smoothed SMR](https://raw.githubusercontent.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/master/Progress%20Reports/CDC%20Monthly%20progress%20reports/2017/LA_KY_GA_smr.png)

![Loiusiana/Kentucky/Georgia posterior probability](https://raw.githubusercontent.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/master/Progress%20Reports/CDC%20Monthly%20progress%20reports/2017/LA_KY_GA_posterior_probability.png)

![SEER Lung Cancer Disease Mapping](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Progress%20Reports/Screenshots/US_SEER_2.PNG)

## Example JSON

This is an example of the configuration files in the expanded format. Normally JSON is all on one line 
with no spaces (the expanded format will be supported in future).

```json
{
	"rif_job_submission": {
		"submitted_by": "peter",
		"job_submission_date": "02/11/2017 09:43:57",
		"project": {
			"name": "",
			"description": ""
		},
		"calculation_methods": {
			"calculation_method": {
				"name": "bym_r_procedure",
				"code_routine_name": "bym_r_procedure",
				"description": "Besag, York and Mollie (BYM) model type",
				"parameters": {
					"parameter": []
				}
			}
		},
		"rif_output_options": {
			"rif_output_option": ["Data",
			"Maps",
			"Ratios and Rates"]
		},
		"disease_mapping_study": {
			"name": "1004 SEER LUNG 00 13",
			"description": "",
			"geography": {
				"name": "USA_2014",
				"description": "USA_2014"
			},
			"investigations": {
				"investigation": [{
					"title": "LUNG CANCER",
					"health_theme": {
						"name": "cancers",
						"description": "covering various types of cancers"
					},
					"numerator_denominator_pair": {
						"numerator_table_name": "SEER_CANCER",
						"numerator_table_description": "SEER Cancer data 1973-2013. 9 States in total",
						"denominator_table_name": "SEER_POPULATION",
						"denominator_table_description": "SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total"
					},
					"age_band": {
						"lower_age_group": {
							"id": 0,
							"name": "0",
							"lower_limit": "0",
							"upper_limit": "0"
						},
						"upper_age_group": {
							"id": 0,
							"name": "85PLUS",
							"lower_limit": "85",
							"upper_limit": "255"
						}
					},
					"health_codes": {
						"health_code": [{
							"code": "C33",
							"name_space": "icd10",
							"description": "Malignant neoplasm of trachea",
							"is_top_level_term": "no"
						},
						{
							"code": "C340",
							"name_space": "icd10",
							"description": "Main bronchus",
							"is_top_level_term": "no"
						},
						{
							"code": "C342",
							"name_space": "icd10",
							"description": "Middle lobe, bronchus or lung",
							"is_top_level_term": "no"
						},
						{
							"code": "C341",
							"name_space": "icd10",
							"description": "Upper lobe, bronchus or lung",
							"is_top_level_term": "no"
						},
						{
							"code": "C343",
							"name_space": "icd10",
							"description": "Lower lobe, bronchus or lung",
							"is_top_level_term": "no"
						},
						{
							"code": "C348",
							"name_space": "icd10",
							"description": "Overlapping lesion of bronchus and lung",
							"is_top_level_term": "no"
						},
						{
							"code": "C349",
							"name_space": "icd10",
							"description": "Bronchus or lung, unspecified",
							"is_top_level_term": "no"
						}]
					},
					"year_range": {
						"lower_bound": 2000,
						"upper_bound": 2013
					},
					"year_intervals": {
						"year_interval": [{
							"start_year": "2000",
							"end_year": "2000"
						},
						{
							"start_year": "2001",
							"end_year": "2001"
						},
						{
							"start_year": "2002",
							"end_year": "2002"
						},
						{
							"start_year": "2003",
							"end_year": "2003"
						},
						{
							"start_year": "2004",
							"end_year": "2004"
						},
						{
							"start_year": "2005",
							"end_year": "2005"
						},
						{
							"start_year": "2006",
							"end_year": "2006"
						},
						{
							"start_year": "2007",
							"end_year": "2007"
						},
						{
							"start_year": "2008",
							"end_year": "2008"
						},
						{
							"start_year": "2009",
							"end_year": "2009"
						},
						{
							"start_year": "2010",
							"end_year": "2010"
						},
						{
							"start_year": "2011",
							"end_year": "2011"
						},
						{
							"start_year": "2012",
							"end_year": "2012"
						},
						{
							"start_year": "2013",
							"end_year": "2013"
						}]
					},
					"years_per_interval": 1,
					"sex": "Both",
					"covariates": [{
						"adjustable_covariate": {
							"name": "MEDIAN_HH_INCOME_QUIN",
							"minimum_value": "1.0",
							"maximum_value": "5.0",
							"covariate_type": "adjustable"
						}
					}]
				}]
			},
			"disease_mapping_study_area": {
				"geo_levels": {
					"geolevel_select": {
						"name": "CB_2014_US_STATE_500K"
					},
					"geolevel_area": {
						"name": ""
					},
					"geolevel_view": {
						"name": "CB_2014_US_COUNTY_500K"
					},
					"geolevel_to_map": {
						"name": "CB_2014_US_COUNTY_500K"
					}
				},
				"map_areas": {
					"map_area": [{
						"id": "01779778",
						"gid": "01779778",
						"label": "California",
						"band": 1
					},
					{
						"id": "01779780",
						"gid": "01779780",
						"label": "Connecticut",
						"band": 1
					},
					{
						"id": "01705317",
						"gid": "01705317",
						"label": "Georgia",
						"band": 1
					},
					{
						"id": "01779785",
						"gid": "01779785",
						"label": "Iowa",
						"band": 1
					},
					{
						"id": "01779786",
						"gid": "01779786",
						"label": "Kentucky",
						"band": 1
					},
					{
						"id": "01629543",
						"gid": "01629543",
						"label": "Louisiana",
						"band": 1
					},
					{
						"id": "01779789",
						"gid": "01779789",
						"label": "Michigan",
						"band": 1
					},
					{
						"id": "01779795",
						"gid": "01779795",
						"label": "New Jersey",
						"band": 1
					},
					{
						"id": "00897535",
						"gid": "00897535",
						"label": "New Mexico",
						"band": 1
					},
					{
						"id": "01455989",
						"gid": "01455989",
						"label": "Utah",
						"band": 1
					},
					{
						"id": "01779804",
						"gid": "01779804",
						"label": "Washington",
						"band": 1
					}]
				}
			},
			"comparison_area": {
				"geo_levels": {
					"geolevel_select": {
						"name": "CB_2014_US_STATE_500K"
					},
					"geolevel_area": {
						"name": ""
					},
					"geolevel_view": {
						"name": "CB_2014_US_STATE_500K"
					},
					"geolevel_to_map": {
						"name": "CB_2014_US_STATE_500K"
					}
				},
				"map_areas": {
					"map_area": [{
						"id": "01779778",
						"gid": "01779778",
						"label": "California",
						"band": 1
					},
					{
						"id": "01779780",
						"gid": "01779780",
						"label": "Connecticut",
						"band": 1
					},
					{
						"id": "01705317",
						"gid": "01705317",
						"label": "Georgia",
						"band": 1
					},
					{
						"id": "01779785",
						"gid": "01779785",
						"label": "Iowa",
						"band": 1
					},
					{
						"id": "01779786",
						"gid": "01779786",
						"label": "Kentucky",
						"band": 1
					},
					{
						"id": "01629543",
						"gid": "01629543",
						"label": "Louisiana",
						"band": 1
					},
					{
						"id": "01779789",
						"gid": "01779789",
						"label": "Michigan",
						"band": 1
					},
					{
						"id": "01779795",
						"gid": "01779795",
						"label": "New Jersey",
						"band": 1
					},
					{
						"id": "00897535",
						"gid": "00897535",
						"label": "New Mexico",
						"band": 1
					},
					{
						"id": "01455989",
						"gid": "01455989",
						"label": "Utah",
						"band": 1
					},
					{
						"id": "01779804",
						"gid": "01779804",
						"label": "Washington",
						"band": 1
					}]
				}
			}
		}
	}
}
```

 

  