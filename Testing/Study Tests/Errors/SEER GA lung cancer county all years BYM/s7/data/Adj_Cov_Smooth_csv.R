####
## The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
## that rapidly addresses epidemiological and public health questions using 
## routinely collected health and population data and generates standardised 
## rates and relative risks for any given health outcome, for specified age 
## and year ranges, for any given geographical area.
##
## Copyright 2016 Imperial College London, developed by the Small Area
## Health Statistics Unit. The work of the Small Area Health Statistics Unit 
## is funded by the Public Health England as part of the MRC-PHE Centre for 
## Environment and Health. Funding for this project has also been received 
## from the United States Centers for Disease Control and Prevention.  
##
## This file is part of the Rapid Inquiry Facility (RIF) project.
## RIF is free software: you can redistribute it and/or modify
## it under the terms of the GNU Lesser General Public License as published by
## the Free Software Foundation, either version 3 of the License, or
## (at your option) any later version.
##
## RIF is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
## GNU Lesser General Public License for more details.
##
## You should have received a copy of the GNU Lesser General Public License
## along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
## to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
## Boston, MA 02110-1301 USA

## Brandon Parkes
## @author bparkes
##

############################################################################################################
#   RIF PROJECT
#   Basic disease map results
#   Adjusted for covariates
#   Apply Bayesian smoothing with INLA
#
#   CSV file driven version for trace
#
############################################################################################################

## CHECK & AUTO INSTALL MISSING PACKAGES
## CHECK .libPaths(), add lib="" argument and RUN AS ADMIN IF NEEDED
#packages <- c("pryr", "plyr", "abind", "maptools", "spdep", "RODBC", "rJava")
#if (length(setdiff(packages, rownames(installed.packages()))) > 0) {
#  install.packages(setdiff(packages, rownames(installed.packages())))  
#}
#if (!require(INLA)) {
#	install.packages("INLA", repos="https://www.math.ntnu.no/inla/R/stable")
#}

library(pryr)
library(plyr)
library(abind)
library(INLA)
library(maptools)
library(spdep)
library(RODBC)
library(Matrix)


##====================================================================
# SCRIPT VARIABLES
##====================================================================

connDB <- ""	# Database connection
exitValue <- 0 	# 0 success, 1 failure
errorCount <- 0	# Smoothing error count
				
#CATALINA_HOME
catalina_home<-Sys.getenv("CATALINA_HOME")

# Set the working directory based on the value from the Java class
setwd(working_dir)

##====================================================================
# SCRIPT VARIABLES
##====================================================================
#Variables that hold database connectivity information.  For now, we
#will rely on named ODBC sources but in future the script should be 
#altered to use host:port/databaseName
userID <- ""
password <- ""
dbName <- "rif_studies"
dbHost <- ""
dbPort <- ""
db_driver_prefix <- ""
db_driver_class_name <- ""
dbConnectionString <- ""
odbcDataSource <- "networkRif"
numberOfInvestigations <- ""

#The identifier of the study whose extract table fields need to be smoothed.
studyID <- "1"

#We expect model to have one of the three values: 'BYM', 'CAR' or 'HET'
model <- "NONE"

#Adjust for other covariate(s) or not
adj=FALSE #either true or false

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

#File names for smoothed (temporarySmoothedResultsFileName) and extract data frames (temporaryExtractFileName)
#Variable to control dumping franes (dumpFramesToCsv)
defaultScratchSpace <- file.path("scratchSpace")
defaultDumpFramesToCsv <- FALSE
scratchSpace <- ""
dumpFramesToCsv <- ""
 
#The name of the investigation. Is an input parameter, but default is set here for debug purposes
investigationName <- "inv_1"
#The id of the investigation - used when writing the results back to the database. Input paremeter
investigationId <- "272"

#name of adjustment (covariate) variable (except age group and sex). 
#todo add more adjustment variables and test the capabilities. 
names.adj<-c('ses')

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
 
#The name of the extract table that is created by the middleware for a given
#study.  It is of the format rif_studies.s[study_id]_extract
	extractTableName <<- paste0("rif_studies.s", vstudyID, "_extract")
  	
#Numbered directory support (1-100 etc) to reduce the number of files/directories per directory to 100. This is to improve filesystem 
#performance on Windows Tomcat servers 
	centile <- as.integer(vstudyID) %/% 100 # 1273 = 12
	# Number directory: d1201-1300\
	numberDir <- paste0("d", (centile*100)+1, "-", (centile+1)*100)
	
