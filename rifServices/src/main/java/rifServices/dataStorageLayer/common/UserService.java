package rifServices.dataStorageLayer.common;

import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.RIFServiceInformation;
import rifServices.businessConceptLayer.YearRange;
import rifServices.system.RIFServiceStartupOptions;

public interface UserService {
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

	YearRange getYearRange(
			User _user,
			Geography _geography,
			NumeratorDenominatorPair _ndPair)
				throws RIFServiceException;

	ArrayList<HealthTheme> getHealthThemes(
			User _user,
			Geography _geography)
					throws RIFServiceException;

	ArrayList<AbstractCovariate> getCovariates(
			User _user,
			Geography _geography,
			GeoLevelToMap _geoLevelToMap)
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

	void initialise(ServiceResources startupParameter);

	void logException(
			User user,
			String methodName,
			RIFServiceException rifServiceException)
										throws RIFServiceException;

	RIFServiceInformation getRIFServiceInformation(
			User _user)
											throws RIFServiceException;

	RIFServiceStartupOptions getRIFServiceStartupOptions();
}
