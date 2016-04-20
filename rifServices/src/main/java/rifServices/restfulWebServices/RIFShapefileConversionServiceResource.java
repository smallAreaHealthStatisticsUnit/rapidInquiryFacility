package rifServices.restfulWebServices;

import com.sun.jersey.multipart.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.HttpServletRequest;
import java.io.*;


/**
 * This class advertises API methods found in 
 * {@link rifServices.businessConceptLayer.RIFJobSubmissionAPI}
 * as a web service.  
 * 
 * Two issues have dominated the design of this class:
 * <ul>
 * <li>
 * the slight mismatch between URL parameter values and corresponding instances of Java
 * objects
 * </li>
 * <li>
 * the level of granularity in the conversations we would expect the web service to have
 * with the client
 * </li>
 * <li>
 * The efficiency with which 
 * </ul>
 * 
 * <p>
 * 
 * 
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
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

@Path("/")
public class RIFShapefileConversionServiceResource {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	private WebServiceResponseGenerator webServiceResponseGenerator;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFShapefileConversionServiceResource() {
		super();
		
		webServiceResponseGenerator = new WebServiceResponseGenerator();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	@GET
	@Produces({"application/json"})	
	@Path("/login")
	public Response login(
		@Context HttpServletRequest servletRequest,
		@QueryParam("userID") String userID,
		@QueryParam("password") String password) {

		String result = "";
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	@GET
	@Produces({"application/json"})	
	@Path("/isLoggedIn")
	public Response login(
		@Context HttpServletRequest servletRequest,
		@QueryParam("userID") String userID) {

		String result = "";
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	@GET
	@Produces({"application/json"})	
	@Path("/logout")
	public Response logout(
		@Context HttpServletRequest servletRequest,
		@QueryParam("userID") String userID) {

		String result = "";

	
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}


	@GET
	@Produces({"application/json"})	
	@Path("/testShapeFileSubmission")
	public Response testShapeFileSubmission(
		@Context HttpServletRequest servletRequest,
		@QueryParam("userID") String userID) {

		String result = "Testing shape file submission with userid==" + userID + "==";
	
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
/*	
@POST
@Produces({"application/json"})	
@Path("/submitShapeFiles")
@Consumes(MediaType.MULTIPART_FORM_DATA)
public Response submitShapeFiles(
	@Context HttpServletRequest servletRequest,
	@FormDataParam("userID") String userID,
	@FormDataParam("fileField") InputStream inputStream) {
*/	
/*	
	POST
	Produces({"application/json"})	
	Path("/submitShapeFiles")
	Consumes(MediaType.MULTIPART_FORM_DATA)
*/	
	@POST
	@Produces(MediaType.TEXT_PLAIN)	
	@Path("/submitShapeFiles")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response submitShapeFiles(
		@Context HttpServletRequest servletRequest,
		@FormDataParam("userID") String userID,
		@FormDataParam("fileField") InputStream inputStream) {

		String result = "test";

/*		
		try {
			
			System.out.println("submitStudy userID=="+userID+"==");
			
			//Convert URL parameters to RIF service API parameters			
			User user = createUser(servletRequest, userID);
			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			result 
				= serialiseException(
					servletRequest,
					exception);			
		}

*/
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);	

		
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
