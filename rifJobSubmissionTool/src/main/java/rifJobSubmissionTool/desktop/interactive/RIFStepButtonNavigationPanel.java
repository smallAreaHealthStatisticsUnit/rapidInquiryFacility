package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.system.RIFActivityStep;

import rifJobSubmissionTool.system.RIFJobSubmissionToolException;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFSession;
import rifJobSubmissionTool.util.UserInterfaceFactory;

import rifServices.businessConceptLayer.RIFJobSubmission;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;
import rifServices.io.RIFZFileFilter;
import rifServices.system.RIFServiceException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
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

public class RIFStepButtonNavigationPanel 
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
	private RIFSession rifSession;
	/** The rif activity state machine. */
	private RIFActivityStateMachine rifActivityStateMachine;
	/** The include start again button. */
	private boolean includeStartAgainButton;
	/** The include back button. */
	private boolean includeBackButton;	
	/** The include next button. */
	private boolean includeNextButton;
	/** The include submit button. */
	private boolean includeSubmitButton;
	/** The include quit button. */
	private boolean includeQuitButton;
	
	//GUI Components
	/** The parent dialog. */
	private JDialog parentDialog;
	/** The current step panel. */
	private AbstractStepPanel currentStepPanel;
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;	
	/** The panel. */
	private JPanel panel;
	/** The start again button. */
	private JButton startAgainButton;
	/** The back button. */
	private JButton backButton;
	/** The next button. */
	private JButton nextButton;
	/** The submit button. */
	private JButton submitButton;
	/** The quit button. */
	private JButton quitButton;
	
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
		RIFSession rifSession,
		JDialog parentDialog,
		RIFActivityStateMachine rifActivityStateMachine) {
		
		this.rifSession = rifSession;
		this.userInterfaceFactory = rifSession.getUIFactory();
		this.parentDialog = parentDialog;
		this.rifActivityStateMachine = rifActivityStateMachine;
		
		panel = userInterfaceFactory.createBorderLayoutPanel();
		String startAgainButtonText
			= RIFJobSubmissionToolMessages.getMessage("general.button.startAgain.label");
		startAgainButton
			= userInterfaceFactory.createButton(startAgainButtonText);
		startAgainButton.addActionListener(this);

		String backButtonText
			= RIFJobSubmissionToolMessages.getMessage("general.button.back.label");
		backButton
			= userInterfaceFactory.createButton(backButtonText);
		backButton.addActionListener(this);
		
		String nextButtonText
			= RIFJobSubmissionToolMessages.getMessage("general.buttons.next.label");
		nextButton
			= userInterfaceFactory.createButton(nextButtonText);
		nextButton.addActionListener(this);
		
		String submitButtonText
			= RIFJobSubmissionToolMessages.getMessage("general.button.submit.label");
		submitButton
			= userInterfaceFactory.createButton(submitButtonText);
		submitButton.setBackground(Color.RED);
		submitButton.addActionListener(this);
		
		String quitButtonText
			= RIFJobSubmissionToolMessages.getMessage("general.button.quit.label");
		quitButton
			= userInterfaceFactory.createButton(quitButtonText);
		quitButton.addActionListener(this);
		
		includeStartAgainButton = false;
		includeBackButton = false;
		includeNextButton = true;
		includeSubmitButton = false;
		includeQuitButton = true;
		rebuildUI();		
	}

	/**
	 * Rebuild ui.
	 */
	public void rebuildUI() {
		
		panel.removeAll();
		
		JPanel leftPanel = userInterfaceFactory.createPanel();	
		GridBagConstraints leftPanelGC 
			= userInterfaceFactory.createGridBagConstraints();
		leftPanelGC.anchor = GridBagConstraints.SOUTHEAST;
		if (includeStartAgainButton == true) {
			leftPanelGC.anchor = GridBagConstraints.SOUTHWEST;
			leftPanel.add(startAgainButton, leftPanelGC);
		}		
		panel.add(leftPanel, BorderLayout.WEST);
		
		//show the other buttons flushed right on the screen
		JPanel rightPanel = userInterfaceFactory.createPanel();	
		GridBagConstraints rightPanelGC 
			= userInterfaceFactory.createGridBagConstraints();
		rightPanelGC.anchor = GridBagConstraints.SOUTHEAST;		
		if (includeBackButton == true) {
			rightPanel.add(backButton, rightPanelGC);
			rightPanelGC.gridx++;
		}		
		if (includeNextButton == true) {
			rightPanel.add(nextButton, rightPanelGC);
			rightPanelGC.gridx++;
		}		
		
		if (includeSubmitButton == true) {
			rightPanel.add(submitButton, rightPanelGC);
		}
		
		if (includeQuitButton == true) {
			rightPanel.add(quitButton, rightPanelGC);			
		}
		panel.add(rightPanel, BorderLayout.EAST);
		
		panel.updateUI();
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
	 * Start again.
	 */
	public void startAgain() {
		
		rifActivityStateMachine.firstActivityStep();
	}
	
	/**
	 * Back.
	 */
	public void back() {
		rifActivityStateMachine.previousActivityStep();
	}
	
	/**
	 * Next.
	 */
	public void next() {
		
		try {
			currentStepPanel.commitChanges();
			rifActivityStateMachine.nextActivityStep();
		}
		catch(RIFJobSubmissionToolException rifJobSubmissionToolException) {
			ErrorDialog.showError(parentDialog, rifJobSubmissionToolException);
		}		
	}
	
	/**
	 * Submit.
	 */
	public void submit() {
		
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
			RIFJobSubmission currentRIFJobSubmission
				= rifSession.getRIFJobSubmission();
			RIFStudySubmissionAPI service = rifSession.getRIFStudySubmissionService();
						
			File selectedFile 
				= RIFZFileFilter.ensureFileNameEndsWithExtension(fileChooser.getSelectedFile());
			
			service.submitStudy(currentUser, currentRIFJobSubmission, selectedFile);
			currentRIFJobSubmission
				= RIFJobSubmission.newInstance();
			rifSession.setCurrentRIFSubmission(currentRIFJobSubmission);
			rifActivityStateMachine.firstActivityStep();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);			
		}
		catch(RIFJobSubmissionToolException rifJobSubmissionToolException) {
			ErrorDialog.showError(parentDialog, rifJobSubmissionToolException);			
		}
	}
	
	/**
	 * Quit.
	 */
	public void quit() {
		
		ShutdownManager shutDownManager
			= new ShutdownManager(parentDialog, rifSession);
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
		RIFActivityStep currentRIFActivityStep,
		AbstractStepPanel currentStepPanel) {
		
		this.currentStepPanel = currentStepPanel;
		
		if (currentRIFActivityStep == RIFActivityStep.CREATE_OR_COPY_STUDY_STEP) {
			includeStartAgainButton = false;
			includeBackButton = false;
			includeNextButton = true;
			includeQuitButton = true;
			rebuildUI();
		}
		else if (currentRIFActivityStep == RIFActivityStep.DESCRIBE_STUDY) {
			includeStartAgainButton = true;
			includeBackButton = false;
			includeNextButton = true;
			includeQuitButton = true;
			rebuildUI();			
		}
		else if (currentRIFActivityStep == RIFActivityStep.CHOOSE_STUDY_AREA) {
			includeStartAgainButton = true;
			includeBackButton = true;
			includeNextButton = true;
			includeQuitButton = true;
			rebuildUI();	
		}
		else if (currentRIFActivityStep == RIFActivityStep.CHOOSE_COMPARISON_AREA) {
			includeStartAgainButton = true;
			includeBackButton = true;
			includeNextButton = true;
			includeQuitButton = true;
			rebuildUI();
		}
		else if (currentRIFActivityStep == RIFActivityStep.SPECIFY_INVESTIGATIONS) {
			includeStartAgainButton = true;
			includeBackButton = true;
			includeNextButton = true;
			includeQuitButton = true;
			rebuildUI();		
		}
		else if (currentRIFActivityStep == RIFActivityStep.SPECIFY_REPORTS) {
			includeStartAgainButton = true;
			includeBackButton = true;
			includeNextButton = true;
			includeQuitButton = true;
			rebuildUI();			
		}
		else if (currentRIFActivityStep == RIFActivityStep.PREVIEW) {
			includeStartAgainButton = true;
			includeBackButton = true;
			includeNextButton = false;
			includeSubmitButton = true;
			includeQuitButton = true;
			rebuildUI();
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
		
		if (button == startAgainButton) {
			startAgain();
		}
		else if (button == backButton) {
			back();
		}
		else if (button == nextButton) {
			next();
		}
		else if (button == quitButton) {
			quit();
		}	
		else if (button == submitButton) {
			submit();
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}

