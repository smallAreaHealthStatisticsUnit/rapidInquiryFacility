############################################################################################################
#  Massage SEER data for dataloader
############################################################################################################
#rm(list=ls()) 

library(plyr)
#library(abind)

setwd('C:/RIF/SAHSULand Update')


#Read in the original SAHSU cancer data
OrigSAHSUCancer = read.table('num_sahsuland_cancer.csv',header = TRUE,  colClasses = "character", sep=',')
#Read the lookup table to convert ICD9 to ICD10
ICD9to10 = read.table('CID9to10.csv',header = TRUE,  colClasses = "character", sep=',')
ICD10SAHSUCancer = OrigSAHSUCancer

for (i in 1:length(ICD9to10[,1])){
  # Alter all the rows in the new cancer table
  ICD10SAHSUCancer$icd[which(ICD10SAHSUCancer$icd == ICD9to10$icd9[i])] = ICD9to10$icd10[i]
}

#write the new data
write.table(ICD10SAHSUCancer, file = 'num_sahsuland_cancer_ICD10.csv', row.name = FALSE, quote= FALSE, sep = ',') 
  
