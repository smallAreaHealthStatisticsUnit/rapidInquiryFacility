/####
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
 ##/

############################################################################################################
#   RIF PROJECT
#   Basic disease map results
#   Adjusted for covariates
#   Apply Bayesian smoothing with INLA
############################################################################################################

## CHECK & AUTO INSTALL MISSING PACKAGES
#packages <- c("plyr", "abind", "maptools", "spdep", "RODBC")
#if (length(setdiff(packages, rownames(installed.packages()))) > 0) {
#  install.packages(setdiff(packages, rownames(installed.packages())))  
#}
#if (!require(INLA)) {
#	install.packages("INLA", repos="https://www.math.ntnu.no/inla/R/stable")
#}

#TODO: (DM) Run if no optional model selected 

library(plyr)
library(abind)
library(INLA)
library(maptools)
library(spdep)
#library(Matrix)
library(RODBC) #will need db libraries as well if we decide to use db specific packages e.g. RSQLServer and RPostgreSQL
rm(list=ls()) 
##====================================================================
# SCRIPT VARIABLES
##====================================================================
#Variables that hold database connectivity information.  For now, we
#will rely on named ODBC sources but in future the script should be 
#altered to use host:port/databaseName
userID <- ""
password <- ""
dbName <- "rif_studies"
dbHost <- ""
dbPort <- ""
dbConnectionString <- ""
odbcDataSource <- "networkRif"
numberOfInvestigations <- ""

#The identifier of the study whose extract table fields need to be smoothed.
studyID <- "1"

#We expect model to have one of the three values: 'BYM', 'CAR' or 'HET'
model <- "BYM"

#Adjust for other covariate(s) or not
adj=FALSE #either true or false

#The name of the extract table that is created by the middleware for a given
#study.  It is of the format rif_studies.s[study_id]_extract
extractTableName <- ""

#The name of the skeleton table created by the RIF middleware to hold smoothed results
#for a given study.  Initially the values for smoothed columns will be empty.  It is 
#of the format rif_studies.s[study_id]_map 
mapTableName <- ""

#The name of the temporary table that this script uses to hold the data frame
#containing smoothed results.  It should have a 1:1 correspondence between
#its fields and fields that appear in the map table skeleton.  
temporarySmoothedResultsTableName <- ""

#The name of the investigation. Is an input parameter, but default is set here for debug purposes
investigationName <- "inv_1"
#The id of the investigation - used when writing the results back to the database. Input paremeter
investigationId <- "1"

#name of adjustment (covariate) variable (except age group and sex). 
#todo add more adjustment variables and test the capabilities. 
names.adj<-c('ses')


##====================================================================
## FUNCTION: processCommandLineArguments
## DESCRIPTION: parses the command line arguments and returns a data
## frame that has two named columns: name and value
##====================================================================
processCommandLineArguments <- function() {
  
  allArgs <- commandArgs(trailingOnly=TRUE)
  numCommandLineArgs <- length(allArgs)
  
  refinedList <- list()
  
  if (numCommandLineArgs==0) {
    print("No arguments supplied")
  }else{
    print("Arguments were supplied")
    for (i in 1:numCommandLineArgs) {
      ##Filter command arguments that fit the form
      ##--X=Y
      if (grepl('^--', allArgs[i]) == TRUE) {
        refinedList <- c(refinedList, allArgs[i])
      }
    }
    parsedNameValuePairs <- strsplit(sub("^--", "", refinedList), "=")
    parametersDataFrame <- as.data.frame(do.call("rbind", parsedNameValuePairs))
    names(parametersDataFrame)[1] <- paste("name")
    names(parametersDataFrame)[2] <- paste("value")	
    
    print("Parsing parameters")
    for (i in 1:nrow(parametersDataFrame)) {
      print(parametersDataFrame[i,1])
      
      if (grepl('user_id', parametersDataFrame[i, 1]) == TRUE) {
        userID <<- parametersDataFrame[i, 2]
      } else if (grepl('password', parametersDataFrame[i, 1]) == TRUE){
        password <<- parametersDataFrame[i, 2]
      } else if (grepl('db_name', parametersDataFrame[i, 1]) == TRUE){
        dbName <<- parametersDataFrame[i, 2]
      } else if (grepl('db_host', parametersDataFrame[i, 1]) == TRUE){
        dbHost <<- parametersDataFrame[i, 2]
      } else if (grepl('db_port', parametersDataFrame[i, 1]) == TRUE){
        dbPort <<- parametersDataFrame[i, 2]	
      } else if (grepl('study_id', parametersDataFrame[i, 1]) == TRUE){
        studyID <<- parametersDataFrame[i, 2]
      } else if (grepl('num_investigations', parametersDataFrame[i, 1]) == TRUE){
        numberOfInvestigations <<- parametersDataFrame[i, 2]
      } else if (grepl('investigation_name', parametersDataFrame[i, 1]) == TRUE){
        investigationName <<- parametersDataFrame[i, 2]
      } else if (grepl('investigation_id', parametersDataFrame[i, 1]) == TRUE){
        investigationId <<- parametersDataFrame[i, 2]
      } else if (grepl('odbc_data_source', parametersDataFrame[i, 1]) == TRUE){
        odbcDataSource <<- parametersDataFrame[i, 2]
      } else if (grepl('models', parametersDataFrame[i, 1]) == TRUE){
        model <<- parametersDataFrame[i, 2]				
      }	else if (grepl('adj', parametersDataFrame[i, 1]) == TRUE){
      if (parametersDataFrame[i, 2] == 'TRUE') {
          adj <<- TRUE
        }
      } else if (grepl('covariate_name', parametersDataFrame[i, 1]) == TRUE){
        names.adj <<- tolower(parametersDataFrame[i, 2])
      }
    }
    
    return(parametersDataFrame)
  }
}



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
establishTableNames <-function(vstudyID) {
  print(paste("Study ID:", vstudyID))
  extractTableName <<- paste0("rif_studies.s", vstudyID, "_extract")
  #temporarySmoothedResultsTableName <<-paste("rif_studies.tmp_s", vstudyID, "_map", sep="")
  temporarySmoothedResultsTableName <<-paste("kgarwood.tmp_s", vstudyID, "_map", sep="")
  mapTableName <<- paste0("rif_studies.s", vstudyID, "_map")
}


