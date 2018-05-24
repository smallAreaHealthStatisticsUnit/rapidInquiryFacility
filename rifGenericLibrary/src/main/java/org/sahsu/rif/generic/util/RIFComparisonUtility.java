package org.sahsu.rif.generic.util;

import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.system.Messages;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
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

public final class RIFComparisonUtility {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public RIFComparisonUtility() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/*
	 * Meant to help determine whether two RIF objects
	 * are null or not
	 */

	
	
	public static boolean identifyNullityDifferences(
		final DisplayableListItemInterface rifObjectA,
		final DisplayableListItemInterface rifObjectB,
		final ArrayList<String> differences) {
		
		if (rifObjectA == rifObjectB) {
			//same nullity: either they're both non-null or
			//they're referring to the same objects
			return false;
		}
		
		//we are left with the cases:
		//a is NULL and b is not NULL
		//a is not NULL and b is NULL
		
		if (rifObjectA == null) {
			String difference
				= GENERIC_MESSAGES.getMessage(
					"differences.rifObjectsHaveDifferentNullity",
					rifObjectB.getDisplayName());
			differences.add(difference);
			return true;
		}
		else if (rifObjectB == null) {
			String difference
				= GENERIC_MESSAGES.getMessage(
					"differences.rifObjectsHaveDifferentNullity",
					rifObjectA.getDisplayName());
			differences.add(difference);
			return true;
		}
		else {
			return false;			
		}		
	}

	
	/**
	 * Assumes rifObjectA and rifObjectB are not null
	 * @param comparedFieldPropertyName
	 * @param rifObjectA
	 * @param fieldA
	 * @param rifObjectB
	 * @param fieldB
	 * @param differences
	 * @return
	 */
	public static boolean identifyFieldNullityDifferences(
		final String comparedFieldPropertyName,
		final DisplayableListItemInterface rifObjectA,
		final Object fieldA,
		final DisplayableListItemInterface rifObjectB,
		final Object fieldB,
		final ArrayList<String> differences) {
		
		if (fieldA == fieldB) {
			//same nullity: either they're both non-null or
			//they're both null
			return false;
		}
		
		//we are left with the cases:
		//a is NULL and b is not NULL
		//a is not NULL and b is NULL
		
		String fieldName
			= GENERIC_MESSAGES.getMessage(comparedFieldPropertyName);
		
		if (fieldA == null) {
			String difference
				= GENERIC_MESSAGES.getMessage(
					"differences.rifObjectFieldsHaveDifferentNullity",
					fieldName,
					rifObjectB.getDisplayName(),
					rifObjectA.getDisplayName());
			differences.add(difference);
			return true;
		}
		else if (fieldB == null) {
			String difference
				= GENERIC_MESSAGES.getMessage(
					"differences.rifObjectFieldsHaveDifferentNullity",
					fieldName,
					rifObjectA.getDisplayName(),
					rifObjectB.getDisplayName());
			differences.add(difference);
			return true;
		}
		else {
			return false;
		}
		
	}
	
	
	/**
	 * Assumes that ownerOfFieldA and ownerOfFieldB are both not null
	 * 
	 * @param ownerOfFieldValueA
	 * @param comparedFieldPropertyName
	 * @param fieldValueA
	 * @param ownerOfFieldValueB
	 * @param fieldValueB
	 * @param differences
	 */
	public static void identifyDifferences(
		final String comparedFieldPropertyName,
		final DisplayableListItemInterface ownerOfFieldValueA,
		final String fieldValueA,
		final DisplayableListItemInterface ownerOfFieldValueB,
		final String fieldValueB,
		final ArrayList<String> differences) {
		
		String fieldNameUsedInComparison
			= GENERIC_MESSAGES.getMessage(comparedFieldPropertyName);

		if ((fieldValueA != null) && (fieldValueB != null)) {
			
			Collator collator = GENERIC_MESSAGES.getCollator();
			if (!collator.equals(fieldValueA, fieldValueB)) {
				
				//eg: a.x = "5", b.x="7"
				String difference
					= GENERIC_MESSAGES.getMessage(
						"differences.fieldsDiffer",
						fieldNameUsedInComparison,
						ownerOfFieldValueA.getDisplayName(),
						fieldValueA,
						ownerOfFieldValueB.getDisplayName(),
						fieldValueB);
				differences.add(difference);				
			}	

		}
		else if ((fieldValueA == null) && (fieldValueB != null)) {

			//eg: a.x = null, b.x="7"

			String difference
				= GENERIC_MESSAGES.getMessage(
					"differences.oneFieldIsNull",
					fieldNameUsedInComparison,
					ownerOfFieldValueB.getDisplayName(),
					fieldValueB,
					ownerOfFieldValueA.getDisplayName());
			differences.add(difference);
		}
		else if ((fieldValueB == null) && (fieldValueA != null)) {
			
			//eg: a.x = "5", b.x=null

			String difference
				= GENERIC_MESSAGES.getMessage(
					"differences.oneFieldIsNull",
					fieldNameUsedInComparison,
					ownerOfFieldValueA.getDisplayName(),
					fieldValueA,
					ownerOfFieldValueB.getDisplayName());
			differences.add(difference);			
		}
		
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
