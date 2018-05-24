package org.sahsu.rif.services.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 */

/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

//TOUR_WEB_SERVICES-4
/*
 * So far in our tour, the client web application has invoked the
 * web service method "getGeoLevelAreaValues", using a URL such as:
 * 
 * http://localhost:8080/rifServices/studySubmission/getGeoLevelAreaValues?
 * userID=kgarwood&geographyName=SAHSULAND&geoLevelSelectName=LEVEL2
 * 
 * The web server has used "rifServices" to find the relevant web service application
 * in the Tomcat folder (eg: C:/Program Files/Apache Software Foundation/Tomcat8.0/webapps)
 * and "studySubmission" to get hold of the RIFStudySubmissionWebServiceApplication.  
 * It is using "getGeoLevelAreaValues" to invoke this web service method you saw in
 * AbstractRIFWebServiceResource: 
 * 
 * protected Response getGeoLevelAreaValues(
 *    final HttpServletRequest servletRequest,
 *    final String userID,
 *    final String geographyName,
 *    final String geoLevelSelectName) {...}
 * 
 * In that method, we turned URL parameters into RIF business objects that we can
 * use to call the Java-based study submission service.  The service has invoked
 * the corresponding "getGeoLevelAreaValues" method described in 
 * rifServices.concepts.RIFStudySubmissionAPI and returned results.
 * 
 * At this point in the code, the RIF business objects need to be converted into 
 * a JSON response that is suitable to be rendered within the web clients. We use
 * a software project called Jackson to serialise Java objects into a String value
 * that contains valid JSON.
 * 
 * Jackson appears to rely on Java reflection in order to serialise data container objects
 * into a JSON format.  It relies on Java annotations such as "XmlRootElement" and
 * "XMLAccessorType", and its output may vary depending on whether a field is returning
 * a single value or an array.
 * 
 * Jackson can generate a different JSON result depending on how the annotations are used, 
 * and whether it is rendering a single String or an array of Strings.  
 * 
 * In our tutorial example, the "getGeoLevelAreas" service method is returning a collection of 
 * GeoLevelSelect objects to AbstractRIFWebServiceResource.  Each object has one main field "name"
 * that contains the value the client is interested in.
 * 
 * We could try to render JSON
 * for:
 * 
 * [{"name":"Abellan"}, {"name":"Andersson"}, {"name":"Beale"}]
 * 
 * but you may notice that we are repeating "name" a lot.  If a web service method returns thousands
 * of results, the duplicate field name may occupy considerable space.  As well, it may be easier
 * for a JavaScript client to process an array of Strings rather than array of serialised JSON
 * objects.
 * 
 * For the purposes of rendering lists of geo level areas in a drop-down list, we would want a 
 * class variable String[] names, and a method "String[] getNames()".  
 * 
 * [{"names":["Abellan","Andersson","Beale"]}]
 * 
 * 
 * However, it does not seem appropriate to adapt the business classes just so that they can make 
 * it easy to render UI components within a JavaScript client.  To the greatest extent possible, 
 * business concept classes are meant to be neutral with respect to how they are stored and how 
 * they are rendered.
 * 
 * If the annotations and String array methods cannot go in the business classes, then we need
 * to have parallel proxy classes like GeoLevelAreaProxy, whose sole purpose is to cater for
 * JSON serialisation for a JavaScript front-end client.  By having proxy classes, we insulate
 * the rest of the RIF code base from the needs of web services.
 * 
 * The proxy classes are greatly simplified versions of the RIF business classes that can be 
 * returned as a result.  Only the fields which are of interest to the JavaScript client are
 * considered and all of them are String fields.  The classes contain no validation logic and
 * are nothing more than temporary data containers.
 * 		
 */



@XmlRootElement(name="geoLevelAreas")
@XmlAccessorType(XmlAccessType.FIELD)
final public class GeoLevelAreasProxy {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
 	@XmlElement(required = true)	
	private String[] names;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GeoLevelAreasProxy() {
		names = new String[0];
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public String[] getNames() {
		return names;
	}
	
	public void setNames(final String[] names) {
		this.names = names;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
