package org.sahsu.rif.services.rest;

import java.io.InputStream;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.RIFStudyResultRetrievalAPI;
import org.sahsu.rif.services.concepts.Sex;

import com.sun.jersey.multipart.FormDataParam;

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
 */
@Path("/")
public class StudyResultRetrievalServiceResource extends WebService {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();

	public StudyResultRetrievalServiceResource() {

	}

	@GET
	@Produces({"application/json"})
	@Path("/getCurrentStatusAllStudies")
	public Response getCurrentStatusAllStudies(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID) {

		String result = "";
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);


			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
					= getRIFStudyResultRetrievalService();
			RIFResultTable resultTable
					= studyResultRetrievalService.getCurrentStatusAllStudies(user);
			RIFResultTableJSONGenerator rifResultTableJSONGenerator
					= new RIFResultTableJSONGenerator();
			result = rifResultTableJSONGenerator.writeResultTable(resultTable);
		}
		catch(Exception exception) {
			rifLogger.error(
					this.getClass(),
					"GET /getCurrentStatusAllStudies method failed: ",
					exception);
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}

		WebServiceResponseGenerator webServiceResponseGenerator
				= getWebServiceResponseGenerator();
		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getGeographies")
	public Response getGeographies(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID) {

		return
				super.getGeographies(
						servletRequest,
						userID);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getGeoLevelSelectValues")
	public Response getGeographicalLevelSelectValues(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName) {

		return super.getGeographicalLevelSelectValues(
				servletRequest,
				userID,
				geographyName);
	}


	@GET
	@Produces({"application/json"})
	@Path("/getDefaultGeoLevelSelectValue")
	public Response getDefaultGeoLevelSelectValue(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName) {

		return super.getDefaultGeoLevelSelectValue(
				servletRequest,
				userID,
				geographyName);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getGeoLevelAreaValues")
	public Response getGeoLevelAreaValues(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName,
			@QueryParam("geoLevelSelectName") String geoLevelSelectName) {

		return super.getGeoLevelAreaValues(
				servletRequest,
				userID,
				geographyName,
				geoLevelSelectName);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getGeoLevelViews")
	public Response getGeoLevelViewValues(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName,
			@QueryParam("geoLevelSelectName") String geoLevelSelectName) {

		return super.getGeoLevelViewValues(
				servletRequest,
				userID,
				geographyName,
				geoLevelSelectName);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getNumerator")
	public Response getNumerator(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName,
			@QueryParam("healthThemeDescription") String healthThemeDescription) {


		return super.getNumerator(
				servletRequest,
				userID,
				geographyName,
				healthThemeDescription);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getDenominator")
	public Response getDenominator(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName,
			@QueryParam("healthThemeDescription") String healthThemeDescription) {

		return super.getDenominator(
				servletRequest,
				userID,
				geographyName,
				healthThemeDescription);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getYearRange")
	public Response getYearRange(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName,
			@QueryParam("numeratorTableName") String numeratorTableName) {

		return super.getYearRange(
				servletRequest,
				userID,
				geographyName,
				numeratorTableName);
	}


	/**
	 * @param userID
	 * @param study_id
	 * @return
	 */
	@GET
	@Produces({"application/json"})
	@Path("/getYearsForStudy")
	public Response getYearsForStudy(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("study_id") String studyID) {

		String result = "";
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
					= getRIFStudyResultRetrievalService();
			ArrayList<Integer> years
					= studyResultRetrievalService.getYearsForStudy(
					user,
					studyID);
			ArrayList<String> yearsAsStrings = new ArrayList<String>();
			for (Integer year : years) {
				yearsAsStrings.add(String.valueOf(year));
			}

			result
					= serialiseNamedArray("years", yearsAsStrings);
		}
		catch(Exception exception) {
			rifLogger.error(
					this.getClass(),
					"GET /getYearsForStudy method failed: ",
					exception);
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}

		WebServiceResponseGenerator webServiceResponseGenerator
				= getWebServiceResponseGenerator();
		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}


	/**
	 * @param userID
	 * @param study_id
	 * @return
	 */
	@GET
	@Produces({"application/json"})
	@Path("/getSexesForStudy")
	public Response getSexesForStudy(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("study_id") String studyID) {

		String result = "";
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
					= getRIFStudyResultRetrievalService();

			ArrayList<Sex> sexes
					= studyResultRetrievalService.getSexesForStudy(
					user,
					studyID);
			String[] sexNames = new String[sexes.size()];
			for (int i = 0; i < sexNames.length; i++) {
				sexNames[i] = sexes.get(i).getName();
			}

			SexesProxy sexesProxy = new SexesProxy();
			sexesProxy.setNames(sexNames);

			result
					= serialiseSingleItemAsArrayResult(
					servletRequest,
					sexesProxy);
		}
		catch(Exception exception) {
			rifLogger.error(
					this.getClass(),
					"GET /getSexesForStudy method failed: ",
					exception);
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}

		WebServiceResponseGenerator webServiceResponseGenerator
				= getWebServiceResponseGenerator();
		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}


	/**
	 * @param userID
	 * @param geographyName
	 * @param geoLevelSelectName
	 * @param geoLevelToMapName
	 * @param mapAreaValues
	 * @return
	 */
	@GET
	@Produces({"application/json"})
	@Path("/getSmoothedResults")
	public String getSmoothedResults(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID,
			@QueryParam("sex") String sex) {

		String result = "";
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
					= getRIFStudyResultRetrievalService();
			RIFResultTable resultTable
					= studyResultRetrievalService.getSmoothedResults(
					user,
					studyID,
					sex);

			RIFResultTableJSONGenerator rifResultTableJSONGenerator
					= new RIFResultTableJSONGenerator();
			result = rifResultTableJSONGenerator.writeResultTable(resultTable);

		}
		catch(Exception exception) {
			rifLogger.error(
					this.getClass(),
					"GET /getSmoothedResults method failed: ",
					exception);
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}

		return result;
	}

	@GET
	@Produces({"application/json"})
	@Path("/getAllPopulationPyramidData")
	public String getAllPopulationPyramidData(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID,
			@QueryParam("year") String year) {

		String result = "";
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
					= getRIFStudyResultRetrievalService();
			RIFResultTable resultTable
					= studyResultRetrievalService.getPopulationPyramidData(
					user,
					studyID,
					year);

			RIFResultTableJSONGenerator rifResultTableJSONGenerator
					= new RIFResultTableJSONGenerator();
			result = rifResultTableJSONGenerator.writeResultTable(resultTable);
		}
		catch(Exception exception) {
			rifLogger.error(
					this.getClass(),
					"GET /getAllPopulationPyramidData method failed: ",
					exception);
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}

		return result;
	}


	@GET
	@Produces({"application/json"})
	@Path("/getTileMakerTiles")
	public Response getTileMakerTiles(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName, //SAHSU
			@QueryParam("geoLevelSelectName") String geoLevelSelectName, //LEVEL2
			@QueryParam("zoomlevel") Integer zoomlevel,	//3
			@QueryParam("x") Integer x, //3
			@QueryParam("y") Integer y, //2 
			@QueryParam("tileType") String tileType /* null (topojson), topojson, geojson or png */) 
				{
		return super.getTileMakerTiles(
				servletRequest,
				userID,
				geographyName,
				geoLevelSelectName,
				zoomlevel,
				x,
				y,
				tileType);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getTileMakerAttributes")
	public Response getTileMakerTiles(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName, //SAHSU
			@QueryParam("geoLevelSelectName") String geoLevelSelectName //LEVEL2
			) { 

		return super.getTileMakerAttributes(
				servletRequest,
				userID,
				geographyName,
				geoLevelSelectName);
	}
	
	@GET
	@Produces({"application/json"})
	@Path("/getMapBackground")
	public Response getMapBackground(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName
			) { 

		return super.getMapBackground(servletRequest, userID, geographyName);
	}	
		
	@GET
	@Produces({"application/json"})
	@Path("/getHomogeneity")
	public Response getHomogeneity(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID
			) { 

		return super.getHomogeneity(servletRequest, userID, studyID);
	}	
	
	@GET
	@Produces({"application/json"})
	@Path("/getCovariateLossReport")
	public Response getCovariateLossReport(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID
			) { 

		return super.getCovariateLossReport(servletRequest, userID, studyID);
	}	
	
	@GET
	@Produces({"application/json"})
	@Path("/getRiskGraph")
	public Response getRiskGraph(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID
			) { 

		return super.getRiskGraph(servletRequest, userID, studyID);
	}		
	
	@GET
	@Produces({"application/json"})
	@Path("/getSelectState")
	public Response getSelectState(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID
			) { 

		return super.getSelectState(servletRequest, userID, studyID);
	}	
		
	@GET
	@Produces({"application/json"})
	@Path("/getPrintState")
	public Response getPrintState(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID
			) { 

		return super.getPrintState(servletRequest, userID, studyID);
	}	

	@POST
	@Produces({"application/json"})
	@Path("/setPrintState")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response submitStudy(
			@Context HttpServletRequest servletRequest,
			@FormDataParam("userID") String userID,
			@FormDataParam("studyID") String studyID,
			@FormDataParam("fileFormat") String fileFormat,
			@FormDataParam("fileField") InputStream inputStream) {

		return super.setPrintState(
				servletRequest,
				userID,
				studyID,
				fileFormat,
				inputStream);

	}					
									
	@GET
	@Produces({"application/json"})
	@Path("/getPostalCodes")
	public Response getPostalCodes(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName, //SAHSU
			@QueryParam("postcode") String postcode
			) { 

		return super.getPostalCodes(servletRequest, userID, geographyName, postcode);
	}
	
	@GET
	@Produces({"application/json"})
	@Path("/getPostalCodeCapabilities")
	public Response getPostalCodeCapabilities(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName
			) { 

		return super.getPostalCodeCapabilities(servletRequest, userID, geographyName);
	}
	
	@GET
	@Produces({"application/json"})
	@Path("/getTileMakerCentroids")
	public Response getTileMakerCentroids(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName, //SAHSU
			@QueryParam("geoLevelSelectName") String geoLevelSelectName ) { //LEVEL2

		return super.getTileMakerCentroids(servletRequest, userID, geographyName, geoLevelSelectName);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getGeographyAndLevelForStudy")
	public String getGeographyAndLevelForStudy(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID) {

		String result;

		try {

			User user = createUser(servletRequest, userID);

			RIFStudyResultRetrievalAPI studyResultRetrievalService =
					getRIFStudyResultRetrievalService();
			String[] results = studyResultRetrievalService.getGeographyAndLevelForStudy(
					user, studyID);

			//Convert results to support JSON
			result = serialiseSingleItemAsArrayResult(servletRequest, results);
		} catch(Exception exception) {
			rifLogger.error(getClass(), "GET /getTileMakerTiles method failed: ",
			                exception);

			//Convert exceptions to support JSON
			result = serialiseException(servletRequest, exception);
		}

		return result;
	}

	@GET
	@Produces({"application/json"})
	@Path("/getDetailsForProcessedStudy")
	public String getDetailsForProcessedStudy(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID) {

		String result = "";

		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
					= getRIFStudyResultRetrievalService();
			String[] results
					= studyResultRetrievalService.getDetailsForProcessedStudy(user, studyID);

			//Convert results to support JSON
			result
					= serialiseSingleItemAsArrayResult(
					servletRequest,
					results);
		}
		catch(Exception exception) {
			rifLogger.error(
					this.getClass(),
					"GET /getDetailsForProcessedStudy method failed: ",
					exception);
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}

		return result;

	}

	@GET
	@Produces({"application/json"})
	@Path("/getStudyTableForProcessedStudy")
	public String getStudyTableForProcessedStudy(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID,
			@QueryParam("type") String type,
			@QueryParam("stt") String stt,
			@QueryParam("stp") String stp) {

		String result = "";

		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
					= getRIFStudyResultRetrievalService();

			RIFResultTable rifResultTable
					= studyResultRetrievalService.getStudyTableForProcessedStudy(user, studyID, type, stt, stp);

			//Convert results to support JSON
			result
					= serialiseSingleItemAsArrayResult(
					servletRequest,
					rifResultTable);
		}
		catch(Exception exception) {
			rifLogger.error(
					this.getClass(),
					"GET /getStudyTableForProcessedStudy method failed: ",
					exception);
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}

		return result;

	}

	@GET
	@Produces({"application/json"})
	@Path("/getHealthCodesForProcessedStudy")
	public String getHealthCodesForProcessedStudy(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID) {

		String result = "";

		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
					= getRIFStudyResultRetrievalService();
			String[] results
					= studyResultRetrievalService.getHealthCodesForProcessedStudy(user, studyID);

			//Convert results to support JSON
			result
					= serialiseSingleItemAsArrayResult(
					servletRequest,
					results);
		}
		catch(Exception exception) {
			rifLogger.error(
					this.getClass(),
					"GET /getHealthCodesForProcessedStudy method failed: ",
					exception);
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}

		return result;

	}
}
