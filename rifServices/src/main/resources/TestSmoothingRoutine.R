

##Validate the command line arguments


##if (require(RODBC)==FALSE) {
##	install(RODBC, true)
##}

library(RODBC)

##if (require(sqldf)==FALSE) {
##	install(sqldf, true)
##}


user <- ""
dbName <- ""
dbHost <- ""
dbPort <- ""
studyID <- "7"
dbConnectionString <- ""
models <- ""

mapTableName <- ""
extractTableName <- ""
smoothingExtractTableName <- ""


linkingColumns <- list()
originalExtractColumns <- list()
smoothedColumns <- list()

useAdjustedSmoothing = FALSE
includeHETColumns = FALSE
includeBYMColumns = FALSE
includeCARColumns = FALSE



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
			
			if (grepl('user', parametersDataFrame[i, 1]) == TRUE) {
				user <<- parametersDataFrame[i, 2]
			} else if (grepl('db_name', parametersDataFrame[i, 1]) == TRUE){
				dbName <<- parametersDataFrame[i, 2]
			} else if (grepl('db_host', parametersDataFrame[i, 1]) == TRUE){
				dbHost <<- parametersDataFrame[i, 2]
			} else if (grepl('db_port', parametersDataFrame[i, 1]) == TRUE){
				dbPort <<- parametersDataFrame[i, 2]	
			} else if (grepl('study_id', parametersDataFrame[i, 1]) == TRUE){
				studyID <<- parametersDataFrame[i, 2]

			} else if (grepl('models', parametersDataFrame[i, 1]) == TRUE){
				models <<- parametersDataFrame[i, 2]
				
				if (models=='BYM') {
					includeBYMColumns = TRUE
					includeCARColumns = FALSE
					includeHETColumns = FALSE				
				} else if (models == 'CAR') {
					includeBYMColumns = FALSE
					includeCARColumns = TRUE
					includeHETColumns = FALSE				
				} else if (models == 'HET') {
					includeBYMColumns = FALSE
					includeCARColumns = FALSE
					includeHETColumns = TRUE				
				} else if (models == 'BYMCAR') {
					includeBYMColumns = TRUE
					includeCARColumns = TRUE
					includeHETColumns = FALSE				
				} else if (models == 'BYMHET') {
					includeBYMColumns = TRUE
					includeCARColumns = FALSE
					includeHETColumns = TRUE				
				} else if (models == 'CARHET') {
					includeBYMColumns = FALSE
					includeCARColumns = TRUE
					includeHETColumns = TRUE				
				} else if (models == 'BYMCARHET') {
					includeBYMColumns = TRUE
					includeCARColumns = TRUE
					includeHETColumns = TRUE				
				}								
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
	return(paste0(dbHost, ":", dbPort, "/", dbName))
}


establishTableNames <-function(vstudyID) {
	print(paste("Study ID:", vstudyID))
	extractTableName <<- paste0("rif_studies.s", vstudyID, "_extract")
	smoothingExtractTableName <<-paste("rif_studies.tmp_s", vstudyID, "_extract", sep="")
	mapTableName <<- paste0("rif_studies.s", vstudyID, "_map")
}

registerLinkingColumn <- function(linkingColumnName) {	
	linkingColumns <<- c(linkingColumns, linkingColumnName)
}

registerOriginalExtractColumn <- function(originalExtractColumnName) {	
	originalExtractColumns <<- c(originalExtractColumns, originalExtractColumnName)
}

registerSmoothedColumn <- function(smoothedColumnName) {
	smoothedColumns <<- c(smoothedColumns, smoothedColumnName)
}

##====================================================================
##  Statistical Functions
##====================================================================


##====================================================================
##FUNCTION: EmpBayes
##DESCRIPTION: performs a Bayesian analysis
##====================================================================






##====================================================================
##FUNCTION: smooth_extract_results
##DESCRIPTION: assembles pieces of database information such as
##the host, port and database name to create a database connection
##string that can be used to make an ODBC connection
##====================================================================


