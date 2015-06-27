# Nan Lin Progress Report RIF4.0 
Principal Work Area: **Java Middleware** 
### 2015 
#### June
RIF development team decides not to distribute the whole set of ICD 10 code in case violating the WHO copyright accidentally. Instead, only a sample set of ICD 10 code, which is used in the table “sahsuland_cancer” (around one percent of the whole), will be provided in order to demonstrate RIF features. The format of such a sample file (the target) should be XML as its details can be easily understood and parsed by third parties. 



A version of ICD 10 file from a WHO website is downloaded; this peculiar XML file uses Classification Markup Language (ClaML) to describe the data. This file is used as the template to generate the target. Consequently, DOM is used to extract the information that RIF is interested in the template and then generate the target also defined by ClaML.



The target keeps the skeleton of the template and ignores the details that are not relevant with RIF. For example, the target has one “chapter”, a few “blocks” with “categories” related to the chapter; details about the “inclusion” and “exclusion” sections of the ICD 10 code description are excluded.  As a result, the prototype code developed last month is revised: the new version not only can parse the template but also generate the target. Actually, the code can parse any XML files defined by ClaML.



In the target, the dot sign is removed from the WHO ICD 10 code in accordance with the RIF data format. The size of the target is small.



The naked eye test shows the target only contains the ICD 10 code details used in RIF. 

##### To do:
- Refactor the prototype code.
- Unit testing of the prototype code using KG’s existing test cases with possible new ones. 
- Fit the feature into RIF properly. Export the feature as a web service. Then the Javascript snippet in the front-end web page can call the service directly; the web page can render the RIF ICD 10 code taxonomy structure successfully.
- Use the similar way to generate a file containing the ICD 9 code used in RIF.

#### May
- Continue reading RIF 3.2 manual to preciously understand the purpose, features, concepts, and business logic of RIF.  
- Learn the database schema to understand RIF terms, how the data is organized, what the functions of database procedures are and how they should be interfaced with the middleware. 
- Understand the system architecture of the Java middleware, libraries required and why they are introduced. In particular, read most of the source code in rifServices package, understand how the system is initialized, the workflow to deal with an HTTP request, and the interaction with the Postgres database. 
- Write the prototype code of a Java thread to check the state of a study processed by the Postgres database. Since each database vendor has its own event notification mechanism, the code cannot use a generic way to fulfil the ‘real-time’ task. On the contrary, the thread periodically executes a select query asking the database what the state of the designated study is. The value of the period can be adjusted in order to improve the system performance. 
- Write the prototype code to build up an ICD 10 code repository.  If a user provides an ICD 10 code, the repository can give the corresponding description with its children and parent code (if available). The detail of the description does not include “Incl.” and “Excl.” parts coming with the code description assuming the web page has limited space to display such information. 

##### To do:
Discuss with PH and KG how to fit the prototype code into RIF. Know other development tasks in RIF middleware.

### April
- Fix a bug about geographic resolution by modifying methods getGeoLevelSelectValues and getGeoLevelViewValues in SQLRIFContextManager class of rifServics.dataStoragelayer package. The revised version applies “less than” or “equal” to replace “less than” condition for the first method; “greater than” or “equal” to replace “greater than” condition for the second method. 
    
    ```Java
    public ArrayList<GeoLevelSelect> getGeoLevelSelectValues(...){
         ...
         getGeoLevelSelectValuesQueryFormatter.addWhereParameterWithOperator("geolevel_id", "<=");
         ...
    }
    
    public ArrayList<GeoLevelView> getGeoLevelViewValues(...){
        ...
        geoLevelViewsQueryFormatter.addWhereParameterWithOperator("geolevel_id",">=");
        ...
    }
    ```
