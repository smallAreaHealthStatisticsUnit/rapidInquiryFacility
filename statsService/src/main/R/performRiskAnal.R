## Brandon Parkes
## @author bparkes
##

############################################################################################################
#   RIF PROJECT
#   RIF performRiskAnal functions
############################################################################################################
#Find NULL data within adjustement covariates and replace by 0
findNULL=function(xd){return(sapply(as.list(xd),FUN=function(yd){ans=yd
if (is.na(yd)){ans=0} else {
  if (yd=='NULL'){ans=0} }
return(ans)}))}

convertToDBFormatRA=function(dataIn){
  
  #Copy the db format for disease mapping because the same table will be used (with quite a few columns blank)
  area_id = as.numeric(NA)
  gid = as.numeric(NA)
  gid_rowindex = as.numeric(NA)
  username = NA
  study_id = dataIn$study_id
  dataOut = cbind(area_id,gid,gid_rowindex,username, study_id)
  inv_id = as.integer(as.character(investigationId)) #Input parameter
  dataOut = cbind(dataOut, inv_id)
  
  band_id = dataIn$band_id
  exposure = dataIn$exposure
  genders = dataIn$gender
  direct_standardisation = 0 # For now RIf only does indirect standardisation
  dataOut = cbind(dataOut, band_id, exposure, genders, direct_standardisation)
  
  observed = dataIn$observed
  adjusted = dataIn$adjusted
  expected = dataIn$expected 
  relative_risk = dataIn$RR
  lower95 = dataIn$RRL95
  upper95 = dataIn$RRU95
  
  smoothed_relative_risk =  as.numeric(NA)
  posterior_probability = as.numeric(NA)
  posterior_probability_upper95 = as.numeric(NA) 
  posterior_probability_lower95 = as.numeric(NA) 
  residual_relative_risk = as.numeric(NA) 
  residual_rr_lower95 = as.numeric(NA) 
  residual_rr_upper95 = as.numeric(NA) 
  smoothed_smr = as.numeric(NA)
  smoothed_smr_lower95 = as.numeric(NA)
  smoothed_smr_upper95 = as.numeric(NA)
  
  
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

conc=function(xd){xd=as.character(xd)
res=c(xd)
if (length(xd)>1){for (i in 2:length(xd)){res=paste(res,xd[i],sep='-')}}
return(res)}

# The function that does all the work
performBandAnal <- function(data) {
  if (studyName == "REXCEPTION") {
    cat("REXCEPTION test study detected: ", studyDescription, "\n", sep="")
    stop("REXCEPTION test study")
  }	
  
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
    exposure=x$exposure_value[1]
    gender=x$sex[1]
    observed=sum(x$inv_1)
    return(data.frame(study_or_comparison,study_id,area_id,band_id,exposure,gender,observed))
    #return(data.frame(study_or_comparison,study_id,area_id,band_id,gender,observed))
  })
  RES=RES[,-1]#We delete the sex column
  RES2=ddply(RES,.variables='area_id',.fun=function(x){
    study_or_comparison=x$study_or_comparison[1]
    study_id=x$study_id[1]
    area_id=x$area_id[1]
    band_id=x$band_id[1]
    exposure=x$exposure[1]
    gender=3
    observed=sum(x$observed)
    return(data.frame(study_or_comparison,study_id,area_id,band_id,exposure,gender,observed))
    #return(data.frame(study_or_comparison,study_id,area_id,band_id,gender,observed))
  })
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
    #sADJPOPNoAREA is the standardised pops from the comparison area pops for age and covariate adjustment
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
    
  } #end adj == false section

  data = RES
  
  #Now the rates have been calculated for each area, need to calulate the rates per band
  #Create dataframe with 1 row for each band/gender
  # For now we are not going to let the exposure get set within the band, so the 'linearity test' will not be run
  
  Bands = ddply(data, .variables=c('band_id','gender'),.fun=function(x){
    study_id=x$study_id[1]
    band_id=x$band_id[1]
    exposure = mean(x$exposure, na.rm = TRUE)
    gender = x$gender[1]
    observed = sum(x$observed)
    if (adj) { 
      expected = sum(x$EXP_ADJ)
    } else {
      expected = sum(x$EXP_UNADJ)
    }
    return(data.frame(study_id, band_id,exposure,gender,observed, expected))
    #return(data.frame(study_id, band_id,gender,observed, expected))
  })
  
  Bands$adjusted = adj
  Bands$RR=Bands$observed/Bands$expected
  
  # calculate the 95% CIs for the RR
  Bands$RRL95=apply(cbind(Bands$observed,Bands$expected),MARGIN=1,
                        FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(-1.96*sqrt(1/x[1])))
                        }else{return(0.5*qchisq(0.025,2*x[1])/x[2])}})
  Bands$RRU95=apply(cbind(Bands$observed,Bands$expected),MARGIN=1,
                        FUN=function(x){if (x[1]>100){return(x[1]/x[2]*exp(1.96*sqrt(1/x[1])))
                        }else{return(0.5*qchisq(0.975,2*x[1]+2)/x[2])}})
  
  

  
  # call the function to convert data to the format the db is expecting
  originalExtractTable = convertToDBFormatRA(Bands)
  return(originalExtractTable)
  
}