##====================================================================
##FUNCTION: createSmoothedExtractResults
##DESCRIPTION: assembles pieces of database information such as
##the host, port and database name to create a database connection
##string that can be used to make an ODBC connection
##====================================================================
performSmoothingActivity <- function() {
  
  
  #Part I: Read in the extract table
  #=================================
  #Read original extract table data into a data frame
  print("============EXTRACT TABLE NAME ====================")
  print(extractTableName)
  print("============EXTRACT TABLE NAME ====================")
  #extract the relevant Study data
  data=sqlFetch(connDB, extractTableName)
  # Get the adjacency matrix from the db
  #data=read.table('sahsuland_example_extract.csv',header=TRUE,sep=',')
  
  numberOfRows <- nrow(data)	
  
  print(paste0(extractTableName," numberOfRows=",numberOfRows, "=="))
  
  if (investigationName != "inv_1"){
    #add a column (inv_1) to data and populate is with InvestigationName data for simplicity
    inv_1 = 0;
    data = cbind(data, inv_1)
    # find the relevant column in the data
    #first need to ignore the case of the investigation names
    ColNames = toupper(names(data))
    invcol =  which(ColNames==toupper(investigationName))
    if (length(invcol)==0){
      print(paste('The column defined by the investigation_name parameter: ', investigationName, 'not found in data table. Data assumed to be all zero!'))
    }
    else{
      #copy to the new inv_1 column
      data$inv_1 = data[,invcol]
    }
  }
  #originalExtractTable$area_id <- as.character(originalExtractTable$area_id)
  #AdjRowset<-sqlQuery(connDB, paste("SELECT * FROM rif40_xml_pkg.rif40_GetAdjacencyMatrix(1) LIMIT 10"))
  # TODO: ensure it's ok hardcode rif40_xml_pkg here rather than pass it in as a parameter
  # This SQL statement will possibly have to be changed to work with SQL
  AdjRowset<-sqlQuery(connDB, paste("SELECT * FROM rif40_xml_pkg.rif40_GetAdjacencyMatrix(", studyID, ")"))
  numberOfRows <- nrow(AdjRowset)	
  
  print(paste0("rif40_GetAdjacencyMatrix numberOfRows=",numberOfRows, "=="))
  
  #Part II: Perform smoothing operation
  #====================================	
  #Perform smoothing operation.
  
  #convert nas in inv_1 column to zeros
  data$inv_1[which(is.na(data$inv_1))]=0
  #ensure area_id is stored as char
  data$area_id=as.character(data$area_id)
  #split comparisons and study records - comparisons held in comp datafram
  comp=data[which(data$study_or_comparison=='C'),]
  data=data[which(data$study_or_comparison=='S'),]
  
  data=data[which(is.na(data$band_id)==FALSE),]

  # Section checking names of adjustment variables that should be passed in as paremeters
  #Find corresponding columns in data and comp
  i.d.adj=c()
  i.c.adj=c()
  for (i in 1:length(names.adj)){
    id=which(tolower(names(data))==names.adj[i])
    ic=which(tolower(names(comp))==names.adj[i])
    if (length(id)==0|length(ic)==0){
      print(paste('covariate', names.adj[i], 'not found, data will not be adjusted for it!'))
    }else{
      i.d.adj=c(i.d.adj,id)
      i.c.adj=c(i.c.adj,ic)
    }
  }
  ncov=length(i.d.adj)
  for (i in 1:ncov){
    data[,i.d.adj[i]]=findNULL(data[,i.d.adj[i]])
    comp[,i.c.adj[i]]=findNULL(comp[,i.c.adj[i]])
    data[,i.d.adj[i]]=as.numeric(as.character(data[,i.d.adj[i]]))
    comp[,i.c.adj[i]]=as.numeric(as.character(comp[,i.c.adj[i]])) 
  }
  #Result table
  RES=ddply(data,.variables=c('area_id','sex'),.fun=function(x){
    study_or_comparison=x$study_or_comparison[1]
    study_id=x$study_id[1]
    area_id=x$area_id[1]
    band_id=x$band_id[1]
    gender=x$sex[1]
    observed=sum(x$inv_1)
    return(data.frame(study_or_comparison,study_id,area_id,band_id,gender,observed))})
  RES=RES[,-1]#We delete the sex column
  RES2=ddply(RES,.variables='area_id',.fun=function(x){
    study_or_comparison=x$study_or_comparison[1]
    study_id=x$study_id[1]
    area_id=x$area_id[1]
    band_id=x$band_id[1]
    gender=3
    observed=sum(x$observed)
    return(data.frame(study_or_comparison,study_id,area_id,band_id,gender,observed))})
  RES=rbind(RES,RES2)
  RES=RES[order(RES$area_id, RES$gender),]
  
  #COPY OF INV_1 and total_pop fields in a array of dim 4
  #It is necessary that data stay order by year, area, sex and age_group
  data=data[order(data$year,data$area_id,data$sex,data$age_group),]
  
  # dim1=age, dim2=sex, dim3=area, dim4=years
  CASES=array(data$inv_1,dim=c(length(unique(data$age_group)),length(unique(data$sex)),length(unique(data$area_id)),length(unique(data$year))))
  CASES=apply(CASES,MARGIN=c(1,2,3),FUN=sum) #Mean over the years
  CASES=abind(CASES,apply(CASES,MARGIN=c(1,3),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
  
  POP=array(data$total_pop,dim=c(length(unique(data$age_group)),length(unique(data$sex)),length(unique(data$area_id)),length(unique(data$year))))
  POP=apply(POP,MARGIN=c(1,2,3),FUN=sum) #Mean over the years
  POP=abind(POP,apply(POP,MARGIN=c(1,3),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
  
  RATES=CASES/POP
  
  
  #from data, determine the total number of adjustement strata (all possible combination of adjustement variables)
  ADJ=matrix(NA,nrow=length(unique(data$area_id)),ncol=ncov)
  for (i in 1:ncov){
    ADJ[,i]=array(data[,i.d.adj[i]],dim=c(length(unique(data$age_group)),length(unique(data$sex)),length(unique(data$area_id)),length(unique(data$year))))[1,1,,1]
  }
  
  concADJ=apply(ADJ,MARGIN=1,FUN=conc)
  
  #ADJcomb is a vector with all encoutered adjustement stratas, m the number of stratas
  ADJcomb=unique(c(concADJ),)
  ADJcomb=ADJcomb[order(ADJcomb)]
  m=length(ADJcomb)
  
  #Make comparative variables in a array of dim: age-sex-area-year-adjustement
  #increase comp to compComplete so that there is one row for each combination of 
  #age-sex-area-year-adjustement 
  # BP- the area field in the comparison rows should NOT be used. Just need to match comparison rows by area, sex, year and ADJcomb
  compComplete=expand.grid(unique(comp$age_group),unique(comp$sex),unique(comp$area_id),unique(comp$year),ADJcomb)
  #compComplete=expand.grid(unique(comp$age_group),unique(comp$sex),unique(comp$year),ADJcomb)
  compComplete=as.data.frame(compComplete)
  names(compComplete)=c('age_group','sex','area_id','year','comb')
  #names(compComplete)=c('age_group','sex','year','comb')
  compComplete$comb=as.character(compComplete$comb)
  comp$comb=apply(as.matrix(comp[,i.c.adj],ncol=ncov),MARGIN=1,FUN=conc)
  compComplete=merge(compComplete, comp,by=c('age_group','sex','area_id','year','comb'),all.x=TRUE)
  #compComplete=merge(compComplete, comp,by=c('age_group','sex','year','comb'),all.x=TRUE)
  
  #Fill the array for comparative areas
  compComplete=compComplete[order(compComplete$comb,compComplete$year,compComplete$area_id,compComplete$sex,compComplete$age_group),]
  cCASES=array(compComplete$inv_1,dim=c(length(unique(compComplete$age_group)),length(unique(compComplete$sex)),length(unique(compComplete$area_id)),length(unique(compComplete$year)),m))
  
  #cCASES=apply(cCASES,MARGIN=c(1,2,3,5),FUN=sum) #Mean over the years -
  cCASES=apply(cCASES,MARGIN=c(1,2,3,5),FUN=sum) #Mean over the years -
  cCASESNoArea=apply(cCASES,MARGIN=c(1,2,4),FUN=sum,na.rm=TRUE) #Mean over the areas
  
  cCASES=abind(cCASES,apply(cCASES,MARGIN=c(1,3,4),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
  cCASESNoArea=abind(cCASESNoArea,apply(cCASESNoArea,MARGIN=c(1,3),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
  
  cPOP=array(compComplete$total_pop,dim=c(length(unique(compComplete$age_group)),length(unique(compComplete$sex)),length(unique(compComplete$area_id)),length(unique(compComplete$year)),m))
  
  cPOP=apply(cPOP,MARGIN=c(1,2,3,5),FUN=sum) #Mean over the years
  cPOPNoArea=apply(cPOP,MARGIN=c(1,2,4),FUN=sum,na.rm=TRUE) #Mean over the areas
  
  cPOP=abind(cPOP,apply(cPOP,MARGIN=c(1,3,4),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
  cPOPNoArea=abind(cPOPNoArea,apply(cPOPNoArea,MARGIN=c(1,3),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
  
  #unadjusted rates
  cRATES=apply(cCASES,MARGIN=c(1,2,3),FUN=sum,na.rm=TRUE)/apply(cPOP,MARGIN=c(1,2,3),FUN=sum,na.rm=TRUE)
  cRATESNoArea=apply(cCASESNoArea,MARGIN=c(1,2),FUN=sum,na.rm=TRUE)/apply(cPOPNoArea,MARGIN=c(1,2),FUN=sum,na.rm=TRUE)
  
  #adjusted rates
  cADJRATES=cCASES/cPOP
  cADJRATESNoArea=cCASESNoArea/cPOPNoArea
  
  #For checking (can be removed later)
  # plot(apply(cCASES[,3,,1],MARGIN=1,FUN=sum,na.rm=TRUE)/apply(cPOP[,3,,1],MARGIN=1,FUN=sum,na.rm=TRUE),type='l',ylab='rates')
  # lines(apply(cCASES[,3,,2],MARGIN=1,FUN=sum,na.rm=TRUE)/apply(cPOP[,3,,2],MARGIN=1,FUN=sum,na.rm=TRUE),col='blue')
  # lines(apply(cCASES[,3,,3],MARGIN=1,FUN=sum,na.rm=TRUE)/apply(cPOP[,3,,3],MARGIN=1,FUN=sum,na.rm=TRUE),col='red')
  # lines(apply(cCASES[,3,,4],MARGIN=1,FUN=sum,na.rm=TRUE)/apply(cPOP[,3,,4],MARGIN=1,FUN=sum,na.rm=TRUE),col='darkgreen')
  # lines(apply(cCASES[,3,,5],MARGIN=1,FUN=sum,na.rm=TRUE)/apply(cPOP[,3,,5],MARGIN=1,FUN=sum,na.rm=TRUE),col='orange')
  
  #link between study region and counts
  #Creates and array of the area ids
  S_area_id=array(data$area_id,dim=c(length(unique(data$age_group)),length(unique(data$sex)),length(unique(data$area_id)),length(unique(data$year))))[1,1,,1]
  C_area_id=array(compComplete$area_id,dim=c(length(unique(compComplete$age_group)),length(unique(compComplete$sex)),length(unique(compComplete$area_id)),length(unique(compComplete$year)),m))
  c_area_id=c()
  for (i in 1:length(unique(compComplete$area_id))){
    c_area_id=c(c_area_id,C_area_id[,,i,,][which(is.na(C_area_id[,,i,,])==FALSE)][1]) 
  }
  C_area_id=c_area_id
  #StoC=sapply(S_area_id,FUN=function(x){which(C_area_id==substr(x,1,6))})
  StoC=sapply(S_area_id,FUN=function(x){which(C_area_id==x)})
  StoCcomp=sapply(concADJ,FUN=function(x){which(ADJcomb==x)})
  
  #Random checks - I think these lines of code are here to throw errors is there's something wrong with the data...
  #ss=sample(1:length(S_area_id), 3)
  #cbind(S_area_id[ss],C_area_id[StoC[ss]])
  #cbind(concADJ[ss],ADJcomb[StoCcomp[ss]])
  
  ###NON ADJUSTED
  #Expected number of cases non adjusted
  RES$EXP_UNADJ=NA
  #RES$EXP_UNADJ[which(RES$gender==1)]=apply(POP[,1,]*cRATES[,1,StoC],MARGIN=2,FUN=sum)
  #RES$EXP_UNADJ[which(RES$gender==2)]=apply(POP[,2,]*cRATES[,2,StoC],MARGIN=2,FUN=sum)
  #RES$EXP_UNADJ[which(RES$gender==3)]=apply(POP[,3,]*cRATES[,3,StoC],MARGIN=2,FUN=sum)
  RES$EXP_UNADJ[which(RES$gender==1)]=apply(POP[,1,]*cRATESNoArea[,1],MARGIN=2,FUN=sum, na.rm = TRUE)
  RES$EXP_UNADJ[which(RES$gender==2)]=apply(POP[,2,]*cRATESNoArea[,2],MARGIN=2,FUN=sum, na.rm = TRUE)
  RES$EXP_UNADJ[which(RES$gender==3)]=apply(POP[,3,]*cRATESNoArea[,3],MARGIN=2,FUN=sum, na.rm = TRUE)
  #RES[1:100,]
  
  #Relative Risk non adjusted
  RES$RR_UNADJ=NA
  RES$RR_UNADJ[which(RES$gender==1)]=colSums(CASES[,1,], na.rm = TRUE)/RES$EXP_UNADJ[which(RES$gender==1)]
  RES$RR_UNADJ[which(RES$gender==2)]=colSums(CASES[,2,], na.rm = TRUE)/RES$EXP_UNADJ[which(RES$gender==2)]
  RES$RR_UNADJ[which(RES$gender==3)]=colSums(CASES[,3,], na.rm = TRUE)/RES$EXP_UNADJ[which(RES$gender==3)]
  #convert NA's to zeros for now
  RES$RR_UNADJ[which(is.na(RES$RR_UNADJ))]=0
  #RES[1:100,]
  
  #Lower 95 percent interval non adjusted
  RES$RRL95_UNADJ=NA
  RES$RRL95_UNADJ[which(RES$gender==1)]=apply(cbind(colSums(CASES[,1,]),RES$EXP_UNADJ[which(RES$gender==1)]),MARGIN=1,
                                              FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(-1.96*sqrt(1/x[1])))
                                              }else{return(0.5*qchisq(0.025,2*x[1])/x[2])}})
  RES$RRL95_UNADJ[which(RES$gender==2)]=apply(cbind(colSums(CASES[,2,]),RES$EXP_UNADJ[which(RES$gender==2)]),MARGIN=1,
                                              FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(-1.96*sqrt(1/x[1])))
                                              }else{return(0.5*qchisq(0.025,2*x[1])/x[2])}})
  RES$RRL95_UNADJ[which(RES$gender==3)]=apply(cbind(colSums(CASES[,3,]),RES$EXP_UNADJ[which(RES$gender==3)]),MARGIN=1,
                                              FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(-1.96*sqrt(1/x[1])))
                                              }else{return(0.5*qchisq(0.025,2*x[1])/x[2])}})
  RES$RRL95_UNADJ[which(is.na(RES$RRL95_UNADJ))]=0
  
  #Upper 95 percent interval non adjusted
  RES$RRU95_UNADJ=NA
  RES$RRU95_UNADJ[which(RES$gender==1)]=apply(cbind(colSums(CASES[,1,]),RES$EXP_UNADJ[which(RES$gender==1)]),MARGIN=1,
                                              FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(1.96*sqrt(1/x[1])))
                                              }else{return(0.5*qchisq(0.975,2*x[1]+2)/x[2])}})
  RES$RRU95_UNADJ[which(RES$gender==2)]=apply(cbind(colSums(CASES[,2,]),RES$EXP_UNADJ[which(RES$gender==2)]),MARGIN=1,
                                              FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(1.96*sqrt(1/x[1])))
                                              }else{return(0.5*qchisq(0.975,2*x[1]+2)/x[2])}})
  RES$RRU95_UNADJ[which(RES$gender==3)]=apply(cbind(colSums(CASES[,3,]),RES$EXP_UNADJ[which(RES$gender==3)]),MARGIN=1,
                                              FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(1.96*sqrt(1/x[1])))
                                              }else{return(0.5*qchisq(0.975,2*x[1]+2)/x[2])}})
  RES$RRU95_UNADJ[which(is.na(RES$RRU95_UNADJ))]=0
  
  #Rate non adjusted
  RES$RATE_UNADJ=NA
  cPOP3d=apply(cPOP,MARGIN=c(1,2,3),FUN=sum,na.rm=TRUE)
  cPOP3dNoArea=apply(cPOPNoArea,MARGIN=c(1,2),FUN=sum,na.rm=TRUE)
  
  #  RES$RATE_UNADJ[which(RES$gender==1)]=colSums(RATES[,1,]*cPOP3d[,1,StoC])/colSums(cPOP3d[,1,StoC])*100000
