R code for RIF 4.0

RIF Stats.rar: Archive of previous versions 

RIF_Manual_stat.docx: A statistical manual for RIF

PRES_BYM.ppt: a power point presentation of the RIF model and implemantation in RIF

u.png, uplusv.png, v.png, graph.png: pictires imported in the PRES_BYM prosentation.
 

Folders: 
	- documentation
A set of statistical paper relative to the R code development:
About INLA: Blangiardo2012, Rue2009
About emprirical bayes: Bernardinelli1992
About confidence interval of Poisson distribution: Dobson1991
About CARbayes package: Lee2012 (This package is finally not used)
About BYM and MH: Furrer (At this time, paper is still not published, it is a copy obtained from Reinhard Furrer.)

	- FunctionsR 
Usefull functions not dedicated uniquely to RIF: 
RegionPlot.R: A R function for mapping from a SpatialPolygons object (sp) and a numeric or factor vector. The vector represent the site quantities to map, be carefull that they are in the same order than the sp object. 

	- sahsuland
		o shapefile: shapefile for Example1 

		o shapefile: shapefile for Example1 
  
                o shapefile3: shapefile for Example2

		o Example1:
Basic_estimates2.R: 
  R code for computing the basic estimates: rates, and SMR with their confidence   intervals in case of NON adjustement variables. This code is now obsolet and can be replace by code   supply in Example2. 
exporR_results.csv :
  Output table build by Basic-estimates2.R
sahsuland_example_extract.csv :
  Input table without adjustement variable. 

		o Example2
Basic_estimates_cov: 
  A program for computing both adjusted and non-adjusted basic estimates from the input table   sahsuland_example_extract.csv
  Note that the list of adjustment covariates should be given in input. The adjustment covariates   should be numeric and are considered as qualitative data. This program deals with several covariates   but as been checked with only one covariate.
  Currently, a ‘0’ value can be introduced when the data is missing for both study or comparative   areas. The missing value is thus treated as strata, and if there is a missing value in the study   data frame, a missing value must be encountered in the comparative dataset, as well.  Currently, the   function ‘FindAdjust’ consider the missing value as strata, but can be changed later. 
  This file may be considered as a new version of Basic_Estimate2.R. 
sahsuland_example_extract.csv:
  Input tablewith adjustement variable 
Export_RresultsADJ.csv: 
  Output table

		o Smoothing
Smoothing.R: 
  A programm for smoothing with INLA, only one model (the BYM model), and one prior is considered   (the defult given by INLA). The neighbourhood graph file is produced from the shape-file and is   'regionINLA.txt' (obsolet). 

smoothingBYM_HET_CAR.R: A program for smoothing (up to date) with choice of model and choice of expected counts (adjusted or unadjusted) for smmothing.
Be careful to the lines for
  - setting directory : setwd(name of the directory)
  - reading data : read.data(name of dataset, ...)
  - reading shape file : readShapeSpatial()
  - writing data: write.data(the dataframe to write, name of the file)
If example1 is used, then the option 'adj=FALSE' is not available and the shape file must be 'shapefile2/SAHSU_GRD_LEVEL4'
If example2 is used, the option 'adj=TRUE' is available and the shapefile must be 'shapefile3/ForExample2'

BYMupdate.R: 
  A MCMC algorithm for sampling in the posterior distribution for the BYM model, using the adptive 
  rejection sampling for sampling into the non-conjugated Poisson-Normal distributions. 
  BYM.UPDATE_ARS=function(SET,Y,E,M,Nsimu=1000,thin=1,au=0.001,bu=0.001,av=0.001,bv=0.001,DIC=FALSE)
  SET: a list with initial values, or previous samples
  Y: The vector of data
  E: The expected counts 
  M: The precision matrix of the iCAR 
  Nsimu: The number of samples to be done 
  thin: The thin parameter (only 1 value every thin each recorded)
  au: shape parameter for iCAR variance 
  bu: rate parameter for iCAR variance 
  av: shape parameter for noise variance 
  bv: rate parameter for noise variance
  DIC: Is the DIC should be computed. 

BYMupdateSparse.R:
  A MCMC algorithm for sampling into the posterior distribution of the BYM model. Here a adaptive     Metropolis Hastings algorithm is used to sample into the poisson-normal posterio distribution, as     defined by Furrer.
  BYM.UPDATE_MH=function(SET,Y,E,M,Nsimu=1000,thin=1,au=0.001,bu=0.001,av=0.001,bv=0.001,DIC=FALSE)
  SET: a list with initial values, or previous samples
  Y: The vector of data
  E: The expected counts 
  M: The precision matrix of the iCAR 
  Nsimu: The number of samples to be done 
  thin: The thin parameter (only 1 value every thin each recorded)
  au: shape parameter for iCAR variance 
  bu: rate parameter for iCAR variance 
  av: shape parameter for noise variance 
  bv: rate parameter for noise variance
  DIC: Is the DIC should be computed. 

SmoothingChoice.R: 
  Implementation of the BYM model with choices about: 
    x The prior (discussed IG (0.001, 0.001) or other non parametric inspired from Mollie in MCMC in       Practice. (book))
    x The inference method: INLA, MCMC with ARS (adaptive rejection sampling), MCMC with Metropolis       Hastings
  Note that MCMC methods raise a problem. The convergence must be checked and this cannot be done   automatically, as a consequence further graphical outputs should be added. 

exportsR_results_BYM.csv : 
  output table from smoothing.R with bayesian estimates of the BYM model

regionINLA.txt: 
  Neighbouring graph used in INLA. 
