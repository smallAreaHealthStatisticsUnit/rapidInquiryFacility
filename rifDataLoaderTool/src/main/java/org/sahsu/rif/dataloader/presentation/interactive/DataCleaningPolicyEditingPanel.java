package org.sahsu.rif.dataloader.presentation.interactive;

import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.CleaningRule;
import org.sahsu.rif.dataloader.concepts.DataLoaderServiceAPI;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.presentation.ErrorDialog;
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

class DataCleaningPolicyEditingPanel
	extends AbstractFieldPolicyEditingPanel {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
		
	// ==========================================
	// Section Construction
	// ==========================================

	public DataCleaningPolicyEditingPanel(final DataLoaderToolSession session) {
		super(session);
	
		String dataTypeCleaningPolicyLabelText
			= RIFDataLoaderToolMessages.getMessage("fieldCleaningPolicy.label");
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("dataTypeEditingPanel.cleaningPolicy.instructions");
		
		DataLoaderServiceAPI dataLoaderService
			= session.getDataLoaderService();
		String[] cleaningFunctionNames = new String[0];
		try {
			cleaningFunctionNames
				= dataLoaderService.getCleaningFunctionNames(session.getRIFManager());
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				null, 
				rifServiceException.getErrorMessages());
		}
		
		buildUI(
			dataTypeCleaningPolicyLabelText, 
			instructionsText,
			cleaningFunctionNames);
	}
	
		
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setCleaningRulesPolicy(final ArrayList<CleaningRule> cleaningRules) {
		ArrayList<DisplayableListItemInterface> rules
			= new ArrayList<DisplayableListItemInterface>();
		for (CleaningRule cleaningRule : cleaningRules) {
			rules.add(cleaningRule);
		}
		
		setUseRulesPolicy(rules);
	}
	
	public ArrayList<CleaningRule> getCleaningRules() {
		ArrayList<CleaningRule> cleaningRules = new ArrayList<CleaningRule>();
		ArrayList<DisplayableListItemInterface> listItems
			= getRules();
		for (DisplayableListItemInterface listItem : listItems) {
			cleaningRules.add((CleaningRule) listItem);
		}
		return cleaningRules;
	}
		
	public String getCleaningFunctionName() {
		return getSelectedFunctionName();
	}

	protected void addRule() {
		DataLoaderToolSession session = getSession();
		CleaningRuleEditorDialog cleaningEditorDialog
			= new CleaningRuleEditorDialog(session);
		CleaningRule cleaningRule
			= CleaningRule.newInstance();
		
		cleaningEditorDialog.setData(
			cleaningRule, 
			getExistingRuleNames(),
			true);
		cleaningEditorDialog.show();
		if (cleaningEditorDialog.isCancelled()) {
			return;
		}
		
		cleaningEditorDialog.saveChanges();		
		session.setSaveChanges(true);
		
		addRuleListItem(cleaningRule);
	}
	
	protected void editRule() {
		CleaningRule selectedCleaningRule = (CleaningRule) getSelectedRule();

		DataLoaderToolSession session
			= getSession();
		String oldDisplayName
			= selectedCleaningRule.getDisplayName();
		CleaningRuleEditorDialog cleaningRuleEditorDialog
			= new CleaningRuleEditorDialog(session);
		cleaningRuleEditorDialog.setData(
			selectedCleaningRule, 
			getExistingRuleNames(),
			true);
		cleaningRuleEditorDialog.show();
	
		if (cleaningRuleEditorDialog.isCancelled()) {
			return;
		}

		boolean saveChanges
			= cleaningRuleEditorDialog.saveChanges();
		if (saveChanges) {
			session.setSaveChanges(true);
		}
		
		String newDisplayName
			= selectedCleaningRule.getDisplayName();		
		updateRuleListItem(
			oldDisplayName, 
			newDisplayName, 
			selectedCleaningRule);
	}

	protected void copyRule() {
		CleaningRule selectedCleaningRule
			= (CleaningRule) getSelectedRule();
		CleaningRule cloneCleaningRule
			= CleaningRule.createCopy(selectedCleaningRule);
		String currentCleaningRuleName
			= selectedCleaningRule.getName();
		
		DataLoaderToolSession session = getSession();
		//should ensure unique name
		cloneCleaningRule.setName("Copy of " + currentCleaningRuleName);
		CleaningRuleEditorDialog cleaningRuleEditorDialog
			= new CleaningRuleEditorDialog(session);
		cleaningRuleEditorDialog.setData(
			selectedCleaningRule, 
			getExistingRuleNames(),
			true);
		cleaningRuleEditorDialog.show();

		if (cleaningRuleEditorDialog.isCancelled()) {
			return;
		}
		
		session.setSaveChanges(true);		
		addRuleListItem(cloneCleaningRule);
	}
		
	protected void updateSelectedFunctionDescription() {
		String selectedCleaningFunctionName = getSelectedFunctionName();
		
		DataLoaderToolSession session = getSession();
		DataLoaderServiceAPI dataLoaderService
			= getSession().getDataLoaderService();
		String functionDescription = "";
		try {
			functionDescription
				= dataLoaderService.getDescriptionForCleaningFunction(
					session.getRIFManager(),
					selectedCleaningFunctionName);
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
			null, 
			rifServiceException.getErrorMessages());
		}	
		
		updateFunctionDescription(functionDescription);
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

}


