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
#   JRI version for calling from Middleware
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
#CATALINA_HOME
catalina_home<-Sys.getenv("CATALINA_HOME")

# Set the working directory based on the value from the Java class
setwd(working_dir)

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
		scratchSpace <<- file.path("scratchSpace")
	}
	# Typically: c:\rifDemo\scratchSpace\d1201-1300\s1273\data
	scratchSpace <<- file.path(scratchSpace, numberDir, paste0("s", vstudyID), "data")
		
	tryCatch({
			#Put all scratch files in sub directory s<study_id>
			if (!file.exists(scratchSpace)) {
				dir.create(scratchSpace, recursive=TRUE)
			}
	  	},
		warning=function(w) {
			cat(paste("UNABLE to create scratchSpace: ", scratchSpace, w, "\n"), sep="")
#			exitValue <<- 1
		},
		error=function(e) {
			cat(paste("ERROR creating scratchSpace: ", scratchSpace, e, "\n"), sep="")
			exitValue <<- 1
		}) # End of tryCatch

	if (exists("dumpFramesToCsv") == FALSE || dumpFramesToCsv == "") {
		dumpFramesToCsv <<- TRUE
	}		
	temporarySmoothedResultsFileName <<-file.path(scratchSpace, paste0("tmp_s", vstudyID, "_map.csv"))
	temporaryExtractFileName <<-file.path(scratchSpace, paste0("tmp_s", vstudyID, "_extract.csv"))
	adjacencyMatrixFileName <<-file.path(scratchSpace, paste0("tmp_s", vstudyID, "_adjacency_matrix.csv"))
  
#The name of the temporary table that this script uses to hold the data frame
#containing smoothed results.  It should have a 1:1 correspondence between
#its fields and fields that appear in the map table skeleton.
  
	temporarySmoothedResultsTableName <<-paste(userID, ".tmp_s", vstudyID, "_map", sep="")
	
#
# Install scripts required to re-run study
#
	if (exists("catalina_home")) {
		performSmoothingActivityScriptA<-file.path(catalina_home, "webapps", "rifServices", "WEB-INF", "classes", "performSmoothingActivity.R") # Source
		performSmoothingActivityScriptB<-file.path(scratchSpace, "performSmoothingActivity.R") # Target
		if (!file.exists(performSmoothingActivityScriptB)) {
			cat(paste("Copy: ", performSmoothingActivityScriptA, " to: ", performSmoothingActivityScriptB, "\n"), sep="")	

			tryCatch({			
				file.copy(performSmoothingActivityScriptA, performSmoothingActivityScriptB)
			},
			warning=function(w) {
				cat(paste("UNABLE to copy: ", performSmoothingActivityScriptA, " to: ", performSmoothingActivityScriptB, w, "\n"), sep="")
				exitValue <<- 0
			},
			error=function(e) {
				cat(paste("ERROR copying: ", performSmoothingActivityScriptA, " to: ", performSmoothingActivityScriptB, e, "\n"), sep="")
				exitValue <<- 1
			}) # End of tryCatch
		}
		else {
			cat(paste("WARNING! No need to copy: ", performSmoothingActivityScriptA, " to: ", performSmoothingActivityScriptB, "\n"), sep="")
		}
		
		Adj_Cov_Smooth_csvA<-file.path(catalina_home, "webapps", "rifServices", "WEB-INF", "classes", "Adj_Cov_Smooth_csv.R") # Source
		Adj_Cov_Smooth_csvB<-file.path(scratchSpace, "Adj_Cov_Smooth_csv.R") # Target
		if (!file.exists(Adj_Cov_Smooth_csvB)) {
			cat(paste("Copy: ", Adj_Cov_Smooth_csvA, " to: ", Adj_Cov_Smooth_csvB, "\n"), sep="")	

			tryCatch({			
				file.copy(Adj_Cov_Smooth_csvA, Adj_Cov_Smooth_csvB)
			},
			warning=function(w) {
				cat(paste("UNABLE to copy: ", Adj_Cov_Smooth_csvA, " to: ", Adj_Cov_Smooth_csvB, w, "\n"), sep="")
				exitValue <<- 0
			},
			error=function(e) {
				cat(paste("ERROR copying: ", Adj_Cov_Smooth_csvA, " to: ", Adj_Cov_Smooth_csvB, e, "\n"), sep="")
				exitValue <<- 1
			}) # End of tryCatch
		}
		else {
			cat(paste("WARNING! No need to copy: ", Adj_Cov_Smooth_csvA, " to: ", Adj_Cov_Smooth_csvB, "\n"), sep="")
		}
				
		rif40_run_RA<-file.path(catalina_home, "webapps", "rifServices", "WEB-INF", "classes", "rif40_run_R.bat") # Source
		rif40_run_RB<-file.path(scratchSpace, "rif40_run_R.bat") # Target
		if (!file.exists(rif40_run_RB)) {
			cat(paste("Copy: ", rif40_run_RA, " to: ", rif40_run_RB, "\n"), sep="")	

			tryCatch({			
				file.copy(rif40_run_RA, rif40_run_RB)
			},
			warning=function(w) {
				cat(paste("UNABLE to copy: ", rif40_run_RA, " to: ", rif40_run_RB, w, "\n"), sep="")
				exitValue <<- 0
			},
			error=function(e) {
				cat(paste("ERROR copying: ", rif40_run_RA, " to: ", rif40_run_RB, e, "\n"), sep="")
				exitValue <<- 1
			}) # End of tryCatch
		}
		else {
			cat(paste("WARNING! No need to copy: ", rif40_run_RA, " to: ", rif40_run_RB, "\n"), sep="")
		}
		
#
# Create rif40_run_R_env.bat
#
		rif40_run_R_env=paste(
					paste0("SET USERID=", userID),
					paste0("SET DB_NAME=", db_name),
					paste0("SET DB_HOST=", db_host),
					paste0("SET DB_PORT=", db_port),
					paste0("SET DB_DRIVER_PREFIX=", db_driver_prefix),
					paste0("SET DB_DRIVER_CLASS_NAME=", db_driver_class_name),
					paste0("SET STUDYID=", studyID),
					paste0("SET INVESTIGATIONNAME=", investigationName),
					paste0("SET STUDYNAME=", studyName),
					paste0("SET INVESTIGATIONID=", investigationId),
					paste0("SET ODBCDATASOURCE=", odbcDataSource),
					paste0("SET MODEL=", model),
					paste0("SET COVARIATENAME=", paste0(names.adj)),
				sep="\n");
		
		rif40_run_R_envB<-file.path(scratchSpace, "rif40_run_R_env.bat") # Target
		
		cat(paste("Create: ", rif40_run_R_envB, "\n"), sep="")	
		tryCatch({			
			cat(rif40_run_R_env, file=rif40_run_R_envB)
		},
		warning=function(w) {
			cat(paste("UNABLE to create: ", rif40_run_RB, w, "\n"), sep="")
			exitValue <<- 0
		},
		error=function(e) {
			cat(paste("ERROR creating: ", rif40_run_RB, e, "\n"), sep="")
			exitValue <<- 1
		}) # End of tryCatch
	}
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
	