createSmoothedExtractResults_bak <- function() {

	#This command returns a data frame
	#originalExtractTable<-sqlQuery(connDB, paste("SELECT * FROM ",
	#										  extractTableName,
	#										  " LIMIT 10"))

	##data=sqlFetch(connDB, "rif_studies.s1_extract")
	originalExtractTable <- sqlFetch(connDB, extractTableName)
	##originalExtractTable <- data.frame()
	##original_extract_rows<-sqlQuery(connDB, paste("CREATE TABLE rif_studies.s9_adj AS SELECT year FROM rif_studies.s9_extract"))


	##register the original column names in order to help with updating the table later
	##Register columns that will be used to link the original extract table and the
	##table of smoothed results together
	registerLinkingColumn("year")
	registerLinkingColumn("study_id")
	registerLinkingColumn("area_id")
	registerLinkingColumn("sex")
	registerLinkingColumn("age_group")

	##Register all other columns that already appear in the extract table
	registerOriginalExtractColumn("band_id")
	registerOriginalExtractColumn("study_or_comparison")
	registerOriginalExtractColumn("ses")
	registerOriginalExtractColumn("inv_1")
	registerOriginalExtractColumn("total_pop")

	print(originalExtractTable)
	numberOfRows <- nrow(originalExtractTable)
	print(numberOfRows)
	originalExtractTable$blah=unlist(rep(5.666, numberOfRows))
	registerSmoothedColumn("blah");
	##sqlDrop(connDB, originalExtractTable, errors = FALSE)
	print(paste0("SMOOTH TABLE NAME==", smoothingExtractTableName))
	sqlDrop(connDB, smoothingExtractTableName, errors=TRUE)
	sqlSave(connDB, originalExtractTable, tablename=smoothingExtractTableName)
	
}

generateFakeSmoothedValues <- function(numberOfRows, value) {
	return(unlist(rep(value, numberOfRows)))
}

#################################################################################

