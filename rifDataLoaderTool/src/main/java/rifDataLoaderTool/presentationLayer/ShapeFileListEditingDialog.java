package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.businessConceptLayer.ShapeFile;
import rifGenericLibrary.presentationLayer.*;
import rifGenericLibrary.system.RIFServiceException;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.event.*;
import java.awt.GridBagConstraints;
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

public class ShapeFileListEditingDialog 
	extends OKCloseButtonDialog {

	public static void main(String[] args) {
		UserInterfaceFactory userInterfaceFactory
			= new UserInterfaceFactory();
		ShapeFileListEditingDialog dialog
			= new ShapeFileListEditingDialog(userInterfaceFactory);
		dialog.show();
	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private OrderedListPanel listPanel;
	private ListEditingButtonPanel listEditingButtonPanel;
	
	private ArrayList<ShapeFile> currentShapeFiles;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ShapeFileListEditingDialog(
		final UserInterfaceFactory userInterfaceFactory) {

		super(userInterfaceFactory);
		currentShapeFiles = new ArrayList<ShapeFile>();
		
		String title
			= RIFDataLoaderToolMessages.getMessage("shapeFileListEditingDialog.title");
		setDialogTitle(title);
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("shapeFileListEditingDialog.instructions");
		setInstructionText(instructionsText);

		setMainPanel(createMainPanel());
		buildUI();
		setSize(500, 500);
	}

	private JPanel createMainPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
	
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;		
		String shapeFileListTitle
			= RIFDataLoaderToolMessages.getMessage("shapeFile.name.plural.label");
		listPanel 
			= new OrderedListPanel(
				shapeFileListTitle, 
				"", 
				userInterfaceFactory, 
				false);
		panel.add(listPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;		
		listEditingButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		listEditingButtonPanel.includeAddButton("");
		listEditingButtonPanel.includeDeleteButton("");
		listEditingButtonPanel.addActionListener(this);
		panel.add(listEditingButtonPanel.getPanel(), panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public ArrayList<ShapeFile> getShapeFiles() {
		ArrayList<ShapeFile> results = new ArrayList<ShapeFile>();
		
		ArrayList<DisplayableListItemInterface> listItems
			= listPanel.getAllItems();
		for (DisplayableListItemInterface listItem : listItems) {
			results.add((ShapeFile) listItem);
		}
		
		return results;
	}
	
	public void setData(final ArrayList<ShapeFile> shapeFiles) {
		currentShapeFiles.clear();
		currentShapeFiles.addAll(shapeFiles);

		for (ShapeFile currentShapeFile : currentShapeFiles) {
			listPanel.addListItem(currentShapeFile);
		}
		listPanel.updateUI();
	}
	
	protected void okAction() 
		throws RIFServiceException {	
	}
	
	private void addShapeFile() {
		ShapeFileSelectionDialog shapeFileSelectionDialog
			= new ShapeFileSelectionDialog(getUserInterfaceFactory());
		shapeFileSelectionDialog.show();
		if (shapeFileSelectionDialog.isCancelled()) {
			return;
		}
		
		ArrayList<ShapeFile> shapeFiles
			= shapeFileSelectionDialog.getShapeFiles();
		
		for (ShapeFile shapeFile : shapeFiles) {
			listPanel.addListItem(shapeFile);
		}
		listPanel.updateUI();
	}
	 
	private void deleteShapeFile() {
		listPanel.deleteSelectedListItems();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	public void actionPerformed(final ActionEvent event) {
		Object button = event.getSource();
		
		if (listEditingButtonPanel.isAddButton(button)) {
			addShapeFile();
		}
		else if (listEditingButtonPanel.isDeleteButton(button)) {
			deleteShapeFile();
		}
		else {
			super.performOKCloseActions(event);
		}
		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


