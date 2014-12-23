package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolSession;
import rifDataLoaderTool.businessConceptLayer.TableFieldCleaningConfiguration;
import rifDataLoaderTool.io.FieldVarianceReport;

import rifDataLoaderTool.dataStorageLayer.RIFDataLoaderServiceAPI;

import rifServices.system.RIFServiceException;

import rifGenericUILibrary.UserInterfaceFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 *
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public class FieldVarianceDialog extends AbstractDataLoaderToolDialog {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private JEditorPane editorPane;

	private TableFieldCleaningConfiguration tableFieldCleaningConfiguration;

	// ==========================================
	// Section Construction
	// ==========================================

	public FieldVarianceDialog(
		final RIFDataLoaderToolSession session) {

		//here we want the button panel to only show "Close"
		//we want to leave out the "OK" button because the
		//dialog is read only.
		super(session, false);
				
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		editorPane = userInterfaceFactory.createHTMLEditorPane();		
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(editorPane);
		
		JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
		panel.add(scrollPane, panelGC);
		
		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		
		panel.add(getOKCloseButtonPanel(), panelGC);
		
		setMainPanel(panel);	
		setSize(300, 300);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void setData(
		final TableFieldCleaningConfiguration tableFieldCleaningConfiguration) 
		throws RIFServiceException {
		
		this.tableFieldCleaningConfiguration = tableFieldCleaningConfiguration;
	
		RIFDataLoaderServiceAPI service = getService();
		
		String[][] varianceData
			= service.getVarianceInFieldData(
				getCurrentUser(),
				tableFieldCleaningConfiguration);
		FieldVarianceReport fieldVarianceReport
			 = new FieldVarianceReport();
		String htmlText
			= fieldVarianceReport.getHTML(
				tableFieldCleaningConfiguration, 
				varianceData);
		editorPane.setText(htmlText);
		editorPane.setCaretPosition(0);
	}
	
	private void close() {
		hide();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	public void actionPerformed(ActionEvent event) {
		Object button = event.getSource();
		
		if (isCloseButton(button)) {
			close();
		}
	}

	
	// ==========================================
	// Section Override
	// ==========================================

}