#The name of the skeleton table created by the RIF middleware to hold smoothed results
#for a given study.  Initially the values for smoothed columns will be empty.  It is 
#of the format rif_studies.s[study_id]_map 
	mapTableName <<- paste0("rif_studies.s", vstudyID, "_map")

# Name of Rdata CSV file for debugging results save
# This needs to be passed in via interface

	if (exists("scratchSpace") == FALSE  || scratchSpace == "") {
	# Typically: c:\rifDemo\scratchSpace\d1201-1300\s1273\data
		scratchSpace <<- file.path(defaultScratchSpace, numberDir, paste0("s", vstudyID), "data")
	}

	if (exists("dumpFramesToCsv") == FALSE || dumpFramesToCsv == "") {
		dumpFramesToCsv <<- defaultDumpFramesToCsv
	}		
	temporarySmoothedResultsFileName <<-paste(scratchSpace, "tmp_s", vstudyID, "_map.csv", sep="")
	temporaryExtractFileName <<-paste(scratchSpace, "tmp_s", vstudyID, "_extract.csv", sep="")
	adjacencyMatrixFileName <<-paste(scratchSpace, "tmp_s", vstudyID, "_adjacency_matrix.csv", sep="")
  
#The name of the temporary table that this script uses to hold the data frame
#containing smoothed results.  It should have a 1:1 correspondence between
#its fields and fields that appear in the map table skeleton.
  
	temporarySmoothedResultsTableName <<-paste(userID, ".tmp_s", vstudyID, "_map", sep="")

}


#make and ODBC connection
#dbHost = 'networkRif'
#dbName = 'rif_studies'
#studyID = '1'

##================================================================================
##FUNCTION: check.integer
##DESCRIPTION
##Check if string is an integer 
##
## Inspiration:
##
## https://stackoverflow.com/questions/3476782/check-if-the-number-is-integer
## https://rosettacode.org/wiki/Determine_if_a_string_is_numeric#R
##
## Test cases: 
##
## isNotRounded is the best test; isInteger is useless (see manual for why!); isIntRegexp needs a better regexp! [string "1e4" give the wrong answer]
##> check.integer("1e4")
#[1] "check.integer: 1e4; as.numeric(str): 10000; isNumeric: TRUE; isInteger: TRUE; isNotRounded: TRUE; isIntRegexp: FALSE"
#[1] FALSE <<<< WRONG!
#
##
## Beware:
##
# isNotRounded will return false when used on a data frame: check.integer(result$area_id[1])
#> check.integer(result$area_id[1]) where the data is "01.001.01000"
#[1] "check.integer: 01.001.01000; as.numeric(str): 1; isNumeric: TRUE; isInteger: FALSE; isNotRounded: TRUE; isIntRegexp: FALSE"
#[1] FALSE
##
#> check.integer(1)
#[1] "check.integer: 1; as.numeric(str): 1; isNumeric: TRUE; isInteger: TRUE; isNotRounded: TRUE; isIntRegexp: TRUE"
#[1] TRUE
#> check.integer(1.1)
#[1] "check.integer: 1.1; as.numeric(str): 1.1; isNumeric: TRUE; isInteger: TRUE; isNotRounded: FALSE; isIntRegexp: FALSE"
#[1] FALSE
#> check.integer("1")
#[1] "check.integer: 1; as.numeric(str): 1; isNumeric: TRUE; isInteger: TRUE; isNotRounded: TRUE; isIntRegexp: TRUE"
#[1] TRUE
#> check.integer("1e4")
#[1] "check.integer: 1e4; as.numeric(str): 10000; isNumeric: TRUE; isInteger: TRUE; isNotRounded: TRUE; isIntRegexp: FALSE"
#[1] FALSE <<<< WRONG!
#> check.integer(1e4)
#[1] "check.integer: 10000; as.numeric(str): 10000; isNumeric: TRUE; isInteger: TRUE; isNotRounded: TRUE; isIntRegexp: TRUE"
#[1] TRUE
#> check.integer("Hello")
#[1] "check.integer: Hello; as.numeric(str): NA; isNumeric: FALSE; isInteger: FALSE; isNotRounded: FALSE; isIntRegexp: FALSE"
#[1] FALSE
#> check.integer("01.01.1000")
#[1] "check.integer: 01.01.1000; as.numeric(str): NA; isNumeric: FALSE; isInteger: FALSE; isNotRounded: FALSE; isIntRegexp: FALSE"
#[1] FALSE
#> check.integer("1.001")
#[1] "check.integer: 1.001; as.numeric(str): 1.001; isNumeric: TRUE; isInteger: TRUE; isNotRounded: FALSE; isIntRegexp: FALSE"
#[1] FALSE
#> check.integer("0011001")
#[1] "check.integer: 0011001; as.numeric(str): 11001; isNumeric: TRUE; isInteger: TRUE; isNotRounded: TRUE; isIntRegexp: TRUE"
#[1] TRUE
#> check.integer("00.11001")
#[1] "check.integer: 00.11001; as.numeric(str): 0.11001; isNumeric: TRUE; isInteger: FALSE; isNotRounded: FALSE; isIntRegexp: FALSE"
#[1] FALSE
##================================================================================
check.integer <- function(N) {
	str<-N # COPY
	isNumeric<-suppressWarnings(!is.na(as.numeric(str)))
	isInteger<-suppressWarnings(!is.na(as.integer(str)) && as.integer(str))
	isNotRounded<-suppressWarnings(isNumeric && as.numeric(str) == round(as.numeric(str)))
	isIntRegexp<-suppressWarnings(!grepl("[^[:digit:]]", format(N,  digits = 20, scientific = FALSE)))
	
	check.integer.Result<-(isNumeric && isInteger && isNotRounded && isIntRegexp)
	
	cat(paste0("check.integer: ", str,
		"; as.numeric(str): ", suppressWarnings(as.numeric(str)),
		"; isNumeric: ", isNumeric,
		"; isInteger: ", isInteger,
		"; isNotRounded: ", isNotRounded,
		"; isIntRegexp: ", isIntRegexp,
		"; check.integer.Result: ", check.integer.Result, "\n"), sep="")
	
    return(check.integer.Result)
}

