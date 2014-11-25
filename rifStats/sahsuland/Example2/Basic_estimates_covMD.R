############################################################################################################
#   RIF PROJECT
#   Basic disease map results
#   Adjusted for covariates
############################################################################################################
rm(list=ls()) 

install.packages("plyr",lib="C:/Users/mdouglas/R packages");
library(plyr)
library(abind)

setwd('S:/Projects/SAHSU/RIF/RIF_stat/sahsuland/Example2')
data=read.table('sahsuland_example_extract.csv',header=TRUE,sep=',')
data$inv_1[which(is.na(data$inv_1))]=0
data$area_id=as.character(data$area_id)
comp=data[which(data$study_or_comparison=='C'),]
data=data[which(data$study_or_comparison=='S'),]
data=data[which(is.na(data$band_id)==FALSE),]

#enter names of adjustement variables (except age and group)
names.adj=c('ses')
#Find corresponding columns in data and comp
i.d.adj=c()
i.c.adj=c()
for (i in 1:length(names.adj)){
  id=which(names(data)==names.adj[i])
  ic=which(names(comp)==names.adj[i])
  if (length(id)==0|length(ic)==0){
    print(paste('covariate', names.adj[i], 'not found, data will not be adjusted for it!'))
  }else{
    i.d.adj=c(i.d.adj,id)
    i.c.adj=c(i.c.adj,ic)
  }
}
ncov=length(i.d.adj)

#Find NULL data within adjustement covariates and replace by 0
findNULL=function(x){return(sapply(x,FUN=function(y){ans=y
                                                     if (y=='NULL'){ans=0}
                                                     return(ans)}))}
for (i in 1:ncov){
  data[,i.d.adj[i]]=findNULL(data[,i.d.adj[i]])
  comp[,i.c.adj[i]]=findNULL(comp[,i.c.adj[i]])
  data[,i.d.adj[i]]=as.numeric(as.character(data[,i.d.adj[i]]))
  comp[,i.c.adj[i]]=as.numeric(as.character(comp[,i.c.adj[i]])) 
}

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

