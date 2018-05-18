package org.sahsu.rif.dataloader.concepts;

import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.Messages;

/**
 * An enumeration that describes the intended area of the RIF schema that
 * an imported data set will support.
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

public enum RIFSchemaArea {
	
	
	COVARIATE_DATA(
		"rifSchemaArea.covariateData.label", 
		"covariate_data",
		"covar_"),
	HEALTH_CODE_DATA(
		"rifSchemaArea.healthCodeData.label", 
		"health_code_data",
		"tax_"),
	HEALTH_THEMES(
		"rifSchemaArea.healthThemes.label", 
		"health_themes",
		"thm_"),
	HEALTH_NUMERATOR_DATA(
		"rifSchemaArea.healthNumeratorData.label", 
		"health_numerator_data",
		"num_"),
	POPULATION_DENOMINATOR_DATA(
		"rifSchemaArea.populationDenominatorData.label", 
		"population_denominator_data",
		"pop_");
	
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	private String propertyName;
	private String code;
	private String tablePrefix;
	
	RIFSchemaArea(
		final String propertyName,
		final String code,
		final String tablePrefix) {
		
		this.propertyName = propertyName;
		this.code = code;
		this.tablePrefix = tablePrefix;
	}
	
	public String getName() {
		return RIFDataLoaderToolMessages.getMessage(propertyName);
	}
	
	public String getCode() {
		return code;
	}
	
	public String getPublishedTableName(final String coreDataSetName) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(tablePrefix);
		
		String modifiedCoreDataSetName
			= coreDataSetName.toLowerCase().replaceAll(" ", "_");
		buffer.append(modifiedCoreDataSetName);
		return buffer.toString();
	}
	
	private boolean matchesName(final String schemaAreaName) {
		Collator collator = GENERIC_MESSAGES.getCollator();
		String name = getName();
		
		return collator.equals(name, schemaAreaName);
	}

	private boolean matchesCode(final String candidateCode) {
		Collator collator = GENERIC_MESSAGES.getCollator();
		
		return collator.equals(code, candidateCode);
	}
	
	public static ArrayList<RIFSchemaArea> getAllSchemaAreas() {
		
		ArrayList<RIFSchemaArea> rifSchemaAreas 
			= new ArrayList<RIFSchemaArea>();
		rifSchemaAreas.add(COVARIATE_DATA);
		rifSchemaAreas.add(HEALTH_CODE_DATA);
		rifSchemaAreas.add(HEALTH_THEMES);
		rifSchemaAreas.add(HEALTH_NUMERATOR_DATA);
		rifSchemaAreas.add(POPULATION_DENOMINATOR_DATA);
		
		return rifSchemaAreas;
	}
	
	public static String[] getAllSchemaNames() {	
		
		ArrayList<String> rifSchemaAreaNames 
			= new ArrayList<String>();
		rifSchemaAreaNames.add(COVARIATE_DATA.getName());
		rifSchemaAreaNames.add(HEALTH_CODE_DATA.getName());
		rifSchemaAreaNames.add(HEALTH_THEMES.getName());
		rifSchemaAreaNames.add(HEALTH_NUMERATOR_DATA.getName());
		rifSchemaAreaNames.add(POPULATION_DENOMINATOR_DATA.getName());
		
		String[] results
			= rifSchemaAreaNames.toArray(new String[0]);
		return results;
	}
	
	
	public static RIFSchemaArea getSchemaAreaFromName(
		final String candidateName) {
		
		RIFSchemaArea result = null;
		
		if (COVARIATE_DATA.matchesName(candidateName)) {
			result = COVARIATE_DATA;
		}
		else if (HEALTH_CODE_DATA.matchesName(candidateName)) {
			result = HEALTH_CODE_DATA;
		}
		else if (HEALTH_THEMES.matchesName(candidateName)) {
			result = HEALTH_THEMES;
		}
		else if (HEALTH_NUMERATOR_DATA.matchesName(candidateName)) {
			result = HEALTH_NUMERATOR_DATA;
		}
		else if (POPULATION_DENOMINATOR_DATA.matchesName(candidateName)) {
			result = POPULATION_DENOMINATOR_DATA;
		}

		return result;		
	}

	
	public static RIFSchemaArea getSchemaAreaFromCode(
		final String candidateCode) {
		
		RIFSchemaArea result = null;
		
		if (COVARIATE_DATA.matchesCode(candidateCode)) {
			result = COVARIATE_DATA;
		}
		else if (HEALTH_CODE_DATA.matchesCode(candidateCode)) {
			result = HEALTH_CODE_DATA;
		}
		else if (HEALTH_THEMES.matchesCode(candidateCode)) {
			result = HEALTH_THEMES;
		}
		else if (HEALTH_NUMERATOR_DATA.matchesCode(candidateCode)) {
			result = HEALTH_NUMERATOR_DATA;
		}
		else if (POPULATION_DENOMINATOR_DATA.matchesCode(candidateCode)) {
			result = POPULATION_DENOMINATOR_DATA;
		}

		return result;		
	}

	
}


