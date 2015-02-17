
package rifServices.fileFormats;


import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


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


public final class RIFZipFileReader {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The job submission content handler. */
	private RIFJobSubmissionContentHandler jobSubmissionContentHandler;
	
	/** The query folder. */
	private String queryFolder = "rif_query";
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new RIF zip file reader.
     */
	public RIFZipFileReader() {
		
		jobSubmissionContentHandler
			= new RIFJobSubmissionContentHandler();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    
	/**
	 * Read file.
	 *
	 * @param zipFile the zip file
	 * @throws RIFServiceException the RIF service exception
	 */
	public void readFile(
		final File zipFile) 
		throws RIFServiceException {		

		String currentFileName = "";
		String zipFileName = zipFile.getName();
		try {
			FileInputStream fileInputStream
				= new FileInputStream(zipFile);
			BufferedInputStream bufferedInputStream
				= new BufferedInputStream(fileInputStream, 2048);		
			ZipInputStream zipInputStream
				= new ZipInputStream(bufferedInputStream, Charset.forName("UTF-8"));

			ZipEntry queryFileZipEntry
				= findZipEntryForQueryFile(zipInputStream);
			
			currentFileName = queryFileZipEntry.getName();
			
			/**
			 * we're only interested in the first file, which will contain the
			 * query submission
			*/

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			
			saxParser.parse(zipInputStream, jobSubmissionContentHandler);
		}
		catch(ParserConfigurationException parserConfigurationException) {
			String errorMessage
				= RIFServiceMessages.getMessage("general.io.xmlFileParsingError",
					currentFileName,
					zipFileName);
			RIFServiceException exception 
				= new RIFServiceException(RIFServiceError.XML_FILE_PARSING_PROBLEM, errorMessage);
			throw exception;
		}
		catch(SAXException saxException) {
			String errorMessage
				= RIFServiceMessages.getMessage("io.error.problemParsingXMLFile",
					currentFileName,
					zipFileName);
			RIFServiceException exception 
				= new RIFServiceException(RIFServiceError.XML_FILE_PARSING_PROBLEM, errorMessage);
			throw exception;			
		}
		catch(IOException ioException) {
			String errorMessage
				= RIFServiceMessages.getMessage("io.error.problemReadingFile",
					zipFileName);
			RIFServiceException exception 
				= new RIFServiceException(RIFServiceError.FILE_READ_PROBLEM, errorMessage);
			throw exception;
		}
	}

	/**
	 * Find zip entry for query file.
	 *
	 * @param zipInputStream the zip input stream
	 * @return the zip entry
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private ZipEntry findZipEntryForQueryFile(
		final ZipInputStream zipInputStream) 
		throws IOException {
		
		ZipEntry currentZipEntry = zipInputStream.getNextEntry();
		while (currentZipEntry != null) {
			String currentZipEntryName = currentZipEntry.getName();
			if (currentZipEntryName.startsWith(queryFolder)) {
				return currentZipEntry;
			}
			currentZipEntry = zipInputStream.getNextEntry();			
		}
		
		return null;
	}

	
	/**
	 * Gets the job submission.
	 *
	 * @return the job submission
	 */
	public RIFStudySubmission getJobSubmission() {
		
		return jobSubmissionContentHandler.getRIFJobSubmission();
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
