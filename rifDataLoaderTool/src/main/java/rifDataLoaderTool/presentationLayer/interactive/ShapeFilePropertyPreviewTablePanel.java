package rifDataLoaderTool.presentationLayer.interactive;

import rifDataLoaderTool.businessConceptLayer.ShapeFile;
import rifDataLoaderTool.businessConceptLayer.DataLoaderServiceAPI;
import rifDataLoaderTool.system.DataLoaderToolSession;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.NoDataAvailablePanel;
import rifGenericLibrary.system.RIFServiceException;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.GridBagConstraints;
import java.util.*;

/**
 *
 *
 * <hr>
 * Copyright 2016 Imperial College London, developed by the Small Area
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

class ShapeFilePropertyPreviewTablePanel {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	private DataLoaderToolSession session;
	
	//GUI Components
	private JLabel panelTitleLabel;
	private JPanel instructionsPanel;
	private JScrollPane scrollPane;	
	private NoDataAvailablePanel noPreviewDataPanel;	
	private JPanel panel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ShapeFilePropertyPreviewTablePanel(
		final DataLoaderToolSession session) {

		this.session = session;
		
		buildUI();
	}

	private void buildUI() {

		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		panel = userInterfaceFactory.createPanel();
		
		GridBagConstraints panelGC
			 = userInterfaceFactory.createGridBagConstraints();
		String panelTitleText
			= RIFDataLoaderToolMessages.getMessage(
				"shapeFilePropertyTablePanel.title.label");
		panelTitleLabel
			= userInterfaceFactory.createLabel(panelTitleText);
		panel.add(panelTitleLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;	
		panelGC.weighty = 0;
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("shapeFilePropertyTablePanel.instructions");
		instructionsPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
		
		String noPreviewDataMessage
			= RIFDataLoaderToolMessages.getMessage("shapeFilePropertyTablePanel.noPreviewData");
		noPreviewDataPanel
			= new NoDataAvailablePanel(
				userInterfaceFactory,
				noPreviewDataMessage,
				true);
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;		
		panelGC.weighty = 1;
		scrollPane
			= userInterfaceFactory.createScrollPane(
				noPreviewDataPanel.getPanel());
		panel.add(scrollPane, panelGC);
		panel.setBorder(LineBorder.createGrayLineBorder());
		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void showNoPreview() {
		scrollPane.setViewportView(noPreviewDataPanel.getPanel());		
		panel.updateUI();
	}
	
	public int getTotalAreaIdentifiers(final ShapeFile shapeFile) 
		throws RIFServiceException {
		
		DataLoaderServiceAPI dataLoaderService = session.getDataLoaderService();
		return dataLoaderService.getTotalAreaIdentifiers(shapeFile);
	}
	
	public ArrayList<String> showPreviewAndRetrieveFieldNames(final ShapeFile shapeFile) 
		throws RIFServiceException {
				
		DataLoaderServiceAPI dataLoaderService = session.getDataLoaderService();
		String[] columnNames
			= dataLoaderService.getShapeFileFieldNames(shapeFile);
		String[][] previewData
			= dataLoaderService.getShapeFileFieldPreviewData(shapeFile);
		DefaultTableModel tableModel = new DefaultTableModel();
		tableModel.setDataVector(previewData, columnNames);
		
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();		
		JTable table = userInterfaceFactory.createTable(tableModel);
		
		scrollPane.setViewportView(table);		
		panel.updateUI();
		
		
		ArrayList<String> results = new ArrayList<String>();
		for (String columnName : columnNames) {
			results.add(columnName);
		}
		
		return results;
	}
	
	public JPanel getPanel() {
		return panel;
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


