package rifServices.dataStorageLayer.common;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.dataStorageLayer.ms.MSSQLAbstractRIFService;
import rifServices.dataStorageLayer.ms.MSSQLConnectionManager;
import rifServices.dataStorageLayer.ms.MSSQLRIFServiceResources;

public class ExceptionLog {
	private User user;
	private String methodName;
	private RIFServiceException rifServiceException;
	private MSSQLRIFServiceResources rifServiceResources;
	private RIFLogger rifLogger;
	
	public ExceptionLog(User user, String methodName, RIFServiceException rifServiceException,
			MSSQLRIFServiceResources rifServiceResources, RIFLogger rifLogger) {
		this.user = user;
		this.methodName = methodName;
		this.rifServiceException = rifServiceException;
		this.rifServiceResources = rifServiceResources;
		this.rifLogger = rifLogger;
	}
	
	public void log() throws RIFServiceException {
		
		MSSQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		
		if (rifServiceException instanceof RIFServiceSecurityException) {
		
			RIFServiceSecurityException rifServiceSecurityException =
					(RIFServiceSecurityException) rifServiceException;
		
			if (rifServiceSecurityException.getSecurityThreatType() ==
		            RIFServiceSecurityException.SecurityThreatType.MALICIOUS_CODE) {
				
				sqlConnectionManager.addUserIDToBlock(user);
				sqlConnectionManager.logout(user);
			}
			else {

				sqlConnectionManager.logSuspiciousUserEvent(user);
				if (sqlConnectionManager.userExceededMaximumSuspiciousEvents(user)) {
					sqlConnectionManager.addUserIDToBlock(user);
					sqlConnectionManager.logout(user);
				}
			}
		}
		
		String callingClassName = new Exception().getStackTrace()[1].getClassName();
		rifLogger.error(callingClassName, methodName, rifServiceException);
		
		throw rifServiceException;
	}
}
