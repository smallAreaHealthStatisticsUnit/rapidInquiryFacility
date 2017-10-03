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
############################################################################################################

##====================================================================
##FUNCTION: createDatabaseConnectionString
##DESCRIPTION: assembles pieces of database information such as
##the host, port and database name to create a database connection
##string that can be used to make an ODBC connection
##====================================================================
createDatabaseConnectionString <- function() {
  
  #print( paste0("host==", dbHost, "=="))
  #print( paste0("port==", dbPort, "=="))
  #print( paste0("datatabase name==", dbName, "=="))
  
  return(paste0(dbHost, ":", dbPort, "/", dbName))
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
  
  print(paste0("EXTRACT TABLE NAME: ", extractTableName))
  
  data=tryCatch(sqlFetch(connDB, extractTableName),
		warning=function(w) {
		  print(paste("UNABLE TO FETCH! ", w))
		  exitValue <<- 1
		},
		error=function(e) {
		  print(paste("ERROR FETCHING! ", geterrmessage()))
		  exitValue <<- 1
		})	
  
#
# Save extract data frame to file
#
	if (dumpFramesToCsv == TRUE) {
		print(paste0("Saving extract frame to: ", temporaryExtractFileName))
		write.csv(data, file=temporaryExtractFileName)
	}
	numberOfRows <- nrow(data)	
	if (is.null(nrow(data))) {
		print(paste("ERROR IN FETCH! (null data returned): ", extractTableName, ", error: ", odbcGetErrMsg(connDB)))
		exitValue <<- 1
	}	  
	print(paste0(extractTableName," numberOfRows=",numberOfRows, "=="))
  
	if (exitValue != 0) {
		throw("Error reading in the extract table from the database");
	}
	
	return(data)
}

##====================================================================
##FUNCTION:    doSQLQuery()
##PARAMETER:   SQL 
##RETURNS:     Data frame
##DESCRIPTION: Run SQL query. On error/warning/NULL data; print SQL, error and exit
##====================================================================
doSQLQuery <- function(sql) {
  sqlData <- tryCatch(sqlQuery(connDB, sql, FALSE),
                      warning=function(w) {
                        print(paste("UNABLE TO QUERY! SQL> ", sql, "; warning: ", w))
                        exitValue <<- 1
                      },
                      error=function(e) {
                        print(paste("ERROR IN QUERY! SQL> ", sql, "; error: ", odbcGetErrMsg(connDB)))
                        exitValue <<- 1
                      })
  if (is.null(nrow(sqlData))) {
    print(paste("ERROR IN QUERY! (null data returned); SQL> ", sql, "; error: ", odbcGetErrMsg(connDB)))
    exitValue <<- 1
  } 
  
  return(sqlData)	  
}

saveDataFrameToDatabaseTable <- function(data) {

  #
  # Save data frame to file
  # 
  if (dumpFramesToCsv == TRUE) {
	  print(paste0("Saving data frame to: ", temporarySmoothedResultsFileName))
	  write.csv(data, file=temporarySmoothedResultsFileName) 
  }
  #
  # Save data frame to table
  #
  print(paste0("Creating temporary table: ", temporarySmoothedResultsTableName))
  sqlDrop(connDB, temporarySmoothedResultsTableName, errors = FALSE) # Ignore errors 
  
  if (db_driver_prefix == "jdbc:postgresql") {
		sqlSave(connDB, data, tablename=temporarySmoothedResultsTableName
#			, verbose=TRUE				# Enable save debug (1 row/tuple!)
			)
  }
  else if (db_driver_prefix == "jdbc:sqlserver") {
		ndata<-do.call(data.frame, lapply(data, function(x) {
				replace(x, is.infinite(x),NA) # Replace INF will NA for SQL Server
			}
			))
		sqlSave(connDB, ndata, tablename=temporarySmoothedResultsTableName
#			, verbose=TRUE				# Enable save debug (1 row/tuple!)
			)
  }
  
  #Add indices to the new table so that its join with s[study_id]_map will be more 
  #efficient
  print("Creating study_id index on temporary table")
  sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "study_id"))
  print("Creating area_id index on temporary table")
  sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "area_id"))
  print("Creating genders index on temporary table")
  sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "genders"))
  print("Created indices on temporary table")
  #sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "band_id"))
  #sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "inv_id"))
  #sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "adjusted"))
  #sqlQuery(connDB, generateTableIndexSQLQuery(temporarySmoothedResultsTableName, "direct_standardisation"))
}

