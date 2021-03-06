package org.sahsu.rif.services.concepts;

import java.util.ArrayList;
import java.util.Locale;

import org.sahsu.rif.generic.concepts.RIFResultTable;
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

	void logException(
			User user,
			String methodName,
			RIFServiceException rifServiceException)
										throws RIFServiceException;

	String getTileMakerTiles(
			User _user,
			Geography _geography,
			GeoLevelSelect _geoLevelSelect,
			Integer zoomlevel,
			Integer x,
			Integer y,
			String tileType)
		throws RIFServiceException;

	String getTileMakerAttributes(
			User _user,
			Geography _geography,
			GeoLevelSelect _geoLevelSelect)
								throws RIFServiceException;

	void setServiceName(String serviceName);

	RIFResultTable getTileMakerCentroids(
			User _user,
			Geography _geography,
			GeoLevelSelect _geoLevelSelect)
								throws RIFServiceException;

	String getPostalCodeCapabilities(
			User _user,
			Geography _geography)
								throws RIFServiceException;

	String getPostalCodes(
			User _user,
			Geography _geography,
			String postcode,
			Locale locale)
					throws RIFServiceException;

	void setPrintState(
			User _user,
			String studyID,
			String printStateText)
					throws RIFServiceException;

	String getPrintState(
			User _user,
			String studyID)
					throws RIFServiceException;

	String getHomogeneity(
			User _user,
			String studyID)
			throws RIFServiceException;

	String getCovariateLossReport(
			User _user,
			String studyID)
			throws RIFServiceException;

	String getRiskGraph(
			User _user,
			String studyID)
			throws RIFServiceException;

	String getSelectState(
			User _user,
			String studyID)
					throws RIFServiceException;

	String getMapBackground(
			User _user,
			Geography _geography)
					throws RIFServiceException;

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

	void validateUser(User user) throws RIFServiceException;

	void initialise(final ServiceResources startupParameter);

	RIFServiceInformation getRIFServiceInformation(User user) throws RIFServiceException;
}
