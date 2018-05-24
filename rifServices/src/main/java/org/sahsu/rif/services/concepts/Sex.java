/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.services.system.RIFServiceMessages;

/**
*
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
public enum Sex {
	
	/** Note: the identifiers are the numbers used to identify sex values in the RIF database. */
	MALES(1, "sex.males.label"),
	
	/** The females. */
	FEMALES(2, "sex.females.label"),
	
	/** The both. */
	BOTH(3, "sex.both.label");
	
	/** the numeric value associated with each sex **/
	private int code;
	
	/** The property name. */
	private final String propertyName;
	
	/**
	 * Instantiates a new sex.
	 *
	 * @param propertyName the property name
	 */
	Sex(
		final int code, 
		final String propertyName) {

		this.code = code;
		this.propertyName = propertyName;
	}
		
	public int getCode() {
		return code;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {

		String displayName = null;
		
		if (this == MALES) {
			displayName 
				= RIFServiceMessages.getMessage(propertyName);
		}
		else if (this == FEMALES) {
			displayName 
				= RIFServiceMessages.getMessage("sex.females.label");			
		}
		else if (this == BOTH) {
			displayName 
				= RIFServiceMessages.getMessage("sex.both.label");			
		}
		else {
			assert false;
		}
		
		return displayName;
	}
	
	/**
	 * Gets the sex from name.
	 *
	 * @param name the name
	 * @return the sex from name
	 */
	static public Sex getSexFromName(
		final String name) {

		if (name.equalsIgnoreCase(MALES.getName()) == true) {
			return MALES;
		}
		else if (name.equalsIgnoreCase(FEMALES.getName()) == true) {
			return FEMALES;
		}
		else if (name.equalsIgnoreCase(BOTH.getName()) == true) {
			return BOTH;
		}
		else {
			assert false;
			return null;
		}
	}
	
	/**
	 * Gets the sex from the numeric code.
	 *
	 * @param code the name
	 * @return the sex from name
	 */
	static public Sex getSexFromCode(
		final int code) {

		if (code == MALES.getCode()) {
			return MALES;			
		}
		else if (code == FEMALES.getCode()) {
			return FEMALES;			
		}
		else if (code == BOTH.getCode()) {
			return BOTH;
		}
		else {
			assert false;
			return null;
		}
	}
	
	
	/**
	 * Gets the sex name list.
	 *
	 * @param sexes the sexes
	 * @return the sex name list
	 */
	static public ArrayList<String> getSexNameList(
		final ArrayList<Sex> sexes) {

		ArrayList<String> results = new ArrayList<String>();
		for (Sex sex : sexes) {
			results.add(sex.getName());
		}
		
		return results;		
	}
}
