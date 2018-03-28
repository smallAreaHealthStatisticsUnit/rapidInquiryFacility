package rifServices.dataStorageLayer.common;

import java.sql.Connection;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;

public interface RIFContextManager extends SQLManager {
	
	/**
	 * Gets the geographies.
	 *
	 * @param connection the connection
	 * @return the geographies
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<Geography> getGeographies(
			Connection connection)
		throws RIFServiceException;
	
	/**
	 * Gets the health themes.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @return the health themes
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<HealthTheme> getHealthThemes(
			Connection connection,
			Geography geography)
		throws RIFServiceException;
	
	/**
	 * A helper method used by services which are deployed within web resources.
	 * When users build up their queries using web-based forms, the forms obtain field values
	 * by making calls to the web services.  The URL is supposed to contain all the
	 * parameter values that are necessary to retrieve the correct information.
	 * The parameter values are strings, not complete Java objects.  The web resource needs
	 * a means of creating Java objects for the api of the RIFJobSubmissionService.  This
	 * method helps obtain a numerator denominator pair given the numerator table name
	 * @param connection
	 * @param geography
	 * @param numeratorTableName
	 * @return
	 * @throws RIFServiceException
	 */
	NumeratorDenominatorPair getNDPairFromNumeratorTableName(User user, Connection connection,
			Geography geography, String numeratorTableName) throws RIFServiceException;
	
	/**
	 * Gets the numerator denominator pairs.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param healthTheme the health theme
	 * @return the numerator denominator pairs
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<NumeratorDenominatorPair> getNumeratorDenominatorPairs(
			Connection connection,
			Geography geography,
			HealthTheme healthTheme,
			User user)
		throws RIFServiceException;
	
	/**
	 * Gets the geo level select values.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @return the geo level select values
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<GeoLevelSelect> getGeoLevelSelectValues(
			Connection connection,
			Geography geography)
		throws RIFServiceException;
	
	/**
	 * Gets the default geo level select value.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @return the default geo level select value
	 * @throws RIFServiceException the RIF service exception
	 */
	GeoLevelSelect getDefaultGeoLevelSelectValue(
			Connection connection,
			Geography geography)
		throws RIFServiceException;
	
	/**
	 * Gets the geo level area values.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @return the geo level area values
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<GeoLevelArea> getGeoLevelAreaValues(
			Connection connection,
			Geography geography,
			GeoLevelSelect geoLevelSelect)
		throws RIFServiceException;
	
	/**
	 * Gets the geo level view values.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @return the geo level view values
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<GeoLevelView> getGeoLevelViewValues(
			Connection connection,
			Geography geography,
			GeoLevelSelect geoLevelSelect)
		throws RIFServiceException;
	
	/**
	 * checks if geography exists.  If it doesn't it throws an exception.
	 *
	 * @param connection the connection
	 * @param geographyName the geography
	 * @throws RIFServiceException the RIF service exception
	 */
	void checkGeographyExists(
			Connection connection,
			String geographyName)
		throws RIFServiceException;
	
	/**
	 * Check non existent geo level select.
	 *
	 * @param connection the connection
	 * @param geographyName the geography
	 * @param geoLevelSelectName the geo level select
	 * @throws RIFServiceException the RIF service exception
	 */
	void checkGeoLevelSelectExists(
			Connection connection,
			String geographyName,
			String geoLevelSelectName)
		throws RIFServiceException;
	
	/**
	 * Check non existent geo level area.
	 *
	 * @param connection the connection
	 * @param geographyName the geography
	 * @param geoLevelSelectName the geo level select
	 * @param geoLevelAreaName the geo level area
	 * @throws RIFServiceException the RIF service exception
	 */
	void checkGeoLevelAreaExists(
			Connection connection,
			String geographyName,
			String geoLevelSelectName,
			String geoLevelAreaName)
		throws RIFServiceException;
	
	/**
	 * Check non existent geo level to map value.
	 *
	 * @param connection the connection
	 * @param geographyName the geography
	 * @param geoLevelSelectName the geo level select
	 * @param geoLevelValueName the geo level to map
	 * @throws RIFServiceException the RIF service exception
	 */
	void checkGeoLevelToMapOrViewValueExists(
			Connection connection,
			String geographyName,
			String geoLevelSelectName,
			String geoLevelValueName,
			boolean isToMapValue)
		throws RIFServiceException;
	
	/**
	 * Check non existent geo level to map value.
	 *
	 * @param connection the connection
	 * @param geographyName the geography
	 * @param geoLevelValueName the geo level select
	 * @param isToMapValue whether or not to map
	 * @throws RIFServiceException the RIF service exception
	 */
	void checkGeoLevelToMapOrViewValueExists(
			Connection connection,
			String geographyName,
			String geoLevelValueName,
			boolean isToMapValue)
		throws RIFServiceException;
	
	/**
	 * Check non existent health theme.
	 *
	 * @param connection the connection
	 * @param healthThemeDescription the health theme
	 * @throws RIFServiceException the RIF service exception
	 */
	void checkHealthThemeExists(
			Connection connection,
			String healthThemeDescription)
		throws RIFServiceException;
	
	/**
	 * Check non existent nd pair.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param ndPair the nd pair
	 * @throws RIFServiceException the RIF service exception
	 */
	void checkNDPairExists(User user, Connection connection, Geography geography,
			NumeratorDenominatorPair ndPair) throws RIFServiceException;
	
	/**
	 * Check non existent nd pair.
	 *
	 * @param user the user
	 * @param connection the connection
	 * @param geography the geography
	 * @param numeratorTableName the numerator table
	 * @throws RIFServiceException the RIF service exception
	 */
	void checkNumeratorTableExists(User user, Connection connection, Geography geography,
			String numeratorTableName) throws RIFServiceException;
}
