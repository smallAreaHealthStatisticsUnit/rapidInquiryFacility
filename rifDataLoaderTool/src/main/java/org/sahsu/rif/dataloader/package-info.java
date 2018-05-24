/**
 * <h1>Overview of Code Organisation</h1>
 * Code for the Data Loader Tool is broadly organised into layers of the same 
 * three-tier architecture as much of the wider RIF code base.  These are:
 * <ul>
 * <li>
 * <b>presentation layer</b>: contains all of the code used to manage the 
 * electronic forms.
 * </li>
 * <li>
 * <b>business concept layer</b>: contains definitions of business concepts 
 * supported in the application. It also contains interface definitions of 
 * APIs.
 * </li>
 * <li>
 * <b>data storage layer</b>: contains code used to extract attributes of 
 * business objects in order to assemble SQL queries and apply them to an underlying
 * database via JDBC.
 * </li>
 * </ul>
 * 
 * <p>
 * Development of the Data Loader Tool envisioned that in the short term, it would 
 * have a Java Swing-based desktop GUI.  In the long term, the GUI would be replaced
 * by web-based electronic forms.  
 * 
 * <p>
 * Swing, an ageing part of the Java SDK distribution, was initially used because 
 * it would support rapid prototyping of forms that could be made using libraries that 
 * would already be part of the existing development tool chain.  Through rapid 
 * prototyping, we would learn more about what features the data loader should have, and
 * what kinds of API method signatures would be most appropriate to support them.
 * </p>
 * 
 * <p>
 * Later development would substitute the Swing-based front-end with a JavaScript-based
 * front end that used the same web service technologies (ie: Jackson, Jersey) that are
 * used in other parts of the tool suite.  Once the feature set had stabilised, we would
 * be able to use JavaScript, web services and Java-based middleware in concert without
 * having to cope with the additional volatility of an immature feature set.
 * </p>
 * 
 * <p>
 * The architecture 'radiates' from the business concept layer.  If you add a new
 * field to one of the business concept class, it will likely result in the following
 * changes that ripple through the architecture:
 * </p>
 * 
 * <ul>
 * <li>
 * modifications to the classes that serialise and deserialise the class 
 * (<code>rifDataLoaderTool.fileformats</code>)
 * </li>
 * <li>
 * modifications to the GUI to either show or support editing of the concept
 * (<code>rifDataLoaderTool.presenationLayer</code>)
 * </li>
 * <li>
 * modifications to database queries whose assembly depends on fields in the 
 * business class (eg: SELECT or INSERT statements)
 * (<code>rifDataLoaderTool.datastorage</code>
 * </li>
 * <li>
 * modifications to <code>src/main/resources/RIFDataLoaderToolMessages.properties</code> 
 * to include the text that would identify a new field in a GUI label or an error
 * </li>
 * </ul>
 * 
 * <ul>
 * <li>
 * it is part of the same core Java libraries that are already used to support the 
 * middleware and would therefore not present additional library dependencies
 * </li>
 * <li>
 * 
 * </li>
 * </ul>
 * 
 * 
 * The front-end of the Data Loader Tool
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */
/**
 * @author kgarwood
 *
 */
package org.sahsu.rif.dataloader;
