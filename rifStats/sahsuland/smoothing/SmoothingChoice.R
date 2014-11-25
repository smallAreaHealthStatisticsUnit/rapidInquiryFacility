
############################################################################################################
#   RIF PROJECT
#   Smoothing with either INLA,
#   adaptive Metropolis-Hastings within Gibbs, 
#   or adaptive rejection sampling within Gibbs
############################################################################################################
rm(list=ls()) 

library(INLA)
library(maptools)
library(spdep)
library(snow) #for parallel computing

setwd('P:/RIF/sahsuland')
data=read.table('exportR_results.csv',header=TRUE,sep=',')
source('P:/RIF/FunctionsR/RegionPlot.R')
source('P:/RIF/sahsuland/BYMupdate.R')
source('P:/RIF/sahsuland/BYMupdateSparse.R')

#################################

########################################################
#Function for determining the mean number of neighbours from INLA graph file
mean_neigh=function(file){
  con=file(file)
  open(con)
  nSubjects=as.numeric(readLines(con,n=1,warn=FALSE))
  nNeighbours=rep(NA,nSubjects)
  for (i in 1:nSubjects){
    tmp=as.numeric(strsplit(readLines(con,n=1,warn=FALSE),split=' ')[[1]])
    nNeighbours[i]=tmp[2]
  }
  close(con)
  return(mean(nNeighbours))
}

###########################################################################################
#Function for writing a matrix from a graph file
prec_Matrix=function(file=NULL){
  if (is.null(file)) file='Neighbours.txt'
  con=file(file)
  open(con)
  nSubjects=as.numeric(readLines(con,n=1,warn=FALSE))
  MAT=matrix(0,ncol=nSubjects,nrow=nSubjects)
  for (i in 1:nSubjects){
    tmp=as.numeric(strsplit(readLines(con,n=1,warn=FALSE),split=' ')[[1]])
    MAT[i,i]=tmp[2]
    MAT[i,tmp[3:length(tmp)]]=-1
  }
  close(con)
  return(MAT)
}

#########################################################################################
#Function for initializing a MCMC chain for BYM model 
initialize=function(logRR){
  vari=var(logRR,na.rm=TRUE)
  TAU2=rlnorm(1,meanlog=2/vari,sdlog=1)
  SIG2=rlnorm(1,meanlog=8/vari,sdlog=1)
  U=matrix(rnorm(n,0,1/TAU2),ncol=1)
  V=matrix(rnorm(n,0,1/SIG2),ncol=1)
  ALPHA=rnorm(1,0,1)
  Dbar=0
  thetabar=0
  SET=list(ALPHA=ALPHA,SIG2=SIG2,TAU2=TAU2,U=U,V=V,Dbar=Dbar,thetabar=thetabar)
  return(SET)
}

#########################################################################################
#THIS PART MAY BE AVOIDED IF WE DIRECTLY HAVE THE NEIHGBOR FILE
region=readShapeSpatial('shapefile2/SAHSU_GRD_Level4')
n=length(region)
nbo=poly2nb(region,snap=5)
summary(nbo)

#Case of island with add one link with the closest polygons
#identifying island
island=c()
for (i in 1:n){
  if (length(nbo[[i]])==1){ if(nbo[[i]]==0){island=c(island,i)}}
}
island=as.list(island)
#defining polygons closest to islands
coords=coordinates(region)
findclose=function(i,coords){
  coordi=coords[i,]
  distance=sqrt((coordi[1]-coords[,1])^2+(coordi[2]-coords[,2])^2)
  distance=as.vector(distance)
  distance[i]=Inf
  mini=min(distance)
  return(which(distance==mini))
}
add=lapply(island, FUN=findclose, coords=coords)   
#add link between polygons
for (i in 1:length(island)){
  nbo[[island[[i]]]]=add[[i]]
  nbo[[add[[i]]]]=c(nbo[[add[[i]]]],island[[i]])
  nbo[[add[[i]]]]=nbo[[add[[i]]]][order(nbo[[add[[i]]]])]  
}

plot(nbo,coords=coordinates(region))
nb2INLA(nbo, file='regionINLA.txt')

#################################################################################
#Define priors
prior='StandardPrior' # 'StandardPrior' or 'MolliesPrior'
if (prior!='StandardPrior' & prior!='MolliePrior'){
  Rprintf('Non valid value for variable prior, standard priors are used.')
  prior='StandardPrior'
}
if (prior=='StandardPrior'){
  #standard non-informative priors
  tau_u.Prior=list(c(0.001,0.001),c(0.001,0.001),c(0.001,0.001))
  tau_v.Prior=list(c(0.001,0.001),c(0.001,0.001),c(0.001,0.001))
}else if (prior=='MolliesPrior'){
  k=1000000
  nw=mean_neigh('regionINLA.txt')
  tau_u.Prior=list(c(0,0),c(0,0),c(0,0))
  tau_v.Prior=list(c(0,0),c(0,0),c(0,0))
  for (g in 1:3){
    s2x=var(log(data$RR_UNADJ[which(data$gender==g)]+0.0001))
    mu_u=s2x/(nw*2)
    tau_u.Prior[[g]]=c(mu_u/k+2,mu_u*(mu_u/k+1))
    mu_v=s2x/2
    tau_v.Prior[[g]]=c(mu_v/k+2,mu_v*(mu_v/k+1))
  }
}

#################################################################################
#Define inference method
InferenceMethod='INLA' # 'INLA', 'MCMC_MH', 'MCMC_ARS' 


