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
#   Mapping prototype
#
#   Generates an example PNG. Awaiting rela GEoJSON
#
############################################################################################################

## CHECK & AUTO INSTALL MISSING PACKAGES

#install.packages(c('leaflet', 'mapview', 'installr'))
#require('installr')
#install.pandoc()
#webshot::install_phantomjs()
# This installs phantomjs to C:\Users\admin\AppData\Roaming\PhantomJS
# Copy phatomJS executable into the 64 bit R bin directory
library(leaflet)
library(mapview)

rm(list=c("m"))

#
# This is a slight variation on: https://rstudio.github.io/leaflet/
# i.e. I added setView, scale bar, chnaged the baseMap
#
# Available maps: names(providers)
#
# OpenStreetMap
# OpenStreetMap.Mapnik
# OpenStreetMap.BlackAndWhite
# OpenStreetMap.DE
# OpenStreetMap.France
# OpenStreetMap.HOT
# OpenSeaMap
# OpenTopoMap
# Thunderforest
# Thunderforest.OpenCycleMap
# Thunderforest.Transport
# Thunderforest.TransportDark
# Thunderforest.SpinalMap
# Thunderforest.Landscape
# Thunderforest.Outdoors
# Thunderforest.Pioneer
# OpenMapSurfer
# OpenMapSurfer.Roads
# OpenMapSurfer.AdminBounds
# OpenMapSurfer.Grayscale
# Hydda
# Hydda.Full
# Hydda.Base
# Hydda.RoadsAndLabels
# MapBox
# Stamen
# Stamen.Toner
# Stamen.TonerBackground
# Stamen.TonerHybrid
# Stamen.TonerLines
# Stamen.TonerLabels
# Stamen.TonerLite
# Stamen.Watercolor
# Stamen.Terrain
# Stamen.TerrainBackground
# Stamen.TopOSMRelief
# Stamen.TopOSMFeatures
# Esri
# Esri.WorldStreetMap
# Esri.DeLorme
# Esri.WorldTopoMap
# Esri.WorldImagery
# Esri.WorldTerrain
# Esri.WorldShadedRelief
# Esri.WorldPhysical
# Esri.OceanBasemap
# Esri.NatGeoWorldMap
# Esri.WorldGrayCanvas
# OpenWeatherMap
# OpenWeatherMap.Clouds
# OpenWeatherMap.CloudsClassic
# OpenWeatherMap.Precipitation
# OpenWeatherMap.PrecipitationClassic
# OpenWeatherMap.Rain
# OpenWeatherMap.RainClassic
# OpenWeatherMap.Pressure
# OpenWeatherMap.PressureContour
# OpenWeatherMap.Wind
# OpenWeatherMap.Temperature
# OpenWeatherMap.Snow
# HERE
# HERE.normalDay
# HERE.normalDayCustom
# HERE.normalDayGrey
# HERE.normalDayMobile
# HERE.normalDayGreyMobile
# HERE.normalDayTransit
# HERE.normalDayTransitMobile
# HERE.normalNight
# HERE.normalNightMobile
# HERE.normalNightGrey
# HERE.normalNightGreyMobile
# HERE.basicMap
# HERE.mapLabels
# HERE.trafficFlow
# HERE.carnavDayGrey
# HERE.hybridDay
# HERE.hybridDayMobile
# HERE.pedestrianDay
# HERE.pedestrianNight
# HERE.satelliteDay
# HERE.terrainDay
# HERE.terrainDayMobile
# FreeMapSK
# MtbMap
# CartoDB
# CartoDB.Positron
# CartoDB.PositronNoLabels
# CartoDB.PositronOnlyLabels
# CartoDB.DarkMatter
# CartoDB.DarkMatterNoLabels
# CartoDB.DarkMatterOnlyLabels
# HikeBike
# HikeBike.HikeBike
# HikeBike.HillShading
# BasemapAT
# BasemapAT.basemap
# BasemapAT.grau
# BasemapAT.overlay
# BasemapAT.highdpi
# BasemapAT.orthofoto
# NASAGIBS
# NASAGIBS.ModisTerraTrueColorCR
# NASAGIBS.ModisTerraBands367CR
# NASAGIBS.ViirsEarthAtNight2012
# NASAGIBS.ModisTerraLSTDay
# NASAGIBS.ModisTerraSnowCover
# NASAGIBS.ModisTerraAOD
# NASAGIBS.ModisTerraChlorophyll
# NLS
#
m <- leaflet(options = leafletOptions(minZoom = 0, maxZoom = 14)) %>% setView(lng=174.768, lat=-36.852, zoom = 12) %>% 
	addProviderTiles(providers$Stamen.Toner) %>% 
	addMarkers(lng=174.768, lat=-36.852, popup="The birthplace of R") %>%
	addScaleBar()
#
# To display in a web browser:
# m
#
mapshot(m, file = "Rbirthplace.png")
#
# It should also be possible to create self contained HTML