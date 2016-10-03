

package rifServices.fileFormats;

import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.RIFOutputOption;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.fileFormats.XMLCommentInjector;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.util.RIFDateFormat;

import java.io.*;
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


public final class RIFZipFileWriter {
	

// ==========================================
// Section Constants
// ==========================================
	/** The Constant queryFolder. */
	private static final String SUBMISSION_DIRECTORY = "submission";
	
// ==========================================
// Section Properties
// ==========================================
	
	private File extraDirectoryForExtractFiles;
	
	private ArrayList<File> dataAccessPolicyFiles;
	
	/** The other directories to include. */
	private ArrayList<File> otherDirectoriesToInclude;
	
	/** The rif job submission content handler. */
	private RIFStudySubmissionContentHandler rifStudySubmissionContentHandler;
	
	/** The source directory for rif output option. */
	private HashMap<RIFOutputOption, ArrayList<File>> filesForRIFOutputOption;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new RIF zip file writer.
     */
	public RIFZipFileWriter() {
		rifStudySubmissionContentHandler = new RIFStudySubmissionContentHandler();
		filesForRIFOutputOption = new HashMap<RIFOutputOption, ArrayList<File>>();
		otherDirectoriesToInclude = new ArrayList<File>();		
		dataAccessPolicyFiles = new ArrayList<File>();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================

	public void setExtraDirectoryForExtractFiles(final File extraDirectoryForExtractFiles) {
		this.extraDirectoryForExtractFiles = extraDirectoryForExtractFiles;
	}
		
	public void addDataAccessPolicy(final File dataAccessPolicyFile) {
		dataAccessPolicyFiles.add(dataAccessPolicyFile);
	}
	
    /**
     * Associate source directory.
     *
     * @param rifOutputOption the rif output option
     * @param sourceDirectory the source directory
     */
	public void addOutputFileToInclude(
    	final RIFOutputOption rifOutputOption, 
    	final File fileToInclude) {
    	
		ArrayList<File> filesForOption
			= filesForRIFOutputOption.get(rifOutputOption);
		if (filesForOption == null) {
			filesForOption = new ArrayList<File>();
			filesForRIFOutputOption.put(rifOutputOption, filesForOption);			
		}
		filesForOption.add(fileToInclude);
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
	 * @param rifStudySubmission the rif job submission
	 * @throws RIFServiceException the RIF service exception
	 */
	public void writeZipFile(
		final User user,
		final File zipFile, 
		final RIFStudySubmission rifStudySubmission) 
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
			rifStudySubmissionContentHandler.initialise(zipOutputStream, commentInjector);
			
			File rifQueryFile 
				= getSubmissionFileName(
					zipFile, 
					user, 
					"xml");
			
			//write the query file to a special directory.
			//this folder should only contain one file
			StringBuilder queryFileName = new StringBuilder();
			queryFileName.append(SUBMISSION_DIRECTORY);
			queryFileName.append(File.separator);
			queryFileName.append(rifQueryFile.getName());
			
			ZipEntry rifQueryFileNameZipEntry = new ZipEntry(queryFileName.toString());
			zipOutputStream.putNextEntry(rifQueryFileNameZipEntry);
			rifStudySubmissionContentHandler.writeXML(
				user, 
				rifStudySubmission);
			zipOutputStream.closeEntry();

			//Add subdirectories for each output option
			ArrayList<RIFOutputOption> selectedRIFOutputOptions
				= rifStudySubmission.getRIFOutputOptions();
			for (RIFOutputOption rifOutputOption : selectedRIFOutputOptions) {
				StringBuilder rifOutputOptionDirectoryName
					= new StringBuilder();
				rifOutputOptionDirectoryName.append(rifOutputOption.getDirectoryName());
				rifOutputOptionDirectoryName.append(File.separator);				
				
				ArrayList<File> filesForOption 
					= filesForRIFOutputOption.get(rifOutputOption);
				if (filesForOption != null) {
					
					for (File fileForOption : filesForOption) {
						addFileToInclude(
							rifOutputOption,
							fileForOption, 
							zipOutputStream);					
					}
				}

			}
			
			if (extraDirectoryForExtractFiles != null) {
				//If there are any extra files in the extra file directory
				//just add them to the root directory of the zip file
				
				if (extraDirectoryForExtractFiles.isDirectory()) {
					File[] extraFiles = extraDirectoryForExtractFiles.listFiles();				
					for (File extraFile : extraFiles) {						
						//specifying null means it will appear in the main directory
						//when user unzips it.
						addFileToInclude(
							null, 
							extraFile, 
							zipOutputStream);
					}
				}
			}
						
			zipOutputStream.close();
		}
		catch(IOException exception) {
			exception.printStackTrace(System.out);
    		RIFServiceExceptionFactory exceptionFactory
    			= new RIFServiceExceptionFactory();
    		throw exceptionFactory.createFileWritingProblemException(
    			zipFile.getAbsolutePath());
		}		
	}
	
	/*
	 * When rifOutputOption is null, then the routine just writes the file to 
	 * the main directory that appears when the user unzips a RIF submission file.
	 */
	public void addFileToInclude(
		final RIFOutputOption rifOutputOption,
		final File sourceFile,		
		ZipOutputStream zipOutputStream) 
		throws FileNotFoundException, 
		IOException {

		StringBuilder filePath = new StringBuilder();
		if (rifOutputOption != null) {			
			filePath.append(rifOutputOption.getDirectoryName());
			filePath.append(File.separator);
		}
		filePath.append(sourceFile.getName());
		
		File file = new File(sourceFile.getAbsolutePath());
		FileInputStream fileInputStream = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(filePath.toString());
		zipOutputStream.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fileInputStream.read(bytes)) >= 0) {
			zipOutputStream.write(bytes, 0, length);
		}

		zipOutputStream.closeEntry();
		fileInputStream.close();
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
