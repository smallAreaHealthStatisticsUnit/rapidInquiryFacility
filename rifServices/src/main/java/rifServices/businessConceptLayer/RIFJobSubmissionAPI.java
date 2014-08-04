package rifServices.businessConceptLayer;

import rifServices.system.RIFServiceException;


import rifServices.system.RIFServiceStartupOptions;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.io.File;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;


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

public interface RIFJobSubmissionAPI {
	
	
	/**
	 * The Enum AgeGroupSortingOption.
	 */
	public enum AgeGroupSortingOption {
		
		/** The ascending lower limit. */
		ASCENDING_LOWER_LIMIT,
		
		/** The descending lower limit. */
		DESCENDING_LOWER_LIMIT,
		
		/** The ascending upper limit. */
		ASCENDING_UPPER_LIMIT,
		
		/** The descending upper limit. */
		DESCENDING_UPPER_LIMIT
	};

	
	public void initialiseService();
	
	public void initialiseService(RIFServiceStartupOptions rifServiceStartupOptions);
	
	/**
	 * Login.
	 *
	 * @param userID the user id
	 * @param password the password
	 * @throws RIFServiceException the RIF service exception
	 */
	public void login(
		final String userID, 
		final char[] password) 
		throws RIFServiceException;
	
	/**
	 * Logout.
	 *
	 * @param user the user
	 * @throws RIFServiceException the RIF service exception
	 */
	public void logout(
		final User user) 
		throws RIFServiceException;
	
	/**
	 * Gets the RIF service information.
	 *
	 * @param user the user
	 * @return the RIF service information
	 * @throws RIFServiceException the RIF service exception
	 */
	public RIFServiceInformation getRIFServiceInformation(
		final User user) 
		throws RIFServiceException;
	
	/**
	 * Gets the available rif output options.
	 *
	 * @param user the user
	 * @return the available rif output options
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<RIFOutputOption> getAvailableRIFOutputOptions(
		final User user)
		throws RIFServiceException;
	
	/**
	 * Gets the available calculation methods.
	 *
	 * @param user the user
	 * @return the available calculation methods
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<CalculationMethod> getAvailableCalculationMethods(
		final User user) 
		throws RIFServiceException;
	
	/**
	 * Gets the disease mapping studies.
	 *
	 * @param user the user
	 * @return the disease mapping studies
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<DiseaseMappingStudy> getDiseaseMappingStudies(
		final User user) 
		throws RIFServiceException;
		
	//Features for RIF Context
	/**
	 * Gets the geographies.
	 *
	 * @param user the user
	 * @return the geographies
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<Geography> getGeographies(
		final User user)
		throws RIFServiceException;	
	
	/**
	 * Gets the geographical level select values.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @return the geographical level select values
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<GeoLevelSelect> getGeographicalLevelSelectValues(
		final User user,
		final Geography geography)
		throws RIFServiceException;
	
	/**
	 * Gets the default geo level select value.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @return the default geo level select value
	 * @throws RIFServiceException the RIF service exception
	 */
	public GeoLevelSelect getDefaultGeoLevelSelectValue(
		final User user,
		final Geography geography) 
		throws RIFServiceException;

