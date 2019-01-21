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
#   RIF ODBC functions
#
# A merge of RIF_OBDC.R and JdbcHandler.R using the OdBC functions
############################################################################################################
# Not sure whether we need both of these or, if only one, which.
require(RODBC)
library(RODBC)

##====================================================================
##FUNCTION: createDatabaseConnectionString
##DESCRIPTION: assembles pieces of database information such as
##the host, port and database name to create a database connection
##string that can be used to make an ODBC connection
##====================================================================
createDatabaseConnectionString <- function() {
  
  #cat( paste0("host==", dbHost, "==", "\n"), sep="")
  #cat( paste0("port==", dbPort, "==", "\n"), sep="")
  #cat( paste0("datatabase name==", dbName, "==", "\n"), sep="")
  
  return(paste0(dbHost, ":", dbPort, "/", dbName))
}
##================================================================================
##FUNCTION: connectToDb
##DESCRIPTION
##Connect to ODBC database
##Set (exitvalue) 0 on success, 1 on failure 
##================================================================================
connectToDb <- function() {
	cat(paste0("Connect to database: ", odbcDataSource, "\n"), sep="")
  
	errorTrace<-capture.output({
		tryCatch({
			if (is.na(odbcDataSource)) {
				cat(paste("UNABLE TO CONNECT! Null odbcDataSource\n"), sep="")
				exitValue <<- 1
			}
			else {
				connection <<- odbcConnect(odbcDataSource, uid=as.character(userID), pwd=as.character(password))
			}
		},
        warning=function(w) {
            cat(paste("UNABLE TO CONNECT! ", w, "\n"), sep="")
            exitValue <<- 0
        },
        error=function(e) {
            cat(paste("ERROR CONNECTING! ", geterrmessage(), "\n"), sep="")
            exitValue <<- 1
        },
		finally={
			if (!is.na(connection)) {
				cat(paste0("Connected to database: ", odbcDataSource, " as ", userID, "\n"), sep="")
#				odbcSetAutoCommit(connection, autoCommit = FALSE)
				cat(odbcGetInfo(connection), "\n", sep="")
#
# Run rif40_startup() procedure on Postgres
#
				if (db_driver_prefix == "jdbc:postgresql") {
					sql <- "SELECT rif40_sql_pkg.rif40_startup()"
					doQuery(sql)
				}
			}
		}) # End of tryCatch
	})
	
	return(connection);
}

##================================================================================
##FUNCTION: disconnect
##DESCRIPTION
##Disconnect from ODBC database
##Set (exitvalue) 0 on success, 1 on failure 
##================================================================================
disconnect <- function() {
		cat("Closing database connection\n")
		odbcEndTran(connection, commit = TRUE)
		odbcClose(connection)
}

##====================================================================
##FUNCTION:    doQuery()
##PARAMETER:   SQL 
##RETURNS:     Data frame
##DESCRIPTION: Run SQL query. On error/warning/NULL data; print SQL, error and exit
##====================================================================
doQuery <- function(sql) {
  sqlData <- tryCatch(sqlQuery(connection, sql, FALSE),
                      warning=function(w) {
                        cat(paste("UNABLE TO QUERY! SQL> ", sql, "; warning: ", w, "\n"), sep="")
                        exitValue <<- 1
                      },
                      error=function(e) {
                        cat(paste("ERROR IN QUERY! SQL> ", sql, "; error: ", odbcGetErrMsg(connection), "\n"), sep="")
                        exitValue <<- 1
                      })
  if (is.null(nrow(sqlData))) {
    cat(paste("ERROR IN QUERY! (null data returned); SQL> ", sql, "; error: ", odbcGetErrMsg(connection), "\n"), sep="")
    exitValue <<- 1
  } 
  
  return(sqlData)	  
}

