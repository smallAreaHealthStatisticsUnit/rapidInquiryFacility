
package org.sahsu.rif.services.concepts;

import org.sahsu.rif.generic.concepts.User;

import java.util.Date;
import java.util.ArrayList;


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


public final class RIFStudySubmissionResult {


// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	/** The rif service information. */
	private RIFServiceInformation rifServiceInformation;
	
	/** The user. */
	private User user;
	
	/** The error messages. */
	private ArrayList<String> errorMessages;
	
	/** The warning messages. */
	private ArrayList<String> warningMessages;
	
	/** The administrator messages. */
	private ArrayList<String> administratorMessages;
	
	/** The job submission file name. */
	private String jobSubmissionFileName;
	
	/** The investigation. */
	private Investigation investigation;
	
	/** The calculation methods. */
	private ArrayList<CalculationMethod> calculationMethods;

	/** The total records passed. */
	private int totalRecordsPassed;
	
	/** The total records failed. */
	private int totalRecordsFailed;
	
	/** The total records processed. */
	private int totalRecordsProcessed;

	//@TODO
	//put in here something that distinguishes execution time and 
	//job submission delay
	
	/** The job submission start date. */
	private Date jobSubmissionStartDate;
	
	/** The job submission end date. */
	private Date jobSubmissionEndDate;
	
	/** The original submission file name. */
	private String originalSubmissionFileName;
	
	/** The original submission user id. */
	private String originalSubmissionUserID;
    
    /** The original submission file creation date. */
    private String originalSubmissionFileCreationDate;

