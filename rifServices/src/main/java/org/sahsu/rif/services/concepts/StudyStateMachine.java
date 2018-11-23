package org.sahsu.rif.services.concepts;

import org.sahsu.rif.generic.util.RIFLogger;

public class StudyStateMachine {

	protected static RIFLogger rifLogger = RIFLogger.getLogger();
	
	private StudyState initialState = StudyState.STUDY_NOT_CREATED;
	private StudyState finalState = StudyState.STUDY_RESULTS_COMPUTED;
	
	private StudyState currentState;

	public StudyStateMachine() {

	}

	public void initialiseState() {
		currentState = initialState;
	}

	public StudyState extractFailure() {
		return StudyState.STUDY_EXTRACT_FAILURE;
	}

	public StudyState rFailure() {
		return StudyState.STUDY_RESULTS_RFAILURE;
	}

	public StudyState next() {
		if (currentState == finalState) {
			return currentState;
		}
		
		if (currentState == StudyState.STUDY_NOT_CREATED) {
			currentState = StudyState.STUDY_CREATED;
		} else if (currentState == StudyState.STUDY_CREATED) {
			currentState = StudyState.STUDY_EXTRACTED;
		} else if (currentState == StudyState.STUDY_EXTRACTED) {
			currentState = StudyState.STUDY_RESULTS_COMPUTED;		
		} else {
			rifLogger.warning(this.getClass(),
			                  "StudyStateMachine -- next -- this should never happen.");
			assert false;
		}
		
		return currentState;
	}
	
	public StudyState getCurrentStudyState() {
		return currentState;
	}
	
	public void setCurrentStudyState(final StudyState currentState) {
		this.currentState = currentState;
	}
	
	public boolean isFinished() {
		return currentState == finalState;
	}
}
