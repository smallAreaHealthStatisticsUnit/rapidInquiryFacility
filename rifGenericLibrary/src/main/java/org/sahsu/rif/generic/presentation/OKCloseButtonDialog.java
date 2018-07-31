package org.sahsu.rif.generic.presentation;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.sahsu.rif.generic.system.RIFServiceException;

/**
 *
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
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

public abstract class OKCloseButtonDialog 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private UserInterfaceFactory userInterfaceFactory;
	private String dialogTitle;
	private String dialogInstructionsText;
	
	private boolean isCancelled;
	
	private JDialog dialog;
	private JPanel mainPanel;
	
	private OKCloseButtonPanel okCloseButtonPanel;
	
	private int mainPanelFillSetting;
	private int mainPanelWeightX;
	private int mainPanelWeightY;
	
	private boolean doSystemExitOnClose;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public OKCloseButtonDialog(
		final UserInterfaceFactory userInterfaceFactory) {
		
		this.userInterfaceFactory = userInterfaceFactory;
		dialog = userInterfaceFactory.createDialog();

		mainPanelFillSetting = GridBagConstraints.BOTH;
		mainPanelWeightX = 1;
		mainPanelWeightY = 1;

		isCancelled = false;
		
		doSystemExitOnClose = false;
	}
	
	protected UserInterfaceFactory getUserInterfaceFactory() {
		return userInterfaceFactory;
	}
	
	protected void setDialogTitle(final String dialogTitle) {
		this.dialogTitle = dialogTitle;
	}
	
	protected void setInstructionText(final String dialogInstructionsText) {
		this.dialogInstructionsText = dialogInstructionsText;
	}

	protected void setMainPanelFillConstraints(
		final int mainPanelFillSetting,
		final int mainPanelWeightX,
		final int mainPanelWeightY) {
		
		
		this.mainPanelFillSetting = GridBagConstraints.BOTH;
		this.mainPanelWeightX = mainPanelWeightX;
		this.mainPanelWeightY = mainPanelWeightY;

	}
	
	protected void setMainPanel(final JPanel mainPanel) {
		this.mainPanel = mainPanel;
	}
	
	protected void buildUI() {
		dialog.setTitle(dialogTitle);
		dialog.setModal(true);

		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		if (dialogInstructionsText != null) {			
			panelGC.fill = GridBagConstraints.HORIZONTAL;
			panelGC.weightx = 1;		
			JPanel instructionsPanel
				= userInterfaceFactory.createHTMLInstructionPanel(dialogInstructionsText);
			panel.add(instructionsPanel, panelGC);
			panelGC.gridy++;
		}
		
	
		panelGC.fill = mainPanelFillSetting;
		panelGC.weightx = mainPanelWeightX;
		panelGC.weighty = mainPanelWeightY;
		panel.add(mainPanel, panelGC);

		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;		
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		okCloseButtonPanel
			= new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		panel.add(okCloseButtonPanel.getPanel(), panelGC);
				
		dialog.getContentPane().add(panel);
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void show() {
		dialog.setVisible(true);
	}
	
	protected JDialog getDialog() {
		return dialog;
	}
	
	private void ok() {
		try {
			okAction();		
			dialog.setVisible(false);
			isCancelled = false;
			if (doSystemExitOnClose) {
				System.exit(0);
			}
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
			okRecoverAction();
		}	
	}
	
	public void setEnableOKButton(final boolean isEnabled) {
		okCloseButtonPanel.setEnableOKButton(isEnabled);
	}
	
	public void okAction() throws RIFServiceException {
		
		
	}

	public void okRecoverAction() {

	}
	
	private void close() {
		isCancelled = true;
		dialog.setVisible(false);
		if (doSystemExitOnClose) {			
			System.exit(0);
		}		
	}
	
	public boolean isCancelled() {
		return isCancelled;
	}
	
	public void setSize(final int width, final int height) {
		dialog.setSize(width, height);
	}
	
	public void doSystemExitOnClose() {
		doSystemExitOnClose = true;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================
	abstract public void actionPerformed(final ActionEvent event);
	
	protected void performOKCloseActions(final ActionEvent event) {
		Object button = event.getSource();
		
		if (okCloseButtonPanel.isOKButton(button)) {
			ok();
		}
		else if (okCloseButtonPanel.isCloseButton(button)) {
			close();
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================
}
