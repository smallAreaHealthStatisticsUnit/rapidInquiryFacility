# Federico Fabbri Progress Report RIF 4.0

Principal Work Area: JavaScript User interface development

#2015
##May
For a technical overview of the progress please have a look at my own commits here:https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/commits?author=fedeee 


#### 1st- 5th 

Study Summary

Allow to visualize a summary of all parameters set within the diseaseSubmission before submitting the study to the Middleware.


#### 6th- 14th 

Map component refactor
The map component developed as part of the disease mapping viewer prototype has needed a fair amount of refactoring and code alteration in order to be fully integrated with the new architecture. Specific attention has been given while refactoring to support more than one map in the same application. See below for full details:
https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/commit/7921307df77be1214ddf7f368fcb9673119ed083




#### 15th

Map area selection
Listening for map event on the map and add/remove selection in memory as needed



#### 15th -18th

Synchronization between map and table start
This allows the 2 components to synchronize their selection, used within both study area  and comparison area definition.


#### 18th -20th
Map selection to table can now be used to transfer sync selection

#### 20th -21st
Table and map are now fully synchronized, a button which allow to select all rows in the table has also been implemented along with one which allows to clear selection on both map and table.

#### 23rd
Started working on replicating what's done so far for the study area on the Comparison area dialog

#### 23rd -25th
Table integration in Comparison Area

#### 25th -26th
Refactor how maps retrieve their initial geographical extent (without which leafletjs would not be able to render a map) and fully integrated the new comparison area's dialog.


#### 27th
Covariates has been refactor so they take into account the study area's denominator and numerator.
Validation on submit study has also been revised and refactored.

#### 28th
Added the event handler for Health theme change which retrieves the correspondingly available num/denom pairs.
Minor age group selection bug fix.

#### 29th
Submit study method added, this executes a post request to the middleware. It uses javascript formdata to upload asynchronously a blob file which is a strigified version of the application mapped model.
Documentation and todo's list started: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/tree/master/rifWebPlatform/web/Documentation  


