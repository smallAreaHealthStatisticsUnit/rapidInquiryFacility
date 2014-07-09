package rifServices.restfulWebServices;


import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.*;
import rifServices.dataStorageLayer.ProductionRIFStudyServiceBundle;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;










import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.List;

import com.fasterxml.jackson.databind.*;


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
public class RIFStudyResultRetrievalWebServiceResource 
	extends AbstractRIFWebServiceResource {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFStudyResultRetrievalWebServiceResource() {
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	//KLG: N

	
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
	@Path("/getGeometry")
	public String getGeometry(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,	
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,			
		@QueryParam("geoLevelToMapName") String geoLevelToMapName,
		@QueryParam("mapAreaValues") final List<String> mapAreaValues) {
					
		String result = "";
		try {
			
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			Geography geography = Geography.newInstance(geographyName, "");
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(geoLevelSelectName);
			GeoLevelToMap geoLevelToMap
				= GeoLevelToMap.newInstance(geoLevelToMapName);
			ArrayList<MapArea> mapAreas = new ArrayList<MapArea>();
			for (String mapAreaValue : mapAreaValues) {
				//TODO: Not sure where the map area label might come in
				MapArea mapArea
					= MapArea.newInstance(mapAreaValue, mapAreaValue);
				mapAreas.add(mapArea);
			}

			result
				= studyResultRetrievalService.getGeometry(
					user, 
					geography, 
					geoLevelSelect,
					geoLevelToMap, 
					mapAreas);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
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
	@Path("/getGeoLevelBoundsForArea")
	public String getGeometry(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,	
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,			
		@QueryParam("mapArea") String mapAreaValue) {
					
		String result = "";
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			Geography geography = Geography.newInstance(geographyName, "");
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(geoLevelSelectName);
			MapArea mapArea
				= MapArea.newInstance(mapAreaValue, mapAreaValue);		
			Rectangle2D.Double boundsRectangle
				= studyResultRetrievalService.getGeoLevelBoundsForArea(
					user, 
					geography, 
					geoLevelSelect,
					mapArea);
			
			//@TODO: convert rectangle into a result
			
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
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
	@Path("/getGeoLevelFullExtentForStudy")
	public String getGeoLevelFullExtentForStudy(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID) {
					
		String result = "";
				
		try {			
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");

			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);
			Rectangle2D.Double boundsRectangle
				= studyResultRetrievalService.getGeoLevelFullExtentForStudy(
					user, 
					studyResultRetrievalContext);
			
			//@TODO: convert rectangle into a result
			
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
	}	
	
	/**
	 * STUB
	 * @param userID
	 * @param geographyName
	 * @param geoLevelSelectName
	 * @return
	 */

	@GET
	@Produces({"application/json"})	
	@Path("/getGeoLevelFullExtent")
	public String getGeoLevelFullExtent(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName) {
					
		String result = "";

		try {			
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			Geography geography = Geography.newInstance(geographyName, "");
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(geoLevelSelectName);
			Rectangle2D.Double boundsRectangle
				= studyResultRetrievalService.getGeoLevelFullExtent(
					user, 
					geography, 
					geoLevelSelect);
			
			//@TODO: convert rectangle into a result
			
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
	}	
	

	@GET
	@Produces({"application/json"})	
	@Path("/getTiles")
	public String getTiles(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("zoomFactor") String zoomFactorValue,
		@QueryParam("tileIdentifier") String tileIdentifier) {
					
		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			Geography geography = Geography.newInstance(geographyName, "");
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(geoLevelSelectName);
			Integer zoomValue = Integer.valueOf(zoomFactorValue);

			result
				= studyResultRetrievalService.getTiles(
					user, 
					geography, 
					geoLevelSelect,
					zoomValue,
					tileIdentifier);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
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
	 * @param geoLevelAttributeName
	 * @return
	 */
	@GET
	@Produces({"application/json"})	
	@Path("/getMapAreaAttributeValues")
	public String getMapAreaAttributeValues(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttributeName") String geoLevelAttributeName) {
					
		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			Geography geography = Geography.newInstance(geographyName, "");
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(geoLevelSelectName);
			
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			DiseaseMappingStudy diseaseMappingStudy = null;
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName, 
					geoLevelSelectName, 
					diseaseMappingStudyID);
			
			ArrayList<MapAreaAttributeValue> mapAreaAttributeValues
				= studyResultRetrievalService.getMapAreaAttributeValues(
					user,
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelAttributeName);
			
			
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
	}	
	
	/**
	 * STUB
	 * @param userID
	 * @param geographyName
	 * @param geoLevelSelectName
	 * @param diseaseMappingStudyID
	 * @return
	 */

	@GET
	@Produces({"application/json"})	
	@Path("/getGeoLevelAttributeThemes")
	public String getGeoLevelAttributeThemes(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName) {
					
		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName, 
					geoLevelSelectName, 
					diseaseMappingStudyID);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			
			ArrayList<GeoLevelAttributeTheme> geoLevelAttributeThemes
				= studyResultRetrievalService.getGeoLevelAttributeThemes(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
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
	 * @param geoLevelAttributeName
	 * @return
	 */
	
	@GET
	@Produces({"application/json"})	
	@Path("/getAllAttributesForGeoLevelAttributeTheme")
	public String getAllAttributesForGeoLevelAttributeTheme(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttributeThemeName") String geoLevelAttributeThemeName) {
					
		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");

			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName, 
					geoLevelSelectName, 
					diseaseMappingStudyID);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			GeoLevelAttributeTheme geoLevelAttributeTheme
				= GeoLevelAttributeTheme.newInstance(geoLevelAttributeThemeName);
						
			String[] attributes
				= studyResultRetrievalService.getAllAttributesForGeoLevelAttributeTheme(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource, 
					geoLevelAttributeTheme);			
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
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
	 * @param geoLevelAttributeThemeName
	 * @return
	 */
	
	@GET
	@Produces({"application/json"})	
	@Path("/getNumericAttributesForGeoLevelAttributeTheme")
	public String getNumericAttributesForGeoLevelAttributeTheme(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttributeThemeName") String geoLevelAttributeThemeName) {
					
		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			DiseaseMappingStudy diseaseMappingStudy = null;
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName, 
					geoLevelSelectName, 
					diseaseMappingStudyID);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			GeoLevelAttributeTheme geoLevelAttributeTheme
				= GeoLevelAttributeTheme.newInstance(geoLevelAttributeThemeName);
			
			String[] attributes
				= studyResultRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource, 
					geoLevelAttributeTheme);			
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
	}	

	
	/**
	 * STUB
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
	@Path("/getCalculatedResultsByBlock")
	public String getCalculatedResultsByBlock(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("calculatedResultColumnFieldNames") List<String> calculatedResultColumnFieldNames,
		@QueryParam("startRowIndex") String startRowIndexValue,
		@QueryParam("endRowIndex") String endRowIndexValue) {
					
		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");

			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName, 
					geoLevelSelectName, 
					diseaseMappingStudyID);
			
			String[] resultTableFieldNames
				= calculatedResultColumnFieldNames.toArray(new String[0]);
			Integer startRowIndex = Integer.valueOf(startRowIndexValue);
			Integer endRowIndex = Integer.valueOf(endRowIndexValue);
			
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getCalculatedResultsByBlock(
					user, 
					studyResultRetrievalContext,
					resultTableFieldNames,
					startRowIndex,
					endRowIndex);
			
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
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
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("calculatedResultColumnFieldNames") List<String> calculatedResultColumnFieldNames,
		@QueryParam("startRowIndex") String startRowIndexValue,
		@QueryParam("endRowIndex") String endRowIndexValue) {
					
		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);

			String[] resultTableFieldNames
				= calculatedResultColumnFieldNames.toArray(new String[0]);
			Integer startRowIndex = Integer.valueOf(startRowIndexValue);
			Integer endRowIndex = Integer.valueOf(endRowIndexValue);
			
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getExtractResultsByBlock(
					user, 
					studyResultRetrievalContext,
					resultTableFieldNames,
					startRowIndex,
					endRowIndex);
			
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
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
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttribute") String geoLevelAttribute,
		@QueryParam("mapAreaValues") List<String> mapAreaValues,
		@QueryParam("year") String yearValue) {
					
		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			
			ArrayList<MapArea> mapAreas = new ArrayList<MapArea>();
			for (String mapAreaValue : mapAreaValues) {
				//TODO: Not sure where the map area label might come in
				MapArea mapArea
					= MapArea.newInstance(mapAreaValue, mapAreaValue);
				mapAreas.add(mapArea);
			}

			Integer year = Integer.valueOf(yearValue);
			
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelAttribute,
					mapAreas,
					year);
			
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
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
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttribute") String geoLevelAttribute) {
					
		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			
			ArrayList<AgeGroup> ageGroups
				= studyResultRetrievalService.getResultAgeGroups(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelAttribute);
			
			ArrayList<AgeGroupProxy> ageGroupProxies = new ArrayList<AgeGroupProxy>();
			for (AgeGroup ageGroup : ageGroups) {
				AgeGroupProxy ageGroupProxy = new AgeGroupProxy();
				ageGroupProxy.setName(ageGroup.getName());
				ageGroupProxy.setLowerLimit(ageGroup.getLowerLimit());
				ageGroupProxy.setUpperLimit(ageGroup.getUpperLimit());
				ageGroupProxies.add(ageGroupProxy);
			}
			result = serialiseResult(ageGroupProxies);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
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
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttribute") String geoLevelAttribute) {
					
		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);

			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getPyramidData(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelAttribute);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
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
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttribute") String geoLevelAttribute,
		@QueryParam("year") String yearValue) {
					
		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);
			Integer year = Integer.valueOf(yearValue);
			
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getPyramidDataByYear(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelAttribute,
					year);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
	}	

	
	
	@GET
	@Produces({"application/json"})	
	@Path("/getPyramidDataByMapAreas")
	public String getPyramidDataByMapAreas(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName,
		@QueryParam("geoLevelAttribute") String geoLevelAttribute,
		@QueryParam("mapAreaValues") List<String> mapAreaValues) {
					
		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);

			ArrayList<MapArea> mapAreas = new ArrayList<MapArea>();
			for (String mapAreaValue : mapAreaValues) {
				//TODO: Not sure where the map area label might come in
				MapArea mapArea
					= MapArea.newInstance(mapAreaValue, mapAreaValue);
				mapAreas.add(mapArea);
			}
						
			RIFResultTable rifResultTable
				= studyResultRetrievalService.getPyramidDataByMapAreas(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelAttribute,
					mapAreas);
			
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
	}	

	@GET
	@Produces({"application/json"})	
	@Path("/getResultFieldsStratifiedByAgeGroup")
	public String getResultFieldsStratifiedByAgeGroup(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID,
		@QueryParam("geoLevelAttributeSourceName") String geoLevelAttributeSourceName) {


		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					geographyName,
					geoLevelSelectName,
					diseaseMappingStudyID);
			GeoLevelAttributeSource geoLevelAttributeSource
				= GeoLevelAttributeSource.newInstance(geoLevelAttributeSourceName);

			String[] resultFields
				= studyResultRetrievalService.getResultFieldsStratifiedByAgeGroup(
					user, 
					studyResultRetrievalContext,
					geoLevelAttributeSource);
			
			

			
			
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
		
	}	
	

	@GET	
	@Produces({"application/json"})	
	@Path("/getSMRValues")
	public String getSMRValues(
		@QueryParam("userID") String userID,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID) {

		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			DiseaseMappingStudy diseaseMappingStudy = null;

			RIFResultTable rifResultTable
				= studyResultRetrievalService.getSMRValues(
					user, 
					diseaseMappingStudy);
						
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
		
	}	
	

	@GET	
	@Produces({"application/json"})	
	@Path("/getRRValues")
	public String getRRValues(
		@QueryParam("userID") String userID,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID) {

		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			DiseaseMappingStudy diseaseMappingStudy = null;

			RIFResultTable rifResultTable
				= studyResultRetrievalService.getRRValues(
					user, 
					diseaseMappingStudy);
						
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
		
	}	
	

	@GET	
	@Produces({"application/json"})	
	@Path("/getRRUnadjustedValues")
	public String getRRUnadjustedValues(
		@QueryParam("userID") String userID,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID) {

		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			DiseaseMappingStudy diseaseMappingStudy = null;

			RIFResultTable rifResultTable
				= studyResultRetrievalService.getRRUnadjustedValues(
					user, 
					diseaseMappingStudy);
						
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
		
	}	
	
	@GET	
	@Produces({"application/json"})	
	@Path("/getStudyResultGeneralInfo")
	public String getStudyResultGeneralInfo(
		@QueryParam("userID") String userID,
		@QueryParam("diseaseMappingStudyID") String diseaseMappingStudyID) {

		String result = "";
		
		try {
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			User user = User.newInstance(userID, "xxxx");
			DiseaseMappingStudy diseaseMappingStudy = null;

			RIFResultTable rifResultTable
				= studyResultRetrievalService.getStudyResultGeneralInfo(
					user, 
					diseaseMappingStudy);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
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