#Function to perform homogeneity tests on the data - pass in the band data generated by performBandAnal function
performHomogAnal <- function(Bands) {
  # The bands might be passed in as an atomic vector, so covert to a dataframe before use
  Bands = data.frame(Bands)
  #calculate the chi-square test for homogeneity (from p94 of the RIF v3.2 manual)
  # df is number of bands - 1
  
  #Establish what we have in the way of exposure data
  # if all the exposure is all NaNs then we substitute the bandid for the exposure
  # if some of the expsures are NaNs then we don't calcuate the linearity tests
  AllNaN = TRUE
  NoNaN = TRUE
  for (i in 1:length(Bands$exposure)){
    if (is.nan(Bands$exposure[i])){
      NoNaN = FALSE
    } else {
      AllNaN = FALSE
    }
  }
  
  if (AllNaN == TRUE) {
    Bands$exposure = Bands$band_id
    NoNaN = TRUE
  }
  
  # A pValHomog < 0.05 suggests the results are not homgenous across the bands (>95% likelihood)
  # A pValLT < 0.05 suggests the
  study_id = c()
  inv_id = c() 
  gender = c()
  df = c()
  chisqHomog = c()
  pValHomog = c()
  chisqLT = c()
  pValLT = c()
  bandsLT5 = c()
  
  HomogRes = data.frame(study_id, inv_id, gender,df, chisqHomog, pValHomog, bandsLT5)
  
  df = length(unique(Bands$band_id)) - 1
  for (i in 1:3)
  {
    Bandi = Bands[which(Bands$gender==i),]
    study_id = as.integer(as.character(studyID)) #Input parameter
    inv_id = as.integer(as.character(investigationId)) #Input parameter
    Osum = sum(Bandi$observed)
    Esum = sum(Bandi$expected)
    
    gender = i
    chisqHomog = sum( (Bandi$observed - (Bandi$expected*Osum/Esum))^2 / (Bandi$expected*Osum/Esum))
    pValHomog = pchisq(chisqHomog, df = df, lower.tail = FALSE)
    # Only do linearity tests if we have something in the exposure fields
    if (NoNaN == TRUE){
      numer = (sum(Bandi$exposure*(Bandi$observed - (Bandi$expected*Osum/Esum))))^2
      denom = sum((Bandi$exposure)^2 * (Bandi$expected*Osum/Esum)) - (((sum(Bandi$exposure * (Bandi$expected*Osum/Esum)))^2)/Osum)
      chisqLT = numer / denom
      pValLT = pchisq(chisqLT, df = df, lower.tail = FALSE)
    }
    bandsLT5 = length(which(Bandi$expected < 5))
    
    HomogRes = rbind(HomogRes, cbind(study_id, inv_id, gender,df, chisqHomog, pValHomog, chisqLT, pValLT, bandsLT5))
    #HomogRes = rbind(HomogRes, cbind(study_id, inv_id, gender,df, chisqHomog, pValHomog, bandsLT5))
  }  
  return(HomogRes)
  
}

