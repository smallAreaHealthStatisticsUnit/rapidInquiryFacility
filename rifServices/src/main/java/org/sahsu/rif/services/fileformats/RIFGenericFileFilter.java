
package org.sahsu.rif.services.fileformats;

import java.io.File;
import java.util.Locale;

import javax.swing.filechooser.FileFilter;

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


public final class RIFGenericFileFilter 
	extends FileFilter {

// ==========================================
// Section Constants
// ==========================================
	/** The html. */
	static public RIFGenericFileFilter HTML 
		= new RIFGenericFileFilter("html", "io.html.description");
	
	/** The rifq. */
	static public RIFGenericFileFilter RIFQ 
		= new RIFGenericFileFilter("rifq", "io.rifq.description"); 
	
	/** The rifz. */
	static public RIFGenericFileFilter RIFZ
		= new RIFGenericFileFilter("rifz", "io.rifz.description");
	
// ==========================================
// Section Properties
// ==========================================
	/** The file extension. */
	private String fileExtension;
	
	/** The message property. */
	private String messageProperty;
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new RIF generic file filter.
     *
     * @param fileEnding the file ending
     * @param messageProperty the message property
     */
	public RIFGenericFileFilter(
		final String fileEnding, 
		final String messageProperty) {
		
		Locale locale = Locale.getDefault();
		fileExtension = "." + fileEnding.toLowerCase(locale);
		this.messageProperty = messageProperty;
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
	/**
	 * Gets the file name with extension.
	 *
	 * @param fileName the file name
	 * @return the file name with extension
	 */
	public String getFileNameWithExtension(
		final String fileName) {

		if (fileName == null) {
			return null;
		}
		
		String lowerCaseFileName = fileName.toLowerCase();
		int dotIndex
			= fileName.lastIndexOf(".");

		StringBuilder fileNameWithExtension = new StringBuilder();
		if (dotIndex == -1) {
			//filename doesn't have an extension yet
			fileNameWithExtension.append(".");
			fileNameWithExtension.append(lowerCaseFileName);
		}
		else {
			fileNameWithExtension.append(lowerCaseFileName.substring(0, dotIndex));
		}
		
		fileNameWithExtension.append(fileExtension);
		return fileNameWithExtension.toString();
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


	@Override
	public boolean accept(
		final File file) {
	
		if (file == null) {
			return false;
		}
		
		String fileName = file.getName();

		Locale locale = Locale.getDefault();
		fileName = fileName.toLowerCase(locale);
		
		if (fileName.endsWith(fileExtension) == true) {
			return true;
		}
		
		return false;		
	}
	

	@Override
	public String getDescription() {
		
		String description
			= RIFServiceMessages.getMessage(messageProperty);
		return description;
	}
}