	/** The job passed. */
	private boolean jobPassed;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new RIF job submission result.
     */
	public RIFStudySubmissionResult() {
		errorMessages = new ArrayList<String>();
		warningMessages = new ArrayList<String>();
		administratorMessages = new ArrayList<String>();
		calculationMethods = new ArrayList<CalculationMethod>();
		
		originalSubmissionFileName = "";
		originalSubmissionUserID = "";
		originalSubmissionFileCreationDate = "";
		
		jobPassed = false;
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    /**
     * Sets the user.
     *
     * @param user the new user
     */
	public void setUser(
		final User user) {

		this.user = user;
	}
	
	/**
	 * Gets the user.
	 *
	 * @return the user
	 */
	public User getUser() {
		
		return user;
	}
	
	/**
	 * Sets the error messages.
	 *
	 * @param errorMessages the new error messages
	 */
	public void setErrorMessages(
		final ArrayList<String> errorMessages) {

		this.errorMessages = errorMessages;
	}
	
	/**
	 * Gets the error messages.
	 *
	 * @return the error messages
	 */
	public ArrayList<String> getErrorMessages() {
		
		return errorMessages;
	}
	
	/**
	 * Sets the warning messages.
	 *
	 * @param warningMessages the new warning messages
	 */
	public void setWarningMessages(
		final ArrayList<String> warningMessages) {

		this.warningMessages = warningMessages;
	}
	
	/**
	 * Gets the warning messages.
	 *
	 * @return the warning messages
	 */
	public ArrayList<String> getWarningMessages() {
		
		return warningMessages;
	}
	
	/**
	 * Sets the administrator messages.
	 *
	 * @param administratorMessages the new administrator messages
	 */
	public void setAdministratorMessages(
		final ArrayList<String> administratorMessages) {

		this.administratorMessages = administratorMessages;
	}
	
	/**
	 * Gets the administrator messages.
	 *
	 * @return the administrator messages
	 */
	public ArrayList<String> getAdministratorMessages() {
		
		return administratorMessages;
	}
		
	/**
	 * Sets the investigation.
	 *
	 * @param investigation the new investigation
	 */
	public void setInvestigation(
		final Investigation investigation) {

		this.investigation = investigation;	
	}
	
	/**
	 * Gets the investigation.
	 *
	 * @return the investigation
	 */
	public Investigation getInvestigation() {
		
		return investigation;
	}
	
	/**
	 * Sets the calculation methods.
	 *
	 * @param calculationMethods the new calculation methods
	 */
	public void setCalculationMethods(
		final ArrayList<CalculationMethod> calculationMethods) {

		this.calculationMethods = calculationMethods;
	}
	
	/**
	 * Gets the calculation methods.
	 *
	 * @return the calculation methods
	 */
	public ArrayList<CalculationMethod> getCalculationMethods() {
		
		return calculationMethods;
	}
	
	/**
	 * Sets the job submission start date.
	 *
	 * @param jobSubmissionStartDate the new job submission start date
	 */
	public void setJobSubmissionStartDate(
		final Date jobSubmissionStartDate) {

		this.jobSubmissionStartDate = jobSubmissionStartDate;
	}
	
	/**
	 * Gets the job submission start date.
	 *
	 * @return the job submission start date
	 */
	public Date getJobSubmissionStartDate() {
		
		return jobSubmissionStartDate;
	}
	
	/**
	 * Sets the job submission end date.
	 *
	 * @param jobSubmissionEndDate the new job submission end date
	 */
	public void setJobSubmissionEndDate(
		final Date jobSubmissionEndDate) {
		
		this.jobSubmissionEndDate = jobSubmissionEndDate;
	}
	
	/**
	 * Gets the job submission end date.
	 *
	 * @return the job submission end date
	 */
	public Date getJobSubmissionEndDate() {

		return jobSubmissionEndDate;
	}
	
	/**
	 * Gets the job submission file name.
	 *
	 * @return the job submission file name
	 */
	public String getJobSubmissionFileName() {
		
		return jobSubmissionFileName;
	}

	/**
	 * Sets the job submission file name.
	 *
	 * @param jobSubmissionFileName the new job submission file name
	 */
	public void setJobSubmissionFileName(
		final String jobSubmissionFileName) {

		this.jobSubmissionFileName = jobSubmissionFileName;
	}
	
	/**
	 * Job passed.
	 *
	 * @return true, if successful
	 */
	public boolean jobPassed() {
		
		return jobPassed;
	}
	
	/**
	 * Sets the job passed.
	 *
	 * @param jobPassed the new job passed
	 */
	public void setJobPassed(
		final boolean jobPassed) {
		
		this.jobPassed = jobPassed;
	}
	
	/**
	 * Gets the RIF service information.
	 *
	 * @return the RIF service information
	 */
	public RIFServiceInformation getRIFServiceInformation() {
		
		return rifServiceInformation;
	}

	/**
	 * Sets the RIF service information.
	 *
	 * @param rifServiceInformation the new RIF service information
	 */
	public void setRIFServiceInformation(
		final RIFServiceInformation rifServiceInformation) {

		this.rifServiceInformation = rifServiceInformation;
	}

	/**
	 * Gets the original submission file name.
	 *
	 * @return the original submission file name
	 */
	public String getOriginalSubmissionFileName() {
		
		return originalSubmissionFileName;
	}

	/**
	 * Sets the original submission file name.
	 *
	 * @param originalSubmissionFileName the new original submission file name
	 */
	public void setOriginalSubmissionFileName(
		final String originalSubmissionFileName) {

		this.originalSubmissionFileName = originalSubmissionFileName;
	}

	/**
	 * Gets the original submission user id.
	 *
	 * @return the original submission user id
	 */
	public String getOriginalSubmissionUserID() {
		
		return originalSubmissionUserID;
	}

	/**
	 * Sets the original submission user id.
	 *
	 * @param originalSubmissionUserID the new original submission user id
	 */
	public void setOriginalSubmissionUserID(
		final String originalSubmissionUserID) {

		this.originalSubmissionUserID = originalSubmissionUserID;
	}

	/**
	 * Gets the original submission file creation date.
	 *
	 * @return the original submission file creation date
	 */
	public String getOriginalSubmissionFileCreationDate() {
		
		return originalSubmissionFileCreationDate;
	}

	/**
	 * Sets the original submission file creation date.
	 *
	 * @param originalSubmissionFileCreationDate the new original submission file creation date
	 */
	public void setOriginalSubmissionFileCreationDate(
		final String originalSubmissionFileCreationDate) {

		this.originalSubmissionFileCreationDate = originalSubmissionFileCreationDate;
	}	
	
	/**
	 * Gets the total records passed.
	 *
	 * @return the total records passed
	 */
	public int getTotalRecordsPassed() {

		return totalRecordsPassed;
	}

	/**
	 * Sets the total records passed.
	 *
	 * @param totalRecordsPassed the new total records passed
	 */
	public void setTotalRecordsPassed(
		final int totalRecordsPassed) {

		this.totalRecordsPassed = totalRecordsPassed;
	}

	/**
	 * Gets the total records failed.
	 *
	 * @return the total records failed
	 */
	public int getTotalRecordsFailed() {
		
		return totalRecordsFailed;
	}

	/**
	 * Sets the total records failed.
	 *
	 * @param totalRecordsFailed the new total records failed
	 */
	public void setTotalRecordsFailed(
		final int totalRecordsFailed) {

		this.totalRecordsFailed = totalRecordsFailed;
	}

	/**
	 * Gets the total records processed.
	 *
	 * @return the total records processed
	 */
	public int getTotalRecordsProcessed() {
		
		return totalRecordsProcessed;
	}

	/**
	 * Sets the total records processed.
	 *
	 * @param totalRecordsProcessed the new total records processed
	 */
	public void setTotalRecordsProcessed(
		final int totalRecordsProcessed) {

		this.totalRecordsProcessed = totalRecordsProcessed;
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
