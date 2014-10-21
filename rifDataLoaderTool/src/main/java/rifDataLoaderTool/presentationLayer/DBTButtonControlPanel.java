package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderMessages;
import rifDataLoaderTool.system.RIFDataLoaderActivityStep;
import rifGenericUILibrary.UserInterfaceFactory;

import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;

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

public class DBTButtonControlPanel {

	// ==========================================
	// Section Constants
	// ==========================================
	private static Color PROCESSED_STEP_COLOUR = new Color(200, 200, 200);
	private static Color UNPROCESSED_STEP_COLOUR = (new JPanel()).getBackground();
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	private UserInterfaceFactory userInterfaceFactory;
	
	private JPanel panel;
	
	private JButton loadTableButton;
	private JButton cleanTableButton;
	private JButton applyRIFChangesButton;
	private JButton combineTablesButton;
	private JButton optimiseTableButton;
	private JButton publishTableButton;
	
	private JButton deleteButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DBTButtonControlPanel(
		final UserInterfaceFactory userInterfaceFactory) {

		this.userInterfaceFactory = userInterfaceFactory;
		
		String loadTableButtonText
			= RIFDataLoaderMessages.getMessage("rifDataLoaderActivityStep.loadRawDataTable.label") 
			+ "...";
		loadTableButton
			= userInterfaceFactory.createButton(loadTableButtonText);
		
		String cleanTableButtonText
			= RIFDataLoaderMessages.getMessage("rifDataLoaderActivityStep.cleanDataTable.label")
			+ "...";
		cleanTableButton
			= userInterfaceFactory.createButton(cleanTableButtonText);
		
		String applyRIFChangesButtonText
			= RIFDataLoaderMessages.getMessage("rifDataLoaderActivityStep.applyRIFSpecificTransformation.label")
				+ "...";

		applyRIFChangesButton
			= userInterfaceFactory.createButton(applyRIFChangesButtonText);
		
		String combineTablesButtonText
			= RIFDataLoaderMessages.getMessage("rifDataLoaderActivityStep.combineTables.label")
				+ "...";
			
		combineTablesButton
			= userInterfaceFactory.createButton(combineTablesButtonText);
		
		String optimiseTableButtonText
			= RIFDataLoaderMessages.getMessage("rifDataLoaderActivityStep.optimiseTable.label")
			+ "...";
		optimiseTableButton
			= userInterfaceFactory.createButton(optimiseTableButtonText);
		
		String publishTableButtonText
			= RIFDataLoaderMessages.getMessage("rifDataLoaderActivityStep.publishTable.label")
				+ "...";		
		publishTableButton
			= userInterfaceFactory.createButton(publishTableButtonText);
	
		String deleteButtonText
			= RIFDataLoaderMessages.getMessage("buttons.delete.label");
		deleteButton
			= userInterfaceFactory.createButton(deleteButtonText);
		
		panel = createPanel();
	}

	private JPanel createPanel() {
		JPanel panel = userInterfaceFactory.createBorderLayoutPanel();

		JPanel leftPanel = userInterfaceFactory.createPanel();
		GridBagConstraints leftPanelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		leftPanel.add(loadTableButton, leftPanelGC);
		leftPanelGC.gridx++;
		leftPanel.add(cleanTableButton, leftPanelGC);
		leftPanelGC.gridx++;
		leftPanel.add(applyRIFChangesButton, leftPanelGC);
		leftPanelGC.gridx++;
		leftPanel.add(combineTablesButton, leftPanelGC);
		leftPanelGC.gridx++;
		leftPanel.add(optimiseTableButton, leftPanelGC);
		leftPanelGC.gridx++;
		leftPanel.add(publishTableButton, leftPanelGC);

		JPanel rightPanel = userInterfaceFactory.createPanel();
		GridBagConstraints rightPanelGC 
			= userInterfaceFactory.createGridBagConstraints();
		rightPanel.add(deleteButton, rightPanelGC);
				
		panel.add(leftPanel, BorderLayout.WEST);
		panel.add(rightPanel, BorderLayout.EAST);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public JPanel getPanel() {
		
		return panel;
	}
	
	public void showEmptyListState() {
		loadTableButton.setEnabled(true);
		cleanTableButton.setEnabled(false);
		applyRIFChangesButton.setEnabled(false);
		combineTablesButton.setEnabled(false);
		optimiseTableButton.setEnabled(false);
		publishTableButton.setEnabled(false);
		deleteButton.setEnabled(false);
	}

	public void showPopulatedListState() {
		loadTableButton.setEnabled(true);
		cleanTableButton.setEnabled(true);
		applyRIFChangesButton.setEnabled(true);
		combineTablesButton.setEnabled(true);
		optimiseTableButton.setEnabled(true);
		publishTableButton.setEnabled(true);
		deleteButton.setEnabled(true);		
	}
	
	public void setCurrentActivityStep(
		final RIFDataLoaderActivityStep activityStep) {
		
		if (activityStep == RIFDataLoaderActivityStep.LOAD) {
			indicateProcessedStep(loadTableButton);
			indicateUnprocessedStep(cleanTableButton);
			indicateUnprocessedStep(applyRIFChangesButton);
			indicateUnprocessedStep(combineTablesButton);
			indicateUnprocessedStep(optimiseTableButton);
			indicateUnprocessedStep(publishTableButton);			
		}
		else if (activityStep == RIFDataLoaderActivityStep.CLEAN) {
			indicateProcessedStep(loadTableButton);
			indicateProcessedStep(cleanTableButton);
			indicateUnprocessedStep(applyRIFChangesButton);
			indicateUnprocessedStep(combineTablesButton);
			indicateUnprocessedStep(optimiseTableButton);
			indicateUnprocessedStep(publishTableButton);			
		}
		else if (activityStep == RIFDataLoaderActivityStep.CONVERT) {
			indicateProcessedStep(loadTableButton);
			indicateProcessedStep(cleanTableButton);
			indicateProcessedStep(applyRIFChangesButton);
			indicateUnprocessedStep(combineTablesButton);
			indicateUnprocessedStep(optimiseTableButton);
			indicateUnprocessedStep(publishTableButton);
		}
		else if (activityStep == RIFDataLoaderActivityStep.COMBINE) {
			indicateProcessedStep(loadTableButton);
			indicateProcessedStep(cleanTableButton);
			indicateProcessedStep(applyRIFChangesButton);
			indicateProcessedStep(combineTablesButton);
			indicateUnprocessedStep(optimiseTableButton);
			indicateUnprocessedStep(publishTableButton);
		}
		else if (activityStep == RIFDataLoaderActivityStep.OPTIMISE) {
			indicateProcessedStep(loadTableButton);
			indicateProcessedStep(cleanTableButton);
			indicateProcessedStep(applyRIFChangesButton);
			indicateProcessedStep(combineTablesButton);
			indicateProcessedStep(optimiseTableButton);
			indicateUnprocessedStep(publishTableButton);
		}
		else if (activityStep == RIFDataLoaderActivityStep.PUBLISH) {
			indicateProcessedStep(loadTableButton);
			indicateProcessedStep(cleanTableButton);
			indicateProcessedStep(applyRIFChangesButton);
			indicateProcessedStep(combineTablesButton);
			indicateProcessedStep(optimiseTableButton);
			indicateProcessedStep(publishTableButton);			
		}
		
	}
	
	private void indicateProcessedStep(
		final JButton button) {
		
		button.setBackground(PROCESSED_STEP_COLOUR);
	}
	
	private void indicateUnprocessedStep(
		final JButton button) {
			
		button.setBackground(UNPROCESSED_STEP_COLOUR);
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


