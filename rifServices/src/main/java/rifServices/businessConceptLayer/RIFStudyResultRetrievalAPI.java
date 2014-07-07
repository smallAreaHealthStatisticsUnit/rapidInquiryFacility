package rifServices.businessConceptLayer;

import rifServices.system.RIFServiceException;




import java.util.ArrayList;
import java.awt.geom.Rectangle2D;



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
 * Copyright 2014 Imperial College London, developed by the Small Area
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
	 * Gets the geometry.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelToMap the geo level to map
	 * @param mapAreas the map areas
	 * @return the geometry
	 * @throws RIFServiceException the RIF service exception
	 */
	public String getGeometry(
		User user,		
		Geography geography,
		GeoLevelSelect geoLevelSelect,
		GeoLevelToMap geoLevelToMap,
		ArrayList<MapArea> mapAreas) 
		throws RIFServiceException;
		
	/**
	 * gets the bounds for one single area identifier
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @param mapArea
	 * @return
	 * @throws RIFServiceException
	 */
	public Rectangle2D.Double getGeoLevelBoundsForArea(
		User user,
		Geography geography,
		GeoLevelSelect geoLevelSelect,
		MapArea mapArea)
		throws RIFServiceException;
	
	/**
	 * gets the extent for only areas on the study area and not the 
	 * whole geolevel
	 * 
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @param study
	 * @return
	 * @throws RIFServiceException
	 */
	public Rectangle2D.Double getGeoLevelFullExtentForStudy(
		User user,
		Geography geography,
		GeoLevelSelect geoLevelSelect,
		DiseaseMappingStudy study) 
		throws RIFServiceException;	
	
		
	public Rectangle2D.Double getGeoLevelFullExtent(
		User user,
		Geography geography,
		GeoLevelSelect geoLevelSelect) 
		throws RIFServiceException;


	/*
	 * Returns JSON stream for a given tile identifier
	 * 
	 * geom - contains geoJson
	 * 
	 */
	public String getTiles(
		User user,
		Geography geography,
		GeoLevelSelect geoLevelSelect,
		Integer zoomFactor,
		String tileIdentifier) 
		throws RIFServiceException;

	/**
	 * Given a given geo level and an attribute theme, returns 
	 * a collection of map areas that are associated with a specific
	 * type of value.  For example, if the attribute theme were
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @param geoLevelAttribute - the name of an attribute eg: "area", "total_males", etc
	 * @return
	 * @throws RIFServiceException
	 */
	public ArrayList<MapAreaAttributeValue> getMapAreaAttributeValues(
		User user,
		Geography geography,
		GeoLevelSelect geoLevelSelect,
		String geoLevelAttribute) 
		throws RIFServiceException;
	
	/**
	 * Given a disease mapping study, this method returns 
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @param diseaseMappingStudy
	 * @return
	 */
	public ArrayList<GeoLevelAttributeSource> getGeoLevelAttributeSources(
		User user,
		StudyResultRetrievalContext studyResultRetrievalContext)
		throws RIFServiceException; 
	
	/**
	 * 
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @return
	 * @throws RIFServiceException
	 */
	public ArrayList<GeoLevelAttributeTheme> getGeoLevelAttributeThemes(
		User user,
		Geography geography,
		GeoLevelSelect geoLevelSelect)
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
		User user,
		StudyResultRetrievalContext studyResultRetrievalContext,
		GeoLevelAttributeSource geoLevelAttributeSource,
		GeoLevelAttributeTheme geoLevelAttributeTheme)
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
		User user,
		StudyResultRetrievalContext studyResultRetrievalContext,
		GeoLevelAttributeSource geoLevelAttributeSource,
		GeoLevelAttributeTheme geoLevelAttributeTheme) 
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
		User user,
		StudyResultRetrievalContext studyResultRetrievalContext,
		String[] calculatedResultColumnFieldNames,
		Integer startRowIndex,
		Integer endRowIndex)
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
	public RIFResultTable getExtractByBlock(
		User user,
		StudyResultRetrievalContext studyResultRetrievalContext,
		String[] calculatedResultColumnFieldNames,			
		Integer startRowIndex,
		Integer endRowIndex)
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
		User user,
		StudyResultRetrievalContext studyResultRetrievalContext,
		GeoLevelAttributeTheme geoLevelAttributeTheme,
		String geoLevelAttribute,
		ArrayList<MapArea> mapAreas,
		Integer year)
		throws RIFServiceException;

	
	
	public ArrayList<AgeGroup> getResultAgeGroups(
		User user,
		StudyResultRetrievalContext studyResultRetrievalContext,
		GeoLevelAttributeTheme geoLevelAttributeTheme,
		GeoLevelAttributeSource geoLevelSource,
		String geoLevalAttribute)
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
		User user,
		StudyResultRetrievalContext studyResultRetrievalContext,
		GeoLevelAttributeSource geoLevelSource,
		String geoLevelAttribute) 
		throws RIFServiceException;	
	
	public RIFResultTable getPyramidDataByYear(
		User user,
		StudyResultRetrievalContext studyResultRetrievalContext,
		GeoLevelAttributeSource geoLevelSource,
		String geoLevelAttribute,
		Integer year) 
		throws RIFServiceException;
		
	/**
	 * returns a table with these fields:
	 * eg:
	 * agegroup     | sex    popcount
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @param geoLevelSource
	 * @param geoLevelAttribute
	 * @param mapAreas
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getPyramidDataByMapAreas(
		User user,
		Geography geography,
		GeoLevelSelect geoLevelSelect,
		GeoLevelAttributeSource geoLevelSource,
		String geoLevelAttribute,
		ArrayList<MapArea> mapAreas) 
		throws RIFServiceException;
	
	public String[] getResultFieldsStratifiedByAgeGroup(
		User user,
		StudyResultRetrievalContext studyResultRetrievalContext,
		GeoLevelAttributeTheme geoLevelAttributeTheme,
		GeoLevelAttributeSource geoLevelAttributeSource)
		throws RIFServiceException;
	
	/**
	 * returns field with the following headers:
	 * GID, SMR, CL, CU
	 * @param user
	 * @param diseaseMappingStudy
	 * @return
	 */
	public RIFResultTable getSMRValues(
		User user,
		DiseaseMappingStudy diseaseMappingStudy)
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
		User user,
		DiseaseMappingStudy diseaseMappingStudy)
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
		User user,
		DiseaseMappingStudy diseaseMappingStudy)
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
	public RIFResultTable getResultStudyGeneralInfo(
		User user,
		DiseaseMappingStudy diseaseMappingStudy)
		throws RIFServiceException;
		
	public void initialise(Object startupParameter)
		throws RIFServiceException;
	
	public boolean isInitialised();
}