generateTableIndexSQLQuery <- function(tableName, columnName) {
  sqlIndexQuery <- paste0(
    "CREATE INDEX ind22_",
    columnName,
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
updateMapTableFromSmoothedResultsTable <- function(area_id_is_integer) {
  
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
  updateStmt <- paste0("SET ",
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
		  "smoothed_smr_upper95=b.smoothed_smr_upper95 ")
  if (db_driver_prefix == "jdbc:postgresql") {	
	if (area_id_is_integer) {
		updateMapTableSQLQuery <- paste0("UPDATE ", mapTableName, " a ",	  
		  updateStmt,
		  "FROM ",
		  temporarySmoothedResultsTableName,
		  " b ",
		  "WHERE ",
		  "a.study_id=b.study_id AND ",
		  "a.band_id=b.band_id AND ",
		  "a.inv_id=b.inv_id AND ",
		  "a.genders=b.genders AND ",
		  "a.area_id::INTEGER=b.area_id::INTEGER");
	}
	else {	
		updateMapTableSQLQuery <- paste0("UPDATE ", mapTableName, " a ",
		  updateStmt,
		  "FROM ",
		  temporarySmoothedResultsTableName,
		  " b ",
		  "WHERE ",
		  "a.study_id=b.study_id AND ",
		  "a.band_id=b.band_id AND ",
		  "a.inv_id=b.inv_id AND ",
		  "a.genders=b.genders AND ",
		  "a.area_id=b.area_id");
	}
  }
  else if (db_driver_prefix == "jdbc:sqlserver") { 	
	if (area_id_is_integer) {
		updateMapTableSQLQuery <- paste0("UPDATE ", mapTableName, " ",
		 
		  updateStmt,
		  "FROM ", mapTableName, " AS a INNER JOIN ",
		  temporarySmoothedResultsTableName, " AS b ",
		  "ON (",
		  "a.study_id=b.study_id AND ",
		  "a.band_id=b.band_id AND ",
		  "a.inv_id=b.inv_id AND ",
		  "a.genders=b.genders AND ",
		  "CAST(a.area_id AS INTEGER)=CAST(b.area_id AS INTEGER))");
	}
	else {
		updateMapTableSQLQuery <- paste0("UPDATE ", mapTableName, " ",
		  updateStmt,
		  "FROM ", mapTableName, " AS a INNER JOIN ",
		  temporarySmoothedResultsTableName, " AS b ",
		  "ON (",
		  "a.study_id=b.study_id AND ",
		  "a.band_id=b.band_id AND ",
		  "a.inv_id=b.inv_id AND ",
		  "a.genders=b.genders AND ",
		  "a.area_id=b.area_id)");
	}
  }
  
  res <- tryCatch(odbcQuery(connDB, updateMapTableSQLQuery, FALSE),
                  warning=function(w) {
                    print(paste("UNABLE TO QUERY! SQL> ", updateMapTableSQLQuery, "; warning: ", w))
                    exitValue <<- 1
                  },
                  error=function(e) {
                    print(paste("CATCH ERROR IN QUERY! SQL> ", updateMapTableSQLQuery, 
						"; error: ", e,
						"; ODBC error: ", odbcGetErrMsg(connDB)))
                    exitValue <<- 1
                  }) 
  if (res == 1) {
 #  print(updateMapTableSQLQuery)
		print(paste0("Updated map table: ", mapTableName))
  }
  else if (res == -1) { # This can be no rows updated!
    print(paste("SQL ERROR IN QUERY! SQL> ", updateMapTableSQLQuery,  
		"; error: ", odbcGetErrMsg(connDB)))
    exitValue <<- 1
  }	
#  else if (res == -2) {
#    print(paste("NO ROWS UPDATED BY QUERY! SQL> ", updateMapTableSQLQuery,
#		"; error: ", odbcGetErrMsg(connDB)))
#    exitValue <<- 1
#  }	
  else {
    print(paste("UNKNOWN ERROR IN QUERY! SQL> ", updateMapTableSQLQuery,  
		"; res: ", res,
		"; error: ", odbcGetErrMsg(connDB)))
    exitValue <<- 1
  }	  
  
	if (exitValue != 0) {
		throw("Error updating Map Table from Smoothed Results Table");
	}
} # End of updateMapTableFromSmoothedResultsTable()

##================================================================================
##FUNCTION: dbConnect
##DESCRIPTION
##Connect to ODBC database
##Set (exitvalue) 0 on success, 1 on failure 
##================================================================================
dbConnect <- function() {
	print(paste0("Connect to database: ", odbcDataSource))
  
	errorTrace<-capture.output({
		tryCatch({
			connDB <<- odbcConnect(odbcDataSource, uid=as.character(userID), pwd=as.character(password))
		},
        warning=function(w) {
            print(paste("UNABLE TO CONNECT! ", w))
            exitValue <<- 0
        },
        error=function(e) {
            print(paste("ERROR CONNECTING! ", geterrmessage()))
            exitValue <<- 1
        },
		finally={
			odbcSetAutoCommit(connDB, autoCommit = FALSE)
			print(odbcGetInfo(connDB))
#
# Run rif40_startup() procedure on Postgres
#
			if (db_driver_prefix == "jdbc:postgresql") {
				sql <- "SELECT rif40_sql_pkg.rif40_startup()"
				doSQLQuery(sql)
			}
		}) # End of tryCatch
	})
	
	return(connDB);
}

##================================================================================
##FUNCTION: dbDisConnect
##DESCRIPTION
##Disconnect from ODBC database
##Set (exitvalue) 0 on success, 1 on failure 
##================================================================================
dbDisConnect <- function() {
		print("Closing database connection")
		odbcEndTran(connDB, commit = TRUE)
		odbcClose(connDB)
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
    AdjRowset=doSQLQuery(sql)
    numberOfRows <- nrow(AdjRowset)	
  }
  else if (db_driver_prefix == "jdbc:sqlserver") {
    sql <- paste("SELECT b2.adjacencytable
                 FROM [rif40].[rif40_studies] b1, [rif40].[rif40_geographies] b2
                 WHERE b1.study_id  = ", studyID ,"   
                 AND b2.geography = b1.geography");
    adjacencyTableRes=doSQLQuery(sql)
    numberOfRows <- nrow(adjacencyTableRes)
    if (numberOfRows != 1) {
      print(paste("Expected 1 row; got: " + numberOfRows + "; SQL> ", sql))
      exitValue <<- 1
    }	
    adjacencyTable <- tolower(adjacencyTableRes$adjacencytable[1])	
    print(adjacencyTable);
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
    AdjRowset=doSQLQuery(sql)
    numberOfRows <- nrow(AdjRowset)	   
  }  
  else {
    print(paste("Unsupported port: ", db_driver_prefix))
    exitValue <<- 1
  }
 
  
	if (exitValue != 0) {
		throw("Error getting adjacency matrix");
	}
	
  print(paste0("rif40_GetAdjacencyMatrix numberOfRows=",numberOfRows, "=="))
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
  #   print(head(AdjRowset, n=10))
  
  #
# Save extract data frame to file
#
	if (dumpFramesToCsv == TRUE) {
		print(paste0("Saving adjacency matrix to: ", adjacencyMatrixFileName))
		write.csv(AdjRowset, file=adjacencyMatrixFileName)
	}
	
  return(AdjRowset);
}