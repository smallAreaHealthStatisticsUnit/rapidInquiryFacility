package rifJobSubmissionTool.desktop.interactive;

import rifGenericUILibrary.ErrorDialog;
import rifGenericUILibrary.ListEditingButtonPanel;
import rifGenericUILibrary.NoDataAvailablePanel;
import rifGenericUILibrary.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFJobSubmissionToolException;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.MapAreaSelectionBasket;
import rifJobSubmissionTool.system.MapAreaSelectionEvent;
import rifJobSubmissionTool.system.RIFSession;
import rifServices.businessConceptLayer.AbstractGeographicalArea;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.MapArea;
import rifServices.businessConceptLayer.MapAreaSummaryData;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;
import rifServices.io.MapAreaListReader;
import rifServices.io.MapAreaListWriter;
import rifServices.io.XMLFileFilter;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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

class MapAreaListSelectionPanel 
	implements ActionListener, 
	Observer {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	/** The rif session. */
	private RIFSession rifSession;
	
	private Geography currentGeography;
	/** The current geographical area. */
	private AbstractGeographicalArea currentGeographicalArea;	
	/** The current map area selection basket. */
	private MapAreaSelectionBasket currentMapAreaSelectionBasket;
		
	//GUI Components
	/** The parent dialog. */
	private JDialog parentDialog;
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;
	/** The panel. */
	private JPanel panel;
	/** The view panel. */
	private JPanel viewPanel;
	/** The no data displayed panel. */
	private NoDataAvailablePanel noDataDisplayedPanel;
	/** The map area selection table view. */
	private MapAreaSelectionTableView mapAreaSelectionTableView;
	/** The selected items status text area. */
	private JTextArea selectedItemsStatusTextArea;	
	/** The map image panel. */
	private MapImagePanel mapImagePanel;	
	/** The add to basket button. */
	
	private ListEditingButtonPanel basketControlButtonPanel;
	
/*	
	private JButton addToBasketButton;
	private JButton deleteFromBasketButton;	
	private JButton editBasketButton;
	private JButton clearBasketButton;
	private JButton importMapAreasButton;
	private JButton exportMapAreasButton;
*/	
		
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new map area list selection panel.
	 *
	 * @param parentDialog the parent dialog
	 * @param rifSession the rif session
	 */
	public MapAreaListSelectionPanel(
		JDialog parentDialog,
		RIFSession rifSession) {
		
		this.parentDialog = parentDialog;
		this.rifSession = rifSession;
		this.userInterfaceFactory = rifSession.getUIFactory();
				
		String noDataDisplayedText
			= RIFJobSubmissionToolMessages.getMessage(
				"mapAreaListSelectionPanel.noDataDisplayed");
		noDataDisplayedPanel
			= new NoDataAvailablePanel(
				userInterfaceFactory,
				noDataDisplayedText,
				true);
		
		mapAreaSelectionTableView 
			= new MapAreaSelectionTableView(parentDialog, rifSession, true);
		//mapAreaSelectionTableView.updateDisplay();
		viewPanel = userInterfaceFactory.createBorderLayoutPanel();
		selectedItemsStatusTextArea
			= userInterfaceFactory.createNonEditableTextArea(4, 30);
		
		mapImagePanel = new MapImagePanel();
		panel = userInterfaceFactory.createPanel();
		
		basketControlButtonPanel = new ListEditingButtonPanel(userInterfaceFactory);
	}

	/**
	 * Builds the ui.
	 */
	private void buildUI() {
		
		panel.removeAll();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		String currentSelectionAreaTitleText
			= RIFJobSubmissionToolMessages.getMessage("mapAreaListSelectionPanel.title");
		JLabel currentSelectionAreaTitleLabel
			= userInterfaceFactory.createLabel(currentSelectionAreaTitleText);
		String currentSelectionAreaTitleToolTipText
			= RIFJobSubmissionToolMessages.getMessage("mapAreaListSelectionPanel.title.toolTip");
		currentSelectionAreaTitleLabel.setToolTipText(currentSelectionAreaTitleToolTipText);
		userInterfaceFactory.setBoldFont(currentSelectionAreaTitleLabel);
		panel.add(currentSelectionAreaTitleLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 0.8;
		panel.add(createMainPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(createMapAreaBasketPanel(), panelGC);

		updateBasketStatusPanel();
	}
	
	/**
	 * Creates the main panel.
	 *
	 * @return the j panel
	 */
	private JPanel createMainPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 0.7;
		panelGC.weighty = 1;
		panel.add(viewPanel, panelGC);
		
		panelGC.gridx++;
		panelGC.weightx = 0.3;		
		panel.add(createMapDisplayPanel(), panelGC);
				
		return panel;
	}
	
	/**
	 * Creates the map display panel.
	 *
	 * @return the j panel
	 */
	private JPanel createMapDisplayPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.NONE;
		
		String mapAreaTitle
			= RIFJobSubmissionToolMessages.getMessage(
				"mapDisplayArea.title");

		JLabel mapAreaStatusLabel 
			= userInterfaceFactory.createLabel(mapAreaTitle);
		userInterfaceFactory.setBoldFont(mapAreaStatusLabel);
		panel.add(mapAreaStatusLabel, panelGC);
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		panel.add(mapImagePanel, panelGC);
		
		return panel;
	}
	
	/**
	 * Creates the map area basket panel.
	 *
	 * @return the j panel
	 */
	private JPanel createMapAreaBasketPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();

		String mapAreaBasketLabelText
			= RIFJobSubmissionToolMessages.getMessage(
				"mapAreaListSelectionPanel.mapAreaBasket.label");
		JLabel mapAreaBasketLabel
			= userInterfaceFactory.createLabel(mapAreaBasketLabelText);
		userInterfaceFactory.setBoldFont(mapAreaBasketLabel);
		panel.add(mapAreaBasketLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1.0;
		panelGC.weighty = 1.0;

		JScrollPane selectedItemsStatusScrollPane
			= userInterfaceFactory.createScrollPane(selectedItemsStatusTextArea);
		panel.add(selectedItemsStatusScrollPane, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(createBasketControlButtonsPanel(), panelGC);
				
		return panel;
	}

	/**
	 * Creates the basket control buttons panel.
	 *
	 * @return the j panel
	 */
	private JPanel createBasketControlButtonsPanel() {
		
		basketControlButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		basketControlButtonPanel.includeAddButton(null);
		basketControlButtonPanel.includeEditButton(null);
		basketControlButtonPanel.includeDeleteButton(null);
		basketControlButtonPanel.includeClearButton(null);
		
		String importMapDataButtonToolTipText
			= RIFJobSubmissionToolMessages.getMessage(
				"mapAreaListSelectionPanel.buttons.importMapData.toolTip");
		basketControlButtonPanel.includeImportButton(importMapDataButtonToolTipText);

		String exportMapDataButtonToolTipText
			= RIFJobSubmissionToolMessages.getMessage(
				"mapAreaListSelectionPanel.button.exportMapData.toolTip");
		basketControlButtonPanel.includeExportButton(exportMapDataButtonToolTipText);

		basketControlButtonPanel.rightJustifyButtons();
		basketControlButtonPanel.addActionListener(this);
		
		return basketControlButtonPanel.getPanel();
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
	 * Adds the to basket.
	 */
	private void addToBasket() {

		ArrayList<MapArea> selectedMapAreas
			= mapAreaSelectionTableView.getSelectedMapAreas();
		currentMapAreaSelectionBasket.addMapAreas(selectedMapAreas);
	}

	/**
	 * Delete from basket.
	 */
	private void deleteFromBasket() {

		ArrayList<MapArea> selectedMapAreas
			= mapAreaSelectionTableView.getSelectedMapAreas();
		currentMapAreaSelectionBasket.deleteMapAreas(selectedMapAreas);		
	}

	/**
	 * Edits the basket.
	 */
	private void editBasket() {

		MapSelectionBasketEditorDialog dialog
			= new MapSelectionBasketEditorDialog(rifSession);
		dialog.initialise(
			currentGeographicalArea, 
			currentMapAreaSelectionBasket);
		dialog.show();
	}
	
	/**
	 * Clear basket.
	 */
	private void clearBasket() {

		//remove all items from the basket and remove
		//any items in the map area list which may still appear highlighted
		mapAreaSelectionTableView.clearHighlightedAreas();
		currentMapAreaSelectionBasket.clearMapAreas();
	}
	
	/**
	 * Update basket status panel.
	 */
	private void updateBasketStatusPanel() {
		
		try {			
			RIFStudySubmissionAPI service
				= rifSession.getRIFStudySubmissionService();
			User currentUser = rifSession.getUser();
			Geography currentGeography
				= rifSession.getCurrentGeography();
			GeoLevelSelect currentGeoLevelSelect
				= currentGeographicalArea.getGeoLevelSelect();
			GeoLevelArea currentGeoLevelArea
				= currentGeographicalArea.getGeoLevelArea();
			GeoLevelToMap currentGeoLevelToMap
				= currentGeographicalArea.getGeoLevelToMap();
			
			//Sensitise the control buttons appropriately to reflect
			//whether basket is empty or not
			if (currentMapAreaSelectionBasket.getSize() == 0) {
				String basketIsEmptyMessage
					= RIFJobSubmissionToolMessages.getMessage("mapAreaListSelectionPanel.basketIsEmpty");
				selectedItemsStatusTextArea.setText(basketIsEmptyMessage);

				//basket is empty
				basketControlButtonPanel.indicateEmptyState();
			}
			else {
				//current geo level select should only be null
				//when the application is first starting.  By this point,
				//GeoLevelSelect will not have been selected
				MapAreaSummaryData mapAreaSummaryData
					= service.getMapAreaSummaryInformation(
						currentUser, 
						currentGeography, 
						currentGeoLevelSelect, 
						currentGeoLevelArea, 
						currentGeoLevelToMap,
						currentMapAreaSelectionBasket.getMapAreas());
					
				//Display information about the areas currently in the basket
				StringBuilder selectedStatusMessage = new StringBuilder();
				String selectedAreasMessage
					= RIFJobSubmissionToolMessages.getMessage(
						"mapAreaListSelectionPanel.basketAreasMessage",
						String.valueOf(mapAreaSummaryData.getTotalViewAreas()),
						String.valueOf(mapAreaSummaryData.getTotalNumberToMapAreas()),
						currentGeoLevelToMap.getDisplayName());
				selectedStatusMessage.append(selectedAreasMessage);
				String totalAreaTotalPopulationMessage
					= RIFJobSubmissionToolMessages.getMessage(
						"mapAreaListSelectionPanel.totalAreaTotalPopulationMessage",
						String.valueOf(mapAreaSummaryData.getTotalPopulation()),
						String.valueOf(mapAreaSummaryData.getTotalArea()));
				selectedStatusMessage.append(totalAreaTotalPopulationMessage);
				selectedItemsStatusTextArea.setText(selectedStatusMessage.toString());				

				basketControlButtonPanel.indicatePopulatedState();
			}
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}
	}
	
	/**
	 * Import map areas.
	 */
	private void importMapAreas() {

		JFileChooser fileChooser
			= userInterfaceFactory.createFileChooser();
		XMLFileFilter xmlFileFilter
			= new XMLFileFilter();
		fileChooser.setFileFilter(xmlFileFilter);
		int result 
			= fileChooser.showOpenDialog(parentDialog);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;			
		}
		
		File selectedFile = fileChooser.getSelectedFile();
		
		MapAreaListReader mapAreaListReader
			= new MapAreaListReader();
		try {
			mapAreaListReader.readFile(selectedFile);
			ArrayList<MapArea> mapAreasToAdd = mapAreaListReader.getMapAreas();
			currentMapAreaSelectionBasket.addMapAreas(mapAreasToAdd);
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}
	}
	
	/**
	 * Export map areas.
	 */
	private void exportMapAreas() {

		JFileChooser fileChooser
			= userInterfaceFactory.createFileChooser();
		XMLFileFilter xmlFileFilter
			= new XMLFileFilter();
		fileChooser.setFileFilter(xmlFileFilter);
		int result 
			= fileChooser.showSaveDialog(parentDialog);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;			
		}

		File selectedFile 
			= XMLFileFilter.ensureFileEndsWithXML(fileChooser.getSelectedFile());   
		ArrayList<MapArea> mapAreasToWrite
			= currentMapAreaSelectionBasket.getMapAreas();
		MapAreaListWriter mapAreaListWriter = new MapAreaListWriter();
		
		try {
			mapAreaListWriter.writeFile(selectedFile, mapAreasToWrite);			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}		
	}
	
	/**
	 * Is the appearance of the GUI if the geo level controls had not yet
	 * been selected.  In this case, the parts of the map area list panel
	 * would behave as follows:
	 * <ul>
	 * <li>
	 * the panel that would normally show the table of map areas would
	 * instead show "Select an area"
	 * </li>
	 * <li>
	 * The panel showing the map would indicate "No map area established"
	 * <li>
	 * <li>
	 * The basket would appear cleared
	 * </li>
	 *
	 * @param currentGeographicalArea the current geographical area
	 * @param currentMapAreaSelectionBasket the current map area selection basket
	 */
	public void initialiseResetSettings(
		AbstractGeographicalArea currentGeographicalArea,
		MapAreaSelectionBasket currentMapAreaSelectionBasket) {

		this.currentGeographicalArea = currentGeographicalArea;
		this.currentMapAreaSelectionBasket = currentMapAreaSelectionBasket;
				
		viewPanel.removeAll();
		viewPanel.add(noDataDisplayedPanel.getPanel(), BorderLayout.CENTER);
		viewPanel.updateUI();
		
		//no image can be established yet for the map image panel. 
		//therefore it will display a "No map area established"
		mapImagePanel.setData(null);
		
		//an update should cause the basket to appear cleared.
		String basketIsEmptyMessage
			= RIFJobSubmissionToolMessages.getMessage("mapAreaListSelectionPanel.basketIsEmpty");
		selectedItemsStatusTextArea.setText(basketIsEmptyMessage);

		updateBasketStatusPanel();
		buildUI();
	}
	
	
	/**
	 * Initialise apply settings.
	 *
	 * @param currentGeographicalArea the current geographical area
	 * @param currentMapAreaSelectionBasket the current map area selection basket
	 */
	public void initialiseApplySettings(
		Geography geography,
		AbstractGeographicalArea currentGeographicalArea,
		MapAreaSelectionBasket currentMapAreaSelectionBasket) {
		
		this.currentGeography = geography;
		this.currentGeographicalArea = currentGeographicalArea;
		this.currentMapAreaSelectionBasket = currentMapAreaSelectionBasket;
		mapAreaSelectionTableView.initialiseForm(
			currentGeographicalArea,
			currentMapAreaSelectionBasket);		
		mapAreaSelectionTableView.showFirstResultPage();
		
		viewPanel.removeAll();
		viewPanel.add(mapAreaSelectionTableView.getPanel(), BorderLayout.CENTER);
		viewPanel.updateUI();

		//update the map display area
		GeoLevelSelect geoLevelSelect = currentGeographicalArea.getGeoLevelSelect();
		GeoLevelView geoLevelView = currentGeographicalArea.getGeoLevelView();
		GeoLevelArea geoLevelArea = currentGeographicalArea.getGeoLevelArea();
		
		//remove any observers for the current basket.  We want to clear
		//it without triggering updates elsewhere in the UI
		currentMapAreaSelectionBasket.deleteObservers();
		currentMapAreaSelectionBasket.addObserver(this);

		ArrayList<MapArea> mapAreas = currentMapAreaSelectionBasket.getMapAreas();
		try {
			RIFStudySubmissionAPI service 
				= rifSession.getRIFStudySubmissionService();
			User currentUser = rifSession.getUser();
			BufferedImage bufferedImage
				= service.getImage(
					currentUser, 
					geography,
					geoLevelSelect, 
					geoLevelArea, 
					geoLevelView, 
					mapAreas);
			mapImagePanel.setData(bufferedImage);
			mapImagePanel.repaint();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}

		//update the status panel
		
		updateBasketStatusPanel();
		
		//add the basket observers back in
		buildUI();
	}
	
	/**
	 * Commit changes.
	 *
	 * @throws RIFJobSubmissionToolException the RIF job submission tool exception
	 */
	public void commitChanges() 
		throws RIFJobSubmissionToolException {

		currentGeographicalArea.setMapAreas(currentMapAreaSelectionBasket.getMapAreas());
	}
	
	/**
	 * Validate form.
	 *
	 * @throws RIFJobSubmissionToolException the RIF job submission tool exception
	 */
	public void validateForm() 
		throws RIFJobSubmissionToolException {

		ArrayList<MapArea> selectedMapAreas
			= currentMapAreaSelectionBasket.getMapAreas();
		ArrayList<MapArea> duplicateMapAreas
			= MapArea.identifyDuplicatesWithinList(selectedMapAreas);
		if (duplicateMapAreas.size() > 0) {
			StringBuilder duplicateList = new StringBuilder();
			for (int i = 0; i < duplicateMapAreas.size(); i++) {
				if (i != 0) {
					duplicateList.append(",");
				}
				duplicateList.append(duplicateMapAreas.get(i));
			}
			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"mapArea.error.duplicateMapAreasFound",
					duplicateList.toString());
			RIFJobSubmissionToolException rifJobSubmissionToolException
				= new RIFJobSubmissionToolException(errorMessage);
			throw rifJobSubmissionToolException;
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

		Object button = event.getSource();

		if (basketControlButtonPanel.isAddButton(button)) {
			addToBasket();
		}
		else if (basketControlButtonPanel.isEditButton(button)) {
		}
		else if (basketControlButtonPanel.isDeleteButton(button)) {
			deleteFromBasket();
		}
		else if (basketControlButtonPanel.isClearButton(button)) {
			clearBasket();
		}
		else if (basketControlButtonPanel.isImportButton(button)) {
			importMapAreas();
		}
		else if (basketControlButtonPanel.isExportButton(button)) {
			exportMapAreas();
		}
	}

	//Interface: Observer
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(
		Observable observable, 
		Object argument) {

		//Update the table display to reflect the contents of the basket.
		//If the table displays a map area found in the basket,
		//ensure that the text for the row is highlighted in bold blue
		//otherwise, make it appear in plain font
		MapAreaSelectionEvent mapSelectionEvent
			= (MapAreaSelectionEvent) argument;		
		MapAreaSelectionEvent.OperationType operationType
			= mapSelectionEvent.getOperationType();
		ArrayList<MapArea> mapAreas = mapSelectionEvent.getAffectedAreas();
				
		if (operationType == MapAreaSelectionEvent.OperationType.ITEMS_ADDED) {
			mapAreaSelectionTableView.highlightMapAreas(mapAreas);
		}
		else if (operationType == MapAreaSelectionEvent.OperationType.ITEMS_DELETED) {
			mapAreaSelectionTableView.unhighlightMapAreas(mapAreas);			
		}
		else if (operationType == MapAreaSelectionEvent.OperationType.BASKET_EMPTIED) {
			mapAreaSelectionTableView.clearHighlightedAreas();
		}

		//Update the image of the map panel
		RIFStudySubmissionAPI service 
			= rifSession.getRIFStudySubmissionService();
		User currentUser = rifSession.getUser();
		GeoLevelSelect geoLevelSelect = currentGeographicalArea.getGeoLevelSelect();
		GeoLevelView geoLevelView = currentGeographicalArea.getGeoLevelView();
		GeoLevelArea geoLevelArea = currentGeographicalArea.getGeoLevelArea();
	
		try {
			BufferedImage updatedImage
				= service.getImage(
					currentUser, 
					currentGeography,
					geoLevelSelect, 
					geoLevelArea, 
					geoLevelView, 
					mapAreas);
		
			mapImagePanel.setData(updatedImage);			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}
		
		//update the statistics shown in the basket status display
		updateBasketStatusPanel();
	}
			
	// ==========================================
	// Section Override
	// ==========================================

}

