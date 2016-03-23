package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.ShapeFile;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.NoDataAvailablePanel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.*;

import java.awt.GridBagConstraints;
import java.awt.event.*;
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

public class ShapeFilePropertyPreviewTablePanel {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private UserInterfaceFactory userInterfaceFactory;
	private JLabel panelTitleLabel;
	private JPanel instructionsPanel;
	
	private JScrollPane scrollPane;
	
	private NoDataAvailablePanel noPreviewDataPanel;
	
	private JPanel panel;
	// ==========================================
	// Section Construction
	// ==========================================

	public ShapeFilePropertyPreviewTablePanel(
		final UserInterfaceFactory userInterfaceFactory) {

		this.userInterfaceFactory = userInterfaceFactory;
		buildUI();
	}

	private void buildUI() {

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
	
	public ArrayList<String> showPreviewAndRetrieveFieldNames(final ShapeFile shapeFile) {
				
		String[] columnNames = new String[7];
		columnNames[0] = "gid";
		columnNames[1] = "district";
		columnNames[2] = "ladua2011";
		columnNames[3] = "xcentroid";
		columnNames[4] = "ycentroid";
		columnNames[5] = "males";
		columnNames[6] = "females";

		String[][] data = new String[3][7];
		data[0][0] = "1";
		data[0][1] = "district one";
		data[0][2] = "AA005566";
		data[0][3] = "45.768";
		data[0][4] = "53.456";
		data[0][5] = "25";
		data[0][6] = "30";

		data[1][0] = "2";
		data[1][1] = "district two";
		data[1][2] = "AA005566";
		data[1][3] = "45.768";
		data[1][4] = "53.456";
		data[1][5] = "25";
		data[1][6] = "30";

		data[2][0] = "3";
		data[2][1] = "district three";
		data[2][2] = "AA22AA33";
		data[2][3] = "65.222";
		data[2][4] = "50.121";
		data[2][5] = "31";
		data[2][6] = "40";

		DefaultTableModel tableModel = new DefaultTableModel();
		tableModel.setDataVector(data, columnNames);
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


