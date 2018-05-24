package org.sahsu.rif.dataloader.presentation.interactive;

import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataLoaderServiceAPI;
import org.sahsu.rif.dataloader.concepts.ValidationRule;
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

class DataValidationPolicyEditingPanel
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

	public DataValidationPolicyEditingPanel(final DataLoaderToolSession session) {
		super(session);
	
		String dataTypeCleaningPolicyLabelText
			= RIFDataLoaderToolMessages.getMessage("fieldValidationPolicy.label");
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("dataTypeEditingPanel.validationPolicy.instructions");
	
		DataLoaderServiceAPI dataLoaderService
			= session.getDataLoaderService();
		String[] validationFunctionNames = new String[0];
		try {
			validationFunctionNames
				= dataLoaderService.getValidationFunctionNames(session.getRIFManager());
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				null, 
				rifServiceException.getErrorMessages());
		}	
		
		
		buildUI(
			dataTypeCleaningPolicyLabelText, 
			instructionsText,
			validationFunctionNames);
	}
	
		
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setValidationRulesPolicy(final ArrayList<ValidationRule> validationRules) {
		ArrayList<DisplayableListItemInterface> rules
			= new ArrayList<DisplayableListItemInterface>();
		for (ValidationRule validationRule : validationRules) {
			rules.add(validationRule);
		}
		
		setUseRulesPolicy(rules);
	}
	
	public ArrayList<ValidationRule> getValidationRules() {
		ArrayList<ValidationRule> validationRules = new ArrayList<ValidationRule>();
		ArrayList<DisplayableListItemInterface> listItems
			= getRules();
		for (DisplayableListItemInterface listItem : listItems) {
			validationRules.add((ValidationRule) listItem);
		}
		return validationRules;
	}
		
	public String getCleaningFunctionName() {
		return getSelectedFunctionName();
	}

	protected void addRule() {
		DataLoaderToolSession session = getSession();
		ValidationRuleEditorDialog validatingEditorDialog
			= new ValidationRuleEditorDialog(session);
		ValidationRule validationRule
			= ValidationRule.newInstance();
		
		validatingEditorDialog.setData(
			validationRule, 
			getExistingRuleNames(),
			true);
		validatingEditorDialog.show();
		if (validatingEditorDialog.isCancelled()) {
			return;
		}
		
		validatingEditorDialog.saveChanges();		
		session.setSaveChanges(true);
		
		addRuleListItem(validationRule);
	}
	
	protected void editRule() {
		ValidationRule selectedValidationRule = (ValidationRule) getSelectedRule();

		DataLoaderToolSession session
			= getSession();
		String oldDisplayName
			= selectedValidationRule.getDisplayName();
		ValidationRuleEditorDialog validationRuleEditorDialog
			= new ValidationRuleEditorDialog(session);
		validationRuleEditorDialog.setData(
			selectedValidationRule, 
			getExistingRuleNames(),
			true);
		validationRuleEditorDialog.show();

		
		if (validationRuleEditorDialog.isCancelled()) {
			return;
		}

		boolean saveChanges
			= validationRuleEditorDialog.saveChanges();
		if (saveChanges) {
			session.setSaveChanges(true);
		}
		
		String newDisplayName
			= selectedValidationRule.getDisplayName();		
		updateRuleListItem(
			oldDisplayName, 
			newDisplayName, 
			selectedValidationRule);
	}

	protected void copyRule() {
		ValidationRule selectedValidationRule
			= (ValidationRule) getSelectedRule();
		ValidationRule cloneValidationRule
			= ValidationRule.createCopy(selectedValidationRule);
		String currentValidationRuleName
			= selectedValidationRule.getName();
		
		DataLoaderToolSession session = getSession();
		//should ensure unique name
		cloneValidationRule.setName("Copy of " + currentValidationRuleName);
		ValidationRuleEditorDialog validationRuleEditorDialog
			= new ValidationRuleEditorDialog(session);
		validationRuleEditorDialog.setData(
			selectedValidationRule, 
			getExistingRuleNames(),
			true);
		validationRuleEditorDialog.show();

		if (validationRuleEditorDialog.isCancelled()) {
			return;
		}
		
		session.setSaveChanges(true);		
		addRuleListItem(cloneValidationRule);
	}
		
	protected void updateSelectedFunctionDescription() {
		String selectedValidationFunctionName = getSelectedFunctionName();
		
		DataLoaderServiceAPI dataLoaderService
			= getSession().getDataLoaderService();
		DataLoaderToolSession session = getSession();		
		String functionDescription = "";
		try {
			functionDescription
				= dataLoaderService.getDescriptionForValidationFunction(
					session.getRIFManager(),
					selectedValidationFunctionName);
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


