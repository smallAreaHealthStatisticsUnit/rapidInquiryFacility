
##====================================================================
# SCRIPT VARIABLES
##====================================================================
#Variables that hold database connectivity information.  For now, we
#will rely on named ODBC sources but in future the script should be 
#altered to use host:port/databaseName
userID <- ""
password <- ""
dbName <- ""
dbHost <- ""
dbPort <- ""
dbConnectionString <- ""
numberOfInvestigations <- ""

#The identifier of the study whose extract table fields need to be smoothed.
studyID <- ""

#We expect models to have one of the three values: 'BYM', 'CAR' or 'HET'
models <- ""

#The name of the extract table that is created by the middleware for a given
#study.  It is of the format rif_studies.s[study_id]_extract
extractTableName <- ""

#The name of the skeleton table created by the RIF middleware to hold smoothed results
#for a given study.  Initially the values for smoothed columns will be empty.  It is 
#of the format rif_studies.s[study_id]_map 
mapTableName <- ""

#The name of the temporary table that this script uses to hold the data frame
#containing smoothed results.  It should have a 1:1 correspondence between
#its fields and fields that appear in the map table skeleton.  
temporarySmoothedResultsTableName <- ""



 

##====================================================================
## FUNCTION: processCommandLineArguments
## DESCRIPTION: parses the command line arguments and returns a data
## frame that has two named columns: name and value
##====================================================================
processCommandLineArguments <- function() {

	allArgs <- commandArgs(trailingOnly=TRUE)
	numCommandLineArgs <- length(allArgs)

	refinedList <- list()

	if (numCommandLineArgs==0) {
		print("No arguments supplied")
	}else{
		print("Arguments were supplied")
		for (i in 1:numCommandLineArgs) {
			##Filter command arguments that fit the form
			##--X=Y
			if (grepl('^--', allArgs[i]) == TRUE) {
				refinedList <- c(refinedList, allArgs[i])
			}
		}
		parsedNameValuePairs <- strsplit(sub("^--", "", refinedList), "=")
		parametersDataFrame <- as.data.frame(do.call("rbind", parsedNameValuePairs))
		names(parametersDataFrame)[1] <- paste("name")
		names(parametersDataFrame)[2] <- paste("value")	
		
		print("About to work out")
		for (i in 1:nrow(parametersDataFrame)) {
			print(parametersDataFrame[i,1])
			
			if (grepl('user_id', parametersDataFrame[i, 1]) == TRUE) {
				userID <<- parametersDataFrame[i, 2]
			} else if (grepl('password', parametersDataFrame[i, 1]) == TRUE){
				password <<- parametersDataFrame[i, 2]
			} else if (grepl('db_name', parametersDataFrame[i, 1]) == TRUE){
				dbName <<- parametersDataFrame[i, 2]
			} else if (grepl('db_host', parametersDataFrame[i, 1]) == TRUE){
				dbHost <<- parametersDataFrame[i, 2]
			} else if (grepl('db_port', parametersDataFrame[i, 1]) == TRUE){
				dbPort <<- parametersDataFrame[i, 2]	
			} else if (grepl('study_id', parametersDataFrame[i, 1]) == TRUE){
				studyID <<- parametersDataFrame[i, 2]
			} else if (grepl('num_investigations', parametersDataFrame[i, 1]) == TRUE){
				numberOfInvestigations <<- parametersDataFrame[i, 2]
			} else if (grepl('odbc_data_source', parametersDataFrame[i, 1]) == TRUE){
				odbcDataSource <<- parametersDataFrame[i, 2]
			} else if (grepl('models', parametersDataFrame[i, 1]) == TRUE){
				models <<- parametersDataFrame[i, 2]				
			}			
		}
			
		return(parametersDataFrame)
	}
}

##====================================================================
##FUNCTION: createDatabaseConnectionString
##DESCRIPTION: assembles pieces of database information such as
##the host, port and database name to create a database connection
##string that can be used to make an ODBC connection
##====================================================================
createDatabaseConnectionString <- function() {
	
#print( paste0("host==", dbHost, "=="))
#print( paste0("port==", dbPort, "=="))
#print( paste0("datatabase name==", dbName, "=="))

	return(paste0(dbHost, ":", dbPort, "/", dbName))
}

