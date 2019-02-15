package org.sahsu.rif.services.concepts;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.datastorage.common.ServiceResources;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public interface RIFStudySubmissionAPI extends RIFStudyServiceAPI {

	String getMapBackground(
			User _user,
			Geography _geography)
					throws RIFServiceException;

	String getSelectState(
			User _user,
			String studyID)
					throws RIFServiceException;

	String getPrintState(
			User _user,
			String studyID)
					throws RIFServiceException;

	String setPrintState(
			User _user,
			String studyID,
			String printStateText)
					throws RIFServiceException;

	String getPostalCodes(
			User _user,
			Geography _geography,
			String postcode,
			Locale locale)
					throws RIFServiceException;

	String getPostalCodeCapabilities(
			User _user,
			Geography _geography)
								throws RIFServiceException;

	RIFResultTable getTileMakerCentroids(
			User _user,
			Geography _geography,
			GeoLevelSelect _geoLevelSelect)
								throws RIFServiceException;

	String getTileMakerTiles(
			User _user,
			Geography _geography,
			GeoLevelSelect _geoLevelSelect,
			Integer zoomlevel,
			Integer x,
			Integer y)
								throws RIFServiceException;

	String getTileMakerAttributes(
			User _user,
			Geography _geography,
			GeoLevelSelect _geoLevelSelect)
								throws RIFServiceException;

	void initialise(ServiceResources startupParameter);

	void setServiceName(String serviceName);

	void logException(
			User user,
			String methodName,
			RIFServiceException rifServiceException)
										throws RIFServiceException;

	abstract RIFServiceInformation getRIFServiceInformation(
			User _user)
											throws RIFServiceException;
	
	/**
	 * Gets the available calculation methods.
	 *
	 * @param user the user
	 * @return the available calculation methods
	 * @throws RIFServiceException the RIF service exception
	 */
	List<CalculationMethod> getAvailableCalculationMethods(
		final User user) 
		throws RIFServiceException;

	boolean isInformationGovernancePolicyActive(
			User _user)
			throws RIFServiceException;

	DiseaseMappingStudy getDiseaseMappingStudy(
			User _user,
			String studyID)
			throws RIFServiceException;

	ArrayList<Geography> getGeographies(
			User _user)
				throws RIFServiceException;

	ArrayList<GeoLevelSelect> getGeoLevelSelectValues(
			User _user,
			Geography _geography)
					throws RIFServiceException;

	GeoLevelSelect getDefaultGeoLevelSelectValue(
			User _user,
			Geography _geography)
						throws RIFServiceException;

	ArrayList<GeoLevelArea> getGeoLevelAreaValues(
			User _user,
			Geography _geography,
			GeoLevelSelect _geoLevelSelect)
		throws RIFServiceException;

	ArrayList<GeoLevelView> getGeoLevelViewValues(
			User _user,
			Geography _geography,
			GeoLevelSelect _geoLevelSelect)
			throws RIFServiceException;

	abstract List<HealthTheme> getHealthThemes(
			User _user,
			Geography _geography)
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
	List<NumeratorDenominatorPair> getNumeratorDenominatorPairs(
		final User user,
		final Geography geography,
		final HealthTheme healthTheme) 
		throws RIFServiceException;
	
	
	NumeratorDenominatorPair getNumeratorDenominatorPairFromNumeratorTable(
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
	List<AgeGroup> getAgeGroups(
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
	List<Sex> getSexes(
		final User user)
		throws RIFServiceException;
		
	/**
	 * Gets the covariates.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param geoLevelToMap the geo level to map
	 * @return the covariates
	 * @throws RIFServiceException the RIF service exception
	 */
	List<AbstractCovariate> getCovariates(
		final User user,
		final Geography geography,
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
	YearRange getYearRange(
		final User user,
		final Geography geography,
		final NumeratorDenominatorPair ndPair) 
		throws RIFServiceException;
			
	/**
	 * Gets the projects.
	 *
	 * @param user the user
	 * @return the projects
	 * @throws RIFServiceException the RIF service exception
	 */
	List<Project> getProjects(
		final User user) 
		throws RIFServiceException;
	

	/**
	 * Submit study.
	 *
	 * @param user the user
	 * @param rifStudySubmission the rif job submission
	 * @param outputFile the output file
	 * @param url the URL of the originating request
	 * @return the studyID for the study that has just been submitted
	 * @throws RIFServiceException the RIF service exception
	 */
	String submitStudy(final User user, final RIFStudySubmission rifStudySubmission,
			final File outputFile, final String url) throws RIFServiceException;
	
	void createStudyExtract(
		final User user,
		final String studyID,
		final String zoomLevel,
		final Locale locale,
		final String url)
		throws RIFServiceException;	

	FileInputStream getStudyExtract(
		final User user,
		final String studyID,
		final String zoomLevel)
		throws RIFServiceException;		

	String getStudyExtractFIleName(
		final User user,
		final String studyID)
		throws RIFServiceException;	

	String getExtractStatus(
		final User user,
		final String studyID)
		throws RIFServiceException;

	String getJsonFile(
		final User user,
		final String studyID,
		final Locale locale,
		final String url)
		throws RIFServiceException;

	String getFrontEndParameters(
		final User user);
	
	void test(final User user, final String url) throws RIFServiceException;

	RIFServiceStartupOptions getRIFServiceStartupOptions();
}