##==========================================================================
#FUNCTION:    fetchExtractTable
#DESCRIPTION: Read in the extract table from the database
#             Save extract data frame to file
#RETURNS:	  Data frame
#
##==========================================================================
fetchExtractTable <- function() {
  #Part I: Read in the extract table
  #=================================
  #Read original extract table data into a data frame
  
  cat(paste0("EXTRACT TABLE NAME: ", extractTableName, "\n"), sep="")
  
  data=tryCatch(sqlFetch(connection, extractTableName),
		warning=function(w) {
		  cat(paste("UNABLE TO FETCH! ", w, "\n"), sep="")
		  exitValue <<- 1
		},
		error=function(e) {
		  cat(paste("ERROR FETCHING! ", geterrmessage(), "\n"), sep="")
		  exitValue <<- 1
		})	
  
#
# Save extract data frame to file
#
	if (dumpFramesToCsv == TRUE) {
		cat(paste0("Saving extract frame to: ", temporaryExtractFileName, "\n"), sep="")
		write.csv(data, file=temporaryExtractFileName)
	}
	numberOfRows <- nrow(data)	
	if (is.null(nrow(data))) {
		cat(paste("ERROR IN FETCH! (null data returned): ", extractTableName, ", error: ", odbcGetErrMsg(connection), "\n"), sep="")
		exitValue <<- 1
	}	  
	cat(paste0(extractTableName," numberOfRows=",numberOfRows, "==", "\n"), sep="")
  
	if (exitValue != 0) {
		stop("Error reading in the extract table from the database");
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
#    print(adjacencyTable);
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
                 AND c1.areaid        = b.area_id
                 ORDER BY 1", sep = "")
    AdjRowset=doQuery(sql)
    numberOfRows <- nrow(AdjRowset)	   
  }  
  else {
    cat(paste("Unsupported port: ", db_driver_prefix, "\n"), sep="")
    exitValue <<- 1
  }
 
  
	if (exitValue != 0) {
		stop("Error getting adjacency matrix");
	}
	
  cat(paste0("rif40_GetAdjacencyMatrix numberOfRows=",numberOfRows, "==", "\n"), sep="")
  #
  # areaid               num_adjacencies adjacency_list_truncated
  # -------------------- --------------- ------------------------------------------------------------------------------------------
  # 01.001.000100.1                   18 01.001.000100.2,01.001.000200.1,01.002.000500.1,01.002.000500.4,01.002.000500.7,01.002.000
  # 01.001.000100.2                    2 01.001.000100.1,01.001.000200.1
  # 01.001.000200.1                    4 01.001.000100.1,01.001.000100.2,01.001.000300.1,01.005.002400.1
  # 01.001.000300.1                    1 01.001.000200.1
  # 01.002.000300.1                    3 01.002.000300.2,01.002.000300.3,01.002.000300.4
  # 01.002.000300.2                    4 01.002.000300.1,01.002.000300.4,01.002.000500.3,01.002.000600.2
  # 01.002.000300.3                    3 01.002.000300.1,01.002.000300.4,01.002.000300.5
  # 01.002.000300.4                    6 01.002.000300.1,01.002.000300.2,01.002.000300.3,01.002.000300.5,01.002.000400.3,01.002.000
  # 01.002.000300.5                    3 01.002.000300.3,01.002.000300.4,01.002.000400.3
  # 01.002.000400.1                    6 01.002.000400.2,01.002.000400.5,01.002.000400.7,01.002.001700.1,01.002.001700.2,01.002.001
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
				sqlDrop(connection, temporarySmoothedResultsTableName, errors = FALSE) # Ignore errors 
				  
				 if (db_driver_prefix == "jdbc:postgresql") {
						sqlSave(connection, data, tablename=temporarySmoothedResultsTableName
				#			, verbose=TRUE				# Enable save debug (1 row/tuple!)
							)
				}
				else if (db_driver_prefix == "jdbc:sqlserver") {
						ndata<-do.call(data.frame, lapply(data, function(x) {
								replace(x, is.infinite(x),NA) # Replace INF will NA for SQL Server
							}
							))
						sqlSave(connection, ndata, tablename=temporarySmoothedResultsTableName
				#			, verbose=TRUE				# Enable save debug (1 row/tuple!)
							)
				}
				  
				  #Add indices to the new table so that its join with s[study_id]_map will be more 
				  #efficient
				cat("Creating study_id index on temporary table\n")
				sqlQuery(connection, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "study_id"))
				cat("Creating area_id index on temporary table\n")
				sqlQuery(connection, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "area_id"))
				cat("Creating genders index on temporary table\n")
				sqlQuery(connection, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "genders"))
				cat("Created indices on temporary table\n")
				sqlQuery(connection, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "band_id"))
				sqlQuery(connection, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "inv_id"))
				sqlQuery(connection, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "adjusted"))
				sqlQuery(connection, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "direct_standardisation"))
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
  
  return(sqlIndexQuery);
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
## do not rows which in turn raises an Cound not SQLEXecDireect error
##
## We need to detect if the frame area_id is an integer and then add a cast
## to the Postgres/SQL Server versions
##
## Cast the maptable to same as temp table which orig. from df
## Postgres: 	a.area_id::INTEGER
## SQL Server: 	CAST(a.area_id AS INTEGER)
##
##================================================================================
  updateStmtPart1 <- paste0("SET ",
		  "genders=b.genders,",
		  "direct_standardisation=b.direct_standardisation,",
		  "adjusted=b.adjusted,",
		  "observed=b.observed,",
		  "expected=b.expected,",
		  "lower95=b.lower95,",
		  "upper95=b.upper95,",
		  "relative_risk=b.relative_risk,",
		  "smoothed_relative_risk=b.smoothed_relative_risk,",
		  "posterior_probability=b.posterior_probability,",
		  "posterior_probability_upper95=b.posterior_probability_upper95,",
		  "posterior_probability_lower95=b.posterior_probability_lower95,",
		  "residual_relative_risk=b.residual_relative_risk,",
		  "residual_rr_lower95=b.residual_rr_lower95,",
		  "residual_rr_upper95=b.residual_rr_upper95,",
		  "smoothed_smr=b.smoothed_smr,",
		  "smoothed_smr_lower95=b.smoothed_smr_lower95,",					
		  "smoothed_smr_upper95=b.smoothed_smr_upper95 ", sep="\n")
		  
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
			updateMapTableSQLQuery <- paste0("UPDATE ", mapTableName, " a\n ",	
				  updateStmtPart2)
		}
		else if (db_driver_prefix == "jdbc:sqlserver") {
			updateMapTableSQLQuery <- paste0("UPDATE ", mapTableName, " \n ",	
				  updateStmtPart2, ')')
		}
	}
	else {	
		if (db_driver_prefix == "jdbc:postgresql") {	
			if (area_id_is_integer) {
				updateMapTableSQLQuery <- paste0("UPDATE ", mapTableName, " a\n ",	
				  updateStmtPart2,
				  " AND a.area_id::INTEGER=b.area_id::INTEGER");
			}
			else {	
				updateMapTableSQLQuery <- paste0("UPDATE ", mapTableName, " a\n ",
				  updateStmtPart2,
				  " AND a.area_id=b.area_id");
			}
		}
		else if (db_driver_prefix == "jdbc:sqlserver") { 	
			if (area_id_is_integer) {
				updateMapTableSQLQuery <- paste0("UPDATE ", mapTableName, "\n ",
				  updateStmtPart2,
				  " AND CAST(a.area_id AS INTEGER)=CAST(b.area_id AS INTEGER))");
			}
			else {
				updateMapTableSQLQuery <- paste0("UPDATE ", mapTableName, "\n ",
				  updateStmtPart2,
				  " AND a.area_id=b.area_id)");
			}
		}
	}

  cat(paste0("SQL> ", updateMapTableSQLQuery, "\n"))  
  res <- tryCatch(odbcQuery(connection, updateMapTableSQLQuery, FALSE),
                  warning=function(w) {
                    cat(paste("UNABLE TO QUERY! SQL> ", updateMapTableSQLQuery, "; warning: ", w, "\n"), sep="")
                    exitValue <<- 1
                  },
                  error=function(e) {
                    cat(paste("CATCH ERROR IN QUERY! SQL> ", updateMapTableSQLQuery, 
						"; error: ", e,
						"; ODBC error: ", odbcGetErrMsg(connection), "\n"), sep="")
                    exitValue <<- 1
                  }) 
  if (res == 1) {
		cat(paste0("Updated map table: ", mapTableName, "\n"), sep="")
  }
  else if (res == -1) { # This can be no rows updated!
    cat(paste("SQL ERROR IN QUERY! SQL> ", updateMapTableSQLQuery,  
		"; error: ", odbcGetErrMsg(connection), "\n"), sep="")
    exitValue <<- 1
  }	
