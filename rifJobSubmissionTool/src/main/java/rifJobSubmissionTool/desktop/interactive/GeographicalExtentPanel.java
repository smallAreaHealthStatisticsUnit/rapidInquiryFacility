package rifJobSubmissionTool.desktop.interactive;


import rifJobSubmissionTool.system.MapAreaSelectionBasket;

import rifJobSubmissionTool.system.RIFJobSubmissionToolException;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFSession;
import rifJobSubmissionTool.util.UserInterfaceFactory;




import rifServices.businessConceptLayer.AbstractGeographicalArea;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.MapAreaSummaryData;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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

class GeographicalExtentPanel 
	extends Observable 
	implements ActionListener {
	
	// ==========================================
	// Section Constants
	// ==========================================
	/**
	 * The Enum ObservableEvent.
	 */
	public enum ObservableEvent {
		/** The apply new settings. */
		APPLY_NEW_SETTINGS, 
		/** The reset. */
		RESET};
	
	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	/** The rif session. */
	private RIFSession rifSession;
	/** The map area selection basket. */
	private MapAreaSelectionBasket mapAreaSelectionBasket;	
	/** The current geographical area. */
	private AbstractGeographicalArea currentGeographicalArea;

	//GUI Components
	/** The parent dialog. */
	private JDialog parentDialog;	
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory; 	
	/** The panel. */
	private JPanel panel;
	/** The geo level select combo box. */
	private OrderedListComboBox geoLevelSelectComboBox;
	/** The geo level area combo box. */
	private OrderedListComboBox geoLevelAreaComboBox;
	/** The geo level view combo box. */
	private OrderedListComboBox geoLevelViewComboBox;
	/** The geo level to map combo box. */
	private OrderedListComboBox geoLevelToMapComboBox;
	/** The preview map areas text area. */
	private JTextArea previewMapAreasTextArea;
	/** The apply extent settings. */
	private JButton applyExtentSettings;
	/** The reset extent settings. */
	private JButton resetExtentSettings;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new geographical extent panel.
	 *
	 * @param parentDialog the parent dialog
	 * @param rifSession the rif session
	 */
	public GeographicalExtentPanel(
		JDialog parentDialog,
		RIFSession rifSession) {

		this.parentDialog = parentDialog;
		this.rifSession = rifSession;
		userInterfaceFactory = rifSession.getUIFactory();
		UserInterfaceFactory userInterfaceFactory
			= rifSession.getUIFactory();
		
		geoLevelSelectComboBox 
			= new OrderedListComboBox(userInterfaceFactory);
		geoLevelAreaComboBox
			= new OrderedListComboBox(userInterfaceFactory);
		geoLevelViewComboBox
			= new OrderedListComboBox(userInterfaceFactory);
		geoLevelToMapComboBox
			= new OrderedListComboBox(userInterfaceFactory);
		previewMapAreasTextArea
			= userInterfaceFactory.createNonEditableTextArea(4, 30);
		panel = userInterfaceFactory.createPanel();
		panel.setBorder(LineBorder.createGrayLineBorder());

		//initially populate the geolevelselect box.  The selected geo level select
		//will in turn set defaults in all the other values

		//now add action listeners.  From now on, any change in the 
		//selection of geo level select will cause the other drop down boxes
		//to change as well.
		geoLevelSelectComboBox.addActionListener(this);
		geoLevelAreaComboBox.addActionListener(this);
		geoLevelViewComboBox.addActionListener(this);
		geoLevelToMapComboBox.addActionListener(this);

		String applyExtentSettingsButtonText
			= RIFJobSubmissionToolMessages.getMessage("geographicalExtentPanel.button.applyExtentSettings.label");		
		applyExtentSettings
			= userInterfaceFactory.createButton(applyExtentSettingsButtonText);
		String applyExtentSettingsButtonToolTipText
			= RIFJobSubmissionToolMessages.getMessage("geographicalExtentPanel.button.applyExtentSettings.toolTip");		
		applyExtentSettings.setToolTipText(applyExtentSettingsButtonToolTipText);
		applyExtentSettings.addActionListener(this);

		String resetExtentSettingsButtonText
			= RIFJobSubmissionToolMessages.getMessage("geographicalExtentPanel.button.resetExtentSettings.label");		
		resetExtentSettings
			= userInterfaceFactory.createButton(resetExtentSettingsButtonText);
		String resetExtentSettingsButtonToolTipText
			= RIFJobSubmissionToolMessages.getMessage("geographicalExtentPanel.button.resetExtentSettings.toolTip");		
		resetExtentSettings.setToolTipText(resetExtentSettingsButtonToolTipText);
		resetExtentSettings.addActionListener(this);
		resetExtentSettings.setEnabled(false);	
	}

	/**
	 * Builds the ui.
	 */
	private void buildUI() {
		
		panel.removeAll();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.insets = new Insets(5,5,5,5);

		String geographicalExtentLabelText
			= RIFJobSubmissionToolMessages.getMessage("geographicalExtentPanel.title");
		JLabel geographicalExtentLabel
			= userInterfaceFactory.createLabel(geographicalExtentLabelText);
		String geographicalExtentLabelToolTipText
			= RIFJobSubmissionToolMessages.getMessage("geographicalExtentPanel.title.toolTip");
		geographicalExtentLabel.setToolTipText(geographicalExtentLabelToolTipText);
		panel.add(geographicalExtentLabel, panelGC);		

		panelGC.gridx = 0;
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		panel.add(createGeographicalExtentControlsPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1.0;
		panelGC.weighty = 1.0;		
		JScrollPane selectedMapAreasScrollPane
			= userInterfaceFactory.createScrollPane(previewMapAreasTextArea);
		panel.add(selectedMapAreasScrollPane, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(createButtonPanel(), panelGC);
		
	}
		
	/**
	 * Creates the geographical extent controls panel.
	 *
	 * @return the j panel
	 */
	private JPanel createGeographicalExtentControlsPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.ipadx = 5;
		//geo level select
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String geoLevelSelectLabelText
			= RIFServiceMessages.getMessage("geoLevelSelect.label");
		JLabel geoLevelSelectLabel
			= userInterfaceFactory.createLabel(geoLevelSelectLabelText);
		String geoLevelSelectToolTipText
			= RIFServiceMessages.getMessage("geoLevelSelect.toolTip");
		geoLevelSelectLabel.setToolTipText(geoLevelSelectToolTipText);
		panel.add(geoLevelSelectLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.33;
		panel.add(geoLevelSelectComboBox.getComboBox(), panelGC);

		//geo level area
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String geoLevelAreasLabelText
			= RIFServiceMessages.getMessage("geoLevelArea.label");
		JLabel geoLevelAreasLabel
			= userInterfaceFactory.createLabel(geoLevelAreasLabelText);
		String geoLevelAreasLabelToolTipText
			= RIFServiceMessages.getMessage("geoLevelArea.toolTip");
		geoLevelAreasLabel.setToolTipText(geoLevelAreasLabelToolTipText);
		panel.add(geoLevelAreasLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.33;
		panel.add(geoLevelAreaComboBox.getComboBox(), panelGC);
		
		//geo level view
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String geoLevelViewsLabelText
			= RIFServiceMessages.getMessage("geoLevelView.label");
		JLabel geoLevelViewsLabel
			= userInterfaceFactory.createLabel(geoLevelViewsLabelText);
		String geoLevelViewsLabelToolTipText
			= RIFServiceMessages.getMessage("geoLevelView.toolTip");
		geoLevelViewsLabel.setToolTipText(geoLevelViewsLabelToolTipText);
		panel.add(geoLevelViewsLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.33;
		panel.add(geoLevelViewComboBox.getComboBox(), panelGC);
		
		//geo level to map
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String geoLevelToMapLabelText
			= RIFServiceMessages.getMessage("geoLevelToMap.label");
		JLabel geoLevelToMapLabel
			= userInterfaceFactory.createLabel(geoLevelToMapLabelText);
		String geoLevelToMapToolTipText
			= RIFServiceMessages.getMessage("geoLevelToMap.toolTip");
		geoLevelToMapLabel.setToolTipText(geoLevelToMapToolTipText);
		panel.add(geoLevelToMapLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.33;
		panel.add(geoLevelToMapComboBox.getComboBox(), panelGC);
		
		return panel;
	}

	/**
	 * Creates the button panel.
	 *
	 * @return the j panel
	 */
	private JPanel createButtonPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panelGC.ipadx = 5;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(applyExtentSettings, panelGC);

		panelGC.gridx++;
		panel.add(resetExtentSettings, panelGC);
		//initially, reset will be desensitised because the panel will already be
		//initialised
		
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
	 * Commit changes.
	 *
	 * @throws RIFJobSubmissionToolException the RIF job submission tool exception
	 */
	public void commitChanges() 
		throws RIFJobSubmissionToolException {
		
		GeoLevelSelect geoLevelSelect
			= (GeoLevelSelect) geoLevelSelectComboBox.getSelectedItem();
		currentGeographicalArea.setGeoLevelSelect(geoLevelSelect);
		GeoLevelArea geoLevelArea
			= (GeoLevelArea) geoLevelAreaComboBox.getSelectedItem();
		currentGeographicalArea.setGeoLevelArea(geoLevelArea);
		GeoLevelView geoLevelView
			= (GeoLevelView) geoLevelViewComboBox.getSelectedItem();
		currentGeographicalArea.setGeoLevelView(geoLevelView);
		GeoLevelToMap geoLevelToMap
			= (GeoLevelToMap) geoLevelToMapComboBox.getSelectedItem();
		currentGeographicalArea.setGeoLevelToMap(geoLevelToMap);
		currentGeographicalArea.setNewRecord(false);
	}
	
	/**
	 * Update combination field values.
	 */
	private void updateCombinationFieldValues() {
		
		//Set the new current GeoLevelSelect 		
		try {
			removeActionListeners();
			updateGeoLevelAreaViewAndToMapValues();
			addActionListeners();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}	
	}
	
	/**
	 * This is setting the display as if the apply button had been pressed
	 * This method is used when we are trying to go "Back" in the work flow
	 * of a RIF study submission, and we come across either the 
	 * SpecifyStudyAreaStepPanel or the SpecifyComparisonAreaStepPanel
	 * work flow steps.  In these cases, we have to make the form look as though
	 * we have already pressed the apply button
	 *
	 * @param currentGeographicalArea the current geographical area
	 * @param mapAreaSelectionBasket the map area selection basket
	 */
	public void initialiseApplySettings(
		AbstractGeographicalArea currentGeographicalArea,
		MapAreaSelectionBasket mapAreaSelectionBasket) {
		
		this.currentGeographicalArea = currentGeographicalArea;
		this.mapAreaSelectionBasket = mapAreaSelectionBasket;
		
		//remove all action listeners so that events are not triggered
		//when all combo box values are set
		geoLevelSelectComboBox.clearActionListeners();
		geoLevelAreaComboBox.clearActionListeners();
		geoLevelViewComboBox.clearActionListeners();
		geoLevelToMapComboBox.clearActionListeners();
		
		//set the values based on those in the current geographic
		//area object (ie either a DiseaseMappingStudyArea or ComparisonArea
		GeoLevelSelect currentGeoLevelSelect
			= currentGeographicalArea.getGeoLevelSelect();
		geoLevelSelectComboBox.setSelectedItem(currentGeoLevelSelect);
		GeoLevelArea currentGeoLevelArea
			= currentGeographicalArea.getGeoLevelArea();
		geoLevelAreaComboBox.setSelectedItem(currentGeoLevelArea);
		GeoLevelView currentGeoLevelView
			= currentGeographicalArea.getGeoLevelView();
		geoLevelViewComboBox.setSelectedItem(currentGeoLevelView);
		GeoLevelToMap currentGeoLevelToMap
			= currentGeographicalArea.getGeoLevelToMap();
		geoLevelToMapComboBox.setSelectedItem(currentGeoLevelToMap);
		
		//now disable most of the display, to make it look as though
		//we just finished pressing "Apply"
		useApplyGUISettings();
		updateAvailableAreas();
		buildUI();

	}
	
	/**
	 * Initialise reset settings.
	 *
	 * @param currentGeographicalArea the current geographical area
	 * @param mapAreaSelectionBasket the map area selection basket
	 */
	public void initialiseResetSettings(
		AbstractGeographicalArea currentGeographicalArea,
		MapAreaSelectionBasket mapAreaSelectionBasket) {
		
		this.currentGeographicalArea = currentGeographicalArea;
		this.mapAreaSelectionBasket = mapAreaSelectionBasket;
				
		//ensure there are no action listeners so that we
		//can set the combo box values without triggering UI events
		removeActionListeners();
		
		//use the default settings to populate the UI
		try {
			geoLevelSelectComboBox.clearActionListeners();			
			initialiseGeoLevelSelectValues();
			updateGeoLevelAreaViewAndToMapValues();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);			
		}
				
		//Enable most of the display, as if we have just pressed the
		//reset button
		useResetGUISettings();	
		updateAvailableAreas();
	
		//add the action listeners again so that changes made
		//in one button will appropriately update the others
		addActionListeners();
		buildUI();
	}
	
	/**
	 * This method used by the buttons owned by the geographical extent panel.
	 */
	private void applyExtentSettings() {
		
		useApplyGUISettings();
		currentGeographicalArea.clearMapAreas();
		mapAreaSelectionBasket.clearMapAreas();
				
		GeoLevelSelect selectedGeoLevelSelect
			= (GeoLevelSelect) geoLevelSelectComboBox.getSelectedItem();
		currentGeographicalArea.setGeoLevelSelect(selectedGeoLevelSelect);
		
		//commit current values to the underlying part of the business object
		//that holds data about the geographical area (eg: Disease Mapping Study Area,
		//Comparison Area
		GeoLevelArea selectedGeoLevelArea
			= (GeoLevelArea) geoLevelAreaComboBox.getSelectedItem();		
		currentGeographicalArea.setGeoLevelArea(selectedGeoLevelArea);
		GeoLevelView selectedGeoLevelView
			= (GeoLevelView) geoLevelViewComboBox.getSelectedItem();
		currentGeographicalArea.setGeoLevelView(selectedGeoLevelView);
		GeoLevelToMap selectedGeoLevelToMap
			= (GeoLevelToMap) geoLevelToMapComboBox.getSelectedItem();
		currentGeographicalArea.setGeoLevelToMap(selectedGeoLevelToMap);
				
		//notify observers that a change has been made
		setChanged();
		notifyObservers(ObservableEvent.APPLY_NEW_SETTINGS);
	}
	
	/**
	 * This method used by the buttons owned by the geographical extent panel.
	 */
	private void resetExtentSettings() {
		
		useResetGUISettings();

		try {
			removeActionListeners();			
			initialiseGeoLevelSelectValues();
			updateGeoLevelAreaViewAndToMapValues();
			addActionListeners();			
			setChanged();
			notifyObservers(ObservableEvent.RESET);
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);			
		}
	}

	/**
	 * Adds the action listeners.
	 */
	private void addActionListeners() {
		
		geoLevelSelectComboBox.addActionListener(this);
		geoLevelAreaComboBox.addActionListener(this);
		geoLevelViewComboBox.addActionListener(this);
		geoLevelToMapComboBox.addActionListener(this);		
	}
	
	/**
	 * Removes the action listeners.
	 */
	private void removeActionListeners() {
		
		geoLevelSelectComboBox.clearActionListeners();
		geoLevelAreaComboBox.clearActionListeners();
		geoLevelViewComboBox.clearActionListeners();
		geoLevelToMapComboBox.clearActionListeners();		
	}
	
	/**
	 * Use apply gui settings.
	 */
	private void useApplyGUISettings() {
		
		applyExtentSettings.setEnabled(false);
		resetExtentSettings.setEnabled(true);		
		geoLevelSelectComboBox.setEnabled(false);
		geoLevelAreaComboBox.setEnabled(false);
		geoLevelViewComboBox.setEnabled(false);
		geoLevelToMapComboBox.setEnabled(false);		
	}
	
	/**
	 * Use reset gui settings.
	 */
	private void useResetGUISettings() {
		
		applyExtentSettings.setEnabled(true);
		resetExtentSettings.setEnabled(false);		
		geoLevelSelectComboBox.setEnabled(true);
		geoLevelAreaComboBox.setEnabled(true);
		geoLevelViewComboBox.setEnabled(true);
		geoLevelToMapComboBox.setEnabled(true);		
	}
	
	/**
	 * Initialise geo level select values.
	 */
	private void initialiseGeoLevelSelectValues() {
		
		try {
			User currentUser
				= rifSession.getUser();
			Geography currentGeography
				= rifSession.getCurrentGeography();
			RIFStudySubmissionAPI service
				= rifSession.getRIFStudySubmissionService();
			ArrayList<GeoLevelSelect> geoLevelSelects
				= service.getGeographicalLevelSelectValues(currentUser, currentGeography);
			for (GeoLevelSelect geoLevelSelect : geoLevelSelects) {
				geoLevelSelectComboBox.addListItem(geoLevelSelect);
			}
			
			GeoLevelSelect defaultGeoLevelSelect
				= service.getDefaultGeoLevelSelectValue(currentUser, currentGeography);
			geoLevelSelectComboBox.setSelectedItem(defaultGeoLevelSelect);			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}		
	}
	
	/**
	 * This method uses the value of geo level select to determine the default
	 * setting for geo level area, geo level view and geo level to map.
	 *
	 * @throws RIFServiceException the RIF service exception
	 */
	private void updateGeoLevelAreaViewAndToMapValues() 
		throws RIFServiceException {

		RIFStudySubmissionAPI service = rifSession.getRIFStudySubmissionService();
		User currentUser = rifSession.getUser();
		Geography currentGeography
			= rifSession.getCurrentGeography();
		
		//Take the first choice	
		GeoLevelSelect geoLevelSelect
			= (GeoLevelSelect) geoLevelSelectComboBox.getSelectedItem();
		geoLevelAreaComboBox.clearActionListeners();
		geoLevelAreaComboBox.clearList();
		ArrayList<GeoLevelArea> geoLevelAreas
			= service.getGeoLevelAreaValues(
				currentUser, 
				currentGeography, 
				geoLevelSelect);
		for (GeoLevelArea geoLevelArea : geoLevelAreas) {
			geoLevelAreaComboBox.addListItem(geoLevelArea);				
		}

		geoLevelAreaComboBox.selectFirstItem();
		
		//Take the first choice
		geoLevelViewComboBox.clearActionListeners();
		geoLevelViewComboBox.clearList();
		ArrayList<GeoLevelView> geoLevelViews
			= service.getGeoLevelViewValues(
				currentUser, 
				currentGeography, 
				geoLevelSelect);
		for (GeoLevelView geoLevelView : geoLevelViews) {
			geoLevelViewComboBox.addListItem(geoLevelView);
		}
		geoLevelViewComboBox.selectFirstItem();
				
		//Take the first choice
		geoLevelToMapComboBox.clearActionListeners();
		geoLevelToMapComboBox.clearList();
		ArrayList<GeoLevelToMap> geoLevelToMaps
			= service.getGeoLevelToMapValues(
				currentUser, 
				currentGeography, 
				geoLevelSelect);
		for (GeoLevelToMap geoLevelToMap : geoLevelToMaps) {
			geoLevelToMapComboBox.addListItem(geoLevelToMap);	
		}
		
		geoLevelToMapComboBox.selectFirstItem();		
	}

	
	/**
	 * Update available areas.
	 */
	private void updateAvailableAreas() {
		
		User currentUser = rifSession.getUser();
		Geography currentGeography
			= rifSession.getCurrentGeography();
		GeoLevelSelect selectedGeoLevelSelect
			= (GeoLevelSelect) geoLevelSelectComboBox.getSelectedItem();
		GeoLevelArea selectedGeoLevelArea
			= (GeoLevelArea) geoLevelAreaComboBox.getSelectedItem();
		GeoLevelToMap selectedGeoLevelToMap
			= (GeoLevelToMap) geoLevelToMapComboBox.getSelectedItem();

		try {
			RIFStudySubmissionAPI service 
				= rifSession.getRIFStudySubmissionService();
			
			MapAreaSummaryData mapAreaSummaryData
				= service.getSummaryDataForCurrentExtent(
					currentUser, 
					currentGeography, 
					selectedGeoLevelSelect, 
					selectedGeoLevelArea, 
					selectedGeoLevelToMap);
			
			String previewStatusMessage = "";
			int totalNumberOfAreas = mapAreaSummaryData.getTotalViewAreas();
			if (totalNumberOfAreas == 0) {
				previewStatusMessage
					= RIFJobSubmissionToolMessages.getMessage(
						"geographicalExtentPanel.previewStatus.noItems");
			}
			else if(totalNumberOfAreas == 1) {
				previewStatusMessage
					= RIFJobSubmissionToolMessages.getMessage(
						"geographicalExtentPanel.previewStatus.oneItem");				
			}
			else {
				previewStatusMessage
					= RIFJobSubmissionToolMessages.getMessage(
						"geographicalExtentPanel.previewStatus.manyItems",
						String.valueOf(totalNumberOfAreas));				
			}
			
			previewMapAreasTextArea.setText(previewStatusMessage);		
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}
		
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
		
		Object source = event.getSource();
		
		if (geoLevelSelectComboBox.isComboBox(source)) {
			updateCombinationFieldValues();
			updateAvailableAreas();
		}
		else if (source == applyExtentSettings) {
			applyExtentSettings();
		}
		else if (source == resetExtentSettings) {
			resetExtentSettings();
		}
		else {

			//we can assume one of the combo boxes was adjusted
			updateAvailableAreas();
		}
			
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}

