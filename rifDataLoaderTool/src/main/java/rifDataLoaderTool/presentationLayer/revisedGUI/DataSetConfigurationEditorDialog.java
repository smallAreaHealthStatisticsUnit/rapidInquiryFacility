package rifDataLoaderTool.presentationLayer.revisedGUI;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.system.*;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.system.RIFServiceException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.ArrayList;

import javax.swing.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

import java.io.*;

/**
 *
 *
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
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

public class DataSetConfigurationEditorDialog 
	implements ActionListener,
	ListSelectionListener {

	public static void main(String[] arguments) {
		
		UserInterfaceFactory userInterfaceFactory
			= new UserInterfaceFactory();
		DataSetConfigurationEditorDialog dialog
			= new DataSetConfigurationEditorDialog(
				userInterfaceFactory);

		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.newInstance(
				"my_numerator", 
				10);
		dialog.setData(dataSetConfiguration);
		
		dialog.show();
		
	}
	
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private UserInterfaceFactory userInterfaceFactory;
	private DataSetConfiguration originalDataSetConfiguration;
	
	private OrderedListPanel dataSetFieldListPanel;
	
	private JScrollPane currentFieldScrollPane;
	
	private JDialog dialog;
	
	
	private JTextField nameTextField;
	private JTextField versionTextField;
	private JTextArea descriptionTextArea;
	private JTextField filePathTextField;
		
	private File selectedInputFile;
	private JButton browseButton;
	
	private JComboBox<String> rifSchemaAreasComboBox;
	
	
	private DataSetFieldPropertyEditorPanel dataSetFieldPropertyEditorPanel;
	
	
	private OKCloseButtonPanel okCloseButtonPanel;	
	private JButton okButton;
	private JButton closeButton;
	
	private boolean isCancelled;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetConfigurationEditorDialog(
		final UserInterfaceFactory userInterfaceFactory) {

		this.userInterfaceFactory = userInterfaceFactory;
		
		originalDataSetConfiguration
			= DataSetConfiguration.newInstance();
		isCancelled = false;
		
		String dataSetFieldListTitleText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfigurationEditorDialog.fieldListName.label");
		
		dataSetFieldListPanel 
			= new OrderedListPanel(
				dataSetFieldListTitleText,
		    	"",
				userInterfaceFactory,
				true);	
		
		dataSetFieldListPanel.addListSelectionListener(this);
		
		dataSetFieldPropertyEditorPanel
			= new DataSetFieldPropertyEditorPanel(userInterfaceFactory);
		
		buildUI();
	}

	private void buildUI() {		

		String title
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfigurationEditorDialog.title");
		dialog
			= userInterfaceFactory.createDialog(title);
		dialog.setModal(true);

		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		JPanel panel
			= userInterfaceFactory.createPanel();
			
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfigurationEditorDialog.instructions");
		JLabel instructionsLabel
			= userInterfaceFactory.createInstructionLabel(
				instructionsText);
		panel.add(
			instructionsLabel,
			panelGC);
				
		panelGC.gridy++;
		
		panel.add(createUpperPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		
		panel.add(
			createCentralPanel(), 
			panelGC);

		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		
		okCloseButtonPanel
			= new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		panel.add(
			okCloseButtonPanel.getPanel(), 
			panelGC);
		
		dialog.getContentPane().add(panel);
		dialog.setSize(600, 600);
	}
	
	private JPanel createUpperPanel() {
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		JPanel panel
			= userInterfaceFactory.createPanel();

		panel.add(
			createGeneralPropertiesPanel(), 
			panelGC);
		
		panelGC.gridy++;
		
		GridBagConstraints chooseFilePanelGC
			= userInterfaceFactory.createGridBagConstraints();
		JPanel chooseFilePanel
			= userInterfaceFactory.createPanel();
		String filePathLabelText
			= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.filePath.label");		
		JLabel filePathLabel
			= userInterfaceFactory.createLabel(filePathLabelText);
		chooseFilePanel.add(
			filePathLabel, 
			chooseFilePanelGC);
		
		chooseFilePanelGC.gridx++;
		chooseFilePanelGC.fill = GridBagConstraints.HORIZONTAL;
		chooseFilePanelGC.weightx = 1;
		filePathTextField
			= userInterfaceFactory.createNonEditableTextField();		
		chooseFilePanel.add(
			filePathTextField,
			chooseFilePanelGC);
		
		chooseFilePanelGC.gridx++;
		chooseFilePanelGC.fill = GridBagConstraints.NONE;
		chooseFilePanelGC.weightx = 0;
		
		String browseButtonText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfigurationEditorPanel.filePathBrowse.label");
		browseButton
			= userInterfaceFactory.createButton(browseButtonText);
		browseButton.addActionListener(this);
		chooseFilePanel.add(
			browseButton, 
			chooseFilePanelGC);
		
		panelGC.gridy++;
		panel.add(chooseFilePanel, panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		
		return panel;
	}
	
	private JPanel createGeneralPropertiesPanel() {
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		JPanel panel
			= userInterfaceFactory.createPanel();
		
		String nameFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.name.label");
		JLabel nameFieldLabel
			= userInterfaceFactory.createLabel(nameFieldLabelText);
		panel.add(nameFieldLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		nameTextField
			= userInterfaceFactory.createTextField();
		panel.add(nameTextField, panelGC);
		
		panelGC.gridy++;
			
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;		
		String versionFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.version.label");
		JLabel versionFieldLabel
			= userInterfaceFactory.createLabel(versionFieldLabelText);
		panel.add(versionFieldLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		versionTextField
			= userInterfaceFactory.createTextField();
		panel.add(versionTextField, panelGC);
		
		panelGC.gridy++;		
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		String descriptionFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.description.label");
		JLabel descriptionFieldLabel
			= userInterfaceFactory.createLabel(descriptionFieldLabelText);
		panel.add(descriptionFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		descriptionTextArea
			= userInterfaceFactory.createTextArea();
		panel.add(
			descriptionTextArea, 
			panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String rifSchemaAreasLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.rifSchemaArea.label");
		JLabel rifSchemaAreasLabel
			= userInterfaceFactory.createLabel(rifSchemaAreasLabelText);
		panel.add(rifSchemaAreasLabel, panelGC);	
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;		
		rifSchemaAreasComboBox
			= userInterfaceFactory.createComboBox(RIFSchemaArea.getAllSchemaNames());		
		panel.add(rifSchemaAreasComboBox, panelGC);

		return panel;
	}
	
	
	private JPanel createCentralPanel() {
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		
		JPanel panel
			= userInterfaceFactory.createPanel();
		
		currentFieldScrollPane
			= userInterfaceFactory.createScrollPane(dataSetFieldPropertyEditorPanel.getPanel());
		
		
		JSplitPane splitPane
			= userInterfaceFactory.createLeftRightSplitPane(
				createDataSetFieldPanel(), 
				currentFieldScrollPane);
		
		splitPane.setDividerSize(40);
		panel.add(splitPane, panelGC);
		
		return panel;
	}
	
	
	
	private JPanel createDataSetFieldPanel() {
		
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		JPanel panel
			= userInterfaceFactory.createPanel();
		
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
				
		JScrollPane fieldPane
			= userInterfaceFactory.createScrollPane(
				dataSetFieldListPanel.getPanel());		
		panel.add(
			fieldPane, 
			panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setData(
		final DataSetConfiguration dataSetConfiguration) {
		
		originalDataSetConfiguration = dataSetConfiguration;
		
		DataSetConfiguration revisedDataSetConfiguration
			= DataSetConfiguration.createCopy(originalDataSetConfiguration);
		populateForm(revisedDataSetConfiguration);
	}
	
	private void populateForm(
		final DataSetConfiguration revisedDataSetConfiguration) {
		
		nameTextField.setText(revisedDataSetConfiguration.getName());
		versionTextField.setText(revisedDataSetConfiguration.getVersion());
		descriptionTextArea.setText(revisedDataSetConfiguration.getDescription());
		filePathTextField.setText(revisedDataSetConfiguration.getFilePath());
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= revisedDataSetConfiguration.getFieldConfigurations();
		
		dataSetFieldListPanel.clearList();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			dataSetFieldListPanel.addListItem(fieldConfiguration);			
		}
		
	}
	
	
	public void show() {
		dialog.setVisible(true);
	}
	
	private void ok() {
		try {
			validateForm();
			
			
			isCancelled = false;
			dialog.setVisible(false);
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
		}
		
	}
	
	private void browseForInputFile() {
		JFileChooser fileChooser
			= userInterfaceFactory.createFileChooser();
		CSVFileFilter csvFileFilter
			= new CSVFileFilter();
		fileChooser.setFileFilter(csvFileFilter);
		int result
			= fileChooser.showOpenDialog(dialog);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		selectedInputFile
			= fileChooser.getSelectedFile();
		filePathTextField.setText(selectedInputFile.getAbsolutePath());
	}
	
	private void close() {
		isCancelled = true;
		dialog.setVisible(false);
	}

	public boolean isCancelled() {
		return isCancelled;		
	}
	
	public DataSetConfiguration getDataSetConfiguration() {
		
		return originalDataSetConfiguration;
	}
	
	public void resetForm() {
		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	private void validateForm() 
		throws RIFServiceException {
		
		
		
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	public void actionPerformed(ActionEvent event) {
		Object button = event.getSource();
		
		if (button == okButton) {
			ok();
		}
		else if (button == browseButton) {
			browseForInputFile();
		}
		else if (button == closeButton) {
			close();
		}
	}
	
	public void valueChanged(ListSelectionEvent event) {		
		DataSetFieldConfiguration selectedFieldConfiguration
			= (DataSetFieldConfiguration) dataSetFieldListPanel.getSelectedItem();
		dataSetFieldPropertyEditorPanel.setDataFieldConfiguration(selectedFieldConfiguration);
		
		JScrollBar verticalScrollBar
			= currentFieldScrollPane.getVerticalScrollBar();
		verticalScrollBar.setValue(verticalScrollBar.getMinimum());
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