createSmoothedExtractResults <- function() {

	sqlGetStuff <- paste0("SELECT * FROM ",
						  extractTableName)
	originalExtractTable <- sqlQuery(connDB, sqlGetStuff, as.is=TRUE)

	originalExtractTable$area_id <- as.character(originalExtractTable$area_id)
	
	
	numberOfRows <- nrow(originalExtractTable)


	##register the original column names in order to help with updating the table later
	##Register columns that will be used to link the original extract table and the
	##table of smoothed results together
	registerLinkingColumn("year")
	registerLinkingColumn("study_id")
	registerLinkingColumn("area_id")
	registerLinkingColumn("sex")
	registerLinkingColumn("age_group")

	##Register all other columns that already appear in the extract table
	registerOriginalExtractColumn("band_id")
	registerOriginalExtractColumn("study_or_comparison")
	registerOriginalExtractColumn("ses")
	registerOriginalExtractColumn("inv_1")
	registerOriginalExtractColumn("total_pop")
	
	
	#Create and register all the columns that are involved with smoothing
	
	originalExtractTable$EXP_UNADJ=generateFakeSmoothedValues(numberOfRows, 1.111)
	
	#Relative Risk non adjusted
	originalExtractTable$RR_UNADJ=generateFakeSmoothedValues(numberOfRows, 1.222)

	#Lower 95 percent interval non adjusted
	originalExtractTable$RRL95_UNADJ=generateFakeSmoothedValues(numberOfRows, 1.223)
	originalExtractTable$RRU95_UNADJ=generateFakeSmoothedValues(numberOfRows, 1.224)

	#Rate non adjusted
	originalExtractTable$RATE_UNADJ=generateFakeSmoothedValues(numberOfRows, 1.225)

	#Lower 95% percent rate
	originalExtractTable$RATEL95_UNADJ=generateFakeSmoothedValues(numberOfRows, 1.226)

	#Upper 95% percent rate
	originalExtractTable$RATEU95_UNADJ=generateFakeSmoothedValues(numberOfRows, 1.227)
	
	#Relative risk with Empirical bayesian estimates non adjusted
	originalExtractTable$SMRR_UNADJ=generateFakeSmoothedValues(numberOfRows, 1.228)

	###ADJUSTED
	#Expected number of cases adjusted
	originalExtractTable$EXP_ADJ=generateFakeSmoothedValues(numberOfRows, 1.229)

	#Relative Risk adjusted
	originalExtractTable$RR_ADJ=generateFakeSmoothedValues(numberOfRows, 1.300)

	#Lower 95 percent interval adjusted
	originalExtractTable$RRL95_ADJ=generateFakeSmoothedValues(numberOfRows, 1.301)

	#Upper 95 percent interval adjusted
	originalExtractTable$RRU95_ADJ=generateFakeSmoothedValues(numberOfRows, 1.302)

	#Rate adjusted
	originalExtractTable$RATE_ADJ=generateFakeSmoothedValues(numberOfRows, 1.303)

	#Lower 95% percent rate adjusted
	originalExtractTable$RATEL95_ADJ=generateFakeSmoothedValues(numberOfRows, 1.304)

	#Upper 95% percent rate adjusted
	originalExtractTable$RATEU95_ADJ=generateFakeSmoothedValues(numberOfRows, 1.305)

	#Relative risk with Empirical bayesian estimates adjusted
	originalExtractTable$SMRR_ADJ=generateFakeSmoothedValues(numberOfRows, 1.306)

	##registerSmoothedColumn("EXP_UNADJ")
	registerSmoothedColumn("RR_UNADJ")
	registerSmoothedColumn("RRL95_UNADJ")
	registerSmoothedColumn("RRU95_UNADJ")
	registerSmoothedColumn("RATE_UNADJ")
	registerSmoothedColumn("RATEL95_UNADJ")
	registerSmoothedColumn("RATEU95_UNADJ")
	registerSmoothedColumn("SMRR_UNADJ")
	registerSmoothedColumn("EXP_ADJ")
	registerSmoothedColumn("RR_ADJ")
	registerSmoothedColumn("RRL95_ADJ")
	registerSmoothedColumn("RRU95_ADJ")
	registerSmoothedColumn("RATE_ADJ")
	registerSmoothedColumn("RATEL95_ADJ")
	registerSmoothedColumn("RATEU95_ADJ")
	registerSmoothedColumn("SMRR_ADJ")

	if (useAdjustedSmoothing==FALSE) {

		if (includeBYMColumns==TRUE) {
			originalExtractTable$BYM_ssRR_UNADJ=generateFakeSmoothedValues(numberOfRows, 2.100)			
			originalExtractTable$BYM_ssRRL95_UNADJ=generateFakeSmoothedValues(numberOfRows, 2.200)
			originalExtractTable$BYM_ssRRU95_UNADJ=generateFakeSmoothedValues(numberOfRows, 2.300)
      
			originalExtractTable$BYM_RR_UNADJ=generateFakeSmoothedValues(numberOfRows, 2.400)
 			originalExtractTable$BYM_RRL95_UNADJ=generateFakeSmoothedValues(numberOfRows, 2.500)
 			originalExtractTable$BYM_RRU95_UNADJ=generateFakeSmoothedValues(numberOfRows, 2.600)
 			
 			registerSmoothedColumn("BYM_ssRR_UNADJ")
			registerSmoothedColumn("BYM_ssRRL95_UNADJ")
			registerSmoothedColumn("BYM_ssRRU95_UNADJ")
			registerSmoothedColumn("BYM_RR_UNADJ")
			registerSmoothedColumn("BYM_RRL95_UNADJ")
			registerSmoothedColumn("BYM_RRU95_UNADJ")
 		}
	
		if (includeCARColumns==TRUE) {
      		originalExtractTable$CAR_RR_UNADJ=generateFakeSmoothedValues(numberOfRows, 2.700)
      		originalExtractTable$CAR_RRL95_UNADJ=generateFakeSmoothedValues(numberOfRows, 2.800)
      		originalExtractTable$CAR_RRU95_UNADJ=generateFakeSmoothedValues(numberOfRows, 2.900) 
      		
      		registerSmoothedColumn("CAR_RR_UNADJ")
       		registerSmoothedColumn("CAR_RRL95_UNADJ")
       		registerSmoothedColumn("CAR_RRU95_UNADJ")
		}
		
		if (includeHETColumns==TRUE) {
      		originalExtractTable$HET_RR_UNADJ=generateFakeSmoothedValues(numberOfRows, 3.000) 
      		originalExtractTable$HET_RRL95_UNADJ=generateFakeSmoothedValues(numberOfRows, 3.100) 
      		originalExtractTable$HET_RRU95_UNADJ=generateFakeSmoothedValues(numberOfRows, 3.200) 

      		registerSmoothedColumn("HET_RR_UNADJ")
      		registerSmoothedColumn("HET_RRL95_UNADJ")
      		registerSmoothedColumn("HET_RRU95_UNADJ")
		}
	}else {

		if (includeBYMColumns==TRUE) {
			originalExtractTable$BYM_ssRR_ADJ=generateFakeSmoothedValues(numberOfRows, 4.000)
			originalExtractTable$BYM_ssRRL95_ADJ=generateFakeSmoothedValues(numberOfRows, 4.100)
			originalExtractTable$BYM_ssRRU95_ADJ=generateFakeSmoothedValues(numberOfRows, 4.200)
      
			originalExtractTable$BYM_RR_ADJ=generateFakeSmoothedValues(numberOfRows, 4.300)
 			originalExtractTable$BYM_RRL95_ADJ=generateFakeSmoothedValues(numberOfRows, 4.400)
 			originalExtractTable$BYM_RRU95_ADJ=generateFakeSmoothedValues(numberOfRows, 4.500)

      		registerSmoothedColumn("BYM_ssRR_ADJ")
      		registerSmoothedColumn("BYM_ssRRL95_ADJ")
      		registerSmoothedColumn("BYM_ssRRU95_ADJ")
      		registerSmoothedColumn("BYM_RR_ADJ")
      		registerSmoothedColumn("BYM_RRL95_ADJ")
      		registerSmoothedColumn("BYM_RRU95_ADJ")
		}
	
		if (includeCARColumns==TRUE) {
      		originalExtractTable$CAR_RR_ADJ=generateFakeSmoothedValues(numberOfRows, 4.600)
      		originalExtractTable$CAR_RRL95_ADJ=generateFakeSmoothedValues(numberOfRows, 4.700)
      		originalExtractTable$CAR_RRU95_ADJ=generateFakeSmoothedValues(numberOfRows, 4.800) 
      		
      		registerSmoothedColumn("CAR_RR_ADJ")
      		registerSmoothedColumn("CAR_RRL95_ADJ")
      		registerSmoothedColumn("CAR_RRU95_ADJ")
		}
		
		if (includeHETColumns==TRUE) {
      		originalExtractTable$HET_RR_ADJ=generateFakeSmoothedValues(numberOfRows, 5.000) 
      		originalExtractTable$HET_RRL95_ADJ=generateFakeSmoothedValues(numberOfRows, 5.100) 
      		originalExtractTable$HET_RRU95_ADJ=generateFakeSmoothedValues(numberOfRows, 5.200) 
      		
      		registerSmoothedColumn("HET_RR_ADJ")
      		registerSmoothedColumn("HET_RRL95_ADJ")
      		registerSmoothedColumn("HET_RRU95_ADJ")      		
		}
	}
	
	##No drop if exists
	##sqlDrop(connDB, smoothingExtractTableName, errors=TRUE)
	
	
	for (i in 1:length(smoothedColumns)) {
		print(smoothedColumns[i]);
	}	
	
	sqlSave(connDB, originalExtractTable, tablename=mapTableName)
	
}

