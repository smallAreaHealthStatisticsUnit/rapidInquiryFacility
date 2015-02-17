package rifJobSubmissionTool.desktop.interactive;

import rifGenericUILibrary.ErrorDialog;
import rifGenericUILibrary.OrderedListComboBox;
import rifGenericUILibrary.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;
import rifServices.businessConceptLayer.Project;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

final class ProjectSelectionPanel 
	implements ActionListener {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	/** The rif session. */
	private RIFStudySubmissionToolSession rifSession;
	
	//GUI Components
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;
	/** The panel. */
	private JPanel panel;
	/** The project combo box. */
	private OrderedListComboBox projectComboBox;
	/** The project description text area. */
	private JTextArea projectDescriptionTextArea;

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new project selection panel.
	 *
	 * @param rifSession the rif session
	 */
	public ProjectSelectionPanel(
		RIFStudySubmissionToolSession rifSession) {

		this.rifSession = rifSession;
		this.userInterfaceFactory = rifSession.getUIFactory();
		buildUI();
		reset();
	}
	
	/**
	 * Builds the ui.
	 */
	private void buildUI() {
		
		projectComboBox 
			= new OrderedListComboBox(userInterfaceFactory);
		
		panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		panel.add(createComboBoxField(), panelGC);
		
		panelGC.gridy++;
		
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1.0;
		projectDescriptionTextArea
			= userInterfaceFactory.createNonEditableTextArea(3, 30);
		JScrollPane scrollPane 
			= userInterfaceFactory.createScrollPane(projectDescriptionTextArea);
		panel.add(scrollPane, panelGC);
		
		projectComboBox.addActionListener(this);
	}
	
	/**
	 * Creates the combo box field.
	 *
	 * @return the j panel
	 */
	private JPanel createComboBoxField() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.ipadx = 5;
		panelGC.ipady = 5;
		String projectLabelText
			= RIFServiceMessages.getMessage("project.label");
		JLabel projectLabel
			= userInterfaceFactory.createLabel(projectLabelText);
		panel.add(projectLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		projectComboBox = new OrderedListComboBox(userInterfaceFactory);
		panel.add(projectComboBox.getComboBox(), panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * Adds the action listener.
	 *
	 * @param actionListener the action listener
	 */
	public void addActionListener(
		ActionListener actionListener) {
		
		projectComboBox.addActionListener(actionListener);
	}
	
	/**
	 * Checks if is combo box.
	 *
	 * @param object the object
	 * @return true, if is combo box
	 */
	public boolean isComboBox(
		Object object) {
		
		if (projectComboBox.getComboBox() == object) {
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the selected project.
	 *
	 * @return the selected project
	 */
	public Project getSelectedProject() {
		
		Project selectedProject
			= (Project) projectComboBox.getSelectedItem();
		return selectedProject;
	}
	
	/**
	 * Sets the selected project.
	 *
	 * @param selectedProject the new selected project
	 */
	public void setSelectedProject(
		Project selectedProject) {
		
		projectComboBox.setSelectedItem(selectedProject);
	}
	
	/**
	 * Reset.
	 */
	public void reset() {
		
		//obtains the list of projects and initialises selection to the
		//first visible one
		try {
			User currentUser = rifSession.getUser();
			RIFStudySubmissionAPI service
				= rifSession.getRIFStudySubmissionService();
			ArrayList<Project> projects
				= service.getProjects(currentUser);
						
			//clear the list
			projectComboBox.clearList();
			for (Project project : projects) {
				projectComboBox.addListItem(project);
			}
			
			if (projects.size() == 0) {
				//should rarely ever have no projects
				//select no item -- which should ensure that the 
				//active choice part of the combobox is cleared
				projectComboBox.setSelectedItem(-1);				
			}
			else {
				//select first item
				projectComboBox.setSelectedItem(0);
			}			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				null, 
				rifServiceException.getErrorMessages());
		}
	}
	
	/**
	 * Clear display.
	 */
	private void clearDisplay() {
		
		projectComboBox.clearList();
		projectComboBox.setSelectedItem(-1);
		projectDescriptionTextArea.setText("");		
	}
	
	/**
	 * Sets the enabled.
	 *
	 * @param isEnabled the new enabled
	 */
	public void setEnabled(
		boolean isEnabled) {
		
		projectComboBox.setEnabled(isEnabled);
		projectDescriptionTextArea.setEnabled(isEnabled);
		if (isEnabled) {
			reset();
		}
		else {
			clearDisplay();
		}		
	}
	
	/**
	 * Gets the panel.
	 *
	 * @return the panel
	 */
	public JPanel getPanel() {
		
		return panel;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(
		ActionEvent event) {
		
		//the source of the event is guaranteed to be the
		//combo box field
		
		//update the text area that describes the currently selected
		//project
		Project currentlySelectedProject
			= getSelectedProject();
		if (currentlySelectedProject != null) {
			projectDescriptionTextArea.setText(currentlySelectedProject.getDescription());			
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}

