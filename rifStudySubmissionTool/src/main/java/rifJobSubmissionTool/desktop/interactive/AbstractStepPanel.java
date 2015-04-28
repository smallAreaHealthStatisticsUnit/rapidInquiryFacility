package rifJobSubmissionTool.desktop.interactive;

import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;
import rifServices.system.RIFServiceException;

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

abstract class AbstractStepPanel {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	/** The rif session. */
	private RIFStudySubmissionToolSession rifSession;

	//GUI Components
	/** The parent dialog. */
	private JDialog parentDialog;
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;
	/** The title label. */
	private JLabel titleLabel;
	/** The panel. */
	private JPanel panel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new abstract step panel.
	 *
	 * @param parentDialog the parent dialog
	 * @param rifSession the rif session
	 */
	public AbstractStepPanel(
		JDialog parentDialog,
		RIFStudySubmissionToolSession rifSession) {
		
		this.parentDialog = parentDialog;
		this.rifSession = rifSession;
		this.userInterfaceFactory = rifSession.getUIFactory();
		
		titleLabel = userInterfaceFactory.createLabel("");
		userInterfaceFactory.setBoldFont(titleLabel);
		
		panel = userInterfaceFactory.createPanel();
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * Gets the RIF session.
	 *
	 * @return the RIF session
	 */
	protected RIFStudySubmissionToolSession getRIFSession() {
		
		return rifSession;
	}
	
	/**
	 * Gets the user interface factory.
	 *
	 * @return the user interface factory
	 */
	protected UserInterfaceFactory getUserInterfaceFactory() {
		
		return userInterfaceFactory;
	}
	
	/**
	 * Gets the parent dialog.
	 *
	 * @return the parent dialog
	 */
	protected JDialog getParentDialog() {
		
		return parentDialog;
	}
	
	/**
	 * Sets the panel title information.
	 *
	 * @param stepNumber the step number
	 * @param panelTitle the panel title
	 */
	public void setPanelTitleInformation(
		int stepNumber,
		String panelTitle) {
		
		String panelTitleText
			= RIFJobSubmissionToolMessages.getMessage(
				"abstractStepPanel.title",
				String.valueOf(stepNumber),
				panelTitle);
		
		String htmlFormattedText
			= userInterfaceFactory.createHTMLLabelText(2, panelTitleText);
		titleLabel.setText(htmlFormattedText);
	}

	/**
	 * Creates the instruction panel.
	 *
	 * @param instructionsText the instructions text
	 * @return the j panel
	 */
	public JPanel createInstructionPanel(
		String instructionsText) {
		
		JPanel panel 
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		return panel;		
	}

	/**
	 * Gets the title label.
	 *
	 * @return the title label
	 */
	public JLabel getTitleLabel() {
		
		return titleLabel;
	}
	
	/**
	 * Gets the panel.
	 *
	 * @return the panel
	 */
	public JPanel getPanel() {
		
		return panel;
	}
		
	/**
	 * Initialise form.
	 *
	 * @throws RIFJobSubmissionToolException the RIF job submission tool exception
	 */
	public abstract void initialiseForm() 
		throws RIFServiceException;
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	/**
	 * Commit changes.
	 *
	 * @throws RIFJobSubmissionToolException the RIF job submission tool exception
	 */
	public abstract void commitChanges() 
		throws RIFServiceException;

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}

