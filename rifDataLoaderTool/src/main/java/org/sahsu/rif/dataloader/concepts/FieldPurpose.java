package org.sahsu.rif.dataloader.concepts;

import java.text.Collator;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.Messages;

/**
 * Describes the meaning of a CSV field as it will relate to the RIF.  
 * Knowing the meaning of a field can help set default configuration properties
 * in data sets and detect problems in them as well.
 * 
 * <p>
 * <b>An aid to setting configuration options</b>.  If a field is a 
 * <code>GEOGRAPHICAL_RESOLUTION</code> then its <code>cleanFieldName</code>
 * value must be the name of one of the geographical resolutions that will
 * be supported by the RIF.  For example, if a CSV field "district_cde" is
 * assigned a purpose of <code>GEOGRAPHICAL_RESOLUTION</code>, then users may
 * be prompted to specify whether "district_cde" maps to the name "district",
 * "region", or "ward".  After data cleaning, the field "district_cde" may
 * be renamed "district" so that its name exactly corresponds with a "district"
 * field that is defined in a shape file.
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

public enum FieldPurpose {
	COVARIATE(
		"covariate", 
		"fieldPurpose.covariate.label"), 
	GEOGRAPHICAL_RESOLUTION(
		"geographical_resolution", 
		"fieldPurpose.geograpicalResolution.label"), 
	HEALTH_CODE(
		"health_code",
		"fieldPurpose.healthCode.label"),
	TOTAL_COUNT(
		"total_count",
		"fieldPurpose.totalCount.label"),
	OTHER(
		"other",
		"fieldPurpose.other.label");
	
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	private String code;
	private String propertyName;
	
	private FieldPurpose(
		final String code, 
		final String propertyName) {
		
		this.code = code;
		this.propertyName = propertyName;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getName() {
		
		return RIFDataLoaderToolMessages.getMessage(propertyName);
	}
	
	public static FieldPurpose getValueFromCode(
		final String code) {
		
		Collator collator = GENERIC_MESSAGES.getCollator();
		if (collator.equals(code, COVARIATE.getCode())) {
			return COVARIATE;
		}
		else if (collator.equals(code, GEOGRAPHICAL_RESOLUTION.getCode())) {
			return GEOGRAPHICAL_RESOLUTION;
		}
		else if (collator.equals(code, HEALTH_CODE.getCode())) {
			return HEALTH_CODE;
		}
		else if (collator.equals(code, TOTAL_COUNT.getCode())) {
			return TOTAL_COUNT;
		}
		else if (collator.equals(code, OTHER.getCode())) {
			return OTHER;
		}
		else {
			assert false;
			return null;
		}		
	}
	
	public static String[] getNames() {
		String[] results = new String[5];
		results[0] = COVARIATE.getName();
		results[1] = GEOGRAPHICAL_RESOLUTION.getName();
		results[2] = HEALTH_CODE.getName();
		results[3] = TOTAL_COUNT.getName();
		results[4] = OTHER.getName();
		
		return results;
	}
	
	
	public static FieldPurpose getFieldPurposeFromName(final String name) {
		Collator collator = GENERIC_MESSAGES.getCollator();
		if (collator.equals(COVARIATE.getName(), name)) {
			return COVARIATE;
		}
		else if (collator.equals(GEOGRAPHICAL_RESOLUTION.getName(), name)) {
			return GEOGRAPHICAL_RESOLUTION;
		}
		else if (collator.equals(GEOGRAPHICAL_RESOLUTION.getName(), name)) {
			return GEOGRAPHICAL_RESOLUTION;
		}
		else if (collator.equals(HEALTH_CODE.getName(), name)) {
			return HEALTH_CODE;
		}
		else if (collator.equals(TOTAL_COUNT.getName(), name)) {
			return TOTAL_COUNT;
		}
		else if (collator.equals(OTHER.getName(), name)) {
			return OTHER;
		}		
		else {
			assert false;
			return null;
		}

	}
	
	
}


