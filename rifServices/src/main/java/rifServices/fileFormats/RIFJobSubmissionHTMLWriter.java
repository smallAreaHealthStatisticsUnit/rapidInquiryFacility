
package rifServices.fileFormats;

import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


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


public final class RIFJobSubmissionHTMLWriter {

	

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The rif job submission content handler. */
	private RIFJobSubmissionContentHandler rifJobSubmissionContentHandler;
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new RIF job submission html writer.
     */
	public RIFJobSubmissionHTMLWriter() {
		
		rifJobSubmissionContentHandler = new RIFJobSubmissionContentHandler();
    }

    // ==========================================
    // Section Accessors and Mutators
    // ==========================================
    
    /**
     * Write job submission.
     *
     * @param rifJobSubmission the rif job submission
     * @return the string
     * @throws RIFServiceException the RIF service exception
     */
    public String writeJobSubmission(
    	final RIFStudySubmission rifJobSubmission) 
    	throws RIFServiceException {    
    	
    	try {
    		ByteArrayOutputStream byteArrayOutputStream
				= new ByteArrayOutputStream();
    		rifJobSubmissionContentHandler.initialise(byteArrayOutputStream);
    		rifJobSubmissionContentHandler.writeHTML(rifJobSubmission);
    		String htmlReport 
				= new String(byteArrayOutputStream.toByteArray(), "UTF-8");
    		return htmlReport;    		
    	}
    	catch(Exception exception) {
    		exception.printStackTrace(System.out);
    		String errorMessage
    			= RIFServiceMessages.getMessage(
    				"io.error.problemGeneratingReport",
    				rifJobSubmission.getDisplayName());
    		RIFServiceException rifServiceException
    			= new RIFServiceException(
    				RIFServiceError.FILE_WRITE_PROBLEM,
    				errorMessage);
    		throw rifServiceException;
    	}
    }
    
    /**
     * Write file.
     *
     * @param rifJobSubmission the rif job submission
     * @param outputFile the output file
     * @throws RIFServiceException the RIF service exception
     */
    public void writeFile(
    	final RIFStudySubmission rifJobSubmission, 
    	final File outputFile) 
    	throws RIFServiceException {

    	try {
        	FileOutputStream fileOutputStream
        		= new FileOutputStream(outputFile); 
        	rifJobSubmissionContentHandler.initialise(fileOutputStream);
        	rifJobSubmissionContentHandler.writeHTML(rifJobSubmission);
    	}  	
		catch(IOException exception) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"io.error.problemWritingFile",
					outputFile.getName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.FILE_WRITE_PROBLEM, 
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
