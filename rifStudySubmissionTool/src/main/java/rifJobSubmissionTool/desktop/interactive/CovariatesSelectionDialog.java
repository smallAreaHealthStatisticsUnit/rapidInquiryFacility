package rifJobSubmissionTool.desktop.interactive;




import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;
import rifServices.fileFormats.CovariateContentHandler;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;



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

final class CovariatesSelectionDialog 
	implements ActionListener, 
	ListSelectionListener {
		
// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================

	//Data
	/** The rif session. */
	private RIFStudySubmissionToolSession rifSession;	
	/** The service. */
	private RIFStudySubmissionAPI service;
	/** The is cancelled. */
	private boolean isCancelled;
	
	//GUI Components
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;
	/** The dialog. */
	private JDialog dialog;
	/** The covariates list panel. */
	private OrderedListPanel covariatesListPanel;
	/** The covariate report pane. */
	private JEditorPane covariateReportPane;	
	/** The ok close panel. */
	private OKCloseButtonPanel okCloseButtonPanel;			
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new covariates selection dialog.
     *
     * @param rifSession the rif session
     */
	public CovariatesSelectionDialog(
		RIFStudySubmissionToolSession rifSession) {

		this.rifSession = rifSession;
    	service = rifSession.getRIFStudySubmissionService();
		this.userInterfaceFactory = rifSession.getUIFactory();
		isCancelled = false;

		buildUI();
		initialiseCovariateListing();
		covariatesListPanel.addListSelectionListener(this);
    }
    
    /**
     * Builds the ui.
     */
    private void buildUI() {
    	
		String dialogTitle
			= RIFJobSubmissionToolMessages.getMessage("covariatesSelectionDialog.title");		
		dialog
			= userInterfaceFactory.createDialog(dialogTitle);
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;		
		panelGC.weightx = 1.0;
		panel.add(createInstructionsPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;		
		panelGC.weighty = 0.4;		
		String availableCovariatesLabelText
			= RIFJobSubmissionToolMessages.getMessage("covariatesSelectionDialog.availableCovariates.label");
		String availableCovariatesLabelToolTipText
			= RIFJobSubmissionToolMessages.getMessage("covariatesSelectionDialog.availableCovariates.toolTip");		
		covariatesListPanel 
			= new OrderedListPanel(
				availableCovariatesLabelText,
				availableCovariatesLabelToolTipText,
				userInterfaceFactory,
				true);
		panel.add(covariatesListPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 0.6;		
		panel.add(createDescriptionPanel(), panelGC);
				
		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;				
		panelGC.weighty = 0;				
		okCloseButtonPanel = new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		panel.add(okCloseButtonPanel.getPanel(), panelGC);
		
		dialog.getContentPane().add(panel);
		dialog.setSize(500, 450);
		dialog.setModal(true);		
    }

    /**
     * Creates the instructions panel.
     *
     * @return the j panel
     */
    private JPanel createInstructionsPanel() {
    	
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		   	
		Geography currentGeography
			= rifSession.getCurrentGeography();
		
    	GeoLevelToMap currentGeoLevelToMap
    		= rifSession.getStudyGeoLevelToMap();

       	String instructions
       		= RIFJobSubmissionToolMessages.getMessage(
        		"covariatesSelectionDialog.instructions",
        		currentGeography.getDisplayName(),
        		currentGeoLevelToMap.getDisplayName());
    	JTextArea immutableTextArea 
    		= userInterfaceFactory.createNonEditableTextArea(2, 30);	
    	immutableTextArea.append(instructions);
    	panel.add(immutableTextArea, panelGC);
    	
    	return panel;
    }
    
	/**
	 * Creates the description panel.
	 *
	 * @return the j panel
	 */
	private JPanel createDescriptionPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		String descriptionLabelText
			= RIFServiceMessages.getMessage("covariate.description.label");
		JLabel descriptionLabel		
			= userInterfaceFactory.createLabel(descriptionLabelText);
		String descriptionLabelToolTipText
			= RIFServiceMessages.getMessage("covariate.description.toolTip");
		descriptionLabel.setToolTipText(descriptionLabelToolTipText);
		panel.add(descriptionLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1.0;
		panelGC.weighty = 1.0;
		covariateReportPane
			= userInterfaceFactory.createHTMLEditorPane();
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(covariateReportPane);
		panel.add(scrollPane, panelGC);
		return panel;
	}
	
	/**
	 * Initialise covariate listing.
	 */
	private void initialiseCovariateListing() {
		
		try {
			covariatesListPanel.clearList();			
			User currentUser = rifSession.getUser();
			Geography currentGeography
				= rifSession.getCurrentGeography();
			GeoLevelToMap currentGeoLevelToMap
				= rifSession.getStudyGeoLevelToMap();
			ArrayList<AbstractCovariate> covariates
				= service.getCovariates(
					currentUser, 
					currentGeography, 
					currentGeoLevelToMap);	
			for (AbstractCovariate covariate : covariates) {
				covariatesListPanel.addListItem(covariate);
			}
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(dialog, rifServiceException.getErrorMessages());
		}
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
	 * Gets the selected covariates.
	 *
	 * @return the selected covariates
	 */
	public ArrayList<AbstractCovariate> getSelectedCovariates() {
		
		ArrayList<AbstractCovariate> covariates 
			= new ArrayList<AbstractCovariate>();
		ArrayList<DisplayableListItemInterface> selectedListItems
			= covariatesListPanel.getSelectedItems();
		for (DisplayableListItemInterface currentListItem : selectedListItems) {
			covariates.add((AbstractCovariate) currentListItem);
		}
		
		return covariates;
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
	 * Ok.
	 */
	private void ok() {
		dialog.setVisible(false);
		isCancelled = false;
	}
	
	/**
	 * Close.
	 */
	private void close() {
		dialog.setVisible(false);
		isCancelled = true;
	}
	
// ==========================================
// Section Errors and Validation
// ==========================================

// ==========================================
// Section Interfaces
// ==========================================
	//Interface: ActionListener


	public void actionPerformed(
		ActionEvent event) {
		
		Object button = event.getSource();
		
		if (okCloseButtonPanel.isOKButton(button)) {
			ok();
		}
		else if (okCloseButtonPanel.isCloseButton(button)) {
			close();
		}
	}
	
	//Interface: ListSelectionListener
	public void valueChanged(
		ListSelectionEvent event) {

		AbstractCovariate selectedCovariate
			= (AbstractCovariate) covariatesListPanel.getSelectedItem();
		
		try {	
			CovariateContentHandler covariateContentHandler
				= new CovariateContentHandler();		
			ByteArrayOutputStream byteArrayOutputStream
				= new ByteArrayOutputStream();
			covariateContentHandler.initialise(byteArrayOutputStream);
			covariateContentHandler.writeHTML(selectedCovariate);
			String htmlReport 
				= new String(byteArrayOutputStream.toByteArray(), "UTF-8");
			covariateReportPane.setText(htmlReport);
		}
		catch(UnsupportedEncodingException exception) {
			//@TODO
			exception.printStackTrace(System.out);
		}
	}
	
	
// ==========================================
// Section Override
// ==========================================

}
