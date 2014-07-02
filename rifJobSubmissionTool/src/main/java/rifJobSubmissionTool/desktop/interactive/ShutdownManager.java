package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;


import rifJobSubmissionTool.system.RIFSession;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;
import rifServices.dataStorageLayer.ProductionRIFStudyServiceBundle;
import rifServices.system.RIFServiceException;

import javax.swing.JDialog;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;


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

public class ShutdownManager extends WindowAdapter {
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	/** The rif session. */
	private RIFSession rifSession;
		
	//GUI Components	
	/** The parent dialog. */
	private JDialog parentDialog;
		
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new shutdown manager.
	 *
	 * @param parentDialog the parent dialog
	 * @param rifSession the rif session
	 */
	public ShutdownManager(
		JDialog parentDialog,
		RIFSession rifSession) {
		
		this.parentDialog = parentDialog;
		parentDialog.addWindowListener(this);
		this.rifSession = rifSession;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Shut down.
	 */
	public void shutDown() {
		
		String dialogTitle
			= RIFJobSubmissionToolMessages.getMessage("shutDown.dialogTitle");
		String message
			= RIFJobSubmissionToolMessages.getMessage("shutDown.warningMessage");
		int result
			= JOptionPane.showConfirmDialog(
				parentDialog, 
				message, 
				dialogTitle,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		
		if (result == JOptionPane.NO_OPTION) {
			return;
		}
		
		User user = rifSession.getUser();
		RIFStudySubmissionAPI service
			= rifSession.getRIFStudySubmissionService();
		try {
			ProductionRIFStudyServiceBundle rifStudyServiceBundle
				= rifSession.getRIFStudyServiceBundle();
			rifStudyServiceBundle.logout(user);
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}
		
		System.exit(0);
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	/* (non-Javadoc)
	 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(
		WindowEvent event) {

		shutDown();
	}

}

