---
layout: default
title: Creating a new restful web service
---

1. Contents
{:toc}

Peter Hambly
16th October 2017

# 1. Define the web service

The object of this document is to detail how to add a restful web service to the RIF. The RIF is structured with an HTML5/Javascript
front end, using [Angular.js](https://angularjs.org/) and [Leaflet](http://leafletjs.com/). Middleware is implemented using
Restfull web services written in [Java](https://www.java.com/en/) and running in [Apache Tomcat](http://tomcat.apache.org/). All data is stored in a
GeoSptial database, with a choice of [MicroSoft SQL Server](https://www.microsoft.com/en-us/sql-server/default.aspx) or [Postgres](https://www.postgresql.org/).

![RIF Architecture]({{ site.baseurl }}/development/RIF_architecture.png)

The web services are thus the key interface in the software, and this document details who to create one. The example code is
principally focused on the Middleware, with the interfacing with the front end and the database detailed.

This markdown document details the steps needed to create the latest Restful web service: *getExtractStatus*.

It should be noted that RIF web services come in Microsoft SQL Server and Postgres variants. The code in the data storage layer is separated
into separate *ms* and *pg* path components with no common code. This causes substantial code duplication and will be rectified by
the new Java Developer currently being hired (October 2017). These links will only work in a RIF browser session!

* http://localhost:8080/rifServices/studySubmission/pg/getExtractStatus?userID=peter&studyID=51
* http://localhost:8080/rifServices/studySubmission/ms/getExtractStatus?userID=peter&studyID=51

This returns the reposnse: ```STUDY_EXTRACTABLE_NEEDS_ZIPPING```.

## 1.1 GetExtractStatus Definition

Get textual extract status of a study.

This function determines whether a study can be extracted from the database and the results returned to the user in a ZIP file.

It is intended that during 2018 this function be enhanced to support the information governance requirements that should an organisation wish, the
(to be created) Informartion Governance tool will control none, one or both of:

* Data extraction from the database to perform the study;
* Data delivery to the user (in the form of a ZIP file);

The middleware export functionality will shortly be modified to use numbered directories (1-100 etc) to reduce the number of files/directories per
directory to 100. This is to improve filesystem performance on Windows Tomcat servers. This will result in the code changing from this example.

The *GetExtractStatus* web service returns the following textual strings:

* STUDY_INCOMPLETE_NOT_ZIPPABLE: returned for the following rif40_studies.study_state codes/meanings:
  * C: created, not verified;
  * V: verified, but no other work done; [NOT USED BY MIDDLEWARE]
  * E: extracted imported or created, but no results or maps created;
  * R: initial results population, create map table; [NOT USED BY MIDDLEWARE] design]
  * W: R warning. [NOT USED BY MIDDLEWARE];

* STUDY_FAILED_NOT_ZIPPABLE: returned for the following rif40_studies.study_state codes/meanings:
  * G: Extract failure, extract, results or maps not created;
  * F: R failure, R has caught one or more exceptions [depends on the exception handler];

* STUDY_EXTRACTABLE_NEEDS_ZIPPING: returned for the following rif40_studies.study_state code/meaning of: S: R success;
  when the ZIP extrsct file has not yet been created;

* STUDY_EXTRABLE_ZIPPID: returned for the following rif40_studies.study_statu code/meaning of: S: R success;
  when the ZIP extrsct file has been created;

* STUDY_NOT_FOUND: returned where the studyID was not found in rif40_studies.

## 1.2 GetExtractStatus Usage

The *GetExtractStatus* web service is used by the export dashboard in the front end and on is one of three export related middleware calls:

* *createStudyExtract*: This creates the ZIP file if the study completed without error;
* *getStudyExtract*: This fetches the ZIP file for the user (i.e. is a download link).

The purpose of the *GetExtractStatus* web service is to manage what the download button does. The *export" button can:

* Do nothing: the download button is disabled; either the study has not yet compoleted, did not complete correctly or in future is not yet permitted by
  Information Governance. Currently studies where the study has not yet compoleted or did not complete correctly are not displayed on the export
  dashboard;
* Export a study: This gets the muddleware to create the zipfile;
* Download a study: This permits the user the download the newly created zip file.

Currently the user has to push the export button at least twice;
firstly to create the extract and then again one or more time every time the extract is downloaded. I.e. the study export omnly needs to be created
once! As exporting a large study may take some minutes it was decided not the "click" the completed download link for the user as the user may have
moved onto other tasks.

Two screenshots of the process are shown below. Firstly, after an export has been requested, including showing the effect of clicking the
download link:

![RIF Export]({{ site.baseurl }}/development/RIF_export.png)

And secondly, after the download the file is available for fresh download:

![RIF Export]({{ site.baseurl }}/development/RIF_export2.png)

If the export is then saved to disk and unzipped, the R phase of the study can be re-run. This proves the validity of the extract and
gives the investigator the basis of future analysis. The export will be enhanced to add maps over the winter of 2017:

```
C:\Users\Peter\Downloads\peter_s53a\data>rif40_run_R.bat
##########################################################################################
#
# Run R script on a study extract.
#
# USERID=peter
# DBNAME=sahsuland
# DBHOST=localhost\SQLEXPRESS
# DBPORT=1433
# DB_DRIVER_PREFIX=jdbc:sqlserver
# DB_DRIVER_CLASS_NAME=com.microsoft.sqlserver.jdbc.SQLServerDriver
# STUDYID=53
# INVESTIGATIONNAME=TEST_1002
# INVESTIGATIONID=53
# ODBCDATASOURCE=SQLServer11
# MODEL=HET
# COVARIATENAME=none
#
##########################################################################################
"C:\Program Files\R\R-3.4.0\bin\x64\RScript" Adj_Cov_Smooth_csv.R ^
--db_driver_prefix=jdbc:sqlserver --db_driver_class_name=com.microsoft.sqlserver.jdbc.SQLServerDriver --odbcDataSource=SQLServer11 ^

--dbHost=localhost\SQLEXPRESS --dbPort=1433 --dbName=sahsuland ^
--studyID=53 --investigationName=TEST_1002 --investigationId=53 ^
--model=HET --covariateName=none ^
--userID=peter --password=XXXXXXXXXXXXXXXXXXXXXX ^
--scratchspace=c:\rifDemo\scratchSpace\ --dumpframestocsv=FALSE
Warning message:
package 'pryr' was built under R version 3.4.2
Loading required package: sp
Loading required package: methods
Loading required package: Matrix
This is INLA 0.0-1485844051, dated 2017-01-31 (09:14:12+0300).
See www.r-inla.org/contact-us for how to get help.

Attaching package: 'INLA'

The following object is masked from 'package:pryr':

    f

Checking rgeos availability: FALSE
        Note: when rgeos is not available, polygon geometry     computations in maptools depend on gpclib,
        which has a restricted licence. It is disabled by default;
        to enable gpclib, type gpclibPermit()
CATALINA_HOME=C:\Program Files\Apache Software Foundation\Tomcat 8.5
Source: C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\rifServices\WEB-INF\classes\performSmoothingActivity.R
13  arguments were supplied
Parsing parameters
                   name                                        value
1      db_driver_prefix                               jdbc:sqlserver
2  db_driver_class_name com.microsoft.sqlserver.jdbc.SQLServerDriver
3        odbcDataSource                                  SQLServer11
4                dbHost                        localhost\\SQLEXPRESS
5                dbPort                                         1433
6                dbName                                    sahsuland
7               studyID                                           53
8     investigationName                                    TEST_1002
9       investigationId                                           53
10                model                                          HET
11        covariateName                                         none
12         scratchspace                  c:\\rifDemo\\scratchSpace\\
13      dumpframestocsv                                        FALSE
Performing basic stats and smoothing
Covariates: NONE
Bayes smoothing with HET model type no adjustment
Posterior probability calculated
callPerformSmoothingActivity exitValue: 0
performSmoothingActivity() OK:  0
check.integer: 01.001.000100.1; as.numeric(str): 1; isNumeric: TRUE; isInteger: TRUE; isNotRounded: TRUE; isIntRegexp: FALSE; check.
integer.Result: FALSE
typeof(result$area_id[1]) ---->  integer ; check.integer(result$area_id[1]):  FALSE ; result$area_id[1]:  01.001.000100.1
check.integer: 01.001.000100.1; as.numeric(str): 1; isNumeric: TRUE; isInteger: TRUE; isNotRounded: TRUE; isIntRegexp: FALSE; check.
integer.Result: FALSE
.Primitive("return")
Total memory is use: 145322216
Memory by object:
AdjRowset: 309240
area_id_is_integer: 48
data: 4414272
errorTrace: 1048
ototal: 48
result: 746696
Garbage collection 169 = 133+15+21 (level 2) ...
103.1 Mbytes of cons cells used (34%)
30.6 Mbytes of vectors used (32%)
Free 5471352 memory; total memory is use: 140009424
Memory by object:
errorTrace: 1048
R script ran OK
Adj_Cov_Smooth_csv.R procedure OK for study: 53; investigation: 53
```

## 1.3 GetExtractStatus Parameters

Tne web service has the following parameters:

* userID: Database username of logged on user.
* studyID: Integer study identifier (database study_id field).

## 1.4 GetExtractStatus Return Data

The GetExtractStatus web service returns the textual extract status, e.g. ```STUDY_EXTRABLE_ZIPPID``` or a JSON error:
```
[{"errorMessages":["Record \"Study\" with value \"535\" not found in the database."]}]
```

# 2. Middleware Steps

The middleware consists of five class in three distinct layers:

* Restful Web Services:
  * PG/MSSQLRIFStudySubmissionWebServiceResource class
  * PG/MSSQLAbstractRIFWebServiceResource class
* Common Business Concept Layer:
  * RIFStudySubmissionAPI class
* Data storage layer:
  * PG/MSSQLAbstractRIFStudySubmissionService class
  * PG/MSSQLStudyExtractManager class

## 2.1 Add to the PG/MSSQLRIFStudySubmissionWebServiceResource class

The key *PG/MSSQLRIFStudySubmissionWebServiceResource* classes define the *ms* and *pg* family webservices:

* rapidInquiryFacility\rifServices\src\main\java\rifServices\restfulWebServices\ms\MSSQLRIFStudySubmissionWebServiceResource.java
* rapidInquiryFacility\rifServices\src\main\java\rifServices\restfulWebServices\pg\PGSQLRIFStudySubmissionWebServiceResource.java

In most cases, as in this example, this extends the *PG/MSSQLAbstractRIFWebServiceResource* class

```
@GET
@Produces({"application/json"})
@Path("/getExtractStatus")
public Response getExtractStatus(
	@Context HttpServletRequest servletRequest,
	@QueryParam("userID") String userID,
	@QueryParam("studyID") String studyID) {

	return super.getExtractStatus(
		servletRequest,
		userID,
		studyID);
}
```
The function *super.getExtractStatus()* is analogous to *PG/MSSQLAbstractRIFWebServiceResource.getExtractStatus()*

This class hierarchy probably cannot be de-duplicated, but it does allow webservics to be disabled for one of the database ports.

## 2.2 Add to the PG/MSSQLAbstractRIFWebServiceResource class

* rapidInquiryFacility\rifServices\src\main\java\rifServices\restfulWebServices\ms\MSSQLAbstractRIFWebServiceResource.java
* rapidInquiryFacility\rifServices\src\main\java\rifServices\restfulWebServices\pg\PGSQLAbstractRIFWebServiceResource.java

```
protected Response getExtractStatus(
		final HttpServletRequest servletRequest,
		final String userID,
		final String studyID) {


	String result = null;

	try {
		User user = createUser(servletRequest, userID);

		RIFStudySubmissionAPI studySubmissionService
		= getRIFStudySubmissionService();

		result=studySubmissionService.getExtractStatus(
				user,
				studyID);
	}
	catch(RIFServiceException rifServiceException) {
		rifLogger.error(this.getClass(),
			"MSSQLAbstractRIFWebServiceResource.getExtractStatus error", rifServiceException);
		result
		= serialiseException(
				servletRequest,
				rifServiceException);
	}

	return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
}
```

**In virtually all cases the code is identical.**. A new class *AbstractRIFWebServiceResource* should be extended by the
*PG/MSSQLAbstractRIFWebServiceResource* class [PH suggestion - I may be wrong as I am not a Java programmer].

## 2.3 Add to the Common Business Concept Layer RIFStudySubmissionAPI class

* rapidInquiryFacility\rifServices\src\main\java\rifServices\businessConceptLayer\RIFStudySubmissionAPI.java

This defines PG/MSSQLAbstractRIFStudySubmissionService.getExtractStatus() in the data storage layer

```
public String getExtractStatus(
	final User user,
	final String studyID)
	throws RIFServiceException;
```

## 2.4 Add to PG/MSSQLAbstractRIFStudySubmissionService class

* rapidInquiryFacility\rifServices\src\main\java\rifServices\dataStorageLayer\pg\PGSQLAbstractRIFStudySubmissionService.java
* rapidInquiryFacility\rifServices\src\main\java\rifServices\dataStorageLayer\pg\MSSQLAbstractRIFStudySubmissionService.java

This function is principally to set up the web service and the call the worker function in the Extract Manager:

* Gets a database connection from the pool;
* Checks the user is not blocked (DDoS) protection;
* Validates the web service fields;
* Audits service call;
* Get the Study submission object (RIFStudySubmission);
* Calls: PG/MSSQLStudyExtractManager.getExtractStatus();
* All exceptions are merely logged (i.e. null is returned instead of the expected string)

```
/**
 * Get textual extract status of a study.
 * <p>
 * This fucntion determines whether a study can be extracted from the database and the results returned to the user in a ZIP file
 * </p>
 * <p>
 * Returns the following textual strings:
 * <il>
 *   <li>STUDY_INCOMPLETE_NOT_ZIPPABLE: returned for the following rif40_studies.study_state codes/meanings:
 *     <ul>
 *	     <li>C: created, not verified;</li>
 *	     <li>V: verified, but no other work done; [NOT USED BY MIDDLEWARE]</li>
 *	     <li>E: extracted imported or created, but no results or maps created;</li>
 *	     <li>R: initial results population, create map table; [NOT USED BY MIDDLEWARE] design]</li>
 *	     <li>W: R warning. [NOT USED BY MIDDLEWARE]</li>
 *     <ul>
 *   </li>
 *   <li>STUDY_FAILED_NOT_ZIPPABLE: returned for the following rif40_studies.study_state codes/meanings:
 *	     <li>G: Extract failure, extract, results or maps not created;</li>
 *	     <li>F: R failure, R has caught one or more exceptions [depends on the exception handler]</li>
 *   </li>
 *   <li>STUDY_EXTRACTABLE_NEEDS_ZIPPING: returned for the following rif40_studies.study_state code/meaning of: S: R success;
 *       when the ZIP extrsct file has not yet been created
 *   </il>
 *   <li>STUDY_EXTRABLE_ZIPPID: returned for the following rif40_studies.study_statu  code/meaning of: S: R success;
 *       when the ZIP extrsct file has been created
 *   </il>
 * </il>
 * </p>
 * <p>
 * Calls MSSQLStudyExtractManager.getExtractStatus()
 * </p>
 *
 * @param  _user 		Database username of logged on user.
 * @param  studyID 		Integer study identifier (database study_id field).
 *
 * @return 				Textual extract status
 *						NULL on exception or permission denied by sqlConnectionManager
 */
public String getExtractStatus(
		final User _user,
		final String studyID)
				throws RIFServiceException {


	String result = null;
	RIFLogger rifLogger = RIFLogger.getLogger();

	//Defensively copy parameters and guard against blocked users
	User user = User.createCopy(_user);
	PGSQLConnectionManager sqlConnectionManager
	= rifServiceResources.getSqlConnectionManager();

	if (sqlConnectionManager.isUserBlocked(user) == true) {
		return null;
	}

	Connection connection = null;
	try {

		//Part II: Check for empty parameter values
		FieldValidationUtility fieldValidationUtility
		= new FieldValidationUtility();
		fieldValidationUtility.checkNullMethodParameter(
				"getExtractStatus",
				"user",
				user);
		fieldValidationUtility.checkNullMethodParameter(
				"getExtractStatus",
				"studyID",
				studyID);

		//Check for security violations
		validateUser(user);
		fieldValidationUtility.checkMaliciousMethodParameter(
				"getExtractStatus",
				"studyID",
				studyID);

		//Audit attempt to do operation
		String auditTrailMessage
		= RIFServiceMessages.getMessage("logging.getExtractStatus",
				user.getUserID(),
				user.getIPAddress(),
				studyID);
		rifLogger.info(
				getClass(),
				auditTrailMessage);

		//Assign pooled connection
		connection
		= sqlConnectionManager.assignPooledWriteConnection(user);

		PGSQLRIFSubmissionManager sqlRIFSubmissionManager
		= rifServiceResources.getRIFSubmissionManager();
		RIFStudySubmission rifStudySubmission
		= sqlRIFSubmissionManager.getRIFStudySubmission(
				connection,
				user,
				studyID);

		PGSQLStudyExtractManager studyExtractManager
		= rifServiceResources.getSQLStudyExtractManager();
		result=studyExtractManager.getExtractStatus(
				connection,
				user,
				rifStudySubmission,
				studyID);

	}
	catch(RIFServiceException rifServiceException) {
		//Audit failure of operation
		logException(
				user,
				"getExtractStatus",
				rifServiceException);
	}
	finally {
		rifLogger.info(getClass(), "get ZIP file extract status: " + result);
		//Reclaim pooled connection
		sqlConnectionManager.reclaimPooledWriteConnection(
				user,
				connection);
	}

	return result;
}
```

**In virtually all cases the code is identical.**.

## 2.5 Add to the PG/MSSQLStudyExtractManager class

* rapidInquiryFacility\rifServices\src\main\java\rifServices\dataStorageLayer\ms\MSSQLStudyExtractManager.java
* rapidInquiryFacility\rifServices\src\main\java\rifServices\dataStorageLayer\pg\PGSQLStudyExtractManager.java

This function does the principal work associated with the web service specification:

* Gets the study state from the database using getRif40StudyState();
* Sets STUDY_INCOMPLETE_NOT_ZIPPABLE or STUDY_FAILED_NOT_ZIPPABLE as the return value
  or if the state is **S** sets STUDY_EXTRACTABLE_NEEDS_ZIPPING/STUDY_EXTRABLE_ZIPPID as the return value depending
  on whether the ZIP file needs creating or not;
* Exceptions are logged and re-thrown as a RIFServiceException

```
/**
 * Get textual extract status of a study.
 * <p>
 * This fucntion determines whether a study can be extracted from the database and the results returned to the user in a ZIP file
 * </p>
 * <p>
 * Returns the following textual strings:
 * <il>
 *   <li>STUDY_INCOMPLETE_NOT_ZIPPABLE: returned for the following rif40_studies.study_state codes/meanings:
 *     <ul>
 *	     <li>C: created, not verified;</li>
 *	     <li>V: verified, but no other work done; [NOT USED BY MIDDLEWARE]</li>
 *	     <li>E: extracted imported or created, but no results or maps created;</li>
 *	     <li>R: initial results population, create map table; [NOT USED BY MIDDLEWARE] design]</li>
 *	     <li>W: R warning. [NOT USED BY MIDDLEWARE]</li>
 *     <ul>
 *   </li>
 *   <li>STUDY_FAILED_NOT_ZIPPABLE: returned for the following rif40_studies.study_state codes/meanings:
 *	     <li>G: Extract failure, extract, results or maps not created;</li>
 *	     <li>F: R failure, R has caught one or more exceptions [depends on the exception handler]</li>
 *   </li>
 *   <li>STUDY_EXTRACTABLE_NEEDS_ZIPPING: returned for the following rif40_studies.study_state code/meaning of: S: R success;
 *       when the ZIP extrsct file has not yet been created
 *   </il>
 *   <li>STUDY_EXTRABLE_ZIPPID: returned for the following rif40_studies.study_status code/meaning of: S: R success;
 *       when the ZIP extrsct file has been created
 *   </il>
 * </il>
 * </p>
 *
 * @param  connection	Database specfic Connection object assigned from pool
 * @param  user 		Database username of logged on user.
 * @param  rifStudySubmission 		RIFStudySubmission object.
 * @param  studyID 		Study_id (as text!).
 *
 * @return 				Textual extract status
 *
 * @exception  			RIFServiceException		Catches all exceptions, logs, and re-throws as RIFServiceException
 */
 public String getExtractStatus(
		final Connection connection,
		final User user,
		final RIFStudySubmission rifStudySubmission,
		final String studyID
		)
				throws RIFServiceException {

	String result=null;
	File submissionZipFile = null;
	String zipFileName="UNKNOWN";

	try {
		//Establish the phrase that will be used to help name the main zip
		//file and data files within its directories

		String studyStatus=getRif40StudyState(connection, studyID);

		if (studyStatus == null) { 	// Study ID does not exist. You will not get this
									// [this is raised as an exception in the calling function: RIFStudySubmission.getRIFStudySubmission()]
			throw new Exception("STUDY_NOT_FOUND: " + studyID);
		}

		if (result != null && studyStatus != null) {
			switch (studyStatus.charAt(0)) {
				case 'C':
				case 'V':
				case 'E':
				case 'R':
				case 'W':
					result="STUDY_INCOMPLETE_NOT_ZIPPABLE";
					break;
				case 'G':
				case 'F':
					result="STUDY_FAILED_NOT_ZIPPABLE";
					break;
				case 'S':	/* R success */
					break;
				default:
					throw new Exception("Invalid rif40_studies.study_state: " + studyStatus);
			}
		}

		if (result == null) {
			String baseStudyName
			= createBaseStudyFileName(rifStudySubmission, studyID);

			submissionZipFile = createSubmissionZipFile(
					user,
					baseStudyName);
			zipFileName=submissionZipFile.getAbsolutePath();
			if (submissionZipFile.isFile()) { // ZIP file exists - no need to recreate
				result="STUDY_EXTRABLE_ZIPPID";
			}
			else { // No zip file
				result="STUDY_EXTRACTABLE_NEEDS_ZIPPING";
			}
		}
	}
	catch(Exception exception) {
		rifLogger.error(this.getClass(), "PGSQLStudyExtractManager ERROR", exception);

		String errorMessage
			= RIFServiceMessages.getMessage(
				"sqlStudyStateManager.error.unableToGetExtractStatus",
				user.getUserID(),
				studyID,
				zipFileName);
		RIFServiceException rifServiceExeption
			= new RIFServiceException(
				RIFServiceError.ZIPFILE_GET_STATUS_FAILED,
				errorMessage);
		throw rifServiceExeption;
	}

	return result;
}

public String getRif40StudyState(
		final Connection connection,
		final String studyID)
				throws Exception {

	//get study_state
	SQLGeneralQueryFormatter studyStatusQueryFormatter = new SQLGeneralQueryFormatter();
	studyStatusQueryFormatter.addQueryLine(0, "SELECT a.study_state");
	studyStatusQueryFormatter.addQueryLine(0, "FROM rif40.rif40_studies a");
	studyStatusQueryFormatter.addQueryLine(0, "WHERE a.study_id = ?");

	ResultSet studyStatusResultSet = null;
	String studyStatus = null;

	try {
		logSQLQuery("getRif40StudyState", studyStatusQueryFormatter, studyID);
		PreparedStatement studyStatusStatement = createPreparedStatement(connection, studyStatusQueryFormatter);
		studyStatusStatement.setInt(1, Integer.parseInt(studyID));
		studyStatusResultSet = studyStatusStatement.executeQuery();
		studyStatusResultSet.next();
		studyStatus = studyStatusResultSet.getString(1);
	}
	catch (Exception exception) {
		rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + lineSeparator + studyStatusQueryFormatter.generateQuery(),
			exception);
		throw exception;
	}
	return studyStatus;
}
```

**In some cases the code is identical**. Principally Microsoft SQL Server has no concept on an object search path or **SYNONYM**s so requires
the the table name to be prefixed with the schema name. The same code would work on Postgres, but this reduces deployment functionality.

## 2.6 Addtional files needing editing

### 2.6.1 Add audit trail message

Add messaage template for RIFServiceMessages.getMessage() messages to:

* rapidInquiryFacility\rifServices\src\main\resources\RIFServiceMessages.properties

```
logging.getExtractStatus=User:\"{0}\" IP:\"{1}\" Cheking status of extract for study {2}.

sqlStudyStateManager.error.unableToGetExtractStatus=Unable to get extract zipfile status for user \"{0}\", study \"{1}\", zipfile \"{2}\".
```

### 2.6.2 Add the new error code to RIFServiceError enum

Add error code to RIFServiceError enum to:

* rapidInquiryFacility\rifServices\src\main\java\rifServices\system\RIFServiceError.java

```
ZIPFILE_GET_STATUS_FAILED,/* Unable to get Zipfile status*/
```

### 2.6.3 To add SQL statement debug to AbstractSQLManager properties file

Add logSQLQuery() SQL statment name to:

* rapidInquiryFacility\rifServices\src\main\resources\AbstractSQLManager.properties

```
getRif40StudyState=true
```

# 3. Front End Steps

## 3.1 Add to backend requests

* rapidInquiryFacility\rifWebApplication\src\main\webapp\WEB-INF\backend\services\rifs-back-requests.js
```
//Get Extract Status - can Zip file be created/fetched
self.getExtractStatus = function (username, studyID) {
	//http://localhost:8080/rifServices/studySubmission/pg/getExtractStatus=dwmorley&studyID=46
	return studySubmissionURL + DatabaseService.getDatabase() + 'getExtractStatus?userID=' + username + '&studyID=' + studyID;
};
```

## 3.2 Add to controller

* rapidInquiryFacility\rifWebApplication\src\main\webapp\WEB-INF\dashboards\export\controllers\rifc-expt-export.js
```
$scope.setupZipButton = function setupZipButton() {
	$scope.extractStatus=user.getExtractStatus(user.currentUser, $scope.studyID["exportmap"].study_id).then(function (res) {
		if (res.data === "STUDY_EXTRACTABLE_NEEDS_ZIPPING") {
			$scope.exportTAG="Export Study Tables";
			$scope.exportURL = undefined;
			$scope.disableMapListButton=false;
		}
		else if (res.data === "STUDY_EXTRABLE_ZIPPID") {
			$scope.exportURL = user.getZipFileURL(user.currentUser, $scope.studyID["exportmap"].study_id,
				$scope.exportLevel); // Set mapListButtonExport URL
			$scope.exportTAG="Download Study Export";
			$scope.disableMapListButton=false;
		}
		else if (res.data === "STUDY_INCOMPLETE_NOT_ZIPPABLE") {
			$scope.exportTAG="Study Incomplete";
			$scope.exportURL = undefined;
			$scope.disableMapListButton=true;
		}
		else if (res.data === "STUDY_FAILED_NOT_ZIPPABLE") {
			$scope.exportTAG="Study Failed";
			$scope.exportURL = undefined;
			$scope.disableMapListButton=true;
		}
		else {
			$scope.exportTAG="Study NOT Exportable";
			$scope.exportURL = undefined;
			$scope.disableMapListButton=true;
		}
	});
}
```

The function ```$scope.setupZipButton();``` is then called at appropriate points in the controller so the export tag (button title), export
download URL and button disable/enable state is set appropriately. See github change:
https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/commit/606fb25e5dd0745f8c26bdf91d27716e44cf11d0

# 4. Example log of error from restful services, including stacks

In this case the study state column was deliberately missspelt to generate an error! (*study_statey* instead of *study_state*):

```
User:"peter" IP:"0:0:0:0:0:0:0:1" Cheking status of extract for study 53.
15:49:04.857 [http-nio-8080-exec-7] WARN  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager]:
MSSQLAbstractSQLManager checkIfQueryLoggingEnabled=FALSE property: getRif40StudyState NOT FOUND
15:49:04.889 [http-nio-8080-exec-7] ERROR rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager]:
Error in SQL Statement: >>>
SELECT a.study_statey
FROM rif40.rif40_studies a
WHERE a.study_id = ?


getMessage:          SQLServerException: Invalid column name 'study_statey'.
getRootCauseMessage: SQLServerException: Invalid column name 'study_statey'.
getThrowableCount:   1
getRootCauseStackTrace >>>
com.microsoft.sqlserver.jdbc.SQLServerException: Invalid column name 'study_statey'.
	at com.microsoft.sqlserver.jdbc.SQLServerException.makeFromDatabaseError(SQLServerException.java:232)
	at com.microsoft.sqlserver.jdbc.SQLServerStatement.getNextResult(SQLServerStatement.java:1672)
	at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.doExecutePreparedStatement(SQLServerPreparedStatement.java:460)
	at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement$PrepStmtExecCmd.doExecute(SQLServerPreparedStatement.java:405)
	at com.microsoft.sqlserver.jdbc.TDSCommand.execute(IOBuffer.java:7535)
	at com.microsoft.sqlserver.jdbc.SQLServerConnection.executeCommand(SQLServerConnection.java:2438)
	at com.microsoft.sqlserver.jdbc.SQLServerStatement.executeCommand(SQLServerStatement.java:208)
	at com.microsoft.sqlserver.jdbc.SQLServerStatement.executeStatement(SQLServerStatement.java:183)
	at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.executeQuery(SQLServerPreparedStatement.java:317)
	at rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager.getRif40StudyState(MSSQLStudyExtractManager.java:773)
	at rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager.getExtractStatus(MSSQLStudyExtractManager.java:263)
	at rifServices.dataStorageLayer.ms.MSSQLAbstractRIFStudySubmissionService.getExtractStatus(MSSQLAbstractRIFStudySubmissionService.java:1190)
	at rifServices.restfulWebServices.ms.MSSQLAbstractRIFWebServiceResource.getExtractStatus(MSSQLAbstractRIFWebServiceResource.java:972)
	at rifServices.restfulWebServices.ms.MSSQLRIFStudySubmissionWebServiceResource.getExtractStatus(MSSQLRIFStudySubmissionWebServiceResource.java:1216)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)
	at com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)
	at com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)
	at com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)
	at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
	at com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)
	at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
	at com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)
	at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1542)
	at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1473)
	at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)
	at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)
	at com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)
	at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)
	at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:742)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:230)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165)
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:192)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165)
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:198)
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96)
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:478)
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:140)
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:80)
	at org.apache.catalina.valves.AbstractAccessLogValve.invoke(AbstractAccessLogValve.java:624)
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:87)
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341)
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:799)
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66)
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:861)
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1455)
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
	at java.lang.Thread.run(Thread.java:745)
<<< End getRootCauseStackTrace.
15:49:04.891 [http-nio-8080-exec-7] ERROR rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager]:
MSSQLStudyExtractManager ERROR
getMessage:          SQLServerException: Invalid column name 'study_statey'.
getRootCauseMessage: SQLServerException: Invalid column name 'study_statey'.
getThrowableCount:   1
getRootCauseStackTrace >>>
com.microsoft.sqlserver.jdbc.SQLServerException: Invalid column name 'study_statey'.
	at com.microsoft.sqlserver.jdbc.SQLServerException.makeFromDatabaseError(SQLServerException.java:232)
	at com.microsoft.sqlserver.jdbc.SQLServerStatement.getNextResult(SQLServerStatement.java:1672)
	at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.doExecutePreparedStatement(SQLServerPreparedStatement.java:460)
	at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement$PrepStmtExecCmd.doExecute(SQLServerPreparedStatement.java:405)
	at com.microsoft.sqlserver.jdbc.TDSCommand.execute(IOBuffer.java:7535)
	at com.microsoft.sqlserver.jdbc.SQLServerConnection.executeCommand(SQLServerConnection.java:2438)
	at com.microsoft.sqlserver.jdbc.SQLServerStatement.executeCommand(SQLServerStatement.java:208)
	at com.microsoft.sqlserver.jdbc.SQLServerStatement.executeStatement(SQLServerStatement.java:183)
	at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.executeQuery(SQLServerPreparedStatement.java:317)
	at rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager.getRif40StudyState(MSSQLStudyExtractManager.java:773)
	at rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager.getExtractStatus(MSSQLStudyExtractManager.java:263)
	at rifServices.dataStorageLayer.ms.MSSQLAbstractRIFStudySubmissionService.getExtractStatus(MSSQLAbstractRIFStudySubmissionService.java:1190)
	at rifServices.restfulWebServices.ms.MSSQLAbstractRIFWebServiceResource.getExtractStatus(MSSQLAbstractRIFWebServiceResource.java:972)
	at rifServices.restfulWebServices.ms.MSSQLRIFStudySubmissionWebServiceResource.getExtractStatus(MSSQLRIFStudySubmissionWebServiceResource.java:1216)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)
	at com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)
	at com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)
	at com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)
	at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
	at com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)
	at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
	at com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)
	at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1542)
	at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1473)
	at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)
	at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)
	at com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)
	at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)
	at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:742)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:230)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165)
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:192)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165)
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:198)
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96)
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:478)
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:140)
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:80)
	at org.apache.catalina.valves.AbstractAccessLogValve.invoke(AbstractAccessLogValve.java:624)
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:87)
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341)
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:799)
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66)
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:861)
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1455)
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
	at java.lang.Thread.run(Thread.java:745)
<<< End getRootCauseStackTrace.
15:49:04.893 [http-nio-8080-exec-7] ERROR rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLAbstractRIFService]:
getExtractStatus
getMessage:          RIFServiceException: Unable to get extract zipfile status for user "peter", study "53", zipfile "UNKNOWN".
getRootCauseMessage: RIFServiceException: Unable to get extract zipfile status for user "peter", study "53", zipfile "UNKNOWN".
getThrowableCount:   1
getRootCauseStackTrace >>>
rifGenericLibrary.system.RIFServiceException: Unable to get extract zipfile status for user "peter", study "53", zipfile "UNKNOWN".
	at rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager.getExtractStatus(MSSQLStudyExtractManager.java:313)
	at rifServices.dataStorageLayer.ms.MSSQLAbstractRIFStudySubmissionService.getExtractStatus(MSSQLAbstractRIFStudySubmissionService.java:1190)
	at rifServices.restfulWebServices.ms.MSSQLAbstractRIFWebServiceResource.getExtractStatus(MSSQLAbstractRIFWebServiceResource.java:972)
	at rifServices.restfulWebServices.ms.MSSQLRIFStudySubmissionWebServiceResource.getExtractStatus(MSSQLRIFStudySubmissionWebServiceResource.java:1216)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)
	at com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)
	at com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)
	at com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)
	at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
	at com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)
	at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
	at com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)
	at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1542)
	at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1473)
	at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)
	at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)
	at com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)
	at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)
	at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:742)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:230)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165)
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:192)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165)
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:198)
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96)
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:478)
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:140)
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:80)
	at org.apache.catalina.valves.AbstractAccessLogValve.invoke(AbstractAccessLogValve.java:624)
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:87)
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341)
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:799)
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66)
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:861)
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1455)
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
	at java.lang.Thread.run(Thread.java:745)
<<< End getRootCauseStackTrace.
15:49:04.894 [http-nio-8080-exec-7] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLProductionRIFStudySubmissionService]:
get ZIP file extract status: null
15:49:04.895 [http-nio-8080-exec-7] ERROR rifGenericLibrary.util.RIFLogger : [rifServices.restfulWebServices.ms.MSSQLRIFStudySubmissionWebServiceResource]:
MSSQLAbstractRIFWebServiceResource.getExtractStatus error
getMessage:          RIFServiceException: Unable to get extract zipfile status for user "peter", study "53", zipfile "UNKNOWN".
getRootCauseMessage: RIFServiceException: Unable to get extract zipfile status for user "peter", study "53", zipfile "UNKNOWN".
getThrowableCount:   1
getRootCauseStackTrace >>>
rifGenericLibrary.system.RIFServiceException: Unable to get extract zipfile status for user "peter", study "53", zipfile "UNKNOWN".
	at rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager.getExtractStatus(MSSQLStudyExtractManager.java:313)
	at rifServices.dataStorageLayer.ms.MSSQLAbstractRIFStudySubmissionService.getExtractStatus(MSSQLAbstractRIFStudySubmissionService.java:1190)
	at rifServices.restfulWebServices.ms.MSSQLAbstractRIFWebServiceResource.getExtractStatus(MSSQLAbstractRIFWebServiceResource.java:972)
	at rifServices.restfulWebServices.ms.MSSQLRIFStudySubmissionWebServiceResource.getExtractStatus(MSSQLRIFStudySubmissionWebServiceResource.java:1216)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)
	at com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)
	at com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)
	at com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)
	at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
	at com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)
	at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
	at com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)
	at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1542)
	at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1473)
	at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)
	at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)
	at com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)
	at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)
	at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:742)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:230)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165)
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:192)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165)
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:198)
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96)
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:478)
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:140)
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:80)
	at org.apache.catalina.valves.AbstractAccessLogValve.invoke(AbstractAccessLogValve.java:624)
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:87)
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341)
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:799)
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66)
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:861)
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1455)
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
	at java.lang.Thread.run(Thread.java:745)
<<< End getRootCauseStackTrace.
```


Peter Hambly
16th October 2017
