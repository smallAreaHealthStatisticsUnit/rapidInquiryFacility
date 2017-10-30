############################################################################################################
#  Massage SEER data for dataloader
############################################################################################################
#rm(list=ls()) 

library(plyr)
#library(abind)

setwd('C:/RIF/SAHSULand Update')


#Read in the original SAHSU cancer data
OrigSAHSUPop = read.table('pop_sahsuland_pop.csv',header = TRUE,  colClasses = "character", sep=',')
OrigSAHSUPop$total = as.numeric(OrigSAHSUPop$total)
ExtendedPop = OrigSAHSUPop

#Create a template dataset of 1 year of data
TemplateYear = OrigSAHSUPop[which(OrigSAHSUPop$year == 1989),]
TemplateYear$total = 0
#order by the level 4 area
TemplateYear = TemplateYear[order(TemplateYear$age_sex_group, TemplateYear$sahsu_grd_level4),]

for (y in 1997:2016){
  # add 20 new years of data
  
  # try just aging the population. slight increase in birth rate
  ThisYear = TemplateYear
  ThisYear$year = y
  #get the previous year
  PrevYear =  ExtendedPop[which(ExtendedPop$year == y - 1),]
  #order by the level 4 area
  PrevYear = PrevYear[order(PrevYear$age_sex_group, PrevYear$sahsu_grd_level4),]
  
  #Just copy the first 5 years (which are stored individually)
  ThisYear$total[which(ThisYear$age_sex_group == 100)] = round (1.01 * PrevYear$total[which(PrevYear$age_sex_group == 100)])
  ThisYear$total[which(ThisYear$age_sex_group == 101)] = PrevYear$total[which(PrevYear$age_sex_group == 100)]
  ThisYear$total[which(ThisYear$age_sex_group == 102)] = PrevYear$total[which(PrevYear$age_sex_group == 101)]
  ThisYear$total[which(ThisYear$age_sex_group == 103)] = PrevYear$total[which(PrevYear$age_sex_group == 102)]
  ThisYear$total[which(ThisYear$age_sex_group == 104)] = PrevYear$total[which(PrevYear$age_sex_group == 103)]
  ThisYear$total[which(ThisYear$age_sex_group == 200)] =  round (1.01 * PrevYear$total[which(PrevYear$age_sex_group == 200)])
  ThisYear$total[which(ThisYear$age_sex_group == 201)] = PrevYear$total[which(PrevYear$age_sex_group == 200)]
  ThisYear$total[which(ThisYear$age_sex_group == 202)] = PrevYear$total[which(PrevYear$age_sex_group == 201)]
  ThisYear$total[which(ThisYear$age_sex_group == 203)] = PrevYear$total[which(PrevYear$age_sex_group == 202)]
  ThisYear$total[which(ThisYear$age_sex_group == 204)] = PrevYear$total[which(PrevYear$age_sex_group == 203)]
  
  #Age group 5-9 is 4/5  of the previous year's age sex group plus the age 4 group from previous year
  ThisYear$total[which(ThisYear$age_sex_group == 105)] = round((0.8 * PrevYear$total[which(PrevYear$age_sex_group == 105)])
    + PrevYear$total[which(PrevYear$age_sex_group == 104)])
  ThisYear$total[which(ThisYear$age_sex_group == 205)] = round((0.8 * PrevYear$total[which(PrevYear$age_sex_group == 205)])
   + PrevYear$total[which(PrevYear$age_sex_group == 204)])
  
  #now do similar for the other age groupings
  for (i in 6:21){
    ThisYear$total[which(ThisYear$age_sex_group == 100 + i)] = round((0.8 * PrevYear$total[which(PrevYear$age_sex_group == 100 + i)])
                                                                 + (0.2 * PrevYear$total[which(PrevYear$age_sex_group == 100 + i - 1)]))
    ThisYear$total[which(ThisYear$age_sex_group == 200 + i)] = round((0.8 * PrevYear$total[which(PrevYear$age_sex_group == 200 + i)])
                                                                 + (0.2 * PrevYear$total[which(PrevYear$age_sex_group == 200 + i - 1)]))
  }
  
  #Take 2% off the last 4 age groups 
  ThisYear$total[which(ThisYear$age_sex_group == 118)] = round (0.98 * ThisYear$total[which(ThisYear$age_sex_group == 118)])
  ThisYear$total[which(ThisYear$age_sex_group == 119)] = round (0.98 * ThisYear$total[which(ThisYear$age_sex_group == 119)])
  ThisYear$total[which(ThisYear$age_sex_group == 120)] = round (0.98 * ThisYear$total[which(ThisYear$age_sex_group == 120)])
  ThisYear$total[which(ThisYear$age_sex_group == 121)] = round (0.98 * ThisYear$total[which(ThisYear$age_sex_group == 121)])
  ThisYear$total[which(ThisYear$age_sex_group == 218)] = round (0.98 * ThisYear$total[which(ThisYear$age_sex_group == 218)])
  ThisYear$total[which(ThisYear$age_sex_group == 219)] = round (0.98 * ThisYear$total[which(ThisYear$age_sex_group == 219)])
  ThisYear$total[which(ThisYear$age_sex_group == 220)] = round (0.98 * ThisYear$total[which(ThisYear$age_sex_group == 220)])
  ThisYear$total[which(ThisYear$age_sex_group == 221)] = round (0.98 * ThisYear$total[which(ThisYear$age_sex_group == 221)])
  
  #Add the new year to the original
  ExtendedPop = rbind(ExtendedPop, ThisYear)
}

#write the new data
write.table(ExtendedPop, file = 'pop_sahsuland_pop_extended.csv', row.name = FALSE, quote= FALSE, sep = ',') 
  