##==========================================================================
#FUNCTION: establishTableNames
#DESCRIPTION: uses the study_id number to determine the names of
#tables that are used in the script.  There are three tables that are
#important for this activity and their meanings are as follows:
#rif_studies.s[study_id]_extract - contains the extract data table created
#by the middleware
#
#rif_studies.s[study_id]_map - the skeleton table that the RIF middleware
#creates to hold smoothed results
#
##==========================================================================
establishTableNames <-function(vstudyID) {
	print(paste("Study ID:", vstudyID))
	extractTableName <<- paste0("rif_studies.s", vstudyID, "_extract")
	temporarySmoothedResultsTableName <<-paste("rif_studies.tmp_s", vstudyID, "_map", sep="")
	mapTableName <<- paste0("rif_studies.s", vstudyID, "_map")
}


##==========================================================================
#FUNCTION: generateFakeValues
#DESCRIPTION: This function is just used to generate a fake value for 
#every row that appears in the extract table that has been loaded into 
#the data frame. This should be removed later on when the real smoothing
#code is used.
##==========================================================================
generateFakeValues <- function(numberOfRows, value) {
	columnValues = rep(value, numberOfRows)
	return(columnValues)
}

generateFakeGenderValues <- function(numberOfRows) {
	print(paste0("generateFakeGenderValues numberOfRows=", numberOfRows))
	genderValues <- vector(,numberOfRows)

	for (i in 1:numberOfRows) {
		#Cycle through assigning values of 1,2,3 so that 
		#rows will get values for male, female and both
		genderValues[i] <-  (i %% 3)	
	}
	
	return(genderValues)
}


##====================================================================
##FUNCTION: createSmoothedExtractResults
##DESCRIPTION: assembles pieces of database information such as
##the host, port and database name to create a database connection
##string that can be used to make an ODBC connection
##====================================================================
createSmoothedExtractResults <- function() {


	#Part I: Read in the extract table
	#=================================
	#Read original extract table data into a data frame
	print("============EXTRACT TABLE NAME ====================")
	print(extractTableName)
	print("============EXTRACT TABLE NAME ====================")
	sqlGetStuff <- paste0("SELECT * FROM ", mapTableName)
	print(paste0("smoothing query==",sqlGetStuff,"=="))
	#Use the as.is flag to prevent R from using its own hints to assign table columns to data types
	#The flag was needed because when it interpretted dummy area_id values that looked like
	#"01.001", it assumed it was a numeric value and made it "1.001". This would mean that a join 
	#condition on the area_id field would fail.
	originalExtractTable <- sqlQuery(connDB, sqlGetStuff, as.is=TRUE)
	numberOfRows <- nrow(originalExtractTable)	
	
	print(paste0("createSmoothedExtractResults numberOfRows=",numberOfRows, "=="))
	##originalExtractTable$area_id <- as.character(originalExtractTable$area_id)

	#Part II: Perform smoothing operation
	#====================================	
	#Perform smoothing operation.  For now, create a set of fake values that will
	#simulate the data transformations done by smoothing.  By the end of the smoothing
	#activity, the data frame will have all the columns below, using exactly the
	#same case as that expected by the RIF
	
	
	#simulating smoothing transformation	
	originalExtractTable$genders <- generateFakeGenderValues(numberOfRows)

	originalExtractTable$direct_standardisation <- rep(0, numberOfRows)
	originalExtractTable$adjusted <- generateFakeValues(numberOfRows, 0)
	originalExtractTable$observed <- rep(1.111, numberOfRows)
	originalExtractTable$expected <- rep(2.111, numberOfRows)
	originalExtractTable$lower95 <- rep(4.111, numberOfRows)
	originalExtractTable$upper95 <- rep(5.111, numberOfRows)
	originalExtractTable$relative_risk <- rep(6.111, numberOfRows)
	originalExtractTable$smoothed_relative_risk <- rep(7.111, numberOfRows)
	originalExtractTable$posterior_probability <- rep(8.111, numberOfRows)
	originalExtractTable$posterior_probability_upper95 <- rep(9.111, numberOfRows)
	originalExtractTable$posterior_probability_lower95 <- rep(10.111, numberOfRows)
	originalExtractTable$residual_relative_risk <- rep(11.111, numberOfRows)
	originalExtractTable$residual_rr_lower95 <- rep(12.111, numberOfRows)
	originalExtractTable$residual_rr_upper95 <- rep(13.111, numberOfRows)
	originalExtractTable$smoothed_smr <- rep(14.111, numberOfRows)
	originalExtractTable$smoothed_smr_lower95 <- rep(15.111, numberOfRows)
	originalExtractTable$smoothed_smr_upper95 <- rep(16.111, numberOfRows)

	return(originalExtractTable) 
}