#################################################################################
#order the RES dataframe into the right order (order of region)
data$area_id=as.character(data$area_id)
region@data$LEVEL4=as.character(region@data$LEVEL4)

data$BYM_ssRR=NA
data$BYM_ssRRL95=NA
data$BYM_ssRRU95=NA

data$BYM_RR=NA
data$BYM_RRL95=NA
data$BYM_RRU95=NA

data$region=NA

ordrel=list(g1=c(),g2=c(),g3=c())

for (g in c(1,2,3)){
  whichrows=which(data$gender==g)
  nsite=length(whichrows)
  ordrereturn=rep(NA,nsite)
  for (i in 1:nsite){
    ordrereturn[i]=which(region@data$LEVEL4==data$area_id[whichrows][i])
  }
  
  ordrel[[g]]=rep(NA,nsite)
  for (i in 1:nsite){
    ordrel[[g]][i]=which(data$area_id[whichrows]==region@data$LEVEL4[i])
  }
  
  if (InferenceMethod=='INLA'){
    data$region[whichrows][ordrel[[g]]]=seq(1:nsite)
    BYMformula=observed~f(region,model='bym',graph='regionINLA.txt', param = c(tau_v.Prior[[g]],tau_u.Prior[[g]]))
    result=inla(BYMformula, family='poisson', E=EXP_UNADJ, data=data[whichrows,][ordrel[[g]],])
    
    cte=result$summary.fixed[1]
    data$BYM_ssRR[whichrows]=exp(cte+result$summary.random$region[nsite+1:nsite,2])[ordrereturn]
    data$BYM_ssRRL95[whichrows]=exp(cte+result$summary.random$region[nsite+1:nsite,4])[ordrereturn]
    data$BYM_ssRRU95[whichrows]=exp(cte+result$summary.random$region[nsite+1:nsite,6])[ordrereturn]
    
    data$BYM_RR[whichrows]=exp(cte+result$summary.random$region[1:nsite,2])[ordrereturn]
    data$BYM_RRL95[whichrows]=exp(cte+result$summary.random$region[1:nsite,4])[ordrereturn]
    data$BYM_RRU95[whichrows]=exp(cte+result$summary.random$region[1:nsite,6])[ordrereturn]
  }else if (InferenceMethod=='MCMC_MH'|InferenceMethod=='MCMC_ARS'){
    SETlist=list()
    for (c in 1:3){
      SETlist[[c]]=initialize(log(data$RR_UNADJ[whichrows]+0.00001))
    }
    M=prec_Matrix('regionINLA.txt')
    cl <- makeCluster(3)
    clusterEvalQ(cl, library(spam))
    clusterEvalQ(cl, library(coda))
    clusterEvalQ(cl, library(ars))
    Sys.time()
    if (InferenceMethod=='MCMC_MH'){
    SET=parLapply(cl,SETlist,fun=BYM.UPDATE_MH,Y=data$observed[whichrows][ordrel[[g]]],E=data$EXP_UNADJ[whichrows][ordrel[[g]]],M=M,Nsimu=30000
                  ,au=tau_v.Prior[[g]][1],bu=tau_u.Prior[[g]][2],av=tau_v.Prior[[g]][1],bv=tau_v.Prior[[g]][2])
    }
    if (InferenceMethod=='MCMC_ARS'){
      SET=parLapply(cl,SETlist,fun=BYM.UPDATE_MH,Y=data$observed[whichrows][ordrel[[g]]],E=data$EXP_UNADJ[whichrows][ordrel[[g]]],M=M,Nsimu=30000
                    ,au=tau_v.Prior[[g]][1],bu=tau_u.Prior[[g]][2],av=tau_v.Prior[[g]][1],bv=tau_v.Prior[[g]][2])
    }
    Sys.time()
    RES=Extract_RES(SET)
    REStab=rbind(RES[[1]],RES[[2]],RES[[3]])
    stopCluster(cl)
    ssRR=exp(REStab[,3+1:nsite]+REStab[,1]%*%matrix(1,ncol=nsite,nrow=1))
    data$BYM_ssRR[whichrows]=colMeans(ssRR)[ordrereturn]
    data$BYM_ssRRL95[whichrows]=apply(ssRR,MARGIN=2,FUN=quantile,probs=0.975)[ordrereturn]
    data$BYM_ssRRU95[whichrows]=apply(ssRR,MARGIN=2,FUN=quantile,probs=0.025)[ordrereturn]
    RR=exp(REStab[,3+1:nsite]+REStab[,3+nsite+1:nsite]+REStab[,1]%*%matrix(1,ncol=nsite,nrow=1))
    data$BYM_RR[whichrows]=colMeans(RR)[ordrereturn]
    data$BYM_RRL95[whichrows]=apply(RR,MARGIN=2,FUN=quantile,probs=0.975)[ordrereturn]
    data$BYM_RRU95[whichrows]=apply(RR,MARGIN=2,FUN=quantile,probs=0.025)[ordrereturn]
  }
  
}


par(mfrow=c(1,2))
RegionPlot(data$BYM_ssRR[which(data$gender==g)][ordrel[[g]]], sp=region)
RegionPlot(data$BYM_RR[which(data$gender==g)][ordrel[[g]]], sp=region)
RegionPlot(data$RR_UNADJ[which(data$gender==g)][ordrel[[g]]], sp=region)
par(mfrow=c(1,1))

