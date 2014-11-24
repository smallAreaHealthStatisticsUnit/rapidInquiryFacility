#
# Error handler
#
#options(error = traceback())

Rmessages <- file("Rmessages.txt", open = "wt")
Routput <- file("Routput.txt", open = "wt")
sink(Rmessages)
sink(Routput)
sink(Rmessages, type = "message")
sink(Routput, type = "output")
options(verbose=TRUE)

#
# Set R repository
#
r <- getOption("repos")
r["CRAN"] <- "http://cran.ma.imperial.ac.uk/"
options(repos = r)
#
# Update
#
update.packages(checkBuilt=TRUE, ask=FALSE)
#
# Install INLA
#
#source("http://www.math.ntnu.no/inla/givemeINLA.R") 
#inla.version()
#
# Install GDAL
#
#install("rgdal")
#
# List libraries
#
print(installed.packages())

#
# Eof