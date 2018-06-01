# Title     : JDBC Handler
# Objective : Manages JDBC connections and queries for the RIF's Java/R interface.
# Created by: martin
# Created on: 31/05/2018

# Not sure whether we need both of these or, if only one, which.
require(RJDBC)
library(RJDBC)

dbConnect <- function() {

    cat(paste("Trying JDBC connection..."))
    driver <- JDBC(db_driver_class_name)
    connection <- dbConnect(driver, db_url, userID, password)

    cat(paste("... JDBC connection established"))
    return(connection)
}

dbDisconnect <- function(conn) {

    dbDisconnect(conn)
}

doSQLQuery <- function(sql) {

    sqlData <- tryCatch(dbGetQuery(connection, sql),
                        warning=function(w) {
                            cat(paste("UNABLE TO QUERY! SQL> ", sql, "; warning: ", w, "\n"), sep="")
                            exitValue <<- 1
                        },
                        error=function(e) {
                            cat(paste("ERROR IN QUERY! SQL> ", sql, "; error: ", odbcGetErrMsg(connDB), "\n"), sep="")
                            exitValue <<- 1
                        })
    if (is.null(nrow(sqlData))) {
        cat(paste("ERROR IN QUERY! (null data returned); SQL> ", sql, "; error: ", odbcGetErrMsg(connDB), "\n"), sep="")
        exitValue <<- 1
    }

    return(sqlData)
}

