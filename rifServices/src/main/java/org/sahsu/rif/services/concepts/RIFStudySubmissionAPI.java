package org.sahsu.rif.services.concepts;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.datastorage.common.ServiceResources;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

import org.json.JSONObject;

public interface RIFStudySubmissionAPI extends RIFStudyServiceAPI {

	void initialise(ServiceResources startupParameter);

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

	List<HealthTheme> getHealthThemes(
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


	/**
	 * Gets RIF40_NUM_DENOM as a JSONObject
	 *
	 * @param connection the connection
	 * @param user the user
	 * @return RIF40_NUM_DENOM as a JSONObject
	 * @throws RIFServiceException the RIF service exception
	 */
	public JSONObject getRif40NumDenom(
		final User user)
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
