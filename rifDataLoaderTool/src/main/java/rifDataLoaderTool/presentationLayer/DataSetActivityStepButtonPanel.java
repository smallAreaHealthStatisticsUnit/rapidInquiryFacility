package rifDataLoaderTool.presentationLayer;


import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderActivityStep;



import rifGenericLibrary.presentationLayer.UserInterfaceFactory;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;

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

public final class DataSetActivityStepButtonPanel {

	// ==========================================
	// Section Constants
	// ==========================================
	//the colour showing that a step has already been done for a configuration
	private static final Color DONE_STEP_COLOUR= new Color(100, 100, 100);
	//the colour showing that a step is an optional future step
	private static final Color OPTIONAL_STEP_COLOUR = new Color(150, 150, 150);
	//the colour showing that a step is a required future step
	private static final Color REQUIRED_STEP_COLOUR = new Color(200, 200, 200);
		
	private static final Color PUBLISH_COLOUR = Color.red;
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	private RIFDataLoaderActivityStep currentActivityStep;
	
	private JPanel panel;
	
	private UserInterfaceFactory userInterfaceFactory;
	
	private JButton loadButton;
	private JButton cleanButton;
	private JButton convertButton;
	private JButton combineButton;
	private JButton optimiseButton;
	private JButton checkButton;
	private JButton publishButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetActivityStepButtonPanel(
		final UserInterfaceFactory userInterfaceFactory) {

		this.userInterfaceFactory = userInterfaceFactory;
				
		panel = userInterfaceFactory.createBorderLayoutPanel();
		
		
		String ellipsisSuffix = "...";
		String loadButtonText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderActivityStep.load.label");
		loadButton
			= userInterfaceFactory.createButton(loadButtonText + ellipsisSuffix);
		panel.add(loadButton, BorderLayout.WEST);

		panel.add(createMainButtonPanel(), BorderLayout.CENTER);
		
		String publishButtonText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderActivityStep.publish.label");
		publishButton
			= userInterfaceFactory.createButton(publishButtonText);
		publishButton.setBackground(PUBLISH_COLOUR);
		panel.add(publishButton, BorderLayout.EAST);
		
	}
	
	private JPanel createMainButtonPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.anchor = GridBagConstraints.NORTHWEST;	
		panelGC.fill = GridBagConstraints.NONE;
		
		
		String ellipsisSuffix = "...";
		String cleanButtonText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderActivityStep.clean.label");
		cleanButton
			= userInterfaceFactory.createButton(cleanButtonText);
		panel.add(cleanButton, panelGC);
		
		panelGC.gridx++;
		String convertButtonText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderActivityStep.convert.label");
		convertButton
			= userInterfaceFactory.createButton(convertButtonText);
		panel.add(convertButton, panelGC);

		panelGC.gridx++;
		String combineButtonText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderActivityStep.combine.label");
		combineButton
			= userInterfaceFactory.createButton(combineButtonText);
		panel.add(combineButton, panelGC);

		panelGC.gridx++;
		String optimiseButtonText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderActivityStep.optimise.label");
		optimiseButton
			= userInterfaceFactory.createButton(optimiseButtonText);
		panel.add(optimiseButton, panelGC);

