package rifJobSubmissionTool.desktop.interactive;

import rifGenericUILibrary.UserInterfaceFactory;
import rifJobSubmissionTool.system.MapAreaSelectionBasket;
import rifJobSubmissionTool.system.RIFStudySubmissionActivityStep;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;

import rifServices.system.RIFServiceException;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.ComparisonArea;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.RIFStudySubmission;

import java.awt.GridBagConstraints;
import java.util.Observer;
import java.util.Observable;

import javax.swing.JDialog;
import javax.swing.JPanel;


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

class SpecifyComparisonAreaStepPanel 
	extends AbstractStepPanel 
	implements Observer {
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================	
	
	//Data
	private Geography currentGeography;
	
	/** The current comparison area. */
	private ComparisonArea currentComparisonArea;	
	/** The current map area selection basket. */
	private MapAreaSelectionBasket currentMapAreaSelectionBasket;	
	
	//GUI Components
	/** The geographical extent panel. */
	private GeographicalExtentPanel geographicalExtentPanel;
	/** The map area list selection panel. */
	private MapAreaListSelectionPanel mapAreaListSelectionPanel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new specify comparison area step panel.
	 *
	 * @param parentDialog the parent dialog
	 * @param rifSession the rif session
	 */
	public SpecifyComparisonAreaStepPanel(
		JDialog parentDialog, 
		RIFStudySubmissionToolSession rifSession) {
		super(parentDialog, rifSession);
		
		geographicalExtentPanel 
			= new GeographicalExtentPanel(parentDialog, rifSession);
		geographicalExtentPanel.addObserver(this);
		mapAreaListSelectionPanel
			= new MapAreaListSelectionPanel(parentDialog, rifSession);	
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
			= RIFJobSubmissionToolMessages.getMessage("specifyComparisonAreaStepPanel.title");
		int stepNumber
			= RIFStudySubmissionActivityStep.CHOOSE_STUDY_AREA.getStepNumber() + 1;
		setPanelTitleInformation(stepNumber, panelTitleLabelText);
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(getTitleLabel(), panelGC);
		
		panelGC.gridy++;
		StringBuilder instructionsText = new StringBuilder();
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyGeographicalAreaStepPanel.instructions1"));
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyGeographicalAreaStepPanel.instructions2"));
		instructionsText.append(RIFJobSubmissionToolMessages.getMessage("specifyGeographicalAreaStepPanel.instructions3"));
		panel.add(createInstructionPanel(instructionsText.toString()), panelGC);
		
		panelGC.gridy++;
		panel.add(geographicalExtentPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		panel.add(mapAreaListSelectionPanel.getPanel(), panelGC);
		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	/**
	 * Validate form.
	 *
	 * @throws RIFJobSubmissionToolException the RIF job submission tool exception
	 */
	public void validateForm() 
		throws RIFServiceException {

		mapAreaListSelectionPanel.validateForm();
		
		int numberOfBasketItems
			= currentMapAreaSelectionBasket.getSize();
		if (numberOfBasketItems == 0) {
			String errorMessage
				= RIFJobSubmissionToolMessages.getMessage(
					"specifyGeographicalAreaStepPanel.error.noBasketItems");
			RIFServiceException rifJobSubmissionToolException
				= new RIFServiceException(errorMessage);
			throw rifJobSubmissionToolException;
		}		
	}	
	
	// ==========================================
	// Section Interfaces
	// ==========================================
	//Interface: Observer
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(
		Observable observedObject, 
		Object argument) {

		if (argument == GeographicalExtentPanel.ObservableEvent.APPLY_NEW_SETTINGS) {
			mapAreaListSelectionPanel.initialiseApplySettings(
				currentGeography,
				currentComparisonArea,
				currentMapAreaSelectionBasket);
		}
		else if (argument == GeographicalExtentPanel.ObservableEvent.RESET) {
			mapAreaListSelectionPanel.initialiseResetSettings(
				currentComparisonArea,
				currentMapAreaSelectionBasket);
		}		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

	/* (non-Javadoc)
	 * @see rifJobSubmissionTool.desktopApplication.AbstractStepPanel#initialiseForm()
	 */
	@Override
	public void initialiseForm() 
		throws RIFServiceException {

		RIFStudySubmissionToolSession rifSession = getRIFSession();

		currentMapAreaSelectionBasket
			= MapAreaSelectionBasket.newInstance();	
		RIFStudySubmission originalJobSubmission = rifSession.getRIFJobSubmission();
		DiseaseMappingStudy originalStudy
			= (DiseaseMappingStudy) originalJobSubmission.getStudy();
		currentGeography = originalStudy.getGeography();
		if (rifSession.isActivityStepCommitted(RIFStudySubmissionActivityStep.CHOOSE_COMPARISON_AREA)) {
			//Derive the working copy based on the study area that 
			//has already been committed in the master job submission

			ComparisonArea originalComparisonArea
				= originalStudy.getComparisonArea();
			currentComparisonArea
				= ComparisonArea.createCopy(originalComparisonArea);			
			//populate the basket with the map areas that are already in the
			//existing disease mapping study area
			rifSession.setCurrentGeographicalArea(currentComparisonArea);	
			currentMapAreaSelectionBasket.addMapAreas(currentComparisonArea.getMapAreas());			

			geographicalExtentPanel.initialiseApplySettings(
				currentComparisonArea,
				currentMapAreaSelectionBasket);
			mapAreaListSelectionPanel.initialiseApplySettings(
				originalStudy.getGeography(),
				currentComparisonArea,
				currentMapAreaSelectionBasket);		
		}
		else {
			currentComparisonArea = ComparisonArea.newInstance();
			rifSession.setCurrentGeographicalArea(currentComparisonArea);	
			
			geographicalExtentPanel.initialiseResetSettings(
				currentComparisonArea,
				currentMapAreaSelectionBasket);
			mapAreaListSelectionPanel.initialiseResetSettings(
				currentComparisonArea,
				currentMapAreaSelectionBasket);
		}
		
		buildUI();		
	}

	
	/* (non-Javadoc)
	 * @see rifJobSubmissionTool.desktopApplication.AbstractStepPanel#commitChanges()
	 */
	@Override
	public void commitChanges() 
		throws RIFServiceException {

		//validate the form first
		validateForm();
	
		geographicalExtentPanel.commitChanges();
		mapAreaListSelectionPanel.commitChanges();
		
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
	}
	
}

