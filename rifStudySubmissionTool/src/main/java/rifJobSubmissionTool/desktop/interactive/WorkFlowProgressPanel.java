
package rifJobSubmissionTool.desktop.interactive;

import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFStudySubmissionActivityStep;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;


/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2016 Imperial College London, developed by the Small Area
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


final class WorkFlowProgressPanel {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	//Data
	
	//GUI Components
	/** The panel. */
	private JPanel panel;	
	/** The panel gc. */
	private GridBagConstraints panelGC;	
	/** The select study. */
	private WorkFlowStep selectStudy;
	/** The describe study. */
	private WorkFlowStep describeStudy;
	/** The select study area. */
	private WorkFlowStep selectStudyArea;
	/** The select comparison area. */
	private WorkFlowStep selectComparisonArea;
	/** The select investigations. */
	private WorkFlowStep selectInvestigations;
	/** The specify reporting options. */
	private WorkFlowStep specifyReportingOptions;	
	/** The show preview. */
	private WorkFlowStep showPreview;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new work flow progress panel.
     *
     * @param userInterfaceFactory the user interface factory
     */
	public WorkFlowProgressPanel(
		UserInterfaceFactory userInterfaceFactory) {
		
		panel = userInterfaceFactory.createPanel();
		panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.insets = new Insets(5, 0, 0, 0);
		panelGC.weightx = 0;
		panelGC.ipadx = 5;
		panelGC.ipady = 5;
		
		String selectStudyLabelText
			= RIFStudySubmissionActivityStep.CREATE_OR_COPY_STUDY_STEP.getName();
		selectStudy = new WorkFlowStep(selectStudyLabelText);

		String describeStudyLabelText
			= RIFStudySubmissionActivityStep.DESCRIBE_STUDY.getName();
		describeStudy = new WorkFlowStep(describeStudyLabelText);
				
		String selectStudyAreaLabelText
			= RIFStudySubmissionActivityStep.CHOOSE_STUDY_AREA.getName();
		selectStudyArea = new WorkFlowStep(selectStudyAreaLabelText);
		
		String selectComparisonAreaLabelText
			= RIFStudySubmissionActivityStep.CHOOSE_COMPARISON_AREA.getName();
		selectComparisonArea = new WorkFlowStep(selectComparisonAreaLabelText);
		
		
		String selectInvestigationsLabelText
			= RIFStudySubmissionActivityStep.SPECIFY_INVESTIGATIONS.getName();
		selectInvestigations = new WorkFlowStep(selectInvestigationsLabelText);
		
		String specifyReportingOptionsLabelText
			= RIFStudySubmissionActivityStep.SPECIFY_REPORTS.getName();
		specifyReportingOptions 
			= new WorkFlowStep(specifyReportingOptionsLabelText);
		
		String previewLabelText
			= RIFStudySubmissionActivityStep.PREVIEW.getName();
		showPreview
			= new WorkFlowStep(previewLabelText);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		
		selectStudyArea();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
	/**
	 * Gets the panel.
	 *
	 * @return the panel
	 */
	public JPanel getPanel() {
		return panel;
	}

	/**
	 * Sets the current activity step.
	 *
	 * @param currentRIFActivityStep the new current activity step
	 */
	public void setCurrentActivityStep(
		RIFStudySubmissionActivityStep currentRIFActivityStep) {
		
		if (currentRIFActivityStep == RIFStudySubmissionActivityStep.CREATE_OR_COPY_STUDY_STEP) {
			selectStudy();
		}
		else if (currentRIFActivityStep == RIFStudySubmissionActivityStep.DESCRIBE_STUDY) {
			describeStudy();			
		}
		else if (currentRIFActivityStep == RIFStudySubmissionActivityStep.CHOOSE_STUDY_AREA) {
			selectStudyArea();			
		}
		else if (currentRIFActivityStep == RIFStudySubmissionActivityStep.CHOOSE_COMPARISON_AREA) {
			selectComparisonArea();			
		}
		else if (currentRIFActivityStep == RIFStudySubmissionActivityStep.SPECIFY_INVESTIGATIONS) {
			selectInvestigation();			
		}
		else if (currentRIFActivityStep == RIFStudySubmissionActivityStep.SPECIFY_REPORTS) {
			selectSpecifyReportingOptions();			
		}
		else if (currentRIFActivityStep == RIFStudySubmissionActivityStep.PREVIEW) {
			showPreview();
		}
		panel.updateUI();
	}
		
	/**
	 * Select study.
	 */
	private void selectStudy() {
		
		addFirstMarkedStep(selectStudy);
		addUnmarkedStep(describeStudy);
		addUnmarkedStep(selectStudyArea);
		addUnmarkedStep(selectComparisonArea);
		addUnmarkedStep(selectInvestigations);
		addUnmarkedStep(specifyReportingOptions);
		addUnmarkedStep(showPreview);		
	}
	
	/**
	 * Describe study.
	 */
	private void describeStudy() {
		
		addFirstMarkedStep(selectStudy);
		addMarkedStep(describeStudy);
		addUnmarkedStep(selectStudyArea);
		addUnmarkedStep(selectComparisonArea);
		addUnmarkedStep(selectInvestigations);
		addUnmarkedStep(specifyReportingOptions);
		addUnmarkedStep(showPreview);	
	}
		
	/**
	 * Select study area.
	 */
	private void selectStudyArea() {
		
		addFirstMarkedStep(selectStudy);
		addMarkedStep(describeStudy);
		addMarkedStep(selectStudyArea);
		addUnmarkedStep(selectComparisonArea);
		addUnmarkedStep(selectInvestigations);
		addUnmarkedStep(specifyReportingOptions);
		addUnmarkedStep(showPreview);	
	}
	
	/**
	 * Select comparison area.
	 */
	private void selectComparisonArea() {
		
		addFirstMarkedStep(selectStudy);
		addMarkedStep(describeStudy);
		addMarkedStep(selectStudyArea);
		addMarkedStep(selectComparisonArea);
		addUnmarkedStep(selectInvestigations);
		addUnmarkedStep(specifyReportingOptions);
		addUnmarkedStep(showPreview);		
	}
	
	/**
	 * Select investigation.
	 */
	private void selectInvestigation() {
		
		addFirstMarkedStep(selectStudy);
		addMarkedStep(describeStudy);
		addMarkedStep(selectStudyArea);
		addMarkedStep(selectComparisonArea);
		addMarkedStep(selectInvestigations);
		addUnmarkedStep(specifyReportingOptions);
		addUnmarkedStep(showPreview);		
	}
	
	/**
	 * Select specify reporting options.
	 */
	private void selectSpecifyReportingOptions() {
		
		addFirstMarkedStep(selectStudy);
		addMarkedStep(describeStudy);
		addMarkedStep(selectStudyArea);
		addMarkedStep(selectComparisonArea);
		addMarkedStep(selectInvestigations);
		addMarkedStep(specifyReportingOptions);
		addUnmarkedStep(showPreview);		
	}
	
	/**
	 * Show preview.
	 */
	private void showPreview() {
		
		addFirstMarkedStep(selectStudy);
		addMarkedStep(describeStudy);
		addMarkedStep(selectStudyArea);
		addMarkedStep(selectComparisonArea);
		addMarkedStep(selectInvestigations);
		addMarkedStep(specifyReportingOptions);
		addMarkedStep(showPreview);		
	}
	
	/**
	 * Adds the first marked step.
	 *
	 * @param workFlowStep the work flow step
	 */
	private void addFirstMarkedStep(
		WorkFlowStep workFlowStep) {
		
		panel.removeAll();
		panelGC.gridx = 0;
		workFlowStep.setSelected(true);
		panel.add(workFlowStep, panelGC);
	}
		
	/**
	 * Adds the marked step.
	 *
	 * @param workFlowStep the work flow step
	 */
	private void addMarkedStep(
		WorkFlowStep workFlowStep) {
		
		panelGC.gridx++;
		
		WorkFlowArrow markedWorkFlowArrow = new WorkFlowArrow();
		markedWorkFlowArrow.setSelected(true);
		panel.add(markedWorkFlowArrow, panelGC);
		
		panelGC.gridx++;
		workFlowStep.setSelected(true);
		panel.add(workFlowStep, panelGC);
	}
	
	/**
	 * Adds the unmarked step.
	 *
	 * @param workFlowStep the work flow step
	 */
	private void addUnmarkedStep(
		WorkFlowStep workFlowStep) {	
		
		panelGC.gridx++;

		WorkFlowArrow unmarkedWorkFlowArrow = new WorkFlowArrow();
		unmarkedWorkFlowArrow.setSelected(false);
		panel.add(unmarkedWorkFlowArrow, panelGC);		
		panelGC.gridx++;
		workFlowStep.setSelected(false);
		panel.add(workFlowStep, panelGC);		
	}

// ==========================================
// Section Errors and Validation
// ==========================================

// ==========================================
// Section Interfaces
// ==========================================

	
// ==========================================
// Section Override
// ==========================================




}
