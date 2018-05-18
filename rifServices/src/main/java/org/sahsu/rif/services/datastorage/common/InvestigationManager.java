package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.util.ArrayList;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AbstractCovariate;
import org.sahsu.rif.services.concepts.AgeBand;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.HealthTheme;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

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
