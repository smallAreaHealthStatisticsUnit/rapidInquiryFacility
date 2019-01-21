## Brandon Parkes
## @author bparkes
##

############################################################################################################
#	 RIF PROJECT
#	 Basic disease map results
#	 Adjusted for covariates
#	 Apply Bayesian smoothing with INLA
#
#	 JRI version for calling from Middleware
#
############################################################################################################

library(pryr)
library(plyr)
library(abind)
library(INLA)
library(maptools)
library(spdep)
library(Matrix)

##====================================================================
# SCRIPT VARIABLES
##====================================================================

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

##================================================================================
##FUNCTION: runRSmoothingFunctions
##DESCRIPTION
##Run the functions defined this script as source
##Called direectly from JRI in the middleware
##Returns (exitvalue) 0 on success, 1 on failure 
##================================================================================
runRSmoothingFunctions <- function() {

	cat(paste("In runRSmoothingFunctions in JRI script", "\n"))

	establishTableNames(studyID)
	cat("Table names established\n")
	
	riskAnal <<- "0"
	#
	# Install scripts required to re-run study
	#
	createWindowsScript("performSmoothingActivity.R")

	errorTrace<-capture.output({
		tryCatch({
			connDB = connectToDb()
			cat(paste0("Connected to DB", "\n"))
		},
		warning=function(w) {
			cat(paste("connectToDb() WARNING: ", w, "\n"), sep="")
			exitValue <<- 0
		},
		error=function(e) {
			e <<- e
			cat(paste("connectToDb() ERROR: ", e$message,
				"; call stack: ", e$call, "\n"), sep="")
			exitValue <<- 1
		},
		finally={
			cat(paste0("connectToDb exitValue: ", exitValue, "\n"), sep="")
		})
	})

	cat(paste("About to test exitValue", exitValue, "and connection", "\n"))
	if (exitValue == 0) {
		cat("Performing basic stats and smoothing\n")
		statsOutput<<-capture.output({
			# tryCatch()is trouble because it replaces the stack! it also copies all global variables!

#			cat(paste0("About to fetch extract table outside of the try", "\n"))
#			data=fetchExtractTable()
			tryCatch({
					# withErrorTracing({
#
# extract the relevant Study data
#
#data=read.table('sahsuland_example_extract.csv',header=TRUE,sep=',')
						cat(paste0("About to fetch extract table", "\n"))
						data=fetchExtractTable()

#
# Get Adjacency matrix
#
						cat(paste0("About to get adjacency matrix", "\n"))
						AdjRowset=getAdjacencyMatrix()
#
# Call: performSmoothingActivity()
#
						cat(paste0("About to smooth", "\n"))
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

							cat("About to save to table\n")
							saveOutput <<- saveDataFrameToDatabaseTable(result)	# may set exitValue	
							if (!is.null(saveOutput) && length(saveOutput)-1 > 0) {
								cat(saveOutput, sep="\n")
							}
							if (exitValue == 0) {
								updateOutput <<- updateMapTableFromSmoothedResultsTable(area_id_is_integer, studyType) # may set exitValue
								if (!is.null(updateOutput) && length(updateOutput)-1 > 0) {
									cat(updateOutput, sep="\n")
								}
							}
						}
						else {
							cat("ERROR! No result$area_id column found\n")
							exitValue <<- 1
						}
					}
				}
			) # End of tryCatch
		})
		
		if (!is.null(statsOutput) && length(statsOutput)-1 > 0) {
#			cat(statsOutput, sep="\n")
			if (!is.null(errorTrace)) {
				cat(paste0("Statistics_JRI.R exitValue: ", exitValue, 
					"; append statsOutput tracer: ", length(statsOutput)-1, 
					"; to errorTrace tracer: ", length(errorTrace)-1, 
					"\n"), sep="")
				errorTrace=c(errorTrace, statsOutput)
			}
			else {
				cat(paste0("Statistics_JRI.R exitValue: ", exitValue, "; statsOutput tracer: ", length(statsOutput)-1, "\n"), sep="")
				errorTrace <<- statsOutput
			}
			cat(paste0("Statistics_JRI.R errorTrace tracer: ", length(errorTrace)-1, "\n"), sep="")
		}
	}
	else {
		cat("Could not connect to database\n")	
		cat(paste0("Statistics_JRI.R exitValue: ", exitValue, "; error tracer: ", length(errorTrace)-1, "\n"), sep="")
		return(list(exitValue=exitValue, errorTrace=errorTrace))
	}

	
	if (exitValue == 0 && !is.na(connDB)) {
		dropTemporaryTable()
	}
	# Dummy change to check conflict is resolved
	
	if (!is.na(connDB)) {
		disconnect()
	}

#
# Free up memory: required for JRI version as the server keeps running! 
#
	cat(paste0("Total memory in use: ", format(mem_used()), "\nMemory by object:\n"), sep="")
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
	
	# Print trace
	if (length(errorTrace)-1 > 0) {
		cat(paste0("\nStatistics_JRI.R errorTrace: >>>\n"), sep="")
	 	cat(errorTrace, sep="\n")
		cat(paste0("\n<<< End of Statistics_JRI.R errorTrace.\n\n"), sep="")
	}	
	cat(paste0("Statistics_JRI.R exitValue: ", exitValue, "; error tracer: ", length(errorTrace)-1, "\n"), sep="")

	return(list(exitValue=exitValue, errorTrace=errorTrace))
}

