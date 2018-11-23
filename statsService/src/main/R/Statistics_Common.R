# Title     : Common routines for the smoothing scripts
# Objective : Provides routines common to the main runtime and rerun scripts.
# Created by: Martin McCallion
# Created on: 01/10/2018

getScratchSpace <- function(studyID) {

	# Numbered directory support (1-100 etc) to reduce the number of files/directories per directory
	# to 100. This is to improve filesystem performance on Windows Tomcat servers
	centile <- as.integer(studyID) %/% 100 # 1273 = 12
	# Number directory: d1201-1300\
	numberDir <- paste0("d", (centile*100)+1, "-", (centile+1)*100)

	if (exists("scratchSpace") == FALSE	|| scratchSpace == "") {
		scratchSpace <<- file.path("scratchSpace")
	}

	# Typically: c:\rifDemo\scratchSpace\d1201-1300\s1273\data
	return(file.path(scratchSpace, numberDir, paste0("s", studyID), "data"))
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
establishTableNames <- function(vstudyID) {

	# Always set scratchSpace (we used to do it otherwise, but it caused problems on repeated runs).
	scratchSpace <<- ""
	scratchSpace <<- getScratchSpace(studyID)
  
	scratchStr = as.character(scratchSpace)
	cat("In establishTableNames; scratchSpace is ", scratchStr, "\n")
	#The name of the extract table that is created by the middleware for a given
	#study.  It is of the format rif_studies.s[study_id]_extract
	extractTableName <<- paste0("rif_studies.s", vstudyID, "_extract")

	#The name of the skeleton table created by the RIF middleware to hold smoothed results
	#for a given study.  Initially the values for smoothed columns will be empty.  It is
	#of the format rif_studies.s[study_id]_map
	mapTableName <<- paste0("rif_studies.s", vstudyID, "_map")

	# Name of Rdata CSV file for debugging results save
	# This needs to be passed in via interface

	tryCatch({
		#Put all scratch files in sub directory s<study_id>
		if (!file.exists(scratchStr)) {
			dir.create(scratchStr, recursive=TRUE)
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
	temporaryHomogFileName <<-file.path(scratchSpace, paste0("tmp_s", vstudyID, "_Homog.csv"))
	#The name of the temporary table that this script uses to hold the data frame
	#containing smoothed results.  It should have a 1:1 correspondence between
	#its fields and fields that appear in the map table skeleton.

	temporarySmoothedResultsTableName <<-paste(userID, ".tmp_s", vstudyID, "_map", sep="")
}


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
	str <- N # COPY
	isNumeric <- suppressWarnings(!is.na(as.numeric(str)))
	isInteger <- suppressWarnings(!is.na(as.integer(str)) && as.integer(str))
	isNotRounded <- suppressWarnings(isNumeric && as.numeric(str) == round(as.numeric(str)))
	isIntRegexp <- suppressWarnings(!grepl("[^[:digit:]]", format(N,  digits = 20,
	scientific = FALSE)))

	check.integer.Result <- (isNumeric && isInteger && isNotRounded && isIntRegexp)

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
