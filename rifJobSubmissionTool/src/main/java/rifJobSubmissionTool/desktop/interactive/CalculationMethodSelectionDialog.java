package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;

import rifJobSubmissionTool.system.RIFSession;
import rifJobSubmissionTool.util.UserInterfaceFactory;

import rifServices.businessConceptLayer.CalculationMethod;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceException;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JPanel;
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

public class CalculationMethodSelectionDialog 
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
	/** The service. */
	private RIFStudySubmissionAPI service;
	/** The is cancelled. */
	private boolean isCancelled;
	
		
	//GUI Components
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;		
	/** The dialog. */
	private JDialog dialog;		
    /** The available calculation methods. */
	   private ArrayList<CalculationMethod> availableCalculationMethods;		
	/** The calculation method table. */
	private CalculationMethodTable calculationMethodTable;				
	/** The ok close panel. */
	private OKClosePanel okClosePanel;
		
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new calculation method selection dialog.
     *
     * @param rifSession the rif session
     * @param currentlyUsedCalculationMethods the currently used calculation methods
     */
	public CalculationMethodSelectionDialog(
    	RIFSession rifSession,
    	ArrayList<CalculationMethod> currentlyUsedCalculationMethods) {
    
    	this.rifSession = rifSession;   	
    	this.service = rifSession.getRIFStudySubmissionService();
    	this.userInterfaceFactory = rifSession.getUIFactory();

		isCancelled = false;
    	determineAvailableCalculationMethods(currentlyUsedCalculationMethods);
    	
		calculationMethodTable = new CalculationMethodTable(userInterfaceFactory);
		calculationMethodTable.useSingleItemSelection();
    			
		String dialogTitle
			= RIFJobSubmissionToolMessages.getMessage("calculationMethodSelectionDialog.title");
		dialog
			= userInterfaceFactory.createDialog(dialogTitle);
				
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();				
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1.0;
		panelGC.weighty = 1.0;
		
		if (availableCalculationMethods.size() == 0) {
			String noDataAvailableMessage
				= RIFJobSubmissionToolMessages.getMessage("calculationMethodSelectionDialog.noOptionsAvailable");
			NoDataAvailablePanel noDataAvailablePanel
				= new NoDataAvailablePanel(
					userInterfaceFactory, 
					noDataAvailableMessage,
					true);
			panel.add(noDataAvailablePanel.getPanel(), panelGC);
		}
		else {
			calculationMethodTable.clearSelection();
			calculationMethodTable.addCalculationMethods(availableCalculationMethods);
			JScrollPane scrollPane
				= userInterfaceFactory.createScrollPane(calculationMethodTable);
			panel.add(scrollPane, panelGC);
		}
				
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		okClosePanel = new OKClosePanel(userInterfaceFactory);
		okClosePanel.addActionListener(this);
		panel.add(okClosePanel.getPanel(), panelGC);
				
		dialog.getContentPane().add(panel);
		dialog.setModal(true);
		dialog.setSize(400, 400);
		
    }
    
    /**
     * Determine available calculation methods.
     *
     * @param currentlyUsedCalculationMethods the currently used calculation methods
     */
    private void determineAvailableCalculationMethods(
    	ArrayList<CalculationMethod> currentlyUsedCalculationMethods) {

    	try {

    		ArrayList<String> methodNamesInUse = new ArrayList<String>();
        	for (CalculationMethod currentlyUsedCalculationMethod : currentlyUsedCalculationMethods) {
        		String currentMethodName
        			= currentlyUsedCalculationMethod.getName();
        		if (methodNamesInUse.contains(currentMethodName) == false) {
        			methodNamesInUse.add(currentMethodName);
        		}
        	}
 		
    		User currentUser = rifSession.getUser();
    		ArrayList<CalculationMethod> allCalculationMethods
    			= service.getAvailableCalculationMethods(currentUser);
    		
    		availableCalculationMethods = new ArrayList<CalculationMethod>();
    		for (CalculationMethod calculationMethod : allCalculationMethods) {
    			String methodName = calculationMethod.getName();
    			if (methodNamesInUse.contains(methodName) == false) {
    				availableCalculationMethods.add(calculationMethod);    				
    			}
    		}    		    		
    	}
    	catch(RIFServiceException rifServiceException) {
    		ErrorDialog.showError(dialog, rifServiceException);
    	}
    }
    
// ==========================================
// Section Accessors and Mutators
// ==========================================
	
	/**
	 * Gets the selected calculation method.
	 *
	 * @return the selected calculation method
	 */
    public CalculationMethod getSelectedCalculationMethod() {
    	
		return calculationMethodTable.getSelectedCalculationMethod();
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
	 * Show.
	 */
	public void show() {
		
		dialog.setVisible(true);
	}
	
	/**
	 * Ok.
	 */
	private void ok() {
		
		isCancelled = false;
		dialog.setVisible(false);
	}
	
	/**
	 * Close.
	 */
	private void close() {
		
		isCancelled = true;
		dialog.setVisible(false);
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
		
		if (okClosePanel.isOKButton(button)) {
			ok();
		}
		else if (okClosePanel.isCloseButton(button)) {
			close();
		}
	}
	
// ==========================================
// Section Override
// ==========================================

}
