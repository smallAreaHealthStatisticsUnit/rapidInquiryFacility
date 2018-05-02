package rifServices.dataStorageLayer.ms;

import java.sql.Connection;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.StudyState;
import rifServices.businessConceptLayer.StudyStateMachine;
import rifServices.dataStorageLayer.common.GenerateResultsSubmissionStep;
import rifServices.dataStorageLayer.common.ServiceResources;
import rifServices.dataStorageLayer.common.StudyStateManager;
import rifServices.dataStorageLayer.common.StudySubmissionStep;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

public class MSSQLRunStudyThread implements Runnable {

	private static final int SLEEP_TIME = 200;

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	
	private Connection connection;
	private User user;
	private RIFStudySubmission studySubmission;
	private String studyID;
	
	private StudyStateMachine studyStateMachine;
	
	private StudyStateManager studyStateManager;
	private StudySubmissionStep createStudySubmissionStep;
	private GenerateResultsSubmissionStep generateResultsSubmissionStep;
	private MSSQLSmoothResultsSubmissionStep smoothResultsSubmissionStep;
	
	public MSSQLRunStudyThread() {
		studyStateMachine = new StudyStateMachine();
		studyStateMachine.initialiseState();
	}
	
	public void initialise(
		final Connection connection,
		final User user,
		final String password,		
		final RIFStudySubmission studySubmission,
		final RIFServiceStartupOptions rifServiceStartupOptions,
		final ServiceResources rifServiceResources) {
		
		this.connection = connection;
		this.user = user;				
		this.studySubmission = studySubmission;
		
		
		RIFDatabaseProperties rifDatabaseProperties 
			= rifServiceStartupOptions.getRIFDatabaseProperties();
		
		studyStateManager = new MSSQLStudyStateManager(rifServiceStartupOptions);
		
		createStudySubmissionStep 
			= new StudySubmissionStep(
				rifServiceStartupOptions,
				rifServiceResources.getSqlDiseaseMappingStudyManager(),
				rifServiceResources.getSQLMapDataManager());

		generateResultsSubmissionStep = GenerateResultsSubmissionStep.getInstance(
				studyStateManager, rifDatabaseProperties.getDatabaseType());
		
		//KLG: @TODO - we need a facility to feed password to this.
		smoothResultsSubmissionStep = new MSSQLSmoothResultsSubmissionStep();
		smoothResultsSubmissionStep.initialise(
			user.getUserID(), 
			password, 
			rifServiceStartupOptions);			
	}
		
	public void run() {
		try {

			establishCurrentStudyState();

			while (studyStateMachine.isFinished() == false) {

				StudyState currentState
					= studyStateMachine.getCurrentStudyState();
				StudyState newState;
				
				if (currentState == StudyState.STUDY_NOT_CREATED) {
					//Study has not been created.  We need to add parts of it to the database.  At the end,
					//we will have a description where we can generate extract tables
					rifLogger.info(this.getClass(), "Create study BEFORE state=="+currentState.getName()+"==");
					createStudy();
					
					newState = studyStateMachine.getCurrentStudyState();
					rifLogger.info(this.getClass(), "Study: " + studyID + 
						"; run create study AFTER state=="+newState.getName()+"==");
				}
				else if (currentState == StudyState.STUDY_CREATED) {
					rifLogger.info(this.getClass(), "Study: " + studyID + 
						"; run generate results BEFORE state=="+currentState.getName()+"==");
					generateResults();
					
					newState = studyStateMachine.getCurrentStudyState();
					rifLogger.info(this.getClass(), "Study: " + studyID + 
						"; run generate results AFTER state=="+newState.getName()+"==");
				}
				else if (currentState == StudyState.STUDY_EXTRACTED) {
					//we are done.  Break out of the loop so that the thread can stop
					rifLogger.info(this.getClass(), "Study: " + studyID + 
						"; run smooth results BEFORE state=="+currentState.getName()+"==");
					smoothResults();
					
					newState = studyStateMachine.getCurrentStudyState();
					rifLogger.info(this.getClass(), "Study: " + studyID + 
						"; run smooth results AFTER state=="+newState.getName()+"==");
				}
				else {
					String errorMessage = "Study: " + studyID + 
							"; unexpected state: " + studyStateMachine.getCurrentStudyState().getName();
					RIFServiceException rifServiceException
						= new RIFServiceException(
							RIFServiceError.STATE_MACHINE_ERROR, 
							errorMessage);
					throw rifServiceException; 
				}
						
				if (newState.getName().equals(currentState.getName())) {
					String errorMessage = "Study: " + studyID + 
							"; no change in state: " + currentState.getName();
					RIFServiceException rifServiceException
						= new RIFServiceException(
							RIFServiceError.STATE_MACHINE_ERROR, 
							errorMessage);
					throw rifServiceException; 
				}
				Thread.sleep(SLEEP_TIME);
			} // End of while loop
			
			rifLogger.info(this.getClass(), "Finished!!");
			
		}
		catch(InterruptedException interruptedException) {
			rifLogger.error(this.getClass(), "MSSQLRunStudyThread ERROR", interruptedException);

		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
		}
		finally {
			
			
		}
	}
	
