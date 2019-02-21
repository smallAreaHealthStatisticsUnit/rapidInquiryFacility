# Title	 : JDBC Handler
# Objective : Manages JDBC connections and queries for the RIF's Java/R interface.
# Created by: martin
# Created on: 31/05/2018

# Not sure whether we need both of these or, if only one, which.
require(RJDBC)
library(RJDBC)

connectToDb <- function() {

	cat(paste0("Trying JDBC connection using driver class ", db_driver_class_name, ", lib dir ",
			   java_lib_path_dir, ", URL ", db_url, ", user ", userID, "\n"))

	# We suppress warnings when getting the driver because on some platforms a warning is
	# generated because the JVM is already running, which stops subsequent statements from
	# being executed.
	suppressWarnings(driver <- JDBC(db_driver_class_name,
		Sys.glob(paste0(java_lib_path_dir, "/*.jar"))))
	connection <<- RJDBC::dbConnect(driver, db_url, userID, password)

	cat(paste("... JDBC connection established", "\n"))
#
# Run rif40_startup() procedure on Postgres
#
	if (db_driver_prefix == "jdbc:postgresql") {
		sql <- "SELECT rif40_sql_pkg.rif40_startup()"
		doQuery(sql)
	}
	return(connection)
}

disconnect <- function() {

	dbDisconnect(connection)
}

doQuery <- function(sql) {

	cat(paste("Querying by JDBC:", sql, "\n"))
	sqlData <- tryCatch(dbGetQuery(connection, sql),
						warning=function(w) {
							cat(paste("UNABLE TO QUERY! SQL> ", sql, "; warning: ", w, "\n"), sep="")
							exitValue <<- 1
						},
						error=function(e) {
							cat(paste("ERROR IN QUERY! SQL> ", sql, "; error: ", e, "\n"), sep="")
							exitValue <<- 1
						})
	if (is.null(nrow(sqlData))) {
		cat(paste("ERROR IN QUERY! (null data returned); SQL> ", sql, "; error: ", "\n"), sep="")
		exitValue <<- 1
	}

	return(sqlData)
}

##==========================================================================
#FUNCTION:	fetchExtractTable
#DESCRIPTION: Read in the extract table from the database
#			 Save extract data frame to file
#RETURNS:	  Data frame
#
##==========================================================================
fetchExtractTable <- function() {
	#Part I: Read in the extract table
	#=================================
	#Read original extract table data into a data frame

	cat(paste0("JDBC EXTRACT TABLE NAME: ", extractTableName, "\n"), sep="")

	data = tryCatch(doQuery(paste0("select * from ", extractTableName)),
	warning=function(w) {
		cat(paste("JDBC UNABLE TO FETCH! ", w, "\n"), sep="")
		exitValue <<- 1
	},
	error=function(e) {
		cat(paste("JDBC ERROR FETCHING! ", geterrmessage(), "\n"), sep="")
		exitValue <<- 1
	})

	#
	# Save extract data frame to file
	#
	if (dumpFramesToCsv == TRUE) {
		cat(paste0("JDBC Saving extract frame to: ", temporaryExtractFileName, "\n"), sep="")
		write.csv(data, file=temporaryExtractFileName)
	}
	numberOfRows <- nrow(data)
	if (is.null(nrow(data))) {
		cat(paste("JDBC ERROR IN FETCH! (null data returned): ", extractTableName), sep = "")
		exitValue <<- 1
	}
	cat(paste0("JDBC ", extractTableName," numberOfRows=", numberOfRows, "==", "\n"), sep="")

	if (exitValue != 0) {
		stop("JDBC Error reading in the extract table from the database");
	}

	return(data)
}


