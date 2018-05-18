package org.sahsu.rif.dataloader.concepts;

import java.text.Collator;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.Messages;


/**
 * Describes different extents to which the Data Loader Tool will audit
 * changes made to a field.  The three levels that are supported are 
 * described in the following table:
 * 
 * <p>
 * </p>
 * 
 * <table>
 * <tr>
 * <th>Level</th>
 * <th>Description</th>
 * </tr>
 * 
 * <tr>
 * <td>
 * <code>NONE</code>
 * </td>
 * <td>
 * Do not record anything when a field value is changed.  This is often useful
 * when either it isn't important to know how a field changes or it is expected
 * to change the format but not the meaning of all of a field's values.  For 
 * example, consider a postal code field where all the entries use lower case
 * letters but a project wants to use only upper case characters.  Changing a 
 * postal code from "n1 9fz" to "N1 9FZ" may change the format but it won't change
 * the meaning.  If it is expected that a change will affect thousands of field values
 * then database administrators may prefer not to flood their change logs with useless
 * information.
 * </td>
 * </tr>
 * </td>
 * 
 * <tr>
 * <td>
 * <code>INCLUDE_FIELD_NAME_ONLY</code>
 * </td>
 * <td>
 * Records a change of the form "X" where X represents the field name.  For example,
 * consider a field "ethnicity" where many specialised ethnicity codes are merged into
 * fewer categories.  Administrators may want to know that a patient's age has been
 * mapped, but not that their age has been mapped from 23 to 20-25.  This setting is
 * useful when it is useful to know that a field has been changed but prevent sensitive
 * information from being written to a log file.
 * </td>
 * </tr>
 * <tr>
 * <code>INCLUDE_FIELD_CHANGE_DESCRIPTION</code>
 * <td>
 * Using this settings, a field change will be recorded with a message of the form:
 * <code>"Field X was changed from Y to Z"</code>.  We suggest you use this setting for 
 * non-sensitive fields where only a few values are expected to be changed through
 * cleaning routines.
 * </td>
 * <td>
 * </td>
 * </tr>
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

public enum FieldChangeAuditLevel {

	NONE(
		"none", 
		"fieldChangeAuditLevel.none.label"),
	INCLUDE_FIELD_NAME_ONLY(
		"include_field_name_only", 
		"fieldChangeAuditLevel.includeFieldNameOnly.label"),
	INCLUDE_FIELD_CHANGE_DESCRIPTION(
		"include_field_change_description",
		"fieldChangeAuditLevel.includeFieldChangeDescription.label");
	
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	private String code;
	private String propertyName;
	
	FieldChangeAuditLevel(
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
		names[0] = NONE.getName();
		names[1] = INCLUDE_FIELD_NAME_ONLY.getName();
		names[2] = INCLUDE_FIELD_CHANGE_DESCRIPTION.getName();
		
		return names;
	}

	
	public static FieldChangeAuditLevel getValueFromName(
		final String code) {
		
		Collator collator = GENERIC_MESSAGES.getCollator();
		if (collator.equals(
						code,
						NONE.getName())) {

			return NONE;
		}
		else if (collator.equals(
						code,
						INCLUDE_FIELD_NAME_ONLY.getName())) {

			return INCLUDE_FIELD_NAME_ONLY;
		}
		else if (collator.equals(
						code,
						INCLUDE_FIELD_CHANGE_DESCRIPTION.getName())) {

			return INCLUDE_FIELD_CHANGE_DESCRIPTION;
		}
		else {
			assert false;
			return null;
		}		
	}	
	
	
	public static FieldChangeAuditLevel getValueFromCode(
		final String code) {
		
		Collator collator = GENERIC_MESSAGES.getCollator();
		if (collator.equals(
						code,
						NONE.getCode())) {

			return NONE;
		}
		else if (collator.equals(
						code,
						INCLUDE_FIELD_NAME_ONLY.getCode())) {

			return INCLUDE_FIELD_NAME_ONLY;
		}
		else if (collator.equals(
						code,
						INCLUDE_FIELD_CHANGE_DESCRIPTION.getCode())) {

			return INCLUDE_FIELD_CHANGE_DESCRIPTION;
		}
		else {
			assert false;
			return null;
		}		
	}	
	
}


