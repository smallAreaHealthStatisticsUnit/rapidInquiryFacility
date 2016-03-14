package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.GeographicalResolutionLevel;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;
import rifGenericLibrary.presentationLayer.ListEditingButtonPanel;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;

import java.awt.event.*;
import java.util.ArrayList;



/**
 *
 *
 * <hr>
 * Copyright 2016 Imperial College London, developed by the Small Area
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

public class GeographicalResolutionEditorDialog 
	implements ActionListener, ListSelectionListener {

	
	public static void main(String[] arguments) {
		UserInterfaceFactory userInterfaceFactory
			= new UserInterfaceFactory();
		GeographicalResolutionEditorDialog dialog
			= new GeographicalResolutionEditorDialog(userInterfaceFactory);
		dialog.show();
	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private UserInterfaceFactory userInterfaceFactory;

	private JDialog dialog;
	
	private JTextField nameTextField;
	private JTextArea descriptionTextArea;
	
	private OrderedListPanel listPanel;
	private ListEditingButtonPanel listEditingButtonPanel;
	private OKCloseButtonPanel okCloseButtonPanel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GeographicalResolutionEditorDialog(
		final UserInterfaceFactory userInterfaceFactory) {

		this.userInterfaceFactory = userInterfaceFactory;
	
		String title
			= RIFDataLoaderToolMessages.getMessage(
				"geographicalResolutionEditorDialog.title");
		dialog
			= userInterfaceFactory.createDialog(title);
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		JSplitPane splitPane
			= userInterfaceFactory.createLeftRightSplitPane(
					createListEditingPanel(), 
					createFieldPanel());
		panel.add(splitPane, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		okCloseButtonPanel = new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		panel.add(okCloseButtonPanel.getPanel(), panelGC);

		dialog.getContentPane().add(panel);
		dialog.setSize(400, 400);
		dialog.setModal(true);
	}

	private JPanel createListEditingPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		
		String geographicalResolutionLevelListTitle
			= RIFDataLoaderToolMessages.getMessage(
				"geographicalResolutionLevel.plural.label");
		listPanel 
			= new OrderedListPanel(
					geographicalResolutionLevelListTitle,
					"",
					userInterfaceFactory,
					false);
		listPanel.addListSelectionListener(this);
		panel.add(listPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		listEditingButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		listEditingButtonPanel.addActionListener(this);
		listEditingButtonPanel.includeAddButton("");
		listEditingButtonPanel.includeEditButton("");
		listEditingButtonPanel.includeCopyButton("");
		listEditingButtonPanel.includeDeleteButton("");
		panel.add(listEditingButtonPanel.getPanel(), panelGC);
		
		return panel;
	}
	
	private JPanel createFieldPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		String nameLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"geographicalResolutionLevel.name.label");
		JLabel nameLabel
			= userInterfaceFactory.createLabel(nameLabelText);
		panel.add(nameLabel, panelGC);
		panelGC.gridx++;		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		nameTextField = userInterfaceFactory.createTextField();
		panel.add(nameTextField, panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		String descriptionLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"geographicalResolutionLevel.description.label");
		JLabel descriptionLabel
			= userInterfaceFactory.createLabel(descriptionLabelText);
		panel.add(descriptionLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		descriptionTextArea
			= userInterfaceFactory.createTextArea();
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(descriptionTextArea);
		panel.add(scrollPane, panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public ArrayList<GeographicalResolutionLevel> getGeographicalResolutionLevels() {
		ArrayList<GeographicalResolutionLevel> results
			= new ArrayList<GeographicalResolutionLevel>();
		ArrayList<DisplayableListItemInterface> listItems
			= listPanel.getAllItems();
		
		for (DisplayableListItemInterface listItem : listItems) {
			results.add((GeographicalResolutionLevel) listItem);
		}
		
		return results;
	}
	
	public void setData(final ArrayList<GeographicalResolutionLevel> levels) {
		listPanel.clearList();		
		for (GeographicalResolutionLevel level : levels) {
			listPanel.addListItem(level);
		}
		listPanel.updateUI();
	}
	
	public void show() {
		dialog.setVisible(true);
	}
	
	private void ok() {

		dialog.setVisible(false);
	}
	
	private void close() {		
		dialog.setVisible(false);
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
		
		if (okCloseButtonPanel.isOKButton(button)) {
			ok();
		}
		else if (okCloseButtonPanel.isCloseButton(button)) {
			close();
		}
	}
	
	public void valueChanged(final ListSelectionEvent event) {
		
		GeographicalResolutionLevel selectedItem
			= (GeographicalResolutionLevel) listPanel.getSelectedItem();
		
		nameTextField.setText(selectedItem.getName());
		descriptionTextArea.setText(selectedItem.getDescription());	
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


