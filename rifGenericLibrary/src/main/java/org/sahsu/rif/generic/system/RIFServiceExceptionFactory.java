package org.sahsu.rif.generic.system;

/**
 *
 * Convenience factory to help manufacture common types of RIFServiceException.
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public class RIFServiceExceptionFactory {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public RIFServiceExceptionFactory() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public RIFServiceException createUnableToChangeDBCommitException() {
		String errorMessage
			= GENERIC_MESSAGES.getMessage("general.db.error.unableToSetCommit");
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.DB_UNABLE_TO_ADJUST_AUTO_COMMIT,
				errorMessage);
		return rifServiceException;
	}
	
	public RIFServiceException createFileExecutionProblemException(final String fileName) {
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"io.error.problemExecutingFile",
				fileName);
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.COMMAND_LINE_EXECUTION_PROBLEM,
				errorMessage);
		return rifServiceException;
	}	

	public RIFServiceException createRScriptException(
		final String rScriptExceptionTrace) {
		
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"R.error.RscriptExecution",
				rScriptExceptionTrace);
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.R_ERROR,
				errorMessage);
		return rifServiceException;
	}	
	
	public RIFServiceException createREngineException(
		final String commandLineExecutable) {
		
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"R.error.unableToCreateREngine",
				commandLineExecutable);
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.R_ERROR,
				errorMessage);
		return rifServiceException;
	}		
	
	public RIFServiceException createExtractException(
		final String dbExceptionTrace) {
		
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"db.error.extractexception",
				dbExceptionTrace);
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.DB_EXCEPTION,
				errorMessage);
		return rifServiceException;
	}	

	public RIFServiceException createFileReadingProblemException(
		final String fileName) {
		
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"io.error.problemReadingFile",
				fileName);
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.FILE_PARSING_PROBLEM,
				errorMessage);
		return rifServiceException;
	}	
	
	
	public RIFServiceException createFileReadingProblemException() {
		
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"io.error.generalProblemReadingFile");
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.FILE_PARSING_PROBLEM,
				errorMessage);
		return rifServiceException;
	}	
		
	public RIFServiceException createFileWritingProblemException(
		final String fileName) {

		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"io.error.problemWritingFile",
				fileName);
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.FILE_WRITING_PROBLEM,
				errorMessage);
		return rifServiceException;
	}		

	
	public RIFServiceException createUnableToUseDBConnectionString(
		final String databaseConnectionString) {
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"db.error.unableToUseDatabaseConnectionString",
				databaseConnectionString);
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.DB_UNABLE_TO_USE_CONNECTION_STRING,
				errorMessage);
		return rifServiceException;
	}	
	
	public RIFServiceException createUnableToRegisterUser(final String userID) {
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"db.error.unableToRegisterUser",
				userID);
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.DB_UNABLE_REGISTER_USER,
				errorMessage);
		return rifServiceException;
	}		

	
	public RIFServiceException createUnableToDeregisterUser(final String userID) {
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"db.error.unableToDeregisterUser",
				userID);
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.DB_UNABLE_DEREGISTER_USER,
				errorMessage);
		return rifServiceException;
	}		
	
	public RIFServiceException createUnableLoadDBDriver() {
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"db.error.unableToLoadDatabaseDriver");
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.DB_UNABLE_TO_LOAD_DRIVER,
				errorMessage);
		return rifServiceException;
	}		
	
	public RIFServiceException createNonExistentFile(final String fileName) {
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"io.error.nonExistentFile",
				fileName);
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.NON_EXISTENT_FILE,
				errorMessage);
		return rifServiceException;
	}		
	
	public RIFServiceException createNonExistentParameter(final String parameterName) {
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"parameter.error.nonExistentParameter",
				parameterName);
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.NON_EXISTENT_FILE,
				errorMessage);
		return rifServiceException;
	}		

	public RIFServiceException createNonExistentTaxonomyTerm(
		final String taxonomyServiceIdentifier,
		final String taxonomyTermLabel) {
		
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"taxonomyService.error.nonExistentTerm",
				taxonomyServiceIdentifier,
				taxonomyTermLabel);
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.NON_EXISTENT_TAXONOMY_TERM,
				errorMessage);
		return rifServiceException;
	}			

	public RIFServiceException createProblemCreatingHTML() {
			
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"io.error.problemGeneratingHTML");
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFGenericLibraryError.FILE_PARSING_PROBLEM,
				errorMessage);
		return rifServiceException;
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
