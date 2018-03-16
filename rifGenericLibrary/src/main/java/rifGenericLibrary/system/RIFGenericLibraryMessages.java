package rifGenericLibrary.system;

import java.text.Collator;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

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
 * @deprecated Use an instance of {@link Messages} instead
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
 * (8)            Section Accessors
 * (9)            Section Mutators
 * (6)            Section Validation
 * (7)            Section Errors
 * (5)            Section Interfaces
 * (4)            Section Overload
 *
*/

@Deprecated
public final class RIFGenericLibraryMessages {
	
	// ==========================================
    // Section Constants
    // ==========================================

    private static final Messages MESSAGES =
				    new Messages(ResourceBundle.getBundle("RIFGenericLibraryMessages"));
	
	// ==========================================
    // Section Accessors
    // ==========================================
    
    public static String getTimeStampForFileName(final Date _date) {
        	
     	  return MESSAGES.getTimeStampForFileName(_date);
    }   

    /**
     * Gets the time phrase.
     *
     * @param _date the _date
     * @return the time phrase
     */
    public static String getTimePhrase(final Date _date) {

    	return MESSAGES.getTimePhrase(_date);
    }
        
  
    /**
     * Gets the date.
     *
     * @param datePhrase the date phrase
     * @return the date
     */
    public static Date getDate(final String datePhrase) {
    	
    	return MESSAGES.getDate(datePhrase);
    }

    /**
     * Gets the time.
     *
     * @param timePhrase the time phrase
     * @return the time
     */
    public static Date getTime(final String timePhrase) {

    	return MESSAGES.getTime(timePhrase);
    }
    
   /**
    * Gets the date phrase.
    *
    * @param _date the _date
    * @return the date phrase
    */
    public static String getDatePhrase(final Date _date) {

	   return MESSAGES.getDatePhrase(_date);
   }
   
   /**
    * Gets the locale.
    *
    * @return the locale
    */
   public static Locale getLocale() {

	   return MESSAGES.getLocale();
   }
 
   /**
     * Gets the collator.
     *
     * @return the collator
     */
    public static Collator getCollator() {
    	
	  return MESSAGES.getCollator();
   }

    /**
     * Gets the message.
     *
     * @param key the key
     * @return the message
     */
    public static String getMessage(final String key) {

        return MESSAGES.getMessage(key);
    }
	
    /**
     * Gets the message.
     *
     * @param key the key
     * @param parameter0 the parameter0
     * @return the message
     */
    static public String getMessage(final String key, final String parameter0) {
	
	    return MESSAGES.getMessage(key, parameter0);
    }

    /**
     * Gets the message.
     *
     * @param key the key
     * @param parameter0 the parameter0
     * @param parameter1 the parameter1
     * @return the message
     */
    static public String getMessage(final String key, final String parameter0,
        final String parameter1) {
	
	    return MESSAGES.getMessage(key, parameter0, parameter1);
    }

    /**
     * Gets the message.
     *
     * @param key the key
     * @param parameter0 the parameter0
     * @param parameter1 the parameter1
     * @param parameter2 the parameter2
     * @return the message
     */
    static public String getMessage(final String key, final String parameter0,
        final String parameter1, final String parameter2) {
	
	    return MESSAGES.getMessage(key, parameter0, parameter1, parameter2);
    }

    /**
     * Gets the message.
     *
     * @param key the key
     * @param parameter0 the parameter0
     * @param parameter1 the parameter1
     * @param parameter2 the parameter2
     * @param parameter3 the parameter3
     * @return the message
     */
    static public String getMessage(final String key, final String parameter0,
				    final String parameter1, final String parameter2,
				    final String parameter3) {
	
	    return MESSAGES.getMessage(key, parameter0, parameter1, parameter2,
					    parameter3);
    }

    /**
     * Gets the message.
     *
     * @param key the key
     * @param parameter0 the parameter0
     * @param parameter1 the parameter1
     * @param parameter2 the parameter2
     * @param parameter3 the parameter3
     * @param parameter3 the parameter4
     * @return the message
     */
    static public String getMessage(final String key, final String parameter0,
				    final String parameter1, final String parameter2, final String parameter3,
				    final String parameter4) {
	
	    return MESSAGES.getMessage(key, parameter0, parameter1, parameter2,
					    parameter3, parameter4);

    }
}