##================================================================================
##FUNCTION: getAdjacencyMatrix
##DESCRIPTION
##Get Adjacency matrix
##Set (exitvalue) 0 on success, 1 on failure
## THROWS EXCEPTION
##================================================================================
getAdjacencyMatrix <- function() {
	if (db_driver_prefix == "jdbc:postgresql") {
		sql <- paste("SELECT * FROM rif40_xml_pkg.rif40_GetAdjacencyMatrix(", studyID, ")")
		AdjRowset=doQuery(sql)
		numberOfRows <- nrow(AdjRowset)
	}
	else if (db_driver_prefix == "jdbc:sqlserver") {
		sql <- paste("SELECT b2.adjacencytable
				 FROM [rif40].[rif40_studies] b1, [rif40].[rif40_geographies] b2
				 WHERE b1.study_id  = ", studyID ,"
				 AND b2.geography = b1.geography");
		adjacencyTableRes=doQuery(sql)
		numberOfRows <- nrow(adjacencyTableRes)
		if (numberOfRows != 1) {
			cat(paste("Expected 1 row; got: " + numberOfRows + "; SQL> ", sql, "\n"), sep="")
			exitValue <<- 1
		}
		adjacencyTable <- tolower(adjacencyTableRes$adjacencytable[1])
		#	print(adjacencyTable);
		sql <- paste("WITH b AS ( /* Tilemaker: has adjacency table */
				 SELECT b1.area_id, b3.geolevel_id
				 FROM [rif40].[rif40_study_areas] b1, [rif40].[rif40_studies] b2, [rif40].[rif40_geolevels] b3
				 WHERE b1.study_id  = ", studyID ,"
				 AND b1.study_id  = b2.study_id
				 AND b2.geography = b3.geography
	)
				 SELECT c1.areaid AS area_id, c1.num_adjacencies, c1.adjacency_list
				 FROM [rif_data].[", adjacencyTable, "] c1, b
				 WHERE c1.geolevel_id   = b.geolevel_id
				 AND c1.areaid		= b.area_id
				 ORDER BY 1", sep = "")
		AdjRowset=doQuery(sql)
		numberOfRows <- nrow(AdjRowset)
	}
	else {
		cat(paste("Unsupported DB: ", db_driver_prefix, "\n"), sep="")
		exitValue <<- 1
	}


	if (exitValue != 0) {
		 stop("Error getting adjacency matrix");
	}

	cat(paste0("rif40_GetAdjacencyMatrix numberOfRows=",numberOfRows, "==", "\n"), sep="")

	#
	# areaid			   num_adjacencies adjacency_list_truncated
	# -------------------- --------------- ------------------------------------------------------------------------------------------
	# 01.001.000100.1				   18 01.001.000100.2,01.001.000200.1,01.002.000500.1,01.002.000500.4,01.002.000500.7,01.002.000
	# 01.001.000100.2					2 01.001.000100.1,01.001.000200.1
	# 01.001.000200.1					4 01.001.000100.1,01.001.000100.2,01.001.000300.1,01.005.002400.1
	# 01.001.000300.1					1 01.001.000200.1
	# 01.002.000300.1					3 01.002.000300.2,01.002.000300.3,01.002.000300.4
	# 01.002.000300.2					4 01.002.000300.1,01.002.000300.4,01.002.000500.3,01.002.000600.2
	# 01.002.000300.3					3 01.002.000300.1,01.002.000300.4,01.002.000300.5
	# 01.002.000300.4					6 01.002.000300.1,01.002.000300.2,01.002.000300.3,01.002.000300.5,01.002.000400.3,01.002.000
	# 01.002.000300.5					3 01.002.000300.3,01.002.000300.4,01.002.000400.3
	# 01.002.000400.1					6 01.002.000400.2,01.002.000400.5,01.002.000400.7,01.002.001700.1,01.002.001700.2,01.002.001
	#
	#   cat(head(AdjRowset, n=10, "\n"), sep="")

	#
	# Save extract data frame to file
	#
	if (dumpFramesToCsv == TRUE) {
		cat(paste0("Saving adjacency matrix to: ", adjacencyMatrixFileName, "\n"), sep="")
		write.csv(AdjRowset, file=adjacencyMatrixFileName)
	}

	return(AdjRowset);
}

saveDataFrameToDatabaseTable <- function(data) {

	lerrorTrace<-capture.output({
		cat("In saveDataFrameToDatabaseTable")

#
# Save data frame to file
#
		if (dumpFramesToCsv == TRUE) {
			cat(paste0("Saving data frame to: ", temporarySmoothedResultsFileName, "\n"), sep="")
			write.csv(data, file=temporarySmoothedResultsFileName)
		}

#
# Save data frame to table
#
		cat(paste0("Creating temporary table: ", temporarySmoothedResultsTableName, "\n"), sep="")
		tryCatch({
			# withErrorTracing({
				if (dbExistsTable(connection, temporarySmoothedResultsTableName)) {
					dropTemporaryTable()
				}
				
				cat(paste0("Replace INF will NA for temporary table: ", temporarySmoothedResultsTableName, "\n"), sep="")
				data<-do.call(data.frame, lapply(data, function(x) {
					replace(x, is.infinite(x), NA) # Replace INF will NA for SQL Server
				}))
				
				cat(paste0("Replace NAN will NA for temporary table: ", temporarySmoothedResultsTableName, "\n"), sep="")
				data<-do.call(data.frame, lapply(data, function(x) {
					replace(x, is.nan(x), NA) # Replace NaN will NA for SQL Server
				}))

				cat(paste0("Replace \"\" will NA for temporary table: ", temporarySmoothedResultsTableName, "\n"), sep="")
				data <- do.call(data.frame, lapply(data, function(x) {
					replace(x, (x == ""), NA)
				}))

				cat(paste0("About to write temporary table: ", temporarySmoothedResultsTableName, "; first 10 rows\n"))
				print(head(data,10))			## First 10 rows
	#
	# Does not work on SQL Server:
	#
	# saveDataFrameToDatabaseTable() ERROR:  execute JDBC update query failed in dbSendUpdate 
	# (The incoming tabular data stream (TDS) remote procedure call (RPC) protocol stream is incorrect. Parameter 5 (""): 
	# The supplied value is not a valid instance of data type float. 
	# Check the source data for invalid values. An example of an invalid value is data of numeric type with scale greater than precision.) ; call stack:  .local 
	#
	# Suspected bug with the MARS (array INSERT) mode. OCDBC does NOT support MARS so will have to be used instead
	#
				dbWriteTable(connection, name=temporarySmoothedResultsTableName, value=data)

				# Add indices to the new table so that its join with s[study_id]_map will be more
				# efficient
				cat(paste("Creating study_id index on temporary table\n"), sep="")
				dbSendUpdate(connection, generateTableIndexSQLQuery(temporarySmoothedResultsTableName,
					"study_id"))
				cat(paste("Creating area_id index on temporary table\n"), sep="")
				dbSendUpdate(connection, generateTableIndexSQLQuery(temporarySmoothedResultsTableName,
					"area_id"))
				cat(paste("Creating genders index on temporary table\n"), sep="")
				dbSendUpdate(connection, generateTableIndexSQLQuery(temporarySmoothedResultsTableName,
					"genders"))
				cat(paste("Created indices on temporary table\n"), sep="")
			# })
			},
			warning=function(w) {
				cat(paste("saveDataFrameToDatabaseTable() WARNING: ", w, "\n"), sep="")
			},
			error=function(e) {
				e <<- e
				cat(paste("saveDataFrameToDatabaseTable() ERROR: ", e$message,
				"; call stack: ", e$call, "\n"), sep="")
				exitValue <<- 1
			},
			finally={
				cat(paste0("saveDataFrameToDatabaseTable finishing", "\n"), sep="")
			}
		)
	})
	
	# Print trace
	if (length(lerrorTrace)-1 > 0) {
	 	cat(lerrorTrace, sep="\n")
	}
	return(lerrorTrace);
}

generateTableIndexSQLQuery <- function(tableName, columnName) {
	sqlIndexQuery <- paste0(
	"CREATE INDEX s", studyID, "_ind_", columnName,
	" ON ",
	tableName,
	"(",
	columnName,
	")")

	cat(paste0("SQL> ", sqlIndexQuery))

	return(sqlIndexQuery);
}

convertSqlNansToNulls <- function(col) {

	protectedCol <- paste("CASE WHEN", col, " = 'NAN' THEN NULL ELSE", col, "END")
	return(protectedCol)
}

convertRNansToNulls <- function(col) {

	if (is.na(col) || is.nan(col)) {
		return("NULL")
	} else {
		return(col)
	}
}

##================================================================================
##FUNCTION: updateMapTable
##DESCRIPTION
##When the study is run, the RIF creates a skeleton map table that has all the
##fields we expect the smoothed results will contain.  After we do the smoothing
##operation, we will have a temporary table that contains all the smoothed results.
##This method updates cell values in the skeleton map file with values from
##corresponding fields that exist in the temporary table.
##================================================================================
updateMapTableFromSmoothedResultsTable <- function(area_id_is_integer, studyType) {

	##================================================================================
	##
	## R reads the area_id and auto casts into to a integer. This causes the UPDATE to
	## do not rows which in turn raises a Could not SQLEXecDirect error
	##
	## We need to detect if the frame area_id is an integer and then add a cast
	##
	##================================================================================

	if (db_driver_prefix == "jdbc:postgresql") {
		updateStmtPart0 <- paste(
			"UPDATE ", mapTableName, " a\n SET ")
	}
	else if (db_driver_prefix == "jdbc:sqlserver") { ## No alaised JOIN allowed
		updateStmtPart0 <- paste(
			"UPDATE ", mapTableName, "\n SET ")
	}

	updateStmtPart1 <- paste0(updateStmtPart0,
		"direct_standardisation=b.direct_standardisation,",
		"adjusted=b.adjusted,",
		"observed=b.observed,",
		"expected=b.expected,",
		"lower95=b.lower95,",
		"upper95=b.upper95,",
		"relative_risk=b.relative_risk,",
		"smoothed_relative_risk=", convertSqlNansToNulls("b.smoothed_relative_risk"),
		", posterior_probability=", convertSqlNansToNulls("b.posterior_probability"),
		", posterior_probability_upper95=", convertSqlNansToNulls("b.posterior_probability_upper95"),
		", posterior_probability_lower95=", convertSqlNansToNulls("b.posterior_probability_lower95"),
		", residual_relative_risk=", convertSqlNansToNulls("b.residual_relative_risk"),
		", residual_rr_lower95=", convertSqlNansToNulls("b.residual_rr_lower95"),
		", residual_rr_upper95=", convertSqlNansToNulls("b.residual_rr_upper95"),
		", smoothed_smr=", convertSqlNansToNulls("b.smoothed_smr"),
		", smoothed_smr_lower95=", convertSqlNansToNulls("b.smoothed_smr_lower95"),
		", smoothed_smr_upper95=", convertSqlNansToNulls("b.smoothed_smr_upper95"), sep="\n")

	if (db_driver_prefix == "jdbc:postgresql") {
		updateStmtPart2 <- paste0(updateStmtPart1,
			" FROM ", temporarySmoothedResultsTableName, " b WHERE ",
			"a.study_id=b.study_id AND ",
			"a.band_id=b.band_id AND ",
			"a.inv_id=b.inv_id AND ",
			"a.genders=b.genders ", sep="\n")
	}
	else if (db_driver_prefix == "jdbc:sqlserver") { ## No alaised JOIN allowed
		updateStmtPart2 <- paste0(updateStmtPart1,
			" FROM ", mapTableName, " AS a INNER JOIN ", temporarySmoothedResultsTableName, " AS b ON (",
			"a.study_id=b.study_id AND ",
			"a.band_id=b.band_id AND ",
			"a.inv_id=b.inv_id AND ",
			"a.genders=b.genders ", sep="\n")
	}

	if (studyType == "riskAnalysis") { # No area id
		if (db_driver_prefix == "jdbc:postgresql") {
			updateMapTableSQLQuery <- updateStmtPart2;
		}
		else if (db_driver_prefix == "jdbc:sqlserver") {
			updateMapTableSQLQuery <- paste0(updateStmtPart2, ')')
		}
	}
	else {
		if (area_id_is_integer) {
			if (db_driver_prefix == "jdbc:postgresql") {
				updateMapTableSQLQuery <- paste0(updateStmtPart2,
							" AND CAST(a.area_id AS INTEGER)=CAST(b.area_id AS INTEGER)")
			}
			else if (db_driver_prefix == "jdbc:sqlserver") { ## No alaised JOIN allowed
				updateMapTableSQLQuery <- paste0(updateStmtPart2,
							" AND CAST(a.area_id AS INTEGER)=CAST(b.area_id AS INTEGER))")
			}
		}
		else {
			if (db_driver_prefix == "jdbc:postgresql") {
				updateMapTableSQLQuery <- paste0(updateStmtPart2, " AND a.area_id=b.area_id")		}
			else if (db_driver_prefix == "jdbc:sqlserver") { ## No alaised JOIN allowed
				updateMapTableSQLQuery <- paste0(updateStmtPart2, " AND a.area_id=b.area_id)")
			}
		}
	}

	cat(paste0("SQL> ", updateMapTableSQLQuery, "\n"))
	flush.console()

	lerrorTrace<-capture.output({
		res <- tryCatch({
				# withErrorTracing({
					dbSendUpdate(connection, updateMapTableSQLQuery)
				# })
			},
			warning=function(w) {
				warn1 <<- paste("UNABLE TO QUERY! SQL> ", updateMapTableSQLQuery, "; warning: ", w, "\n")
				cat(warn1, sep="")
				exitValue <<- 1
			}, error=function(e) {
				err <<- paste("CATCH ERROR IN QUERY! SQL> ", updateMapTableSQLQuery,
				"; error: ", e,
				"; JDBC error", "\n")
				cat(err, sep="")
				exitValue <<- 1
			}, finally=function() {

				if (is.null(res)) {
					cat(paste("QUERY FAILED! SQL> ", updateMapTableSQLQuery,
					"; res: ", res, "\n"), sep="")
					exitValue <<- 1
				}
				else if (res == 1) {
					cat(paste0("Updated map table: ", mapTableName, "\n"), sep="")
				}
				else if (res == -1) { # This can be no rows updated!
					cat(paste("SQL ERROR IN QUERY! SQL> ", updateMapTableSQLQuery,
					"; error: no rows returned", "\n"), sep="")
					exitValue <<- 1
				}
				else {
					cat(paste("UNKNOWN ERROR IN QUERY! SQL> ", updateMapTableSQLQuery,
					"; res: ", res, "\n"), sep="")
					exitValue <<- 1
				}

				flush.console()
				if (exitValue != 0) {
					msg <- paste0("Error updating Map Table from Smoothed Results Table\n")
					stop(msg);
				}
			}
		)
	})

	# Print trace
	if (length(lerrorTrace)-1 > 0) {
	 	cat(lerrorTrace, sep="\n")
	}
	return(lerrorTrace);
} # End of updateMapTableFromSmoothedResultsTable()


##================================================================================
##FUNCTION: dropTemporaryTable
##DESCRIPTION
##Drops temporary table used for results update
##Set (exitvalue) 0 on success, 1 on failure
##================================================================================
dropTemporaryTable <- function() {
	tryCatch({
		cat(paste0("Dropping temporary table: ", temporarySmoothedResultsTableName, "\n"), sep="")
		dbSendUpdate(connection, paste0("DROP TABLE ", temporarySmoothedResultsTableName))
	},
	warning=function(w) {
		cat(paste("UNABLE to drop temporary table: ", temporarySmoothedResultsTableName, w, "\n"), sep="")
		exitValue <<- 0
	},
	error=function(e) {
		cat(paste("WARNING: An error occurred dropping temporary table: ", temporarySmoothedResultsTableName, geterrmessage(), "\n"), sep="")
		exitValue <<- 1
	})
}


##================================================================================
##FUNCTION: insertHomogeneityResults
##DESCRIPTION
##Writes the results of the homogeneity tests to the t_rif40_homogeneity table
## Will insert results - needs to check if results have already been written first??
##================================================================================
insertHomogeneityResults <- function(homogData) {
  
  ##================================================================================
  ##
  ## R reads the area_id and auto casts into to a integer. This causes the UPDATE to
  ## do not rows which in turn raises a Could not SQLEXecDirect error
  ##
  ## We need to detect if the frame area_id is an integer and then add a cast
  ##
  ##================================================================================
  homogData = data.frame(homogData)
  
  
  
  #
  # Save data frame to csv file for debugging
  #
  if (dumpFramesToCsv == TRUE) {
	cat(paste0("Saving data frame to: ", temporaryHomogFileName, "\n"), sep="")
	write.csv(homogData, file=temporaryHomogFileName)
  }
  
  
  #Insert 3 rows, M,F,Both
  for (i in 1:3)
  {
	 selHomog <- paste0(
	 	"select * FROM rif40.t_rif40_homogeneity WHERE inv_id =", homogData$inv_id[i],
	 	" AND study_id=", homogData$study_id[i], " and adjusted = ", as.integer(adj),
	 	" and genders = ", homogData$gender[i], sep="")
	 homogExists <- tryCatch(doQuery(selHomog),
	 warning=function(w) {
	   cat(paste("JDBC UNABLE TO SELECT FROM t_rif40_homogeneity! ", w, "\n"), sep="")
	   exitValue <<- 1
	 },
	 error=function(e) {
	   cat(paste("JDBC ERROR SELECTING FROM t_rif40_homogeneity! ", geterrmessage(), "\n"), sep="")
	   exitValue <<- 1
	 })
	 numberOfRows <- nrow(homogExists)

	 if (numberOfRows == 0) {
	 
	  insertStmt <- paste(
	  	"INSERT INTO rif40.t_rif40_homogeneity(inv_id, study_id, adjusted, genders) VALUES (",
	  		homogData$inv_id[i], ",", homogData$study_id[i], ",", as.integer(adj), ",",
	  		homogData$gender[i],");")
	  res <- tryCatch({
				# withErrorTracing({
					dbSendUpdate(connection, insertStmt)
				# })
			 },warning=function(w) {
				cat(paste("JDBC UNABLE TO INSERT INTO t_rif40_homogeneity! ", w, "\n"), sep="")
				exitValue <<- 1
			  },
			error=function(e) {
			  cat(paste("JDBC ERROR INSERTING INTO t_rif40_homogeneity! ", geterrmessage(), "\n"),
			  sep="")
			  exitValue <<- 1
			})
	}
	
	# Finally update the record which should now exist
	updateStmt <- paste("UPDATE rif40.t_rif40_homogeneity SET username = '" , userID,
						"', homogeneity_dof = ", convertRNansToNulls(homogData$df[i]),
						", homogeneity_chi2 = ", convertRNansToNulls(homogData$chisqHomog[i]),
						", homogeneity_p = ", convertRNansToNulls(homogData$pValHomog[i]),
						", linearity_chi2 = ", convertRNansToNulls(homogData$chisqLT[i]),
						", linearity_p = ", convertRNansToNulls(homogData$pValLT[i]),
						", explt5 = ", convertRNansToNulls(homogData$bandsLT5[i]),
						" WHERE inv_id = ", homogData$inv_id[i],
						" AND study_id = ", homogData$study_id[i],
						" and adjusted = ", as.integer(adj),
						" and genders = ", homogData$gender[i], ";\n", sep="") 
			res <- tryCatch({
			  # withErrorTracing({
				dbSendUpdate(connection, updateStmt)
				# })
			},warning=function(w) {
			  cat(paste("JDBC UNABLE TO UPDATE t_rif40_homogeneity! ", w, "\n"), sep="")
			  exitValue <<- 1
			},
			error=function(e) {
			  cat(paste("JDBC ERROR UPDATING t_rif40_homogeneity! ", geterrmessage(), "\n"), sep="")
			  exitValue <<- 1
			})
  }
  return(exitValue);
} # End of updateMapTableFromSmoothedResultsTable()