generateSQLUpdateStatement <- function() {

	#Step 1: Create new columns in the extract table
	#Example:
	# ALTER TABLE rif_studies.s6_extract ADD RR_ADJ;
	print("=================BEGIN ADD COLUMNS=============")
	for (i in 1:length(smoothedColumns)) {
		addColumnToExtractQuery <- paste0("ALTER TABLE ",
										  extractTableName,
									      " ADD ",
									      smoothedColumns[i],
									      " DOUBLE PRECISION")
		print(addColumnToExtractQuery)
		sqlQuery(connDB, addColumnToExtractQuery, errors = FALSE)
	}
	print("=================END ADD COLUMNS===============")


	#Step 2: Update new columns using values from the 
	#smoothed table.
	#Example:
	#		
	setValueConditions <- "";
	for (i in 1:length(smoothedColumns)) {
		if (i == 1) {
			setValueConditions <- paste0(setValueConditions,
										smoothedColumns[i],
										"=b.",
										smoothedColumns[i]);
		}
		else {
			setValueConditions <- paste0(setValueConditions,
										",",
										smoothedColumns[i],
										"=b.",
										smoothedColumns[i],
										sep="");
		}
	}							
	
	whereValueConditions <- "";
	for (i in 1:length(linkingColumns)) {
		if (i != 1) {
			whereValueConditions <- paste0(" AND ", whereValueConditions)
		}
		whereValueConditions <- paste0("a.",
								linkingColumns[i],
								"=b.",
								linkingColumns[i],
								whereValueConditions);
	}
		
	updateSQLQuery <- paste0("UPDATE ",
							extractTableName,
							" a",
							" SET ",
							setValueConditions,
							" FROM ",
							smoothingExtractTableName,
							" b WHERE ",
							whereValueConditions);
							
	print("===================BEGIN UPDATE=========================")
	print(updateSQLQuery)
	print("===================END UPDATE===========================")
	
	sqlQuery(connDB, updateSQLQuery)
	
	#Step 3: Add indexes to smoothed columns
	#Example:
	#CREATE INDEX ind_RR_ADJ ON rif_studies.s6_extract(RR_ADJ)
	print("===================BEGIN ADD INDEX===================")
	for (i in 1:length(smoothedColumns)) {
		addIndexToSmoothedColumn <- paste0("CREATE INDEX ",
		                                  "ind_",
									      smoothedColumns[i],
									      " ON ",
										  extractTableName,
										  "(",
									      smoothedColumns[i],
										  ")")
		print(addIndexToSmoothedColumn)
		sqlQuery(connDB, addIndexToSmoothedColumn)
	}
	print("===================END ADD INDEX=====================")
	
	sqlQuery(connDB, "COMMIT")
	

}

