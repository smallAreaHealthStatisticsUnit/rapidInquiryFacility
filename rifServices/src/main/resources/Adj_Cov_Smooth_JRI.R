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
############################################################################################################

## CHECK & AUTO INSTALL MISSING PACKAGES
## CHECK .libPaths(), add lib="" argument and RUN AS ADMIN IF NEEDED
#packages <- c("plyr", "abind", "maptools", "spdep", "RODBC", "rJava")
#if (length(setdiff(packages, rownames(installed.packages()))) > 0) {
#  install.packages(setdiff(packages, rownames(installed.packages())))  
#}
#if (!require(INLA)) {
#	install.packages("INLA", repos="https://www.math.ntnu.no/inla/R/stable")
#}

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
				
#Adjust for other covariate(s) or not
#Reformat Java type > R
adj <- FALSE #either true or false
if (adj.1 == "TRUE") {
  adj <- TRUE
}

# Ignore CONTROL-C interrupts
#signal("SIGINT","ignore") # DOES NOT WORK!

#name of adjustment (covariate) variable (except age group and sex). 
#todo add more adjustment variables and test the capabilities. 
#Reformat Java type > R
if (names.adj.1 == "NONE") {
  names.adj<-c('none')
} else {
  names.adj<-c(names.adj.1)
}
#
#catalina_home<-Sys.getenv(c("R_HOME"))
#RIF_odbc<-paste0(catalina_home.1, "\webapps\rifServices\WEB-INF\classes\RIF_odbc.R")
#print(paste("Source: ", RIF_odbc))
#source(RIF_odbc)
#

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
  
  #The name of the skeleton table created by the RIF middleware to hold smoothed results
  #for a given study.  Initially the values for smoothed columns will be empty.  It is 
  #of the format rif_studies.s[study_id]_map 
  mapTableName <<- paste0("rif_studies.s", vstudyID, "_map")

  # Name of Rdata CSV file for debugging results save
  # This needs to be passed in via interface
  if (exists("scratchSpace") == FALSE  || scratchSpace == "") {
	  scratchSpace <<- "c:\\rifDemo\\scratchSpace\\"
  }
  if (exists("dumpFramesToCsv") == FALSE || dumpFramesToCsv == "") {
	  dumpFramesToCsv <<- TRUE
  }
  temporarySmoothedResultsFileName <<-paste(scratchSpace, "tmp_s", vstudyID, "_map.csv", sep="")
  temporaryExtractFileName <<-paste(scratchSpace, "tmp_s", vstudyID, "_extract.csv", sep="")
  adjacencyMatrixFileName <<-paste(scratchSpace, "tmp_s", vstudyID, "_adjacency_matrix.csv", sep="")
  
  #The name of the temporary table that this script uses to hold the data frame
  #containing smoothed results.  It should have a 1:1 correspondence between
  #its fields and fields that appear in the map table skeleton.
  
  # temporarySmoothedResultsTableName does NOT support SQL Server temporary tables
#  if (db_driver_prefix == "jdbc:sqlserver") {
#		temporarySmoothedResultsTableName <<-paste("#tmp_s", vstudyID, "_map", sep="")
# }
#  else {
		temporarySmoothedResultsTableName <<-paste(userID, ".tmp_s", vstudyID, "_map", sep="")
#  }
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
	
	print(paste0("check.integer: ", str,
		"; as.numeric(str): ", suppressWarnings(as.numeric(str)),
		"; isNumeric: ", isNumeric,
		"; isInteger: ", isInteger,
		"; isNotRounded: ", isNotRounded,
		"; isIntRegexp: ", isIntRegexp,
		"; check.integer.Result: ", check.integer.Result))
	
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
        print(paste0("Stack tracer: ", trace[length(trace):1]))
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
	connDB=dbConnect()

	if (exitValue == 0) {  
		errorTrace<-capture.output({
			# tryCatch()is trouble because it replaces the stack! it also copies all global variables!
					
			tryCatch({
					withErrorTracing({  				
						print("Performing basic stats and smoothing")	
#
# extract the relevant Study data
#
#data=read.table('sahsuland_example_extract.csv',header=TRUE,sep=',')
						data=fetchExtractTable()

#
# Get Adjacency matrix
#  	
						AdjRowset=getAdjacencyMatrix()

#
# Call: performSmoothingActivity()
#						
						result <- performSmoothingActivity(data, AdjRowset)
					})
				},
				warning=function(w) {		
					print(paste("callPerformSmoothingActivity() WARNING: ", w))
				},
				error=function(e) {
					e <<- e
					print(paste("callPerformSmoothingActivity() ERROR: ", e$message, 
						"; call stack: ", e$call))
					exitValue <<- 1
				},
				finally={
					print(paste0("callPerformSmoothingActivity exitValue: ", exitValue))
					if (exitValue == 0) {
						print(paste("performSmoothingActivity() OK: ", exitValue))
							
						# Cast area_id to char. This is ignored by sqlSave!
						area_id_is_integer <- FALSE
						print(paste("typeof(result$area_id[1]) ----> ", typeof(result$area_id[1]), 
							"; check.integer(result$area_id[1]): ", check.integer(result$area_id[1]),
							"; result$area_id[1]: ", result$area_id[1]))
						 
						# Use check.integer() to reliably test if the string is an integer 
						if (check.integer(result$area_id[1])) {
							area_id_is_integer <- TRUE
							result$area_id <- sapply(result$area_id, as.character)
							print(paste("AFTER CAST typeof(result$area_id[1]) ----> ", typeof(result$area_id[1]),
								"; result$area_id[1]: ", result$area_id[1]))
						}

						saveDataFrameToDatabaseTable(result)
						updateMapTableFromSmoothedResultsTable(area_id_is_integer) # may set exitValue  
					}
				}
			) # End of tryCatch
		})
	}

	# Print trace
	if (length(errorTrace)-1 > 0) {
		print(paste(errorTrace, sep="\n"))
	}
	
	if (exitValue == 0 && !is.na(connDB)) {
		print(paste0("Dropping temporary table: ", temporarySmoothedResultsTableName))
		sqlDrop(connDB, temporarySmoothedResultsTableName)
	}
	# Dummy change to check conflict is resolved
  
	if (!is.na(connDB)) {
		dbDisConnect()
	}
	print(paste0("Adj_Cov_Smooth_JRI.R exitValue: ", exitValue, "; error tracer: ", length(errorTrace)-1))

	return(list(exitValue=exitValue, errorTrace=errorTrace))
}