#  RES$RATE_UNADJ[which(RES$gender==2)]=colSums(RATES[,2,]*cPOP3d[,2,StoC])/colSums(cPOP3d[,2,StoC])*100000
#  RES$RATE_UNADJ[which(RES$gender==3)]=colSums(RATES[,3,]*cPOP3d[,3,StoC])/colSums(cPOP3d[,3,StoC])*100000
  RES$RATE_UNADJ[which(RES$gender==1)]=colSums(RATES[,1,]*cPOP3dNoArea[,1])/sum(cPOP3dNoArea[,1])*100000
  RES$RATE_UNADJ[which(RES$gender==2)]=colSums(RATES[,2,]*cPOP3dNoArea[,2])/sum(cPOP3dNoArea[,2])*100000
  RES$RATE_UNADJ[which(RES$gender==3)]=colSums(RATES[,3,]*cPOP3dNoArea[,3])/sum(cPOP3dNoArea[,3])*100000
  #RES[1:100,2:11]
  RES$RATE_UNADJ[which(is.na(RES$RATE_UNADJ))]=0
  
  #Rate Confidence interval non adjusted
  #SElog1=sqrt(colSums(cPOP3d[,1,StoC]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(cPOP3d[,1,StoC]*RATES[,1,])
  #SElog2=sqrt(colSums(cPOP3d[,2,StoC]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(cPOP3d[,2,StoC]*RATES[,2,])
  #SElog3=sqrt(colSums(cPOP3d[,3,StoC]^2*RATES[,3,]*(1-RATES[,3,])/POP[,3,]))/colSums(cPOP3d[,3,StoC]*RATES[,3,])
  SElog1=sqrt(colSums(cPOP3dNoArea[,1]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/sum(cPOP3dNoArea[,1]*RATES[,1,])
  SElog2=sqrt(colSums(cPOP3dNoArea[,2]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/sum(cPOP3dNoArea[,2]*RATES[,2,])
  SElog3=sqrt(colSums(cPOP3dNoArea[,3]^2*RATES[,3,]*(1-RATES[,3,])/POP[,3,]))/sum(cPOP3dNoArea[,3]*RATES[,3,])
  
  #SE1=sqrt(colSums(cPOP3d[,1,StoC]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(cPOP3d[,1,StoC])*100000
  #SE2=sqrt(colSums(cPOP3d[,2,StoC]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(cPOP3d[,2,StoC])*100000
  #SE3=sqrt(colSums(cPOP3d[,3,StoC]^2*RATES[,1,]*(1-RATES[,3,])/POP[,3,]))/colSums(cPOP3d[,3,StoC])*100000
  SE1=sqrt(colSums(cPOP3dNoArea[,1]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/sum(cPOP3dNoArea[,1])*100000
  SE2=sqrt(colSums(cPOP3dNoArea[,2]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/sum(cPOP3dNoArea[,2])*100000
  SE3=sqrt(colSums(cPOP3dNoArea[,3]^2*RATES[,1,]*(1-RATES[,3,])/POP[,3,]))/sum(cPOP3dNoArea[,3])*100000
  
  #Lower 95% percent rate
  RES$RATEL95_UNADJ=NA
  RES$RATEL95_UNADJ[which(RES$gender==1)]=apply(cbind(RES$RATE_UNADJ[which(RES$gender==1)],SElog1,SE1,colSums(CASES[,1,])),MARGIN=1,
                                                FUN=function(x){if(x[4]>100){return(x[1]*exp(-1.96*x[2]))
                                                }else{return(x[1]+(x[3]/sqrt(x[4])*(0.5*qchisq(0.025,2*x[4])-x[4])))}})                                                                                                      
  RES$RATEL95_UNADJ[which(RES$gender==2)]=apply(cbind(RES$RATE_UNADJ[which(RES$gender==2)],SElog2,SE2,colSums(CASES[,2,])),MARGIN=1,
                                                FUN=function(x){if(x[4]>100){return(x[1]*exp(-1.96*x[2]))
                                                }else{return(x[1]+(x[3]/sqrt(x[4])*(0.5*qchisq(0.025,2*x[4])-x[4])))}})   
  RES$RATEL95_UNADJ[which(RES$gender==3)]=apply(cbind(RES$RATE_UNADJ[which(RES$gender==3)],SElog3,SE3,colSums(CASES[,3,])),MARGIN=1,
                                                FUN=function(x){if(x[4]>100){return(x[1]*exp(-1.96*x[2]))
                                                }else{return(x[1]+(x[3]/sqrt(x[4])*(0.5*qchisq(0.025,2*x[4])-x[4])))}})   
  RES$RATEL95_UNADJ[which(is.na(RES$RATEL95_UNADJ))]=0
  
  #Upper 95% percent rate
  RES$RATEU95_UNADJ=NA
  RES$RATEU95_UNADJ[which(RES$gender==1)]=apply(cbind(RES$RATE_UNADJ[which(RES$gender==1)],SElog1,SE1,colSums(CASES[,1,])),MARGIN=1,
                                                FUN=function(x){if(x[4]>100){return(x[1]*exp(1.96*x[2]))
                                                }else{return(x[1]+(x[3]/sqrt(x[4])*(0.5*qchisq(0.975,2*x[4]+2)-x[4])))}})                                                                                                      
  RES$RATEU95_UNADJ[which(RES$gender==2)]=apply(cbind(RES$RATE_UNADJ[which(RES$gender==2)],SElog2,SE2,colSums(CASES[,2,])),MARGIN=1,
                                                FUN=function(x){if(x[4]>100){return(x[1]*exp(1.96*x[2]))
                                                }else{return(x[1]+(x[3]/sqrt(x[4])*(0.5*qchisq(0.975,2*x[4]+2)-x[4])))}}) 
  RES$RATEU95_UNADJ[which(RES$gender==3)]=apply(cbind(RES$RATE_UNADJ[which(RES$gender==3)],SElog3,SE3,colSums(CASES[,3,])),MARGIN=1,
                                                FUN=function(x){if(x[4]>100){return(x[1]*exp(1.96*x[2]))
                                                }else{return(x[1]+(x[3]/sqrt(x[4])*(0.5*qchisq(0.975,2*x[4]+2)-x[4])))}})
  RES$RATEU95_UNADJ[which(is.na(RES$RATEU95_UNADJ))]=0
  
  #Relative risk with Empirical bayesian estimates non adjusted
  RES$SMRR_UNADJ=NA
  if (max(RES$observed[which(RES$gender==1)]) > 0 || max(RES$EXP_UNADJ[which(RES$gender==1)])  > 0){
    RES$SMRR_UNADJ[which(RES$gender==1)]=EmpBayes(O=RES$observed[which(RES$gender==1)],E=RES$EXP_UNADJ[which(RES$gender==1)])
  }
  if (max(RES$observed[which(RES$gender==2)]) > 0 || max(RES$EXP_UNADJ[which(RES$gender==2)])  > 0){
    RES$SMRR_UNADJ[which(RES$gender==2)]=EmpBayes(O=RES$observed[which(RES$gender==2)],E=RES$EXP_UNADJ[which(RES$gender==2)])
  }
  if (max(RES$observed[which(RES$gender==3)]) > 0 || max(RES$EXP_UNADJ[which(RES$gender==3)])  > 0){
    RES$SMRR_UNADJ[which(RES$gender==3)]=EmpBayes(O=RES$observed[which(RES$gender==3)],E=RES$EXP_UNADJ[which(RES$gender==3)])
  }
  
  
  ###ADJUSTED
  #Expected number of cases adjusted
  RES$EXP_ADJ=NA

  #sADJRATES=FindAdjust(cADJRATES, StoC=StoC, StoCcomp=StoCcomp)
  sADJRATESNoArea=FindAdjustNoArea(cADJRATESNoArea, StoCcomp=StoCcomp)
  
  
  #RES$EXP_ADJ[which(RES$gender==1)]=apply(POP[,1,]*sADJRATES[,1,],MARGIN=2,FUN=sum)
  #RES$EXP_ADJ[which(RES$gender==2)]=apply(POP[,2,]*sADJRATES[,2,],MARGIN=2,FUN=sum)
  #RES$EXP_ADJ[which(RES$gender==3)]=apply(POP[,3,]*sADJRATES[,3,],MARGIN=2,FUN=sum)
  #RES[1:100,]
  
  RES$EXP_ADJ[which(RES$gender==1)]=apply(POP[,1,]*sADJRATESNoArea[,1,],MARGIN=2,FUN=sum)
  RES$EXP_ADJ[which(RES$gender==2)]=apply(POP[,2,]*sADJRATESNoArea[,2,],MARGIN=2,FUN=sum)
  RES$EXP_ADJ[which(RES$gender==3)]=apply(POP[,3,]*sADJRATESNoArea[,3,],MARGIN=2,FUN=sum)
  
  #Relative Risk adjusted
  RES$RR_ADJ=NA
  RES$RR_ADJ[which(RES$gender==1)]=colSums(CASES[,1,])/RES$EXP_ADJ[which(RES$gender==1)]
  RES$RR_ADJ[which(RES$gender==2)]=colSums(CASES[,2,])/RES$EXP_ADJ[which(RES$gender==2)]
  RES$RR_ADJ[which(RES$gender==3)]=colSums(CASES[,3,])/RES$EXP_ADJ[which(RES$gender==3)]
  RES[1:100,]
  
  #Lower 95 percent interval adjusted
  RES$RRL95_ADJ=NA
  RES$RRL95_ADJ[which(RES$gender==1)]=apply(cbind(colSums(CASES[,1,]),RES$EXP_ADJ[which(RES$gender==1)]),MARGIN=1,
                                            FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(-1.96*sqrt(1/x[1])))
                                            }else{return(0.5*qchisq(0.025,2*x[1])/x[2])}})
  RES$RRL95_ADJ[which(RES$gender==2)]=apply(cbind(colSums(CASES[,2,]),RES$EXP_ADJ[which(RES$gender==2)]),MARGIN=1,
                                            FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(-1.96*sqrt(1/x[1])))
                                            }else{return(0.5*qchisq(0.025,2*x[1])/x[2])}})
  RES$RRL95_ADJ[which(RES$gender==3)]=apply(cbind(colSums(CASES[,3,]),RES$EXP_ADJ[which(RES$gender==3)]),MARGIN=1,
                                            FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(-1.96*sqrt(1/x[1])))
                                            }else{return(0.5*qchisq(0.025,2*x[1])/x[2])}})
  
  #Upper 95 percent interval adjusted
  RES$RRU95_ADJ=NA
  RES$RRU95_ADJ[which(RES$gender==1)]=apply(cbind(colSums(CASES[,1,]),RES$EXP_ADJ[which(RES$gender==1)]),MARGIN=1,
                                            FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(1.96*sqrt(1/x[1])))
                                            }else{return(0.5*qchisq(0.975,2*x[1]+2)/x[2])}})
  RES$RRU95_ADJ[which(RES$gender==2)]=apply(cbind(colSums(CASES[,2,]),RES$EXP_ADJ[which(RES$gender==2)]),MARGIN=1,
                                            FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(1.96*sqrt(1/x[1])))
                                            }else{return(0.5*qchisq(0.975,2*x[1]+2)/x[2])}})
  RES$RRU95_ADJ[which(RES$gender==3)]=apply(cbind(colSums(CASES[,3,]),RES$EXP_ADJ[which(RES$gender==3)]),MARGIN=1,
                                            FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(1.96*sqrt(1/x[1])))
                                            }else{return(0.5*qchisq(0.975,2*x[1]+2)/x[2])}})
  
  #Rate adjusted
  RES$RATE_ADJ=NA
  #sADJPOP=FindAdjust(cPOP, StoC=StoC, StoCcomp=StoCcomp)
  sADJPOPNoArea=FindAdjustNoArea(cPOPNoArea, StoCcomp=StoCcomp)
  
  #RES$RATE_ADJ[which(RES$gender==1)]=colSums(RATES[,1,]*sADJPOP[,1,])/colSums(sADJPOP[,1,])*100000
  #RES$RATE_ADJ[which(RES$gender==2)]=colSums(RATES[,2,]*sADJPOP[,2,])/colSums(sADJPOP[,2,])*100000
  #RES$RATE_ADJ[which(RES$gender==3)]=colSums(RATES[,3,]*sADJPOP[,3,])/colSums(sADJPOP[,3,])*100000
  #RES[1:100,2:11]
  RES$RATE_ADJ[which(RES$gender==1)]=colSums(RATES[,1,]*sADJPOPNoArea[,1,])/colSums(sADJPOPNoArea[,1,])*100000
  RES$RATE_ADJ[which(RES$gender==2)]=colSums(RATES[,2,]*sADJPOPNoArea[,2,])/colSums(sADJPOPNoArea[,2,])*100000
  RES$RATE_ADJ[which(RES$gender==3)]=colSums(RATES[,3,]*sADJPOPNoArea[,3,])/colSums(sADJPOPNoArea[,3,])*100000
  
  #Rate Confidence interval adjusted
  #SElog1=sqrt(colSums(sADJPOP[,1,]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(sADJPOP[,1,]*RATES[,1,])
  #SElog2=sqrt(colSums(sADJPOP[,2,]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(sADJPOP[,2,]*RATES[,2,])
  #SElog3=sqrt(colSums(sADJPOP[,3,]^2*RATES[,3,]*(1-RATES[,3,])/POP[,3,]))/colSums(sADJPOP[,3,]*RATES[,3,])
  SElog1=sqrt(colSums(sADJPOPNoArea[,1,]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(sADJPOPNoArea[,1,]*RATES[,1,])
  SElog2=sqrt(colSums(sADJPOPNoArea[,2,]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(sADJPOPNoArea[,2,]*RATES[,2,])
  SElog3=sqrt(colSums(sADJPOPNoArea[,3,]^2*RATES[,3,]*(1-RATES[,3,])/POP[,3,]))/colSums(sADJPOPNoArea[,3,]*RATES[,3,])
  
  #SE1=sqrt(colSums(sADJPOP[,1,]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(sADJPOP[,1,])*100000
  #SE2=sqrt(colSums(sADJPOP[,2,]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(sADJPOP[,2,])*100000
  #SE3=sqrt(colSums(sADJPOP[,3,]^2*RATES[,1,]*(1-RATES[,3,])/POP[,3,]))/colSums(sADJPOP[,3,])*100000
  SE1=sqrt(colSums(sADJPOPNoArea[,1,]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(sADJPOPNoArea[,1,])*100000
  SE2=sqrt(colSums(sADJPOPNoArea[,2,]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(sADJPOPNoArea[,2,])*100000
  SE3=sqrt(colSums(sADJPOPNoArea[,3,]^2*RATES[,1,]*(1-RATES[,3,])/POP[,3,]))/colSums(sADJPOPNoArea[,3,])*100000
  
  #Lower 95% percent rate adjusted
  RES$RATEL95_ADJ=NA
  RES$RATEL95_ADJ[which(RES$gender==1)]=apply(cbind(RES$RATE_ADJ[which(RES$gender==1)],SElog1,SE1,colSums(CASES[,1,])),MARGIN=1,
                                              FUN=function(x){if(x[4]>100){return(x[1]*exp(-1.96*x[2]))
                                              }else{return(x[1]+(x[3]/sqrt(x[4])*(0.5*qchisq(0.025,2*x[4])-x[4])))}})                                                                                                      
  RES$RATEL95_ADJ[which(RES$gender==2)]=apply(cbind(RES$RATE_ADJ[which(RES$gender==2)],SElog2,SE2,colSums(CASES[,2,])),MARGIN=1,
                                              FUN=function(x){if(x[4]>100){return(x[1]*exp(-1.96*x[2]))
                                              }else{return(x[1]+(x[3]/sqrt(x[4])*(0.5*qchisq(0.025,2*x[4])-x[4])))}})   
  RES$RATEL95_ADJ[which(RES$gender==3)]=apply(cbind(RES$RATE_ADJ[which(RES$gender==3)],SElog3,SE3,colSums(CASES[,3,])),MARGIN=1,
                                              FUN=function(x){if(x[4]>100){return(x[1]*exp(-1.96*x[2]))
                                              }else{return(x[1]+(x[3]/sqrt(x[4])*(0.5*qchisq(0.025,2*x[4])-x[4])))}})   
  
  #Upper 95% percent rate adjusted
  RES$RATEU95_ADJ=NA
  RES$RATEU95_ADJ[which(RES$gender==1)]=apply(cbind(RES$RATE_ADJ[which(RES$gender==1)],SElog1,SE1,colSums(CASES[,1,])),MARGIN=1,
                                              FUN=function(x){if(x[4]>100){return(x[1]*exp(1.96*x[2]))
                                              }else{return(x[1]+(x[3]/sqrt(x[4])*(0.5*qchisq(0.975,2*x[4]+2)-x[4])))}})                                                                                                      
  RES$RATEU95_ADJ[which(RES$gender==2)]=apply(cbind(RES$RATE_ADJ[which(RES$gender==2)],SElog2,SE2,colSums(CASES[,2,])),MARGIN=1,
                                              FUN=function(x){if(x[4]>100){return(x[1]*exp(1.96*x[2]))
                                              }else{return(x[1]+(x[3]/sqrt(x[4])*(0.5*qchisq(0.975,2*x[4]+2)-x[4])))}}) 
  RES$RATEU95_ADJ[which(RES$gender==3)]=apply(cbind(RES$RATE_ADJ[which(RES$gender==3)],SElog3,SE3,colSums(CASES[,3,])),MARGIN=1,
                                              FUN=function(x){if(x[4]>100){return(x[1]*exp(1.96*x[2]))
                                              }else{return(x[1]+(x[3]/sqrt(x[4])*(0.5*qchisq(0.975,2*x[4]+2)-x[4])))}})
  
  #Relative risk with Empirical bayesian estimates adjusted
  RES$SMRR_ADJ=NA
  # only do the emprirical Bayes smoothing if there is some data
  if (max(RES$observed[which(RES$gender==1)]) > 0 || max(RES$EXP_ADJ[which(RES$gender==1)]) > 0) {
    RES$SMRR_ADJ[which(RES$gender==1)]=EmpBayes(O=RES$observed[which(RES$gender==1)],E=RES$EXP_ADJ[which(RES$gender==1)])
  }
  if (max(RES$observed[which(RES$gender==2)]) > 0 || max(RES$EXP_ADJ[which(RES$gender==2)]) > 0) {
    RES$SMRR_ADJ[which(RES$gender==2)]=EmpBayes(O=RES$observed[which(RES$gender==2)],E=RES$EXP_ADJ[which(RES$gender==2)])
  }
  if (max(RES$observed[which(RES$gender==3)]) > 0 || max(RES$EXP_ADJ[which(RES$gender==3)]) > 0) {
    RES$SMRR_ADJ[which(RES$gender==3)]=EmpBayes(O=RES$observed[which(RES$gender==3)],E=RES$EXP_ADJ[which(RES$gender==3)])
  }
  
  # now start the smoothing bit
  data = RES
  #adj=TRUE #either true or false
  if (adj==FALSE){
    Exp='EXP_UNADJ'
    Sadj='UNADJ'
  }else{
    Exp='EXP_ADJ'
    Sadj='ADJ'
  }
  if (length(which(names(data)==Exp))==0) {print('The Expected counts for the adjustement given does not exist in data.')}
  
  # do we need the mean_neigh function?
  
  ## new code to handle the format of adjacency file returned by GetAdjMatrix. 
  ## This might not be in the right order and may have missing entries that will need to be
  # filled in with zero entries
  
  data$area_id=as.character(data$area_id)
  AdjRowset$area_id=as.character(AdjRowset$area_id)
  AdjRowset$adjacency_list= as.character(AdjRowset$adjacency_list)
  # Create integer ids for each area_id that is in data. 
  # The order will be that of the order in data unlike Aurora's original code.
  
  data$area_order=NA
  
  # Use the order of male records (assume the other records are in the same order)
  maleRows=which(data$gender==1)
  femaleRows=which(data$gender==2)
  bothRows=which(data$gender==3)
  narea=length(maleRows)
  data$area_order[maleRows]=seq(1:narea)
  data$area_order[femaleRows]=seq(1:narea)
  data$area_order[bothRows]=seq(1:narea) 
  
  # Create the sparse matrix of neighbours:
  rowNums = c()
  colNums = c()
  for (i in 1:narea){
    wr = which(AdjRowset$area_id == data$area_id[i * 3])
    if (length(wr) == 0) { #empty - treat as an 'island' with no neighbours (add just diagonal)
      rowNums<-c(rowNums,i)
      colNums<-c(colNums,i)
    } 
    if (length(wr) > 1) {} #throw and exception because there are duplicate areas_ids in the ajd
    if (length(wr) == 1){
      rowNums<-c(rowNums,i)
      colNums<-c(colNums,i)
      # go through the neighbours of this row and AdjRows and find the index in data
      neighbours = strsplit(AdjRowset$adjacency_list[wr], split=",")
      for (j in 1:length(neighbours[[1]])){
        whichIndex = data$area_order[which(data$area_id == neighbours[[1]][j])]
        if (length(whichIndex) == 0) {} # through an exception because neighbour file contains unknown neighbours
        rowNums<-c(rowNums,i)
        colNums<-c(colNums,whichIndex[1])
      }
    }
  }
  
  # TODO need to findout if we need the 'identifying island' code from SmoothingBYM-HET-CAR.R
  # hopefully not because that will require the shapefile!
  
  IM<-sparseMatrix(i=rowNums, j=colNums, x=1)
  #IM1<-sparseMatrix(i=rowNums, j=colNums, symmetric= TRUE)
  #IM2<-as.matrix(IM1) #coerce to a dense matrix
  #IM<-as(IM2, "dgCMatrix") # coerce to a sparse matrix - this is the wrong way to do it
  # code that sets up the INLA formula
  # Field to hold the posterior probability
  data$POSTERIOR_PROBABILITY=NA
  if (adj==FALSE){
    if (model=='BYM'){
      print("Bayes smoothing with BYM model type no adjustment")
      formula=observed~f(area_order,model='bym',graph=IM, adjust.for.con.comp = FALSE,
                         hyper=list(prec.unstruct=list(param=c(0.5,0.0005)), 
                                    prec.spatial=list(param=c(0.5,0.0005))))
      data$BYM_RR_UNADJ=NA
      data$BYM_RRL95_UNADJ=NA
      data$BYM_RRU95_UNADJ=NA
      
      data$BYM_ssRR_UNADJ=NA
      data$BYM_ssRRL95_UNADJ=NA
      data$BYM_ssRRU95_UNADJ=NA
    }
    if (model=='HET'){
      print("Bayes smoothing with HET model type no adjustment")
      formula=observed~f(area_order, model='iid',
                         hyper=list(prec=list(param=c(0.5,0.0005))))
      data$HET_RR_UNADJ=NA
      data$HET_RRL95_UNADJ=NA
      data$HET_RRU95_UNADJ=NA
    }
    if (model=='CAR'){
      print("Bayes smoothing with CAR model type no adjustment")
      formula=observed~f(area_order, model='besag',graph=IM,
                         hyper=list(prec=list(param=c(0.5,0.0005))))
      data$CAR_RR_UNADJ=NA
      data$CAR_RRL95_UNADJ=NA
      data$CAR_RRU95_UNADJ=NA
    }
  }else {
    if (model=='BYM'){
      print("Bayes smoothing with BYM model type, adjusted")
      formula=observed~f(area_order,model='bym',graph=IM, 
                         hyper=list(prec.unstruct=list(param=c(0.5,0.0005)), 
                                    prec.spatial=list(param=c(0.5,0.0005))))
      data$BYM_RR_ADJ=NA
      data$BYM_RRL95_ADJ=NA
      data$BYM_RRU95_ADJ=NA
      
      data$BYM_ssRR_ADJ=NA
      data$BYM_ssRRL95_ADJ=NA
      data$BYM_ssRRU95_ADJ=NA
    }
    if (model=='HET'){
      print("Bayes smoothing with HET model type, adjusted")
      formula=observed~f(area_order, model='iid',
                         hyper=list(prec=list(param=c(0.5,0.0005))))
      data$HET_RR_ADJ=NA
      data$HET_RRL95_ADJ=NA
      data$HET_RRU95_ADJ=NA
    }
    if (model=='CAR'){
      print("Bayes smoothing with CAR model type, adjusted")
      formula=observed~f(area_order, model='besag',graph=IM,
                         hyper=list(prec=list(param=c(0.5,0.0005))))
      data$CAR_RR_ADJ=NA
      data$CAR_RRL95_ADJ=NA
      data$CAR_RRU95_ADJ=NA
    }
  }
  
  # all the ordering code that Aurore did removed because the code to
  # generate the IM matrix should mean the Matrix is in the same order as
  # the entries in the data.
  
  for (g in c(1,2,3)){
    whichrows=which(data$gender==g)
    
    # finally execute the inla code
    # result=inla(formula, family='poisson', E=get(Exp), data=data[whichrows,])
    # the line above used to work until this code was moved into a function, which breats the get() call
    # replaced with the explicit lines below
    result = c()
    if (adj==FALSE){
      result=inla(formula, family='poisson', E=EXP_UNADJ, data=data[whichrows,])
     }else{
       result=inla(formula, family='poisson', E=EXP_ADJ, data=data[whichrows,])
     }
    
    # store the results the dataframe
    if (adj==FALSE){
      if (model=='BYM'){
        cte=result$summary.fixed[1]
        data$BYM_ssRR_UNADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[narea+1:narea,2])
        data$BYM_ssRRL95_UNADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[narea+1:narea,4])
        data$BYM_ssRRU95_UNADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[narea+1:narea,6])
        
        data$BYM_RR_UNADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,2])
        data$BYM_RRL95_UNADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,4])
        data$BYM_RRU95_UNADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,6]) 
      }
      if (model=='HET'){
        cte=result$summary.fixed[1]
        data$HET_RR_UNADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,2])
        data$HET_RRL95_UNADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,4])
        data$HET_RRU95_UNADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,6])  
      }
      if (model=='CAR'){
        cte=result$summary.fixed[1]
        data$CAR_RR_UNADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,2])
        data$CAR_RRL95_UNADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,4])
        data$CAR_RRU95_UNADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,6])  
      }
    }else {
      if (model=='BYM'){
        cte=result$summary.fixed[1]
        data$BYM_ssRR_ADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[narea+1:narea,2])
        data$BYM_ssRRL95_ADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[narea+1:narea,4])
        data$BYM_ssRRU95_ADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[narea+1:narea,6])
        
        data$BYM_RR_ADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,2])
        data$BYM_RRL95_ADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,4])
        data$BYM_RRU95_ADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,6])  
      }
      if (model=='HET'){
        cte=result$summary.fixed[1]
        data$HET_RR_ADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,2])
        data$HET_RRL95_ADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,4])
        data$HET_RRU95_ADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,6])  
      }
      if (model=='CAR'){
        cte=result$summary.fixed[1]
        data$CAR_RR_ADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,2])
        data$CAR_RRL95_ADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,4])
        data$CAR_RRU95_ADJ[whichrows]=exp(cte$mean+result$summary.random$area_order[1:narea,6])  
      }
    }
    
    #Calculate the posterior probability (refer to pages 184 & 185 in Blangiardo and Cameletti)
    csi <- result$marginals.random$area_order[1:narea]
    a<- 0
    prob.csi<- lapply(csi, function(x) {1 - inla.pmarginal(a, x)})
    data$POSTERIOR_PROBABILITY[whichrows]=unlist(prob.csi)
    print("Posterior probability calculated")
    
  }

  # call the function to convert data to the format the db is expecting
  originalExtractTable = convertToDBFormat(data)
  return(originalExtractTable) 
}

