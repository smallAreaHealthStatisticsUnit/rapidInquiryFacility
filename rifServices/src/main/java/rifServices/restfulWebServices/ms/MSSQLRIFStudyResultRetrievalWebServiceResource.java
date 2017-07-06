package rifServices.restfulWebServices.ms;

import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.businessConceptLayer.User;
import rifServices.businessConceptLayer.*;
import rifServices.restfulWebServices.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;


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

@Path("/")
public class MSSQLRIFStudyResultRetrievalWebServiceResource 
	extends MSSQLAbstractRIFWebServiceResource {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLRIFStudyResultRetrievalWebServiceResource() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	//KLG: 
	
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
	 * STUB
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
	 * STUB
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
	 * STUB
	 * @param userID
	 * @param study_id
	 * @return
	 */
	@GET
	@Produces({"application/json"})	
	@Path("/getSmoothedResultAttributes")
	public String getSmoothedResultAttributes(
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
			ArrayList<String> smoothedAttributes
				= studyResultRetrievalService.getSmoothedResultAttributes(
					user);
			
			result = serialiseArrayResult(servletRequest, smoothedAttributes);
			
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result 
				= serialiseException(
					servletRequest,
					exception);			
		}
		
		return result;
	}	
	
	
	/**
	 * STUB
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
			//Convert exceptions to support JSON
			result 
				= serialiseException(
					servletRequest,
					exception);			
		}
		
		return result;
	}	
	
	
	/**
	 * STUB
	 * @param userID
	 * @param geographyName
	 * @param geoLevelSelectName
	 * @param geoLevelToMapName
	 * @param mapAreaValues
	 * @return
	 */
	@GET
	@Produces({"application/json"})	
	@Path("/getSmoothedResultsForAttributes")
	public String getSmoothedResultsForAttributes(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("studyID") String studyID,
		@QueryParam("sex") String sex,		
		@QueryParam("smoothedAttribute") List<String> smoothedAttributeList) {
	
		String result = "";
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			
			//We convert contents of a list into an ArrayList.  List is one of the 
			//types supported by the @QueryParam feature, but the middleware method
			//signatures tend to use ArrayLists for parameters containing lists
			ArrayList<String> smoothedAttributesToInclude = new ArrayList<String>();
			smoothedAttributesToInclude.addAll(smoothedAttributeList);
			
			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			RIFResultTable resultTable
				= studyResultRetrievalService.getSmoothedResultsForAttributes(
					user, 
					smoothedAttributesToInclude,
					studyID,
					sex);

			RIFResultTableJSONGenerator rifResultTableJSONGenerator
				= new RIFResultTableJSONGenerator();
			result = rifResultTableJSONGenerator.writeResultTable(resultTable);	
			
		}
		catch(Exception exception) {
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
			//Convert exceptions to support JSON
			result 
				= serialiseException(
					servletRequest,
					exception);			
		}
		
		return result;
	}	
	
	/**
	 * STUB
	 * @param userID
	 * @param studyID
	 * @param areaIDList
	 * @return
	 */

	@GET
	@Produces({"application/json"})	
	@Path("/getPopulationPyramidDataForAreas")
	public String getPopulationPyramidDataForAreas(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("studyID") String studyID,
		@QueryParam("year") String year,
		@QueryParam("areaID") List<String> areaIDList) {
	
		String result = "";
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			
			//We convert contents of a list into an ArrayList.  List is one of the 
			//types supported by the @QueryParam feature, but the middleware method
			//signatures tend to use ArrayLists for parameters containing lists
			ArrayList<MapArea> mapAreas = new ArrayList<MapArea>();
			
			for (String areaID : areaIDList) {
				mapAreas.add(MapArea.newInstance(areaID, areaID, areaID));
			}
						
			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			RIFResultTable resultTable
				= studyResultRetrievalService.getPopulationPyramidData(
					user, 
					studyID,
					year,
					mapAreas);
			
			RIFResultTableJSONGenerator rifResultTableJSONGenerator
				= new RIFResultTableJSONGenerator();
			result = rifResultTableJSONGenerator.writeResultTable(resultTable);	
		}
		catch(Exception exception) {
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
		@QueryParam("y") Integer y) { //2
				
		return super.getTileMakerTiles(
				servletRequest, 
				userID, 
				geographyName, 
				geoLevelSelectName, 
				zoomlevel, 
				x, 
				y);	
	}
		
	@GET
	@Produces({"application/json"})	
	@Path("/getMapAreasForBoundaryRectangle")
	public Response getMapAreasForBoundaryRectangle(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("tileIdentifier") String tileIdentifier,
		@QueryParam("zoomFactor") Integer zoomFactor,		
		@QueryParam("yMax") String yMax,
		@QueryParam("xMax") String xMax,
		@QueryParam("yMin") String yMin,
		@QueryParam("xMin") String xMin) {
					
		return super.getMapAreasForBoundaryRectangle(
			servletRequest, 
			userID, 
			geographyName, 
			geoLevelSelectName, 
			yMax, 
			xMax, 
			yMin, 
			xMin);
		
	}	
	
	/**
	 * STUB
	 * 
	 * -- note - are we assuming that we don't need the geo level attribute source
	 * because the results will be in one of two tables: "calculated" or "extract"?
	 * 
	 * @param userID
	 * @param geographyName
	 * @param geoLevelSelectName
	 * @param diseaseMappingStudyID
	 * @param calculatedResultColumnFieldNames
	 * @param startRowIndexValue
	 * @param endRowIndexValue
	 * @return
	 */
	
	@GET
	@Produces({"application/json"})	
	@Path("/getExtractResultsByBlock")
	public String getExtractResultsByBlock(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("calculatedResultColumnFieldNames") List<String> calculatedResultColumnFieldNames,
		@QueryParam("startRowIndex") String startRowIndexValue,
		@QueryParam("endRowIndex") String endRowIndexValue) {
					
		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters			
			User user = createUser(servletRequest, userID);
			StudySummary studySummary
				= StudySummary.newInstance(
					diseaseMappingStudyID, 
					"", 
					"");
			String[] resultTableFieldNames
				= calculatedResultColumnFieldNames.toArray(new String[0]);
			Integer startRowIndex = Integer.valueOf(startRowIndexValue);
			Integer endRowIndex = Integer.valueOf(endRowIndexValue);
			
			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getExtractResultsByBlock(
					user, 
					studySummary,
					resultTableFieldNames,
					startRowIndex,
					endRowIndex);

			//Convert results to support JSON
			result 
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					rifResultTable);						
			
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result 
				= serialiseException(
					servletRequest,
					exception);			
		}
		
		return result;
	}	

	/**
	 * STUB
	 * @param userID
	 * @param geographyName
	 * @param geoLevelSelectName
	 * @param diseaseMappingStudyID
	 * @param geoLevelAttributeSourceName
	 * @param geoLevelAttribute
	 * @param mapAreaValues
	 * @param yearValue
	 * @return
	 */
	@GET
	@Produces({"application/json"})	
	@Path("/getResultsStratifiedByGenderAndAgeGroup")
	public String getResultsStratifiedByGenderAndAgeGroup(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelToMapName") String geoLevelToMapName,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttribute") String geoLevelAttribute,
		@QueryParam("gids") List<String> geographicalIdentifiers,
		@QueryParam("year") String yearValue) {
					
		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters			
			User user = createUser(servletRequest, userID);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);
			GeoLevelToMap geoLevelToMap
				= GeoLevelToMap.newInstance(geoLevelToMapName);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			ArrayList<MapArea> mapAreas = new ArrayList<MapArea>();
			for (String geographicalIdentifier : geographicalIdentifiers) {
				//TODO: Not sure where the map area label might come in
				MapArea mapArea
					= MapArea.newInstance(geographicalIdentifier, "", "");
				mapAreas.add(mapArea);
			}
			Integer year = Integer.valueOf(yearValue);
			
			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
					user, 
					studyResultRetrievalContext,
					geoLevelToMap,
					geoLevelAttributeSource,
					geoLevelAttribute,
					mapAreas,
					year);

			//Convert results to support JSON
			result 
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					rifResultTable);						

		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result 
				= serialiseException(
					servletRequest,
					exception);			
		}
		
		return result;
	}	

	/**
	 * STUB
	 * @param userID
	 * @param geographyName
	 * @param geoLevelSelectName
	 * @param diseaseMappingStudyID
	 * @param geoLevelAttributeSourceName
	 * @param geoLevelAttribute
	 * @return
	 */
	@GET
	@Produces({"application/json"})	
	@Path("/getResultAgeGroups")
	public String getResultAgeGroups(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttribute") String geoLevelAttribute) {
					
		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters			
			User user = createUser(servletRequest, userID);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			
			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			ArrayList<AgeGroup> ageGroups
				= studyResultRetrievalService.getResultAgeGroups(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelAttribute);
			
			
			//Convert results to support JSON	
			AgeGroupJSONGenerator ageGroupJSONGenerator
				= new AgeGroupJSONGenerator();
			result
				= ageGroupJSONGenerator.writeJSONMapAreas(ageGroups);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result 
				= serialiseException(
					servletRequest,
					exception);			
		}
		
		return result;
	}	

	
	/**
	 * STUB
	 * @param userID
	 * @param geographyName
	 * @param geoLevelSelectName
	 * @param diseaseMappingStudyID
	 * @param geoLevelAttributeSourceName
	 * @param geoLevelAttribute
	 * @return
	 */
	@GET
	@Produces({"application/json"})	
	@Path("/getPyramidData")
	public String getPyramidData(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttribute") String geoLevelAttribute) {
					
		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters			
			User user = createUser(servletRequest, userID);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);

			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			
			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getPyramidData(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelAttribute);

			//Convert results to support JSON
			result 
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					rifResultTable);						
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result 
				= serialiseException(
					servletRequest,
					exception);			
		}
		
		return result;
	}	

	
	/**
	 * STUB
	 * @param userID
	 * @param geographyName
	 * @param geoLevelSelectName
	 * @param diseaseMappingStudyID
	 * @param geoLevelAttributeSourceName
	 * @param geoLevelAttribute
	 * @param yearValue
	 * @return
	 */	
	@GET
	@Produces({"application/json"})	
	@Path("/getPyramidDataByYear")
	public String getPyramidDataByYear(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttribute") String geoLevelAttribute,
		@QueryParam("year") String yearValue) {
					
		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters			
			User user = createUser(servletRequest, userID);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			Integer year = Integer.valueOf(yearValue);
			
			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getPyramidDataByYear(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelAttribute,
					year);

			//Convert results to support JSON
			result 
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					rifResultTable);						
		}
		catch(Exception exception) {
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
	@Path("/getPyramidDataByMapAreas")
	public String getPyramidDataByMapAreas(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("geoLevelToMapName") String geoLevelToMapName,		
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttribute") String geoLevelAttribute,
		@QueryParam("geographicalIdentifiers") List<String> geographicalIdentifiers) {
					
		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters			
			User user = createUser(servletRequest, userID);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);
			GeoLevelToMap geoLevelToMap
				= GeoLevelToMap.newInstance(geoLevelToMapName);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			ArrayList<MapArea> mapAreas = new ArrayList<MapArea>();
			for (String geographicalIdentifier : geographicalIdentifiers) {
				//TODO: Not sure where the map area label might come in
				MapArea mapArea
					= MapArea.newInstance(geographicalIdentifier, "", "");
				mapAreas.add(mapArea);
			}
						
			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getPyramidDataByMapAreas(
					user, 
					studyResultRetrievalContext,
					geoLevelToMap,
					geoLevelAttributeSource,
					geoLevelAttribute,
					mapAreas);

			//Convert results to support JSON
			result 
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					rifResultTable);						
			
		}
		catch(Exception exception) {
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
	@Path("/getResultFieldsStratifiedByAgeGroup")
	public String getResultFieldsStratifiedByAgeGroup(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName) {


		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters			
			User user = createUser(servletRequest, userID);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);

			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			String[] resultFields
				= studyResultRetrievalService.getResultFieldsStratifiedByAgeGroup(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource);

			//Convert results to support JSON
			result 
				= serialiseArrayResult(
					servletRequest,
					resultFields);		
		}
		catch(Exception exception) {
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
	@Path("/getSMRValues")
	public String getSMRValues(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID) {

		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters			
			User user = createUser(servletRequest, userID);
			StudySummary studySummary
				= StudySummary.newInstance(
					diseaseMappingStudyID, 
					"", 
					"");
			
			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getSMRValues(
					user, 
					studySummary);

			//Convert results to support JSON
			result 
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					rifResultTable);						
						
		}
		catch(Exception exception) {
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
	@Path("/getRRValues")
	public String getRRValues(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID) {

		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters			
			User user = createUser(servletRequest, userID);
			StudySummary studySummary
				= StudySummary.newInstance(
					diseaseMappingStudyID, 
					"", 
					"");
		
			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getRRValues(
					user, 
					studySummary);

			//Convert results to support JSON
			result 
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					rifResultTable);						
						
		}
		//Convert exceptions to support JSON
		catch(Exception exception) {
			result 
				= serialiseException(
					servletRequest,
					exception);			
		}
		
		return result;
		
	}	
	

	@GET	
	@Produces({"application/json"})	
	@Path("/getRRUnadjustedValues")
	public String getRRUnadjustedValues(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID) {

		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters			
			User user = createUser(servletRequest, userID);
			StudySummary studySummary
				= StudySummary.newInstance(
					diseaseMappingStudyID, 
					"", 
					"");

			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getRRUnadjustedValues(
					user, 
					studySummary);

			//Convert results to support JSON
			result 
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					rifResultTable);						
		}
		catch(Exception exception) {
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
	@Path("/getStudyResultGeneralInfo")
	public String getStudyResultGeneralInfo(
		@Context HttpServletRequest servletRequest,	
		@QueryParam("userID") String userID,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID) {

		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters			
			User user = createUser(servletRequest, userID);
			StudySummary studySummary
				= StudySummary.newInstance(
					diseaseMappingStudyID, 
					"", 
					"");

			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getStudyResultGeneralInfo(
					user, 
					studySummary);

			//Convert results to support JSON
			result 
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					rifResultTable);
		}
		catch(Exception exception) {
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
	@Path("/getGeographyAndLevelForStudy")
	public String getGeographyAndLevelForStudy(
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
				= studyResultRetrievalService.getGeographyAndLevelForStudy(user, studyID);

			//Convert results to support JSON
			result 
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					results);
		}
		catch(Exception exception) {
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
			//Convert exceptions to support JSON
			result 
				= serialiseException(
					servletRequest,
					exception);			
		}
		
		return result;
		
	}	
		
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
