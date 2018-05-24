
package org.sahsu.rif.services.fileformats;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.system.RIFServiceError;
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


final public class HealthCodeListReader {


// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The health code content handler. */
	private HealthCodeContentHandler healthCodeContentHandler;
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new health code list reader.
     */
	public HealthCodeListReader() {
		
    	healthCodeContentHandler = new HealthCodeContentHandler();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    
	/**
	 * Read file.
	 *
	 * @param healthCodeListFile the health code list file
	 * @throws RIFServiceException the RIF service exception
	 */
	public void readFile(
		final File healthCodeListFile) 
		throws RIFServiceException {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(healthCodeListFile, healthCodeContentHandler);
		}
		catch(Exception exception) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"io.error.problemReadingHealthCodes",
					healthCodeListFile.getName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.XML_PROBLEM_READING_HEALTH_CODE_LIST,
					errorMessage);
			throw rifServiceException;
		}
	}
	
	/**
	 * Gets the health codes.
	 *
	 * @return the health codes
	 */
	public ArrayList<HealthCode> getHealthCodes() {
		
		return healthCodeContentHandler.getHealthCodes(); 
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