# Error tracing function
# https://stackoverflow.com/questions/40629715/how-to-show-error-location-in-trycatch
withErrorTracing = function(expr, silentSuccess=FALSE) {
    hasFailed = FALSE
    messages = list()
    warnings = list()
		
    errorTracer = function(obj) {

        # Storing the call stack 
        calls = sys.calls()
        calls = calls[1:length(calls)-1]
        # Keeping the calls only
        trace = limitedLabels(c(calls, attr(obj, "calls")))

        # Printing the 2nd and 3rd traces that contain the line where the error occured
        # This is the part you might want to edit to suit your needs
        #print(paste0("Error occuring: ", trace[length(trace):1][2:3]))
        cat("Stack tracer >>>\n\n", trace[length(trace):1], "\n<<< End of stack tracer.\n")
        # Muffle any redundant output of the same message
        optionalRestart = function(r) { res = findRestart(r); if (!is.null(res)) invokeRestart(res) }
        optionalRestart("muffleMessage")
        optionalRestart("muffleWarning")
    }

    vexpr = withCallingHandlers(withVisible(expr),  error=errorTracer)
    if (silentSuccess && !hasFailed) {
        cat(paste(warnings, collapse=""))
    }
    if (vexpr$visible) vexpr$value else invisible(vexpr$value)
	return
}

