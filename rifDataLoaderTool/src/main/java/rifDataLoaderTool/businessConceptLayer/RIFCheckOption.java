package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

import java.text.Collator;
import java.util.ArrayList;


/**
 * Describes data quality indicators that will be associated with table fields.
 * For example, <code>PERCENT_EMPTY</code> will cause the RIF to retain data 
 * quality statistics for how many field values of a 
 * {@link rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration}
 * are empty.  <code>PERCENT_EMPTY_PER_YEAR</code> will cause the RIF to make the 
 * percentages on a per-year basis.  Note that this second option will likely only 
 * be relevant for numerator and denominator data sets, which are guaranteed to 
 * have a year field present in the tables.
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

public enum RIFCheckOption {
	PERCENT_EMPTY_PER_YEAR(
		"percent_empty_per_year",
		"rifCheckOption.percentEmptyPerYear.label",
		"rifCheckOption.percentEmptyPerYear.description"),
	PERCENT_EMPTY(
		"percent_empty",
		"rifCheckOption.percentEmpty.label",
		"rifCheckOption.percentEmpty.description");
	
	
	private String code;
	private String nameProperty;
	private String descriptionProperty;
	
	private RIFCheckOption(
		final String code,
		final String nameProperty,
		final String descriptionProperty) {
		
		this.code = code;
		this.nameProperty = nameProperty;
		this.descriptionProperty = descriptionProperty;
	}
	
	public static RIFCheckOption getOptionFromCode(
		final String code) {
		
		Collator collator = RIFDataLoaderToolMessages.getCollator();
		if (collator.equals(code, "percent_empty")) {
			return RIFCheckOption.PERCENT_EMPTY;
		}
		if (collator.equals(code, "percent_empty_per_year")) {
			return RIFCheckOption.PERCENT_EMPTY_PER_YEAR;
		}
		else {
			//should never happen
			assert false;
			return null;
		}
		
	}
	
	public String getName() {
		String displayName
			= RIFDataLoaderToolMessages.getMessage(nameProperty);
		return displayName;
	}
	
	public String getDescription() {
		String displayName
			= RIFDataLoaderToolMessages.getMessage(descriptionProperty);
		return displayName;
	}
		
	public ArrayList<RIFCheckOption> getAllCheckOptions() {

		ArrayList<RIFCheckOption> allCheckOptions 
			= new ArrayList<RIFCheckOption>();
		allCheckOptions.add(PERCENT_EMPTY);
		allCheckOptions.add(PERCENT_EMPTY_PER_YEAR);
		
		return allCheckOptions;
	}
	
	public String[] getAllCheckOptionNames() {

		ArrayList<String> allCheckOptionNames 
			= new ArrayList<String>();
		allCheckOptionNames.add(PERCENT_EMPTY.getName());
		allCheckOptionNames.add(PERCENT_EMPTY_PER_YEAR.getName());
	
		String[] results
			= allCheckOptionNames.toArray(new String[0]);
		return results;
	}
	
	public String getCode() {
		return code;
	}
	
	/*
	 * Assume neither collection is null
	 */
	public static boolean hasIdenticalContents(
		final ArrayList<RIFCheckOption> rifCheckOptionsA,
		final ArrayList<RIFCheckOption> rifCheckOptionsB) {
		
		if (rifCheckOptionsA == rifCheckOptionsB) {
			return true;
		}
		
		if ((rifCheckOptionsA == null && rifCheckOptionsB != null) ||
			(rifCheckOptionsB != null && rifCheckOptionsB == null)) {
			
			return false;
		}
		
		int numberCheckOptionsA = rifCheckOptionsA.size();		
		if (numberCheckOptionsA != rifCheckOptionsB.size()) {
			return false;
		}
		
		
		for (int i = 0; i < numberCheckOptionsA; i++) {
			RIFCheckOption rifCheckOptionA = rifCheckOptionsA.get(i);
			RIFCheckOption rifCheckOptionB = rifCheckOptionsB.get(i);
			if (rifCheckOptionA != rifCheckOptionB) {
				return false;
			}
		}
						
		return true;
	}
	
}


