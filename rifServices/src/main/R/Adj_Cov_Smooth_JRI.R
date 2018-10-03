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

	#Numbered directory support (1-100 etc) to reduce the number of files/directories per directory to 100. This is to improve filesystem
	#performance on Windows Tomcat servers
	centile <- as.integer(studyID) %/% 100 # 1273 = 12
	# Number directory: d1201-1300\
	numberDir <- paste0("d", (centile*100)+1, "-", (centile+1)*100)

	if (exists("scratchSpace") == FALSE  || scratchSpace == "") {
		scratchSpace <<- file.path("scratchSpace")
	}
	# Typically: c:\rifDemo\scratchSpace\d1201-1300\s1273\data
	scratchSpace <<- file.path(scratchSpace, numberDir, paste0("s", studyID), "data")


	establishTableNames(studyID)
	cat("Table names established\n")

	#
	# Install scripts required to re-run study
	#
	createWindowsScript("Adj_Cov_Smooth_csv.R")

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
		errorTrace<<-capture.output({
			# tryCatch()is trouble because it replaces the stack! it also copies all global variables!

#			cat(paste0("About to fetch extract table outside of the try", "\n"))
#			data=fetchExtractTable()
			tryCatch({
					withErrorTracing({  				
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

							cat("About to save to table\n")
							lerrorTrace <<- saveDataFrameToDatabaseTable(result)	# may set exitValue
							if (exitValue == 0) {
								lerrorTrace2 <<- updateMapTableFromSmoothedResultsTable(area_id_is_integer) # may set exitValue
								if (!is.null(lerrorTrace2) && length(lerrorTrace2)-1 > 0) {
									append(lerrorTrace, lerrorTrace2)
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
	}
	else {
		cat("Could not connect to database\n")	
		cat(paste0("Adj_Cov_Smooth_JRI.R exitValue: ", exitValue, "; error tracer: ", length(errorTrace)-1, "\n"), sep="")
		return(list(exitValue=exitValue, errorTrace=errorTrace))
	}

	if (!is.null(lerrorTrace) && length(lerrorTrace)-1 > 0) {
		if (!is.null(errorTrace)) {
			append(errorTrace, lerrorTrace)
		}
		else {
			errorTrace <<- lerrorTrace
		}
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
		cat(paste0("\nAdj_Cov_Smooth_JRI.R errorTrace: >>>\n"), sep="")
	 	cat(errorTrace, sep="\n")
		cat(paste0("\n<<< End of Adj_Cov_Smooth_JRI.R errorTrace.\n\n"), sep="")
	}	
	cat(paste0("Adj_Cov_Smooth_JRI.R exitValue: ", exitValue, "; error tracer: ", length(errorTrace)-1, "\n"), sep="")

	return(list(exitValue=exitValue, errorTrace=errorTrace))
}
