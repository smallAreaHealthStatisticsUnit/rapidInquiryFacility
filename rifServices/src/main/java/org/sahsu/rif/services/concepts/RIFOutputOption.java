
package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.system.RIFServiceMessages;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
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
 * @version
 */

public enum RIFOutputOption {
	
	/** The data. */
    DATA("rifOutputOption.data.label", "rifOutputOption.data.directoryName"),
    
    /** The maps. */
    MAPS("rifOutputOption.maps.label", "rifOutputOption.maps.directoryName"),
    
    /** The population holes. */
    POPULATION_HOLES("rifOutputOption.populationHoles.label", "rifOutputOption.populationHoles.label"),
    
    /** The ratios and rates. */
    RATIOS_AND_RATES("rifOutputOption.ratiosAndRates.label", "rifOutputOption.ratiosAndRates.directoryName");
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	/** The message property. */
	private String messageProperty;
	
	/** The property for directory name. */
	private String propertyForDirectoryName;
	
	/**
	 * Instantiates a new RIF output option.
	 *
	 * @param messageProperty the message property
	 * @param propertyForDirectoryName the property for directory name
	 */
	RIFOutputOption(
		final String messageProperty, 
		final String propertyForDirectoryName) {
		this.messageProperty = messageProperty;
		this.propertyForDirectoryName = propertyForDirectoryName;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		
		return RIFServiceMessages.getMessage(messageProperty);
	}
	
	/**
	 * Gets the directory name.
	 *
	 * @return the directory name
	 */
	public String getDirectoryName() {
		
		return RIFServiceMessages.getMessage(propertyForDirectoryName);		
	}
	
	/**
	 * Gets the option from phrase.
	 *
	 * @param phrase the phrase
	 * @return the option from phrase
	 */
	public static RIFOutputOption getOptionFromPhrase(
		final String phrase) {

		Collator collator = GENERIC_MESSAGES.getCollator();
		
		String dataPhrase
			= RIFServiceMessages.getMessage("rifOutputOption.data.label");
		String mapsPhrase
			= RIFServiceMessages.getMessage("rifOutputOption.maps.label");
		String populationHolesPhrase
			= RIFServiceMessages.getMessage("rifOutputOption.populationHoles.label");
		String ratiosAndRatesPhrase
			= RIFServiceMessages.getMessage("rifOutputOption.ratiosAndRates.label");
		
		if (collator.equals(phrase, dataPhrase)) {
			return DATA;
		}
		else if (collator.equals(phrase, mapsPhrase)) {
			return MAPS;
		}
		else if (collator.equals(phrase, populationHolesPhrase)) {
			return POPULATION_HOLES;
		}
		else if (collator.equals(phrase, ratiosAndRatesPhrase)) {
			return RATIOS_AND_RATES;
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Checks for identical contents.
	 *
	 * @param rifOutputOptionListA the rif output option list a
	 * @param rifOutputOptionListB the rif output option list b
	 * @return true, if successful
	 */
	public static boolean hasIdenticalContents(
		final ArrayList<RIFOutputOption> rifOutputOptionListA, 
		final ArrayList<RIFOutputOption> rifOutputOptionListB) {

		if (FieldValidationUtility.hasDifferentNullity(
			rifOutputOptionListA, 
			rifOutputOptionListB)) {
			//reject if one is null and the other is non-null
			return false;
		}			
		if (rifOutputOptionListA.size() != rifOutputOptionListB.size() ) {
			//reject if lists do not have the same size
			return false;
		}
			
		//create temporary sorted lists to enable item by item comparisons
		//in corresponding lists
		ArrayList<RIFOutputOption> rifOutputOptionsA
			= sortRIFOutputOptions(rifOutputOptionListA);
		ArrayList<RIFOutputOption> rifOutputOptionsB
			= sortRIFOutputOptions(rifOutputOptionListB);
			
		int numberOfRIFOutputOptions = rifOutputOptionsA.size();
		for (int i = 0; i < numberOfRIFOutputOptions; i++) {
			RIFOutputOption rifOutputOptionA
				= rifOutputOptionsA.get(i);
			
			RIFOutputOption rifOutputOptionB
				= rifOutputOptionsB.get(i);
			if (rifOutputOptionA != rifOutputOptionB) {
				return false;
			}
		}
		
		return true;		
	}
		
	/**
	 * Sort rif output options.
	 *
	 * @param rifOutputOptions the rif output options
	 * @return the array list
	 */
	private static ArrayList<RIFOutputOption> sortRIFOutputOptions(
		final ArrayList<RIFOutputOption> rifOutputOptions) {
	
		HashMap<String, RIFOutputOption> rifOutputOptionFromName
			= new HashMap<String, RIFOutputOption>();
		for (RIFOutputOption rifOutputOption : rifOutputOptions) {
			rifOutputOptionFromName.put(
				rifOutputOption.getName(),
				rifOutputOption);
		}
			
		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(rifOutputOptionFromName.keySet());
		
		//@TODO: how well doe this handle duplicates
		ArrayList<RIFOutputOption> results = new ArrayList<RIFOutputOption>();
		Collections.sort(keys);
		for (String key : keys) {
			results.add(rifOutputOptionFromName.get(key));
		}
			
		return results;
	}

	/**
	 * Gets the all available rif output options.
	 *
	 * @return the all available rif output options
	 */
	public static ArrayList<RIFOutputOption> getAllAvailableRIFOutputOptions() {
		
		ArrayList<RIFOutputOption> rifOutputOptions 
			= new ArrayList<RIFOutputOption>();
		rifOutputOptions.add(DATA);
		rifOutputOptions.add(MAPS);
		rifOutputOptions.add(POPULATION_HOLES);
		rifOutputOptions.add(RATIOS_AND_RATES);
		
		return rifOutputOptions;
	}
	
	/**
	 * Gets the all available rif output option names.
	 *
	 * @return the all available rif output option names
	 */
	public static ArrayList<String> getAllAvailableRIFOutputOptionNames() {
		
		ArrayList<String> rifOutputOptionNames 
			= new ArrayList<String>();
		rifOutputOptionNames.add(DATA.getName());
		rifOutputOptionNames.add(MAPS.getName());
		rifOutputOptionNames.add(POPULATION_HOLES.getName());
		rifOutputOptionNames.add(RATIOS_AND_RATES.getName());
		
		return rifOutputOptionNames;
	}
}