# When the monster createSmoothedExtractResults function has completed and returned its results
# Update the database correctly using the functions in TestSmoothingRoutine.R



#Find NULL data within adjustement covariates and replace by 0
findNULL=function(x){return(sapply(x,FUN=function(y){ans=y
                                                     if (y=='NULL'){ans=0}
                                                     return(ans)}))}
#Empirical Bayes function
EmpBayes=function(O,E){
  #initialize
  N=length(O)
  theta=O/E
  # Check to stop the function giving an error if the Expected value is zero
  theta[is.nan(theta)] = 1
  gamma=mean(theta)
  B=sum((theta-gamma)^2/E)
  C=sum((theta-gamma)^2)/(gamma*(N-1))
  DELTA=C^2+4*B
  alpha=(-C+sqrt(DELTA))/(2*B)
  nu=gamma*alpha
  thetap=theta
  alpha
  nu
  c=0
  while((max(abs(thetap-theta))>0.0001)&c==0){
    thetap=theta
    theta=(O+nu)/(E+alpha)
    gamma=mean(theta)
    B=sum((theta-gamma)^2/E)
    C=sum((theta-gamma)^2)/(gamma*(N-1))
    DELTA=C^2+4*B
    alpha=(-C+sqrt(DELTA))/(2*B)
    nu=gamma*alpha
    alpha
    nu
    c=c+1
  }
  return(theta)
}

