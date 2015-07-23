#Kevin Garwood Progress Report
##Week of July 20

###General

As a general comment, I have been doing en masse change commits with very little useful information in the commits.  Much of the core logic for the data loader tool appears to becoming more stable and less volatile with respects to drastic code changes.  However, a lot of code is being created for rapid prototyping other features we'd like to be able to demonstrate in August.  For now, many of the commits are being made simply to ensure that the most current changes that correctly build are committed to GitHub.

Much of the code will require significant testing efforts.  My thoughts are that until the end of August, the most important tasks are to produce features for a rapid prototype in order to take advantage of our limited face-to-face opportunities for eliciting feedback both from the CDC and other potential users.  Afterwards, the main design documents will be updated with a more meaningful discussion on development issues.  Then check-ins for the existing data loader code should become more carefully described.
 

###Recurring Design Issues
Deferring but also anticipating future support for Split and Combine activities for data sets
how best to incorporate steps for "split" and "combine" data sets into a work flow that normally assumes that a single data set will be transformed from an imported CSV file to a published part of the RIF schema


####Automatic generation of query fragments that validate or clean table fields
It is now clear that there will be a need to have four ways to construct SQL code that validates or cleans a table field:
1. No activity
2. Use one or more regular expressions in a CASE statement (eg: simple cleaning of 'M', 'm', 'male', ... where search-and-replace conditions give '1' or '0' for a cleaned result)
3. Call a function (eg: cleaning or validating UK postal codes)
4. Use an SQL fragment.  eg calling the ntile function to convert a column of score values into quantilised values that range from one to 5.

It is also clear we need support for auditing changes, as well as the ability to limit the amount of information that ends up in the audit logs.  RIF managers may want to record a lot of information about changes if they anticipate that only a few would ever happen.  They may also want more of a summary than a detailed description of changes if they expect a lot of changes.  In some cases, they may want no auditing for a field.  This level would be useful to help ensure that senstive data do not appear in change logs and to make data for a large number of changes more meaningful.

####Determining how best to support auditing the creation of auto-generated queries
Should they be written to one big file or a few little files? Is it better to use Java's stream or reader/writer classes?


###Main Activities

The activities below represent an incomplete tick list of tasks I've had to deal in the last week.

* Rewriting GUI code for generating data entry forms for a prototypical data loader tool.  


* Rewriting classes that generate code for creating search-and-replace, validation and casting tables for the CLEAN step.  

* Centralising code for auditing changes by creating a new ChangeAuditManager class.

* Adding exception support to ensure that meaningful errors are thrown if log files for each imported data set are not created correctly.

* Added a few basic SQL functions which could be used to validate or clean table columns. In some cases, 'cleaning' isn't just a call to a specific RIF function.  In the case of the quantilised data type, we'd use a piece of postgres code that would likely have to be rewritten in MS Server.  


* Added a convenience method for creating a DateRIFDataType that uses different data formats (eg: dd/MM/yyyy, MM/dd/yyyy).  Trying to figure out the best way to construct the query fragment that contains a call to an SQL validation function.  


* Added helper methods to DataSetConfiguration to gather all fields which will be used in auditing (ie have a field auditing level of 'field level only' or 'field level description'. 

* Corrected bugs to ensure that a work flow reaches a completed state.

* trying to add support for logging an audit of SQL queries to a file.  I currently think that that each loaded file would warrant an audit file ".log" which would only show queries that were executed to process a single data set.  The log may become useful in guiding porting efforts from PostgreSQL to MS Server.

* Adding support for quintilise type - it will take as input a numeric column and transform it into an integer value from 1 to 5 inclusively.

* Adding support for specifying field-level control of auditing.  RIF managers can specify one of three levels for auditing a column: 
NONE - may be useful when rif manager wants to avoid trapping sensitive data in logs.  May also want to avoid logging changes when every field would be expected to change. Two examples of this would include removing periods from every ICD input value and quantilising numeric columns.  In both examples, almost all field values would change so there may not be a practical need to record a large quantity of changes.
FIELD LEVEL ONLY - Changes will be limited to something like: "data set A row number B, format Field X was changed"  This setting helps reduce the amount of sensitive data that appears in the change logs but is detailed enough to do statistics on the number of fields changed.
FIELD LEVEL CHANGE DESCRIPTION - changes such as "data set A row number B, field value X was changed from Y to Z."

* Added two states "START" and "STOP" to work flow states so now main work flow is: START, LOAD, CLEAN, CONVERT, OPTIMISE, CHECK, PUBLISH, STOP

* START and STOP merely help to make it easier to run a work flow.  At the end of the operation for each step, a table of data set configurations is updated with that state.  For example, after a CSV file has been processed by LOAD, LOAD will appear as the last updated step.

* Added meaningful messages to the WorkFlowValidator routines which check whether the linear work flow contains data sets which are in either a SPLIT or COMBINE step.  We will manage work flows made of SPLIT and combine steps in a separate workflow "BranchedWorkflow", which will be examined later on.  For now, we should ensure that none of the data sets in a LinearWorkflow start in either of these two states.

* Added in steps to update the last completed work flow step in the table which registers imported data sets.

* Adding in cleanup code to ensure that in the clean step, we only retain one table with the changed results.  We will delete tables for search-and-replace, validation and casting.  These three temporary tables are retained only long enough to do proper audits such as how many field values needed to be changed or how many cleaned field values still failed validation.

* Modified query formatters for creating tables, added a new class for generating SQL update queries
