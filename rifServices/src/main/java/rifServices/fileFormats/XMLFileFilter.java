package rifServices.fileFormats;


import rifServices.system.RIFServiceMessages;

import java.io.File;



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
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public final class XMLFileFilter 
	extends javax.swing.filechooser.FileFilter 
	implements java.io.FileFilter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The xml extension. */
	private static String XML_EXTENSION = "XML";
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new XML file filter.
	 */
	public XMLFileFilter() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public boolean accept(
		final File file) {

		if (file.isDirectory() == true) {
			return true;
		}
					
		String fileName
			= file.getAbsolutePath().toUpperCase();
		return fileName.endsWith(XML_EXTENSION);
	}
	

	public String getDescription() {
		
		String description
			= RIFServiceMessages.getMessage("io.xml.description");
		return description;
	}
	
	/**
	 * Ensure file ends with xml.
	 *
	 * @param file the file
	 * @return the file
	 */
	public static File ensureFileEndsWithXML(
		final File file) {

		String fileNamePath = file.getAbsolutePath();
		int lastIndexOfDot
			= fileNamePath.lastIndexOf(".");
		StringBuilder buffer = new StringBuilder();
		
		if (lastIndexOfDot == -1) {
			//no dot found, therefore append one
			buffer.append(fileNamePath);
			buffer.append(".");
			buffer.append(XML_EXTENSION.toLowerCase());
		}
		else {
			buffer.append(fileNamePath.substring(0, lastIndexOfDot));
			buffer.append(XML_EXTENSION.toLowerCase());
		}
		
		File resultFile = new File(buffer.toString());
		return resultFile;		
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