	private void establishCurrentStudyState() 
		throws RIFServiceException {
		
		/*
		 * Determine the initial state of the study state machine.  If there is no
		 * study ID yet, then it means it hasn't even been created yet and the first
		 * step should be to call the create study submission step.  Otherwise, if it
		 * already exists, then the study state manager will be able to determine the
		 * state by checking RIF table status flags.
		 */
		if (studyID == null) {
			//it means it hasn't been created yet
			studyStateMachine.setCurrentStudyState(StudyState.STUDY_NOT_CREATED);				
		}
		else {
			StudyState studyState 
				= studyStateManager.getStudyState(
					connection, 
					user, 
					studyID);				
			studyStateMachine.setCurrentStudyState(studyState);
		}		
		
	}
	
	private void createStudy() 
		throws RIFServiceException {

		try {		
			studyID = createStudySubmissionStep.performStep(
				connection, 
				user, 
				studySubmission);
		
			String statusMessage
				= RIFServiceMessages.getMessage(
					"studyState.studyCreated.description");
			updateStudyStatusState(statusMessage);	
		}	
		catch (RIFServiceException rifServiceException) {
			// Do not update status; 
			// because this is a database procedure that failed the transaction must be rolled back
			rollbackStudy();
			throw rifServiceException;
		}			
	}
	
	private void generateResults() 
		throws RIFServiceException {

		try {			
			generateResultsSubmissionStep.performStep(
				connection, 
				studyID);
			StudyState currentStudyState = studyStateMachine.next(); // Advance to next state
			
// Done by DB procedure - will generate duplicate PK
//			String statusMessage
//				= RIFServiceMessages.getMessage(
//					"studyState.studyExtracted.description");
//			updateStudyStatusState(statusMessage);	
		}	
		catch (RIFServiceException rifServiceException) {
			// because this is a database procedure that failed the transaction must be rolled back
			rollbackStudy();

// Done by DB procedure - will generate duplicate PK	
			StudyState errorStudyState = studyStateMachine.ExtractFailure();
			String statusMessage
				= RIFServiceMessages.getMessage(
					"studyState.studyExtractFailure.description");
			updateStudyStatusState(statusMessage, rifServiceException, errorStudyState);
			throw rifServiceException;
		}	
	}
	
	private void smoothResults() 
		throws RIFServiceException {

		try {
			smoothResultsSubmissionStep.performStep(
				connection,
				studySubmission, 
				studyID);
			String statusMessage
				= RIFServiceMessages.getMessage(
					"studyState.studyResultsComputed.description");
			updateStudyStatusState(statusMessage);
		}	
		catch (RIFServiceException rifServiceException) {
			StudyState errorStudyState = studyStateMachine.RFailure();
			String statusMessage
				= RIFServiceMessages.getMessage(
					"studyState.studyResultsRFailure.description");
			updateStudyStatusState(statusMessage, rifServiceException, errorStudyState);
			throw rifServiceException;
		}

	}
	
//	private void advertiseDataSet() 
//		throws RIFServiceException {
//		
//		//This is where we should save the study to a ZIP file
//		publishResultsSubmissionStep.performStep(
//			connection, 
//			user, 
//			studySubmission, 
//			studyID);
//
//		String statusMessage
//			= RIFServiceMessages.getMessage(
//				"studyState.readyForUse");
//		updateStudyStatusState(statusMessage);
//
//		rifLogger.info(this.getClass(), "RIF study should be FINISHED!!");
//	}
	
	// Normal state transition
	private void updateStudyStatusState(final String statusMessage) 
		throws RIFServiceException {

		StudyState currentStudyState = studyStateMachine.next(); // Advance to next state
				
		studyStateManager.updateStudyStatus(
			connection,
			user, 
			studyID, 
			currentStudyState,
			statusMessage,
			null);
	}
	
	// Error 
	private void updateStudyStatusState(
		final String statusMessage, 
		final RIFServiceException rifServiceException, 
		StudyState errorStudyState) 
		throws RIFServiceException {
				
		StringBuilder errorString = new StringBuilder();
		for (String errorMessage : rifServiceException.getErrorMessages()) {
			errorString.append(errorMessage + lineSeparator);
		}
					
		studyStateManager.updateStudyStatus(
			connection,
			user, 
			studyID, 
			errorStudyState,
			statusMessage,
			errorString.toString());
	}
	
	private void rollbackStudy() 
		throws RIFServiceException {
				
		studyStateManager.rollbackStudy(
			connection, studyID);
	}
}
