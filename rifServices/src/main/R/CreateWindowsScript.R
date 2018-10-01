# Title     : createWindowsScript 
# Objective : Generate the Windows version of the script used for rerunning studies.
# Created by: Martin McCallion
# Created on: 21/09/2018
createWindowsScript <- function(script_name) {

	if (exists("catalina_home")) {
		performSmoothingActivityScriptA <- file.path(catalina_home, "webapps", "rifServices", 
		"WEB-INF", "classes", script_name) # Source
		performSmoothingActivityScriptB <- file.path(scratchSpace, "performSmoothingActivity.R") # Target
		if (!file.exists(performSmoothingActivityScriptB)) {
			cat(paste("Copy: ", performSmoothingActivityScriptA, " to: ", 
				performSmoothingActivityScriptB, "\n"), sep="")

			tryCatch({
				file.copy(performSmoothingActivityScriptA, performSmoothingActivityScriptB)
			},
				warning = function(w) {
					cat(paste("UNABLE to copy: ", performSmoothingActivityScriptA, " to: ",
						performSmoothingActivityScriptB, w, "\n"), sep="")
					exitValue <<- 0
			},
				error = function(e) {
					cat(paste("ERROR copying: ", performSmoothingActivityScriptA, " to: ",
						performSmoothingActivityScriptB, e, "\n"), sep="")
					exitValue <<- 1
			}) # End of tryCatch
		} else {
			cat(paste("WARNING! No need to copy: ", performSmoothingActivityScriptA, " to: ",
				performSmoothingActivityScriptB, "\n"), sep="")
		}

		Adj_Cov_Smooth_csvA <- file.path(catalina_home, "webapps", "rifServices", "WEB-INF", 
			"classes", "Adj_Cov_Smooth_csv.R") # Source
		Adj_Cov_Smooth_csvB <- file.path(scratchSpace, "Adj_Cov_Smooth_csv.R") # Target
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
				cat(paste("ERROR copying: ", Adj_Cov_Smooth_csvA, " to: ", Adj_Cov_Smooth_csvB, e, 
					"\n"), sep="")
				exitValue <<- 1
			}) # End of tryCatch
		} else {
			cat(paste("WARNING! No need to copy: ", Adj_Cov_Smooth_csvA, " to: ", 
				Adj_Cov_Smooth_csvB, "\n"), sep="")
		}

		Adj_Cov_Smooth_CommonA <- file.path(catalina_home, "webapps", "rifServices", "WEB-INF",
		"classes", "Adj_Cov_Smooth_Common.R") # Source
		Adj_Cov_Smooth_CommonB <- file.path(scratchSpace, "Adj_Cov_Smooth_Common.R") # Target
		if (!file.exists(Adj_Cov_Smooth_CommonB)) {
			cat(paste("Copy: ", Adj_Cov_Smooth_CommonA, " to: ", Adj_Cov_Smooth_CommonB, "\n"), sep="")

			tryCatch({
				file.copy(Adj_Cov_Smooth_CommonA, Adj_Cov_Smooth_CommonB)
			},
			warning=function(w) {
				cat(paste("UNABLE to copy: ", Adj_Cov_Smooth_CommonA, " to: ", Adj_Cov_Smooth_CommonB, w, "\n"), sep="")
				exitValue <<- 0
			},
			error=function(e) {
				cat(paste("ERROR copying: ", Adj_Cov_Smooth_CommonA, " to: ", Adj_Cov_Smooth_CommonB, e,
				"\n"), sep="")
				exitValue <<- 1
			}) # End of tryCatch
		} else {
			cat(paste("WARNING! No need to copy: ", Adj_Cov_Smooth_CommonA, " to: ",
			Adj_Cov_Smooth_CommonB, "\n"), sep="")
		}

		rif40_run_RA <- file.path(catalina_home, "webapps", "rifServices", "WEB-INF", "classes", 
			"rif40_run_R.bat") # Source
		rif40_run_RB <- file.path(scratchSpace, "rif40_run_R.bat") # Target
		
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
		} else {
			cat(paste("WARNING! No need to copy: ", rif40_run_RA, " to: ", rif40_run_RB, "\n"), sep="")
		}

		#
		# Create rif40_run_R_env.bat
		#
		rif40_run_R_env = paste(
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
			paste0("SET MODEL=", model),
			paste0("SET COVARIATENAME=", paste0(names.adj)),
			paste0("SET SCRIPT_NAME=", paste0(script_name)),
			sep="\n");

		rif40_run_R_envB <- file.path(scratchSpace, "rif40_run_R_env.bat") # Target

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
