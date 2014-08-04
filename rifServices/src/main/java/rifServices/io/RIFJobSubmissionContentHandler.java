
package rifServices.io;

import rifServices.businessConceptLayer.CalculationMethod;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.Project;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.RIFOutputOption;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceMessages;
import rifServices.util.HTMLUtility;
import rifServices.util.XMLCommentInjector;
import rifServices.util.XMLUtility;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Date;


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


class RIFJobSubmissionContentHandler 
	extends AbstractRIFConceptContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================

	/** The current rif job submission. */
	private RIFStudySubmission currentRIFJobSubmission;
	
	/** The project content handler. */
	private ProjectContentHandler projectContentHandler;
	
	/** The disease mapping study content handler. */
	private DiseaseMappingStudyContentHandler diseaseMappingStudyContentHandler;
	
	/** The calculation method content handler. */
	private CalculationMethodContentHandler calculationMethodContentHandler; 
	
	/** The rif output option content handler. */
	private RIFOutputOptionContentHandler rifOutputOptionContentHandler;
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new RIF job submission content handler.
     */
	public RIFJobSubmissionContentHandler() {
		
    	setSingularRecordName("rif_job_submission");
    	projectContentHandler = new ProjectContentHandler();
    	diseaseMappingStudyContentHandler = new DiseaseMappingStudyContentHandler();
    	calculationMethodContentHandler = new CalculationMethodContentHandler();
    	rifOutputOptionContentHandler = new RIFOutputOptionContentHandler();
    	
    	ignoreXMLStartTag("submitted_by");
    	ignoreXMLStartTag("job_submission_date");
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
	
	/**
	 * Gets the RIF job submission.
	 *
	 * @return the RIF job submission
	 */
	public RIFStudySubmission getRIFJobSubmission() {
		
		return currentRIFJobSubmission;
	}
		
	
	/**
	 * Write html.
	 *
	 * @param rifJobSubmission the rif job submission
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeHTML(
		final RIFStudySubmission rifJobSubmission) 
		throws IOException {

		HTMLUtility htmlUtility = getHTMLUtility();
		
		htmlUtility.beginDocument();
				
    	DiseaseMappingStudy diseaseMappingStudy
    		= (DiseaseMappingStudy) rifJobSubmission.getStudy();
    	Project project = rifJobSubmission.getProject();
    	diseaseMappingStudyContentHandler.writeHTML(
    		1, 
    		diseaseMappingStudy,
    		project,
    		false);
    	    	
		ArrayList<CalculationMethod> calculationMethods
			= new ArrayList<CalculationMethod>();
		calculationMethodContentHandler.writeHTML(
			1,
			calculationMethods,
			false);
    	
    	ArrayList<RIFOutputOption> rifOutputOptions
    		= rifJobSubmission.getRIFOutputOptions();
    	rifOutputOptionContentHandler.writeHTML(
    		1, 
    		rifOutputOptions,
    		false);
		
		htmlUtility.endDocument();		
	}
	
    /**
     * Write xml.
     *
     * @param rifJobSubmission the rif job submission
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void writeXML(
    	final RIFStudySubmission rifJobSubmission) 
    	throws IOException {

    	XMLUtility xmlUtility = getXMLUtility();
		
		xmlUtility.writeStartXML();

		XMLCommentInjector commentInjector = getCommentInjector();		
		ArrayList<String> introductionComments
			= commentInjector.getIntroductionComments();
		for (String introductionComment : introductionComments) {
			xmlUtility.writeComment(introductionComment);			
		}
		
		String recordName = getSingularRecordName();		
		xmlUtility.writeRecordStartTag(recordName);
		
		User user = rifJobSubmission.getUser();
		xmlUtility.writeField(
			recordName, 
			"submitted_by", 
			user.getUserID());
		
		Date jobSubmissionTime
			= rifJobSubmission.getJobSubmissionTime();
		if (jobSubmissionTime == null) {
			xmlUtility.writeField(
				recordName,
				"job_submission_date", 
				"");
		}
		else {
			String jobSubmissionDatePhrase 
				= RIFServiceMessages.getTimePhrase(jobSubmissionTime);
			xmlUtility.writeField(
				recordName,
				"job_submission_date", 
				jobSubmissionDatePhrase);			
		}
		
		Project project = rifJobSubmission.getProject();
		projectContentHandler.writeXML(project);
		
		DiseaseMappingStudy diseaseMappingStudy
			= (DiseaseMappingStudy) rifJobSubmission.getStudy();
		diseaseMappingStudyContentHandler.writeXML(diseaseMappingStudy);
		calculationMethodContentHandler.writeXML(rifJobSubmission.getCalculationMethods());
		
		xmlUtility.writeRecordEndTag(recordName);
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
	/* (non-Javadoc)
	 * @see rifServices.io.AbstractRIFConceptContentHandler#initialise(java.io.OutputStream, rifServices.util.XMLCommentInjector)
	 */
    @Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
		projectContentHandler.initialise(outputStream, commentInjector);
		diseaseMappingStudyContentHandler.initialise(outputStream, commentInjector);
		calculationMethodContentHandler.initialise(outputStream, commentInjector);
		rifOutputOptionContentHandler.initialise(outputStream, commentInjector);
	}

	/* (non-Javadoc)
	 * @see rifServices.io.AbstractRIFConceptContentHandler#initialise(java.io.OutputStream)
	 */
	@Override
	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
		projectContentHandler.initialise(outputStream);
		diseaseMappingStudyContentHandler.initialise(outputStream);
		calculationMethodContentHandler.initialise(outputStream);
		rifOutputOptionContentHandler.initialise(outputStream);
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isSingularRecordName(qualifiedName)) {
			currentRIFJobSubmission = RIFStudySubmission.newInstance();
			activate();
		}
		else if (isDelegatedHandlerAssigned() == true) {
			AbstractRIFConceptContentHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.startElement(
				nameSpaceURI, 
				localName, 
				qualifiedName, 
				attributes);
		}
		else {			
			//determine if a delegate handler can be assigned to do future processing
			if (projectContentHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(projectContentHandler);
			}
			else if (diseaseMappingStudyContentHandler.isSingularRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(diseaseMappingStudyContentHandler);
			}
			else if (calculationMethodContentHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(calculationMethodContentHandler);
			}
			else if (rifOutputOptionContentHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(rifOutputOptionContentHandler);
			}
		
			//either delegate or scan for field tags releated to this handler
			if (isDelegatedHandlerAssigned() == true) {
				//one of the above cases results in an active delegated handler.  Now delegate
				AbstractRIFConceptContentHandler currentDelegatedHandler
					= getCurrentDelegatedHandler();
				currentDelegatedHandler.startElement(
					nameSpaceURI, 
					localName, 
					qualifiedName, 
					attributes);
			}
			else if (equalsFieldName(qualifiedName, "submitted_by")) {
				User user = User.newInstance(getCurrentFieldValue(), "");
				currentRIFJobSubmission.setUser(user);
			}
			else if (equalsFieldName(qualifiedName, "job_submission_date")) {
				String jobSubmissionTimePhrase
					= getCurrentFieldValue();
				Collator collator = RIFServiceMessages.getCollator();
				if (collator.equals(jobSubmissionTimePhrase, "") == false) {
					Date jobSubmissionTime
						= RIFServiceMessages.getTime(jobSubmissionTimePhrase);
					currentRIFJobSubmission.setJobSubmissionTime(jobSubmissionTime);
				}
			}
			else if (isIgnoredStartTag(qualifiedName) == false) {
				assert false;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {

		if (isSingularRecordName(qualifiedName) == true) {
			deactivate();
		}
		else if (isDelegatedHandlerAssigned() == true) {
			AbstractRIFConceptContentHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
			
			if (currentDelegatedHandler.isActive() == false) {
				//current handler has finished.  Therefore, cast delegator and obtain data
				if (currentDelegatedHandler == projectContentHandler) {
					Project project
						= projectContentHandler.getProject();
					currentRIFJobSubmission.setProject(project);
				}
				else if (currentDelegatedHandler == diseaseMappingStudyContentHandler) {
					DiseaseMappingStudy diseaseMappingStudy
						= diseaseMappingStudyContentHandler.getDiseaseMappingStudy();
					currentRIFJobSubmission.setStudy(diseaseMappingStudy);
				}
				else if (currentDelegatedHandler == calculationMethodContentHandler) {
					ArrayList<CalculationMethod> calculationMethods
						= calculationMethodContentHandler.getCalculationMethods();
					currentRIFJobSubmission.setCalculationMethods(calculationMethods);
				}
				else if (currentDelegatedHandler == rifOutputOptionContentHandler) {
					ArrayList<RIFOutputOption> rifOutputOptions
						= rifOutputOptionContentHandler.getRIFOutputOptions();
					currentRIFJobSubmission.setRIFOutputOptions(rifOutputOptions);
				}
				else {
					assert false;
				}
				
				unassignDelegatedHandler();
			}
		}
		else if (equalsFieldName(qualifiedName, "submitted_by")) {			
			User user = User.newInstance(getCurrentFieldValue(), "");
			currentRIFJobSubmission.setUser(user);
		}
		else if (equalsFieldName(qualifiedName, "job_submission_date")) {
			Date jobSubmissionTime
				= RIFServiceMessages.getTime(getCurrentFieldValue());
			currentRIFJobSubmission.setJobSubmissionTime(jobSubmissionTime);
		}
		else {
			assert false;
		}
	}
}
