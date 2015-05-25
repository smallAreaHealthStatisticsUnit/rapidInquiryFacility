package rifJobSubmissionTool.desktop.interactive;

import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;
import rifGenericLibrary.presentationLayer.ListEditingButtonPanel;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFStudySubmissionActivityStep;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.businessConceptLayer.AbstractStudy;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JDialog;
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

final class SpecifyInvestigationsStepPanel 
	extends AbstractStepPanel 
	implements ActionListener {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================	
	
	//Data
	
	//GUI Components
	/** The investigations list panel. */
	private OrderedListPanel investigationsListPanel;	
	/** The button panel. */
	private ListEditingButtonPanel buttonPanel;

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new specify investigations step panel.
	 *
	 * @param parentDialog the parent dialog
	 * @param rifSession the rif session
	 */
	public SpecifyInvestigationsStepPanel(
		JDialog parentDialog, 
		RIFStudySubmissionToolSession rifSession) {
		
		super(parentDialog, rifSession);
		
		String listTitle
			= RIFServiceMessages.getMessage("investigation.plural.label");
		String listToolTipText
			= RIFServiceMessages.getMessage("investigation.plural.toolTip");
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		investigationsListPanel 
			= new OrderedListPanel(
				listTitle,
				listToolTipText,
				userInterfaceFactory,
				false);
		buttonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		
		buttonPanel.includeAddButton(null);
		buttonPanel.includeEditButton(null);
		buttonPanel.includeCopyButton(null);
		buttonPanel.includeDeleteButton(null);
		buttonPanel.addActionListener(this);
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
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		
		//Add the title of the panel
		String panelTitleLabelText
			= RIFJobSubmissionToolMessages.getMessage("specifyInvestigationsStepPanel.title");
		int stepNumber
			= RIFStudySubmissionActivityStep.SPECIFY_INVESTIGATIONS.getStepNumber() + 1;
		setPanelTitleInformation(stepNumber, panelTitleLabelText);
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(getTitleLabel(), panelGC);
		
		panelGC.gridy++;
		StringBuilder instructionsText = new StringBuilder();
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyInvestigationsStepPanel.instructions1"));
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyInvestigationsStepPanel.instructions2"));
		panel.add(createInstructionPanel(instructionsText.toString()), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		panel.add(investigationsListPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panel.add(buttonPanel.getPanel(), panelGC);
		
		if (investigationsListPanel.isEmpty()) {
			buttonPanel.indicateEmptyState();
		}
		else {
			buttonPanel.indicatePopulatedState();
		}
		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Adds the investigation.
	 */
	private void addInvestigation() {
		
		Investigation investigation
			= Investigation.newInstance();
		InvestigationEditorDialog investigationEditorDialog
			= new InvestigationEditorDialog(getRIFSession());
		investigationEditorDialog.setInvestigation(investigation);
		investigationEditorDialog.show();
		if (investigationEditorDialog.isCancelled() == false) {
			investigation
				= investigationEditorDialog.getInvestigation();
			investigationsListPanel.addListItem(investigation);
			investigationsListPanel.setSelectedItem(investigation);
			investigationsListPanel.updateUI();
		}
		
		updateButtonStates();
	}
	
	/**
	 * Edits the investigation.
	 */
	private void editInvestigation() {
		
		Investigation currentlySelectedInvestigation
			= (Investigation) investigationsListPanel.getSelectedItem();
		InvestigationEditorDialog investigationEditorDialog
			= new InvestigationEditorDialog(getRIFSession());
		investigationEditorDialog.setInvestigation(currentlySelectedInvestigation);
		investigationEditorDialog.show();
		if (investigationEditorDialog.isCancelled() == false) {
			//refresh list to reflect changes
			investigationsListPanel.updateUI();
		}
		updateButtonStates();	
	}
	
	/**
	 * Copy investigation.
	 */
	private void copyInvestigation() {
		
		Investigation currentlySelectedInvestigation
			= (Investigation) investigationsListPanel.getSelectedItem();
		Investigation cloneInvestigation
			= Investigation.createCopy(currentlySelectedInvestigation);
		makeInvestigationTitleUnique(cloneInvestigation);
		investigationsListPanel.addListItem(cloneInvestigation);
		updateButtonStates();
	}
	
	/**
	 * Make investigation title unique.
	 *
	 * @param investigationToCopy the investigation to copy
	 */
	private void makeInvestigationTitleUnique(
		Investigation investigationToCopy) {

		//Obtain a list of unique titles from the current list of 
		//investigations for the study
		ArrayList<DisplayableListItemInterface> listItems
			= investigationsListPanel.getAllItems();
		ArrayList<String> investigationTitles = new ArrayList<String>();
		for (DisplayableListItemInterface listItem : listItems) {
			Investigation investigation = (Investigation) listItem;
			investigationTitles.add(investigation.getTitle());
		}
		
		//We will attempt to generate names that resemble:
		//<baseTitle>-N where n is the ith copy
		String baseTitle = investigationToCopy.getTitle();
		String copyTitle = null;
		int copyCounter = 2;
		while (copyTitle == null) {
			StringBuilder candidateTitle = new StringBuilder();
			candidateTitle.append(baseTitle);
			candidateTitle.append("-");
			candidateTitle.append(String.valueOf(copyCounter));
			
			if (investigationTitles.contains(candidateTitle.toString()) == false) {
				//we have found a title that won't clash with 
				//the titles of other investigations in the list
				copyTitle = candidateTitle.toString();
				break;
			}
			copyCounter++;
		}
		
		investigationToCopy.setTitle(copyTitle);		
		updateButtonStates();
	}
	
	/**
	 * Delete investigation.
	 */
	private void deleteInvestigation() {
		
		investigationsListPanel.deleteSelectedListItems();
		updateButtonStates();
	}

	/**
	 * Update button states.
	 */
	private void updateButtonStates() {
		
		if (investigationsListPanel.getNumberOfItems() == 0) {
			buttonPanel.indicateEmptyState();
		}
		else {
			buttonPanel.indicatePopulatedState();			
		}
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	/**
	 * Validate form.
	 *
	 * @throws RIFJobSubmissionToolException the RIF job submission tool exception
	 */
	private void validateForm() 
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();
		
		ArrayList<DisplayableListItemInterface> listItems
			= investigationsListPanel.getAllItems();
		if (listItems.size() == 0) {
			String errorMessage
				= RIFJobSubmissionToolMessages.getMessage(
					"specifyInvestigationsStepPanel.error.noInvestigationsSpecified");
			errorMessages.add(errorMessage);
		}
		else {			
			ArrayList<Investigation> investigations
				= new ArrayList<Investigation>();
			for (DisplayableListItemInterface listItem : listItems) {
				Investigation workingCopyInvestigation
					= (Investigation) listItem;
				try {
					workingCopyInvestigation.checkErrors(ValidationPolicy.STRICT);
					investigations.add(workingCopyInvestigation);
				}
				catch(RIFServiceException rifServiceException) {
					errorMessages.addAll(rifServiceException.getErrorMessages());
				}
			}
			String denominatorTableResult
				= AbstractStudy.identifyDifferentDenominators(investigations);
			if (denominatorTableResult != null) {
				errorMessages.add(denominatorTableResult);
			}
		}
		
		if (errorMessages.size() > 0) {
			RIFServiceException rifJobSubmissionToolException
				= new RIFServiceException(errorMessages);
			throw rifJobSubmissionToolException;
		}
	}
		
	// ==========================================
	// Section Interfaces
	// ==========================================
	
	//Interface: Action Listener

	public void actionPerformed(
		ActionEvent event) {

		Object source = event.getSource();
		
		if (buttonPanel.isAddButton(source)) {
			addInvestigation();
		}
		else if (buttonPanel.isEditButton(source)) {
			editInvestigation();
		}
		else if (buttonPanel.isCopyButton(source)) {
			copyInvestigation();
		}
		else if (buttonPanel.isDeleteButton(source)) {
			deleteInvestigation();
		}		
	}
		
	// ==========================================
	// Section Override
	// ==========================================


	@Override
	public void initialiseForm() 
		throws RIFServiceException {

		RIFStudySubmissionToolSession rifSession = getRIFSession();
		
		RIFStudySubmission originalJobSubmission
			= rifSession.getRIFJobSubmission();
		DiseaseMappingStudy originalStudy
			= (DiseaseMappingStudy) originalJobSubmission.getStudy();
		ArrayList<Investigation> originalInvestigations
			= originalStudy.getInvestigations();
		
		investigationsListPanel.clearList();
		ArrayList<Investigation> workingCopyInvestigations
			= Investigation.createCopy(originalInvestigations);
		for (Investigation workingCopyInvestigation : workingCopyInvestigations) {
			investigationsListPanel.addListItem(workingCopyInvestigation);			
		}
		
		investigationsListPanel.selectFirstItem();
		buildUI();
	}


	@Override
	public void commitChanges() 
		throws RIFServiceException {

		validateForm();
		
		ArrayList<DisplayableListItemInterface> listItems
			= investigationsListPanel.getAllItems();
		ArrayList<Investigation> workingCopyInvestigations
			= new ArrayList<Investigation>();
		for (DisplayableListItemInterface listItem : listItems) {
			Investigation workingCopyInvestigation
				= (Investigation) listItem;
			workingCopyInvestigations.add(workingCopyInvestigation);
		}

		RIFStudySubmissionToolSession rifSession = getRIFSession();
		RIFStudySubmission originalJobSubmission
			= rifSession.getRIFJobSubmission();
		DiseaseMappingStudy originalStudy
			= (DiseaseMappingStudy) originalJobSubmission.getStudy();
		originalStudy.setInvestigations(workingCopyInvestigations);

		rifSession.addCommittedActivityStep(RIFStudySubmissionActivityStep.SPECIFY_INVESTIGATIONS);
	}

}
