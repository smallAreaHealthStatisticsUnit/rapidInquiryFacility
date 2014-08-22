package rifJobSubmissionTool.desktop.interactive;

import rifGenericUILibrary.ErrorDialog;
import rifGenericUILibrary.OKClosePanel;
import rifGenericUILibrary.OrderedListComboBox;
import rifGenericUILibrary.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFSession;


import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.AgeGroup;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.AgeGroupSortingOption;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceException;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JLabel;
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
 *<p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *</p>
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

class AgeBandEditorDialog 
	implements ActionListener {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	/** The rif session. */
	private RIFSession rifSession;	
	/** The is cancelled. */
	private boolean isCancelled;
	/** The current investigation. */
	private Investigation currentInvestigation;	
	/** The current age band. */
	private AgeBand currentAgeBand;
	
	//GUI Components
	/** The lower limit age group combo box. */
	private OrderedListComboBox lowerLimitAgeGroupComboBox;
	/** The upper limit age group combo box. */
	private OrderedListComboBox upperLimitAgeGroupComboBox;
	/** The panel. */
	private JPanel panel;	
	/** The dialog. */
	private JDialog dialog;	
	/** The ok close panel. */
	private OKClosePanel okClosePanel;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new age band editor dialog.
	 *
	 * @param rifSession the rif session
	 */
	public AgeBandEditorDialog(
		RIFSession rifSession) {
		
		this.rifSession = rifSession;
		UserInterfaceFactory userInterfaceFactory
			= rifSession.getUIFactory();
		
		String titleText
			= RIFJobSubmissionToolMessages.getMessage("ageBandEditorDialog.title");
		dialog = userInterfaceFactory.createDialog(titleText);
		
		panel = userInterfaceFactory.createPanel();
		
		lowerLimitAgeGroupComboBox
			= new OrderedListComboBox(userInterfaceFactory);
		lowerLimitAgeGroupComboBox.setAlphabeticallySortItems(false);
		upperLimitAgeGroupComboBox
			= new OrderedListComboBox(userInterfaceFactory);
		upperLimitAgeGroupComboBox.setAlphabeticallySortItems(false);

		okClosePanel = new OKClosePanel(userInterfaceFactory);
		okClosePanel.addActionListener(this);
		
		dialog.getContentPane().add(panel);
		dialog.setModal(true);
		dialog.setSize(300, 100);
	}
	
	/**
	 * Builds the ui.
	 */
	private void buildUI() {
		
		panel.removeAll();
		UserInterfaceFactory userInterfaceFactory
			= rifSession.getUIFactory();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		panel.add(createLowerAndUpperLimitPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panel.add(okClosePanel.getPanel(), panelGC);		
	}
	
	/**
	 * Creates the lower and upper limit panel.
	 *
	 * @return the j panel
	 */
	private JPanel createLowerAndUpperLimitPanel() {
		
		UserInterfaceFactory userInterfaceFactory
			= rifSession.getUIFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		String lowerLimitAgeGroupText
			= RIFServiceMessages.getMessage("ageBand.lowerLimit.label");
		JLabel lowerLimitLabel
			= userInterfaceFactory.createLabel(lowerLimitAgeGroupText);
		panel.add(lowerLimitLabel, panelGC);
		panelGC.gridx++;		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(lowerLimitAgeGroupComboBox.getComboBox(), panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		String toText
			= RIFJobSubmissionToolMessages.getMessage("general.labels.to");
		JLabel toLabel 
			= userInterfaceFactory.createLabel("  " + toText + " ");
		panel.add(toLabel, panelGC);
				
		panelGC.gridx++;
		String upperLimitAgeGroupText
			= RIFServiceMessages.getMessage("ageBand.upperLimit.label");
		JLabel upperLimitLabel
			= userInterfaceFactory.createLabel(upperLimitAgeGroupText);
		panel.add(upperLimitLabel, panelGC);
		panelGC.gridx++;		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(upperLimitAgeGroupComboBox.getComboBox(), panelGC);
		
		return panel;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Show.
	 */
	public void show() {
		
		dialog.setVisible(true);
	}
	
	/**
	 * Ok.
	 */
	private void ok() {
		
		dialog.setVisible(false);
		isCancelled = false;
		
		AgeGroup lowerLimitAgeGroup
			= (AgeGroup) lowerLimitAgeGroupComboBox.getSelectedItem();
		currentAgeBand.setLowerLimitAgeGroup(lowerLimitAgeGroup);
		
		AgeGroup upperLimitAgeGroup
			= (AgeGroup) upperLimitAgeGroupComboBox.getSelectedItem();
		currentAgeBand.setUpperLimitAgeGroup(upperLimitAgeGroup);
		
	}
	
	/**
	 * Close.
	 */
	private void close() {
		
		dialog.setVisible(false);
		isCancelled = true;
	}
	
	/**
	 * Checks if is cancelled.
	 *
	 * @return true, if is cancelled
	 */
	public boolean isCancelled() {
		
		return isCancelled;
	}
	
	/**
	 * Gets the age band.
	 *
	 * @return the age band
	 */
	public AgeBand getAgeBand() {		
		
		AgeGroup lowerAgeGroup
			= (AgeGroup) lowerLimitAgeGroupComboBox.getSelectedItem();
		currentAgeBand.setLowerLimitAgeGroup(lowerAgeGroup);

		AgeGroup upperAgeGroup
			= (AgeGroup) upperLimitAgeGroupComboBox.getSelectedItem();
		currentAgeBand.setUpperLimitAgeGroup(upperAgeGroup);
		
		return currentAgeBand;
	}
	
	/**
	 * Initialise.
	 *
	 * @param currentInvestigation the current investigation
	 * @param currentAgeBand the current age band
	 */
	public void initialise(
		final Investigation currentInvestigation,
		final AgeBand currentAgeBand) {
		
		this.currentInvestigation = currentInvestigation;
		this.currentAgeBand = currentAgeBand;
		initialiseAgeGroupChoices();
		buildUI();
	}
	
	/**
	 * Initialise age group choices.
	 */
	private void initialiseAgeGroupChoices() {
		
		RIFStudySubmissionAPI service
			= rifSession.getRIFStudySubmissionService();
		
		try {
			//update the Year List
			User currentUser = rifSession.getUser();
			Geography currentGeography
				= rifSession.getCurrentGeography();
			NumeratorDenominatorPair currentNDPair
				= currentInvestigation.getNdPair();
			
			//we know from this api method that the age groups will
			//be returned in the ascending order of 
			ArrayList<AgeGroup> ageGroupsByLowerLimit
				= service.getAgeGroups(
					currentUser, 
					currentGeography, 
					currentNDPair,
					AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);

			for (AgeGroup ageGroupByLowerLimit : ageGroupsByLowerLimit) {
				ageGroupByLowerLimit.showOnlyLowerLimitInDisplayName();
				lowerLimitAgeGroupComboBox.addListItem(ageGroupByLowerLimit);
			}
			
			ArrayList<AgeGroup> ageGroupsByUpperLimit
				= service.getAgeGroups(
					currentUser, 
					currentGeography, 
					currentNDPair,
					AgeGroupSortingOption.DESCENDING_UPPER_LIMIT);

			for (AgeGroup ageGroupByUpperLimit : ageGroupsByUpperLimit) {
				ageGroupByUpperLimit.showOnlyUpperLimitInDisplayName();
				upperLimitAgeGroupComboBox.addListItem(ageGroupByUpperLimit);
			}

			
			lowerLimitAgeGroupComboBox.selectFirstItem();
			upperLimitAgeGroupComboBox.selectLastItem();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(dialog, rifServiceException);
		}
				
	}
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	public void actionPerformed(
		ActionEvent event) {
		
		Object source = event.getSource();
		
		if (okClosePanel.isOKButton(source)) {
			ok();
		}
		else if (okClosePanel.isCloseButton(source)) {
			close();
		}		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}

