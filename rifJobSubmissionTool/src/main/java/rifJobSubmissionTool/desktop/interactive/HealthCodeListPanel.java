
package rifJobSubmissionTool.desktop.interactive;

import rifGenericUILibrary.ErrorDialog;

import rifGenericUILibrary.UserInterfaceFactory;
import rifGenericUILibrary.ListEditingButtonPanel;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.io.HealthCodeListReader;
import rifServices.io.HealthCodeListWriter;
import rifServices.io.XMLFileFilter;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;


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


class HealthCodeListPanel 
	implements ActionListener {

// ==========================================
// Section Constants
// ==========================================
	
	/** The Constant SOURCE_COLUMN_WIDTH. */
	private static final int SOURCE_COLUMN_WIDTH=15;	
	/** The Constant CODE_COLUMN_WIDTH. */
	private static final int CODE_COLUMN_WIDTH=10;	
	/** The Constant DESCRIPTION_COLUMN_WIDTH. */
	private static final int DESCRIPTION_COLUMN_WIDTH=30;
	
	private ListEditingButtonPanel listEditingButtonPanel;

	// ==========================================
// Section Properties
// ==========================================

	//Data
	/** The rif session. */
	private RIFStudySubmissionToolSession rifSession;

	//GUI Components
	/** The parent dialog. */
	private JDialog parentDialog;	
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;
	/** The panel. */
	private JPanel panel;
	/** The selected health code table. */
	private JTable selectedHealthCodeTable;
	/** The selected health code table model. */
	private HealthCodeTableModel selectedHealthCodeTableModel;
	/** The export to saved list button. */
	/*
	private JButton exportToSavedListButton;
	private JButton importFromSavedListButton;
	private JButton addHealthCodeButton;
	private JButton deleteHealthCodeButton;
	*/
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new health code list panel.
     *
     * @param parentDialog the parent dialog
     * @param rifSession the rif session
     */
	public HealthCodeListPanel(
    	JDialog parentDialog,
    	RIFStudySubmissionToolSession rifSession) {
    	
    	this.rifSession = rifSession;
		this.userInterfaceFactory = rifSession.getUIFactory();
		panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();

		String panelTitleLabelText
			= RIFServiceMessages.getMessage("healthCode.plural.label");
		JLabel panelTitleLabel
			= userInterfaceFactory.createLabel(panelTitleLabelText);
		String panelTitleLabelToolTipText
			= RIFServiceMessages.getMessage("healthCode.plural.toolTip");
		panelTitleLabel.setToolTipText(panelTitleLabelToolTipText);
		userInterfaceFactory.setBoldFont(panelTitleLabel);
		panel.add(panelTitleLabel, panelGC);

		panelGC.gridy++;
		panelGC.gridx = 0;
		String instructionLabelText
			= RIFJobSubmissionToolMessages.getMessage("healthCodeListPanel.instructions");
		JLabel instructionLabel
			= userInterfaceFactory.createInstructionLabel(instructionLabelText);
		panel.add(instructionLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1.0;
		panelGC.weighty = 1.0;
		panel.add(createTablePanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;		
		panelGC.anchor = GridBagConstraints.SOUTHEAST;

		panel.add(createButtonPanel(), panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
    }
	
	/**
	 * Creates the table panel.
	 *
	 * @return the j panel
	 */
	private JPanel createTablePanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		String selectedCodesLabelText
			= RIFJobSubmissionToolMessages.getMessage("healthCodeListPanel.selectedCodes.label");
		JLabel selectedCodesLabel
			= userInterfaceFactory.createLabel(selectedCodesLabelText);
		String selectedCodesLabelToolTipText
			= RIFJobSubmissionToolMessages.getMessage("healthCodeListPanel.selectedCodes.toolTip");
		selectedCodesLabel.setToolTipText(selectedCodesLabelToolTipText);
		panel.add(selectedCodesLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1.0;
		panelGC.weighty = 1.0;		
		selectedHealthCodeTableModel = new HealthCodeTableModel();
		selectedHealthCodeTable
			= userInterfaceFactory.createTable(selectedHealthCodeTableModel);
		//selectedHealthCodeTable.setColumnSelectionAllowed(false);
		//selectedHealthCodeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		
		DefaultTableColumnModel columnModel
			= (DefaultTableColumnModel) selectedHealthCodeTable.getColumnModel();
		TableColumn sourceTableColumn
			= columnModel.getColumn(HealthCodeTableModel.SOURCE);
		sourceTableColumn.setPreferredWidth(SOURCE_COLUMN_WIDTH);
		sourceTableColumn.setResizable(true);
	
		TableColumn codeTableColumn
			= columnModel.getColumn(HealthCodeTableModel.CODE);
		codeTableColumn.setPreferredWidth(CODE_COLUMN_WIDTH);
		codeTableColumn.setResizable(true);
				
		TableColumn descriptionTableColumn
			= columnModel.getColumn(HealthCodeTableModel.DESCRIPTION);
		descriptionTableColumn.setPreferredWidth(DESCRIPTION_COLUMN_WIDTH);
		descriptionTableColumn.setResizable(true);
		
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(selectedHealthCodeTable);
		panel.add(scrollPane, panelGC);
		
		return panel;
		
	}

	/**
	 * Creates the button panel.
	 *
	 * @return the j panel
	 */
	private JPanel createButtonPanel() {
		
		listEditingButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		listEditingButtonPanel.includeExportButton("");
		listEditingButtonPanel.includeImportButton("");
		listEditingButtonPanel.includeAddButton("");
		listEditingButtonPanel.includeDeleteButton("");
		listEditingButtonPanel.addActionListener(this);

		return listEditingButtonPanel.getPanel();
		
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
	 * Clear list.
	 */
	public void clearList() {
		
		selectedHealthCodeTableModel.clearList();
		selectedHealthCodeTableModel.fireTableDataChanged();
	}
	
	/**
	 * Gets the health codes.
	 *
	 * @return the health codes
	 */
	public ArrayList<HealthCode> getHealthCodes() {
		
		ArrayList<HealthCode> healthCodes
			= selectedHealthCodeTableModel.getHealthCodes();
		return healthCodes;
	}
	
	/**
	 * Sets the data.
	 *
	 * @param healthCodes the new data
	 */
	public void setData(ArrayList<HealthCode> healthCodes) {
		
		clearList();
		selectedHealthCodeTableModel.addHealthCodes(healthCodes);

		updateListGUIFeatures();		
	}
	
    /**
     * Export to saved list.
     */
    private void exportToSavedList() {
    	
    	JFileChooser fileChooser 
    		= userInterfaceFactory.createFileChooser();
    	XMLFileFilter xmlFileFilter
    		= new XMLFileFilter();
    	fileChooser.setFileFilter(xmlFileFilter);
    	File workingDirectory
    		= rifSession.getWorkingDirectory();
    	fileChooser.setCurrentDirectory(workingDirectory);
    	int result
    		= fileChooser.showSaveDialog(parentDialog);
    	if (result != JFileChooser.APPROVE_OPTION) {
    		return;
    	}

    	try {
    		File selectedFile 
    			= XMLFileFilter.ensureFileEndsWithXML(fileChooser.getSelectedFile());   
    		
    		HealthCodeListWriter healthCodeListWriter
    			= new HealthCodeListWriter();
    	
    		ArrayList<HealthCode> healthCodes = getHealthCodes();
    		healthCodeListWriter.writeFile(selectedFile, healthCodes);
    	}
    	catch(RIFServiceException rifServiceException) {
    		ErrorDialog.showError(parentDialog, rifServiceException.getErrorMessages());
    	}
    	
	}
	
	/**
	 * Import from saved list.
	 */
	private void importFromSavedList() {
		
    	JFileChooser fileChooser 
    		= userInterfaceFactory.createFileChooser();
    	XMLFileFilter xmlFileFilter
			= new XMLFileFilter();
    	fileChooser.setFileFilter(xmlFileFilter);
    	File workingDirectory
			= rifSession.getWorkingDirectory();
    	fileChooser.setCurrentDirectory(workingDirectory);    	
    	int result
			= fileChooser.showOpenDialog(parentDialog);
    	if (result != JFileChooser.APPROVE_OPTION) {
    		return;
    	}
    	
    	File selectedFile = fileChooser.getSelectedFile();
    	try {
    		HealthCodeListReader healthCodeListReader
    			= new HealthCodeListReader();
    		healthCodeListReader.readFile(selectedFile);
    		ArrayList<HealthCode> importedHealthCodes
    			= healthCodeListReader.getHealthCodes();
    		selectedHealthCodeTableModel.addHealthCodes(importedHealthCodes);
    		updateListGUIFeatures();    		
    	}
    	catch(RIFServiceException rifServiceException) {
    		ErrorDialog.showError(parentDialog, rifServiceException);
    	}		
	}
	
	/**
	 * Adds the health code.
	 */
	private void addHealthCode() {
		
		HealthCodeSelectionDialog healthCodeSelectionDialog
			= new HealthCodeSelectionDialog(rifSession);
		healthCodeSelectionDialog.show();
		
		ArrayList<HealthCode> selectedHealthCodes		
			= healthCodeSelectionDialog.getSelectedHealthCodes();
		
		selectedHealthCodeTableModel.addHealthCodes(selectedHealthCodes);
		selectedHealthCodeTableModel.fireTableDataChanged();
		
		updateListGUIFeatures();
	}
	
	/**
	 * Delete selected health codes.
	 */
	private void deleteSelectedHealthCodes() {
		
		int[] rowsToDelete = selectedHealthCodeTable.getSelectedRows();
		selectedHealthCodeTableModel.deleteHealthCodes(rowsToDelete);
		updateListGUIFeatures();		
	}
	
	/*
	 * updates list features depending on whether the list is empty or not
	 * The export an delete buttons are desensitised in the event of an empty list
	 * When the item becomes non-empty, the first item in the list is highlighted
	 */
	/**
	 * Update list gui features.
	 */
	private void updateListGUIFeatures() {
		
		int totalNumberOfRows 
			= selectedHealthCodeTableModel.getRowCount();
		
		if (totalNumberOfRows == 0) {
			//update buttons in a way that is appropriate for an empty list
			listEditingButtonPanel.indicateEmptyState();
		}
		else {
			ListSelectionModel listSelectionModel
				= selectedHealthCodeTable.getSelectionModel();
			listSelectionModel.setSelectionInterval(0, 0);			
			listEditingButtonPanel.indicatePopulatedState();
		}
	}
	
// ==========================================
// Section Errors and Validation
// ==========================================

// ==========================================
// Section Interfaces
// ==========================================

	//Interface: ActionListener

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(
		ActionEvent event) {
		
		Object button = event.getSource();
		if (listEditingButtonPanel.isExportButton(button)) {
			exportToSavedList();
		}
		else if (listEditingButtonPanel.isImportButton(button)) {
			importFromSavedList();
		}
		else if (listEditingButtonPanel.isAddButton(button)) {		
			addHealthCode();
		}
		else if (listEditingButtonPanel.isDeleteButton(button)) {		
			deleteSelectedHealthCodes();
		}	
	}
	
	
// ==========================================
// Section Override
// ==========================================




}
