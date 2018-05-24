
package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.Arrays;

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


public enum CalculationMethodPrior {
	
	/** The standard deviation. */
	STANDARD_DEVIATION("1", "calculationMethod.standardDeviation"),
    
    /** The precision. */
    PRECISION("2", "calculationMethod.categoricalOrdinal.label");
		
	/** The identifier. */
	private String identifier;
	
	/** The message property. */
	private String messageProperty;
	
	/**
	 * Instantiates a new calculation method prior.
	 *
	 * @param identifier the identifier
	 * @param messageProperty the message property
	 */
	CalculationMethodPrior(
		final String identifier, 
		final String messageProperty) {
		
		this.identifier = identifier;
		this.messageProperty = messageProperty;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		
		String name
			= RIFServiceMessages.getMessage(messageProperty);
		return name;
	}
	
	/**
	 * Gets the identifier.
	 *
	 * @return the identifier
	 */
	public String getIdentifier() {
		
		return identifier;
	}
	
	/**
	 * Gets the prior list.
	 *
	 * @return the prior list
	 */
	static public String[] getPriorList() {
		
		String[] priorList = new String[2];
		priorList[0] = PRECISION.getName();		
		priorList[1] = STANDARD_DEVIATION.getName();
		Arrays.sort(priorList);
		
		return priorList;
	}
	
	/**
	 * Gets the calculation method prior.
	 *
	 * @param priorName the prior name
	 * @return the calculation method prior
	 */
	public static CalculationMethodPrior getCalculationMethodPrior(
		final String priorName) {
		
		Collator collator = Collator.getInstance();
		
		if (collator.equals(priorName, PRECISION.getName())) {
			return PRECISION;
		}
		else if (collator.equals(priorName, STANDARD_DEVIATION.getName())) {
			return STANDARD_DEVIATION;
		}
		else {
			return null;
		}
	}
}
