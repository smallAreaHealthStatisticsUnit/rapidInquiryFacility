package rifServices.businessConceptLayer;

import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;

/**
 * Defines methods that are common to both study submission and study result
 * retrieval APIs.
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

public interface RIFStudyServiceAPI {
	
	public boolean isInformationGovernancePolicyActive(
		final User user) 
		throws RIFServiceException;

	public DiseaseMappingStudy getDiseaseMappingStudy(
		final User user,
		final String studyID)
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
	public ArrayList<GeoLevelSelect> getGeoLevelSelectValues(
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
	
	public void initialise(final Object startupParameter)
		throws RIFServiceException;
		
	public boolean isInitialised();	
	
}
