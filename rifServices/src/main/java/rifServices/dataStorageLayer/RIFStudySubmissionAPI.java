package rifServices.dataStorageLayer;

import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.AgeGroup;
import rifServices.businessConceptLayer.AgeGroupSortingOption;
import rifServices.businessConceptLayer.CalculationMethod;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.MapArea;
import rifServices.businessConceptLayer.MapAreaSummaryData;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.Project;
import rifServices.businessConceptLayer.RIFOutputOption;
import rifServices.businessConceptLayer.RIFServiceInformation;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.Sex;
import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.YearRange;
import rifServices.system.RIFServiceException;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.io.File;



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

public interface RIFStudySubmissionAPI extends RIFStudyServiceAPI {
	
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
	
	
	public NumeratorDenominatorPair getNumeratorDenominatorPairFromNumeratorTable(
		final User user,
		final Geography geography,
		final String numeratorTableName) 
		throws RIFServiceException;
	
	//Features for Age, Sex and Study Years
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
	 * Gets the sexes.
	 *
	 * @param user the user
	 * @return the sexes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<Sex> getSexes(
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
		final String healthCodeTaxonomyNameSpace) 
		throws RIFServiceException;	
	
	/**
	 * Gets the top level codes.
	 *
	 * @param user the user
	 * @param healthCodeTaxonomy the health code taxonomy
	 * @return the top level codes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCode> getTopLevelHealthCodes(
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
	public ArrayList<HealthCode> getImmediateChildHealthCodes(
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
	public ArrayList<HealthCode> getHealthCodesMatchingSearchText(
		final User user,
		final HealthCodeTaxonomy healthCodeTaxonomy,
		final String searchText) 
		throws RIFServiceException;
	

	public HealthCode getHealthCode(
		final User user,
		final String healthCodeName,
		final String healthCodeNameSpace) 
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
	public ArrayList<MapArea> getMapAreasByBlock(
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
	 * Gets the image.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelArea the geo level area
	 * @param geoLevelView the geo level view
	 * @param mapAreas the map areas
	 * @return the image
	 * @throws RIFServiceException the RIF service exception
	 */
	public BufferedImage getImage(
		final User user,
		final Geography geography,
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
	

}