#produce study array taking counts for adjustement
FindAdjust=function(cADJRATES, StoC, StoCcomp){
  i=1 
  RES=cADJRATES[,,StoC[i],StoCcomp[i]]
  for (i in 2:length(StoC))
    RES=abind(RES,cADJRATES[,,StoC[i],StoCcomp[i]],along=3)
  return(RES)
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

conc=function(x){x=as.character(x)
                 res=c(x)
                 if (length(x)>1){for (i in 2:length(x)){res=paste(res,x[i],sep='-')}}
                 return(res)}
concADJ=apply(ADJ,MARGIN=1,FUN=conc)

#ADJcomb is a vector with all encoutered adjustement stratas, m the number of stratas
ADJcomb=unique(c(concADJ),)
ADJcomb=ADJcomb[order(ADJcomb)]
m=length(ADJcomb)

#Make comparative variables in a array of dim: age-sex-area-year-adjustement
#increase comp to compComplete so that there is one row for each combination of 
#age-sex-area-year-adjustement 
compComplete=expand.grid(unique(comp$age_group),unique(comp$sex),unique(comp$area_id),unique(comp$year),ADJcomb)
compComplete=as.data.frame(compComplete)
names(compComplete)=c('age_group','sex','area_id','year','comb')
compComplete$comb=as.character(compComplete$comb)
comp$comb=apply(as.matrix(comp[,i.c.adj],ncol=ncov),MARGIN=1,FUN=conc)
compComplete=merge(compComplete, comp,by=c('age_group','sex','area_id','year','comb'),all.x=TRUE)

#Fill the array for comparative areas
compComplete=compComplete[order(compComplete$comb,compComplete$year,compComplete$area_id,compComplete$sex,compComplete$age_group),]
cCASES=array(compComplete$inv_1,dim=c(length(unique(compComplete$age_group)),length(unique(compComplete$sex)),length(unique(compComplete$area_id)),length(unique(compComplete$year)),m))
cCASES=apply(cCASES,MARGIN=c(1,2,3,5),FUN=sum) #Mean over the years
cCASES=abind(cCASES,apply(cCASES,MARGIN=c(1,3,4),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)

cPOP=array(compComplete$total_pop,dim=c(length(unique(compComplete$age_group)),length(unique(compComplete$sex)),length(unique(compComplete$area_id)),length(unique(compComplete$year)),m))
cPOP=apply(cPOP,MARGIN=c(1,2,3,5),FUN=sum) #Mean over the years
cPOP=abind(cPOP,apply(cPOP,MARGIN=c(1,3,4),FUN=sum),along=2)#Add a third sex (sum of 1 and 2)

#unadjusted rates
cRATES=apply(cCASES,MARGIN=c(1,2,3),FUN=sum,na.rm=TRUE)/apply(cPOP,MARGIN=c(1,2,3),FUN=sum,na.rm=TRUE)
#adjusted rates
cADJRATES=cCASES/cPOP

#For checking (can be removed later)
#plot(apply(cCASES[,3,,1],MARGIN=1,FUN=sum,na.rm=TRUE)/apply(cPOP[,3,,1],MARGIN=1,FUN=sum,na.rm=TRUE),type='l',ylab='rates')
lines(apply(cCASES[,3,,2],MARGIN=1,FUN=sum,na.rm=TRUE)/apply(cPOP[,3,,2],MARGIN=1,FUN=sum,na.rm=TRUE),col='blue')
lines(apply(cCASES[,3,,3],MARGIN=1,FUN=sum,na.rm=TRUE)/apply(cPOP[,3,,3],MARGIN=1,FUN=sum,na.rm=TRUE),col='red')
lines(apply(cCASES[,3,,4],MARGIN=1,FUN=sum,na.rm=TRUE)/apply(cPOP[,3,,4],MARGIN=1,FUN=sum,na.rm=TRUE),col='darkgreen')
lines(apply(cCASES[,3,,5],MARGIN=1,FUN=sum,na.rm=TRUE)/apply(cPOP[,3,,5],MARGIN=1,FUN=sum,na.rm=TRUE),col='orange')


#link between study region and counts
S_area_id=array(data$area_id,dim=c(length(unique(data$age_group)),length(unique(data$sex)),length(unique(data$area_id)),length(unique(data$year))))[1,1,,1]
C_area_id=array(compComplete$area_id,dim=c(length(unique(compComplete$age_group)),length(unique(compComplete$sex)),length(unique(compComplete$area_id)),length(unique(compComplete$year)),m))
c_area_id=c()
for (i in 1:length(unique(compComplete$area_id))){
  c_area_id=c(c_area_id,C_area_id[,,i,,][which(is.na(C_area_id[,,i,,])==FALSE)][1]) 
}
C_area_id=c_area_id
StoC=sapply(S_area_id,FUN=function(x){which(C_area_id==substr(x,1,6))})
StoCcomp=sapply(concADJ,FUN=function(x){which(ADJcomb==x)})

#Random checks
ss=sample(1:length(S_area_id), 100)
cbind(S_area_id[ss],C_area_id[StoC[ss]])
cbind(concADJ[ss],ADJcomb[StoCcomp[ss]])

###NON ADJUSTED
#Expected number of cases non adjusted
RES$EXP_UNADJ=NA
RES$EXP_UNADJ[which(RES$gender==1)]=apply(POP[,1,]*cRATES[,1,StoC],MARGIN=2,FUN=sum)
RES$EXP_UNADJ[which(RES$gender==2)]=apply(POP[,2,]*cRATES[,2,StoC],MARGIN=2,FUN=sum)
RES$EXP_UNADJ[which(RES$gender==3)]=apply(POP[,3,]*cRATES[,3,StoC],MARGIN=2,FUN=sum)
RES[1:100,]

#Relative Risk non adjusted
RES$RR_UNADJ=NA
RES$RR_UNADJ[which(RES$gender==1)]=colSums(CASES[,1,])/RES$EXP_UNADJ[which(RES$gender==1)]
RES$RR_UNADJ[which(RES$gender==2)]=colSums(CASES[,2,])/RES$EXP_UNADJ[which(RES$gender==2)]
RES$RR_UNADJ[which(RES$gender==3)]=colSums(CASES[,3,])/RES$EXP_UNADJ[which(RES$gender==3)]
RES[1:100,]

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

#Rate non adjusted
RES$RATE_UNADJ=NA
cPOP3d=apply(cPOP,MARGIN=c(1,2,3),FUN=sum,na.rm=TRUE)
RES$RATE_UNADJ[which(RES$gender==1)]=colSums(RATES[,1,]*cPOP3d[,1,StoC])/colSums(cPOP3d[,1,StoC])*100000
RES$RATE_UNADJ[which(RES$gender==2)]=colSums(RATES[,2,]*cPOP3d[,2,StoC])/colSums(cPOP3d[,2,StoC])*100000
RES$RATE_UNADJ[which(RES$gender==3)]=colSums(RATES[,3,]*cPOP3d[,3,StoC])/colSums(cPOP3d[,3,StoC])*100000
RES[1:100,2:11]

#Rate Confidence interval non adjusted
SElog1=sqrt(colSums(cPOP3d[,1,StoC]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(cPOP3d[,1,StoC]*RATES[,1,])
SElog2=sqrt(colSums(cPOP3d[,2,StoC]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(cPOP3d[,2,StoC]*RATES[,2,])
SElog3=sqrt(colSums(cPOP3d[,3,StoC]^2*RATES[,3,]*(1-RATES[,3,])/POP[,3,]))/colSums(cPOP3d[,3,StoC]*RATES[,3,])

SE1=sqrt(colSums(cPOP3d[,1,StoC]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(cPOP3d[,1,StoC])*100000
SE2=sqrt(colSums(cPOP3d[,2,StoC]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(cPOP3d[,2,StoC])*100000
SE3=sqrt(colSums(cPOP3d[,3,StoC]^2*RATES[,1,]*(1-RATES[,3,])/POP[,3,]))/colSums(cPOP3d[,3,StoC])*100000

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

#Relative risk with Empirical bayesian estimates non adjusted
RES$SMRR_UNADJ=NA
RES$SMRR_UNADJ[which(RES$gender==1)]=EmpBayes(O=RES$observed[which(RES$gender==1)],E=RES$EXP_UNADJ[which(RES$gender==1)])
RES$SMRR_UNADJ[which(RES$gender==2)]=EmpBayes(O=RES$observed[which(RES$gender==2)],E=RES$EXP_UNADJ[which(RES$gender==2)])
RES$SMRR_UNADJ[which(RES$gender==3)]=EmpBayes(O=RES$observed[which(RES$gender==3)],E=RES$EXP_UNADJ[which(RES$gender==3)])


###ADJUSTED
#Expected number of cases adjusted
RES$EXP_ADJ=NA
sADJRATES=FindAdjust(cADJRATES, StoC=StoC, StoCcomp=StoCcomp)
RES$EXP_ADJ[which(RES$gender==1)]=apply(POP[,1,]*sADJRATES[,1,],MARGIN=2,FUN=sum)
RES$EXP_ADJ[which(RES$gender==2)]=apply(POP[,2,]*sADJRATES[,2,],MARGIN=2,FUN=sum)
RES$EXP_ADJ[which(RES$gender==3)]=apply(POP[,3,]*sADJRATES[,3,],MARGIN=2,FUN=sum)
RES[1:100,]

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
sADJPOP=FindAdjust(cPOP, StoC=StoC, StoCcomp=StoCcomp)
RES$RATE_ADJ=NA
RES$RATE_ADJ[which(RES$gender==1)]=colSums(RATES[,1,]*sADJPOP[,1,])/colSums(sADJPOP[,1,])*100000
RES$RATE_ADJ[which(RES$gender==2)]=colSums(RATES[,2,]*sADJPOP[,2,])/colSums(sADJPOP[,2,])*100000
RES$RATE_ADJ[which(RES$gender==3)]=colSums(RATES[,3,]*sADJPOP[,3,])/colSums(sADJPOP[,3,])*100000
RES[1:100,2:11]

#Rate Confidence interval adjusted
SElog1=sqrt(colSums(sADJPOP[,1,]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(sADJPOP[,1,]*RATES[,1,])
SElog2=sqrt(colSums(sADJPOP[,2,]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(sADJPOP[,2,]*RATES[,2,])
SElog3=sqrt(colSums(sADJPOP[,3,]^2*RATES[,3,]*(1-RATES[,3,])/POP[,3,]))/colSums(sADJPOP[,3,]*RATES[,3,])

SE1=sqrt(colSums(sADJPOP[,1,]^2*RATES[,1,]*(1-RATES[,1,])/POP[,1,]))/colSums(sADJPOP[,1,])*100000
SE2=sqrt(colSums(sADJPOP[,2,]^2*RATES[,2,]*(1-RATES[,2,])/POP[,2,]))/colSums(sADJPOP[,2,])*100000
SE3=sqrt(colSums(sADJPOP[,3,]^2*RATES[,1,]*(1-RATES[,3,])/POP[,3,]))/colSums(sADJPOP[,3,])*100000

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
RES$SMRR_ADJ[which(RES$gender==1)]=EmpBayes(O=RES$observed[which(RES$gender==1)],E=RES$EXP_ADJ[which(RES$gender==1)])
RES$SMRR_ADJ[which(RES$gender==2)]=EmpBayes(O=RES$observed[which(RES$gender==2)],E=RES$EXP_ADJ[which(RES$gender==2)])
RES$SMRR_ADJ[which(RES$gender==3)]=EmpBayes(O=RES$observed[which(RES$gender==3)],E=RES$EXP_ADJ[which(RES$gender==3)])

write.table(RES,file='Export_RresultsADJ_MD.csv', sep=',', row.names=FALSE)
