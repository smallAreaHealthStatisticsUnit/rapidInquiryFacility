############################################################################################################
#  Massage SEER data for dataloader
############################################################################################################
#rm(list=ls()) 

library(plyr)
#library(abind)

setwd('C:/RIF/SAHSULand Update')


#Read in the original SAHSU cancer data
SAHSUCancer = read.table('num_sahsuland_cancer_ICD10.csv',header = TRUE,  colClasses = "character", sep=',')
SAHSUPop = read.table('pop_sahsuland_pop_extended.csv',header = TRUE,  colClasses = "character", sep=',')

NewPop = SAHSUPop[which(SAHSUPop$year > 1996),]
# Using the cancer data we do have (years 1989 - 1996), for each cancer type, for each age-sex-group, work out the risk (per person)
# of developing a cancer of that type in any one year. Then randomly assign cases using those risks
OrigPop = SAHSUPop[which(SAHSUPop$year < 1997),]
PopSummary = ddply(OrigPop, "age_sex_group", summarise, pop = sum(as.numeric(total)))
#Get cases per sex age group/ICD
CancerSummary = ddply(SAHSUCancer, .(age_sex_group,icd), summarise, cases = sum(as.numeric(total)))
risk = 0
CancerSummary = cbind(CancerSummary,risk )
for (i in 1:length(CancerSummary[,1])){
#for (i in 1:10){
  CancerSummary$risk[i] = CancerSummary$cases[i] / PopSummary$pop[which(PopSummary$age_sex_group == CancerSummary$age_sex_group[i])]
}
CancerSummary = cbind(CancerSummary,risk )

NewCancer = data.frame()
year = c()
age_sex_group = c()
sahsu_grd_level1 = c()
sahsu_grd_level2 = c()
sahsu_grd_level3 = c()
sahsu_grd_level4 = c()
icd = c()
total = c()
NewCancer = cbind(NewCancer, year, age_sex_group, sahsu_grd_level1,sahsu_grd_level2,sahsu_grd_level3,sahsu_grd_level4,icd,total)

#NewPop1Year = NewPop[which(NewPop$year == 1997),]

# We now have the risk for each grouping. Now need to randomly allocate cases as per the risk.
newrow = 0
for (i in 1:length(NewPop[,1])){
#for (i in 1:length(NewPop1Year[,1])){
    
  thisAgeSex = CancerSummary[which(CancerSummary$age_sex_group == NewPop$age_sex_group[i]),]
  #iterate through each ICD code 
  for (j in 1:length(thisAgeSex[,1])){
    
    # work out how many cases there are for this age-sex group
    risk = thisAgeSex$risk[j]
    cases = sum(sample(c(0,1), NewPop$total[i], replace = TRUE, prob = c(1-risk,risk)))
    if(cases > 0) {
      #add a new row to the cancer stats
      year = NewPop$year[i]
      age_sex_group =  NewPop$age_sex_group[i]
      sahsu_grd_level1 = NewPop$sahsu_grd_level1[i]
      sahsu_grd_level2 = NewPop$sahsu_grd_level2[i]
      sahsu_grd_level3 = NewPop$sahsu_grd_level3[i]
      sahsu_grd_level4 = NewPop$sahsu_grd_level4[i]
      icd = thisAgeSex$icd[j]
      total = cases
      NewCancerRow = cbind(year, age_sex_group, sahsu_grd_level1,sahsu_grd_level2,sahsu_grd_level3,sahsu_grd_level4,icd,total)
      
      # add new row to existing
      NewCancer = rbind(NewCancer, NewCancerRow)
      newrow = newrow + 1
      if (newrow %% 1000 == 0) {
        cat(paste(newrow, ' '), sep="")
      }
      
      
    }
    
  }
  
}


#write the new data
#write.table(ICD10SAHSUCancer, file = 'num_sahsuland_cancer_ICD10.csv', row.name = FALSE, quote= FALSE, sep = ',') 
