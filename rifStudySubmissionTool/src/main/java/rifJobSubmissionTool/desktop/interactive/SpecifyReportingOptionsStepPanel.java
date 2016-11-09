package rifJobSubmissionTool.desktop.interactive;


import rifGenericLibrary.presentationLayer.ListEditingButtonPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFStudySubmissionActivityStep;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.businessConceptLayer.CalculationMethod;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.RIFOutputOption;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.border.LineBorder;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;


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

final class SpecifyReportingOptionsStepPanel 
	extends AbstractStepPanel
	implements ActionListener, 
	MouseListener {
	
// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	//Data
	
	//GUI Components
	/** The calculation method table. */
	private CalculationMethodTable calculationMethodTable;	
	/** The add edit delete panel. */
	private ListEditingButtonPanel addEditDeletePanel;
	/** The check box from output option name. */
	private HashMap<String, JCheckBox> checkBoxFromOutputOptionName;
		
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new specify reporting options step panel.
     *
     * @param parentDialog the parent dialog
     * @param rifSession the rif session
     */
	public SpecifyReportingOptionsStepPanel(
    	JDialog parentDialog, 
    	RIFStudySubmissionToolSession rifSession) {
    	super(parentDialog, rifSession);
		
		checkBoxFromOutputOptionName
			= new HashMap<String, JCheckBox>();
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		
		ArrayList<RIFOutputOption> rifOutputOptions
			= RIFOutputOption.getAllAvailableRIFOutputOptions();
		for (RIFOutputOption rifOutputOption : rifOutputOptions) {
			String rifOptionLabelText = rifOutputOption.getName();
			JCheckBox checkBox 
				= userInterfaceFactory.createCheckBox(rifOptionLabelText);
			checkBoxFromOutputOptionName.put(rifOptionLabelText, checkBox);			
		}
		
		calculationMethodTable
			= new CalculationMethodTable(userInterfaceFactory);
		addEditDeletePanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		addEditDeletePanel.includeAddButton(null);
		addEditDeletePanel.includeEditButton(null);
		addEditDeletePanel.includeDeleteButton(null);		
		addEditDeletePanel.addActionListener(this);
	}
    
    /**
     * Builds the ui.
     */
    private void buildUI() {
    	
    	JPanel panel = getPanel();
    	panel.removeAll();
    	UserInterfaceFactory userInterfaceFactory
    		= getUserInterfaceFactory();
    			
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		
		panel.add(createTitlePanel(), panelGC);
		panelGC.gridy++;
				
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1.0;
		
		panel.add(createPluginSelectionPanel(), panelGC);
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;		
		panel.add(createDataTypeOptions(), panelGC);
		
		ensureDefaultListItemSelected();
		calculationMethodTable.addMouseListener(this);   	
    }

	/**
	 * Creates the title panel.
	 *
	 * @return the j panel
	 */
	private JPanel createTitlePanel() {
		
    	UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;

		String titleText
			= RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.title");
		JLabel titleLabel
			= userInterfaceFactory.createLabel(titleText);
		String titleToolTipText
			= RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.title.toolTip");
		titleLabel.setToolTipText(titleToolTipText);
		panel.add(titleLabel, panelGC);
		
		panelGC.gridy++;
		StringBuilder instructionsText = new StringBuilder();
		instructionsText.append("<html>");		
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.instructions1"));
		instructionsText.append(" ");
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.instructions2"));
		instructionsText.append(" ");
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.instructions3"));
		instructionsText.append(" ");
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.instructions4"));
		instructionsText.append(" ");
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.instructions5"));
		instructionsText.append("</html>");		
		JLabel instructionsLabel
			= userInterfaceFactory.createInstructionLabel(instructionsText.toString());
		panel.add(instructionsLabel, panelGC);
		
		return panel;
	}
	
	/**
	 * Creates the plugin selection panel.
	 *
	 * @return the j panel
	 */
	private JPanel createPluginSelectionPanel() {
		
    	UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		String pluginListLabelText
			= RIFServiceMessages.getMessage("calculationMethod.plural.label");
		JLabel pluginListLabel
			= userInterfaceFactory.createLabel(pluginListLabelText);
		String pluginListLabelToolTipText
			= RIFServiceMessages.getMessage("calculationMethod.plural.toolTip");
		pluginListLabel.setToolTipText(pluginListLabelToolTipText);

		userInterfaceFactory.setBoldFont(pluginListLabel);
		panel.add(pluginListLabel, panelGC);
		
		panelGC.gridy++;
		StringBuilder instructionsText = new StringBuilder();
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.pluginListPanel.instructions1"));
		instructionsText.append(" ");
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.pluginListPanel.instructions2"));
		instructionsText.append(" ");
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.pluginListPanel.instructions3"));
		instructionsText.append(" ");
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.pluginListPanel.instructions4"));
		
		JLabel pluginListInstructionsLabel
			= userInterfaceFactory.createInstructionLabel(instructionsText.toString());
		panel.add(pluginListInstructionsLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1.0;
		panelGC.weighty = 1.0;		
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(calculationMethodTable);
		panel.add(scrollPane, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(addEditDeletePanel.getPanel(), panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		return panel;
	}
	
	/**
	 * Creates the data type options.
	 *
	 * @return the j panel
	 */
	private JPanel createDataTypeOptions() {
		
    	UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		
		
		String outputOptionsTitleText
			= RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.includeTheseOutputs.label");
		JLabel outputOptionsTitleTextLabel
			= userInterfaceFactory.createLabel(outputOptionsTitleText);
		String outputOptionsTitleToolTipText
			= RIFJobSubmissionToolMessages.getMessage("specifyReportingOptionsStepPanel.includeTheseOutputs.toolTip");
		outputOptionsTitleTextLabel.setToolTipText(outputOptionsTitleToolTipText);
		userInterfaceFactory.setBoldFont(outputOptionsTitleTextLabel);
		panel.add(outputOptionsTitleTextLabel, panelGC);
		panelGC.gridy++;
		
		ArrayList<String> rifOutputOptionNames 
			= RIFOutputOption.getAllAvailableRIFOutputOptionNames();
		
		for (int i = 0; i < rifOutputOptionNames.size(); i++) {
			if (i != 0) {
				panelGC.gridy++;				
			}
			String rifOptionLabelText = rifOutputOptionNames.get(i);
			JCheckBox checkBox 
				= userInterfaceFactory.createCheckBox(rifOptionLabelText);
			checkBoxFromOutputOptionName.put(rifOutputOptionNames.get(i), checkBox);
			panel.add(checkBox, panelGC);			
		}
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		
		return panel;
	}
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
		
	/**
	 * Adds the plugin.
	 */
	private void addPlugin() {
		ArrayList<CalculationMethod> usedCalculationMethods
			= calculationMethodTable.getCalculationMethods();
		
		RIFStudySubmissionToolSession rifSession = getRIFSession();
		CalculationMethodSelectionDialog selectionDialog
			= new CalculationMethodSelectionDialog(
				rifSession, 
				usedCalculationMethods);
		selectionDialog.show();
		if (selectionDialog.isCancelled() == true) {
			return;
		}

		ArrayList<CalculationMethod> selectedCalculationMethods
			= selectionDialog.getSelectedCalculationMethods();
		calculationMethodTable.addCalculationMethods(selectedCalculationMethods);			
		
		ensureDefaultListItemSelected();
		updatePluginListButtonStates();
	}	
	
	/**
	 * Edits the plugin.
	 */
	private void editPlugin() {		
		
		CalculationMethod calculationMethod
			= calculationMethodTable.getSelectedCalculationMethod();
		RIFStudySubmissionToolSession rifSession = getRIFSession();
		CalculationMethodEditorDialog editorDialog
			= new CalculationMethodEditorDialog(rifSession);
		editorDialog.setCalculationMethod(calculationMethod);
										
		editorDialog.show();
		
		if (editorDialog.isCancelled() == true) {
			return;
		}
		
		calculationMethodTable.updateCalculationMethod(calculationMethod);
	}

	/**
	 * Delete plugin.
	 */
	private void deletePlugin() {
		
		calculationMethodTable.deleteSelectedSelectedCalculationMethods();
		ensureDefaultListItemSelected();
		updatePluginListButtonStates();
	}
	
	/**
	 * Update plugin list button states.
	 */
	private void updatePluginListButtonStates() {
		
		int currentNumberOfPlugins = calculationMethodTable.getRowCount();
		if (currentNumberOfPlugins == 0) {
			addEditDeletePanel.indicateEmptyState();
		}
		else {
			addEditDeletePanel.indicatePopulatedState();
		}		
	}
	
	/**
	 * Ensure default list item selected.
	 */
	private void ensureDefaultListItemSelected() {
		
		ListSelectionModel listSelectionModel
			= calculationMethodTable.getSelectionModel();
		int currentLeadIndex
			= listSelectionModel.getLeadSelectionIndex();
		if (currentLeadIndex == -1) {
			//Ensure first item in the list is selected		
			int numberOfRows = calculationMethodTable.getRowCount();
			if (numberOfRows > 0) {
				calculationMethodTable.setRowSelectionInterval(0, 0);
			}
		}
	}
	
	/**
	 * Gets the currently selected rif output options.
	 *
	 * @return the currently selected rif output options
	 */
	private ArrayList<RIFOutputOption> getCurrentlySelectedRIFOutputOptions() {
		
		ArrayList<RIFOutputOption> selectedRIFOutputOptions
			= new ArrayList<RIFOutputOption>();
		
		ArrayList<String> listedOptionNames 
			= new ArrayList<String>();
		listedOptionNames.addAll(checkBoxFromOutputOptionName.keySet());

		for (String listedOptionName : listedOptionNames) {
			JCheckBox checkBox 
				= checkBoxFromOutputOptionName.get(listedOptionName);
			if (checkBox.isSelected()) {
				RIFOutputOption selectedOption
					= RIFOutputOption.getOptionFromPhrase(listedOptionName);		
				selectedRIFOutputOptions.add(selectedOption);
			}
		}
		
		return selectedRIFOutputOptions;
	}
	
// ==========================================
// Section Errors and Validation
// ==========================================
	/**
	 * Validate form.
	 *
	 * @throws RIFJobSubmissionToolException the RIF job submission tool exception
	 */
	public void validateForm() 
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();
		
		ArrayList<CalculationMethod> calculationMethods
			= calculationMethodTable.getCalculationMethods();
	
		for (CalculationMethod calculationMethod : calculationMethods) {
			try {
				calculationMethod.checkErrors(ValidationPolicy.STRICT);				
			}
			catch(RIFServiceException rifServiceException) {
				errorMessages.addAll(rifServiceException.getErrorMessages());
			}
		}
		
		//Ensure at least one of the check boxes is ticked
		boolean atLeastOneOutputOptionTicked = false;
		ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
		checkBoxes.addAll(checkBoxFromOutputOptionName.values());
		for (JCheckBox checkBox : checkBoxes) {
			if (checkBox.isSelected()) {
				atLeastOneOutputOptionTicked = true;
				break;
			}
		}
		if (atLeastOneOutputOptionTicked == false) {
			String errorMessage
				= RIFJobSubmissionToolMessages.getMessage(
					"specifyReportingOptionsStepPanel.error.noOptionsSpecified");
			RIFServiceException rifJobSubmissionToolException
				= new RIFServiceException(errorMessage);
			throw rifJobSubmissionToolException;
		}		
	}

// ==========================================
// Section Interfaces
// ==========================================

	//Interface: ActionListener


	public void actionPerformed(
		ActionEvent event) {

		Object button = event.getSource();
		
		if (addEditDeletePanel.isAddButton(button)) {
			addPlugin();
		}
		else if (addEditDeletePanel.isEditButton(button)) {
			editPlugin();			
		}
		else if (addEditDeletePanel.isDeleteButton(button)) {
			deletePlugin();
		}	
	}
	
	//Interface: MouseListener
	public void mouseClicked(
		MouseEvent event) {

		if (event.getClickCount() == 2) {
			editPlugin();
		}
	}
	

	public void mouseEntered(
		MouseEvent event) {		
	}
	

	public void mouseExited(
		MouseEvent event) {
	}
	

	public void mousePressed(
		MouseEvent event) {
	}

	public void mouseReleased(
		MouseEvent event) {
	}
 
// ==========================================
// Section Override
// ==========================================


	@Override
	public void initialiseForm() 
		throws RIFServiceException {

		RIFStudySubmissionToolSession rifSession = getRIFSession();
		
		RIFStudySubmission rifJobSubmission
			= rifSession.getRIFJobSubmission();
		ArrayList<CalculationMethod> currentCalculationMethods
			= rifJobSubmission.getCalculationMethods();
		calculationMethodTable.setCalculationMethods(currentCalculationMethods);	

		//determine which rif output options should appear ticked.
		//If this is the first time the user has visited this form as part of 
		//a new submission, then tick the "data" option by default and leave
		//all other options unticked.  Otherwise, rely on the existing
		//rif output options to indicate selection

		clearTickedReportingOptions();
		if (rifSession.isActivityStepCommitted(RIFStudySubmissionActivityStep.SPECIFY_REPORTS)) {
			ArrayList<RIFOutputOption> currentRIFOutputOptions
				= rifJobSubmission.getRIFOutputOptions();
			tickReportingOptions(currentRIFOutputOptions);
		}
		else {
			//we have not visited this form before.  Apply defaults

			//Ensure that the option for "DATA" is ticked
			JCheckBox dataCheckBox 
				= checkBoxFromOutputOptionName.get(RIFOutputOption.DATA.getName());
			dataCheckBox.setSelected(true);			
		}

		buildUI();
	}

	/**
	 * Tick reporting options.
	 *
	 * @param reportingOutputOptions the reporting output options
	 */
	private void tickReportingOptions(
		ArrayList<RIFOutputOption> reportingOutputOptions) {

		for (RIFOutputOption reportingOutputOption : reportingOutputOptions) {
			String reportingOutputOptionName
				= reportingOutputOption.getName();
			JCheckBox checkBox 
				= checkBoxFromOutputOptionName.get(reportingOutputOptionName);
			checkBox.setSelected(true);
		}
	}
	
	/**
	 * Clear ticked reporting options.
	 */
	private void clearTickedReportingOptions() {
		
		ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
		checkBoxes.addAll(checkBoxFromOutputOptionName.values());
		for (JCheckBox checkBox : checkBoxes) {
			checkBox.setSelected(false);
		}
	}


	@Override
	public void commitChanges() 
		throws RIFServiceException {

		validateForm();
		
		RIFStudySubmissionToolSession rifSession = getRIFSession();

		ArrayList<CalculationMethod> calculationMethods
			= calculationMethodTable.getCalculationMethods();
		
		RIFStudySubmission originalJobSubmission
			= rifSession.getRIFJobSubmission();
		originalJobSubmission.setCalculationMethods(calculationMethods);
		
		ArrayList<RIFOutputOption> currentRIFOutputOptions
			= getCurrentlySelectedRIFOutputOptions();		
		originalJobSubmission.setRIFOutputOptions(currentRIFOutputOptions);

		rifSession.addCommittedActivityStep(RIFStudySubmissionActivityStep.SPECIFY_REPORTS);
	}
}
