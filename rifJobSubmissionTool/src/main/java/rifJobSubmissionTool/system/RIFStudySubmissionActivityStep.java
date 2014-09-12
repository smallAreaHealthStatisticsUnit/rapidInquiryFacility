package rifJobSubmissionTool.system;



/**
 *
 *
 * <hr>

 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
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
 * @version
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

public enum RIFStudySubmissionActivityStep {
	
	/** The create or copy study step. */
	CREATE_OR_COPY_STUDY_STEP(0, "rifActivityStep.createStudy"),
	
	/** The describe study. */
	DESCRIBE_STUDY(1, "rifActivityStep.describeStudy"),
	
	/** The choose study area. */
	CHOOSE_STUDY_AREA(2, "rifActivityStep.chooseStudyArea"),
	
	/** The choose comparison area. */
	CHOOSE_COMPARISON_AREA(3, "rifActivityStep.chooseComparisonArea"),
	
	/** The specify investigations. */
	SPECIFY_INVESTIGATIONS(4, "rifActivityStep.specifyInvestigation"),
	
	/** The specify reports. */
	SPECIFY_REPORTS(5, "rifActivityStep.specifyReports"),
	
	/** The preview. */
	PREVIEW(6, "rifActivityStep.preview"),
	
	/** The submit study. */
	SUBMIT_STUDY(7, "rifActivityStep.submitStudy");
		
	/** The step number. */
	private int stepNumber;
	
	/** The property name. */
	private String propertyName;
	
	/**
	 * Instantiates a new RIF activity step.
	 *
	 * @param stepNumber the step number
	 * @param propertyName the property name
	 */
	private RIFStudySubmissionActivityStep(
		int stepNumber,
		String propertyName) {
		
		this.stepNumber = stepNumber;
		this.propertyName = propertyName;
	}
	
	/**
	 * Gets the step number.
	 *
	 * @return the step number
	 */
	public int getStepNumber() {
		
		return stepNumber;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		
		return RIFJobSubmissionToolMessages.getMessage(propertyName);
	}
	
	/**
	 * Gets the total steps.
	 *
	 * @return the total steps
	 */
	public static int getTotalSteps() {
		
		return 7;	
	}
	
	
	/**
	 * Gets the first activity step.
	 *
	 * @return the first activity step
	 */
	public static RIFStudySubmissionActivityStep getFirstActivityStep() {
		
		return CREATE_OR_COPY_STUDY_STEP;
	}
	
	/**
	 * Gets the previous activity step.
	 *
	 * @return the previous activity step
	 */
	public RIFStudySubmissionActivityStep getPreviousActivityStep() {
		
		if (this == RIFStudySubmissionActivityStep.CREATE_OR_COPY_STUDY_STEP) {
			return CREATE_OR_COPY_STUDY_STEP;
		}
		else if (this == RIFStudySubmissionActivityStep.DESCRIBE_STUDY) {
			return CREATE_OR_COPY_STUDY_STEP;
		}
		else if (this == RIFStudySubmissionActivityStep.CHOOSE_STUDY_AREA) {
			return DESCRIBE_STUDY;
		}
		else if (this == RIFStudySubmissionActivityStep.CHOOSE_COMPARISON_AREA) {
			return CHOOSE_STUDY_AREA;
		}
		else if (this == RIFStudySubmissionActivityStep.SPECIFY_INVESTIGATIONS) {
			return CHOOSE_COMPARISON_AREA;
		}
		else if (this == RIFStudySubmissionActivityStep.SPECIFY_REPORTS){
			return SPECIFY_INVESTIGATIONS;
		}
		else {
			return SPECIFY_REPORTS;
		}
	}
	
	/**
	 * Gets the next activity step.
	 *
	 * @return the next activity step
	 */
	public RIFStudySubmissionActivityStep getNextActivityStep() {
		
		if (this == RIFStudySubmissionActivityStep.CREATE_OR_COPY_STUDY_STEP) {
			return DESCRIBE_STUDY;
		}
		else if (this == RIFStudySubmissionActivityStep.DESCRIBE_STUDY) {
			return CHOOSE_STUDY_AREA;
		}
		else if (this == RIFStudySubmissionActivityStep.CHOOSE_STUDY_AREA) {
			return CHOOSE_COMPARISON_AREA;
		}
		else if (this == RIFStudySubmissionActivityStep.CHOOSE_COMPARISON_AREA) {
			return SPECIFY_INVESTIGATIONS;
		}
		else if (this == RIFStudySubmissionActivityStep.SPECIFY_INVESTIGATIONS) {
			return SPECIFY_REPORTS;
		}
		else if (this == SPECIFY_REPORTS) {
			return PREVIEW;
		}
		else {
			//user will have submitted a study, time to go back to the
			//beginning of the work flow
			return CREATE_OR_COPY_STUDY_STEP;
		}		
	}
	
}