#produce study array taking counts for adjustement
FindAdjust=function(cADJRATES, StoC, StoCcomp){
  i=1 
  RES=cADJRATES[,,StoC[i],StoCcomp[i]]
  for (i in 2:length(StoC))
    RES=abind(RES,cADJRATES[,,StoC[i],StoCcomp[i]],along=3)
  return(RES)
}

#produce study array taking counts for adjustement
FindAdjustNoArea=function(cADJRATESNoArea, StoCcomp){
  i=1 
  RES=cADJRATESNoArea[,,StoCcomp[i]]
  for (i in 2:length(StoCcomp))
    RES=abind(RES,cADJRATESNoArea[,,StoCcomp[i]],along=3)
  return(RES)
}

conc=function(x){x=as.character(x)
                 res=c(x)
                 if (length(x)>1){for (i in 2:length(x)){res=paste(res,x[i],sep='-')}}
                 return(res)}

##====================================================================
##FUNCTION: convertToDBFormat
##DESCRIPTION: converts old format of output of INLA data into 
## a format compatible with what's expected in the database
##====================================================================
convertToDBFormat=function(dataIn){
  area_id = dataIn$area_id
  gid = as.numeric(NA)
  gid_rowindex = as.numeric(NA)
  username = NA
  dataOut = data.frame(area_id,gid,gid_rowindex,username)
  study_id = dataIn$study_id
  dataOut = cbind(dataOut, study_id)
  inv_id = as.integer(as.character(investigationId)) #Input parameter
  dataOut = cbind(dataOut, inv_id)
 
  band_id = dataIn$band_id
  genders = dataIn$gender
  direct_standardisation = 0 # For now RIf only does indirect standardisation
  dataOut = cbind(dataOut, band_id, genders, direct_standardisation)
  
  observed = dataIn$observed
  if (adj==FALSE){
    adjusted = 0
    expected = dataIn$EXP_UNADJ # assuming direct = TRUE. If direct = FALSE, use RR_UNADJ
    lower95 = dataIn$RRL95_UNADJ # assuming direct = TRUE. If direct = FALSE, use RRL95_UNADJ
    upper95 = dataIn$RRU95_UNADJ # assuming direct = TRUE. If direct = FALSE, use RRU95_UNADJ
    relative_risk = dataIn$RR_UNADJ # assuming indirect. If direct, use null
    smoothed_relative_risk = dataIn$SMRR_UNADJ # assuming indirect. If direct, use null
  } else {
    adjusted = 1
    expected = dataIn$EXP_ADJ # assuming direct = TRUE. If direct = FALSE, use RR_UNADJ
    lower95 = dataIn$RRL95_ADJ # assuming direct = TRUE. If direct = FALSE, use RRL95_UNADJ
    upper95 = dataIn$RRU95_ADJ # assuming direct = TRUE. If direct = FALSE, use RRU95_UNADJ
    relative_risk = dataIn$RR_ADJ # assuming indirect. If direct, use null
    smoothed_relative_risk = dataIn$SMRR_ADJ # assuming indirect. If direct, use null
  }
  posterior_probability = dataIn$POSTERIOR_PROBABILITY
  posterior_probability_upper95 = as.numeric(NA) # unclear what should go in here
  posterior_probability_lower95 = as.numeric(NA) # unclear what should go in here
  residual_relative_risk = as.numeric(NA) # unclear what should go in here
  residual_rr_lower95 = as.numeric(NA) # unclear what should go in here
  residual_rr_upper95 = as.numeric(NA) # unclear what should go in here
  #INLA section
  smoothed_smr = as.numeric(NA)
  smoothed_smr_lower95 = as.numeric(NA)
  smoothed_smr_upper95 = as.numeric(NA)
  if (model=='BYM'){
    if (adj==TRUE){
      smoothed_smr = dataIn$BYM_RR_ADJ # for BYM model, also have the ss (spatially structured) terms. Check with Marta if this should be used somehow
      smoothed_smr_lower95 = dataIn$BYM_RRL95_ADJ
      smoothed_smr_upper95 = dataIn$BYM_RRU95_ADJ
    }else {      
      smoothed_smr = dataIn$BYM_RR_UNADJ # for BYM model, also have the ss (spatially structured) terms. Check with Marta if this should be used somehow
      smoothed_smr_lower95 = dataIn$BYM_RRL95_UNADJ
      smoothed_smr_upper95 = dataIn$BYM_RRU95_UNADJ
    }
  }
  if (model=='HET'){
    if (adj==TRUE){
      smoothed_smr = dataIn$HET_RR_ADJ
      smoothed_smr_lower95 = dataIn$HET_RRL95_ADJ
      smoothed_smr_upper95 = dataIn$HET_RRU95_ADJ
    } else { 
      smoothed_smr = dataIn$HET_RR_UNADJ
      smoothed_smr_lower95 = dataIn$HET_RRL95_UNADJ
      smoothed_smr_upper95 = dataIn$HET_RRU95_UNADJ
    }
  }
  if (model=='CAR'){
    if (adj==TRUE){
      smoothed_smr = dataIn$CAR_RR_ADJ
      smoothed_smr_lower95 = dataIn$CAR_RRL95_ADJ
      smoothed_smr_upper95 = dataIn$CAR_RRU95_ADJ
    } else {
      smoothed_smr = dataIn$CAR_RR_UNADJ
      smoothed_smr_lower95 = dataIn$CAR_RRL95_UNADJ
      smoothed_smr_upper95 = dataIn$CAR_RRU95_UNADJ
      
    }
  }
  
  dataOut = cbind(dataOut, adjusted, observed,expected,lower95,upper95,relative_risk,smoothed_relative_risk,
                  posterior_probability ,posterior_probability_upper95,posterior_probability_lower95,
                  residual_relative_risk,residual_rr_lower95,residual_rr_upper95,
                  smoothed_smr,smoothed_smr_lower95,smoothed_smr_upper95)
  return(dataOut)
}


