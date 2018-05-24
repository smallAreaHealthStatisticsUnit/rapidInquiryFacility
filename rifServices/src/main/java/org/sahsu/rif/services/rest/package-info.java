/**
 * 
 * <p>
 * This package contains the classes that are needed to advertise methods of the
 * web services to clients.  Our aim is to develop a JavaScript-based web application client 
 * which will support the same features as the Swing desktop application described by classes in
 * the related RIF Job Submission Tool project.  Electronic forms, written in JavaScript,
 * will be populated based on data provided by a number of web service methods.
 * </p>
 * 
 * <p>
 * There are three kinds of classes in the package:
 * <ul>
 * <li>
 * classes used to advertise the web service 
 * ({@link rifServices.restfulWebServices.RIFRestfulWebServiceApplication} and
 * {@link rifServices.restfulWebservices.RIFRestfulWebServiceResource})
 * </li>
 * <li>
 * Proxy business classes that are designed to be serialised by web services.  Names for these
 * classes end in "Proxy".  example: {@link rifServices.restfulWebServices.HealthCodeProxy}
 * </li>
 * <li>
 * Proxy-to-Native converters
 * </li>
 * </ul>
 * 
 * <p>
 * REST web services have been developed to best suit the needs of front end clients which will
 * be written in JavaScript.  We anticipate that REST web services that generate JSON will suit
 * web browser applications and that SOAP web services that generate XML will suit versions of
 * the query submission tool which support batch processing.  
 * </p>
 * 
 * <p>
 * The web service technologies being used are Jersey and Jackson.  Jersey implements a web service
 * specification, whereas Jackson caters for generating JSON code.  The web service receives 
 * requests as URL, and transform the parameter values into instances of Java objects that can 
 * be passed to the RIFJobSubmissionAPI.  When the service implementing the API executes, the 
 * results it returns are serialised into JSON.
 * </p>
 * 
 * <p>
 * Jersey has mechanisms for serialising data container objects, but the associated classes need
 * to include annotations and appear to require the class to conform to certain properties.  Initially,
 * we began to modify domain classes in the <code>rifServices.concepts</b> package
 * by putting in annotations that would allow them to be serialised by Jersey.  However, we
 * had some concerns about making the code for the business classes dependent on technologies 
 * that are specific for web services.  
 * </p>
 * <p>
 * We decided to develop a sub-package within the business concept layer which would
 * contain proxy classes.  These are versions of business classes that are designed
 * to cater for being serialised and deserialised through web services.  This decision allows
 * the original domain classes developed to support the Java API to remain plain old java objects 
 * (pojo), and not be influenced by dependencies on web services.  The cost of the decision is
 * the maintenance overhead of the proxy classes.
 * </p>
 * <p>
 * The converter classes may be deprecated.  They were initially developed to take advantage of
 * JAXB's XMLAdapter class.  The idea was that when native objects would be returned to the 
 * client using the web service, JAXB would use the adapter class to determine what converter
 * class would transform the native object into an object suited for serialisation.
 * </p>
 * <p>
 * Now, all of the conversion happens within the body of the methods developed to support
 * web service calls. The main reasons the converter classes have not been used is that we
 * would need to reference them in the RIF's business classes through annotations.  For example,
 * we might have to reference some HealthCodeCoverter class through an annotation in HealthCode,
 * to let JAXB know which converter to invoke whenever it encounters a HealthCode instance.
 * </p>
 *  
 * <p>
 * We viewed the need to specify an explicit link to the converter in the business class as too
 * much of an intrusion on the domain classes, whose design should not be influenced by 
 * particular technologies.
 * </p>
 * 
 * <p>
 * We have begun to develop different types of Proxy classes; some cater to serialising a single
 * Java object, whereas others are used to more efficiently serialise lists of objects which are of
 * the same type.  For example, we initially supported JSON serialisation of 
 * {@link org.sahsu.rif.services.concepts.Geography} objects using a class called
 * <code>GeographyProxy</code>.  This class was marked up with XML annotations that allowed it
 * to be serialised in a way that resembles: {"geography":["UK"]}.
 * However, if we had a list of geographies, we would create a lot of repetition by mentioning
 * "geography" in each list item.  Therefore we developed {@link rifServices.rifWebServices.GeographiesProxy},
 * which produces output such as: [{"names":["EW01", "SAHSU", "UK91"]}].
 * </p>
 *  
 * <p>
 * As we have evolved the web services, we have become more aware of different ways we could serialise
 * business objects as JSON fragments.  In some cases it may be appropriate to rely on a proxy class
 * that is designed to map the fields of a single object.  Proxy classes that process groups of the same
 * kind of object may provide a better way to generate JSON.  However, this works best if each a single
 * dimension array is returned.  However, we have developed some other specialised classes to render
 * {@link rifServices.businessConceptLayer.AgeGroup} and {@link org.sahsu.rif.services.concepts.MapArea},
 * which each have multiple field attributes.  For example, an <code>AgeGroup</code> has a name, a lower age limit
 * and an upper age limit.  If a list contains multiple <code>AgeGroup</code> objects, then we will need
 * a way to efficiently produce JSON which includes values for each of these fields.  In the case of 
 * <code>AgeGroup</code>, we have developed AgeGroupJSONGenerator, which is designed to return the list
 * as three parallel arrays, one for each attribute.  The JSON generation code ensures that the ith
 * position in one array refers to the same age group as the ith position in another (ie: names[5]
 * refers to the same object as lowerLimit[5].
 * </p>
 * 
 * <h2>Coding Conventions</h2>
 * <table>
 * <tr valign="top">
 * <td>
 * <b>Convention</b>
 * </td>
 * <td>
 * <b>Meaning</b>
 * </td>
 * <td>
 * <b>Example</b>
 * </td>
 * </tr>
 * 
 * 
 * <tr valign="top">
 * <td>
 * <code>[singular business concept]Proxy</code>
 * </td>
 * <td>
 * designed to generate JSON for each attribute in a single instance of business classes found in 
 * the {@link rifServices.businessConceptLayer} package.  These classes are good to use in cases
 * when one object is viewed in detail.
 * </td>
 * <td>
 * <code>HealthCodeProxy</code>
 * </td>
 * </tr>
 * 
 * 
 * <tr valign="top">
 * <td>
 * <code>[plural business concept]Proxy</code>
 * </td>
 * <td>
 * designed to generate JSON for each attribute in a collection of business objects of the same kind.
 * These classes are good for efficiently rendering items in lists.
 * </td>
 * <td>
 * <code>GeoLevelSelectsProxy</code>
 * </td>
 * </tr>
 * 
 * 
 * <tr valign="top">
 * <td>
 * <code>[plural business concept]JSONGenerator</code>
 * </td>
 * <td>
 * efficently renders collections of business objects which have multiple attributes.
 * </td>
 * <td>
 * <code>MapAreaJSONGenerator</code>
 * </td>
 * </tr>
 * 
 * 
 * </table>
 * @author kgarwood
 *
 */
package org.sahsu.rif.services.rest;


