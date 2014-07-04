package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.system.RIFActivityStep;
import rifJobSubmissionTool.system.RIFJobSubmissionToolException;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFSession;
import rifJobSubmissionTool.util.UserInterfaceFactory;
import rifServices.businessConceptLayer.RIFJobSubmission;
import rifServices.businessConceptLayer.User;
import rifServices.dataStorageLayer.ProductionRIFStudyServiceBundle;
import rifServices.system.RIFServiceException;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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

public class RIFInteractiveStudySubmissionTool 
	implements Observer, 
	ActionListener {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {	
			ProductionRIFStudyServiceBundle rifStudyServiceBundle
				= new ProductionRIFStudyServiceBundle();
			rifStudyServiceBundle.initialise();
			rifStudyServiceBundle.login("kgarwood", new String("a").toCharArray());				

			String ipAddress = InetAddress.getLocalHost().getHostAddress();
			
			User testUser = User.newInstance("kgarwood", ipAddress);
			RIFSession rifSession 
				= new RIFSession(rifStudyServiceBundle, testUser);
						
			RIFInteractiveStudySubmissionTool rifStudySubmissionTool
				= new RIFInteractiveStudySubmissionTool(rifSession);
			rifStudySubmissionTool.show();
			rifStudyServiceBundle.logout(testUser);
			System.exit(0);
		}
		catch(UnknownHostException unknownHostException) {
			ErrorDialog.showError(null, unknownHostException.toString());			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(null, rifServiceException);
		}
	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data	
	/** The rif session. */
	private RIFSession rifSession;	
	/** The current rif job submission. */
	private RIFJobSubmission currentRIFJobSubmission;
	/** The rif activity state machine. */
	private RIFActivityStateMachine rifActivityStateMachine;
	
	//GUI Components
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;	
	/** The main view panel. */
	private JPanel mainViewPanel;
	/** The exit button. */
	private JMenuItem exitButton;	
	/** The work flow progress panel. */
	private WorkFlowProgressPanel workFlowProgressPanel;	
	/** The current activity panel. */
	private AbstractStepPanel currentActivityPanel;	
	/** The create or copy study step panel. */
	private CreateOrCopyStudyStepPanel createOrCopyStudyStepPanel;	
	/** The describe study step panel. */
	private DescribeStudyStepPanel describeStudyStepPanel;	
	/** The specify study area step panel. */
	private SpecifyStudyAreaStepPanel specifyStudyAreaStepPanel;	
	/** The specify comparison area step panel. */
	private SpecifyComparisonAreaStepPanel specifyComparisonAreaStepPanel;	
	/** The specify investigations step panel. */
	private SpecifyInvestigationsStepPanel specifyInvestigationsStepPanel;	
	/** The specify reporting options step panel. */
	private SpecifyReportingOptionsStepPanel specifyReportingOptionsStepPanel;
	/** The preview step panel. */
	private PreviewStepPanel previewStepPanel;
	/** The step button navigation panel. */
	private RIFStepButtonNavigationPanel stepButtonNavigationPanel;
	/** The dialog. */
	private JDialog dialog;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new RIF study submission tool.
	 *
	 * @param rifSession the rif session
	 */
	public RIFInteractiveStudySubmissionTool(
		RIFSession rifSession) {

		this.rifSession = rifSession;
		userInterfaceFactory = rifSession.getUIFactory();

		rifActivityStateMachine = new RIFActivityStateMachine();
		rifActivityStateMachine.addObserver(this);

		currentRIFJobSubmission = RIFJobSubmission.newInstance();
		currentRIFJobSubmission.setUser(rifSession.getUser());
		rifSession.setCurrentRIFSubmission(currentRIFJobSubmission);
		
		String titleText
			= RIFJobSubmissionToolMessages.getMessage("rifJobSubmissionTool.title");
		dialog = userInterfaceFactory.createDialog(titleText);
				
		workFlowProgressPanel 
			= new WorkFlowProgressPanel(userInterfaceFactory);
		
		//create the sequence of editing panels
		createOrCopyStudyStepPanel
			= new CreateOrCopyStudyStepPanel(dialog, rifSession);
		describeStudyStepPanel 
			= new DescribeStudyStepPanel(dialog, rifSession);
		specifyStudyAreaStepPanel
			= new SpecifyStudyAreaStepPanel(dialog, rifSession);
		specifyComparisonAreaStepPanel
			= new SpecifyComparisonAreaStepPanel(dialog, rifSession);
		specifyReportingOptionsStepPanel
			= new SpecifyReportingOptionsStepPanel(dialog, rifSession);
		specifyInvestigationsStepPanel
			= new SpecifyInvestigationsStepPanel(dialog, rifSession);
		previewStepPanel
			= new PreviewStepPanel(dialog, rifSession);
		
		buildUI();
		stepButtonNavigationPanel.setCurrentActivityStep(
			RIFActivityStep.CREATE_OR_COPY_STUDY_STEP, 
			createOrCopyStudyStepPanel);
		rifActivityStateMachine.firstActivityStep();
	}
	
	/**
	 * Builds the ui.
	 */
	private void buildUI() {
	
		ShutdownManager shutDownManager
			= new ShutdownManager(dialog, rifSession);
		dialog.addWindowListener(shutDownManager);

		createMenuItems();

		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
				
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(workFlowProgressPanel.getPanel(), panelGC);

		panelGC.gridy++;
		mainViewPanel
			= userInterfaceFactory.createBorderLayoutPanel();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		panel.add(mainViewPanel, panelGC);
	
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
			
		stepButtonNavigationPanel
			= new RIFStepButtonNavigationPanel(
				rifSession, 
				dialog, 
				rifActivityStateMachine);
	
		panel.add(stepButtonNavigationPanel.getPanel(), panelGC);
		
		dialog.getContentPane().add(panel);
		dialog.setSize(940, 700);
		dialog.setModal(true);		
	}

	/**
	 * Creates the menu items.
	 */
	private void createMenuItems() {
		
		//create menu
		JMenuBar menuBar = userInterfaceFactory.createMenuBar();
		
		String fileMenuText
			= RIFJobSubmissionToolMessages.getMessage("fileMenu.label");
		JMenu fileMenu = userInterfaceFactory.createMenu(fileMenuText);
		
		String exitButtonText
			= RIFJobSubmissionToolMessages.getMessage("fileMenu.exitButton.label");
		exitButton
			= userInterfaceFactory.createJMenuItem(exitButtonText);
		exitButton.addActionListener(this);
		fileMenu.add(exitButton);
		menuBar.add(fileMenu);
		
		dialog.setJMenuBar(menuBar);
		
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
	 * Update current step panel.
	 */
	public void updateCurrentStepPanel() {		
		
		mainViewPanel.removeAll();
		try {
			currentActivityPanel.initialiseForm();			
		}
		catch(RIFJobSubmissionToolException rifJobSubmissionToolException) {
			ErrorDialog.showError(dialog, rifJobSubmissionToolException);
		}
		mainViewPanel.add(currentActivityPanel.getPanel(), BorderLayout.CENTER);
		mainViewPanel.updateUI();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================
	
	//Interface: ActionListener
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(
		ActionEvent event) {
		
		Object source = event.getSource();
		if (source == exitButton) {
			ShutdownManager shutDownManager
				= new ShutdownManager(dialog, rifSession);
			shutDownManager.shutDown();
		}
	}
	
	//Interface: Observer
	//updating in reaction to a change in the state machine
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(
		Observable observable, 
		Object argument) {
		
		RIFActivityStep currentRIFActivityStep
			= (RIFActivityStep) argument;

		if (currentRIFActivityStep == RIFActivityStep.CREATE_OR_COPY_STUDY_STEP) {
			currentActivityPanel = createOrCopyStudyStepPanel;
		}
		else if (currentRIFActivityStep == RIFActivityStep.DESCRIBE_STUDY) {
			currentActivityPanel = describeStudyStepPanel;
		}
		else if (currentRIFActivityStep == RIFActivityStep.CHOOSE_STUDY_AREA) {
			currentActivityPanel = specifyStudyAreaStepPanel;
		}
		else if (currentRIFActivityStep == RIFActivityStep.CHOOSE_COMPARISON_AREA) {
			currentActivityPanel = specifyComparisonAreaStepPanel;
		}
		else if (currentRIFActivityStep == RIFActivityStep.SPECIFY_INVESTIGATIONS) {
			currentActivityPanel = specifyInvestigationsStepPanel;
		}
		else if (currentRIFActivityStep == RIFActivityStep.SPECIFY_REPORTS) {
			currentActivityPanel = specifyReportingOptionsStepPanel;
		}
		else if (currentRIFActivityStep == RIFActivityStep.PREVIEW) {
			currentActivityPanel = previewStepPanel;
		}
		else if (currentRIFActivityStep == RIFActivityStep.SUBMIT_STUDY) {
			currentActivityPanel = createOrCopyStudyStepPanel;
		}

		//update the three main parts of the display which have to respond
		//to the new state: 
		//   (1) the work flow indicator at the top
		//   (2) the main form in the middle
		//   (3) the set of navigation buttons at the bottom
		
		workFlowProgressPanel.setCurrentActivityStep(currentRIFActivityStep);
		updateCurrentStepPanel();	
		stepButtonNavigationPanel.setCurrentActivityStep(
			currentRIFActivityStep, 
			currentActivityPanel);		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}