##================================================================================
##FUNCTION: runRRiskAnalFunctions
##DESCRIPTION
##Run the functions defined this script as source
##Called directly from JRI in the middleware
##Returns (exitvalue) 0 on success, 1 on failure 
##================================================================================
runRRiskAnalFunctions <- function() {
	
	cat(paste("In runRRiskAnalFunctions in JRI script", "\n"))

	establishTableNames(studyID)
	cat("Table names established\n")

	riskAnal <<- "1"
	#
	# Install scripts required to re-run study
	#
	createWindowsScript("performRiskAnal.R")
	
	errorTrace<-capture.output({
		tryCatch({
			connDB = connectToDb()
			cat(paste0("Connected to DB", "\n"))
		},
		warning=function(w) {
			cat(paste("connectToDb() WARNING: ", w, "\n"), sep="")
			exitValue <<- 0
		},
		error=function(e) {
			e <<- e
			cat(paste("connectToDb() ERROR: ", e$message,
								"; call stack: ", e$call, "\n"), sep="")
			exitValue <<- 1
		},
		finally={
			cat(paste0("connectToDb exitValue: ", exitValue, "\n"), sep="")
		})
	})

	cat(paste("About to test exitValue", exitValue, "\n"))
	if (exitValue == 0) {
		cat("Performing basic stats and risk analysis\n")
		statsOutput <- capture.output({
			# tryCatch()is trouble because it replaces the stack! it also copies all global variables!
			
			#			cat(paste0("About to fetch extract table outside of the try", "\n"))
			#			data=fetchExtractTable()

			tryCatch({
				# withErrorTracing({

					#
					# extract the relevant Study data
					#
					#data=read.table('sahsuland_example_extract.csv',header=TRUE,sep=',')
					cat(paste0("About to fetch extract table", "\n"))
					data=fetchExtractTable()
					
					#
					# Call: performSmoothingActivity()
					#
					cat(paste0("About to calculate band data", "\n"))
					resultBands <- performBandAnal(data)
					resultBands = data.frame(resultBands)

										#
					# Call: performHomogAnal()
					# This runs the test for homogeneity and linearity using the band results from the performBandAnal function 
					cat(paste0("About to run homogeneity tests", "\n"))
					resultHomog <- performHomogAnal(resultBands)
					resultHomog = data.frame(resultHomog)
					
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
 
				  # Cast area_id to char. This is ignored by sqlSave!
				  area_id_is_integer <- FALSE

					cat("About to save to table\n")
					#Save the band data
					errorTrace <<- saveDataFrameToDatabaseTable(resultBands)	# may set exitValue
					if (!is.na(errorTrace) && !is.null(errorTrace) && length(errorTrace)-1 > 0) {
						cat(errorTrace, sep="\n")
					}
					if (exitValue == 0) {
						errorTrace <<- updateMapTableFromSmoothedResultsTable(area_id_is_integer, studyType) # may set exitValue
						if (!is.na(errorTrace) && !is.null(errorTrace) && length(errorTrace)-1 > 0) {
							cat(errorTrace, sep="\n")
						}
					}

					#save the Homogenity test results to the data base (in rif40_homogenity table?)
					errorTrace <<- insertHomogeneityResults(resultHomog)
					if (!is.na(errorTrace) && !is.null(errorTrace) && length(errorTrace)-1 > 0) {
					  cat(errorTrace, sep="\n")
					}
					
				}
			}) # End of tryCatch
		})
		if (!is.null(statsOutput) && length(statsOutput)-1 > 0) {
#			cat(statsOutput, sep="\n")
			if (!is.null(errorTrace)) {
				cat(paste0("Statistics_JRI.R exitValue: ", exitValue, 
					"; append statsOutput tracer: ", length(statsOutput)-1, 
					"; to errorTrace tracer: ", length(errorTrace)-1, 
					"\n"), sep="")
				errorTrace=c(errorTrace, statsOutput)
			}
			else {
				cat(paste0("Statistics_JRI.R exitValue: ", exitValue, "; statsOutput tracer: ", length(statsOutput)-1, "\n"), sep="")
				errorTrace <<- statsOutput
			}
			cat(paste0("Statistics_JRI.R errorTrace tracer: ", length(errorTrace)-1, "\n"), sep="")
		}
		cat("Performing basic stats and risk analysis: Done\n")
	}
	else {
		cat("Could not connect to database\n")	
		cat(paste0("Statistics_JRI.R exitValue: ", exitValue, "; error tracer: ", length(errorTrace)-1, "\n"), sep="")
		return(list(exitValue=exitValue, errorTrace=errorTrace))
	}
	
	if (exitValue == 0 && !is.na(connDB)) {
		dropTemporaryTable()
	}
	# Dummy change to check conflict is resolved
	
	if (!is.na(connDB)) {
		disconnect()
	}
	
	#
	# Free up memory: required for JRI version as the server keeps running! 
	#
	cat(paste0("Total memory in use: ", format(mem_used()), "\nMemory by object:\n"), sep="")
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
	
	# Print trace
	if (length(errorTrace)-1 > 0) {
		cat(paste0("\nStatistics_JRI.R errorTrace: >>>\n"), sep="")
		cat(errorTrace, sep="\n")
		cat(paste0("\n<<< End of Statistics_JRI.R errorTrace.\n\n"), sep="")
	}	
	cat(paste0("Statistics_JRI.R exitValue: ", exitValue, "; error tracer: ", length(errorTrace)-1, "\n"), sep="")
	
	return(list(exitValue=exitValue, errorTrace=errorTrace))	
}