		panelGC.gridx++;
		String checkButtonText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderActivityStep.check.label");
		checkButton
			= userInterfaceFactory.createButton(checkButtonText);
		panel.add(checkButton, panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public JPanel getPanel() {
		return panel;
	}
	
	public boolean isLoadButton(
		final Object button) {

		return(button == loadButton);
	}
	
	public boolean isCleanButton(
		final Object button) {
		
		return(button == cleanButton);		
	}
	
	public boolean isConvertButton(
		final Object button) {
		
		return(button == convertButton);		
	}
	
	public boolean isCombineButton(
		final Object button) {
		
		return(button == combineButton);		
	}

	public boolean isOptimiseButton(
		final Object button) {
			
		return(button == optimiseButton);		
	}
	

	public boolean isCheckButton(
		final Object button) {
			
		return(button == checkButton);		
	}


	public boolean isPublishButton(
		final Object button) {
			
		return(button == publishButton);		
	}

	public void addActionListener(
		final ActionListener actionListener) {

		loadButton.addActionListener(actionListener);
		cleanButton.addActionListener(actionListener);
		convertButton.addActionListener(actionListener);
		combineButton.addActionListener(actionListener);
		optimiseButton.addActionListener(actionListener);
		checkButton.addActionListener(actionListener);
		publishButton.addActionListener(actionListener);
	}
	
	public void setCurrentActivityStep(
		final RIFDataLoaderActivityStep currentActivityStep) {

		//current activity step represents the state of activity that was most
		//recently completed.  For example "LOAD" means a user finished loading a data
		//set.  The next steps would be clean
		
		/*
		 * load will always be enabled because user may want to load a new file
		 * 
		 * clean will always be enabled because all tables showing in the main data set table
		 * will always be in a state of at least LOAD.  Also, we allow going backward
		 * 
		 * Convert will only be sensitised if the configuration is at least in the CLEAN state.
		 * If the configuration is only marked LOAD, then it must be cleaned first
		 * 
		 * 
		 */
		
		
		this.currentActivityStep = currentActivityStep;
		if (currentActivityStep == RIFDataLoaderActivityStep.LOAD) {
			//data set has been loaded 
						
			cleanButton.setBackground(REQUIRED_STEP_COLOUR);
			
			convertButton.setBackground(REQUIRED_STEP_COLOUR);
			convertButton.setEnabled(false);
			
			combineButton.setBackground(OPTIONAL_STEP_COLOUR);
			combineButton.setEnabled(false);
			
			optimiseButton.setBackground(REQUIRED_STEP_COLOUR);
			optimiseButton.setEnabled(false);
			
			checkButton.setBackground(REQUIRED_STEP_COLOUR);
			checkButton.setEnabled(false);
			
			publishButton.setBackground(REQUIRED_STEP_COLOUR);
			publishButton.setEnabled(false);
		}
		else if (currentActivityStep == RIFDataLoaderActivityStep.CLEAN) {
			
			
			cleanButton.setBackground(DONE_STEP_COLOUR);
			
			convertButton.setBackground(REQUIRED_STEP_COLOUR);
			convertButton.setEnabled(true);
			
			combineButton.setBackground(OPTIONAL_STEP_COLOUR);
			combineButton.setEnabled(false);
			
			optimiseButton.setBackground(REQUIRED_STEP_COLOUR);
			optimiseButton.setEnabled(false);
			
			checkButton.setBackground(REQUIRED_STEP_COLOUR);
			checkButton.setEnabled(false);
			
			publishButton.setBackground(REQUIRED_STEP_COLOUR);
			publishButton.setEnabled(false);
			
			
		}
		else if (currentActivityStep == RIFDataLoaderActivityStep.CONVERT) {
			
			
			cleanButton.setBackground(DONE_STEP_COLOUR);
			
			convertButton.setBackground(DONE_STEP_COLOUR);
			convertButton.setEnabled(true);
			
			//if a table has just been converted, then either it can be combined
			//with other tables or the user can just skip the step and 
			//try to optimise the table they have converted.
			combineButton.setBackground(OPTIONAL_STEP_COLOUR);
			combineButton.setEnabled(true);
			
			optimiseButton.setBackground(REQUIRED_STEP_COLOUR);
			optimiseButton.setEnabled(true);
			
			checkButton.setBackground(REQUIRED_STEP_COLOUR);
			checkButton.setEnabled(false);
			
			publishButton.setBackground(REQUIRED_STEP_COLOUR);
			publishButton.setEnabled(false);
			
		}
		else if (currentActivityStep == RIFDataLoaderActivityStep.COMBINE) {

			cleanButton.setBackground(DONE_STEP_COLOUR);
			
			convertButton.setBackground(DONE_STEP_COLOUR);
			convertButton.setEnabled(true);
			
			combineButton.setBackground(DONE_STEP_COLOUR);
			combineButton.setEnabled(true);
			
			optimiseButton.setBackground(REQUIRED_STEP_COLOUR);
			optimiseButton.setEnabled(true);
			
			checkButton.setBackground(REQUIRED_STEP_COLOUR);
			checkButton.setEnabled(false);
			
			publishButton.setBackground(REQUIRED_STEP_COLOUR);
			publishButton.setEnabled(false);
			
		}
		else if (currentActivityStep == RIFDataLoaderActivityStep.OPTIMISE) {

			cleanButton.setBackground(DONE_STEP_COLOUR);
			
			convertButton.setBackground(DONE_STEP_COLOUR);
			convertButton.setEnabled(true);
			
			combineButton.setBackground(DONE_STEP_COLOUR);
			combineButton.setEnabled(true);
			
			optimiseButton.setBackground(DONE_STEP_COLOUR);
			optimiseButton.setEnabled(true);
			
			checkButton.setBackground(REQUIRED_STEP_COLOUR);
			checkButton.setEnabled(true);
			
			publishButton.setBackground(REQUIRED_STEP_COLOUR);
			publishButton.setEnabled(false);			

			
		}
		else if (currentActivityStep == RIFDataLoaderActivityStep.OPTIMISE) {


			cleanButton.setBackground(DONE_STEP_COLOUR);
			
			convertButton.setBackground(DONE_STEP_COLOUR);
			convertButton.setEnabled(true);
			
			combineButton.setBackground(DONE_STEP_COLOUR);
			combineButton.setEnabled(true);
			
			optimiseButton.setBackground(DONE_STEP_COLOUR);
			optimiseButton.setEnabled(true);
			
			checkButton.setBackground(REQUIRED_STEP_COLOUR);
			checkButton.setEnabled(true);
			
			publishButton.setBackground(REQUIRED_STEP_COLOUR);
			publishButton.setEnabled(false);			

		}
		else if (currentActivityStep == RIFDataLoaderActivityStep.CHECK) {

			cleanButton.setBackground(DONE_STEP_COLOUR);
			
			convertButton.setBackground(DONE_STEP_COLOUR);
			convertButton.setEnabled(true);
			
			combineButton.setBackground(DONE_STEP_COLOUR);
			combineButton.setEnabled(true);
			
			optimiseButton.setBackground(DONE_STEP_COLOUR);
			optimiseButton.setEnabled(true);
			
			checkButton.setBackground(DONE_STEP_COLOUR);
			checkButton.setEnabled(true);
			
			publishButton.setBackground(REQUIRED_STEP_COLOUR);
			publishButton.setEnabled(false);			
			
		}
		else if (currentActivityStep == RIFDataLoaderActivityStep.PUBLISH) {
			
		}
		
	}
	
	public void indicateEmptyList() {
		cleanButton.setEnabled(false);
		convertButton.setEnabled(false);
		optimiseButton.setEnabled(false);
		checkButton.setEnabled(false);
		publishButton.setEnabled(false);
	}
	
	public void indicatePopulatedList() {
		cleanButton.setEnabled(false);
		convertButton.setEnabled(false);
		optimiseButton.setEnabled(false);
		checkButton.setEnabled(false);
		publishButton.setEnabled(false);		
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


