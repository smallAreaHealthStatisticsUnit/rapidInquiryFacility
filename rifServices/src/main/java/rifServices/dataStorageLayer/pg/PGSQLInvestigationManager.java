package rifServices.dataStorageLayer.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLRecordExistsQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.AbstractStudy;
import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.dataStorageLayer.common.AbstractSQLManager;
import rifServices.dataStorageLayer.common.AgeGenderYearManager;
import rifServices.dataStorageLayer.common.CovariateManager;
import rifServices.dataStorageLayer.common.InvestigationManager;
import rifServices.dataStorageLayer.common.RIFContextManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

final class PGSQLInvestigationManager extends AbstractSQLManager implements InvestigationManager {

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
		//ArrayList<HealthCode> healthCodes
		//	= investigation.getHealthCodes();
		//healthOutcomeManager.checkNonExistentHealthCodes(healthCodes);

		
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
	
	@Override
	public void checkInvestigationExists(
			final Connection connection,
			final AbstractStudy study,
			final Investigation investigation)
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLRecordExistsQueryFormatter queryFormatter
				= new PGSQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.setFromTable("rif40_investigations");
			queryFormatter.addWhereParameter("study_id");
			queryFormatter.addWhereParameter("inv_id");

			logSQLQuery(
				"checkInvestigationExists",
				queryFormatter,
				study.getIdentifier(),
				investigation.getIdentifier());
				
			statement 
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.setString(1, study.getIdentifier());
			statement.setString(2, investigation.getIdentifier());
			resultSet = statement.executeQuery();
			RIFServiceException rifServiceException = null;
			if (resultSet.next() == false) {
				String recordType
					= investigation.getRecordType();
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						investigation.getDisplayName());

				rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_AGE_GROUP, 
						errorMessage);
				
				connection.commit();
				
				throw rifServiceException;
			}

			connection.commit();
			
		}
		catch(SQLException sqlException) {			
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			SQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					investigation.getRecordType(),
					investigation.getDisplayName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLInvestigationManager.class, 
				errorMessage, 
				sqlException);										
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