saveDataFrameToDatabaseTable <- function(data) {

	sqlSave(connDB, data, tablename=temporarySmoothedResultsTableName)
	
	#Add indices to the new table so that its join with s[study_id]_map will be more 
	#efficient
	sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "study_id"))
	sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "band_id"))
	sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "inv_id"))
	sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "genders"))
	sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "adjusted"))
	sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "direct_standardisation"))
	sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "area_id"))
}

generateTableIndexSQLQuery <- function(tableName, columnName) {
	sqlIndexQuery <- paste0(
		"CREATE INDEX ind22_",
		columnName,
		" ON ",
		tableName,
		"(",
		columnName,
		")")
		
	return(sqlIndexQuery);
}

##================================================================================
##FUNCTION: updateMapTable
##DESCRIPTION
##When the study is run, the RIF creates a skeleton map table that has all the 
##fields we expect the smoothed results will contain.  After we do the smoothing
##operation, we will have a temporary table that contains all the smoothed results.
##This method updates cell values in the skeleton map file with values from 
##corresponding fields that exist in the temporary table.
##================================================================================
updateMapTableFromSmoothedResultsTable <- function() {

	updateMapTableSQLQuery <- paste0(
		"UPDATE ", mapTableName, " a ",
		"SET ",
			"genders=b.genders,",
			"direct_standardisation=b.direct_standardisation,",
			"adjusted=b.adjusted,",
			"observed=b.observed,",
			"expected=b.expected,",
			"lower95=b.lower95,",
			"upper95=b.upper95,",
			"relative_risk=b.relative_risk,",
			"smoothed_relative_risk=b.smoothed_relative_risk,",
			"posterior_probability=b.posterior_probability,",
			"posterior_probability_upper95=b.posterior_probability_upper95,",
			"posterior_probability_lower95=b.posterior_probability_lower95,",
			"residual_relative_risk=b.residual_relative_risk,",
			"residual_rr_lower95=b.residual_rr_lower95,",
			"residual_rr_upper95=b.residual_rr_upper95,",
			"smoothed_smr=b.smoothed_smr,",
			"smoothed_smr_lower95=b.smoothed_smr_lower95,",					
			"smoothed_smr_upper95=b.smoothed_smr_upper95 ",
		"FROM ",
			temporarySmoothedResultsTableName,
		" b ",
		"WHERE ",
			"a.study_id=b.study_id AND ",
			"a.band_id=b.band_id AND ",
			"a.inv_id=b.inv_id ",
			"a.inv_id=b.inv_id AND ",
			"a.genders=b.genders AND ",
			"a.area_id=b.area_id");

		print(updateMapTableSQLQuery)
		sqlQuery(connDB, updateMapTableSQLQuery)				
}						

##=============================================================================
#MAIN PROGRAM
#DESCRIPTION
#This script is designed to be called by the Java class
#   rifServices.statisticalServices.BayesianSmoothingService
#When the RIF middleware is creating a study, it creates extract and map
#tables that are of the form rif_studies.s[study_id]_extract and
#rif_studies.s[study_id]_map respectively.  The map table is initially a skeleton
#table which has all the column names that the RIF expects to appear in the 
#smoothed results.  
#
#In this script we read the extract table into an R data frame, and add columns 
#that hold the smoothed results.  We write the data frame table to a temporary 
#table and then update the skeleton map table using values that come from the
#corresponding temporary table that was just created.
#
##=============================================================================

performSmoothingActivity <- function() {	

	code <- 111
	tryCatch({
		dataFrameToSmooth <<- createSmoothedExtractResults()
		saveDataFrameToDatabaseTable(dataFrameToSmooth)
		updateMapTableFromSmoothedResultsTable()
		odbcEndTran(connDB, commit=TRUE)
		},
	error=function(c) {
		print(paste0("error==",c))
		#Error occurred perform rollback
		odbcEndTran(connDB, commit=FALSE)
		code <<- 666
	},
	finally={
	})		
	return(code)
}

processCommandLineArguments()
establishTableNames(studyID)
connDB <- odbcConnect(odbcDataSource, uid=as.character(userID), pwd=as.character(password))
odbcSetAutoCommit(connDB, autoCommit=FALSE)
result <- performSmoothingActivity()
print(paste0("RESULT==", result, "=="))
odbcClose(connDB)
quit(status=result)

