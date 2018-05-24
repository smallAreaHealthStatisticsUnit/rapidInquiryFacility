package org.sahsu.rif.generic.fileformats;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.sahsu.rif.generic.system.Messages;

/**
 *
 *
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
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

public class XMLFileFilter extends FileFilter {
	
	// ==========================================
	// Section Constants
	// ==========================================
	private static final String XML_EXTENSION = "XML";
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public XMLFileFilter() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public static String createXMLFileName(final String fileName) {
		if (fileName.toUpperCase().endsWith(XML_EXTENSION) == false) {
			return fileName + "." + XML_EXTENSION.toLowerCase();
		}
		else {
			return fileName;
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

	public boolean accept(final File candidateFile) {
		
		if (candidateFile.isDirectory()) {
			return true;
		}
		
		String upperCaseFilePath
			= candidateFile.getAbsolutePath().toUpperCase();
		if (upperCaseFilePath.endsWith(XML_EXTENSION) == true) {
			return true;
		}
		
		return false;
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
		String dotExtension = "." + XML_EXTENSION;
		if (fileNamePath.toUpperCase().endsWith(dotExtension.toUpperCase())) {
			//File already ends in XML
			return file;
		}
		
		File revisedFile = new File(file.getAbsolutePath() + dotExtension.toLowerCase());
		return revisedFile;
	}
		
	public String getDescription() {
		String description
			= GENERIC_MESSAGES.getMessage(
				"io.xmlFileFilter.description");
		return description;
	}
	
}


