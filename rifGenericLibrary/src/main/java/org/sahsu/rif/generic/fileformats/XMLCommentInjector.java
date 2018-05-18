
package org.sahsu.rif.generic.fileformats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.sahsu.rif.generic.system.Messages;

/**
 * Is used to write a description of an XML tag where it first occurs in an XML
 * file.  
 *
 * <hr>
 * Copyright 2012 Imperial College London, developed by the Small Area
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


public final class XMLCommentInjector {

// ==========================================
// Section Constants
// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
// ==========================================
// Section Properties
// ==========================================
	/** The introduction comments. */
	private ArrayList<String> introductionComments;
	
	/** The comment from tag name. */
	private HashMap<String, String> commentFromTagName;
	
	/** The first record encountered for tag name. */
	private HashSet<String> firstRecordEncounteredForTagName;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new XML comment injector.
     */
	public XMLCommentInjector() {
		
		commentFromTagName = new HashMap<String, String>();
		firstRecordEncounteredForTagName = new HashSet<String>();
		introductionComments = new ArrayList<String>();
		
		/*
		String introductionMessage
			= GENERIC_MESSAGES.getMessage("general.dataSharingComment1");
		addIntroductionComment(introductionMessage);
		
		setRecordToolTipMessage("user", "user.toolTip");
		setRecordToolTipMessage("calculation_method", "calculationMethod.toolTip");
		setRecordToolTipMessage("parameter", "parameter.toolTip");
		setRecordToolTipMessage("rif_output_option", "rifOutputOption.toolTip");
		setRecordToolTipMessage("disease_mapping_study", "diseaseMappingStudy.label");
		setRecordToolTipMessage("comparison_area", "comparisonArea.toolTip");
		setRecordToolTipMessage("disease_mapping_study_area", "diseaseMappingStudyArea.toolTip");

		setRecordToolTipMessage("age_group", "ageGroup.toolTip");
		setRecordToolTipMessage("health_code", "healthCode.toolTip");
		setRecordToolTipMessage("exposure_covariate", "exposureCovariate.toolTip");
		setRecordToolTipMessage("adjustable_covariate", "adjustableCovariate.toolTip");		
		setRecordToolTipMessage("sex", "sex.toolTip");

		setRecordToolTipMessage("year_range", "yearRange.toolTip");
		setRecordToolTipMessage("year_interval", "yearInterval.toolTip");
		
		setRecordToolTipMessage("geolevel_select", "geoLevelSelect.toolTip");
		setRecordToolTipMessage("geolevel_area", "geoLevelArea.toolTip");
		setRecordToolTipMessage("geolevel_view", "geoLevelView.toolTip");
		setRecordToolTipMessage("geolevel_to_map", "geoLevelToMap.toolTip");

		
		setFieldToolTipMessage("health_code", "code", "healthCode.code.toolTip");
		
		*/
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
	/**
	 * Gets the record comment.
	 *
	 * @param recordName the record name
	 * @return the record comment
	 */
	public String getRecordComment(
		final String recordName) {

		String key = deriveKey(recordName, "");				
		return commentFromTagName.get(key);			
	}

	/**
	 * Gets the field comment.
	 *
	 * @param recordName the record name
	 * @param fieldName the field name
	 * @return the field comment
	 */
	public String getFieldComment(
		final String recordName,
		final String fieldName) {

		String key = deriveKey(recordName, fieldName);				
		return commentFromTagName.get(key);		
	}
	
	/**
	 * Register record comment.
	 *
	 * @param recordName the record name
	 * @param comment the comment
	 */
	public void registerRecordComment(
		final String recordName,
		final String comment) {

		String key = deriveKey(recordName, "");
		commentFromTagName.put(key, comment);
	}	
	
	/**
	 * Register field comment.
	 *
	 * @param recordName the record name
	 * @param fieldName the field name
	 * @param comment the comment
	 */
	public void registerFieldComment(
		final String recordName,
		final String fieldName,
		final String comment) {		

		String key = deriveKey(recordName, fieldName);
		commentFromTagName.put(key, comment);	
	}
		
	/**
	 * Checks if is first record occurrence.
	 *
	 * @param recordName the record name
	 * @return true, if is first record occurrence
	 */
	public boolean isFirstRecordOccurrence(
		final String recordName) {

		String key = deriveKey(recordName, "");	
		if (firstRecordEncounteredForTagName.contains(key)) {
			//if key has been registered it has been used once
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Checks if is first field occurrence.
	 *
	 * @param recordName the record name
	 * @param fieldName the field name
	 * @return true, if is first field occurrence
	 */
	public boolean isFirstFieldOccurrence(
		final String recordName,
		final String fieldName) {

		String key = deriveKey(recordName, fieldName);
		if (firstRecordEncounteredForTagName.contains(key)) {
			//if key has been registered it has been used once
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Sets the first record occurrence.
	 *
	 * @param recordName the new first record occurrence
	 */
	public void setFirstRecordOccurrence(
		final String recordName) {

		String key = deriveKey(recordName, "");				
		firstRecordEncounteredForTagName.add(key);
	}
	
	/**
	 * Sets the first field occurrence.
	 *
	 * @param recordName the record name
	 * @param fieldName the field name
	 */
	public void setFirstFieldOccurrence(
		final String recordName,
		final String fieldName) {

		String key = deriveKey(recordName, fieldName);				
		firstRecordEncounteredForTagName.add(key);		
	}
	
	/**
	 * Gets the introduction comments.
	 *
	 * @return the introduction comments
	 */
	public ArrayList<String> getIntroductionComments() {

		return introductionComments;
	}

	/**
	 * Adds the introduction comment.
	 *
	 * @param comment the comment
	 */
	public void addIntroductionComment(
		final String comment) {

		introductionComments.add(comment);
	}
	
	/**
	 * Derive key.
	 *
	 * @param recordTagName the record tag name
	 * @param fieldName the field name
	 * @return the string
	 */
	private String deriveKey(
		final String recordTagName, 
		final String fieldName) {

		StringBuilder buffer = new StringBuilder();
		buffer.append(recordTagName);
		buffer.append("-");
		buffer.append(fieldName);
		return buffer.toString();
	}
	
	/**
	 * Sets the record tool tip message.
	 *
	 * @param recordTagName the record tag name
	 * @param propertyName the property name
	 */
	public void setRecordToolTipMessage(
		final String recordTagName,
		final String propertyName) {
		
		String toolTipText
			= GENERIC_MESSAGES.getMessage(propertyName);
		registerFieldComment(recordTagName, "", toolTipText);
	}	
	
	/**
	 * Sets the field tool tip message.
	 *
	 * @param recordTagName the record tag name
	 * @param fieldTagName the field tag name
	 * @param propertyName the property name
	 */
	public void setFieldToolTipMessage(
		final String recordTagName, 
		final String fieldTagName,
		final String propertyName) {
		
		String toolTipText
			= GENERIC_MESSAGES.getMessage(propertyName);
		registerFieldComment(recordTagName, fieldTagName, toolTipText);
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
