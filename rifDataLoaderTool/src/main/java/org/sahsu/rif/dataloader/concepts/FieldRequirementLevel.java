package org.sahsu.rif.dataloader.concepts;

import java.text.Collator;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.Messages;

/**
 * Describes whether a CSV field is a required field, an extra field
 * or if it should be ignored.  If the setting is 
 * <code>REQUIRED_BY_RIF</code>, then some aspect of the RIF database
 * schema requires the field.  Examples of required fields include:
 * <ul>
 * <li>
 * numerator and denominator table fields require at least one 
 * field that has a {@link rifDataLoaderTool.businessConceptLayer.FieldPurpose}
 * of <code>GEOGRAPHICAL_RESOLUTION</code>, one field with a 
 * {@link rifDataLoaderTool.businessConceptLayer.RIFDataType} of 
 * <code>RIF_SEX_DATA_TYPE</code> and another field of type
 * <code>RIF_AGE_DATA_TYPE</code>.
 * </li>
 * <li>
 * data sets that are meant to have a 
 * {@link rifDataLoaderTool.businessConceptLayer.RIFSchemaArea}
 * of <code>HEALTH_NUMERATOR_DATA</code> or <code>HEALTH_CODE_DATA</code>
 * must have at least one field that has a <code>FieldPurpose</code>
 * of <code>HEALTH_CODE</code>.
 * </li>
 * </ul>
 * 
 * <p>
 * Some administrators may want to include a data field which
 * is not necessary for RIF operations but which is useful for studies
 * in their projects.  For example, a field called <code>ethnicity</code>
 * may be useful in numerator and denominator tables, even if it is not
 * processed by the RIF's extract facilities.
 * </p>
 * 
 * <p>
 * Many fields that are imported from a CSV may have no use at all in
 * the RIF.  If they are marked as <code>IGNORE_FIELD</code> then they
 * will be extracted from the file but will not be processed any further.
 * </p>
 * 
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
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

public enum FieldRequirementLevel {
	REQUIRED_BY_RIF(
		"required_by_rif",
		"fieldRequirementLevel.requiredByRIF"),		
	EXTRA_FIELD(
		"extra_field",
		"fieldRequirementLevel.extraField"),
	IGNORE_FIELD(
		"ignore_field",
		"fieldRequirementLevel.ignoreField");
	
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	private String code;
	private String propertyName;
	
	private FieldRequirementLevel(
		final String code,
		final String propertyName) {
		
		this.code = code;
		this.propertyName = propertyName;	
	}
	
	public String getCode() {
		return code;
	}
	
	public String getName() {
		String name
			= RIFDataLoaderToolMessages.getMessage(propertyName);
		return name;
	}	
	
	public static String[] getNames() {
		String[] names = new String[3];
		names[0] = REQUIRED_BY_RIF.getName();
		names[1] = EXTRA_FIELD.getName();
		names[2] = IGNORE_FIELD.getName();
		
		return names;
		
	}
	
	
	public static FieldRequirementLevel getValueFromCode(
		final String code) {
		
		Collator collator = GENERIC_MESSAGES.getCollator();
		if (collator.equals(code, REQUIRED_BY_RIF.getCode()) == true) {
			return REQUIRED_BY_RIF;
		}
		else if (collator.equals(code, EXTRA_FIELD.getCode()) == true) {
			return EXTRA_FIELD;
		}
		else if (collator.equals(code, IGNORE_FIELD.getCode()) == true) {
			return IGNORE_FIELD;
		}
		else {
			assert false;
			return null;
		}		
	}	

	public static FieldRequirementLevel getValueFromName(
		final String name) {
			
		Collator collator = GENERIC_MESSAGES.getCollator();
		if (collator.equals(name, REQUIRED_BY_RIF.getName()) == true) {
			return REQUIRED_BY_RIF;
		}
		else if (collator.equals(name, EXTRA_FIELD.getName()) == true) {
			return EXTRA_FIELD;
		}
		else if (collator.equals(name, IGNORE_FIELD.getName()) == true) {
			return IGNORE_FIELD;
		}
		else {
			assert false;
			return null;
		}		
	}	
	
	
}


