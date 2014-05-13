/**
 * 
 * <p>
 * This package contains the classes that are needed to advertise methods of the
 * {@link rifServices.businessConceptLayer.RIFJobSubmissionAPI} as a web service to
 * clients.  Our aim is to develop a JavaScript-based web application client which will
 * support the same features as the Swing desktop application described by classes in
 * the related RIF Job Submission Tool project.  Electronic forms, written in JavaScript,
 * will be populated based on data provided by a number of web service methods.
 * </p>
 * 
 * <p>
 * There are three kinds of classes in the package:
 * <ul>
 * <li>
 * classes used to advertise the web service 
 * (@link rifServices.restfulWebServices.RIFRestfulWebServiceApplication and
 * @link rifServices.restfulWebservices.RIFRestfulWebServiceResource)
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
 *  <p>
 *  Jersey has mechanisms for serialising data container objects, but the associated classes need
 *  to include annotations and appear to require the class to conform to certain properties.  Initially,
 *  we began to modify domain classes in the <code>rifServices.businessConceptLayer</b> package
 *  by putting in annotations that would allow them to be serialised by Jersey.  However, we
 *  had some concerns about making the code for the business classes depend on technologies 
 *  that are specific for web services.  
 *  </p>
 *  <p>
 *  Eventually, we decided to develop a sub-package within the business concept layer which would
 *  contain proxy classes, which are versions of specific business classes that are designed
 *  to cater for being serialised and deserialised through web services.  This decision allows
 *  the original domain classes developed to suppor the Java API to remain plain old java objects 
 *  (pojo), and not be influenced by dependencies on web services.  The cost of the decision is
 *  a maintenance overhead of the proxy classes.
 *  </p>
 *  <p>
 *  The converter classes may be deprecated.  They were initially developed to take advantage of
 *  JAXB's XMLAdapter class.  The idea was that when native objects would be returned to the 
 *  client using the web service, JAXB would use the adapter class to determine what converter
 *  class would transform the native object into an object suited for serialisation.
 *  </p>
 *  <p>
 *  Now, all of the conversion happens within the body of the methods developed to support
 *  web service calls. The main reasons the converter classes have not been used is that we
 *  would need to reference them in the RIF's business classes through annotations.  For example,
 *  we might have to reference some HealthCodeCoverter class through an annotation in HealthCode,
 *  to let JAXB know which converter to invoke whenever it encounters a HealthCode instance.
 *  We viewed the need to specify an explicit link to the converter in the business class as too
 *  much of an intrusion on the domain classes, whose design should not be influenced by 
 *  particular technologies.
 *  </p>
 * 
 * 
 * 
 * 
 * @author kgarwood
 *
 */
package rifServices.restfulWebServices;