saveDataFrameToDatabaseTable <- function(data) {
  print(paste0("Creating ",temporarySmoothedResultsTableName))
  
  sqlSave(connDB, data, tablename=temporarySmoothedResultsTableName)
  #sqlSave(connDB, data, tablename = "kgarwood.rifSmoothTest")
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
updateMapTableFromSmoothedResultsTable <- function() {
  
  updateMapTableSQLQuery <- paste0(
    "UPDATE ", mapTableName, " a ",
    "SET ",
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
    "smoothed_smr_upper95=b.smoothed_smr_upper95 ",
    "FROM ",
    temporarySmoothedResultsTableName,
    " b ",
    "WHERE ",
    "a.study_id=b.study_id AND ",
    "a.band_id=b.band_id AND ",
    "a.inv_id=b.inv_id AND ",
    "a.genders=b.genders AND ",
    "a.area_id=b.area_id");
  
  sqlQuery(connDB, updateMapTableSQLQuery)				
  #print(updateMapTableSQLQuery)
  print(paste0("Updated map table: ", mapTableName))
}

#make and ODBC connection
#dbHost = 'networkRif'
#dbName = 'rif_studies'
#studyID = '1'

processCommandLineArguments()
establishTableNames(studyID)
#connDB <- odbcConnect(odbcDataSource, uid=as.character(userID), pwd=as.character(password))
connDB = odbcConnect(odbcDataSource)

#odbcSetAutoCommit(connDB, autoCommit=FALSE)
print("Performing basic stats and smoothing")
result <- performSmoothingActivity()
print("Creating temporary table")
saveDataFrameToDatabaseTable(result)
print("Updating map table")
updateMapTableFromSmoothedResultsTable()
print("Dropping temporary table")
sqlDrop(connDB, temporarySmoothedResultsTableName)
print("Closing database connection")
#print(paste0("RESULT==", result, "=="))
odbcClose(connDB)
#quit(status=result)
