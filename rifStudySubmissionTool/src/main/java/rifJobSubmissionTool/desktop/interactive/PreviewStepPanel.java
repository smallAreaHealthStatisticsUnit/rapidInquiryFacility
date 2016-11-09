package rifJobSubmissionTool.desktop.interactive;

import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFStudySubmissionActivityStep;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;
import rifServices.businessConceptLayer.AbstractStudy;
import rifServices.businessConceptLayer.ComparisonArea;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.fileFormats.RIFStudySubmissionHTMLWriter;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import java.awt.GridBagConstraints;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;



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
 * Copyright 2016 Imperial College London, developed by the Small Area
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

final class PreviewStepPanel 
	extends AbstractStepPanel {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	
	
	//GUI Components
	/** The scroll pane. */
	private JScrollPane scrollPane;
	/** The preview report html pane. */
	private JEditorPane previewReportHTMLPane;
	
	private JTextArea otherNotesTextArea;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new preview step panel.
	 *
	 * @param parentDialog the parent dialog
	 * @param rifSession the rif session
	 */
	public PreviewStepPanel(
		JDialog parentDialog, 
		RIFStudySubmissionToolSession rifSession) {
		
		super(parentDialog, rifSession);
		
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		previewReportHTMLPane = userInterfaceFactory.createHTMLEditorPane();	
		scrollPane 
			= userInterfaceFactory.createScrollPane(previewReportHTMLPane);
		
		otherNotesTextArea = userInterfaceFactory.createTextArea();
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
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		
		//Add the title of the panel
		String panelTitleLabelText
			= RIFJobSubmissionToolMessages.getMessage("previewStepPanel.title");
		int stepNumber
			= RIFStudySubmissionActivityStep.CHOOSE_STUDY_AREA.getStepNumber() + 1;
		setPanelTitleInformation(stepNumber, panelTitleLabelText);
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(getTitleLabel(), panelGC);
		
		panelGC.gridy++;
		StringBuilder instructionsText = new StringBuilder();
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("previewStepPanel.instructions1"));
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("previewStepPanel.instructions2"));
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("previewStepPanel.instructions3"));
		panel.add(createInstructionPanel(instructionsText.toString()), panelGC);
		
		panelGC.gridy++;		
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 0.80;
		
		panel.add(scrollPane, panelGC);		
		scrollPane.getVerticalScrollBar().setValue(0);
		
		panelGC.gridy++;
		panelGC.weighty = 0.20;
		panel.add(createOtherNotesPanel(), panelGC);
		
	}
	
	private JPanel createOtherNotesPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel 
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		String otherNotesLabelText
			= RIFServiceMessages.getMessage("abstractStudy.otherNotes.label");
		JLabel otherNotesLabel
			= userInterfaceFactory.createLabel(otherNotesLabelText);
		panel.add(otherNotesLabel, panelGC);

		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		String otherNotesInstructionsText
			= RIFJobSubmissionToolMessages.getMessage("previewStepPanel.otherNotesInstructions");
		JLabel instructionsLabel
			= userInterfaceFactory.createInstructionLabel(otherNotesInstructionsText);
		instructionsLabel.setText(otherNotesInstructionsText);
		instructionsLabel.setBorder(LineBorder.createGrayLineBorder());
		panel.add(instructionsLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		JScrollPane otherNotesScrollPane
			 = userInterfaceFactory.createScrollPane(otherNotesTextArea);
		panel.add(otherNotesScrollPane, panelGC);
		
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

	@Override
	public void initialiseForm() 
		throws RIFServiceException {
		
		RIFStudySubmissionToolSession rifSession = getRIFSession();

		RIFStudySubmission originalJobSubmission = rifSession.getRIFJobSubmission();	
		try {
			RIFStudySubmissionHTMLWriter rifJobSubmissionWriter
				= new RIFStudySubmissionHTMLWriter();
			String htmlReport
				= rifJobSubmissionWriter.writeJobSubmission(originalJobSubmission);
			previewReportHTMLPane.setText(htmlReport);	
		
			AbstractStudy study = originalJobSubmission.getStudy();
			otherNotesTextArea.setText(study.getOtherNotes());
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getParentDialog(), 
				rifServiceException.getErrorMessages());
		}
		
		buildUI();		
	}


	@Override
	public void commitChanges() 
		throws RIFServiceException {
		
		//validate the form first
			
		RIFStudySubmissionToolSession rifSession = getRIFSession();
		ComparisonArea workingCopyComparisonArea
			= (ComparisonArea) rifSession.getCurrentGeographicalArea();
		workingCopyComparisonArea.setNewRecord(false);
				
		RIFStudySubmission originalJobSubmission
			= rifSession.getRIFJobSubmission();
		DiseaseMappingStudy originalStudy
			= (DiseaseMappingStudy) originalJobSubmission.getStudy();
		originalStudy.setComparisonArea(workingCopyComparisonArea);
		
		rifSession.addCommittedActivityStep(RIFStudySubmissionActivityStep.CHOOSE_COMPARISON_AREA);
		
		
		AbstractStudy study
			= originalJobSubmission.getStudy();
		study.setOtherNotes(otherNotesTextArea.getText().trim());
	}
	
	/**
	 * Validate form.
	 *
	 * @throws RIFServiceException the RIF service exception
	 */
	public void validateForm() throws RIFServiceException {

	}
}

