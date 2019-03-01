package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.RIFSQLException;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.DiseaseMappingStudy;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelView;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.RIFServiceInformation;
import org.sahsu.rif.services.concepts.RIFStudyServiceAPI;
import org.sahsu.rif.services.system.RIFServiceError;

public class StudyService implements RIFStudyServiceAPI {

	protected static final RIFLogger rifLogger = RIFLogger.getLogger();
	protected static final Messages SERVICE_MESSAGES = Messages.serviceMessages();
	private String serviceName;
	private String serviceDescription;
	private String serviceContactEmail;
	ServiceResources rifServiceResources;

	@Override
	public void initialise(final ServiceResources startupParameter) {

		this.rifServiceResources = startupParameter;
	}

	@Override
	public boolean isInformationGovernancePolicyActive(final User _user)
		throws RIFServiceException {

		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return true;
		}

		try {

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"isInformationGovernancePolicyActive",
				"user",
				user);

			//Check for security violations
			validateUser(user);

			//This will change when the Information governance tool
			//is developed
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"isInformationGovernancePolicyActive",
				rifServiceException);
		}

		return false;

	}

	@Override
	public DiseaseMappingStudy getDiseaseMappingStudy(
			final User _user,
			final String studyID)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}

		Connection connection = null;
		DiseaseMappingStudy result = null;
		try {

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getDiseaseMappingStudy",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getDiseaseMappingStudy",
				"studyID",
				studyID);

			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getDiseaseMappingStudy",
				"studyID",
				studyID);

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getDiseaseMappingStudy",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SubmissionManager rifSubmissionManager
				= rifServiceResources.getRIFSubmissionManager();

			result
				= rifSubmissionManager.getDiseaseMappingStudy(
					connection,
					user,
					studyID);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getDiseaseMappingStudy",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user,
				connection);
		}
		return result;

	}

	@Override
	public ArrayList<Geography> getGeographies(
			final User _user)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}

		ArrayList<Geography> results = new ArrayList<>();
		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeographies",
				"user",
				user);

			//Check for security violations
			validateUser(user);

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getGeographies",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			RIFContextManager sqlRIFContextManager
				= rifServiceResources.getSQLRIFContextManager();
			results
				= sqlRIFContextManager.getGeographies(connection);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeographies",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user,
				connection);
		}

		return results;
	}

	@Override
	public ArrayList<GeoLevelSelect> getGeoLevelSelectValues(
			final User _user,
			final Geography _geography)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		Geography geography
			= Geography.createCopy(_geography);

		ArrayList<GeoLevelSelect> results = new ArrayList<>();
		Connection connection = null;
		try {
			//TOUR_VALIDATION
			/*
			 * Make sure that none of the required parameter values are null.
			 */

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeographicalLevelSelectValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeographicalLevelSelectValues",
				"geography",
				geography);

			//TOUR_VALIDATION
			/*
			 * Checks that the user is valid
			 */
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getGeographicalLevelSelectValues",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			RIFContextManager sqlRIFContextManager
				= rifServiceResources.getSQLRIFContextManager();
			results
				= sqlRIFContextManager.getGeoLevelSelectValues(
					connection,
					geography);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeographicalLevelSelectValues",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user,
				connection);
		}

		return results;
	}

	@Override
	public GeoLevelSelect getDefaultGeoLevelSelectValue(
			final User _user,
			final Geography _geography)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		Geography geography
			= Geography.createCopy(_geography);

		GeoLevelSelect result = GeoLevelSelect.newInstance();
		Connection connection = null;
		try {

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getDefaultGeoLevelSelectValue",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getDefaultGeoLevelSelectValue",
				"geography",
				geography);

			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getDefaultGeoLevelSelectValue",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			RIFContextManager sqlRIFContextManager
				= rifServiceResources.getSQLRIFContextManager();
			result
				= sqlRIFContextManager.getDefaultGeoLevelSelectValue(
					connection,
					geography);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getDefaultGeoLevelSelectValue",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user,
				connection);
		}

		return result;

	}

	//TOUR_CONCURRENCY
	/*
	 * We mark all method parameters as "final" to ensure that the code within the method
	 * cannot reassign the parameter within the code block.  It is used to prevent accidentally
	 * doing it as the code block is altered for maintenance.
	 */
	@Override
	public ArrayList<GeoLevelArea> getGeoLevelAreaValues(
			final User _user,
			final Geography _geography,
			final GeoLevelSelect _geoLevelSelect)
		throws RIFServiceException {

		//TOUR_CONCURRENCY
		/*
		 * The main task of this code block is to create complete local copies
		 * of method parameter values.  Doing this means that the
		 * code block is not vulnerable to multiple threads which may attempt to change
		 * the parameter objects provided by the client, as the code block is executing.
		 *
		 */
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		Geography geography
			= Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(_geoLevelSelect);

		ArrayList<GeoLevelArea> results = new ArrayList<>();
		Connection connection = null;
		try {

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelAreaValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelAreaValues",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelAreaValues",
				"geoLevelSelect",
				geoLevelSelect);

			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getGeoLevelAreaValues",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//TOUR_CONCURRENCY
			/*
			 * The pool of connections for each user will be a resource that could
			 * be a source of contention between threads.  However, mechanism for
			 * synchronising access does not have to be exposed in this class.  Instead,
			 * it can be developed and improved within the implementation of
			 * the SQLConnectionManager class's methods that include:
			 * <ul>
			 * <li>assignPooledReadConnection</li>
			 * <li>reclaimPooledReadConnection</li>
			 * <li>assignPooledWriteConnection</li>
			 * <li>reclaimPooledWriteConnection</li>
			 * </ul>
			 *
			 *
			 * <p>
			 * Also note that because we are only reading geo level area values,
			 * we can use the more constraining read-only connection.  Using
			 * the read-only connection may help improve performance. It also
			 * acts as a safety check if developers later alter the database query code
			 * in a way that suddenly causes the code to write values.
			 */
			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			RIFContextManager sqlRIFContextManager
				= rifServiceResources.getSQLRIFContextManager();
			results
				= sqlRIFContextManager.getGeoLevelAreaValues(
					connection,
					geography,
					geoLevelSelect);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeoLevelAreaValues",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user,
				connection);
		}

		return results;
	}

	@Override
	public ArrayList<GeoLevelView> getGeoLevelViewValues(
			final User _user,
			final Geography _geography,
			final GeoLevelSelect _geoLevelSelect)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		Geography geography
			= Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(_geoLevelSelect);

		ArrayList<GeoLevelView> results = new ArrayList<>();
		Connection connection = null;
		try {

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelViewValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelViewValues",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelViewValues",
				"geoLevelSelect",
				geoLevelSelect);

			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getGeoLevelViewValues",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			RIFContextManager sqlRIFContextManager
				= rifServiceResources.getSQLRIFContextManager();
			results
				= sqlRIFContextManager.getGeoLevelViewValues(
					connection,
					geography,
					geoLevelSelect);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeoLevelViewValues",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user,
				connection);
		}

		return results;
	}

	/**
	 * Validate user.
	 *
	 * @param user the user
	 * @throws RIFServiceException the RIF service exception
	 */
	@Override
	public void validateUser(final User user) throws RIFServiceException {

		new ValidateUser(user, rifServiceResources.getSqlConnectionManager()).validate();
	}

	@Override
	public void logException(
			final User user,
			final String methodName,
			final RIFServiceException rifServiceException)
		throws RIFServiceException {

		new ExceptionLog(user, methodName, rifServiceException, rifServiceResources, rifLogger).log();
	}

	@Override
	public void setServiceName(final String serviceName) {
		this.serviceName = serviceName;
	}

	void setServiceDescription(final String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	void setServiceContactEmail(final String serviceContactEmail) {
		this.serviceContactEmail = serviceContactEmail;
	}

	@Override
	public RIFResultTable getTileMakerCentroids(
			final User _user,
			final Geography _geography,
			final GeoLevelSelect _geoLevelSelect)
			throws RIFServiceException {

			//Defensively copy parameters and guard against blocked users
			User user = User.createCopy(_user);
			SQLManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
			if (sqlConnectionManager.isUserBlocked(user)) {
				return null;
			}
			Geography geography
				= Geography.createCopy(_geography);
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.createCopy(_geoLevelSelect);

			RIFResultTable result = new RIFResultTable();
			Connection connection = null;
			try {
				//Check for empty parameters
				FieldValidationUtility fieldValidationUtility
					= new FieldValidationUtility();
				fieldValidationUtility.checkNullMethodParameter(
					"getTileMakerCentroids",
					"user",
					user);
				fieldValidationUtility.checkNullMethodParameter(
					"getTileMakerCentroids",
					"geography",
					geography);
				fieldValidationUtility.checkNullMethodParameter(
					"getTileMakerCentroids",
					"getLevelSelect",
					geoLevelSelect);

				//Check for security violations
				validateUser(user);
				geography.checkSecurityViolations();
				geoLevelSelect.checkSecurityViolations();

				//rifLogger.info(this.getClass(), geography.getDisplayName());

				//Audit attempt to do operation
				String auditTrailMessage
					= SERVICE_MESSAGES.getMessage("logging.getTileMakerCentroids",
						user.getUserID(),
						user.getIPAddress(),
						geography.getDisplayName(),
						geoLevelSelect.getDisplayName());
				rifLogger.info(
					getClass(),
					auditTrailMessage);

				//Assign pooled connection
				connection
					= sqlConnectionManager.assignPooledReadConnection(user);

				//Delegate operation to a specialised manager class
				ResultsQueryManager sqlResultsQueryManager
					= rifServiceResources.getSqlResultsQueryManager();
				result
					= sqlResultsQueryManager.getTileMakerCentroids(
						connection,
						geography,
						geoLevelSelect);
			}
			catch(RIFServiceException rifServiceException) {
				//Audit failure of operation
				logException(
					user,
					"getTileMakerCentroids",
					rifServiceException);
			}
			finally {
				//Reclaim pooled connection
				sqlConnectionManager.reclaimPooledReadConnection(
					user,
					connection);
			}

			return result;
		}

		@Override
		public String getMapBackground(
			final User _user,
			final Geography _geography)
					throws RIFServiceException{

			//Defensively copy parameters and guard against blocked users
			User user = User.createCopy(_user);
			SQLManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
			if (sqlConnectionManager.isUserBlocked(user)) {
				return null;
			}
			Geography geography
				= Geography.createCopy(_geography);
			String result="{}";

			Connection connection = null;
			try {
				//Check for empty parameters
				FieldValidationUtility fieldValidationUtility
					= new FieldValidationUtility();
				fieldValidationUtility.checkNullMethodParameter(
					"getMapBackground",
					"user",
					user);
				fieldValidationUtility.checkNullMethodParameter(
					"getMapBackground",
					"geography",
					geography);

				//Check for security violations
				validateUser(user);
				geography.checkSecurityViolations();

				//rifLogger.info(this.getClass(), geography.getDisplayName());

				//Audit attempt to do operation
				String auditTrailMessage
					= SERVICE_MESSAGES.getMessage("logging.getMapBackground",
						user.getUserID(),
						user.getIPAddress(),
						geography.getDisplayName());
				rifLogger.info(
					getClass(),
					auditTrailMessage);

				//Assign pooled connection
				connection
					= sqlConnectionManager.assignPooledReadConnection(user);

				//Delegate operation to a specialised manager class
				ResultsQueryManager sqlResultsQueryManager
					= rifServiceResources.getSqlResultsQueryManager();
				result
					= sqlResultsQueryManager.getMapBackground(
						connection,
						geography);
			}
			catch(RIFServiceException rifServiceException) {
				//Audit failure of operation
				logException(
					user,
					"getMapBackground",
					rifServiceException);
			}
			finally {
				//Reclaim pooled connection
				sqlConnectionManager.reclaimPooledReadConnection(
					user,
					connection);
			}

			return result;
		}

		@Override
		public String getTileMakerTiles(
				final User _user,
				final Geography _geography,
				final GeoLevelSelect _geoLevelSelect,
				final Integer zoomlevel,
				final Integer x,
				final Integer y,
				final String tileType)
			throws RIFServiceException {

			//Defensively copy parameters and guard against blocked users
			User user = User.createCopy(_user);
			SQLManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
			if (sqlConnectionManager.isUserBlocked(user)) {
				return null;
			}
			Geography geography
				= Geography.createCopy(_geography);
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.createCopy(_geoLevelSelect);

			String result = "";
			Connection connection = null;
			try {
				//Check for empty parameters
				FieldValidationUtility fieldValidationUtility
					= new FieldValidationUtility();
				fieldValidationUtility.checkNullMethodParameter(
					"getTileMakerTiles",
					"user",
					user);
				fieldValidationUtility.checkNullMethodParameter(
					"getTileMakerTiles",
					"geography",
					geography);
				fieldValidationUtility.checkNullMethodParameter(
					"getTileMakerTiles",
					"getLevelSelect",
					geoLevelSelect);
				fieldValidationUtility.checkNullMethodParameter(
					"getTileMakerTiles",
					"zoomlevel",
					zoomlevel);
				fieldValidationUtility.checkNullMethodParameter(
					"getTileMakerTiles",
					"x",
					x);
				fieldValidationUtility.checkNullMethodParameter(
					"getTileMakerTiles",
					"y",
					y);

				//check that zoomFactor
				if ((zoomlevel <0) || (zoomlevel > 11)) {
					//zoom factor is out of range.
					String errorMessage
						= SERVICE_MESSAGES.getMessage(
							"getTiles.zoomFactor.error",
							String.valueOf(zoomlevel));
					throw new RIFServiceException(
							RIFServiceError.INVALID_ZOOM_FACTOR,
							errorMessage);
				}

				//Check for security violations
				validateUser(user);
				geography.checkSecurityViolations();
				geoLevelSelect.checkSecurityViolations();

				//rifLogger.info(this.getClass(), geography.getDisplayName());

				//Audit attempt to do operation
				String auditTrailMessage
					= SERVICE_MESSAGES.getMessage("logging.getTileMakerTiles",
						user.getUserID(),
						user.getIPAddress(),
						geography.getDisplayName(),
						geoLevelSelect.getDisplayName(),
						String.valueOf(zoomlevel),
						String.valueOf(x),
						String.valueOf(y));
				rifLogger.info(
					getClass(),
					auditTrailMessage);

				//Assign pooled connection
				connection
					= sqlConnectionManager.assignPooledReadConnection(user);

				//Delegate operation to a specialised manager class
				ResultsQueryManager sqlResultsQueryManager
					= rifServiceResources.getSqlResultsQueryManager();
				result
					= sqlResultsQueryManager.getTileMakerTiles(
						connection,
						geography,
						geoLevelSelect,
						zoomlevel,
						x,
						y,
						tileType);
			}
			catch (SQLException sqlException) {
				throw new RIFSQLException(this.getClass(), sqlException, null, null);
			}
			catch(RIFServiceException rifServiceException) {
				//Audit failure of operation
				logException(
					user,
					"getTileMakerTiles",
					rifServiceException);
			}
			finally {
				//Reclaim pooled connection
				sqlConnectionManager.reclaimPooledReadConnection(
					user,
					connection);
			}

			return result;
		}

	@Override
	public String getTileMakerAttributes(
				final User _user,
				final Geography _geography,
				final GeoLevelSelect _geoLevelSelect)
			throws RIFServiceException {

			//Defensively copy parameters and guard against blocked users
			User user = User.createCopy(_user);
			SQLManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
			if (sqlConnectionManager.isUserBlocked(user)) {
				return null;
			}
			Geography geography
				= Geography.createCopy(_geography);
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.createCopy(_geoLevelSelect);

			String result = "";
			Connection connection = null;
			try {
				//Check for empty parameters
				FieldValidationUtility fieldValidationUtility
					= new FieldValidationUtility();
				fieldValidationUtility.checkNullMethodParameter(
					"getTileMakerAttributes",
					"user",
					user);
				fieldValidationUtility.checkNullMethodParameter(
					"getTileMakerAttributes",
					"geography",
					geography);
				fieldValidationUtility.checkNullMethodParameter(
					"getTileMakerAttributes",
					"getLevelSelect",
					geoLevelSelect);

				//Check for security violations
				validateUser(user);
				geography.checkSecurityViolations();
				geoLevelSelect.checkSecurityViolations();

				//rifLogger.info(this.getClass(), geography.getDisplayName());

				//Audit attempt to do operation
				String auditTrailMessage
					= SERVICE_MESSAGES.getMessage("logging.getTileMakerAttributes",
						user.getUserID(),
						user.getIPAddress(),
						geography.getDisplayName(),
						geoLevelSelect.getDisplayName());
				rifLogger.info(
					getClass(),
					auditTrailMessage);

				//Assign pooled connection
				connection
					= sqlConnectionManager.assignPooledReadConnection(user);

				//Delegate operation to a specialised manager class
				ResultsQueryManager sqlResultsQueryManager
					= rifServiceResources.getSqlResultsQueryManager();
				result
					= sqlResultsQueryManager.getTileMakerAttributes(
						connection,
						geography,
						geoLevelSelect);
			}
			catch(RIFServiceException rifServiceException) {
				//Audit failure of operation
				logException(
					user,
					"getTileMakerAttributes",
					rifServiceException);
			}
			finally {
				//Reclaim pooled connection
				sqlConnectionManager.reclaimPooledReadConnection(
					user,
					connection);
			}

			return result;
		}

	@Override
	public String getHomogeneity(
			final User _user,
			final String studyID)
			throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		String result="{}";

		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
					= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
					"getHomogeneity",
					"user",
					user);
			fieldValidationUtility.checkNullMethodParameter(
					"getHomogeneity",
					"studyID",
					studyID);

			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
					"getHomogeneity",
					"studyID",
					studyID);

			//Check for security violations
			validateUser(user);

			//rifLogger.info(this.getClass(), geography.getDisplayName());

			//Audit attempt to do operation
			String auditTrailMessage
					= SERVICE_MESSAGES.getMessage("logging.getHomogeneity",
					                              user.getUserID(),
					                              user.getIPAddress(),
					                              studyID);
			rifLogger.info(
					getClass(),
					auditTrailMessage);

			//Assign pooled connection
			connection
					= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			ResultsQueryManager sqlResultsQueryManager
					= rifServiceResources.getSqlResultsQueryManager();
			result
					= sqlResultsQueryManager.getHomogeneity(
					connection,
					studyID);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
					user,
					"getHomogeneity",
					rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
					user,
					connection);
		}

		return result;
	}

	@Override
	public String getCovariateLossReport(
			final User _user,
			final String studyID)
			throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		String result="{}";

		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
					= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
					"getCovariateLossReport",
					"user",
					user);
			fieldValidationUtility.checkNullMethodParameter(
					"getCovariateLossReport",
					"studyID",
					studyID);

			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
					"getCovariateLossReport",
					"studyID",
					studyID);

			//Check for security violations
			validateUser(user);

			//rifLogger.info(this.getClass(), geography.getDisplayName());

			//Audit attempt to do operation
			String auditTrailMessage
					= SERVICE_MESSAGES.getMessage("logging.getCovariateLossReport",
					                              user.getUserID(),
					                              user.getIPAddress(),
					                              studyID);
			rifLogger.info(
					getClass(),
					auditTrailMessage);

			//Assign pooled connection
			connection
					= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			ResultsQueryManager sqlResultsQueryManager
					= rifServiceResources.getSqlResultsQueryManager();
			result
					= sqlResultsQueryManager.getCovariateLossReport(
					connection,
					studyID);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
					user,
					"getCovariateLossReport",
					rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
					user,
					connection);
		}

		return result;
	}

	@Override
	public String getRiskGraph(
			final User _user,
			final String studyID)
			throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		String result="{}";

		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
					= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
					"getRiskGraph",
					"user",
					user);
			fieldValidationUtility.checkNullMethodParameter(
					"getRiskGraph",
					"studyID",
					studyID);

			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
					"getRiskGraph",
					"studyID",
					studyID);

			//Check for security violations
			validateUser(user);

			//rifLogger.info(this.getClass(), geography.getDisplayName());

			//Audit attempt to do operation
			String auditTrailMessage
					= SERVICE_MESSAGES.getMessage("logging.getRiskGraph",
					                              user.getUserID(),
					                              user.getIPAddress(),
					                              studyID);
			rifLogger.info(
					getClass(),
					auditTrailMessage);

			//Assign pooled connection
			connection
					= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			ResultsQueryManager sqlResultsQueryManager
					= rifServiceResources.getSqlResultsQueryManager();
			result
					= sqlResultsQueryManager.getRiskGraph(
					connection,
					studyID);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
					user,
					"getRiskGraph",
					rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
					user,
					connection);
		}

		return result;
	}

	@Override
	public String getSelectState(
		final User _user,
		final String studyID)
				throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		String result="{}";

		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getSelectState",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getSelectState",
				"studyID",
				studyID);

			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getSelectState",
				"studyID",
				studyID);

			//Check for security violations
			validateUser(user);

			//rifLogger.info(this.getClass(), geography.getDisplayName());

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getSelectState",
					user.getUserID(),
					user.getIPAddress(),
					studyID);
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			ResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			result
				= sqlResultsQueryManager.getSelectState(
					connection,
					studyID);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getSelectState",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user,
				connection);
		}

		return result;
	}

	@Override
	public String getPrintState(
			final User _user,
			final String studyID)
					throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		String result="{}";

		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getPrintState",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getPrintState",
				"studyID",
				studyID);

			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getPrintState",
				"studyID",
				studyID);

			//Check for security violations
			validateUser(user);

			//rifLogger.info(this.getClass(), geography.getDisplayName());

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getPrintState",
					user.getUserID(),
					user.getIPAddress(),
					studyID);
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			ResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			result
				= sqlResultsQueryManager.getPrintState(
					connection,
					studyID);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getPrintState",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user,
				connection);
		}

		return result;
	}

	@Override
	public void setPrintState(
		final User _user,
		final String studyID,
		final String printStateText)
				throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return;
		}

		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"setPrintState",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"setPrintState",
				"studyID",
				studyID);

			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"setPrintState",
				"studyID",
				studyID);

			//Check for security violations
			validateUser(user);

			//rifLogger.info(this.getClass(), geography.getDisplayName());

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.setPrintState",
					user.getUserID(),
					user.getIPAddress(),
					studyID);
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			ResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			sqlResultsQueryManager.setPrintState(
					connection,
					studyID,
					printStateText);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"setPrintState",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user,
				connection);
		}
	}

	@Override
	public String getPostalCodeCapabilities(
		final User _user,
		final Geography _geography)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		Geography geography
			= Geography.createCopy(_geography);
		String result="{}";

		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getPostalCodeCapabilities",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getPostalCodeCapabilities",
				"geography",
				geography);

			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();

			//rifLogger.info(this.getClass(), geography.getDisplayName());

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getPostalCodeCapabilities",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			ResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			result
				= sqlResultsQueryManager.getPostalCodeCapabilities(
					connection,
					geography);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getPostalCodes",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user,
				connection);
		}

		return result;
	}

	@Override
	public String getPostalCodes(
		final User _user,
		final Geography _geography,
		final String postcode,
		final Locale locale)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		Geography geography
			= Geography.createCopy(_geography);
		String result="{}";

		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getPostalCodes",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getPostalCodes",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getPostalCodes",
				"postcode",
				postcode);

			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();

			//rifLogger.info(this.getClass(), geography.getDisplayName());

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getPostalCodes",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					postcode);
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			ResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			result
				= sqlResultsQueryManager.getPostalCodes(
					connection,
					geography,
					postcode,
					locale);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getPostalCodes",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user,
				connection);
		}

		return result;
	}

	@Override
	public RIFServiceInformation getRIFServiceInformation(
			final User _user)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}

		RIFServiceInformation result
			= RIFServiceInformation.newInstance();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getRIFServiceInformation",
				"user",
				user);

			//Check for security violations
			validateUser(user);

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getRIFSubmissionServiceInformation",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Delegate operation to a specialised manager class
			result.setServiceName(serviceName);
			result.setServiceDescription(serviceDescription);
			result.setContactEmail(serviceContactEmail);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getRIFServiceInformation",
				rifServiceException);
		}

		return result;
	}
}
