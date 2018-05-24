
package org.sahsu.rif.services.concepts;

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


public enum CovariateType {
    
    /** The continuous variable. */
    CONTINUOUS_VARIABLE("covariate.type.continuousVariable.label"),
    
    /** The binary integer score. */
    BINARY_INTEGER_SCORE("covariate.type.binaryIntegerScore.label"),
	
	/** The ntile integer score. */
	NTILE_INTEGER_SCORE("covariate.type.ntileIntegerScore.label");
	
	/** The message property. */
	private String messageProperty;
	
	/**
	 * Instantiates a new covariate type.
	 *
	 * @param messageProperty the message property
	 */
	CovariateType(
		final String messageProperty) {
		
		this.messageProperty = messageProperty;
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
	 * Gets the type from name.
	 *
	 * @param name the name
	 * @return the type from name
	 */
	static public CovariateType getTypeFromName(
		final String name) {
		
		if (name.equalsIgnoreCase(CONTINUOUS_VARIABLE.getName()) == true) {
			return CONTINUOUS_VARIABLE;
		}
		else if (name.equalsIgnoreCase(BINARY_INTEGER_SCORE.getName()) == true) {
			return BINARY_INTEGER_SCORE;
		}
		else if (name.equalsIgnoreCase(NTILE_INTEGER_SCORE.getName()) == true) {
			return NTILE_INTEGER_SCORE;
		}
		else {
			assert false;
			return null;
		}
	}
}
