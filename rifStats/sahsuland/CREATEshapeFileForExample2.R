##########################################################################
###  IN EXAMPLE 2 SOME POLYGONS ARE MISSING IN THE DATASET            ####
###  THIS CODE AIMS AT IDENTIFYING THEM AND CREATING A NEW            #### 
###  SHAPE FILE IN WHICH THEY ARE EXCLUDED                            ####
##########################################################################

library(maptools)
setwd('P:/RIF/sahsuland')
#IMPORT DATASET
data=read.table('Example2/Export_RresultsADJ.csv',header=TRUE,sep=',')
#IMPORT SHAPEFILE
region=readShapeSpatial('shapefile2/SAHSU_GRD_Level4')
data$area_id=as.character(data$area_id)
region@data$LEVEL4=as.character(region@data$LEVEL4)

#IDENTIFY THE MISSING POLYGONS IN DATASET
missing=c()
for (i in 1:nsite){
  if (length(which(data$area_id==region@data$LEVEL4[i]))==0){missing=c(missing,i)}
}
region@data$LEVEL4[missing]

#Remove missings polygons from Spatial polygons and the dataset in region
dregion=region@data[-missing,]
pregion=region@polygons[-missing]
region2=SpatialPolygonsDataFrame(SpatialPolygons(pregion),dregion)
plot(region2)

writeSpatialShape(region2, fn='shapefile3/ForExample2')
