package org.sahsu.rif.services.datastorage.common;

import java.util.ArrayList;
import java.util.Locale;

import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AbstractCovariate;
import org.sahsu.rif.services.concepts.DiseaseMappingStudy;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.GeoLevelView;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.HealthTheme;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.concepts.RIFServiceInformation;
import org.sahsu.rif.services.concepts.YearRange;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

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

	String getMapBackground(
			User _user,
			Geography _geography)
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
			Integer y,
			String tileType)
								throws RIFServiceException;
									
	String getTileMakerAttributes(
			User _user,
			Geography _geography,
			GeoLevelSelect _geoLevelSelect)
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
