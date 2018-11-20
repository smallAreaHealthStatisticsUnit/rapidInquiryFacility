package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.RIFDatabaseProperties;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.concepts.StudyState;
import org.sahsu.rif.services.concepts.StudyStateMachine;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public class RunStudyThread implements Runnable {

	private static final int SLEEP_TIME = 200;

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	
	private Connection connection;
	private User user;
	private RIFStudySubmission studySubmission;
	private String studyID;
	private String url;

	private StudyStateMachine studyStateMachine;
	
	private StudyStateManager studyStateManager;
	private StudySubmissionStep createStudySubmissionStep;
	private GenerateResultsSubmissionStep generateResultsSubmissionStep;
	private StatisticsProcessing statisticsProcessing;
	
	public RunStudyThread() {
		studyStateMachine = new StudyStateMachine();
		studyStateMachine.initialiseState();
	}
	
	public void initialise(final Connection connection, final User user, final String password,
			final RIFStudySubmission studySubmission,
			final RIFServiceStartupOptions rifServiceStartupOptions,
			final ServiceResources rifServiceResources, final String url)
			throws RIFServiceException {
		
		this.connection = connection;
		this.user = user;				
		this.studySubmission = studySubmission;
		this.url = url;
		
		RIFDatabaseProperties rifDatabaseProperties 
			= rifServiceStartupOptions.getRIFDatabaseProperties();
		
		studyStateManager = new CommonStudyStateManager(rifServiceStartupOptions);
		
		createStudySubmissionStep 
			= new StudySubmissionStep(
				rifServiceStartupOptions,
				rifServiceResources.getSqlDiseaseMappingStudyManager(),
				rifServiceResources.getSQLMapDataManager());

		generateResultsSubmissionStep = GenerateResultsSubmissionStep.getInstance(
				studyStateManager, rifDatabaseProperties.getDatabaseType());
		
		statisticsProcessing = new StatisticsProcessing();
		statisticsProcessing.initialise(
			user.getUserID(), 
			password, 
			rifServiceStartupOptions);			
	}
		
	public void run() {
		try {

			establishCurrentStudyState();

			while (!studyStateMachine.isFinished()) {

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

					throw new RIFServiceException(
							RIFServiceError.STATE_MACHINE_ERROR,
							errorMessage);
				}
						
				if (newState.getName().equals(currentState.getName())) {
					String errorMessage = "Study: " + studyID + 
							"; no change in state: " + currentState.getName();
					throw new RIFServiceException(
						RIFServiceError.STATE_MACHINE_ERROR,
						errorMessage);
				}
				Thread.sleep(SLEEP_TIME);
			} // End of while loop
			
			rifLogger.info(this.getClass(), "run() Finished OK!!");
			
		}
		catch(InterruptedException interruptedException) {
			rifLogger.info(this.getClass(), "run() FAILED: " + interruptedException.getMessage());
			rifLogger.error(this.getClass(), getClass().getSimpleName() + " ERROR",
			                interruptedException);

		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.info(this.getClass(), "run() FAILED: " + rifServiceException.getMessage());
			rifServiceException.printErrors();
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
		    
			createStudySubmissionStep.updateSelectState(
				connection, 
				user, studyID, studySubmission.getStudySelection());
			createStudySubmissionStep.updatePrintState(
				connection, 
				user, studyID);
			
			String statusMessage
				= RIFServiceMessages.getMessage(
					"studyState.studyCreated.description");
			updateStudyStatusState(statusMessage);	
			rifLogger.info(this.getClass(), "createStudy() OK");
		}	
		catch (RIFServiceException rifServiceException) {
			rifLogger.info(this.getClass(), "createStudy() FAILED: " + rifServiceException.getMessage());
			rollbackStudy();
			throw rifServiceException;
		}
		catch (Exception exception) {
			// Do not update status; 
			// because this is a database procedure that failed the transaction must be rolled back
			rifLogger.info(this.getClass(), "createStudy() FAILED: " + exception.getMessage());
			rollbackStudy();
			
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToAddStudySubmission",
					studyID);
			RIFServiceException rifServiceException
					= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}			
	}
	
	private void generateResults() 
		throws RIFServiceException {

		try {			
			if (generateResultsSubmissionStep.performStep(connection, studyID)) {

				studyStateMachine.next(); // Advance to next state
			} else {
				createStudySubmissionStep.setStudyExtractToFail(connection, studyID, 
					generateResultsSubmissionStep.getResult(),
					generateResultsSubmissionStep.getStack());
			}
		}
		catch (RIFServiceException rifServiceException) {
			// because this is a database procedure that failed the transaction must be rolled back
			rollbackStudy();

			StudyState errorStudyState = studyStateMachine.extractFailure();
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
			statisticsProcessing.performStep(connection, studySubmission, studyID, url);
			String statusMessage
				= RIFServiceMessages.getMessage(
					"studyState.studyResultsComputed.description");
			updateStudyStatusState(statusMessage);
		}	
		catch (RIFServiceException rifServiceException) {
			StudyState errorStudyState = studyStateMachine.rFailure();
			String statusMessage
				= RIFServiceMessages.getMessage(
					"studyState.studyResultsRFailure.description");
			updateStudyStatusState(statusMessage, rifServiceException, errorStudyState);
			throw new RIFServiceException(rifServiceException,
			                              "Status is '%s'; error study state is '%s'",
			                              statusMessage, errorStudyState);
		}
	}

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
			errorString.append(errorMessage).append(lineSeparator);
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
