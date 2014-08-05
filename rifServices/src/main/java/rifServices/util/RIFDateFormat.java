
package rifServices.util;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * This class helps provide synchronous access to DateFormat objects by multiple threads
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
 * @version
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


public class RIFDateFormat {

// ==========================================
// Section Constants
// ==========================================
	
// ==========================================
// Section Properties
// ==========================================
	/** The rif date format. */
	static private RIFDateFormat rifDateFormat 	
		= new RIFDateFormat();
	
	/** The normal time stamp. */
	private final SimpleDateFormat normalTimeStamp 
		= new SimpleDateFormat("dd-mm-yyyy HH:mm:ss");
	
	/** The file date stamp. */
	private final SimpleDateFormat fileDateStamp 
		= new SimpleDateFormat("dd-MMM-yy");
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new RIF date format.
     */
	private RIFDateFormat() {

    }

	/**
	 * Gets the RIF date format.
	 *
	 * @return the RIF date format
	 */
	static public RIFDateFormat getRIFDateFormat() {
		
		return rifDateFormat;
	}
	
// ==========================================
// Section Accessors and Mutators
// ==========================================

	/**
	 * Gets the file time stamp.
	 *
	 * @param _date the _date
	 * @return the file time stamp
	 */
	public String getFileTimeStamp(
		final Date _date) {

		if (_date == null) {
			return null;
		}
		
		//Safe copy
		Date date = new Date(_date.getTime());
		
		synchronized (fileDateStamp) {
			return fileDateStamp.format(date);
		}
	}
	
	/**
	 * Gets the time phrase.
	 *
	 * @param _date the _date
	 * @return the time phrase
	 */
	public String getTimePhrase(
		final Date _date) {

		if (_date == null) {
			return null;
		}
		
		//Safe copy
		Date date = new Date(_date.getTime());
		
		synchronized (normalTimeStamp) {
			return normalTimeStamp.format(date);
		}
	}
	
	/**
	 * Gets the time from phrase.
	 *
	 * @param dateValue the date value
	 * @return the time from phrase
	 */
	public Date getTimeFromPhrase(
		final String dateValue){
		
		synchronized (normalTimeStamp) {
			Date result = null;
			try {
				result = normalTimeStamp.parse(dateValue);			
			}
			catch(Exception exception) {
				/**
				* @TODO
				*/
			}
			return result;
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
