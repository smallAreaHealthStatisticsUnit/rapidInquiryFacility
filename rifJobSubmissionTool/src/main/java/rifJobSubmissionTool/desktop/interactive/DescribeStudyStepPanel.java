package rifJobSubmissionTool.desktop.interactive;

import rifGenericUILibrary.ErrorDialog;
import rifGenericUILibrary.OrderedListComboBox;
import rifGenericUILibrary.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFStudySubmissionActivityStep;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;

import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.Project;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.User;
import rifServices.dataStorageLayer.RIFStudySubmissionAPI;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.util.FieldValidationUtility;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;

import java.awt.GridBagConstraints;
import java.util.ArrayList;


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

class DescribeStudyStepPanel 
	extends AbstractStepPanel {
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// =========================================

	//Data
	
	//GUI Components
	/** The study name text field. */
	private JTextField studyNameTextField;	
	/** The study description text area. */
	private JTextArea studyDescriptionTextArea;	
	/** The project selection panel. */
	private ProjectSelectionPanel projectSelectionPanel;		
	/** The geography combo box. */
	private OrderedListComboBox geographyComboBox;
		
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new describe study step panel.
	 *
	 * @param parentDialog the parent dialog
	 * @param rifSession the rif session
	 */
	public DescribeStudyStepPanel(
		JDialog parentDialog,
		RIFStudySubmissionToolSession rifSession) {
		
		super(parentDialog, rifSession);
	}

	/**
	 * Builds the ui.
	 */
	private void buildUI() {
		
		JPanel panel = getPanel();
		panel.removeAll();
		
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		String describeStudyTitle
			= RIFJobSubmissionToolMessages.getMessage("describeStudyStepPanel.title");
		int stepNumber
			= RIFStudySubmissionActivityStep.DESCRIBE_STUDY.getStepNumber() + 1;
		setPanelTitleInformation(stepNumber, describeStudyTitle);
		panel.add(getTitleLabel(), panelGC);

		panelGC.gridy++;		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(createInstructionPanel("This is what you do"), panelGC);

		panelGC.gridy++;				
		panel.add(createNameFieldPanel(), panelGC);
		
		panelGC.gridy++;
		RIFStudySubmissionToolSession rifSession = getRIFSession();
		projectSelectionPanel = new ProjectSelectionPanel(rifSession);		
		panel.add(projectSelectionPanel.getPanel(), panelGC);

		panelGC.gridy++;
		panel.add(createGeographySelectionPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String descriptionLabelText
			= RIFServiceMessages.getMessage("abstractStudy.description.label");
		JLabel descriptionLabel
			= userInterfaceFactory.createLabel(descriptionLabelText);
		panel.add(descriptionLabel, panelGC);

		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		studyDescriptionTextArea
			= userInterfaceFactory.createTextArea();
		JScrollPane studyDescriptionScrollPane
			= userInterfaceFactory.createScrollPane(studyDescriptionTextArea);
		panel.add(studyDescriptionScrollPane, panelGC);		
	}
	
	/**
	 * Creates the name field panel.
	 *
	 * @return the j panel
	 */
	private JPanel createNameFieldPanel() {
		
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		//Study name field
		String studyNameLabelText
			= RIFServiceMessages.getMessage("abstractStudy.name.label");
		JLabel studyNameLabel
			= userInterfaceFactory.createLabel(studyNameLabelText);
		panel.add(studyNameLabel, panelGC);			
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		studyNameTextField
			= userInterfaceFactory.createTextField();
		panel.add(studyNameTextField, panelGC);

		return panel;
	}
	
	/**
	 * Creates the geography selection panel.
	 *
	 * @return the j panel
	 */
	private JPanel createGeographySelectionPanel() {
		
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();

		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String geographyLabelText
			= RIFServiceMessages.getMessage("geography.label");
		JLabel geographyLabel
			= userInterfaceFactory.createLabel(geographyLabelText);
		panel.add(geographyLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		geographyComboBox = new OrderedListComboBox(userInterfaceFactory);
		
		panel.add(geographyComboBox.getComboBox(), panelGC);

		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	public void initialiseForm() 
		throws RIFServiceException {

		buildUI();

		RIFStudySubmissionToolSession rifSession = getRIFSession();
				
		RIFStudySubmission currentRIFJobSubmission
			= rifSession.getRIFJobSubmission();
		DiseaseMappingStudy currentDiseaseMappingStudy
			= (DiseaseMappingStudy) currentRIFJobSubmission.getStudy();
		DiseaseMappingStudy cloneDiseaseMappingStudy
			= DiseaseMappingStudy.createCopy(currentDiseaseMappingStudy);
		cloneDiseaseMappingStudy.setNewRecord(true);
		rifSession.setCurrentDiseaseMappingStudy(cloneDiseaseMappingStudy);
		
		//populate fields using values from clone study object
		studyNameTextField.setText(cloneDiseaseMappingStudy.getName());
		studyDescriptionTextArea.setText(cloneDiseaseMappingStudy.getDescription());

		Project currentProject = currentRIFJobSubmission.getProject();
		Geography currentGeography = cloneDiseaseMappingStudy.getGeography();
		
		projectSelectionPanel.reset();
		projectSelectionPanel.setSelectedProject(currentProject);

		//set the geography settings
		try {
			User currentUser = rifSession.getUser();
			RIFStudySubmissionAPI service
				= rifSession.getRIFStudySubmissionService();
			ArrayList<Geography> geographies
				= service.getGeographies(currentUser);
			geographyComboBox.clearList();
			for (Geography geography : geographies) {
				geographyComboBox.addListItem(geography);
			}
			if (geographyComboBox.contains(currentGeography)) {
				geographyComboBox.setSelectedItem(currentGeography);
			}
			else {
				//by default, choose the first geography listing
				geographyComboBox.setSelectedItem(0);
			}
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(getParentDialog(), rifServiceException);
		}
	}
	
	
	/**
	 * Validate form.
	 *
	 * @throws RIFJobSubmissionToolException the RIF job submission tool exception
	 */
	private void validateForm() 
		throws RIFServiceException {	

		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String studyName = studyNameTextField.getText().trim();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(studyName)) {
			String studyNameFieldName
				= RIFServiceMessages.getMessage("abstractStudy.name.label");		
			String errorMessage
				= RIFJobSubmissionToolMessages.getMessage(
					"general.error.blankMandatoryField",
					studyNameFieldName);
			errorMessages.add(errorMessage);
		}

		String description = studyDescriptionTextArea.getText().trim();
		if (fieldValidationUtility.isEmpty(description)) {
			String descriptionFieldName
				= RIFServiceMessages.getMessage("abstractStudy.description.label");		
			String errorMessage
				= RIFJobSubmissionToolMessages.getMessage(
					"general.error.blankMandatoryField",
					descriptionFieldName);
			errorMessages.add(errorMessage);
		}
		
		if (errorMessages.size() > 0) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					errorMessages);
			throw rifServiceException;
		}
	}
	
	public void commitChanges() 
		throws RIFServiceException {

		validateForm();
		
		RIFStudySubmissionToolSession rifSession = getRIFSession();

		//set the project associated with the current job submission
		RIFStudySubmission rifJobSubmission
			= rifSession.getRIFJobSubmission();
		Project selectedProject
			= (Project) projectSelectionPanel.getSelectedProject();
		rifJobSubmission.setProject(selectedProject);

		//set description fields for study
		DiseaseMappingStudy currentDiseaseMappingStudy
			= rifSession.getCurrentDiseaseMappingStudy();
		
		//commit field values to the disease mapping study
		currentDiseaseMappingStudy.setName(studyNameTextField.getText().trim());
		currentDiseaseMappingStudy.setDescription(studyDescriptionTextArea.getText().trim());
		Geography geography = (Geography) geographyComboBox.getSelectedItem();
		currentDiseaseMappingStudy.setGeography(geography);
		currentDiseaseMappingStudy.setNewRecord(true);
		
		//commit the study object to the master job submission object
		rifJobSubmission.setStudy(currentDiseaseMappingStudy);
		
		rifSession.addCommittedActivityStep(RIFStudySubmissionActivityStep.DESCRIBE_STUDY);
		
	}
	
}

