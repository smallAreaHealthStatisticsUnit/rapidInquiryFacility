package rifJobSubmissionTool.desktop.interactive;

import rifGenericUILibrary.ErrorDialog;
import rifGenericUILibrary.UserInterfaceFactory;
import rifGenericUILibrary.ListNavigationButtonPanel;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;


import rifJobSubmissionTool.system.MapAreaSelectionBasket;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifServices.businessConceptLayer.AbstractGeographicalArea;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.MapArea;
import rifServices.businessConceptLayer.MapAreaSummaryData;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceException;

import java.util.ArrayList;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.*;

import java.util.Observable;


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

class MapAreaSelectionTableView 
	extends Observable 
	implements ActionListener {


	// ==========================================
	// Section Constants
	// ==========================================
	/** The Constant RESULTS_PER_PAGE. */
	private static final int RESULTS_PER_PAGE = 50;
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	
	//Data
	/** The current geographical area. */
	private AbstractGeographicalArea currentGeographicalArea;
	/** The current map area selection basket. */
	private MapAreaSelectionBasket currentMapAreaSelectionBasket;	
	/** The current result batch start index. */
	private int currentResultBatchStartIndex;	
	/** The current result batch end index. */
	private int currentResultBatchEndIndex;	
	/** The total results available. */
	private int totalResultsAvailable;	
	/** The current page. */
	private int currentPage;	
	/** The maximum page. */
	private int maximumPage;	
	/** The rif session. */
	private RIFStudySubmissionToolSession rifSession;
	/** The service. */
	private RIFStudySubmissionAPI service;
	/** The is data source service. */
	private boolean isDataSourceService;
	
	//GUI Components	
	/** The parent dialog. */
	private JDialog parentDialog;
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;
	/** The map area selection table. */
	private MapAreaSelectionTable mapAreaSelectionTable;
	/** The panel. */
	private JPanel panel;
	/** The status label. */
	private JLabel statusLabel;
		
	private ListNavigationButtonPanel listNavigationButtonPanel;
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new map area selection table view.
	 *
	 * @param parentDialog the parent dialog
	 * @param rifSession the rif session
	 * @param isDataSourceService the is data source service
	 */
	public MapAreaSelectionTableView(
		JDialog parentDialog,
		RIFStudySubmissionToolSession rifSession,
		boolean isDataSourceService) {
		
		this.parentDialog = parentDialog;
		this.rifSession = rifSession;
		this.service = rifSession.getRIFStudySubmissionService();
		this.userInterfaceFactory = rifSession.getUIFactory();
		this.isDataSourceService = isDataSourceService;
		mapAreaSelectionTable = new MapAreaSelectionTable(userInterfaceFactory);
		
		listNavigationButtonPanel 
			= new ListNavigationButtonPanel(userInterfaceFactory);
	}

	/**
	 * Builds the ui.
	 */
	private void buildUI() {
		
		panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
	
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;		
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(mapAreaSelectionTable.getTable());
		panel.add(scrollPane, panelGC);
		
		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panel.add(createControlPanel(), panelGC);
	}
		
	/**
	 * Creates the control panel.
	 *
	 * @return the j panel
	 */
	private JPanel createControlPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.anchor = GridBagConstraints.SOUTHWEST;

		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;		
		statusLabel = userInterfaceFactory.createLabel("");
		panel.add(statusLabel, panelGC);
				
		panelGC.gridx++;
		panel.add(listNavigationButtonPanel.getPanel(), panelGC);
			
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
	 * Show first result page.
	 */
	public void showFirstResultPage() {
		
		first();
	}

	
	/**
	 * Highlight map areas.
	 *
	 * @param mapAreas the map areas
	 */
	public void highlightMapAreas(
		ArrayList<MapArea> mapAreas) {

		mapAreaSelectionTable.highlightMapAreas(mapAreas);
	}
	
	/**
	 * Unhighlight map areas.
	 *
	 * @param mapAreas the map areas
	 */
	public void unhighlightMapAreas(
		ArrayList<MapArea> mapAreas) {

		mapAreaSelectionTable.unhighlightMapAreas(mapAreas);		
	}
	
	/**
	 * Clear highlighted areas.
	 */
	public void clearHighlightedAreas() {
		
		mapAreaSelectionTable.clearHighlightedAreas();		
	}
	
	/**
	 * Compute result page limits.
	 */
	private void computeResultPageLimits() {
		
		try {		
			MapAreaSummaryData mapAreaSummaryData
				= getMapAreaSummaryData();
			
			totalResultsAvailable = mapAreaSummaryData.getTotalViewAreas();
			maximumPage = totalResultsAvailable / RESULTS_PER_PAGE;
			if (totalResultsAvailable % RESULTS_PER_PAGE > 0) {
				maximumPage = maximumPage + 1;
			}
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}		
	}
	
	/**
	 * Update display.
	 */
	public void updateDisplay() {
		
		//Update the status message
		String statusMessage = "";
		if (totalResultsAvailable == 0) {
			statusMessage
				= RIFJobSubmissionToolMessages.getMessage(
					"mapAreaSelectionTableView.status.noItems");			
		}
		else if (totalResultsAvailable == 1) {
			statusMessage
			= RIFJobSubmissionToolMessages.getMessage(
				"mapAreaSelectionTableView.status.oneItem",
				String.valueOf(currentPage),
				String.valueOf(maximumPage));			
		}
		else {
			statusMessage
				= RIFJobSubmissionToolMessages.getMessage(
					"mapAreaSelectionTableView.status.manyItems",
					String.valueOf(totalResultsAvailable),
					String.valueOf(currentPage),
					String.valueOf(maximumPage));
		}

		statusLabel.setText(statusMessage);		

		//Update states of buttons
		if (maximumPage == 1) {
			listNavigationButtonPanel.showOnlyItemState();
		}
		else if (currentPage == 1) {
			listNavigationButtonPanel.showFirstItemState();
		}
		else if (currentPage == maximumPage) {
			listNavigationButtonPanel.showLastItemState();
		}
		else {
			listNavigationButtonPanel.showMiddleItemState();
		}
	}
	
	/**
	 * Sets the map areas.
	 *
	 * @param mapAreas the new map areas
	 */
	public void setMapAreas(
		ArrayList<MapArea> mapAreas) {
		
		mapAreaSelectionTable.setMapAreas(mapAreas);
	}
	
	/**
	 * Gets the selected map areas.
	 *
	 * @return the selected map areas
	 */
	public ArrayList<MapArea> getSelectedMapAreas() {
		
		return mapAreaSelectionTable.getSelectedMapAreas();
	}
	
	/**
	 * Delete selected items.
	 */
	public void deleteSelectedItems() {		
		
		if (isDataSourceService == false) {
			//Data are coming from the basket.  Therefore,
			//also delete from the basket
			ArrayList<MapArea> selectedRows
				= mapAreaSelectionTable.getSelectedMapAreas();
			currentMapAreaSelectionBasket.deleteMapAreas(selectedRows);
		}
		mapAreaSelectionTable.deleteSelectedMapAreas();
		computeResultPageLimits();
		updateDisplay();
	}
	
	/**
	 * First.
	 */
	private void first() {
		
		//update bounds of data batch
		currentPage = 1;
		currentResultBatchStartIndex = 0;
		currentResultBatchEndIndex 
			= currentResultBatchStartIndex + RESULTS_PER_PAGE;
		updateModelWithNewBatch();
		updateDisplay();
	}
	
	/**
	 * Previous.
	 */
	private void previous() {
		
		currentPage = currentPage - 1;
		updateModelWithNewBatch();
		updateDisplay();
	}
	
	/**
	 * Next.
	 */
	private void next() {
		
		currentPage = currentPage + 1;
		updateModelWithNewBatch();		
		updateDisplay();
	}
	
	/**
	 * Last.
	 */
	private void last() {
		
		currentPage = maximumPage;
		updateModelWithNewBatch();
		updateDisplay();
	}
	
	/**
	 * Update model with new batch.
	 */
	private void updateModelWithNewBatch() {
		
		try {
			mapAreaSelectionTable.clearHighlightedAreas();
			currentResultBatchStartIndex 
				= (currentPage - 1) * RESULTS_PER_PAGE;
			currentResultBatchEndIndex 
				= currentResultBatchStartIndex + RESULTS_PER_PAGE;
		
			ArrayList<MapArea> currentMapAreas = new ArrayList<MapArea>();
			if (isDataSourceService) {
				//the source of data will be map areas returned by the service
				currentMapAreas.addAll(getMapAreasFromService());
			}
			else {
				currentMapAreas.addAll(getMapAreasFromBasket());
			}
					
			mapAreaSelectionTable.setMapAreas(currentMapAreas);
			
			//check if there are any map areas currently being displayed
			//which are in the basket

			if (isDataSourceService) {
				ArrayList<MapArea> mapAreasToHighlight = new ArrayList<MapArea>();
				for (MapArea currentMapArea : currentMapAreas) {
					if (currentMapAreaSelectionBasket.containsMapArea(currentMapArea)) {
						mapAreasToHighlight.add(currentMapArea);
					}					
				}
				mapAreaSelectionTable.highlightMapAreas(mapAreasToHighlight);
			}
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}
	}

	/**
	 * Gets the map area summary data.
	 *
	 * @return the map area summary data
	 * @throws RIFServiceException the RIF service exception
	 */
	private MapAreaSummaryData getMapAreaSummaryData() 
		throws RIFServiceException {
		
		if (isDataSourceService) {
			RIFStudySubmissionAPI service
				= rifSession.getRIFStudySubmissionService();
			User currentUser = rifSession.getUser();
			Geography currentGeography
				= rifSession.getCurrentGeography();
			GeoLevelSelect currentGeoLevelSelect
				= currentGeographicalArea.getGeoLevelSelect();
			GeoLevelToMap currentGeoLevelToMap
				= currentGeographicalArea.getGeoLevelToMap();
			GeoLevelArea currentGeoLevelArea
				= currentGeographicalArea.getGeoLevelArea();

			MapAreaSummaryData result
				= service.getSummaryDataForCurrentExtent(
					currentUser,
					currentGeography,
					currentGeoLevelSelect,
					currentGeoLevelArea,
					currentGeoLevelToMap);
			return result;
		}
		else {			
			//obtain total area count from the basket
			MapAreaSummaryData mapAreaSummaryData
				= MapAreaSummaryData.newInstance();
			mapAreaSummaryData.setTotalViewAreas(currentMapAreaSelectionBasket.getSize());
			return mapAreaSummaryData;			
		}
	}
	
	/**
	 * Gets the map areas from service.
	 *
	 * @return the map areas from service
	 * @throws RIFServiceException the RIF service exception
	 */
	private ArrayList<MapArea> getMapAreasFromService() 
		throws RIFServiceException {

		User currentUser = rifSession.getUser();
		Geography currentGeography
			= rifSession.getCurrentGeography();
		GeoLevelSelect currentGeoLevelSelect
			= currentGeographicalArea.getGeoLevelSelect();
		GeoLevelToMap currentGeoLevelToMap
			= currentGeographicalArea.getGeoLevelToMap();
		GeoLevelArea currentGeoLevelArea
			= currentGeographicalArea.getGeoLevelArea();

		ArrayList<MapArea> currentResultBatch
			= service.getMapAreasByBlock(
				currentUser, 
				currentGeography, 
				currentGeoLevelSelect, 
				currentGeoLevelArea, 
				currentGeoLevelToMap,
				currentResultBatchStartIndex,
				currentResultBatchEndIndex);
		
		return currentResultBatch;
	}

	/**
	 * Gets the map areas from basket.
	 *
	 * @return the map areas from basket
	 */
	private ArrayList<MapArea> getMapAreasFromBasket() {
		
		ArrayList<MapArea> results
			= currentMapAreaSelectionBasket.getMapAreas(
				currentResultBatchStartIndex, 
				currentResultBatchEndIndex);
		return results;
	}
	
	/**
	 * Initialise form.
	 *
	 * @param currentGeographicalArea the current geographical area
	 * @param currentMapAreaSelectionBasket the current map area selection basket
	 */
	public void initialiseForm(
		AbstractGeographicalArea currentGeographicalArea,
		MapAreaSelectionBasket currentMapAreaSelectionBasket) {
		
		this.currentGeographicalArea = currentGeographicalArea;
		this.currentMapAreaSelectionBasket = currentMapAreaSelectionBasket;
		buildUI();
		first();
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
		
		if (listNavigationButtonPanel.isFirstButton(button)) {
			first();
		}
		else if (listNavigationButtonPanel.isPreviousButton(button)) {
			previous();
		}
		else if (listNavigationButtonPanel.isNextButton(button)) {
			next();
		}
		else if (listNavigationButtonPanel.isLastButton(button)) {
			last();
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}

