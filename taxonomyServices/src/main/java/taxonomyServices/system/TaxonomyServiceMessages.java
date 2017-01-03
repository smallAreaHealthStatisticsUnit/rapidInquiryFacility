package taxonomyServices.system;

import java.text.MessageFormat;
import java.util.ResourceBundle;



/**
 * A class that retrieves messages that may appear in reports or exceptions that
 * are created by the rif service middleware.  The class is also used to retrieve
 * labels that clients of the rif services library could use to label user
 * interface components such as record and field names.
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

public final class TaxonomyServiceMessages {

    // ==========================================
    // Section Constants
    // ==========================================
	
	/** The Constant resourceBundle. */
    public static final ResourceBundle resourceBundle;

    // ==========================================
    // Section Properties
    // ==========================================

    // ==========================================
    // Section Construction
    // ==========================================

    static {
        resourceBundle = ResourceBundle.getBundle("TaxonomyServiceMessages");
    }

    // ==========================================
    // Section Accessors
    // ==========================================

    /**
     * Gets the message.
     *
     * @param key the key
     * @return the message
     */
    public static String getMessage(
    	final String key) {

    	if (resourceBundle != null) {
            return (resourceBundle.getString(key));
        }
        return key;
    }

    /**
     * Gets the message.
     *
     * @param key the key
     * @param parameter0 the parameter0
     * @return the message
     */
    static public String getMessage(
		final String key,
        final String parameter0) {

        String[] parameters = new String[1];
        parameters[0] = parameter0;

        return fillInTheBlanks(key,
        	parameters);
    }

    /**
     * Gets the message.
     *
     * @param key the key
     * @param parameter0 the parameter0
     * @param parameter1 the parameter1
     * @return the message
     */
    static public String getMessage(
		final String key,
        final String parameter0,
        final String parameter1) {

        String[] parameters = new String[2];
        parameters[0] = parameter0;
        parameters[1] = parameter1;

        return fillInTheBlanks(key,
        	parameters);
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
    static public String getMessage(
		final String key,
        final String parameter0,
        final String parameter1,
        final String parameter2) {

        String[] parameters = new String[3];
        parameters[0] = parameter0;
        parameters[1] = parameter1;
        parameters[2] = parameter2;

        return fillInTheBlanks(key,
        	parameters);
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
    static public String getMessage(
		final String key,
        final String parameter0,
        final String parameter1,
        final String parameter2,
        final String parameter3) {

        String[] parameters = new String[4];
        parameters[0] = parameter0;
        parameters[1] = parameter1;
        parameters[2] = parameter2;
        parameters[3] = parameter3;
        return fillInTheBlanks(key,
        	parameters);

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
    static public String getMessage(
		final String key,
        final String parameter0,
        final String parameter1,
        final String parameter2,
        final String parameter3,
        final String parameter4) {

        String[] parameters = new String[5];
        parameters[0] = parameter0;
        parameters[1] = parameter1;
        parameters[2] = parameter2;
        parameters[3] = parameter3;
        parameters[4] = parameter4;
        return fillInTheBlanks(key,
        	parameters);

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
     * @param parameter3 the parameter5
     * @return the message
     */
    static public String getMessage(
		final String key,
        final String parameter0,
        final String parameter1,
        final String parameter2,
        final String parameter3,
        final String parameter4,
        final String parameter5) {

        String[] parameters = new String[6];
        parameters[0] = parameter0;
        parameters[1] = parameter1;
        parameters[2] = parameter2;
        parameters[3] = parameter3;
        parameters[4] = parameter4;
        parameters[5] = parameter5;
        return fillInTheBlanks(key,
        	parameters);
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
     * @param parameter3 the parameter5
     * @param parameter3 the parameter6
     * @return the message
     */
    static public String getMessage(
		final String key,
        final String parameter0,
        final String parameter1,
        final String parameter2,
        final String parameter3,
        final String parameter4,
        final String parameter5,
        final String parameter6) {

        String[] parameters = new String[7];
        parameters[0] = parameter0;
        parameters[1] = parameter1;
        parameters[2] = parameter2;
        parameters[3] = parameter3;
        parameters[4] = parameter4;
        parameters[5] = parameter5;
        parameters[6] = parameter6;
        return fillInTheBlanks(key,
        	parameters);
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
     * @param parameter3 the parameter5
     * @param parameter3 the parameter6
     * @param parameter3 the parameter7
     * @return the message
     */
    static public String getMessage(
		final String key,
        final String parameter0,
        final String parameter1,
        final String parameter2,
        final String parameter3,
        final String parameter4,
        final String parameter5,
        final String parameter6,        
        final String parameter7) {

        String[] parameters = new String[8];
        parameters[0] = parameter0;
        parameters[1] = parameter1;
        parameters[2] = parameter2;
        parameters[3] = parameter3;
        parameters[4] = parameter4;
        parameters[5] = parameter5;
        parameters[6] = parameter6;
        parameters[7] = parameter7;
        
        return fillInTheBlanks(key,
        	parameters);
    }
    
    
    /**
     * Fill in the blanks.
     *
     * @param key the key
     * @param parameters the parameters
     * @return the string
     */
    static private String fillInTheBlanks(
		final String key,
        final String[] parameters) {

        String messageWithBlanks = resourceBundle.getString(key);

        MessageFormat messageFormat
                = new MessageFormat(messageWithBlanks);
        String messageWithoutBlanks
                = messageFormat.format(parameters);
        return messageWithoutBlanks;
    }

    public static ResourceBundle getResourceBundle() {

    	return resourceBundle;
    }

    // ==========================================
    // Section Mutators
    // ==========================================

    /*
    public static String getRIFServicesClassRootLocation() {
    	String classPath = System.getProperty("java.class.path");
    	
    	//we will get every directory used in the classpath.  Now we want to 
    	//find the directory that is the start of where the class files for the
    	//rifServices project are located.  This location can be used to find
    	//other resource files (eg: text and XML files).
		String[] classPathEntries
			= classPath.split(File.pathSeparator);
		StringBuilder pathToFind = new StringBuilder();
		pathToFind.append("rapidInquiryFacility");
		pathToFind.append(File.separator);
		pathToFind.append("rifServices");
		pathToFind.append(File.separator);
		pathToFind.append("target");
		pathToFind.append(File.separator);
		pathToFind.append("classes");
    	
		String targetClassesEntry = null;
    	for (String classPathEntry : classPathEntries) {
    		if (classPathEntry.endsWith(pathToFind.toString())) {
    			targetClassesEntry = classPathEntry;
    			break;
    		}
    	}
    	
    	return targetClassesEntry;
    	
    }
    */
    // ==========================================
    // Section Validation
    // ==========================================

    // ==========================================
    // Section Errors
    // ==========================================

    // ==========================================
    // Section Interfaces
    // ==========================================

    // ==========================================
    // Section Overload
    // ==========================================

}
