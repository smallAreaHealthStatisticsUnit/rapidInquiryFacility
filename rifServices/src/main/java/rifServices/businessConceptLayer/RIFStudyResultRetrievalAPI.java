package rifServices.businessConceptLayer;

import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;

import java.util.ArrayList;



/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
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
 * @version
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

public interface RIFStudyResultRetrievalAPI extends RIFStudyServiceAPI {

	/**
	 * gets the centroids for all polygons 
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getTileMakerCentroids(
			final User user,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect)
					throws RIFServiceException;

	/*
	 * Returns JSON stream for a given tile identifier
	 * 
	 * geom - contains geoJson
	 * 
	 */
	public String getTileMakerTiles(
			final User user,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect,
			final Integer zoomlevel,
			final Integer x,
			final Integer y)
					throws RIFServiceException;

	/**
	 * There are multiple data attributes that may be associated at a given 
	 * geo level select (eg: "region", "district"). The attributes are 
	 * categorised by a geo level attribute theme (eg: "health", "geometry")
	 * and depend on which result source (GeoLevelAttributeSource) is being used
	 * (eg: "map table", "extract table"). 
	 * 
	 * @param user
	 * @param geoLevelSelect describes the kind of area being considered. eg: region,
	 * district, ward.
	 * @param geoLevelAttributeSource describes a table whose column names would 
	 * provide geo level attributes
	 * @param geoLevelAttributeTheme: used to group the column names of a result
	 * table into themes
	 * @return list of data attributes that are available for the 
	 * @throws RIFServiceException
	 */
	public String[] getAllAttributesForGeoLevelAttributeTheme(
			final User user,
			final StudyResultRetrievalContext studyResultRetrievalContext,
			final GeoLevelAttributeSource geoLevelAttributeSource,
			final GeoLevelAttributeTheme geoLevelAttributeTheme)
					throws RIFServiceException;

	/**
	 * 
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @param geoLevelAttributeTheme
	 * @return
	 * @throws RIFServiceException
	 */

	public String[] getNumericAttributesForGeoLevelAttributeTheme(
			final User user,
			final StudyResultRetrievalContext studyResultRetrievalContext,
			final GeoLevelAttributeSource geoLevelAttributeSource,
			final GeoLevelAttributeTheme geoLevelAttributeTheme) 
					throws RIFServiceException;


	/**
	 * just displays the statistically calculated data
	 * @param user
	 * @param study
	 * @param investigation
	 * @param startRowIndex
	 * @param endRowIndex
	 * @return
	 * @throws RIFServiceException
	 */	
	public RIFResultTable getCalculatedResultsByBlock(
			final User user,
			final StudySummary studySummary,
			final String[] calculatedResultColumnFieldNames,
			final Integer startRowIndex,
			final Integer endRowIndex)
					throws RIFServiceException;


