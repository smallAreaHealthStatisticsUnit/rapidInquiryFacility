# Nan Lin Progress Report RIF4.0 
Principal Work Area: **Java Middleware** 
### 2015 
#### September to October
Download RIF 4.0 from Github on a laptop, try to build the database (Postgres version) by following Peter’s online documents.

The long process encounters errors, the bugs have been reported to Peter. Accordingly, Peter modifies the code and text in the documentation. Finally, the database is installed successfully. (However, make provided by MinGW sometimes cannot work when Windows installs the latest updates.)


After this, try to build RIF middleware. Modify the pom.xml under rifServices directory: in the dependency section about javax.servlet, add scope attribute with the value ‘provided’. The package has been deployed on to a Tomcat 8 server, the start page and the user login procedure work fine.

####August
Wrap ICD10ClaMLTaxonomyProvider class developed last month into Jersey, exposing the work-alone service as a RESTful web service. 


The methodology follows the MVC convention: ICD10ClaMLTaxonomyProvider works as the controller, borrowed class HealthCode, TaxonomyTerm (from RIF) as the model, borrowed class HealthCodeProxy and WebServiceResponseGenerator (from RIF) as the view. 


The project is managed by maven, which uses the latest version of Jersey and Jackson. The package has been deployed on a Tomcat 8 container, it returns expected results when URLs have different combinations of parameters. 


#### July
Convert the prototype code developed last month into ICD10ClaMLTaxonomyProvider class in rifServices.taxonomyservices package, which implements RIF HealthCodeProviderInterface. This class provides an interpretation of ICD 10 code service and then save results into an internal HashMap container. As a result:
- The tree structure of the ICD 10 code is exposed: the class can return the parent and immediate children of a health code.
- Use the label of health code as the key, the HashMap container can quickly return the corresponding taxonomy term when users provide an ICD 10 code label.
- It provides a utility method that can transform a taxonomy term into the corresponding health code.
- Any services that are interested in ICD 10 code can access the HashMap container.

A well formatted example ClaML file containing the sample ICD 10 code from sahsuland_cancer becomes part of RIF resource files.


Unit tests of the class. Only focus on five important methods: getTopLevelCodes(), getHealthCodes(), getHealthCode(), getParentHealthCode(), and getImmediateSubterms(). Test cases are made up of the following:
- Normal situations: an ICD 10 code in the example file, whose position may be top, middle, or bottom.
- Abnormal situations: an ICD 10 code excluded in the example file; not an ICD 10 code; special term “null”.

Add the necessary content in the constructor section of HealthOutcomeManager class in order to add ICD10ClaMLTaxonomyProvider into the healthCodeProviders list.

##### To do:
Wrap ICD10ClaMLTaxonomyProvider into Jersey, expose the service as a RESTful web service.





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
- Develop complete ICD 9+10 lists for inserted into the RIF build outside of Github. This is to preserve WHO's copyright and licensing conditions.

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