#  else if (res == -2) {
#    cat(paste("NO ROWS UPDATED BY QUERY! SQL> ", updateMapTableSQLQuery,
#		"; error: ", odbcGetErrMsg(connection), "\n"), sep="")
#    exitValue <<- 1
#  }	
  else {
    cat(paste("UNKNOWN ERROR IN QUERY! SQL> ", updateMapTableSQLQuery,  
		"; res: ", res,
		"; error: ", odbcGetErrMsg(connection), "\n"), sep="")
    exitValue <<- 1
  }	  
  
#	if (exitValue != 0) {
#		stop("Error updating Map Table from Smoothed Results Table");
#	}
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
			sqlDrop(connection, temporarySmoothedResultsTableName)
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
    selHomog <- paste0("select * FROM rif40.t_rif40_homogeneity WHERE inv_id =", homogData$inv_id[i]," AND study_id=", homogData$study_id[i], " and adjusted = ", as.integer(adj), " and genders = ", homogData$gender[i], sep="")
    homogExists <- tryCatch(doQuery(selHomog),
                            warning=function(w) {
                              cat(paste("ODBC UNABLE TO FETCH! ", w, "\n"), sep="")
                              exitValue <<- 1
                            },
                            error=function(e) {
                              cat(paste("ODBC ERROR FETCHING! ", geterrmessage(), "\n"), sep="")
                              exitValue <<- 1
                            })
    numberOfRows <- nrow(homogExists)
    

   if (numberOfRows == 0) {
      
      insertStmt <- paste("INSERT INTO rif40.t_rif40_homogeneity(inv_id, study_id, adjusted, genders) VALUES (",
                          homogData$inv_id[i],",",homogData$study_id[i],",",as.integer(adj),",",homogData$gender[i],");")
      res <- tryCatch(odbcQuery(connection, insertStmt, FALSE),
        error=function(e) {
        cat(paste("ODBC ERROR inserting homogeneity: ", geterrmessage(), "\n"), sep="")
        exitValue <<- 1 })
    }
    
    # Finally update the record which should now exist
    updateStmt <- paste("UPDATE rif40.t_rif40_homogeneity SET username=\'" , userID,"\', homogeneity_dof=", homogData$df[i],
                        ", homogeneity_chi2=", homogData$chisqHomog[i], ", homogeneity_p=", homogData$pValHomog[i], 
                        ", linearity_chi2=", homogData$chisqLT[i], ", linearity_p=", homogData$pValLT[i], ", explt5=",homogData$bandsLT5[i],
                        " WHERE inv_id =", homogData$inv_id[i], " AND study_id=", homogData$study_id[i], " and adjusted = ", as.integer(adj), 
                        " and genders = ", homogData$gender[i], ";\n", sep="") 
    res <- tryCatch(odbcQuery(connection, updateStmt, FALSE),
    error=function(e) {
      cat(paste("ODBC ERROR UPDATING homogenity: ", geterrmessage(), "\n"), sep="")
      exitValue <<- 1
    })
  }
  return(exitValue);
} # End of updateMapTableFromSmoothedResultsTable()


