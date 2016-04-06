
package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;

import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.YearInterval;
import rifServices.businessConceptLayer.YearRange;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;


import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.presentationLayer.OrderedListComboBox;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.businessConceptLayer.User;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.text.Collator;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;

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

final class InvestigationEditorDialog 
	implements ActionListener {
		
// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	//Data
	/** The service. */
	private RIFStudySubmissionAPI service;	
	/** The rif session. */
	private RIFStudySubmissionToolSession rifSession;	
	/** The current user. */
	private User currentUser;	
	/** The is cancelled. */
	private boolean isCancelled;	
	/** The current investigation. */
	private Investigation currentInvestigation;	
		
	//GUI Components	
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;	
	/** The dialog. */
	private JDialog dialog;	
	/** The title text field. */
	private JTextField titleTextField;	
	/** The description field */
	private JTextArea descriptionTextArea;
		
	/** The health theme combo box. */
	private OrderedListComboBox healthThemeComboBox;			
	/** The numerator denominator combo box. */
	private OrderedListComboBox numeratorDenominatorComboBox;	
	/** The health code list panel. */
	private HealthCodeListPanel healthCodeListPanel;	
	/** The age sex year band panel. */
	private AgeSexYearBandPanel ageSexYearBandPanel;	
	/** The denominator information text area. */
	private JTextArea denominatorInformationTextArea;
	/** The covariates list panel. */
	private CovariatesListPanel covariatesListPanel;	
	/** The ok close panel. */
	private OKCloseButtonPanel okCloseButtonPanel;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new investigation editor dialog.
     *
     * @param rifSession the rif session
     */
	public InvestigationEditorDialog(
		RIFStudySubmissionToolSession rifSession) {
   
		this.rifSession = rifSession;
    	service = rifSession.getRIFStudySubmissionService();
    	currentUser = rifSession.getUser();
		userInterfaceFactory = rifSession.getUIFactory();

		currentInvestigation = Investigation.newInstance();
		titleTextField
			= userInterfaceFactory.createTextField();
		descriptionTextArea
			= userInterfaceFactory.createTextArea();
		healthThemeComboBox 
			= new OrderedListComboBox(userInterfaceFactory);
		numeratorDenominatorComboBox 
			= new OrderedListComboBox(userInterfaceFactory);
		ageSexYearBandPanel 
			= new AgeSexYearBandPanel(dialog, rifSession);
		healthCodeListPanel
			= new HealthCodeListPanel(dialog, rifSession);
		denominatorInformationTextArea
			= userInterfaceFactory.createNonEditableTextArea(5,  30);
		covariatesListPanel = new CovariatesListPanel(rifSession);
				
		okCloseButtonPanel = new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		
		buildUI();
    }

    /**
     * Builds the ui.
     */
    private void buildUI() {
    	
		String dialogTitle
			= RIFJobSubmissionToolMessages.getMessage(
				"investigationEditorDialog.title");
		dialog
			= userInterfaceFactory.createDialog(dialogTitle);
	
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(createTitleNumeratorPanel(), panelGC);
	
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1.0;
		panelGC.weighty = 0.7;
		panel.add(healthCodeListPanel.getPanel(), panelGC);
			
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(ageSexYearBandPanel.getPanel(), panelGC);
			
		panelGC.gridy++;	
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 0.3;		
		panel.add(covariatesListPanel.getPanel(), panelGC);
	
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;		
		panel.add(okCloseButtonPanel.getPanel(), panelGC);
	
		dialog.getContentPane().add(panel);
		dialog.setModal(true);
		dialog.setSize(600, 650);    	
    }
        
	/**
	 * Creates the title numerator panel.
	 *
	 * @return the j panel
	 */
	private JPanel createTitleNumeratorPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		//Add title field
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0;		
		String titleLabelText
			= RIFServiceMessages.getMessage("investigation.title.label");
		JLabel titleLabel
			= userInterfaceFactory.createLabel(titleLabelText);
		String titleLabelToolTipText
			= RIFServiceMessages.getMessage("investigation.title.label.toolTip");
		titleLabel.setToolTipText(titleLabelToolTipText);
		panel.add(titleLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		panel.add(titleTextField, panelGC);
		
		panelGC.gridy++;
		
		//Add the investigation description field
		panelGC.gridx = 0;
		String descriptionLabelText
			= RIFServiceMessages.getMessage("investigation.description.label");
		JLabel descriptionLabel
			= userInterfaceFactory.createLabel(descriptionLabelText);
		String descriptionLabelToolTipText
			= RIFServiceMessages.getMessage("investigation.description.label.toolTip");
		descriptionLabel.setToolTipText(descriptionLabelToolTipText);	
		panel.add(descriptionLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;		
		JScrollPane investigationDescriptionScrollPane
			= userInterfaceFactory.createScrollPane(descriptionTextArea);
		panel.add(investigationDescriptionScrollPane, panelGC);
		
		panelGC.gridy++;
		
		//Add health theme field
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String healthThemeLabelText
			= RIFServiceMessages.getMessage("healthTheme.label");
		JLabel healthThemeLabel
			= userInterfaceFactory.createLabel(healthThemeLabelText);
		String healthThemeLabelToolTipText
			= RIFServiceMessages.getMessage("healthTheme.toolTip");
		healthThemeLabel.setToolTipText(healthThemeLabelToolTipText);
		panel.add(healthThemeLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx= 1;		
				
		panel.add(healthThemeComboBox.getComboBox(), panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		String numeratorLabelText
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.numerator.label");
		JLabel numeratorLabel
			= userInterfaceFactory.createLabel(numeratorLabelText);
		String numeratorLabelToolTipText
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.numerator.toolTip");
		numeratorLabel.setToolTipText(numeratorLabelToolTipText);
		panel.add(numeratorLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;		
				
		panel.add(numeratorDenominatorComboBox.getComboBox(), panelGC);
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(createDenominatorPanel(), panelGC);
					
		return panel;
	}
	
	/**
	 * Creates the denominator panel.
	 *
	 * @return the jpanel
	 */
	private JPanel createDenominatorPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		String denominatorLabelText
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.denominator.label");
		JLabel denominatorLabel
			= userInterfaceFactory.createLabel(denominatorLabelText);
		String denominatorLabelToolTipText
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.denominator.toolTip");
		denominatorLabel.setToolTipText(denominatorLabelToolTipText);
		userInterfaceFactory.setBoldFont(denominatorLabel);
		panel.add(denominatorLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1.0;
		panelGC.weighty = 1.0;
		
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(denominatorInformationTextArea);
		panel.add(scrollPane, panelGC);
		
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
     * Checks if is cancelled.
     *
     * @return true, if is cancelled
     */
    public boolean isCancelled() {

    	return isCancelled;
    }

    /**
     * Sets the investigation.
     *
     * @param currentInvestigation the new investigation
     */
    public void setInvestigation(
    	Investigation currentInvestigation) {

    	try {  		
    		this.currentInvestigation = currentInvestigation;
    	
    		if (currentInvestigation.isNewRecord()) {
    			//populate all aspects of the current investigation and let
    			//the the forms populate 
    			User currentUser = rifSession.getUser();
    			Geography currentGeography = rifSession.getCurrentGeography();
    			ArrayList<HealthTheme> healthThemes
    				= service.getHealthThemes(currentUser, currentGeography);
    			HealthTheme defaultHealthTheme = healthThemes.get(0);
    			currentInvestigation.setHealthTheme(defaultHealthTheme);
    		
    			ArrayList<NumeratorDenominatorPair> ndPairs
    				= service.getNumeratorDenominatorPairs(
    					currentUser, 
    					currentGeography, 
    					defaultHealthTheme);
    			NumeratorDenominatorPair defaultNDPair = ndPairs.get(0);
    			currentInvestigation.setNdPair(defaultNDPair);
    		
    			YearRange yearRange
    				= service.getYearRange(currentUser, currentGeography, defaultNDPair);
    			currentInvestigation.setYearRange(yearRange);
    			String noneValue
    				= RIFServiceMessages.getMessage("general.choices.none");
    			currentInvestigation.setInterval(noneValue);   		
    		}
        	//initially when we're initialising all of the fields, we want to 
        	//avoid triggering action events associated with populating the form 
        	//fields.  Remove action listeners
        	healthThemeComboBox.clearActionListeners();
        	numeratorDenominatorComboBox.clearActionListeners();    
 		
    		populateForm(currentInvestigation);	
    		healthThemeComboBox.addActionListener(this);
    		numeratorDenominatorComboBox.addActionListener(this);
    	}
    	catch(RIFServiceException rifServiceException) {
    		ErrorDialog.showError(
    			dialog, 
    			rifServiceException.getErrorMessages());
    	}  	
    }
    
    /**
     * Populate form.
     *
     * @param currentInvestigation the current investigation
     * @throws RIFServiceException the RIF service exception
     */
    private void populateForm(
    	Investigation currentInvestigation) 
    	throws RIFServiceException {
 
    	//set defaults for the current investigation
    	
    	titleTextField.setText(currentInvestigation.getTitle());
    	populateHealthThemeComboBox();    	
    	HealthTheme currentHealthTheme
    		= currentInvestigation.getHealthTheme();
    	healthThemeComboBox.setSelectedItem(currentHealthTheme);
       	populateNumeratorDenominatorComboBox(currentHealthTheme);       	
       	NumeratorDenominatorPair currentNDPair
       		= currentInvestigation.getNdPair();
       	numeratorDenominatorComboBox.setSelectedItem(currentNDPair);
       	denominatorInformationTextArea.setText(currentNDPair.getDenominatorTableDescription());
       	
       	ArrayList<HealthCode> healthCodes
       		= currentInvestigation.getHealthCodes();
       	healthCodeListPanel.clearList();
       	healthCodeListPanel.setData(healthCodes);

		ageSexYearBandPanel.populateForm(currentInvestigation);

    	ArrayList<AbstractCovariate> covariates
    		= currentInvestigation.getCovariates();
    	covariatesListPanel.clearList();
    	covariatesListPanel.setData(covariates);  	
    }
    
    /**
     * Populate health theme combo box.
     *
     * @throws RIFServiceException the RIF service exception
     */
    private void populateHealthThemeComboBox() 
    	throws RIFServiceException {
    	
    	//initialise health theme
		Geography currentGeography
			= rifSession.getCurrentGeography();
		ArrayList<HealthTheme> healthThemes
			= service.getHealthThemes(currentUser, currentGeography);
		healthThemeComboBox.clearList();
		for (int i = 0; i < healthThemes.size(); i++) {
			healthThemeComboBox.addListItem(healthThemes.get(i));
		}	
    }
    
    /**
     * Populate numerator denominator combo box.
     *
     * @param healthTheme the health theme
     * @throws RIFServiceException the RIF service exception
     */
    private void populateNumeratorDenominatorComboBox(
    	HealthTheme healthTheme) 
    	throws RIFServiceException {
    	
		User currentUser = rifSession.getUser();
		Geography currentGeography
			= rifSession.getCurrentGeography();
		ArrayList<NumeratorDenominatorPair> ndPairs
			= service.getNumeratorDenominatorPairs(
				currentUser, 
				currentGeography, 
				healthTheme);
		numeratorDenominatorComboBox.clearList();
		for (int i = 0; i < ndPairs.size(); i++) {
			numeratorDenominatorComboBox.addListItem(ndPairs.get(i));
		}
    }
    
    /**
     * Gets the investigation.
     *
     * @return the investigation
     */
    public Investigation getInvestigation() {
    	
    	return currentInvestigation;
    }
    
    /**
     * Gets the investigation from form data.
     *
     * @return the investigation from form data
     */
    private Investigation getInvestigationFromFormData() {
    	
    	Investigation investigation
    		= Investigation.newInstance();
    	
    	String title 
    		= titleTextField.getText().trim();
    	investigation.setTitle(title);
		HealthTheme selectedHealthTheme
			= (HealthTheme) healthThemeComboBox.getSelectedItem();
		investigation.setHealthTheme(selectedHealthTheme);
		NumeratorDenominatorPair selectedNDPair
			= (NumeratorDenominatorPair) numeratorDenominatorComboBox.getSelectedItem();
		investigation.setNdPair(selectedNDPair);
    	
    	ArrayList<AgeBand> selectedAgeBands
			= ageSexYearBandPanel.getAgeBands();
    	investigation.setAgeBands(selectedAgeBands);
    	YearRange yearRange
			= ageSexYearBandPanel.getYearRange();
    	investigation.setYearRange(yearRange);
    	ArrayList<YearInterval> yearIntervals
			= ageSexYearBandPanel.getYearIntervals();
    	investigation.setYearIntervals(yearIntervals);

		ArrayList<HealthCode> healthCodes
			= healthCodeListPanel.getHealthCodes();
		investigation.setHealthCodes(healthCodes);

		String interval
			= ageSexYearBandPanel.getIntervalValue();	
		String noneChoice
			= RIFServiceMessages.getMessage("general.choices.none");			
		Collator collator = RIFServiceMessages.getCollator();
		if (collator.equals(interval, noneChoice)) {
			investigation.setInterval(null);
		}
		else {
			investigation.setInterval(interval);				
		}
		
		ArrayList<AbstractCovariate> covariates
			= covariatesListPanel.getCovariates();
		investigation.setCovariates(covariates);
		
    	return investigation;
    }
    
	/**
	 * Ok.
	 */
	private void ok() {
		
		try {			
			Investigation investigationFromForm
				= getInvestigationFromFormData();
			investigationFromForm.checkErrors(ValidationPolicy.STRICT);
			
			currentInvestigation.setTitle(investigationFromForm.getTitle());
			currentInvestigation.setHealthTheme(investigationFromForm.getHealthTheme());
			currentInvestigation.setNdPair(investigationFromForm.getNdPair());
			currentInvestigation.setHealthCodes(investigationFromForm.getHealthCodes());
			currentInvestigation.setSex(investigationFromForm.getSex());
			currentInvestigation.setInterval(investigationFromForm.getInterval());
			currentInvestigation.setAgeBands(investigationFromForm.getAgeBands());
			currentInvestigation.setYearRange(investigationFromForm.getYearRange());
			currentInvestigation.setCovariates(investigationFromForm.getCovariates());

			dialog.setVisible(false);
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
		}		
	}
	
	/**
	 * Close.
	 */
	private void close() {
		
		isCancelled = true;
		dialog.setVisible(false);
	}
	
	/**
	 * Update form from health theme change.
	 */
	private void updateFormFromHealthThemeChange() {
		
		try {		
			//Obtain selected health theme
			HealthTheme currentlySelectedHealthTheme
				= (HealthTheme) healthThemeComboBox.getSelectedItem();
		
			//Given the selected health theme, find the default
			//numerator denominator pair
			Geography currentGeography
				= rifSession.getCurrentGeography();
			ArrayList<NumeratorDenominatorPair> ndPairs
				= service.getNumeratorDenominatorPairs(
					currentUser, 
					currentGeography, 
					currentlySelectedHealthTheme);
			
			numeratorDenominatorComboBox.clearList();
			for (NumeratorDenominatorPair ndPair : ndPairs) {
				numeratorDenominatorComboBox.addListItem(ndPair);
			}
			numeratorDenominatorComboBox.setSelectedItem(0);
			
			//reset health code list
			healthCodeListPanel.clearList();

			//reset the age band sex panel
			ageSexYearBandPanel.update(ndPairs.get(0));	

			//update the denominator description
			NumeratorDenominatorPair currentNDPair
				= (NumeratorDenominatorPair) numeratorDenominatorComboBox.getSelectedItem();
			String denominatorDescription
				= currentNDPair.getDenominatorTableDescription();
			denominatorInformationTextArea.setText(denominatorDescription);
					
			//reset the covariates list
			covariatesListPanel.clearList();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(dialog, rifServiceException.getErrorMessages());
		}		
	}
	
	/**
	 * Update form from numerator change.
	 */
	private void updateFormFromNumeratorChange() {		

		//Obtain the currently selected numerator denominator pair
		NumeratorDenominatorPair currentNDPair
			= (NumeratorDenominatorPair) numeratorDenominatorComboBox.getSelectedItem();
		
		//reset health code list
		healthCodeListPanel.clearList();

		//update the age band sex panel
		ageSexYearBandPanel.update(currentNDPair);
		
		//update the denominator description
		String denominatorDescription
			= currentNDPair.getDenominatorTableDescription();
		denominatorInformationTextArea.setText(denominatorDescription);
		
		//reset the covariates list
		covariatesListPanel.clearList();
	}
	
// ==========================================
// Section Errors and Validation
// ==========================================

// ==========================================
// Section Interfaces
// ==========================================

	//ActionListener

	public void actionPerformed(
		ActionEvent event) {
		
		Object source = event.getSource();
		
		if (okCloseButtonPanel.isOKButton(source)) {
			ok();
		}
		else if (okCloseButtonPanel.isCloseButton(source)) {
			close();
		}
		else if (source == healthThemeComboBox.getComboBox()) {
			updateFormFromHealthThemeChange();	
		}
		else if (source == numeratorDenominatorComboBox.getComboBox()) {
			updateFormFromNumeratorChange();
		}
		
	}
	
// ==========================================
// Section Override
// ==========================================

}
