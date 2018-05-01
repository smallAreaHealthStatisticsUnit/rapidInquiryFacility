package rifServices.dataStorageLayer.pg;

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
import rifServices.dataStorageLayer.common.BaseSQLManager;
import rifServices.dataStorageLayer.common.AgeGenderYearManager;
import rifServices.dataStorageLayer.common.CovariateManager;
import rifServices.dataStorageLayer.common.InvestigationManager;
import rifServices.dataStorageLayer.common.RIFContextManager;
import rifServices.system.RIFServiceStartupOptions;

public final class PGSQLInvestigationManager extends BaseSQLManager
		implements InvestigationManager {

	private RIFContextManager rifContextManager;
	private AgeGenderYearManager ageGenderYearManager;
	private CovariateManager covariateManager;
	
	/**
	 * Instantiates a new SQL investigation manager.
	 */
	public PGSQLInvestigationManager(
			final RIFServiceStartupOptions startupOptions,
			final RIFContextManager rifContextManager,
			final AgeGenderYearManager ageGenderYearManager,
			final CovariateManager covariateManager) {

		super(startupOptions);
		this.rifContextManager = rifContextManager;
		this.ageGenderYearManager = ageGenderYearManager;
		this.covariateManager = covariateManager;
	}

	@Override
	public void checkNonExistentItems(
			final User user,
			final Connection connection,
			final Geography geography,
			final GeoLevelToMap geoLevelToMap,
			final Investigation investigation)
		throws RIFServiceException {
		

		ArrayList<AbstractCovariate> covariates
			= investigation.getCovariates();
		covariateManager.checkNonExistentCovariates(
			connection, 
			geography,
			geoLevelToMap,
			covariates);

		//we will not check whether the health codes exist
		//for now, we'll assume they do

		HealthTheme healthTheme
			= investigation.getHealthTheme();
		rifContextManager.checkHealthThemeExists(
			connection, 
			healthTheme.getDescription());

		NumeratorDenominatorPair ndPair = investigation.getNdPair();
		rifContextManager.checkNDPairExists(User.NULL_USER,
			connection, 
			geography, 
			ndPair);
		
		ArrayList<AgeBand> ageBands
			= investigation.getAgeBands();
		ageGenderYearManager.checkNonExistentAgeGroups(
			connection, 
			ndPair,
			ageBands);
		
		
	}

}
