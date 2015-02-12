

package rifServices.fileFormats;

import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.RIFOutputOption;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.util.RIFDateFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


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


public class RIFZipFileWriter {
	

// ==========================================
// Section Constants
// ==========================================
	/** The Constant queryFolder. */
	private static final String queryFolder = "rif_query";
// ==========================================
// Section Properties
// ==========================================
	
	/** The other directories to include. */
	private ArrayList<File> otherDirectoriesToInclude;
	
	/** The rif job submission content handler. */
	private RIFJobSubmissionContentHandler rifJobSubmissionContentHandler;
	
	/** The source directory for rif output option. */
	private HashMap<RIFOutputOption, File> sourceDirectoryForRIFOutputOption;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new RIF zip file writer.
     */
	public RIFZipFileWriter() {
		rifJobSubmissionContentHandler = new RIFJobSubmissionContentHandler();
		sourceDirectoryForRIFOutputOption = new HashMap<RIFOutputOption, File>();
		otherDirectoriesToInclude = new ArrayList<File>();		
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    /**
     * Associate source directory.
     *
     * @param rifOutputOption the rif output option
     * @param sourceDirectory the source directory
     */
	public void associateSourceDirectory(
    	final RIFOutputOption rifOutputOption, 
    	final File sourceDirectory) {
    	
    	sourceDirectoryForRIFOutputOption.put(rifOutputOption, sourceDirectory);
    }
    
    /**
     * Adds the files to include directory.
     *
     * @param otherDirectoryToInclude the other directory to include
     */
    public void addFilesToIncludeDirectory(
    	final File otherDirectoryToInclude) {
 
    	otherDirectoriesToInclude.add(otherDirectoryToInclude);
    }
	
	/**
	 * Write zip file.
	 *
	 * @param zipFile the zip file
	 * @param rifJobSubmission the rif job submission
	 * @throws RIFServiceException the RIF service exception
	 */
	public void writeZipFile(
		final User user,
		final File zipFile, 
		final RIFStudySubmission rifJobSubmission) 
		throws RIFServiceException {

		try {
		
			ZipOutputStream zipOutputStream = null;
		
			File zipFileNameWithTimeStamp 
				= getSubmissionFileName(
					zipFile, 
					user, 
					"rifz");
			
			zipOutputStream 
				= new ZipOutputStream(new FileOutputStream(zipFileNameWithTimeStamp));

			XMLCommentInjector commentInjector = new XMLCommentInjector();
			rifJobSubmissionContentHandler.initialise(zipOutputStream, commentInjector);
			
			File rifQueryFile 
				= getSubmissionFileName(
					zipFile, 
					user, 
					"xml");

			
			//write the query file to a special directory.
			//this folder should only contain one file
			StringBuilder queryFileName = new StringBuilder();
			queryFileName.append(queryFolder);
			queryFileName.append(File.separator);
			queryFileName.append(rifQueryFile.getName());
			
			ZipEntry rifQueryFileNameZipEntry = new ZipEntry(queryFileName.toString());
			zipOutputStream.putNextEntry(rifQueryFileNameZipEntry);
			rifJobSubmissionContentHandler.writeXML(
				user, 
				rifJobSubmission);
			zipOutputStream.closeEntry();

			//Add subdirectories for each output option
			ArrayList<RIFOutputOption> selectedRIFOutputOptions
				= rifJobSubmission.getRIFOutputOptions();
			for (RIFOutputOption rifOutputOption : selectedRIFOutputOptions) {
				StringBuilder rifOutputOptionDirectoryName
					= new StringBuilder();
				rifOutputOptionDirectoryName.append(rifOutputOption.getDirectoryName());
				rifOutputOptionDirectoryName.append(File.separator);
				
				ZipEntry rifOutputOptionDirectoryEntry 
					= new ZipEntry(rifOutputOptionDirectoryName.toString());
				zipOutputStream.putNextEntry(rifOutputOptionDirectoryEntry);
			}
			
			zipOutputStream.close();
		}
		catch(IOException exception) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"io.error.problemWritingFile",
					zipFile.getName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.FILE_WRITE_PROBLEM, 
					errorMessage);
			throw rifServiceException;
		}		
	}
		
	/**
	 * Gets the submission file name.
	 *
	 * @param zipFile the zip file
	 * @param user the user
	 * @param fileExtension the file extension
	 * @return the submission file name
	 */
	private File getSubmissionFileName(
		final File zipFile, 
		final User user,
		final String fileExtension) {
		
		String absoluteFilePath = zipFile.getAbsolutePath();
		int fileExtensionDotIndex = absoluteFilePath.lastIndexOf(".");
		StringBuilder fileName = new StringBuilder();
		fileName.append(absoluteFilePath.substring(0, fileExtensionDotIndex));
		fileName.append("-");
		if (user != null) {
			fileName.append(user.getUserID());
			fileName.append("-");
		}
		
		RIFDateFormat rifDateFormat = RIFDateFormat.getRIFDateFormat();
		String timeStamp = rifDateFormat.getFileTimeStamp(new Date());
		if (timeStamp != null) {
			fileName.append(timeStamp);
		}
		
		fileName.append(".");
		fileName.append(fileExtension);
		
		return new File(fileName.toString());
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
