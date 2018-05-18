package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.datastorage.common.ServiceResources;

/**
 * Defines methods that are common to both study submission and study result
 * retrieval APIs.
 * 
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 */

public interface RIFStudyServiceAPI {

	boolean isInformationGovernancePolicyActive(
					final User user)
		throws RIFServiceException;

	DiseaseMappingStudy getDiseaseMappingStudy(
					final User user,
					final String studyID)
		throws RIFServiceException;
		
	/**
	 * Gets the geographies.
	 *
	 * @param user the user
	 * @return the geographies
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<Geography> getGeographies(
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
	ArrayList<GeoLevelSelect> getGeoLevelSelectValues(
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
	GeoLevelSelect getDefaultGeoLevelSelectValue(
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
	ArrayList<GeoLevelArea> getGeoLevelAreaValues(
					final User user,
					final Geography geography,
					final GeoLevelSelect geoLevelSelect)
		throws RIFServiceException;
	
	/**
	 * Gets the geo level view values.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @return the geo level view values
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<GeoLevelView> getGeoLevelViewValues(
					final User user,
					final Geography geography,
					final GeoLevelSelect geoLevelSelect)
		throws RIFServiceException;	
	
	void initialise(final ServiceResources startupParameter);
}
