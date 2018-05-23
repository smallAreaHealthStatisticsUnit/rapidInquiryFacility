package rifServices.dataStorageLayer.common;

import java.sql.Connection;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.system.RIFServiceStartupOptions;

public class InvestigationManager extends BaseSQLManager {

	private RIFContextManager rifContextManager;
	private AgeGenderYearManager ageGenderYearManager;
	private CovariateManager covariateManager;

	InvestigationManager(
			final RIFServiceStartupOptions startupOptions,
			final RIFContextManager rifContextManager,
			final AgeGenderYearManager ageGenderYearManager,
			final CovariateManager covariateManager) {

		super(startupOptions);
		this.rifContextManager = rifContextManager;
		this.ageGenderYearManager = ageGenderYearManager;
		this.covariateManager = covariateManager;
	}

	void checkNonExistentItems(User user, Connection connection, Geography geography,
			GeoLevelToMap geoLevelToMap, Investigation investigation)
			throws RIFServiceException {

		ArrayList<AbstractCovariate> covariates = investigation.getCovariates();
		covariateManager.checkNonExistentCovariates(connection, geography, geoLevelToMap,
		                                            covariates);

		HealthTheme healthTheme = investigation.getHealthTheme();
		rifContextManager.checkHealthThemeExists(connection, healthTheme.getDescription());

		NumeratorDenominatorPair ndPair = investigation.getNdPair();
		rifContextManager.checkNDPairExists(user, connection, geography, ndPair);

		ArrayList<AgeBand> ageBands = investigation.getAgeBands();
		ageGenderYearManager.checkNonExistentAgeGroups(connection, ndPair, ageBands);
	}
}
