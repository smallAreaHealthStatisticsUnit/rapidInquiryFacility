package rifServices.dataStorageLayer;

import rifServices.system.RIFServiceMessages;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.StudyState;
import rifServices.businessConceptLayer.StudyStateMachine;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;


import java.util.ArrayList;
import java.sql.*;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 */

/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

public class RunStudyThread 
	implements Runnable {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final int SLEEP_TIME = 500;
	// ==========================================
	// Section Properties
	// ==========================================
	private Connection connection;
	private User user;
	private SQLStudyStateManager studyStateManager;
	private SQLRIFSubmissionManager studySubmissionManager;
	private RIFStudySubmission studySubmission;
	private String studyID;
	
	private StudyStateMachine studyStateMachine;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RunStudyThread() {
		studyStateMachine = new StudyStateMachine();
		studyStateMachine.initialiseState();
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void initialise(
		final Connection connection,
		final User user,
		final SQLStudyStateManager studyStateManager, 
		final SQLRIFSubmissionManager studySubmissionManager,
		final RIFStudySubmission studySubmission,
		final String studyID) {
		
		this.connection = connection;
		this.user = user;
		this.studyStateManager = studyStateManager;
		this.studySubmissionManager = studySubmissionManager;
		this.studySubmission = studySubmission;
		this.studyID = studyID;
	}
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================
	public void run() {
		try {
			while (studyStateMachine.isFinished() == false) {				
				StudyState currentState
					= studyStateMachine.getCurrentStudyState();
				if (currentState == StudyState.STUDY_CREATED) {
					//Study has been extracted but neither the extract nor map tables
					//for that study have been produced.
					verifyStudyProperlyCreated();					
				}
				else if (currentState == StudyState.STUDY_VERIFIED) {
					createExtractTable();
				}
				else if (currentState == StudyState.STUDY_EXTRACTED) {
					computeSmoothedResults();
				}
				else if (currentState == StudyState.STUDY_RESULTS_COMPUTED) {
					
					//we are done.  Break out of the loop so that the thread can stop
					break;
				}

				
				Thread.sleep(SLEEP_TIME);
			}
		}
		catch(InterruptedException interruptedException) {
			
		}
		catch(RIFServiceException rifServiceException) {
			
		}
		finally {
			
			
		}
	}
	
	
	private void verifyStudyProperlyCreated() 
		throws RIFServiceException {
		
		/*
		studySubmissionManager.verifyStudyProperlyCreated(
			user,
			studyID);
		
		String statusMessage
			= RIFServiceMessages.getMessage(
				"",
				studyID);
		studyStateManager.addStatusMessage(
			user, 
			studyID, 
			statusMessage);
		studyStateMachine.next();
		*/
	}
	
	
	private void createExtractTable() 
		throws RIFServiceException {
		
		/*
		studySubmissionManager.verifyStudyProperlyCreated(
			user,
			studyID);
			
		studyStateManager.updateStudyState(
			connection, 
			user, 
			studyID, 
			studyStateMachine.getCurrentStudyState());
		String statusMessage
			= RIFServiceMessages.getMessage(
				"",
				studyID);
		studyStateManager.addStatusMessage(user, studyID, statusMessage);
		studyStateMachine.next();	
		
		*/
	}
	
	private void computeSmoothedResults() {
		
		
	}
	
	
	// ==========================================
	// Section Override
	// ==========================================
}
