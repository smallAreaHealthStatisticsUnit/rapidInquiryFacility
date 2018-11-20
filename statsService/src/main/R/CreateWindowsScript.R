# Title     : createWindowsScript 
# Objective : Generate the Windows version of the script used for rerunning studies.
# Created by: Martin McCallion
# Created on: 21/09/2018

createWindowsScript <- function(script_name) {

	if (exists("catalina_home")) {

		copy_file(script_name)#
		copy_file("Statistics_csv.R")
		copy_file("Statistics_Common.R")
		copy_file("rif40_run_R.bat")

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
			paste0("SET RISKANAL=", paste0(riskAnal)),
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

copy_file <- function(script_name) {

	source <- file.path(catalina_home, "webapps", "statistics", "WEB-INF", "classes", script_name)
	target <- file.path(scratchSpace, script_name)
	if (!file.exists(target)) {
		cat(paste("Copy: ", source, " to: ", target, "\n"), sep="")

		tryCatch({
			file.copy(source, target)
		},
		warning = function(w) {
			cat(paste("UNABLE to copy: ", source, " to: ", target, w, "\n"), sep="")
			exitValue <<- 0
		},
		error = function(e) {
			cat(paste("ERROR copying: ", source, " to: ", target, e, "\n"), sep="")
			exitValue <<- 1
		}) # End of tryCatch
	} else {
		cat(paste("WARNING! No need to copy: ", source, " to: ", target, "\n"), sep="")
	}

}