##================================================================================
##FUNCTION: runRSmoothingFunctions
##DESCRIPTION
##Run the functions defined this script as source
##Called direectly from JRI in the middleware
##Returns (exitvalue) 0 on success, 1 on failure 
##================================================================================
runRSmoothingFunctions <- function() {
	establishTableNames(studyID)
#	connDB=dbConnect()

	if (exitValue == 0) {  
		cat("Performing basic stats and smoothing\n")	
		errorTrace<-capture.output({
			# tryCatch()is trouble because it replaces the stack! it also copies all global variables!
					
			tryCatch({
					withErrorTracing({  				
#
# extract the relevant Study data
#
						data=read.table(temporaryExtractFileName,header=TRUE,sep=',')
#						data=fetchExtractTable()

#
# Get Adjacency matrix
#  	
						AdjRowset=read.table(adjacencyMatrixFileName,header=TRUE,sep=',')
#						AdjRowset=getAdjacencyMatrix()

#
# Call: performSmoothingActivity()
#						
						result <- performSmoothingActivity(data, AdjRowset)
					})
				},
				warning=function(w) {		
					cat(paste("callPerformSmoothingActivity() WARNING: ", w, "\n"), sep="")
					exitValue <<- 1
				},
				error=function(e) {
					e <<- e
					cat(paste("callPerformSmoothingActivity() ERROR: ", e$message, 
						"; call stack: ", e$call, "\n"), sep="")
					exitValue <<- 1
				},
				finally={
					cat(paste0("callPerformSmoothingActivity exitValue: ", exitValue, "\n"), sep="")
					if (exitValue == 0) {
						cat(paste("performSmoothingActivity() OK: ", exitValue, "\n"), sep="")
							
						# Cast area_id to char. This is ignored by sqlSave!
						area_id_is_integer <- FALSE
						if ("area_id" %in% colnames(result)) {
							cat(paste("typeof(result$area_id[1]) ----> ", typeof(result$area_id[1]), 
								"; check.integer(result$area_id[1]): ", check.integer(result$area_id[1]),
								"; result$area_id[1]: ", result$area_id[1], "\n"), sep="")
							 
							# Use check.integer() to reliably test if the string is an integer 
							if (check.integer(result$area_id[1])) {
								area_id_is_integer <- TRUE
								result$area_id <- sapply(result$area_id, as.character)
								cat(paste("AFTER CAST typeof(result$area_id[1]) ----> ", typeof(result$area_id[1]),
									"; result$area_id[1]: ", result$area_id[1], "\n"), sep="")
							}

#							saveDataFrameToDatabaseTable(result)
#							updateMapTableFromSmoothedResultsTable(area_id_is_integer) # may set exitValue  
						}
						else {
							cat("ERROR! No result$area_id column found\n")
							exitValue <<- 1
						}
					}
				}
			) # End of tryCatch
		})
	}

	# Print trace
	if (length(errorTrace)-1 > 0) {
		cat(errorTrace, sep="\n")
	}
	
#	if (exitValue == 0 && !is.na(connDB)) {
#		dropTemporaryTable()
#	}
	# Dummy change to check conflict is resolved
  
#	if (!is.na(connDB)) {
#		dbDisConnect()
#	}

#
# Free up memory: required for JRI version as the server keeps running! 
#
	cat(paste0("Total memory is use: ", format(mem_used()), "\nMemory by object:\n"), sep="")
	ototal<-0
	for (oname in ls()) {
		osize<-as.integer(object_size(get(oname)))
		ototal<-ototal+osize
		cat(oname, ": ", format(osize), "\n", sep="")	
	}
	rm(list=c("result", "AdjRowset", "area_id_is_integer", "data")) 
	gc(verbose=true)
	cat(paste0("Free ", ototal , " memory; total memory is use: ", format(mem_used()), "\nMemory by object:\n"), sep="")
	rm(list=c("osize", "ototal")) 
	for (oname in ls()) {
		if (oname != "oname") {
			cat(oname, ": ", format(object_size(get(oname))), "\n", sep="")
		}
	}
	
	return(list(exitValue=exitValue, errorTrace=errorTrace))
}

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
    cat(numCommandLineArgs, " arguments were supplied", "\n")
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
    
    cat("Parsing parameters\n")
    for (i in 1:nrow(parametersDataFrame)) {
	
      if (grepl('userID', parametersDataFrame[i, 1]) == TRUE) {
        userID <<- parametersDataFrame[i, 2]
      } else if (grepl('password', parametersDataFrame[i, 1]) == TRUE){
		password <<- parametersDataFrame[i, 2]
		parametersDataFrame[i, 2] <- NA	 
      } else if (grepl('dbName', parametersDataFrame[i, 1]) == TRUE){
        dbName <<- parametersDataFrame[i, 2]
      } else if (grepl('dbHost', parametersDataFrame[i, 1]) == TRUE){
        dbHost <<- parametersDataFrame[i, 2]
      } else if (grepl('dbPort', parametersDataFrame[i, 1]) == TRUE){
        dbPort <<- parametersDataFrame[i, 2]
      } else if (grepl('db_driver_prefix', parametersDataFrame[i, 1]) == TRUE){
        db_driver_prefix <<- parametersDataFrame[i, 2]
      } else if (grepl('db_driver_class_name', parametersDataFrame[i, 1]) == TRUE){
        db_driver_class_name <<- parametersDataFrame[i, 2]	
      } else if (grepl('scratchspace', parametersDataFrame[i, 1]) == TRUE){
        scratchSpace <<- parametersDataFrame[i, 2]
      } else if (grepl('dumpframestocsv', parametersDataFrame[i, 1]) == TRUE){
		if (parametersDataFrame[i, 2] == "true" || parametersDataFrame[i, 2] == "TRUE") {
			dumpFramesToCsv <<- TRUE
		} else if (parametersDataFrame[i, 2] == "false" || parametersDataFrame[i, 2] == "FALSE") {
			dumpFramesToCsv <<- FALSE
		}
      } else if (grepl('studyID', parametersDataFrame[i, 1]) == TRUE){
        studyID <<- parametersDataFrame[i, 2]
      } else if (grepl('num_investigations', parametersDataFrame[i, 1]) == TRUE){
        numberOfInvestigations <<- parametersDataFrame[i, 2]
      } else if (grepl('investigationName', parametersDataFrame[i, 1]) == TRUE){
        investigationName <<- parametersDataFrame[i, 2]
      } else if (grepl('investigationId', parametersDataFrame[i, 1]) == TRUE){
        investigationId <<- parametersDataFrame[i, 2]
      } else if (grepl('odbcDataSource', parametersDataFrame[i, 1]) == TRUE){
        odbcDataSource <<- parametersDataFrame[i, 2]
      } else if (grepl('model', parametersDataFrame[i, 1]) == TRUE){
        model <<- parametersDataFrame[i, 2]
      } else if (grepl('covariateName', parametersDataFrame[i, 1]) == TRUE){
	    names.adj <<- c(toupper(parametersDataFrame[i, 2]))
        if (names.adj[1] != "NONE") {
           adj <<- TRUE
        } else {
		   adj <<- FALSE
		   names.adj<-c('none')
		}
      } else {
		cat(paste("WARNING! Unexpected paremeter: ",  parametersDataFrame[i, 1], "=",  parametersDataFrame[i, 2], "\n", sep=""))
	  }
    }

	# Set defaults
	if (scratchSpace == "") {
	  scratchSpace <<- defaultScratchSpace
	  newRow <- data.frame(name="scratchSpace", value=scratchSpace)
	  parametersDataFrame = rbind(parametersDataFrame, newRow)
	}
	if (dumpFramesToCsv == "") {
	  dumpFramesToCsv <<- defaultDumpFramesToCsv
	  newRow <- data.frame(name="dumpframestocsv", value=dumpFramesToCsv)
	  parametersDataFrame = rbind(parametersDataFrame, newRow)
	}
	
    return(parametersDataFrame)
  }
}