	/**
	 * Gets the geo level area values.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @return the geo level area values
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<GeoLevelArea> getGeoLevelAreaValues(
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect) 
		throws RIFServiceException;
	
	/**
	 * Gets the geo level view values.
	 *
	 * @param _user the _user
	 * @param _geography the _geography
	 * @param _geoLevelSelect the _geo level select
	 * @return the geo level view values
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<GeoLevelView> getGeoLevelViewValues(
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect) 
		throws RIFServiceException;	
	
	/**
	 * Gets the geo level to map values.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @return the geo level to map values
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<GeoLevelToMap> getGeoLevelToMapValues(
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect) 
		throws RIFServiceException;
	
	/**
	 * Gets the health themes.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @return the health themes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthTheme> getHealthThemes(
		final User user,
		final Geography geography)
		throws RIFServiceException;	
	
	/**
	 * Gets the numerator denominator pairs.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param healthTheme the health theme
	 * @return the numerator denominator pairs
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<NumeratorDenominatorPair> getNumeratorDenominatorPairs(
		final User user,
		final Geography geography,
		final HealthTheme healthTheme) 
		throws RIFServiceException;
	
	//Features for Age, Gender and Study Years
	/**
	 * Gets the age groups.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param ndPair the nd pair
	 * @param sortingOrder the sorting order
	 * @return the age groups
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<AgeGroup> getAgeGroups(
		final User user,
		final Geography geography,
		final NumeratorDenominatorPair ndPair,
		final AgeGroupSortingOption sortingOrder) 
		throws RIFServiceException;
	
	/**
	 * Gets the genders.
	 *
	 * @param user the user
	 * @return the genders
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<Sex> getGenders(
		final User user)
		throws RIFServiceException;
		
	//Features for Health Codes
	
	/**
	 * Gets the health code taxonomies.
	 *
	 * @param user the user
	 * @return the health code taxonomies
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCodeTaxonomy> getHealthCodeTaxonomies(
		final User user)
		throws RIFServiceException;

	
	/**
	 * Gets the health code taxonomy given the name space
	 *
	 * @param user the user
	 * @return the health code taxonomy corresponding to the name space
	 * @throws RIFServiceException the RIF service exception
	 */
	public HealthCodeTaxonomy getHealthCodeTaxonomyFromNameSpace(
		final User user, 
		final String healthCodeTaxonomyNameSpace) throws RIFServiceException;	
	
	/**
	 * Gets the top level codes.
	 *
	 * @param user the user
	 * @param healthCodeTaxonomy the health code taxonomy
	 * @return the top level codes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCode> getTopLevelCodes(
		final User user,
		final HealthCodeTaxonomy healthCodeTaxonomy) 
		throws RIFServiceException;	
	
	/**
	 * Gets the immediate subterms.
	 *
	 * @param user the user
	 * @param parentHealthCode the parent health code
	 * @return the immediate subterms
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCode> getImmediateSubterms(
		final User user,
		final HealthCode parentHealthCode) 
		throws RIFServiceException;	
	
	/**
	 * Gets the parent health code.
	 *
	 * @param user the user
	 * @param childHealthCode the child health code
	 * @return the parent health code
	 * @throws RIFServiceException the RIF service exception
	 */
	public HealthCode getParentHealthCode(
		final User user,
		final HealthCode childHealthCode) 
		throws RIFServiceException;	
	
	/**
	 * Gets the health codes.
	 *
	 * @param user the user
	 * @param healthCodeTaxonomy the health code taxonomy
	 * @param searchText the search text
	 * @return the health codes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCode> getHealthCodes(
		final User user,
		final HealthCodeTaxonomy healthCodeTaxonomy,
		final String searchText) 
		throws RIFServiceException;
	
	//Features for Covariates
	/**
	 * Gets the covariates.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelToMap the geo level to map
	 * @return the covariates
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<AbstractCovariate> getCovariates(
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelToMap geoLevelToMap)
		throws RIFServiceException;
	
	/**
	 * Gets the year range.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param ndPair the nd pair
	 * @return the year range
	 * @throws RIFServiceException the RIF service exception
	 */
	public YearRange getYearRange(
		final User user,
		final Geography geography,
		final NumeratorDenominatorPair ndPair) 
		throws RIFServiceException;
	
