setwd('C:/RIF/SAHSULand Update')


#Read in the original covariater data
covar3 = read.table('covar_sahsuland_covariates3.csv',header = TRUE,  colClasses = "character", sep=',')
covar4 = read.table('covar_sahsuland_covariates4.csv',header = TRUE,  colClasses = "character", sep=',')

covar3_1989 = covar3[which(covar3$year == 1989),]
covar4_1989 = covar4[which(covar4$year == 1989),]

for (i in 1997:2016){
  covar3_thisyear = covar3_1989
  covar3_thisyear$year = i
  covar3 = rbind(covar3, covar3_thisyear)
  
  covar4_thisyear = covar4_1989
  covar4_thisyear$year = i
  covar4 = rbind(covar4, covar4_thisyear)
}

covar3$year = as.numeric(covar3$year)
covar3$ses = as.numeric(covar3$ses)
covar3$ethnicity = as.numeric(covar3$ethnicity)

covar4$year = as.numeric(covar4$year)
covar4$ses = as.numeric(covar4$ses)
covar4$areatri1km = as.numeric(covar4$areatri1km)
covar4$near_dist = as.numeric(covar4$near_dist)
#write data
write.table(covar3, file = 'covar_sahsuland_covariates3_extended.csv', row.name = FALSE, quote= FALSE, sep = ',') 
write.table(covar4, file = 'covar_sahsuland_covariates4_extended.csv', row.name = FALSE, quote= FALSE, sep = ',') 
