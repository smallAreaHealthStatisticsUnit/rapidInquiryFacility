
package org.sahsu.rif.services.fileformats;

import java.io.File;
import java.text.Collator;
import java.util.Locale;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.services.system.RIFServiceMessages;

/**
 *
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

public final class RIFZFileFilter 
	extends javax.swing.filechooser.FileFilter 
	implements java.io.FileFilter {

// ==========================================
// Section Constants
// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
// ==========================================
// Section Properties
// ==========================================
	/** The Constant RIFZ_EXTENSION. */
	private static final String RIFZ_EXTENSION = "RIFZ";
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new RIFZ file filter.
     */
	public RIFZFileFilter() {
		
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================

// ==========================================
// Section Errors and Validation
// ==========================================

// ==========================================
// Section Interfaces
// ==========================================
    
	/**
	 * Ensure file name ends with extension.
	 *
	 * @param selectedFile the selected file
	 * @return the file
	 */
	public static File ensureFileNameEndsWithExtension(
		final File selectedFile) {
   
		String currentFilePath = selectedFile.getAbsolutePath();
    	StringBuilder resultingPath = new StringBuilder();
    	resultingPath.append(currentFilePath);    	
    	int fileExtensionDotIndex
    		= currentFilePath.lastIndexOf(".");
    	
    	Locale locale = GENERIC_MESSAGES.getLocale();
    	String desiredExtension = "." + RIFZ_EXTENSION.toLowerCase(locale);
    	if (fileExtensionDotIndex == -1) {
    		//the file doesn't have a file extension so add one
    		resultingPath.append(".");
    		resultingPath.append(RIFZ_EXTENSION.toLowerCase()); 		
    	}
    	else {
    		String upperCasePath 
    			= currentFilePath.toUpperCase(GENERIC_MESSAGES.getLocale());
    		if (upperCasePath.endsWith(desiredExtension.toUpperCase(locale)) == false) {
    			resultingPath.append(desiredExtension);
    		} 		
    	}
    	
    	File resultFile = new File(resultingPath.toString());
    	return resultFile;
    }
    

	public boolean accept(
		final File pathName) {

		if (pathName.isDirectory() == true) {
			return true;
		}
		
		String upperCasePathName
			= pathName.getName().toUpperCase();
		int dotPosition
			= upperCasePathName.lastIndexOf(".");
		if (dotPosition == -1) {
			return false;
		}
		
		String currentExtension
			= upperCasePathName.substring(dotPosition+1);
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		if (collator.equals(RIFZ_EXTENSION, currentExtension) == true) {
			return true;
		}
		
		return false;
	}
	

	public String getDescription() {
		
		String rifZFileTypeDescription
			= RIFServiceMessages.getMessage("io.rifz.description");
		return rifZFileTypeDescription;
	}
	
// ==========================================
// Section Override
// ==========================================




}