##===========================================================================
##MAIN PROGRAM
##===========================================================================


dbConnectionString <- createDatabaseConnectionString()
#print( paste("command line==", dbConnectionString, "==", sep=""))
#print( paste("temp smoothed table name==", createTemporarySmoothedTableName(studyID), "==", sep=""))
#print( paste("extract table name==", createExtractTableName(studyID), "==", sep=""))
#print( paste("map table name==", createMapTableName(studyID), "==", sep=""))

processCommandLineArguments()
establishTableNames(studyID)

connDB = odbcConnect("PostgreSQL30", uid="kgarwood", pwd="kgarwood")
createSmoothedExtractResults()

##sqlQuery(connDB, "ALTER TABLE rif_studies.s9_extract ADD blah DOUBLE PRECISION")

##generateSQLUpdateStatement()

odbcClose(connDB)



















##original_extract_rows<-sqlQuery(connDB, paste("CREATE TABLE rif_studies.s9_adj AS SELECT year FROM rif_studies.s9_extract")
##original_extract_rows<-sqlQuery(connDB, paste("SELECT * FROM rif_studies.s9_extract"))
##print(AdjRowset)






##colnames(originalExtractTable)


##fileConn <- file("C:/rif_test/stuff.txt")
##writeLines(c("Hello", "World"), fileConn)

#writeLines(commandLineArgsList, fileConn)
##close(fileConn)