#	cat(paste0("check.integer: ", str,
#		"; as.numeric(str): ", suppressWarnings(as.numeric(str)),
#		"; isNumeric: ", isNumeric,
#		"; isInteger: ", isInteger,
#		"; isNotRounded: ", isNotRounded,
#		"; isIntRegexp: ", isIntRegexp,
#		"; check.integer.Result: ", check.integer.Result, "\n"), sep="")
	
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
	errorTrace<-capture.output({
		tryCatch({
			connDB=dbConnect()
		},
		warning=function(w) {		
			cat(paste("dbConnect() WARNING: ", w, "\n"), sep="")
			exitValue <<- 1
		},
		error=function(e) {
			e <<- e
			cat(paste("dbConnect() ERROR: ", e$message, 
				"; call stack: ", e$call, "\n"), sep="")
			exitValue <<- 1
		},
		finally={
			cat(paste0("dbConnect exitValue: ", exitValue, "\n"), sep="")
		})
	})
		

	if (exitValue == 0 && !is.na(connDB)) {  callPerformSmoothingActivity
		cat("Performing basic stats and smoothing\n")	
		errorTrace<-capture.output({
			# tryCatch()is trouble because it replaces the stack! it also copies all global variables!
					
			tryCatch({
					withErrorTracing({  				
#
# extract the relevant Study data
#
#data=read.table('sahsuland_example_extract.csv',header=TRUE,sep=',')
						# data=fetchExtractTable()

#
# Get Adjacency matrix
#  	
# 						AdjRowset=getAdjacencyMatrix()
						AdjRowset = read.csv(adjacencyMatrixFileName)
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

							saveDataFrameToDatabaseTable(result)
							updateMapTableFromSmoothedResultsTable(area_id_is_integer) # may set exitValue  
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
	else {
		cat("Could not connect to database\n")	
		return(list(exitValue=exitValue, errorTrace=errorTrace))
	}

	# Print trace
	if (length(errorTrace)-1 > 0) {
		cat(errorTrace, sep="\n")
	}
	
	if (exitValue == 0 && !is.na(connDB)) {
		dropTemporaryTable()
	}
	# Dummy change to check conflict is resolved
  
	if (!is.na(connDB)) {
		dbDisConnect()
	}
	

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
	rm(list=c("result", "AdjRowset", "area_id_is_integer", "data", "connDB")) 
	gc(verbose=true)
	cat(paste0("Free ", ototal , " memory; total memory is use: ", format(mem_used()), "\nMemory by object:\n"), sep="")
	rm(list=c("osize", "ototal")) 
	for (oname in ls()) {
		if (oname != "oname") {
			cat(oname, ": ", format(object_size(get(oname))), "\n", sep="")
		}
	}	
	cat(paste0("Adj_Cov_Smooth_JRI.R exitValue: ", exitValue, "; error tracer: ", length(errorTrace)-1, "\n"), sep="")

	return(list(exitValue=exitValue, errorTrace=errorTrace))
}
