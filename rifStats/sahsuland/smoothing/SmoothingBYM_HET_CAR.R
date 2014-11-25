############################################################################################################
#   RIF PROJECT
#   Smoothing with INLA
############################################################################################################
rm(list=ls()) 

library(INLA)
library(maptools)
library(spdep)

setwd('P:/RIF/sahsuland')
data=read.table('Example1/exportR_results.csv',header=TRUE,sep=',')
source('P:/RIF/FunctionsR/RegionPlot.R')

#########################################################################################
#SELECT MODEL 
model='HET' #Either 'BYM', 'CAR' or 'HET'
#SELECT adj or not adj
adj=FALSE #either true or false
if (adj==FALSE){
  Exp='EXP_UNADJ'
  Sadj='UNADJ'
}else{
  Exp='EXP_ADJ'
  Sadj='ADJ'
}
if (length(which(names(data)==Exp))==0) {print('The Expected counts for the adjustement given does not exist in data.')}

#########################################################################################
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

#plot(nbo,coords=coordinates(region))
nb2INLA(nbo, file='regionINLA.txt')

#################################################################################
#order the RES dataframe into the right order (order of region)
data$area_id=as.character(data$area_id)
region@data$LEVEL4=as.character(region@data$LEVEL4)

if (adj==FALSE){
  if (model=='BYM'){
    formula=observed~f(region,model='bym',graph='regionINLA.txt', 
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
    formula=observed~f(region, model='iid',
                       hyper=list(prec=list(param=c(0.5,0.0005))))
    data$HET_RR_UNADJ=NA
    data$HET_RRL95_UNADJ=NA
    data$HET_RRU95_UNADJ=NA
  }
  if (model=='CAR'){
    formula=observed~f(region, model='besag',graph='regionINLA.txt',
                       hyper=list(prec=list(param=c(0.5,0.0005))))
    data$CAR_RR_UNADJ=NA
    data$CAR_RRL95_UNADJ=NA
    data$CAR_RRU95_UNADJ=NA
  }
}else {
  if (model=='BYM'){
    formula=observed~f(region,model='bym',graph='regionINLA.txt', 
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
    formula=observed~f(region, model='iid',
                       hyper=list(prec=list(param=c(0.5,0.0005))))
    data$HET_RR_ADJ=NA
    data$HET_RRL95_ADJ=NA
    data$HET_RRU95_ADJ=NA
  }
  if (model=='CAR'){
    formula=observed~f(region, model='besag',graph='regionINLA.txt',
                       hyper=list(prec=list(param=c(0.5,0.0005))))
    data$CAR_RR_ADJ=NA
    data$CAR_RRL95_ADJ=NA
    data$CAR_RRU95_ADJ=NA
  }
}

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
  
  data$region[whichrows][ordrel[[g]]]=seq(1:nsite)
  result=inla(formula, family='poisson', E=get(Exp), data=data[whichrows,][ordrel[[g]],])
  
  if (adj==FALSE){
    if (model=='BYM'){
      cte=result$summary.fixed[1]
      data$BYM_ssRR_UNADJ[whichrows]=exp(cte+result$summary.random$region[nsite+1:nsite,2])[ordrereturn]
      data$BYM_ssRRL95_UNADJ[whichrows]=exp(cte+result$summary.random$region[nsite+1:nsite,4])[ordrereturn]
      data$BYM_ssRRU95_UNADJ[whichrows]=exp(cte+result$summary.random$region[nsite+1:nsite,6])[ordrereturn]
      
      data$BYM_RR_UNADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,2])[ordrereturn]
      data$BYM_RRL95_UNADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,4])[ordrereturn]
      data$BYM_RRU95_UNADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,6])[ordrereturn]  
    }
    if (model=='HET'){
      cte=result$summary.fixed[1]
      data$HET_RR_UNADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,2])[ordrereturn]
      data$HET_RRL95_UNADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,4])[ordrereturn]
      data$HET_RRU95_UNADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,6])[ordrereturn]  
    }
    if (model=='CAR'){
      cte=result$summary.fixed[1]
      data$CAR_RR_UNADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,2])[ordrereturn]
      data$CAR_RRL95_UNADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,4])[ordrereturn]
      data$CAR_RRU95_UNADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,6])[ordrereturn]  
    }
  }else {
    if (model=='BYM'){
      cte=result$summary.fixed[1]
      data$BYM_ssRR_ADJ[whichrows]=exp(cte+result$summary.random$region[nsite+1:nsite,2])[ordrereturn]
      data$BYM_ssRRL95_ADJ[whichrows]=exp(cte+result$summary.random$region[nsite+1:nsite,4])[ordrereturn]
      data$BYM_ssRRU95_ADJ[whichrows]=exp(cte+result$summary.random$region[nsite+1:nsite,6])[ordrereturn]
      
      data$BYM_RR_ADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,2])[ordrereturn]
      data$BYM_RRL95_ADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,4])[ordrereturn]
      data$BYM_RRU95_ADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,6])[ordrereturn]  
    }
    if (model=='HET'){
      cte=result$summary.fixed[1]
      data$HET_RR_ADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,2])[ordrereturn]
      data$HET_RRL95_ADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,4])[ordrereturn]
      data$HET_RRU95_ADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,6])[ordrereturn]  
    }
    if (model=='CAR'){
      cte=result$summary.fixed[1]
      data$CAR_RR_ADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,2])[ordrereturn]
      data$CAR_RRL95_ADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,4])[ordrereturn]
      data$CAR_RRU95_ADJ[whichrows]=exp(cte+result$summary.random$region[1:nsite,6])[ordrereturn]  
    }
  }
}


# par(mfrow=c(1,2))
# RegionPlot(data$CAR_ssRR_UNADJ[which(data$gender==g)][ordrel[[g]]], sp=region)
# RegionPlot(data$HET_RR_UNADJ[which(data$gender==g)][ordrel[[g]]], sp=region)
# RegionPlot(data$RR_UNADJ[which(data$gender==g)][ordrel[[g]]], sp=region)
# par(mfrow=c(1,1))

write.table(data,'Example1/exportR_resultsSmooth.csv',sep=',',row.names=FALSE)
