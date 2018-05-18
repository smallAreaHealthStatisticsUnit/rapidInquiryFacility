package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.presentation.ListEditingButtonPanel;
import org.sahsu.rif.generic.presentation.OrderedListPanel;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;

/**
 *
 * The class is meant to support common properties for the GUI components
 * that support each of the Data Loader Tool's main sections.  
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

public abstract class AbstractDataLoadingThemePanel 
	implements ActionListener, 
	Observer {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final Color populatedColour = new Color(0, 128, 0);
	private static final Color unpopulatedColour = Color.BLACK;
	private static final Color disabledColour = Color.BLACK;
	
	// ==========================================
	// Section Properties
	// ==========================================
	private JFrame frame;
	private DataLoaderToolSession session;
	private UserInterfaceFactory userInterfaceFactory;
	private DLDependencyManager dependencyManager;	
	private DataLoaderToolChangeManager changeManager;
	private OrderedListPanel listPanel;
	private ListEditingButtonPanel listEditingButtonPanel;	
	private JPanel mainPanel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractDataLoadingThemePanel(
		final JFrame frame,
		final DataLoaderToolSession session,
		final DLDependencyManager dependencyManager,
		final DataLoaderToolChangeManager changeManager) {

		this.frame = frame;
		this.session = session;
		this.userInterfaceFactory = session.getUserInterfaceFactory();
		this.dependencyManager = dependencyManager;
		this.changeManager = changeManager;
		
		listPanel 
			= new OrderedListPanel(userInterfaceFactory);
		listPanel.setUseDefaultSelectionPolicy(true);
		listEditingButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		listEditingButtonPanel.includeAddButton("");
		listEditingButtonPanel.includeEditButton("");
		listEditingButtonPanel.includeDeleteButton("");
		listEditingButtonPanel.addActionListener(this);
	}
	
	protected void setListTitle(final String titleText) {
		listPanel.setListTitle(titleText, true);
	}


	protected void buildUI() {
		mainPanel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.anchor = GridBagConstraints.NORTHWEST;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;	
		mainPanel.add(listPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		mainPanel.add(listEditingButtonPanel.getPanel(), panelGC);
		mainPanel.setBorder(LineBorder.createGrayLineBorder());
	}	

	private void updateLabelPopulatedStateColour() {
		if (listPanel.isEnabled() == false) {
			listPanel.setListLabelColour(disabledColour);			
		}
		else if (listPanel.isEmpty() == true) {
			listPanel.setListLabelColour(unpopulatedColour);				
		}
		else {
			listPanel.setListLabelColour(populatedColour);				
		}		
	}
	
	public void setEnable(final boolean isEnabled) {

		if (isEnabled == listPanel.isEnabled()) {
			//no change in state
			return;
		}
		
		listPanel.setEnabled(isEnabled);
		if (isEnabled) {
			updateListButtonStates();
		}
		else {
			listEditingButtonPanel.disableAllButtons();
		}
		updateLabelPopulatedStateColour();
		
	}
	
	private void updateListButtonStates() {
		if (listPanel.isEmpty()) {
			listPanel.setListLabelColour(unpopulatedColour);			
			listEditingButtonPanel.indicateEmptyState();
		}
		else {
			listPanel.setListLabelColour(populatedColour);			
			listEditingButtonPanel.indicatePopulatedState();
		}
		
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public JFrame getFrame() {
		return frame;
	}
	
	public JPanel getPanel() {
		return mainPanel;
	}
	
	public DataLoaderToolSession getSession() {
		return session;
	}
	
	public UserInterfaceFactory getUserInterfaceFactory() {
		return userInterfaceFactory;
	}
	
	public ArrayList<String> getExistingDisplayNames() {
		return listPanel.getDisplayNames();
	}
	
	protected void clearListItems() {
		listPanel.clearList();
	}
	
	protected void setListItems(
		final ArrayList<DisplayableListItemInterface> listItems) {	
		listPanel.clearList();
		listPanel.addListItems(listItems);
		listPanel.updateUI();
		updateLabelPopulatedStateColour();
	}
	
	protected void addListItem(final DisplayableListItemInterface listItem) {
		listPanel.addListItem(listItem);
		listEditingButtonPanel.indicateFullEditingState();
		updateLabelPopulatedStateColour();		
	}
	
	protected void updateListItem(
		final DisplayableListItemInterface originalItem, 
		final DisplayableListItemInterface revisedItem) {
		
		listPanel.replaceItem(originalItem, revisedItem);
	}
	
	protected void deleteSelectedListItems() {
		listPanel.deleteSelectedListItems();
		if (listPanel.isEmpty()) {
			listEditingButtonPanel.indicateEmptyState();
		}
		updateLabelPopulatedStateColour();
	}
	
	protected abstract void refresh();
	protected abstract void addListItem();	
	protected abstract void editSelectedListItem();
	
	protected DLDependencyManager getDependencyManager() {
		return dependencyManager;
	}
	
	protected DataLoaderToolChangeManager getChangeManager() {
		return changeManager;
	}
	
	public ArrayList<DisplayableListItemInterface> getSelectedListItems() {
		return listPanel.getSelectedItems();
	}

	public DisplayableListItemInterface getSelectedListItem() {
		return listPanel.getSelectedItem();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	public void actionPerformed(final ActionEvent event) {
		Object button = event.getSource();
		
		if (listEditingButtonPanel.isAddButton(button)) {
			addListItem();
		}
		else if (listEditingButtonPanel.isEditButton(button)) {
			editSelectedListItem();
		}
		else if (listEditingButtonPanel.isDeleteButton(button)) {
			deleteSelectedListItems();				
		}
	}
	
	//Interface: Observer
	public abstract void update(
		final Observable observable,
	    final Object editingState);
	
	// ==========================================
	// Section Override
	// ==========================================

}


