package rifServices.fileFormats;


import rifServices.businessConceptLayer.HealthCode;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifGenericLibrary.fileFormats.XMLCommentInjector;
import rifGenericLibrary.fileFormats.XMLUtility;
import rifGenericLibrary.system.RIFServiceException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;



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
 * Copyright 2016 Imperial College London, developed by the Small Area
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

public final class HealthCodeListWriter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new health code list writer.
	 */
	public HealthCodeListWriter() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Write file.
	 *
	 * @param file the file
	 * @param healthCodes the health codes
	 * @throws RIFServiceException the RIF service exception
	 */
	public void writeFile(
		final File file, 
		final ArrayList<HealthCode> healthCodes) 
		throws RIFServiceException {

		try {
			FileOutputStream fileOutputStream 
				= new FileOutputStream(file);
			HealthCodeContentHandler healthCodeContentHandler
				= new HealthCodeContentHandler();
			XMLCommentInjector commentInjector = new XMLCommentInjector();
			healthCodeContentHandler.initialise(fileOutputStream, commentInjector);
			XMLUtility xmlUtility
				= healthCodeContentHandler.getXMLUtility();
			xmlUtility.writeStartXML();
			healthCodeContentHandler.writeXML(healthCodes);
			fileOutputStream.flush();
			fileOutputStream.close();
		}
		catch(IOException ioException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"io.error.problemWritingHealthCodes",
					file.getName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.XML_PROBLEM_WRITING_HEALTH_CODE_LIST, 
					errorMessage);
			throw rifServiceException;
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
