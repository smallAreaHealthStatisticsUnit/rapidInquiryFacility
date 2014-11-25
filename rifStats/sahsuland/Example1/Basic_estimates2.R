############################################################################################################
#   RIF PROJECT
#   Basic disease map results
############################################################################################################
rm(list=ls()) 

library(plyr)
library(abind)

setwd('P:/RIF/sahsuland')
data=read.table('sahsuland_example_extract.csv',header=TRUE,sep=',')
data$inv_1[which(is.na(data$inv_1))]=0
data$area_id=as.character(data$area_id)
comp=data[which(data$study_or_comparison=='C'),]
data=data[which(data$study_or_comparison=='S'),]
data=data[which(is.na(data$band_id)==FALSE),]

EmpBayes=function(O,E){
  #initialize
  N=length(O)
  theta=O/E
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

#Idem for comp
comp=comp[order(comp$year,comp$area_id,comp$sex,comp$age_group),]
# dim1=age, dim2=sex, dim3=area, dim4=years
cCASES=array(comp$inv_1,dim=c(length(unique(comp$age_group)),length(unique(comp$sex)),length(unique(comp$area_id)),length(unique(comp$year))))
cCASES=apply(cCASES,MARGIN=c(1,2,3),FUN=sum) #Mean over the years
cCASES=abind(cCASES,apply(cCASES,MARGIN=c(1,3),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)

cPOP=array(comp$total_pop,dim=c(length(unique(comp$age_group)),length(unique(comp$sex)),length(unique(comp$area_id)),length(unique(comp$year))))
cPOP=apply(cPOP,MARGIN=c(1,2,3),FUN=sum) #Mean over the years
cPOP=abind(cPOP,apply(cPOP,MARGIN=c(1,3),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)

cRATES=cCASES/cPOP

#link between study region and counts
S_area_id=array(data$area_id,dim=c(length(unique(data$age_group)),length(unique(data$sex)),length(unique(data$area_id)),length(unique(data$year))))[1,1,,1]
C_area_id=array(comp$area_id,dim=c(length(unique(comp$age_group)),length(unique(comp$sex)),length(unique(comp$area_id)),length(unique(comp$year))))[1,1,,1]
StoC=sapply(S_area_id,FUN=function(x){which(C_area_id==substr(x,1,6))})
#Random checks
ss=sample(1:length(S_area_id), 100)
cbind(S_area_id[ss],C_area_id[StoC[ss]])

#Expected number of cases 
RES$EXP_UNADJ=NA
RES$EXP_UNADJ[which(RES$gender==1)]=apply(POP[,1,]*cRATES[,1,StoC],MARGIN=2,FUN=sum)
RES$EXP_UNADJ[which(RES$gender==2)]=apply(POP[,2,]*cRATES[,2,StoC],MARGIN=2,FUN=sum)
RES$EXP_UNADJ[which(RES$gender==3)]=apply(POP[,3,]*cRATES[,3,StoC],MARGIN=2,FUN=sum)
RES[1:100,]


#Relative Risk
RES$RR_UNADJ=NA
RES$RR_UNADJ[which(RES$gender==1)]=colSums(CASES[,1,])/RES$EXP_UNADJ[which(RES$gender==1)]
RES$RR_UNADJ[which(RES$gender==2)]=colSums(CASES[,2,])/RES$EXP_UNADJ[which(RES$gender==2)]
RES$RR_UNADJ[which(RES$gender==3)]=colSums(CASES[,3,])/RES$EXP_UNADJ[which(RES$gender==3)]
RES[1:100,]

#Lower 95 percent interval
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

#Upper 95 percent interval
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
RES[1:100,]

#Rate 
RES$RATE_UNADJ=NA
RES$RATE_UNADJ[which(RES$gender==1)]=colSums(RATES[,1,]*cPOP[,1,StoC])/colSums(cPOP[,1,StoC])*100000
RES$RATE_UNADJ[which(RES$gender==2)]=colSums(RATES[,2,]*cPOP[,2,StoC])/colSums(cPOP[,2,StoC])*100000
RES$RATE_UNADJ[which(RES$gender==3)]=colSums(RATES[,3,]*cPOP[,3,StoC])/colSums(cPOP[,3,StoC])*100000
RES[1:100,2:11]

#Rate Confidence interval
SElog1=sqrt(colSums(cPOP[,1,StoC]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(cPOP[,1,StoC]*RATES[,1,])
SElog2=sqrt(colSums(cPOP[,2,StoC]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(cPOP[,2,StoC]*RATES[,2,])
SElog3=sqrt(colSums(cPOP[,3,StoC]^2*RATES[,3,]*(1-RATES[,3,])/POP[,3,]))/colSums(cPOP[,3,StoC]*RATES[,3,])

SE1=sqrt(colSums(cPOP[,1,StoC]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(cPOP[,1,StoC])*100000
SE2=sqrt(colSums(cPOP[,2,StoC]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(cPOP[,2,StoC])*100000
SE3=sqrt(colSums(cPOP[,3,StoC]^2*RATES[,1,]*(1-RATES[,3,])/POP[,3,]))/colSums(cPOP[,3,StoC])*100000

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



#Relative risk with Empirical bayesian estimates
RES$SMRR_UNADJ=NA
RES$SMRR_UNADJ[which(RES$gender==1)]=EmpBayes(O=RES$observed[which(RES$gender==1)],E=RES$EXP_UNADJ[which(RES$gender==1)])
RES$SMRR_UNADJ[which(RES$gender==2)]=EmpBayes(O=RES$observed[which(RES$gender==2)],E=RES$EXP_UNADJ[which(RES$gender==2)])
RES$SMRR_UNADJ[which(RES$gender==3)]=EmpBayes(O=RES$observed[which(RES$gender==3)],E=RES$EXP_UNADJ[which(RES$gender==3)])

write.table(RES,file='exportR_results.csv',sep=',',row.names=FALSE)

RES[1:100,5:14]


