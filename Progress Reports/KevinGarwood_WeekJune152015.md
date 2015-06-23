##Week of June 15, 2015
I spent most of the week developing business classes and XML serialisation classes that support configuration options 
in the Data Loader Tool.  However, three issues have arisen:
•	determining what steps should be supported in the process of transforming data from a CSV file into a published data set
•	the influence  that command-line and GUI-based versions of the data loader tool have on the code
•	the design tension between data-centric and process-centric views of the data loader activities
•	the skill levels a RIF manager should have

The outcome of this work is a design discussion that I am trying to write up as part of the formal design documentation 
for the tool.

The first issue is defining steps in the workflow that will transform an imported data set into a data set that can 
be used as part of the RIF database.  
