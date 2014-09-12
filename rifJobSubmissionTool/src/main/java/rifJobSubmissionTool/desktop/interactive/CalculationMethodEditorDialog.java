package rifJobSubmissionTool.desktop.interactive;

import rifGenericUILibrary.OKClosePanel;
import rifGenericUILibrary.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFSession;
import rifServices.businessConceptLayer.CalculationMethod;
import rifServices.businessConceptLayer.CalculationMethodPrior;
import rifServices.businessConceptLayer.Parameter;
import rifServices.system.RIFServiceMessages;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.CellEditor;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;

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

/**
 * The Class CalculationMethodEditorDialog.
 */
class CalculationMethodEditorDialog 
	implements ActionListener {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	
	//Data
	/** The original calculation method. */
	private CalculationMethod originalCalculationMethod;	
	/** The current calculation method. */
	private CalculationMethod currentCalculationMethod;
	/** The is cancelled. */
	private boolean isCancelled;
	
	
	//GUI Components
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;	
	/** The dialog. */
	private JDialog dialog;	
	/** The name text field. */
	private JTextField nameTextField;
	/** The prior combo box. */
	private JComboBox<String> priorComboBox;
	/** The description text area. */
	private JTextArea descriptionTextArea;
	/** The parameter table. */
	private ParameterTable parameterTable;	
	/** The restore default settings button. */
	private JButton restoreDefaultSettingsButton;	
	/** The ok close panel. */
	private OKClosePanel okClosePanel;

// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new calculation method editor dialog.
     *
     * @param rifSession the rif session
 	*/
	public CalculationMethodEditorDialog(
	RIFSession rifSession) {
		
		this.userInterfaceFactory = rifSession.getUIFactory();
		String dialogTitle
			= RIFJobSubmissionToolMessages.getMessage("calculationMethodEditorDialog.title");
		dialog
			= userInterfaceFactory.createDialog(dialogTitle);
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		panel.add(createNamePriorPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 0.7;
		panel.add(createDescriptionPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.weighty = 0.3;
		panel.add(createParameterListPanel(), panelGC);
				
		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panel.add(createButtonPanel(), panelGC);
		
		dialog.getContentPane().add(panel);
		dialog.setSize(400, 300);
		dialog.setModal(true);
    }

	/**
	 * Creates the name prior panel.
	 *
	 * @return the j panel
	 */
	private JPanel createNamePriorPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.ipady = UserInterfaceFactory.VERTICAL_COMPONENT_GAP;
		
		String nameFieldLabelText
			= RIFServiceMessages.getMessage("calculationMethod.name.toolTip");
		JLabel nameFieldLabel
			= userInterfaceFactory.createLabel(nameFieldLabelText);
		String nameFieldLabelToolTipText
			= RIFServiceMessages.getMessage("calculationMethod.name.toolTip");
		nameFieldLabel.setText(nameFieldLabelToolTipText);
		panel.add(nameFieldLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		nameTextField
			= userInterfaceFactory.createNonEditableTextField(20);
		panel.add(nameTextField, panelGC);
		
		//Adding prior field
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		
		String priorLabelText
			= RIFServiceMessages.getMessage("calculationMethod.prior.label");
		JLabel priorLabel
			= userInterfaceFactory.createLabel(priorLabelText);
		String priorLabelToolTipText
			= RIFServiceMessages.getMessage("calculationMethod.prior.toolTip");
		priorLabel.setToolTipText(priorLabelToolTipText);
		panel.add(priorLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		
		String[] priorChoices = CalculationMethodPrior.getPriorList();
		priorComboBox
			= userInterfaceFactory.createComboBox(priorChoices);
		panel.add(priorComboBox, panelGC);
		
		return panel;
	}

	/**
	 * Creates the description panel.
	 *
	 * @return the j panel
	 */
	private JPanel createDescriptionPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
				
		String descriptionLabelText
			= RIFServiceMessages.getMessage("calculationMethod.description.toolTip");
		JLabel descriptionLabel
			= userInterfaceFactory.createLabel(descriptionLabelText);
		panel.add(descriptionLabel, panelGC);
				
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1.0;
		panelGC.weighty = 1.0;
		descriptionTextArea
			= userInterfaceFactory.createNonEditableTextArea(5, 20);
		descriptionTextArea.setLineWrap(true);
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(descriptionTextArea);
		panel.add(scrollPane, panelGC);	
		
		return panel;
	}
	
	/**
	 * Creates the parameter list panel.
	 *
	 * @return the j panel
	 */
	private JPanel createParameterListPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		
		String parameterListLabelText
			= RIFServiceMessages.getMessage("parameter.plural.label");
		JLabel parameterListLabel
			= userInterfaceFactory.createLabel(parameterListLabelText);
		String parameterListLabelToolTipText
			= RIFServiceMessages.getMessage("parameter.plural.toolTip");
		parameterListLabel.setToolTipText(parameterListLabelToolTipText);
		panel.add(parameterListLabel, panelGC);
				
		panelGC.gridy++;		
		StringBuilder instructionsText = new StringBuilder();
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("calculationMethodSelectionDialog.parameters.instructions1"));
		instructionsText.append(" ");
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("calculationMethodSelectionDialog.parameters.instructions2"));
		
		JLabel parameterListInstructionLabel
			= userInterfaceFactory.createInstructionLabel(instructionsText.toString());
		panel.add(parameterListInstructionLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1.0;
		panelGC.weighty = 1.0;
		parameterTable = new ParameterTable(userInterfaceFactory);
				
		JScrollPane scrollPane = userInterfaceFactory.createScrollPane(parameterTable);
		panel.add(scrollPane, panelGC);		
		
		return panel;
	}
	
	/**
	 * Creates the button panel.
	 *
	 * @return the j panel
	 */
	private JPanel createButtonPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;

		String restoreDefaultSettingsButtonText
			= RIFJobSubmissionToolMessages.getMessage("calculationMethodEditorDialog.restoreDefaultSettings.label");
		restoreDefaultSettingsButton
			= userInterfaceFactory.createButton(restoreDefaultSettingsButtonText);
		restoreDefaultSettingsButton.addActionListener(this);
		
		panelGC.gridx++;
		okClosePanel
			= new OKClosePanel(userInterfaceFactory);
		okClosePanel.addActionListener(this);
		panel.add(okClosePanel.getPanel());
		
		return panel;
	}
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
	/**
	 * Show.
	 */
	public void show() {
	
		dialog.setVisible(true);
	}
	
	/**
	 * Gets the calculation method.
	 *
	 * @return the calculation method
	 */
	public CalculationMethod getCalculationMethod() {
		
		return currentCalculationMethod;
	}
	
	/**
	 * Sets the calculation method.
	 *
	 * @param calculationMethod the new calculation method
	 */
	public void setCalculationMethod(
		CalculationMethod calculationMethod) {

		originalCalculationMethod = calculationMethod;
		restoreDefaultSettings();
	}
	
	/**
	 * Update display.
	 */
	private void updateDisplay() {
		
		nameTextField.setText(currentCalculationMethod.getName());
		priorComboBox.setSelectedItem(currentCalculationMethod.getPrior().getName());
		descriptionTextArea.setText(currentCalculationMethod.getDescription());		
	}
		
	/**
	 * Checks if is cancelled.
	 *
	 * @return true, if is cancelled
	 */
	public boolean isCancelled() {
		
		return isCancelled;
	}	
	
	/**
	 * Restore default settings.
	 */
	private void restoreDefaultSettings() {
		
		currentCalculationMethod 
			= CalculationMethod.createCopy(originalCalculationMethod);
		ArrayList<Parameter> parameters
			= currentCalculationMethod.getParameters();
		parameterTable.setParameters(parameters);
		updateDisplay();
	}
	
	/**
	 * Ok.
	 */
	private void ok() {
		
		String currentPriorPhrase
			= (String) priorComboBox.getSelectedItem();
		CalculationMethodPrior currentCalculationMethodPrior
			= CalculationMethodPrior.getCalculationMethodPrior(currentPriorPhrase);
		originalCalculationMethod.setPrior(currentCalculationMethodPrior);
		
		CellEditor tableCellEditor = parameterTable.getCellEditor();
		if (tableCellEditor != null) {
			tableCellEditor.stopCellEditing();			
		}
				
		ArrayList<Parameter> currentParameters
			= parameterTable.getParameters();
		originalCalculationMethod.setParameters(currentParameters);
		
		dialog.setVisible(false);
	}
	
	/**
	 * Close.
	 */
	private void close() {

		dialog.setVisible(false);		
	}
	
// ==========================================
// Section Errors and Validation
// ==========================================

// ==========================================
// Section Interfaces
// ==========================================
	
	//Interface: ActionListener
	public void actionPerformed(
		ActionEvent event) {
		
		Object button = event.getSource();
		
		if (button == restoreDefaultSettingsButton) {
			restoreDefaultSettings();
		}
		else if (okClosePanel.isOKButton(button)) {
			ok();
		}
		else if (okClosePanel.isCloseButton(button)) {
			close();
		}
		
	}
	
	
// ==========================================
// Section Override
// ==========================================

}
