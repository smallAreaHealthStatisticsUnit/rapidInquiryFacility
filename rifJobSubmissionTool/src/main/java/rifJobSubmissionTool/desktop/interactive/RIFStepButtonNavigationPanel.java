package rifJobSubmissionTool.desktop.interactive;

import rifGenericUILibrary.ErrorDialog;
import rifGenericUILibrary.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFStudySubmissionActivityStep;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;


import rifGenericUILibrary.WorkflowNavigationButtonPanel;


import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;
import rifServices.fileFormats.RIFZFileFilter;
import rifServices.system.RIFServiceException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;



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

class RIFStepButtonNavigationPanel 
	extends Observable 
	implements ActionListener {
	
	// ==========================================
	// Section Constants
	// ==========================================
	
	//===========================================
	// Section Properties
	// ==========================================
	
	//Data
	/** The rif session. */
	private RIFStudySubmissionToolSession rifSession;
	/** The rif activity state machine. */
	private RIFStudySubmissionActivityStateMachine rifActivityStateMachine;

	private WorkflowNavigationButtonPanel navigationButtonPanel;
	
	//GUI Components
	/** The parent dialog. */
	private JDialog parentDialog;
	/** The current step panel. */
	private AbstractStepPanel currentStepPanel;
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;	
	/** The panel. */
	private JPanel panel;
	
	private JButton startAgainButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new RIF step button navigation panel.
	 *
	 * @param rifSession the rif session
	 * @param parentDialog the parent dialog
	 * @param rifActivityStateMachine the rif activity state machine
	 */
	public RIFStepButtonNavigationPanel(
		RIFStudySubmissionToolSession rifSession,
		JDialog parentDialog,
		RIFStudySubmissionActivityStateMachine rifActivityStateMachine) {
		
		this.rifSession = rifSession;
		this.userInterfaceFactory = rifSession.getUIFactory();
		this.parentDialog = parentDialog;
		this.rifActivityStateMachine = rifActivityStateMachine;
		
		panel = userInterfaceFactory.createBorderLayoutPanel();
		navigationButtonPanel 
			= new WorkflowNavigationButtonPanel(
				userInterfaceFactory);
		String startAgainButtonText
			= RIFJobSubmissionToolMessages.getMessage("buttons.startAgain.label");
		startAgainButton
			= userInterfaceFactory.createButton(startAgainButtonText);
		navigationButtonPanel.startAgainButton(startAgainButton);
		
		navigationButtonPanel.addActionListener(this);
		navigationButtonPanel.showStartState();
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
		
		return navigationButtonPanel.getPanel();
	}

	/**
	 * Start again.
	 */
	private void startAgain() {
		
		rifActivityStateMachine.firstActivityStep();
	}
	
	/**
	 * Back.
	 */
	private void back() {
		rifActivityStateMachine.previousActivityStep();
	}
	
	/**
	 * Next.
	 */
	private void next() {
		
		try {
			currentStepPanel.commitChanges();
			rifActivityStateMachine.nextActivityStep();
		}
		catch(RIFServiceException rifJobSubmissionToolException) {
			ErrorDialog.showError(
				parentDialog, 
				rifJobSubmissionToolException.getErrorMessages());
		}		
	}
	
	/**
	 * Submit.
	 */
	private void submit() {
		
		try {
			currentStepPanel.commitChanges();		
			
			JFileChooser fileChooser
				= userInterfaceFactory.createFileChooser();
			fileChooser.setFileFilter(new RIFZFileFilter());
			
			int result = fileChooser.showSaveDialog(parentDialog);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}

			User currentUser
				= rifSession.getUser();
			RIFStudySubmission currentRIFJobSubmission
				= rifSession.getRIFJobSubmission();
			RIFStudySubmissionAPI service = rifSession.getRIFStudySubmissionService();
						
			File selectedFile 
				= RIFZFileFilter.ensureFileNameEndsWithExtension(fileChooser.getSelectedFile());
			
			service.submitStudy(currentUser, currentRIFJobSubmission, selectedFile);
			currentRIFJobSubmission
				= RIFStudySubmission.newInstance();
			rifSession.setCurrentRIFSubmission(currentRIFJobSubmission);
			rifActivityStateMachine.firstActivityStep();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				parentDialog, 
				rifServiceException.getErrorMessages());			
		}
	}
	
	/**
	 * Quit.
	 */
	private void quit() {
		
		RIFSubmissionToolShutdownManager shutDownManager
			= new RIFSubmissionToolShutdownManager(parentDialog, rifSession);
		shutDownManager.shutDown();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================
	
	//Interface: Observer
	
	//updating in reaction to a change in the state machine
	/**
	 * Sets the current activity step.
	 *
	 * @param currentRIFActivityStep the current rif activity step
	 * @param currentStepPanel the current step panel
	 */
	public void setCurrentActivityStep(
		RIFStudySubmissionActivityStep currentRIFActivityStep,
		AbstractStepPanel currentStepPanel) {
		
		this.currentStepPanel = currentStepPanel;
		
		if (currentRIFActivityStep == RIFStudySubmissionActivityStep.CREATE_OR_COPY_STUDY_STEP) {
			navigationButtonPanel.showStartState();
		}
		else if (currentRIFActivityStep == RIFStudySubmissionActivityStep.DESCRIBE_STUDY) {
			navigationButtonPanel.showMiddleState();
		}
		else if (currentRIFActivityStep == RIFStudySubmissionActivityStep.CHOOSE_STUDY_AREA) {
			navigationButtonPanel.showMiddleState();
		}
		else if (currentRIFActivityStep == RIFStudySubmissionActivityStep.CHOOSE_COMPARISON_AREA) {
			navigationButtonPanel.showMiddleState();
		}
		else if (currentRIFActivityStep == RIFStudySubmissionActivityStep.SPECIFY_INVESTIGATIONS) {
			navigationButtonPanel.showMiddleState();
		}
		else if (currentRIFActivityStep == RIFStudySubmissionActivityStep.SPECIFY_REPORTS) {
			navigationButtonPanel.showMiddleState();
		}
		else if (currentRIFActivityStep == RIFStudySubmissionActivityStep.PREVIEW) {
			navigationButtonPanel.showEndState();
		}	
		else {
			assert false;
		}
	}
	
	//Interface: Action Listener
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(
		ActionEvent event) {

		Object button = event.getSource();
		
		if (navigationButtonPanel.isStartAgainButton(button)) {
			startAgain();
		}
		else if (navigationButtonPanel.isPreviousButton(button)) {
			back();
		}
		else if (navigationButtonPanel.isNextButton(button)) {
			next();
		}
		else if (navigationButtonPanel.isQuitButton(button)) {
			quit();
		}	
		else if (navigationButtonPanel.isSubmitButton(button)) {
			submit();
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}

