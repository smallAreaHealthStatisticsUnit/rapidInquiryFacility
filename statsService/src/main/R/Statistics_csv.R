####
## Brandon Parkes
## @author bparkes
## @author Martin McCallion
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

library(RODBC)
library(pryr)
library(plyr)
library(abind)
library(INLA)
library(maptools)
library(spdep)
library(Matrix)
library(here)

source(here::here("Statistics_Common.R"))
source(here::here("performSmoothingActivity.R"))

##====================================================================
# SCRIPT VARIABLES
##====================================================================

connDB <- ""	# Database connection
exitValue <- 0 	# 0 success, 1 failure
errorCount <- 0	# Smoothing error count
				
#CATALINA_HOME
catalina_home <- Sys.getenv("CATALINA_HOME")

# Variables that hold database connectivity information.  For now, we
# will rely on named ODBC sources but in future the script should be
# altered to use host:port/databaseName
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
riskAnal <- "0"

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

# File names for smoothed (temporarySmoothedResultsFileName) and extract data frames (temporaryExtractFileName)
# Variable to control dumping franes (dumpFramesToCsv)
# Scratchspace in this version should be the current directory, as the assumption is the script
# will be run from there.
defaultDumpFramesToCsv <- FALSE
dumpFramesToCsv <- ""
 
#The name of the investigation. Is an input parameter, but default is set here for debug purposes
investigationName <- "inv_1"
studyName <- "UNKNOWN"
#The id of the investigation - used when writing the results back to the database. Input paremeter
investigationId <- "272"

#name of adjustment (covariate) variable (except age group and sex). 
#todo add more adjustment variables and test the capabilities. 
names.adj<-c('ses')

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
					# withErrorTracing({
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
					# })
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
#							updateMapTableFromSmoothedResultsTable(area_id_is_integer, studyType) # may set exitValue  
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


##================================================================================
##FUNCTION: runRRiskAnalFunctions
##DESCRIPTION
##Run the functions defined this script as source
##Called direectly from JRI in the middleware
##Returns (exitvalue) 0 on success, 1 on failure 
##================================================================================
runRRiskAnalFunctions <- function() {
  establishTableNames(studyID)
  #	connDB=dbConnect()
  
  if (exitValue == 0) {  
    cat("Performing basic stats and risk anal\n")	
    errorTrace<-capture.output({
      # tryCatch()is trouble because it replaces the stack! it also copies all global variables!
      
      tryCatch({
        # withErrorTracing({
          #
          # extract the relevant Study data
          #
          data=read.table(temporaryExtractFileName,header=TRUE,sep=',')
          #						data=fetchExtractTable()
          
          #
          # Call: performRiskAnal()
          #
          cat(paste0("About to calculate band data", "\n"))
          resultBands <- performBandAnal(data)
          
          #
          # Call: performHomogAnal()
          # This runs the test for homogeneity and linearity using the band results from the performBandAnal function 
          cat(paste0("About to run homogeneity tests", "\n"))
          resultHomog <- performHomogAnal(resultBands)
        # })
      },
      warning=function(w) {		
        cat(paste("callRiskAnal() WARNING: ", w, "\n"), sep="")
        exitValue <<- 1
      },
      error=function(e) {
        e <<- e
        cat(paste("callRiskAnal() ERROR: ", e$message, 
                  "; call stack: ", e$call, "\n"), sep="")
        exitValue <<- 1
      },
      finally={
        cat(paste0("callRiskAnal exitValue: ", exitValue, "\n"), sep="")
        if (exitValue == 0) {
          cat(paste("callRiskAnal() OK: ", exitValue, "\n"), sep="")
          
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
  rm(list=c("resultBands", "resultHomog", "data")) 
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
      } else if (grepl('studyName', parametersDataFrame[i, 1]) == TRUE){
        studyName <<- parametersDataFrame[i, 2]
      } else if (grepl('investigationId', parametersDataFrame[i, 1]) == TRUE){
        investigationId <<- parametersDataFrame[i, 2]
      } else if (grepl('odbcDataSource', parametersDataFrame[i, 1]) == TRUE){
        odbcDataSource <<- parametersDataFrame[i, 2]
      } else if (grepl('model', parametersDataFrame[i, 1]) == TRUE){
        model <<- parametersDataFrame[i, 2]
      } else if (grepl('riskAnal', parametersDataFrame[i, 1]) == TRUE){
        riskAnal <<- parametersDataFrame[i, 2]
      } else if (grepl('covariateName', parametersDataFrame[i, 1]) == TRUE){
	    names.adj <<- c(toupper(parametersDataFrame[i, 2]))
        if (names.adj[1] != "NONE") {
           adj <<- TRUE
        } else {
		   adj <<- FALSE
		   names.adj<-c('none')
		}
      } else {
		cat(paste("WARNING! Unexpected parameter: ",  parametersDataFrame[i, 1], "=",  parametersDataFrame[i, 2], "\n", sep=""))
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

# Smoothing
hasperformSmoothingActivityScript <- FALSE
performSmoothingActivityScript<-"performSmoothingActivity.R"
if (file.exists(performSmoothingActivityScript)) {
	hasperformSmoothingActivityScript<-TRUE
	cat("Source: ", performSmoothingActivityScript, "\n", sep="")
	source(performSmoothingActivityScript)
}

# Risk analysis
hasperformRiskAnalScript<-FALSE
performRiskAnalScript<-"performRiskAnal.R"
    
if (file.exists(performRiskAnalScript)) {
	hasperformRiskAnalScript<-TRUE
	cat("Source: ", performRiskAnalScript, "\n", sep="")
	source(performRiskAnalScript)
}

if (hasperformRiskAnalScript & hasperformSmoothingActivityScript) {

	parametersDataFrame <- processCommandLineArguments()
	print(parametersDataFrame)
	if (riskAnal == "0") {
		returnValues <- runRSmoothingFunctions()
	} else {
		returnValues <- runRRiskAnalFunctions()
  }
} else {
	returnValues <- list(exitValue=1, errorTrace="Cannot find R scripts")
}

if (!exists('returnValues') {
	cat("R script ran but did not return returnValues\n")
	quit("no", 1, FALSE)
}
else if (returnValues$exitValue == 0) {
	cat("R script ran OK\n")
	quit("no", 0, FALSE)
} else {
	cat("R script had error >>>\n", paste(returnValues$errorTrace, "\n"), "\n<<< End of error trace.\n", sep="")
	quit("no", 1, FALSE)
}
  