	/**
	 * Gets the map areas.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelArea the geo level area	
	 * @param geoLevelToMap the geo level to map
	 * @return the map areas
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<MapArea> getMapAreas(
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelArea geoLevelArea,
		final GeoLevelToMap geoLevelToMap) 
		throws RIFServiceException;	
	
	/**
	 * Gets the map areas.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelArea the geo level area
	 * @param geoLevelToMap the geo level to map
	 * @param startIndex the start index
	 * @param endIndex the end index
	 * @return the map areas
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<MapArea> getMapAreas(
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelArea geoLevelArea,
		final GeoLevelToMap geoLevelToMap,
		final Integer startIndex,
		final Integer endIndex) 
		throws RIFServiceException;

	/**
	 * Gets the map area summary information.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelArea the geo level area
	 * @param geoLevelToMap the geo level to map
	 * @param mapAreas the map areas
	 * @return the map area summary information
	 * @throws RIFServiceException the RIF service exception
	 */
	public MapAreaSummaryData getMapAreaSummaryInformation(
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelArea geoLevelArea,
		final GeoLevelToMap geoLevelToMap,
		final ArrayList<MapArea> mapAreas) 
		throws RIFServiceException;

	
	/**
	 * Gets the summary data for current extent.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelArea the geo level area
	 * @param geoLevelToMap the geo level to map
	 * @return the summary data for current extent
	 * @throws RIFServiceException the RIF service exception
	 */
	public MapAreaSummaryData getSummaryDataForCurrentExtent(
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelArea geoLevelArea,
		final GeoLevelToMap geoLevelToMap) 
		throws RIFServiceException;
	
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
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelToMap geoLevelToMap,
		final ArrayList<MapArea> mapAreas) 
		throws RIFServiceException;
	
	
	/**
	 * Gets the projects.
	 *
	 * @param user the user
	 * @return the projects
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<Project> getProjects(
		final User user) 
		throws RIFServiceException;
	
	/**
	 * Gets the studies.
	 *
	 * @param user the user
	 * @param project the project
	 * @return the studies
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<AbstractStudy> getStudies(
		final User user,
		final Project project) 
		throws RIFServiceException;
	
	/**
	 * Gets the image.
	 *
	 * @param user the user
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelArea the geo level area
	 * @param geoLevelView the geo level view
	 * @param mapAreas the map areas
	 * @return the image
	 * @throws RIFServiceException the RIF service exception
	 */
	public BufferedImage getImage(
		final User user,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelArea geoLevelArea,
		final GeoLevelView geoLevelView,
		final ArrayList<MapArea> mapAreas)
		throws RIFServiceException;
	
	/**
	 * Submit study.
	 *
	 * @param user the user
	 * @param rifJobSubmission the rif job submission
	 * @param outputFile the output file
	 * @throws RIFServiceException the RIF service exception
	 */
	public void submitStudy(
		final User user,
		final RIFStudySubmission rifJobSubmission,
		final File outputFile) 
		throws RIFServiceException;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//Calls to obtain geographical extents of areas
	
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
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final MapArea mapArea)
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
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final DiseaseMappingStudy study) 
		throws RIFServiceException;	
	
	
	
	public Rectangle2D.Double getGeoLevelFullExtent(
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
	public String getTiles(
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final Integer zoomFactor,
		final String tileIdentifier) 
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
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final String geoLevelAttribute) 
		throws RIFServiceException;
	
	
	public ArrayList<GeoLevelAttributeTheme> getGeoLevelAttributeThemes(
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect)
		throws RIFServiceException;

	
	/**
	 * There are multiple data attributes that may be associated at a given 
	 * geo level (eg: "region", "district").  There may be many attributes so they 
	 * have been organised into various themes (eg: health, geometry)
	 * 
	 * @param user
	 * @param geoLevelSelect
	 * @param geoLevelAttributeTheme
	 * @return list of data attributes that are available for the 
	 * @throws RIFServiceException
	 */
	public String[] getAllAttributesForGeoLevelAttributeTheme(
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelAttributeTheme geoLevelAttributeTheme)
		throws RIFServiceException;
		
	public String[] getNumericAttributesForGeoLevelAttributeTheme(
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
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
		User user,
		DiseaseMappingStudy diseaseMappingStudy,
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
		DiseaseMappingStudy diseaseMappingStudy,
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
		Geography geography,
		GeoLevelSelect geoLevelSelect,
		DiseaseMappingStudy diseaseMappingStudy,
		GeoLevelAttributeTheme geoLevelAttributeTheme,
		String geoLevelAttribute,
		ArrayList<MapArea> mapAreas,
		Integer year)
		throws RIFServiceException;
}
