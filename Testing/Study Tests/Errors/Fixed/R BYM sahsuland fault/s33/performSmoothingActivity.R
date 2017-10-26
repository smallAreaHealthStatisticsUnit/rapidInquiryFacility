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
#   RIF performSmoothingActivity functions
############################################################################################################

##====================================================================
##FUNCTION: performSmoothingActivity
##PARAMETERS: Extract data frame, adjacency matrix frame
##DESCRIPTION: assembles pieces of database information such as
##the host, port and database name to create a database connection
##string that can be used to make an ODBC connection
##====================================================================
performSmoothingActivity <- function(data, AdjRowset) {
						
  cat("Covariates: ", paste0(names.adj), "\n", sep="")
  if (investigationName != "inv_1"){
    #add a column (inv_1) to data and populate is with InvestigationName data for simplicity
    inv_1 = 0;
    data = cbind(data, inv_1)
    # find the relevant column in the data
    #first need to ignore the case of the investigation names
    ColNames = toupper(names(data))
    invcol =  which(ColNames==toupper(investigationName))
    if (length(invcol)==0){
      cat(paste('The column defined by the investigation_name parameter: ', investigationName, 
		'not found in data table. Data assumed to be all zero!\n'), sep="")
    }
    else{
      #copy to the new inv_1 column
      data$inv_1 = data[,invcol]
    }
  }
 

  
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
  if (adj) {
    for (i in 1:length(names.adj)){
      id=which(toupper(names(data))==toupper(names.adj[i]))
      ic=which(toupper(names(comp))==toupper(names.adj[i]))
      if (length(id)==0|length(ic)==0){
        cat(paste('covariate', names.adj[i], 'not found, data will not be adjusted for it!\n'), sep="")
      }else{
        i.d.adj=c(i.d.adj,id)
        i.c.adj=c(i.c.adj,ic)
      }
    }
  }	  
  ncov=length(i.d.adj)
  if (adj) {
    for (i in 1:ncov){
      data[,i.d.adj[i]]=findNULL(data[,i.d.adj[i]])
      comp[,i.c.adj[i]]=findNULL(comp[,i.c.adj[i]])
      data[,i.d.adj[i]]=as.numeric(as.character(data[,i.d.adj[i]]))
      comp[,i.c.adj[i]]=as.numeric(as.character(comp[,i.c.adj[i]])) 
    } 
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
  if (adj) {
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
    # BP- the area field in the comparison rows should NOT be used. Just need to match comparison rows by age, sex, year and ADJcomb
    compComplete=expand.grid(unique(comp$age_group),unique(comp$sex),unique(comp$area_id),unique(comp$year),ADJcomb)
    compComplete=as.data.frame(compComplete)
    names(compComplete)=c('age_group','sex','area_id','year','comb')
    
    compComplete$comb=as.character(compComplete$comb)
    comp$comb=apply(as.matrix(comp[,i.c.adj],ncol=ncov),MARGIN=1,FUN=conc)
    compComplete=merge(compComplete, comp,by=c('age_group','sex','area_id','year','comb'),all.x=TRUE)
    
    #Fill the array for comparative areas
    compComplete=compComplete[order(compComplete$comb,compComplete$year,compComplete$area_id,compComplete$sex,compComplete$age_group),]
    cCASES=array(compComplete$inv_1,dim=c(length(unique(compComplete$age_group)),length(unique(compComplete$sex)),length(unique(compComplete$area_id)),length(unique(compComplete$year)),m))
    cCASES=apply(cCASES,MARGIN=c(1,2,3,5),FUN=sum) #Mean over the years -
    cCASESNoArea=apply(cCASES,MARGIN=c(1,2,4),FUN=sum,na.rm=TRUE) #Mean over the areas
    
    cCASES=abind(cCASES,apply(cCASES,MARGIN=c(1,3,4),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
    cCASESNoArea=abind(cCASESNoArea,apply(cCASESNoArea,MARGIN=c(1,3),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
    
    cPOP=array(compComplete$total_pop,dim=c(length(unique(compComplete$age_group)),length(unique(compComplete$sex)),length(unique(compComplete$area_id)),length(unique(compComplete$year)),m))
    
    cPOP=apply(cPOP,MARGIN=c(1,2,3,5),FUN=sum) #Mean over the years
    cPOPNoArea=apply(cPOP,MARGIN=c(1,2,4),FUN=sum,na.rm=TRUE) #Mean over the areas
    
    cPOP=abind(cPOP,apply(cPOP,MARGIN=c(1,3,4),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
    cPOPNoArea=abind(cPOPNoArea,apply(cPOPNoArea,MARGIN=c(1,3),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
    
    #adjusted rates
    cADJRATESNoArea=cCASESNoArea/cPOPNoArea
    
    #link between study region and counts
    #Creates and array of the area ids
    S_area_id=array(data$area_id,dim=c(length(unique(data$age_group)),length(unique(data$sex)),length(unique(data$area_id)),length(unique(data$year))))[1,1,,1]
    C_area_id=array(compComplete$area_id,dim=c(length(unique(compComplete$age_group)),length(unique(compComplete$sex)),length(unique(compComplete$area_id)),length(unique(compComplete$year)),m))
    c_area_id=c()
    for (i in 1:length(unique(compComplete$area_id))){
      c_area_id=c(c_area_id,C_area_id[,,i,,][which(is.na(C_area_id[,,i,,])==FALSE)][1]) 
    }
    C_area_id=c_area_id
    #StoC=sapply(S_area_id,FUN=function(x){which(C_area_id==x)})
    StoCcomp=sapply(concADJ,FUN=function(x){which(ADJcomb==x)})
    
    ###ADJUSTED
    #Expected number of cases adjusted
    RES$EXP_ADJ=NA
    
    sADJRATESNoArea=FindAdjustNoArea(cADJRATESNoArea, StoCcomp=StoCcomp)
    
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
    sADJPOPNoArea=FindAdjustNoArea(cPOPNoArea, StoCcomp=StoCcomp)
    
    RES$RATE_ADJ[which(RES$gender==1)]=colSums(RATES[,1,]*sADJPOPNoArea[,1,])/colSums(sADJPOPNoArea[,1,])*100000
    RES$RATE_ADJ[which(RES$gender==2)]=colSums(RATES[,2,]*sADJPOPNoArea[,2,])/colSums(sADJPOPNoArea[,2,])*100000
    RES$RATE_ADJ[which(RES$gender==3)]=colSums(RATES[,3,]*sADJPOPNoArea[,3,])/colSums(sADJPOPNoArea[,3,])*100000
    
    #Rate Confidence interval adjusted
    SElog1=sqrt(colSums(sADJPOPNoArea[,1,]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(sADJPOPNoArea[,1,]*RATES[,1,])
    SElog2=sqrt(colSums(sADJPOPNoArea[,2,]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(sADJPOPNoArea[,2,]*RATES[,2,])
    SElog3=sqrt(colSums(sADJPOPNoArea[,3,]^2*RATES[,3,]*(1-RATES[,3,])/POP[,3,]))/colSums(sADJPOPNoArea[,3,]*RATES[,3,])
    
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
    
  } else {
    ## Long UNDAJ section
    
    #Make comparative variables in a array of dim: age-sex-area-year-adjustement
    #increase comp to compComplete so that there is one row for each combination of 
    #age-sex-area-year-adjustement 
    # BP- the area field in the comparison rows should NOT be used. Just need to match comparison rows by age, sex, year
    compComplete=expand.grid(unique(comp$age_group),unique(comp$sex),unique(comp$area_id),unique(comp$year))
    compComplete=as.data.frame(compComplete)
    names(compComplete)=c('age_group','sex','area_id','year')
    
    compComplete=merge(compComplete, comp,by=c('age_group','sex','area_id','year'),all.x=TRUE)
    
    #Fill the array for comparative areas
    compComplete=compComplete[order(compComplete$year,compComplete$area_id,compComplete$sex,compComplete$age_group),]
    cCASES=array(compComplete$inv_1,dim=c(length(unique(compComplete$age_group)),length(unique(compComplete$sex)),length(unique(compComplete$area_id)),length(unique(compComplete$year))))
    cCASES=apply(cCASES,MARGIN=c(1,2,3),FUN=sum) #Mean over the years -
    cCASESNoArea=apply(cCASES,MARGIN=c(1,2),FUN=sum,na.rm=TRUE) #Mean over the areas
    
    cCASES=abind(cCASES,apply(cCASES,MARGIN=c(1,3),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
    cCASESNoArea=abind(cCASESNoArea,apply(cCASESNoArea,MARGIN=c(1),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
    
    cPOP=array(compComplete$total_pop,dim=c(length(unique(compComplete$age_group)),length(unique(compComplete$sex)),length(unique(compComplete$area_id)),length(unique(compComplete$year))))
    
    cPOP=apply(cPOP,MARGIN=c(1,2,3),FUN=sum) #Mean over the years
    cPOPNoArea=apply(cPOP,MARGIN=c(1,2),FUN=sum,na.rm=TRUE) #Mean over the areas
    
    cPOPNoArea=abind(cPOPNoArea,apply(cPOPNoArea,MARGIN=c(1),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)
    
    #unadjusted rates
    # CHeck this line works as expected!
    cRATESNoArea=apply(cCASESNoArea,MARGIN=c(1,2),FUN=sum,na.rm=TRUE)/apply(cPOPNoArea,MARGIN=c(1,2),FUN=sum,na.rm=TRUE)
    
    #link between study region and counts
    #Creates and array of the area ids
    S_area_id=array(data$area_id,dim=c(length(unique(data$age_group)),length(unique(data$sex)),length(unique(data$area_id)),length(unique(data$year))))[1,1,,1]
    C_area_id=array(compComplete$area_id,dim=c(length(unique(compComplete$age_group)),length(unique(compComplete$sex)),length(unique(compComplete$area_id)),length(unique(compComplete$year))))
    c_area_id=c()
    for (i in 1:length(unique(compComplete$area_id))){
      c_area_id=c(c_area_id,C_area_id[,,i,][which(is.na(C_area_id[,,i,])==FALSE)][1]) 
    }
    C_area_id=c_area_id
    
    ###NON ADJUSTED
    #Expected number of cases non adjusted
    RES$EXP_UNADJ=NA
    RES$EXP_UNADJ[which(RES$gender==1)]=apply(POP[,1,]*cRATESNoArea[,1],MARGIN=2,FUN=sum, na.rm = TRUE)
    RES$EXP_UNADJ[which(RES$gender==2)]=apply(POP[,2,]*cRATESNoArea[,2],MARGIN=2,FUN=sum, na.rm = TRUE)
    RES$EXP_UNADJ[which(RES$gender==3)]=apply(POP[,3,]*cRATESNoArea[,3],MARGIN=2,FUN=sum, na.rm = TRUE)
    
    #Relative Risk non adjusted
    RES$RR_UNADJ=NA
    RES$RR_UNADJ[which(RES$gender==1)]=colSums(CASES[,1,], na.rm = TRUE)/RES$EXP_UNADJ[which(RES$gender==1)]
    RES$RR_UNADJ[which(RES$gender==2)]=colSums(CASES[,2,], na.rm = TRUE)/RES$EXP_UNADJ[which(RES$gender==2)]
    RES$RR_UNADJ[which(RES$gender==3)]=colSums(CASES[,3,], na.rm = TRUE)/RES$EXP_UNADJ[which(RES$gender==3)]
    #convert NA's to zeros for now
    RES$RR_UNADJ[which(is.na(RES$RR_UNADJ))]=0
    
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
    
    RES$RATE_UNADJ[which(RES$gender==1)]=colSums(RATES[,1,]*cPOP3dNoArea[,1])/sum(cPOP3dNoArea[,1])*100000
    RES$RATE_UNADJ[which(RES$gender==2)]=colSums(RATES[,2,]*cPOP3dNoArea[,2])/sum(cPOP3dNoArea[,2])*100000
    RES$RATE_UNADJ[which(RES$gender==3)]=colSums(RATES[,3,]*cPOP3dNoArea[,3])/sum(cPOP3dNoArea[,3])*100000
    
    RES$RATE_UNADJ[which(is.na(RES$RATE_UNADJ))]=0
    
    #Rate Confidence interval non adjusted
    SElog1=sqrt(colSums(cPOP3dNoArea[,1]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/sum(cPOP3dNoArea[,1]*RATES[,1,])
    SElog2=sqrt(colSums(cPOP3dNoArea[,2]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/sum(cPOP3dNoArea[,2]*RATES[,2,])
    SElog3=sqrt(colSums(cPOP3dNoArea[,3]^2*RATES[,3,]*(1-RATES[,3,])/POP[,3,]))/sum(cPOP3dNoArea[,3]*RATES[,3,])
    
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
  } #end adj == false section
  
  
  data = RES
  # now start the smoothing bit, only do it if smoothing is specified as one of the supported models
  if (model=='BYM' | model == 'HET' | model == 'CAR') {
    #adj=TRUE #either true or false
    if (adj==FALSE){
      Exp='EXP_UNADJ'
      Sadj='UNADJ'
    }else{
      Exp='EXP_ADJ'
      Sadj='ADJ'
    }
    if (length(which(names(data)==Exp))==0) {
		cat('The Expected counts for the adjustement given does not exist in data.\n')
	}
    
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
      else if (length(wr) > 1) {
        #throw an exception because there are duplicate areas_ids in the adjacency list
        stop(paste("duplicate areas_ids: ", wr , " in the adjacency list"))
      } 
      else if (length(wr) == 1){
        rowNums<-c(rowNums,i)
        colNums<-c(colNums,i)
        # go through the neighbours of this row and AdjRows and find the index in data
        neighbours = strsplit(AdjRowset$adjacency_list[wr], split=",")
        for (j in 1:length(neighbours[[1]])){
          whichIndex = data$area_order[which(data$area_id == neighbours[[1]][j])]
          
          #          if (length(whichIndex) == 0) {} # through an exception because neighbour file contains unknown neighbours
          #          rowNums<-c(rowNums,i)
          #          colNums<-c(colNums,whichIndex[1])
          
          if (length(whichIndex) == 0) {
            cat(paste0("[Ignored] Area: ", AdjRowset$area_id[i], " Invalid adjacent Area: "  ,neighbours[[1]][j], "\n"), sep="")
            # Print a message, but ignore the nieghbours which shouldn't be in the list
          }
          else {
            rowNums<-c(rowNums,i)
            colNums<-c(colNums,whichIndex[1])
          }
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
        cat("Bayes smoothing with BYM model type no adjustment\n")
        # need to set adjust.for.con.comp = FALSE for now
        # while the GetAdjacencyRows function isn't working propery. This means the BYM and CAR models won't work propery
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
        cat("Bayes smoothing with HET model type no adjustment\n")
        formula=observed~f(area_order, model='iid',
                           hyper=list(prec=list(param=c(0.5,0.0005))))
        data$HET_RR_UNADJ=NA
        data$HET_RRL95_UNADJ=NA
        data$HET_RRU95_UNADJ=NA
      }
      if (model=='CAR'){
        cat("Bayes smoothing with CAR model type no adjustment\n")
        # need to set adjust.for.con.comp = FALSE for now
        # while the GetAdjacencyRows function isn't working propery. This means the BYM and CAR models won't work propery
        formula=observed~f(area_order, model='besag', graph=IM,adjust.for.con.comp = FALSE,
                           hyper=list(prec=list(param=c(0.5,0.0005))))
        data$CAR_RR_UNADJ=NA
        data$CAR_RRL95_UNADJ=NA
        data$CAR_RRU95_UNADJ=NA
      }
    }else {
      if (model=='BYM'){
        cat("Bayes smoothing with BYM model type, adjusted\n")
        # need to set adjust.for.con.comp = FALSE for now
        # while the GetAdjacencyRows function isn't working propery. This means the BYM and CAR models won't work propery
        formula=observed~f(area_order,model='bym',graph=IM, adjust.for.con.comp = FALSE,
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
        cat("Bayes smoothing with HET model type, adjusted\n")
        formula=observed~f(area_order, model='iid',
                           hyper=list(prec=list(param=c(0.5,0.0005))))
        data$HET_RR_ADJ=NA
        data$HET_RRL95_ADJ=NA
        data$HET_RRU95_ADJ=NA
      }
      if (model=='CAR'){
        cat("Bayes smoothing with CAR model type, adjusted\n")
        # need to set adjust.for.con.comp = FALSE for now
        # while the GetAdjacencyRows function isn't working propery. This means the BYM and CAR models won't work propery
        formula=observed~f(area_order, model='besag',graph=IM, adjust.for.con.comp = FALSE, 
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
      if (adj==FALSE) {
        result=inla(formula, family='poisson', E=EXP_UNADJ, data=data[whichrows,], verbose = TRUE)
      } else {
        result=inla(formula, family='poisson', E=EXP_ADJ, data=data[whichrows,], verbose = TRUE)
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
    }
    cat("Posterior probability calculated\n")
  }  #end if model == BYM or HET or CAR
  else {       
    cat("No Bayesian smoothing performed\n") 
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
  posterior_probability = as.numeric(NA)
  posterior_probability_upper95 = as.numeric(NA) # unclear what should go in here
  posterior_probability_lower95 = as.numeric(NA) # unclear what should go in here
  residual_relative_risk = as.numeric(NA) # unclear what should go in here
  residual_rr_lower95 = as.numeric(NA) # unclear what should go in here
  residual_rr_upper95 = as.numeric(NA) # unclear what should go in here
  smoothed_smr = as.numeric(NA)
  smoothed_smr_lower95 = as.numeric(NA)
  smoothed_smr_upper95 = as.numeric(NA)
  
  
  #INLA section
  if (model=='BYM' | model == 'HET' | model == 'CAR') {
    posterior_probability = dataIn$POSTERIOR_PROBABILITY
  }    
  
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

