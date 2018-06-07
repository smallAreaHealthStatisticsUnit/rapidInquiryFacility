# Title     : JDBC Handler
# Objective : Manages JDBC connections and queries for the RIF's Java/R interface.
# Created by: martin
# Created on: 31/05/2018

# Not sure whether we need both of these or, if only one, which.
require(RJDBC)
library(RJDBC)

connectToDb <- function() {

    cat(paste0("Trying JDBC connection using driver class ", db_driver_class_name, ", lib dir ",
               java_lib_path_dir, ", URL ", db_url, ", user ", userID, ", password ", password, "\n"))
    driver <- JDBC(db_driver_class_name, Sys.glob(paste0(java_lib_path_dir, "/*.jar")))
    connection <<- RJDBC::dbConnect(driver, db_url, userID, password)

    cat(paste("... JDBC connection established", "\n"))
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
        throw("JDBC Error reading in the extract table from the database");
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
        cat(paste("Unsupported DB: ", db_driver_prefix, "\n"), sep="")
        exitValue <<- 1
    }


    # if (exitValue != 0) {
    #     throw("Error getting adjacency matrix");
    # }

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
