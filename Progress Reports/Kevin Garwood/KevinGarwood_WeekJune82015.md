##Week of June 8
###Summary
	* finished a method that allows a study and its results to be written to ZIP files
	* resumed work on features in the Data Loader Tool that would allow the processing of new data sets to be specified as 
	an XML file

###Finished method used to write study results to zip files
I finished method that uses the study ID to generate a ZIP file containing the original query, the extract files and 
information governance documents.  In future we will need to determine where zip files are generated.  At the moment it 
is coded as a user-configurable property that is read at startup of the main RIF services.  However in a production 
deployment, having the property file available within a web application folder of Tomcat would present a security issue. 

We have to decide how we want users to retrieve any zip files that are created.  In one scenario, when a scientist submits 
a study and there is no governance tool to delay the production of results, then a zip file may be streamed back to the 
browser client.  The user would then download the zip file to somewhere local.  The problem with this approach is that 
   1. it doesn’t work well when information governance does delay the production of results and 
   2. it doesn’t work well if running the study would take a long time.  
   
I personally favour having the user become used to a batch submission approach.  In this scenario, a scientists submit a 
study but do not expect immediate results back.  When the study is eventually processed and a ZIP file of the results is 
created, the RIF will then e-mail the scientists.  The e-mail message will include a link to access a secure FTP server 
which is available on another host that is not the same as the one running the web application.  Scientists would then 
log into the secure FTP site and retrieve an encrypted zip file.  ZIP files would be available in tightly controlled 
directory space that deleted any files after a period of a few days.

However for now, the zip files will be generated in a directory that is on the same machine as the browser client.  
Eventually we will change this setup.
Resuming work on Data Loader Tool

In response to changes in staff, I had switched emphasis of development from working on the Data Loader Tool to 
finishing more features to support the web-based Study Submission Tool.  I’ve now resumed working on the Data Loader 
Tool and have been working to address some challenges.  The first challenge is trying to support two ways of loading 
data: relying on a GUI application and editing an XML file.  Both must support the same basic workflow of steps that 
promote newly loaded data to become new parts of the RIF schema.  However, it is unclear whether these two methods 
should support exactly the same feature set and whether they should assume different levels of technical skill.

I feel that non-technical users will only use a GUI application that uses guided data entry features to strictly 
control the way data are promoted through different stages.  However, RIF managers who are more technically skilled 
will likely want to use a command-line tool that processes a data set based on an XML-based work flow description.  
A key part of designing either tool is to assess the technical skill of RIF managers, which is a topic that warrants 
further discussion.

For now I’m giving priority to having the RIF support an XML-based workflow engine that will load CSV files into 
areas of the RIF schema such as numerator health data.
