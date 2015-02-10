package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolSession;
import rifDataLoaderTool.businessConceptLayer.RIFDataLoaderServiceAPI;
import rifServices.businessConceptLayer.User;
import rifGenericUILibrary.OKCloseButtonPanel;
import rifGenericUILibrary.UserInterfaceFactory;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public abstract class AbstractDataLoaderToolDialog 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private JDialog dialog;
	private RIFDataLoaderToolSession session;
	private UserInterfaceFactory userInterfaceFactory;
	
	private OKCloseButtonPanel okCloseButtonPanel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractDataLoaderToolDialog(
		final RIFDataLoaderToolSession session) {

		initialise(session, true);
	}

	public AbstractDataLoaderToolDialog(
		final RIFDataLoaderToolSession session,
		final boolean includeOKButton) {

		initialise(session, includeOKButton);
	}
	
	private void initialise(
		final RIFDataLoaderToolSession session,
		final boolean includeOKButton) {

		this.session = session;
		userInterfaceFactory = session.getUserInterfaceFactory();
		
		dialog = userInterfaceFactory.createDialog("");
		dialog.setModal(true);
		okCloseButtonPanel 
			= new OKCloseButtonPanel(
				userInterfaceFactory,
				includeOKButton);
		okCloseButtonPanel.addActionListener(this);		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	protected RIFDataLoaderToolSession getSession() {
		return session;		
	}
	
	protected User getCurrentUser() {
		return session.getUser();
	}
	
	protected RIFDataLoaderServiceAPI getService() {
		return session.getService();
	}
	
	protected void setDialogTitle(
		final String dialogTitle) {
		
		dialog.setTitle(dialogTitle);
	}
	
	protected void setSize(
		final int width,
		final int length) {
		
		dialog.setSize(width, length);
	}
	
	protected void setMainPanel(final JPanel panel) {
		dialog.getContentPane().add(panel);
	}

	public void show() {
		dialog.setVisible(true);
	}
	
	public void hide() {
		dialog.setVisible(false);
	}
	
	protected boolean isOKButton(final Object button) {
		return okCloseButtonPanel.isOKButton(button);
	}
	
	protected boolean isCloseButton(final Object button) {
		return okCloseButtonPanel.isCloseButton(button);		
	}
	
	protected UserInterfaceFactory getUserInterfaceFactory() {
		return userInterfaceFactory;
	}
	
	protected JPanel getOKCloseButtonPanel() {
		return okCloseButtonPanel.getPanel();
	}
	
	protected JDialog getDialog() {
		return dialog;
	}
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	public abstract void actionPerformed(ActionEvent event);
	
	// ==========================================
	// Section Override
	// ==========================================

}


