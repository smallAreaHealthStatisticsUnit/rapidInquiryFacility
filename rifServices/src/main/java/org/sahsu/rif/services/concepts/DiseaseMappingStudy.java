
package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.system.RIFServiceError;

public final class DiseaseMappingStudy extends AbstractStudy {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();

	private DiseaseMappingStudy() {

		super();
	}
	
	static public DiseaseMappingStudy newInstance() {

		return new DiseaseMappingStudy();
	}

	/**
	 * Creates the copy.
	 *
	 * @param originalDiseaseMappingStudy the original disease mapping study
	 * @return the disease mapping study
	 */
	static public DiseaseMappingStudy createCopy(
		final DiseaseMappingStudy originalDiseaseMappingStudy) {

		if (originalDiseaseMappingStudy == null) {
			return null;
		}

		DiseaseMappingStudy cloneDiseaseMappingStudy = new DiseaseMappingStudy();
		Geography originalGeography 
			= originalDiseaseMappingStudy.getGeography();
		Geography cloneGeography = Geography.createCopy(originalGeography);
		cloneDiseaseMappingStudy.setGeography(cloneGeography);
		
		cloneDiseaseMappingStudy.setIdentifier(originalDiseaseMappingStudy.getIdentifier());	     
		cloneDiseaseMappingStudy.setName(originalDiseaseMappingStudy.getName());
		cloneDiseaseMappingStudy.setDescription(originalDiseaseMappingStudy.getDescription());
		AbstractStudyArea cloneDiseaseMappingStudyArea
			= AbstractStudyArea.copy(originalDiseaseMappingStudy.getStudyArea());
		cloneDiseaseMappingStudy.setStudyArea(cloneDiseaseMappingStudyArea);
		ComparisonArea cloneComparisonArea 
			= ComparisonArea.createCopy(originalDiseaseMappingStudy.getComparisonArea());
		cloneDiseaseMappingStudy.setComparisonArea(cloneComparisonArea);
		
		ArrayList<Investigation> originalInvestigations
			= originalDiseaseMappingStudy.getInvestigations();
		ArrayList<Investigation> cloneInvestigations
			= Investigation.createCopy(originalInvestigations);
		cloneDiseaseMappingStudy.setInvestigations(cloneInvestigations);

		return cloneDiseaseMappingStudy;
	}

	public void identifyDifferences(
		final DiseaseMappingStudy anotherDiseaseMappingStudy,
		final ArrayList<String> differences) {
	
		super.identifyDifferences(
			anotherDiseaseMappingStudy, 
			differences);
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherDiseaseMappingStudy the other disease mapping study
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(final DiseaseMappingStudy otherDiseaseMappingStudy) {
		
		if (otherDiseaseMappingStudy == null) {
			return false;
		}
		
		AbstractStudyArea otherDiseaseMappingStudyArea
			= otherDiseaseMappingStudy.getStudyArea();
		
		if (studyArea == null) {
			if (otherDiseaseMappingStudyArea != null) {
				return false;
			}			
		}
		else {
			if (!studyArea.hasIdenticalContents(otherDiseaseMappingStudyArea)) {
				return false;
			}
		}
		
		return super.hasIdenticalContents(otherDiseaseMappingStudy);
	}
	
	public void checkErrors(final ValidationPolicy validationPolicy)
		throws RIFServiceException {		
		
		ArrayList<String> errorMessages = new ArrayList<>();
		super.checkErrors(
			validationPolicy,
			errorMessages);
		if (errorMessages.size() > 0) {
			rifLogger.debug(this.getClass(), "AbstractStudy.checkErrors(): " + 
				errorMessages.size());
		}	
				
		//add any errors inherent in the study area object
		if (studyArea == null) {
			String diseaseMappingStudyAreaFieldName
				= studyArea.getRecordType();
		
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					getRecordType(),
					diseaseMappingStudyAreaFieldName);
			errorMessages.add(errorMessage);						
		}
		else {			
			try {
				studyArea.checkErrors(validationPolicy);
			}
			catch(RIFServiceException exception) {
				rifLogger.debug(this.getClass(), "AbstractStudyArea.checkErrors(): " +
					exception.getErrorMessages().size());
				errorMessages.addAll(exception.getErrorMessages());
			}
		}
		
		countErrors(RIFServiceError.INVALID_DISEASE_MAPPING_STUDY, errorMessages);
	}
	
	@Override
	public String getRecordType() {

		return SERVICE_MESSAGES.getMessage("diseaseMappingStudy.label");
	}

	@Override
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		super.checkSecurityViolations();
		if (studyArea != null) {
			studyArea.checkSecurityViolations();
		}
	}
}
