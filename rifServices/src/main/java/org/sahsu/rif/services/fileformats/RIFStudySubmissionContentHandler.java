
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.concepts.AbstractStudy;
import org.sahsu.rif.services.concepts.CalculationMethod;
import org.sahsu.rif.services.concepts.DiseaseMappingStudy;
import org.sahsu.rif.services.concepts.Project;
import org.sahsu.rif.services.concepts.RIFOutputOption;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

final public class RIFStudySubmissionContentHandler
	extends AbstractXMLContentHandler {

	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	/** The current rif job submission. */
	private RIFStudySubmission currentRIFJobSubmission;
	
	/** The project content handler. */
	private ProjectContentHandler projectContentHandler;
	
	/** The disease mapping study content handler. */
	private DiseaseMappingStudyContentHandler diseaseMappingStudyContentHandler;

	private final RiskAnalysisStudyContentHandler riskAnalysisStudyContentHandler;
	
	/** The calculation method content handler. */
	private CalculationMethodContentHandler calculationMethodContentHandler; 
	
	/** The rif output option content handler. */
	private RIFOutputOptionContentHandler rifOutputOptionContentHandler;

    /**
     * Instantiates a new RIF job submission content handler.
     */
	public RIFStudySubmissionContentHandler() {
		
    	setSingularRecordName("rif_job_submission");
    	projectContentHandler = new ProjectContentHandler();
    	diseaseMappingStudyContentHandler = new DiseaseMappingStudyContentHandler();
    	riskAnalysisStudyContentHandler = new RiskAnalysisStudyContentHandler();
    	calculationMethodContentHandler = new CalculationMethodContentHandler();
    	rifOutputOptionContentHandler = new RIFOutputOptionContentHandler();
    	
    	ignoreXMLStartTag("submitted_by");
    	ignoreXMLStartTag("job_submission_date");
    }

	/**
	 * Gets the RIF job submission.
	 *
	 * @return the RIF job submission
	 */
	RIFStudySubmission getRIFJobSubmission() {
		
		return currentRIFJobSubmission;
	}

	/**
     * Write xml.
     *
     * @param rifStudySubmission the rif job submission
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void writeXML(
    	final User user,
    	final RIFStudySubmission rifStudySubmission) 
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
		
		xmlUtility.writeField(
			recordName, 
			"submitted_by", 
			user.getUserID());
		
		Date jobSubmissionTime
			= rifStudySubmission.getJobSubmissionTime();
		if (jobSubmissionTime == null) {
			xmlUtility.writeField(
				recordName,
				"job_submission_date", 
				"");
		}
		else {
			String jobSubmissionDatePhrase 
				= GENERIC_MESSAGES.getTimePhrase(jobSubmissionTime);
			xmlUtility.writeField(
				recordName,
				"job_submission_date", 
				jobSubmissionDatePhrase);			
		}
		
		Project project = rifStudySubmission.getProject();
		projectContentHandler.writeXML(project);
		
		AbstractStudy study = rifStudySubmission.getStudy();

		if (study.getClass().isAssignableFrom(DiseaseMappingStudy.class)) {
			diseaseMappingStudyContentHandler.writeXML(study);
		} else {
			riskAnalysisStudyContentHandler.writeXML(study);
		}

		calculationMethodContentHandler.writeXML(rifStudySubmission.getCalculationMethods());		
		rifOutputOptionContentHandler.writeXML(rifStudySubmission.getRIFOutputOptions());
		
		xmlUtility.writeRecordEndTag(recordName);
	}

    @Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
		projectContentHandler.initialise(outputStream, commentInjector);
		diseaseMappingStudyContentHandler.initialise(outputStream, commentInjector);
	    riskAnalysisStudyContentHandler.initialise(outputStream, commentInjector);
		calculationMethodContentHandler.initialise(outputStream, commentInjector);
		rifOutputOptionContentHandler.initialise(outputStream, commentInjector);
	}

	@Override
	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
		projectContentHandler.initialise(outputStream);
		diseaseMappingStudyContentHandler.initialise(outputStream);
		riskAnalysisStudyContentHandler.initialise(outputStream);
		calculationMethodContentHandler.initialise(outputStream);
		rifOutputOptionContentHandler.initialise(outputStream);
	}

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
		else if (isDelegatedHandlerAssigned()) {
			AbstractXMLContentHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.startElement(
				nameSpaceURI, 
				localName, 
				qualifiedName, 
				attributes);
		}
		else {				
			//determine if a delegate handler can be assigned to do future processing
			if (projectContentHandler.isSingularRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(projectContentHandler);
			} else if (diseaseMappingStudyContentHandler.isSingularRecordTypeApplicable(
					qualifiedName)) {
				assignDelegatedHandler(diseaseMappingStudyContentHandler);
			} else if (riskAnalysisStudyContentHandler.isSingularRecordTypeApplicable(
					qualifiedName)) {
				assignDelegatedHandler(riskAnalysisStudyContentHandler);
			} else if (calculationMethodContentHandler.isPluralRecordTypeApplicable(
					qualifiedName)) {
				assignDelegatedHandler(calculationMethodContentHandler);
			} else if (rifOutputOptionContentHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(rifOutputOptionContentHandler);
			}
		
			//either delegate or scan for field tags releated to this handler
			if (isDelegatedHandlerAssigned()) {
				//one of the above cases results in an active delegated handler.  Now delegate
				AbstractXMLContentHandler currentDelegatedHandler
					= getCurrentDelegatedHandler();
				currentDelegatedHandler.startElement(
					nameSpaceURI, 
					localName, 
					qualifiedName, 
					attributes);
			} else if (equalsFieldName(qualifiedName, "job_submission_date")) {

				String jobSubmissionTimePhrase
					= getCurrentFieldValue();
				Collator collator = GENERIC_MESSAGES.getCollator();
				
				currentRIFJobSubmission.setJobSubmissionTime(new Date());

				FieldValidationUtility fieldValidationUtility = new FieldValidationUtility();
				if (!fieldValidationUtility.isEmpty(jobSubmissionTimePhrase)) {
					if (!collator.equals(jobSubmissionTimePhrase, "")) {
						Date jobSubmissionTime
							= GENERIC_MESSAGES.getTime(jobSubmissionTimePhrase);
						currentRIFJobSubmission.setJobSubmissionTime(jobSubmissionTime);
					}
				}
			} else {
				assert isIgnoredStartTag(qualifiedName);
			}
		}
	}

	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {

		if (isSingularRecordName(qualifiedName)) {
			deactivate();
		}
		else if (isDelegatedHandlerAssigned()) {
			AbstractXMLContentHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
			
			if (!currentDelegatedHandler.isActive()) {
				//current handler has finished.  Therefore, cast delegator and obtain data
				if (currentDelegatedHandler == projectContentHandler) {
					Project project
						= projectContentHandler.getProject();
					currentRIFJobSubmission.setProject(project);
				} else if (currentDelegatedHandler == diseaseMappingStudyContentHandler) {

					AbstractStudy study = diseaseMappingStudyContentHandler.getStudy();
					currentRIFJobSubmission.setStudy(study);
				} else if (currentDelegatedHandler == riskAnalysisStudyContentHandler) {

					AbstractStudy study = riskAnalysisStudyContentHandler.getStudy();
					currentRIFJobSubmission.setStudy(study);
				} else if (currentDelegatedHandler == calculationMethodContentHandler) {

					List<CalculationMethod> calculationMethods =
							calculationMethodContentHandler.getCalculationMethods();
					currentRIFJobSubmission.setCalculationMethods(calculationMethods);
				} else if (currentDelegatedHandler == rifOutputOptionContentHandler) {

					ArrayList<RIFOutputOption> rifOutputOptions
						= rifOutputOptionContentHandler.getRIFOutputOptions();
					currentRIFJobSubmission.setRIFOutputOptions(rifOutputOptions);
				}
				
				unassignDelegatedHandler();
			}
		}
		else if (equalsFieldName(qualifiedName, "job_submission_date")) {
			Date jobSubmissionTime
				= GENERIC_MESSAGES.getTime(getCurrentFieldValue());
			currentRIFJobSubmission.setJobSubmissionTime(jobSubmissionTime);
		}
		else {
			assert false;
		}
	}
}
