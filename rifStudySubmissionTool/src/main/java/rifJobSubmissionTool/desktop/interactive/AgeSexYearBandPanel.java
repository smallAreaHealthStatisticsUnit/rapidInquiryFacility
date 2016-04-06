
package rifJobSubmissionTool.desktop.interactive;

import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.Sex;
import rifServices.businessConceptLayer.YearInterval;
import rifServices.businessConceptLayer.YearRange;
import rifGenericLibrary.util.FieldValidationUtility;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JComboBox;
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


final class AgeSexYearBandPanel 
	implements ActionListener {


// ==========================================
// Section Constants
// ==========================================
	/** The Constant HORIZONTAL_GAP_DISTANCE. */
	private static final int HORIZONTAL_GAP_DISTANCE = 10;

	/** The Constant NONE_CHOICE. */
	private static final String NONE_CHOICE
		= RIFServiceMessages.getMessage("general.choices.none");
	
	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	/** The rif session. */
	private RIFStudySubmissionToolSession rifSession;	
	/** The service. */
	private RIFStudySubmissionAPI service;
	
	//GUI Components
	/** The parent dialog. */
	private JDialog parentDialog;	
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;
	/** The panel. */
	private JPanel panel;		
	/** The age band list panel. */
	private AgeBandListPanel ageBandListPanel;
	/** The start year interval combo box. */
	private JComboBox<String> startYearIntervalComboBox;	
	/** The end year interval combo box. */
	private JComboBox<String> endYearIntervalComboBox;
	/** The sex combo box. */
	private JComboBox<String> sexComboBox;
	/** The interval combo box. */
	private JComboBox<String> intervalComboBox;

    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new age sex year band panel.
     *
     * @param parentDialog the parent dialog
     * @param rifSession the rif session
     */
	public AgeSexYearBandPanel(
    	JDialog parentDialog,
    	RIFStudySubmissionToolSession rifSession) {
		
    	this.parentDialog = parentDialog;
    	this.rifSession = rifSession;
    	userInterfaceFactory = rifSession.getUIFactory();
    	service = rifSession.getRIFStudySubmissionService();
    	//user = rifSession.getUser();

		panel = userInterfaceFactory.createPanel();
    	
    	ageBandListPanel 
    		= new AgeBandListPanel(parentDialog, rifSession);
		startYearIntervalComboBox
			= userInterfaceFactory.createComboBox(new Vector<String>());
		startYearIntervalComboBox.setPrototypeDisplayValue("2013");
		endYearIntervalComboBox
			= userInterfaceFactory.createComboBox(new Vector<String>());
		endYearIntervalComboBox.setPrototypeDisplayValue("2013");		
		intervalComboBox
			= userInterfaceFactory.createComboBox(new Vector<String>());
		endYearIntervalComboBox.setPrototypeDisplayValue("11");	
		sexComboBox
			= userInterfaceFactory.createComboBox(new Vector<String>());
		sexComboBox.setPrototypeDisplayValue("Both");	
		
		buildUI();
    }

    /**
     * Builds the ui.
     */
    private void buildUI() {
    	
    	panel.removeAll();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.ipadx = HORIZONTAL_GAP_DISTANCE;
		panelGC.ipady = 10;
		
		String panelTitleLabelText
			= RIFJobSubmissionToolMessages.getMessage("ageSexYearBandsPanel.title.label");
		JLabel panelTitleLabel
			= userInterfaceFactory.createLabel(panelTitleLabelText);
		userInterfaceFactory.setBoldFont(panelTitleLabel);
		panel.add(panelTitleLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(createSexPanel(), panelGC);
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		panel.add(ageBandListPanel.getPanel(), panelGC);
		panelGC.gridy++;		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(createYearBandPanel(), panelGC);
    	
    	
    }
    
    /**
     * Creates the sex panel.
     *
     * @return the j panel
     */
    private JPanel createSexPanel() {
    	
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();

		String sexLabelText
			= RIFServiceMessages.getMessage("sex.label");
		JLabel sexLabel = userInterfaceFactory.createLabel(sexLabelText);
		sexLabel.setText(sexLabelText);
		String sexLabelToolTipText
			= RIFServiceMessages.getMessage("sex.toolTip");
		sexLabel.setToolTipText(sexLabelToolTipText);
		panel.add(sexLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(sexComboBox, panelGC);
		
		return panel;
    }
    
	/**
	 * Creates the year band panel.
	 *
	 * @return the j panel
	 */
	private JPanel createYearBandPanel() {		
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.ipadx = HORIZONTAL_GAP_DISTANCE;
		
		String yearBandLabelText
			= RIFServiceMessages.getMessage("yearRange.label");
		JLabel yearBandLabel
			= userInterfaceFactory.createLabel(yearBandLabelText);
		String yearBandLabelToolTipText
			= RIFServiceMessages.getMessage("yearRange.toolTip");
		yearBandLabel.setToolTipText(yearBandLabelToolTipText);
		panel.add(yearBandLabel, panelGC);
		
		panelGC.gridx++;		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.33;
		panel.add(startYearIntervalComboBox, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String toText
			= RIFJobSubmissionToolMessages.getMessage("general.labels.to");
		JLabel toLabel 
			= userInterfaceFactory.createLabel("  " + toText);
		panel.add(toLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.33;
		panel.add(endYearIntervalComboBox, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String intervalLabelText
			= RIFServiceMessages.getMessage("investigation.interval.label");
		JLabel intervalLabel
			= userInterfaceFactory.createLabel("  " + intervalLabelText);
		String intervalLabelToolTipText
			= RIFServiceMessages.getMessage("investigation.interval.toolTip");
		intervalLabel.setToolTipText(intervalLabelToolTipText);
		panel.add(intervalLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.33;
		panel.add(intervalComboBox, panelGC);
				
		return panel;
	}	
		
// ==========================================
// Section Accessors and Mutators
// ==========================================
	/**
	 * Gets the panel.
	 *
	 * @return the panel
	 */
	public JPanel getPanel() {
	
		return panel;
	}
	
	/**
	 * Populate field choices.
	 *
	 * @param currentInvestigation the current investigation
	 */
	public void populateFieldChoices(
		Investigation currentInvestigation) {

		//update the Year List
		YearRange yearRange
			= currentInvestigation.getYearRange();

		startYearIntervalComboBox.removeAllItems();	
		ArrayList<String> startYearList = yearRange.getLowToHighYears();
		for (String startYear : startYearList) {
			startYearIntervalComboBox.addItem(startYear);
		}
		startYearIntervalComboBox.setSelectedIndex(0);
		
		endYearIntervalComboBox.removeAllItems();
		ArrayList<String> endYearList = yearRange.getHighToLowYears();
		for (String endYear : endYearList) {
			endYearIntervalComboBox.addItem(endYear);
		}
		int sizeOfEndYearList = endYearList.size();
		endYearIntervalComboBox.setSelectedIndex(sizeOfEndYearList - 1);
		
		updateIntervalValues();		
	}

	/**
	 * Update.
	 *
	 * @param currentNDPair the current nd pair
	 */
	public void update(		
		NumeratorDenominatorPair currentNDPair) {
		
		try {
			User currentUser = rifSession.getUser();
			Geography currentGeography 
				= rifSession.getCurrentGeography();
			
			//Update the age group choices

			//update the Year List
			YearRange yearRange
				= service.getYearRange(
					currentUser, 
					currentGeography, 
					currentNDPair);
						
			startYearIntervalComboBox.removeAllItems();	
			ArrayList<String> startYearList = yearRange.getLowToHighYears();
			for (String startYear : startYearList) {
				startYearIntervalComboBox.addItem(startYear);
			}
			
			endYearIntervalComboBox.removeAllItems();
			ArrayList<String> endYearList = yearRange.getHighToLowYears();
			for (String endYear : endYearList) {
				endYearIntervalComboBox.addItem(endYear);
			}
			
			endYearIntervalComboBox.doLayout();
			endYearIntervalComboBox.repaint();

			updateIntervalValues();
						
			sexComboBox.removeAllItems();
			ArrayList<String> sexNameList
				= Sex.getSexNameList(service.getSexes(currentUser));
			for (String sexName : sexNameList) {
				sexComboBox.addItem(sexName);
			}			

			intervalComboBox.removeAllItems();
			ArrayList<String> intervalValueList 
				= yearRange.getYearIntervalChoices();	
			for (String intervalValue : intervalValueList) {
				intervalComboBox.addItem(intervalValue);
			}
			intervalComboBox.insertItemAt(NONE_CHOICE, 0);
						
			panel.updateUI();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException.getErrorMessages());
		}		
	}
	
	/**
	 * Update interval values.
	 */
	private void updateIntervalValues() {
		
		intervalComboBox.removeAllItems();
		//Obtain the currently selected start and end years
		
		String startYearPhrase
			= (String) startYearIntervalComboBox.getSelectedItem();
		String endYearPhrase
			= (String) endYearIntervalComboBox.getSelectedItem();
		
		String choosePhrase
			= RIFServiceMessages.getMessage("general.choices.choose");
		Collator collator = RIFServiceMessages.getCollator();
		if (collator.equals(choosePhrase, startYearPhrase) ||
			collator.equals(choosePhrase, endYearPhrase)) {
			//either of start or end years is not defined.
			//therefore, interval will be undefined as well
			intervalComboBox.addItem(choosePhrase);
			intervalComboBox.setSelectedIndex(0);
			intervalComboBox.setEnabled(false);
		}
		else {
			YearRange currentYearRange 
				= YearRange.newInstance(startYearPhrase, endYearPhrase);
			ArrayList<String> intervalValueList 
				= currentYearRange.getYearIntervalChoices();	
			for (String intervalValue : intervalValueList) {
				intervalComboBox.addItem(intervalValue);
			}
			intervalComboBox.insertItemAt(NONE_CHOICE, 0);
			intervalComboBox.setEnabled(true);				
			intervalComboBox.setSelectedItem(0);
		}
	}
	
	/**
	 * Gets the year range.
	 *
	 * @return the year range
	 */
	public YearRange getYearRange() {
		
		String startYearPhrase
			= (String) startYearIntervalComboBox.getSelectedItem();
		String endYearPhrase
			= (String) endYearIntervalComboBox.getSelectedItem();
		YearRange currentYearRange 
			= YearRange.newInstance(startYearPhrase, endYearPhrase);
		return currentYearRange;
	}

	/**
	 * Gets the interval value.
	 *
	 * @return the interval value
	 */
	public String getIntervalValue() {
		
		String selectedIntervalChoice
			= (String) intervalComboBox.getSelectedItem();
		return selectedIntervalChoice;
	}
	
	/**
	 * Gets the age bands.
	 *
	 * @return the age bands
	 */
	public ArrayList<AgeBand> getAgeBands() {
		
		ArrayList<AgeBand> ageBands
			= ageBandListPanel.getAgeBands();
		return ageBands;
	}
	
	/**
	 * Gets the year intervals.
	 *
	 * @return the year intervals
	 */
	public ArrayList<YearInterval> getYearIntervals() {
		
		ArrayList<YearInterval> yearIntervals = new ArrayList<YearInterval>();
		try {
			//Make a year range based on the currently selected start and end years
			String startYearPhrase
				= (String) startYearIntervalComboBox.getSelectedItem();
			String endYearPhrase
				= (String) endYearIntervalComboBox.getSelectedItem();
			YearRange yearRange = YearRange.newInstance(startYearPhrase, endYearPhrase);
			String intervalPhrase	
				= (String) intervalComboBox.getSelectedItem();
			
			Collator collator = RIFServiceMessages.getCollator();
			if (collator.equals(intervalPhrase, NONE_CHOICE)) {
				//if there is no interval then we will treat
				//the lower and upper limits as one single year interval				
				YearInterval yearInterval
					= YearRange.getYearInterval(yearRange);
				yearIntervals.add(yearInterval);				
				return yearIntervals;
			}
			
			int intervalValue = Integer.valueOf(intervalPhrase);		
			yearIntervals = yearRange.splitYearRange(intervalValue);			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				parentDialog, 
				rifServiceException.getErrorMessages());
		}
		return yearIntervals;
	}
	
	/**
	 * Gets the selected sex.
	 *
	 * @return the selected sex
	 */
	public Sex getSelectedSex() {
		
		String selectedSexFieldValue = (String) sexComboBox.getSelectedItem();
	
		Sex selectedSex = Sex.getSexFromName(selectedSexFieldValue);			
		return selectedSex;	
	}
	
	/**
	 * Clear action listeners.
	 */
	private void clearActionListeners() {
		
		ActionListener[] startYearListeners
			= startYearIntervalComboBox.getActionListeners();
		for (ActionListener startYearListener : startYearListeners) {
			startYearIntervalComboBox.removeActionListener(startYearListener);
		}
		
		ActionListener[] endYearListeners
			= endYearIntervalComboBox.getActionListeners();
		for (ActionListener endYearListener : endYearListeners) {
			endYearIntervalComboBox.removeActionListener(endYearListener);
		}
	}
	
	/**
	 * Adds the action listeners.
	 */
	private void addActionListeners() {
		
		startYearIntervalComboBox.addActionListener(this);
		endYearIntervalComboBox.addActionListener(this);
	}
	
	/**
	 * Populate form.
	 *
	 * @param currentInvestigation the current investigation
	 */
	public void populateForm(
		Investigation currentInvestigation) {
		
		clearActionListeners();
				
		ageBandListPanel.initialise(currentInvestigation);
		populateFieldChoices(currentInvestigation);

		String interval = currentInvestigation.getInterval();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(interval)) {
			//nothing selected
			intervalComboBox.setSelectedItem(NONE_CHOICE);			
		}
		else {
			intervalComboBox.setSelectedItem(interval);			
		}

		sexComboBox.removeAllItems();
		User currentUser = rifSession.getUser();
		try {
			ArrayList<String> sexNameList
				= Sex.getSexNameList(service.getSexes(currentUser));
			for (String sexName : sexNameList) {
				sexComboBox.addItem(sexName);
			}
			
			Sex currentSex = currentInvestigation.getSex();
			sexComboBox.setSelectedItem(currentSex.getName());			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				parentDialog, 
				rifServiceException.getErrorMessages());			
		}
				
		ArrayList<YearInterval> yearIntervals 
			= currentInvestigation.getYearIntervals();
		Integer currentLowerYearValue = YearInterval.getMinimumLowerLimit(yearIntervals);
		startYearIntervalComboBox.setSelectedItem(String.valueOf(currentLowerYearValue));
		
		Integer currentUpperYearValue = YearInterval.getMaximumUpperLimit(yearIntervals);
		endYearIntervalComboBox.setSelectedItem(String.valueOf(currentUpperYearValue));
		
		updateIntervalValues();
		addActionListeners();
	}
		
// ==========================================
// Section Errors and Validation
// ==========================================

// ==========================================
// Section Interfaces
// ==========================================
	//Interface: Action Listener
	

	public void actionPerformed(ActionEvent event) {
		
		//this will be either the start year combination box or
		//the end year combination box
		updateIntervalValues();
	}
	
// ==========================================
// Section Override
// ==========================================




}