	/**
	 * accessing data from sources such as the health theme,
	 * the covariates,or data from the extract table
	 * @param user
	 * @param study
	 * @param investigation
	 * @param startRowIndex
	 * @param endRowIndex
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getExtractResultsByBlock(
			final User user,
			final StudySummary studySummary,
			final String[] calculatedResultColumnFieldNames,			
			final Integer startRowIndex,
			final Integer endRowIndex)
					throws RIFServiceException;	

	/**
	 * 
	 * returns a table with the following fields:
	 * age group  |  sex  |  popcount
	 * 
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @param diseaseMappingStudy
	 * @param geoLevelAttributeTheme
	 * @param geoLevelAttribute
	 * @param mapAreas
	 * @param year
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getResultsStratifiedByGenderAndAgeGroup(
			final User user,
			final StudyResultRetrievalContext studyResultRetrievalContext,
			final GeoLevelToMap geoLevelToMap,
			final GeoLevelAttributeSource geoLevelAttributeSource,
			final String geoLevelAttribute,
			final ArrayList<MapArea> mapAreas,
			final Integer year)
					throws RIFServiceException;


	public ArrayList<AgeGroup> getResultAgeGroups(
			final User user,
			final StudyResultRetrievalContext studyResultRetrievalContext,
			final GeoLevelAttributeSource geoLevelSource,
			final String geoLevalAttribute)
					throws RIFServiceException;

	/**
	 * returns data stratified by age group
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @param geoLevelSource
	 * @param geoLevelAttribute
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getPyramidData(
			final User user,
			final StudyResultRetrievalContext studyResultRetrievalContext,
			final GeoLevelAttributeSource geoLevelSource,
			final String geoLevelAttribute) 
					throws RIFServiceException;	
	public RIFResultTable getPyramidDataByYear(
			final User user,
			final StudyResultRetrievalContext studyResultRetrievalContext,
			final GeoLevelAttributeSource geoLevelSource,
			final String geoLevelAttribute,
			final Integer year) 
					throws RIFServiceException;

	/**
	 * returns a table with these fields:
	 * eg:
	 * agegroup     | sex    popcount
	 * @param user
	 * @param studyResultRetrievalContext
	 * @param geoLevelToMap
	 * @param geoLevelSource
	 * @param geoLevelAttribute
	 * @param mapAreas
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getPyramidDataByMapAreas(
			final User user,
			final StudyResultRetrievalContext studyResultRetrievalContext,
			final GeoLevelToMap geoLevelToMap,
			final GeoLevelAttributeSource geoLevelSource,
			final String geoLevelAttribute,
			final ArrayList<MapArea> mapAreas) 
					throws RIFServiceException;


	public String[] getResultFieldsStratifiedByAgeGroup(
			final User user,
			final StudyResultRetrievalContext studyResultRetrievalContext,
			final GeoLevelAttributeSource geoLevelAttributeSource)
					throws RIFServiceException;


	public ArrayList<Integer> getYearsForStudy(
			final User user, 
			final String studyID)
					throws RIFServiceException;

	public ArrayList<Sex> getSexesForStudy(
			final User user,
			final String studyID)
					throws RIFServiceException;

	public String[] getGeographyAndLevelForStudy(
			final User user,
			final String studyID)
					throws RIFServiceException;

	/**
	 * returns field with the following headers:
	 * GID, SMR, CL, CU
	 * @param user
	 * @param diseaseMappingStudy
	 * @return
	 */
	public RIFResultTable getSMRValues(
			final User user,
			final StudySummary studySummary)
					throws RIFServiceException;

	/**
	 * obtains RR (unsmoothed - adjusted) and its confidence intervals for the
	 * study area
	 * @param user
	 * @param diseaseMappingStudy
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getRRValues(
			final User user,
			final StudySummary studySummary)
					throws RIFServiceException;


	/**
	 * returns a table with the following columns
	 * GID  |  RR_unadj  | CL  | CU
	 * @param user
	 * @param diseaseMappingStudy
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getRRUnadjustedValues(
			final User user,
			final StudySummary studySummary)
					throws RIFServiceException;	


	/**
	 * Returns a table with the following fields:
	 *    Total denominator in study
	 *    Observed in study
		- Number of areas in study
		- Average observed in study
		- Total expected adj
		- Average expected adj
		- Relative Risk adj
		- Total expected unadj
		- Average expected unadj
		- Relative Risk unadj
	 * 
	 * @param user
	 * @param diseaseMappingStudy
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getStudyResultGeneralInfo(
			final User user,
			final StudySummary studySummary)
					throws RIFServiceException;


	public ArrayList<String> getSmoothedResultAttributes(
			final User user) 
					throws RIFServiceException;

	public RIFResultTable getSmoothedResults(
			final User user,
			final String studyID,
			final String sex) 
					throws RIFServiceException;

	public RIFResultTable getSmoothedResultsForAttributes(
			final User user,
			final ArrayList<String> smoothedAttributesToInclude,
			final String studyID,
			final String sex) 
					throws RIFServiceException;

	public RIFResultTable getPopulationPyramidData(
			final User user,
			final String studyID,
			final String year)
					throws RIFServiceException;

	public RIFResultTable getPopulationPyramidData(
			final User user,
			final String studyID,
			final String year,
			final ArrayList<MapArea> mapAreas)
					throws RIFServiceException;

	public RIFResultTable getCurrentStatusAllStudies(
			final User user)
					throws RIFServiceException;

	public String[] getDetailsForProcessedStudy(
			final User user,
			final String studyID)
					throws RIFServiceException;
	
	public RIFResultTable getStudyTableForProcessedStudy(
			final User user,
			final String studyID,
			final String type,
			final String stt,
			final String stp)
					throws RIFServiceException;
	
	public String[] getHealthCodesForProcessedStudy(
			final User user,
			final String studyID)
					throws RIFServiceException;


}
