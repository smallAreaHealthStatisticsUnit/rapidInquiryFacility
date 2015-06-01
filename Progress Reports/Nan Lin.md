# Nan Lin Progress Report RIF4.0 
## Principal Work Area: Middleware 
### 2015 
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