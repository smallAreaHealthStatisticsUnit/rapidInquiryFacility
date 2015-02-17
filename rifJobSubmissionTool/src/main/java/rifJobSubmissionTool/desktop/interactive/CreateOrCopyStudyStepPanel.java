package rifJobSubmissionTool.desktop.interactive;

import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFStudySubmissionActivityStep;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifServices.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractStudy;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.Project;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


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

final class CreateOrCopyStudyStepPanel 
	extends AbstractStepPanel 
	implements ActionListener{

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	//Data

	//GUI Components
	/** The project selection panel. */
	private ProjectSelectionPanel projectSelectionPanel;
	/** The available studies list panel. */
	private OrderedListPanel availableStudiesListPanel;	
	/** The create study radio button. */
	private JRadioButton createStudyRadioButton;	
	/** The copy existing study radio button. */
	private JRadioButton copyExistingStudyRadioButton;
		
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new creates the or copy study step panel.
	 *
	 * @param parentDialog the parent dialog
	 * @param rifSession the rif session
	 */
	public CreateOrCopyStudyStepPanel(
		JDialog parentDialog,
		RIFStudySubmissionToolSession rifSession) {
		
		super(parentDialog, rifSession);
		
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		ButtonGroup buttonGroup = userInterfaceFactory.createButtonGroup();
		String createStudyButtonText
			= RIFJobSubmissionToolMessages.getMessage("createOrCopyStudyStepPanel.createStudy.button.label");
		createStudyRadioButton
			= userInterfaceFactory.createRadioButton(createStudyButtonText);		
		createStudyRadioButton.addActionListener(this);
		buttonGroup.add(createStudyRadioButton);
		String copyExistingStudyText
			= RIFJobSubmissionToolMessages.getMessage("createOrCopyStudyStepPanel.copyStudy.button.label");
		copyExistingStudyRadioButton
			= userInterfaceFactory.createRadioButton(copyExistingStudyText);
		copyExistingStudyRadioButton.addActionListener(this);
		buttonGroup.add(copyExistingStudyRadioButton);
		
		projectSelectionPanel = new ProjectSelectionPanel(rifSession);	
		projectSelectionPanel.addActionListener(this);

		String availableStudiesListText
			= RIFJobSubmissionToolMessages.getMessage("createOrCopyStudyStepPanel.availableStudies.label");
		String availableStudiesToolTip
			= RIFJobSubmissionToolMessages.getMessage("createOrCopyStudyStepPanel.availableStudies.toolTip");		
		availableStudiesListPanel
			= new OrderedListPanel(
				availableStudiesListText,
				availableStudiesToolTip,
				userInterfaceFactory,
				false);	
	}
	
	/**
	 * Builds the ui.
	 */
	private void buildUI() {
		
		JPanel panel = getPanel();
		panel.removeAll();

		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		//Add the title of the panel
		String panelTitleLabelText
			= RIFJobSubmissionToolMessages.getMessage("createOrCopyStudyStepPanel.title");
		int stepNumber
			= RIFStudySubmissionActivityStep.CREATE_OR_COPY_STUDY_STEP.getStepNumber() + 1;
		setPanelTitleInformation(stepNumber, panelTitleLabelText);
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(getTitleLabel(), panelGC);
		
		panelGC.gridy++;
		StringBuilder instructionsText = new StringBuilder();
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("createOrCopyStudyStepPanel.instructions1"));
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("createOrCopyStudyStepPanel.instructions2"));
		panel.add(createInstructionPanel(instructionsText.toString()), panelGC);
		
		panelGC.gridy++;
		panel.add(createStudyRadioButton, panelGC);
				
		panelGC.gridy++;
		panel.add(copyExistingStudyRadioButton, panelGC);
		
		panelGC.gridy++;		
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		panelGC.insets = new Insets(0, 20, 0, 0);		
		panel.add(createCopyExistingStudyPanel(), panelGC);
	}
	
	/**
	 * Creates the copy existing study panel.
	 *
	 * @return the j panel
	 */
	private JPanel createCopyExistingStudyPanel() {
		
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 0.3;
		panel.add(projectSelectionPanel.getPanel(), panelGC);
				
		panelGC.gridy++;
		panelGC.weighty = 0.7;
		panel.add(availableStudiesListPanel.getPanel(), panelGC);
		return panel;
	}
	
	
	/**
	 * Sets the enable copy existing study panel.
	 *
	 * @param isEnabled the new enable copy existing study panel
	 */
	private void setEnableCopyExistingStudyPanel(
		boolean isEnabled) {
		
		projectSelectionPanel.setEnabled(isEnabled);
		availableStudiesListPanel.setEnabled(isEnabled);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Update available studies list panel.
	 */
	private void updateAvailableStudiesListPanel() {
		
		Project selectedProject
			= projectSelectionPanel.getSelectedProject();
		if (selectedProject == null) {
			//This means the project selection panel has
			//been cleared and there are no choices.
			//Therefore, the response from the available studies
			//list panel should be to clear itself
			availableStudiesListPanel.setEnabled(false);
			return;
		}
		
		//User has changed their selection of the current project
		try {
			RIFStudySubmissionToolSession rifSession = getRIFSession();
			RIFStudySubmissionAPI service 
				= rifSession.getRIFStudySubmissionService();
			User currentUser = rifSession.getUser();
			ArrayList<AbstractStudy> studies
				= service.getStudies(currentUser, selectedProject);
			availableStudiesListPanel.clearList();
			for (AbstractStudy study : studies) {
				availableStudiesListPanel.addListItem(study);				
			}
			availableStudiesListPanel.selectFirstItem();
			availableStudiesListPanel.updateUI();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getParentDialog(), 
				rifServiceException.getErrorMessages());
		}
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Action Listener
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(
		ActionEvent event) {
		
		Object source = event.getSource();
		
		if (source == createStudyRadioButton) {
			setEnableCopyExistingStudyPanel(false);
		}
		else if (source == copyExistingStudyRadioButton) {			
			setEnableCopyExistingStudyPanel(true);
		}
		else if (projectSelectionPanel.isComboBox(source)) {
			updateAvailableStudiesListPanel();
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

	/* (non-Javadoc)
	 * @see rifJobSubmissionTool.desktopApplication.AbstractStepPanel#initialiseForm()
	 */
	@Override
	public void initialiseForm() {
		
		//always initialise the form to show create a study,
		//initially desensitise lower portion that lets user choose
		//which study they want to copy
		createStudyRadioButton.setSelected(true);			
		setEnableCopyExistingStudyPanel(false);		
		buildUI();		
	}

	/**
	 * Validate form.
	 *
	 * @throws RIFJobSubmissionToolException the RIF job submission tool exception
	 */
	private void validateForm() 
		throws RIFServiceException {
		
		//if the user has chosen to copy a study then some study must
		//appear selected.  We have tried to ensure the first study in the
		//list is selected by default.  However, the list may be empty
		//if there are no available studies.
		if (copyExistingStudyRadioButton.isSelected() &&
			availableStudiesListPanel.noItemsSelected()) {
			
			String errorMessage
				= RIFJobSubmissionToolMessages.getMessage(
					"createOrCopyStudyStepPanel.error.noExistingStudySelected");
			
			RIFServiceException rifJobSubmissionToolException
				= new RIFServiceException(errorMessage);
			throw rifJobSubmissionToolException;
		}
	}
	
	/* (non-Javadoc)
	 * @see rifJobSubmissionTool.desktopApplication.AbstractStepPanel#commitChanges()
	 */
	@Override
	public void commitChanges() 
		throws RIFServiceException {
		
		validateForm();

		RIFStudySubmissionToolSession rifSession = getRIFSession();
		RIFStudySubmission currentRIFJobSubmission
			= rifSession.getRIFJobSubmission();
		
		if (copyExistingStudyRadioButton.isSelected()) {
			DiseaseMappingStudy selectedDiseaseMappingStudy
				= (DiseaseMappingStudy) availableStudiesListPanel.getSelectedItem();
			DiseaseMappingStudy cloneSelectedDiseaseMappingStudy
				= DiseaseMappingStudy.createCopy(selectedDiseaseMappingStudy);
			rifSession.setCurrentDiseaseMappingStudy(cloneSelectedDiseaseMappingStudy);
		}
		else {
			DiseaseMappingStudy blankDiseaseMappingStudy
				= DiseaseMappingStudy.newInstance();
			rifSession.setCurrentDiseaseMappingStudy(blankDiseaseMappingStudy);
		}
		currentRIFJobSubmission.setNewRecord(false);
		rifSession.addCommittedActivityStep(RIFStudySubmissionActivityStep.CREATE_OR_COPY_STUDY_STEP);		
	}
}

