package org.sahsu.rif.services.rest;

import java.io.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import java.util.Base64;
import java.util.Base64.Decoder;

import org.sahsu.rif.generic.util.RIFLogger;

//TOUR_WEB_SERVICES-7
/*
 * We finish the web services tour by looking at the issues that the
 * WebServiceResponseGenerator has to consider when it decides whether
 * a Response should use "application/json" or "text/plain".  Please read the
 * main class comments below.  It's not easy, and represents a future area of development! 
 */


/**
 * This class was developed to help configure the content type that can be 
 * returned in the response for a web service method.  The class was developed
 * in response to a problem we observed in trying to support JSON in Internet 
 * Explorer.
 * 
 * <p>
 * Most modern browsers should support a JSON content type, but Internet Explorer
 * appears to have some kind of bug in its support.  Whereas browsers such as 
 * Chrome will automatically display the JSON on the web page, IE will instead prompt users
 * to download a *.json file containing the result instead.
 * </p>
 * 
 * <p>
 * One way of solving this problem is to try and alter the registry file on the client machine
 * so that IE would display the JSON responses.  However, this is not a practical solution.  
 * The alternative solutions all rely on answering the question: 
 * <p>
 * <i>"Should our response type be 'application/json' or 'text/plain'?</i>
 * 
 * <p>
 * We would rely on one of three questions to determine which to return:
 * <ul>
 * <li>Is the browser Internet Explorer?</li>
 * <li>Does the browser support the feature of displaying a JSON response?</li>
 * <li>Does the browser support the application/json MIME type?</li>
 * </ul>
 *
 * Our solution involves writing code to answer one of these three questions.  None of them
 * are great, but we picked one for the short term.  This class provides a way of encapsulating
 * inspection of client browser properties so that it may easily be changed for all web service
 * responses in future.  The approaches we looked at are described below and we chose to use
 * browser sniffing for now.
 * </p>
 * 
 * <h2>Browser Sniffing</h2>
 * Browser sniffing involves having the web service mining properties found in the 
 * <code>HttpServletRequest</code> object to determine the type and version of the browser.  
 * The relevant property here is "user-agent", whose value can be blank, or can be some 
 * vendor-specific free text String value.  In browser sniffing, we would examine phrases 
 * in this parameter value in order to determine the browser.
 * 
 * <p>
 * For example, for Internet Explorer v.11, the default value for the "user-agent" property is:
 * <code>Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko</code> whereas the same
 * property value in Internet Explorer v.10 is <code>Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0)
 * </code>.  For Google Chrome, its "user-agent" will look very different.
 * </p>
 * 
 * <p>
 * Although browser sniffing is often used in web applications, it produces brittle code whose
 * assumptions about phrases being present in the user-agent property may not hold in future
 * releases of any given browser. The proliferation of bespoke coding solutions to identify
 * browser types has stimulated community projects such as WURFL, which effectively act as a
 * kind of lookup service for these strings.  Using this kind of project would be good if 
 * the RIF were a product destined for many different types of mobile devices.  However, it
 * will likely be supported mainly through Chrome and Internet Explorer browsers instead.
 * </p>
 * 
 * <p>
 * We have not yet evaluated the cost-benefits of using WURFL libraries.  One one hand,
 * using the project would give us a robust way of asking: "Is the browser IE?" so that we could
 * know to return plain/text rather than application/json responses.  However, WURFL relies on
 * an evolving repository of device descriptions.  We would have to either ensure updated versions
 * of the repository are regularly shipped with installations.  Alternatively we could delegate
 * to a remotely managed service.  However, we would then wonder whether a reliance on other
 * web services might compromise privacy concerns related to another service potentially 
 * monitoring RIF activities.
 * </p>
 * 
 * <p>
 * Support web pages for the Microsoft Internet Explorer community advocate developers using
 * feature detection rather than browser sniffing. 
 * 
 * <h2>Feature Detection</h2>
 * Feature detection attempts to answer: "Does the browser support feature X?" rather than "What type 
 * of browser is the client using?".  Many developers view this approach as being better because
 * it avoids the seemingly arbitrary changes that browser vendors might make in the value of
 * <code>user-agent</code>.
 * 
 * <p>
 * Feature detection seems to be most commonly implemented by the JavaScript on the client rather
 * than by Java on the web service server.  In order to determine whether a browser supports a 
 * feature, the JavaScript code usually tries to access the DOM document associated with 
 * the web page.  The script will try to determine feature support by whether certain DOM nodes 
 * are present.  Support for a feature could presumably help the program alter the way it calls
 * web service methods.
 * </p>
 * 
 * <p>
 * The challenge with this approach is whether you can actually identify a specific feature that
 * would inform your decision about how you would return your web service response.  Being able
 * to view a particular image format might be a feature.  But in this case, "Is the browser
 * able to support JSON directly in the web page?" seems to describe more of a bug in 
 * Internet Explorer than a feature in browsers in general.
 * </p>
 * 
 * <p>
 * Like browser sniffing, feature detection would tend to lead to the proliferation of bespoke
 * coding solutions.  JavaScript libraries such as Modernizr are designed to detect features so
 * that web developers do not have to write their own code.  However, once again we have yet
 * to weigh the cost-benefits of relying on this external software project. 
 * </p>
 * 
 * <h2>Determining MIME Support in HttpServletRequest</h2>
 * Another way to determine which content type to return in the Response type is to check
 * the property string that indicates what MIME types are supported.  If the property 
 * "Accept" in the request header indicates "application/json", then we would return 
 * that content type.  However, using this property comes with the same problems as 
 * user-agent.  In our initial tests, we found that Chrome, which did display the JSON 
 * content type, didn't indicate "application/json" explicitly. Therefore, even if browsers
 * did support directly rendering JSON, their support would not necessarily be described well
 * in the property value of the servlet request.
 * 
 * 
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

final public class WebServiceResponseGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	private static String lineSeparator = System.getProperty("line.separator");
	
	// ==========================================
	// Section Properties
	// ==========================================

	private RIFLogger rifLogger = RIFLogger.getLogger();
	
	// ==========================================
	// Section Construction
	// ==========================================

	public WebServiceResponseGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public Response generateWebServiceResponse(
			final HttpServletRequest servletRequest,
			final String data) {
				
			ResponseBuilder responseBuilder = null;
			try {
				if (clientBrowserIsInternetExplorer(servletRequest)) {
					responseBuilder = Response.ok(
							data, 
							MediaType.TEXT_PLAIN);	
				}
				else {
					responseBuilder = Response.ok(
							data, 
							MediaType.APPLICATION_JSON);	
				}
			}
			catch (Exception exception) {
				
				rifLogger.error(this.getClass(), "generateWebServiceResponse() error",
					exception);
				
				responseBuilder = Response.status(500).entity("generateWebServiceResponse(): Error in response generation: " + 
					exception.getMessage());
				responseBuilder.type("text/plain");
			}
			finally {
				return responseBuilder.build();		
			}
		}

		public Response generateWebServicePngResponse(
			final HttpServletRequest servletRequest,
			final String data) {
			
			ResponseBuilder responseBuilder = null;
			try {
				byte[] bytes = Base64.getDecoder().decode(data);
				responseBuilder = Response.ok(
						bytes, 
						MediaType.WILDCARD_TYPE);
				responseBuilder.header("Cache-Control", "max-age=2592000"); //30days (60sec * 60min * 24hours * 30days)

				responseBuilder.type("image/png");
			}
			catch (Exception exception) {
				
				rifLogger.error(this.getClass(), "generateWebServicePdfResponse() error",
					exception);
				
				responseBuilder = Response.status(500).entity("generateWebServicePdfResponse(): Error in PNG generation: " + 
					exception.getMessage() + lineSeparator +
//					"Stack >>>" + lineSeparator + exception.getStack() + lineSeparator +
					"Data: " + data);
					
				responseBuilder.type("text/plain");
			}
			finally {
				return responseBuilder.build();		
			}				
		}
		
	public Response generateWebServiceResponse( // streaming version
			final HttpServletRequest servletRequest,
			final FileInputStream fileInputStream,
			final String fileName) throws FileNotFoundException, IOException {
						
			ResponseBuilder responseBuilder = null;
				
			rifLogger.info(this.getClass(), "Create ZIP response: " + fileName + "; length: " + fileInputStream.getChannel().size());

            responseBuilder = Response.ok(fileInputStream);
//			responseBuilder.setContentLength(len);
//			responseBuilder.setContentType("application/zip");
			
            responseBuilder.header("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"");
			
			return responseBuilder.build();
		}
	
	
	/*
	 * Here we're trying to use some way of determining whether
	 * the browser will automatically display JSON.  This method is
	 * meant to use the 'feature detection' in the client browser.
	 * 
	 * In future this could be an extra parameter the client web 
	 * page stuffs into the request header after is uses something like
	 * Modernizr
	 * 
	 */
	private boolean clientBrowserSupportsJSONContentType(
		final HttpServletRequest servletRequest) {
		

		//@TODO
		return false;
	}
	
	/*
	 * Here we are interrogating the value of "user-agent" in the header.
	 * This approach would try to interrogate the free text field value
	 * to figure out the type and version of the browser client.  In future,
	 * it might use the WURFL project to do a look-up in a database of device
	 * profiles.
	 */
	private boolean clientBrowserIsInternetExplorer(
		final HttpServletRequest servletRequest) {
		
		//this approach is known to be weak but it will do for now until
		//we develop a more robust method
	
		//by default it's true
		boolean result = true;
		String browserType = servletRequest.getHeader("User-Agent");
		
		if (browserType != null) {
			browserType = browserType.toUpperCase();
			
			int foundIndex
				= browserType.indexOf("CHROME");
			if (foundIndex != -1) {
				result = false;
			}			
		}
		
		//we're going to make the incredibly lame assumption for now that if it
		//doesn't contain "Chrome", we'll assume it's Internet Explorer.
		//again, the question of what information do we use to determine if we
		//send JSON content type back or not needs to be discussed more
		
		//System.out.println("isClientBrowserIE=="+result+"==");
		
		//System.out.println("clientBrowserIsInternetExplorer IS TRUE");
		//@TODO
		
		return result;
	}
	
	/*
	 * Here we are interrogating the value of "Accept" in the header
	 */
	private boolean clientBrowserMimeTypesIncludeJSON(
		final HttpServletRequest servletRequest) {
		
		
		return true;		
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
