/**
 * 
 */
/**
 * 
 * <h1>
 * </h1>
 * 
 * <h1>Conceptual Overlap with RIF Data Loader Tool and RIF Services Packages</h1>
 * 
 * This package contains classes for all the business concepts that are involved 
 * with loading data into the RIF production database (see subproject 
 * <code>rifDatabase</code>.  It is important to note that:
 * <ul>
 * <li>
 * some of the class names here also appear in the 
 * <code>concepts</code> of the <code>rifServices</code> sub-project
 * </li>
 * <li>
 * some of the classes are a response to priorities which may not be important
 * in future releases of the RIF
 * </li>
 * </ul>
 * 
 * <h2>Same Name Different Context</h2>
 * <p>
 * There are some business concepts that are implemented in both the 
 * <code>rifDataLoaderTool</code> and <code>rifServices</code> sub-projects.
 * For example,consider {@link rifDataloaderTool.businessConceptLayer.Geography}
 * and {@link rifServices.businessConceptLayer.Geography}.  Both of these
 * represent the same concept but appeal differently to the 'audience' of a 
 * scientist end user and the RIF production database.  
 * {@link rifServices.businessConceptLayer.Geography} is intended as a drop
 * down list selection item that will influence what other choices appear in other
 * lists. {@link rifDataLoaderTool.businessConceptLayer.Geography} also has
 * a list of {@link rifDataLoaderTool.businessConceptLayer.GeographicalResolutionLevel}
 * items.  The design is meant to cater to the task of specifying a geography
 * in the Data Loader Tool using auto-generated scripts.
 * </p>
 * 
 * <h2>Analagous Classes</h2>
 * <p>
 * In other cases, some business concepts have analogous classes that may have
 * different names.  For example, in the Data Loader Tool, we use 
 * {@link rifDataLoadertool.businessConceptLayer.GeographicalResolutionLevel},
 * whereas in the rifServices package that services the scientist web application,
 * we have classes that represent kinds of resolutions:
 * <ul>
 * <li>
 * {@link rifServices.businessConceptLayer.GeoLevelArea}
 * </li>
 * <li>
 * {@link rifServices.businessConceptLayer.GeoLevelSelect}
 * </li>
 * <li>
 * {@link rifServices.businessConceptLayer.GeoLevelToMap}
 * </li>
 * <li>
 * {@link rifServices.businessConceptLayer.GeoLevelView}
 * </li>
 * </ul>
 * 
 * <p>
 * In the Data Loader Tool, "region", "district", "ward" and "output area" are 
 * geographical resolutions which have an order of increasing details.  Beyond that,
 * the data loading activity does not care about how users select or find those
 * levels in a user interface.  In the scientist web application, 
 * <code>GeoLevelArea</code>, <code>GeoLevelSelect</code>, 
 * <code>GeoLevelToMap</code> all <code>GeoLevelView</code> all refer to 
 * how resolutions are used to support features in the web application.  Therefore,
 * although in one sense they both mean the same thing, they are used in different
 * contexts.
 * </p>
 * 
 * <p>
 * More generally, there are similar concepts that have evolved to support the
 * data loader tool and the scientist study submission application.  In future,
 * these concepts may be merged but they appear as they do now because they 
 * cater to different design needs and I thought it was safer to support overlapping
 * concepts than to invest in abstractions that would have to service both 
 * sub-projects.  
 * </p>
 * 
 * @author kgarwood
 *
 */
package org.sahsu.rif.dataloader.concepts;
