package rifServices.dataStorageLayer.common;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.sun.rowset.CachedRowSetImpl;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;

public interface SQLManager {
	
	ValidationPolicy getValidationPolicy();
	
	void setValidationPolicy(ValidationPolicy validationPolicy);
	
	void configureQueryFormatterForDB(
			AbstractSQLQueryFormatter queryFormatter);
	
	PreparedStatement createPreparedStatement(
			Connection connection,
			AbstractSQLQueryFormatter queryFormatter)
		throws SQLException;
	
	CachedRowSetImpl createCachedRowSet(
			Connection connection,
			AbstractSQLQueryFormatter queryFormatter,
			String queryName)
				throws Exception;
	
	CachedRowSetImpl createCachedRowSet(
			Connection connection,
			AbstractSQLQueryFormatter queryFormatter,
			String queryName,
			int[] params)
				throws Exception;
	
	String getColumnFromResultSet(
			CachedRowSetImpl cachedRowSet,
			String columnName)
			throws Exception;
	
	String getColumnFromResultSet(
			CachedRowSetImpl cachedRowSet,
			String columnName,
			boolean allowNulls,
			boolean allowNoRows)
			throws Exception;
	
	String getColumnComment(Connection connection,
			String schemaName, String tableName, String columnName)
			throws Exception;
	
	void enableDatabaseDebugMessages(
			Connection connection)
		throws RIFServiceException;
	
	void setEnableLogging(boolean enableLogging);
	
	String getUserPassword(final User user);
	
	boolean userExists(final String userID);
	
	void logSQLQuery(
			String queryName,
			AbstractSQLQueryFormatter queryFormatter,
			String... parameters);
	
	void logSQLException(SQLException sqlException);
	
	boolean isUserBlocked(final User user);
	
	void logSuspiciousUserEvent(final User user);
	
	boolean userExceededMaximumSuspiciousEvents(final User user);
	
	void addUserIDToBlock(final User user);
	
	void login(final String userID, final String password) throws RIFServiceException;
	
	boolean isLoggedIn(final String userID);
	
	Connection assignPooledReadConnection(final User user) throws RIFServiceException;
	
	void reclaimPooledReadConnection(final User user, final Connection connection)
			throws RIFServiceException;
	
	void reclaimPooledWriteConnection(final User user, final Connection connection)
			throws RIFServiceException;
	
	Connection assignPooledWriteConnection(final User user) throws RIFServiceException;
	
	void logout(final User user) throws RIFServiceException;
	
	void deregisterAllUsers() throws RIFServiceException;
	
	default CallableStatement createPreparedCall(final Connection connection, final String query)
			throws SQLException {
		
		throw new UnsupportedOperationException("Method not implemented.");
	}
}