#
hasperformSmoothingActivityScript<-FALSE
if (exists("catalina_home")) {
	cat("CATALINA_HOME=", catalina_home, "\n", sep="")
	performSmoothingActivityScript<-file.path(catalina_home, "webapps", "rifServices", "WEB-INF", "classes", "performSmoothingActivity.R")
	
	if (file.exists(performSmoothingActivityScript)) {
		hasperformSmoothingActivityScript<-TRUE
		cat("Source: ", performSmoothingActivityScript, "\n", sep="")
		source(performSmoothingActivityScript)
	}	
} else {
	cat("CATALINA_HOME not set\n")
	if (!hasperformSmoothingActivityScript) {

		performSmoothingActivityScript<-"performSmoothingActivity.R"
	
		if (file.exists(performSmoothingActivityScript)) {
			hasperformSmoothingActivityScript<-TRUE
			cat("Source: ", performSmoothingActivityScript, "\n", sep="")
			source(performSmoothingActivityScript)
		}
	}
}

if (hasperformSmoothingActivityScript) {
#
	parametersDataFrame=processCommandLineArguments()
	print(parametersDataFrame)
	returnValues <- runRSmoothingFunctions()
} else {
	returnValues <- list(exitValue=1, errorTrace="Cannot find R scripts")
}

if (returnValues$exitValue == 0) {
	cat("R script ran OK\n")
	quit("no", 0, FALSE)
} else {
	cat("R script had error >>>\n", paste(returnValues$errorTrace, "\n"), "\n<<< End of error trace.\n", sep="")
	quit("no", 1, FALSE)
}
