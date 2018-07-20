
package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.system.RIFServiceError;

public final class DiseaseMappingStudy extends AbstractStudy {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	
	private AbstractStudyArea diseaseMappingStudyArea;
    
	private DiseaseMappingStudy() {
    	
		diseaseMappingStudyArea = AbstractStudyArea.newInstance();
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
			= AbstractStudyArea.copy(originalDiseaseMappingStudy.getDiseaseMappingStudyArea());
		cloneDiseaseMappingStudy.setDiseaseMappingStudyArea(cloneDiseaseMappingStudyArea);
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

	
    /**
     * Gets the disease mapping study area.
     *
     * @return the disease mapping study area
     */
	public AbstractStudyArea getDiseaseMappingStudyArea() {
    	
		return diseaseMappingStudyArea;
	}
	
	/**
	 * Sets the disease mapping study area.
	 *
	 * @param diseaseMappingStudyArea the new disease mapping study area
	 */
	public void setDiseaseMappingStudyArea(final AbstractStudyArea diseaseMappingStudyArea) {
		
		this.diseaseMappingStudyArea = diseaseMappingStudyArea;
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
	public boolean hasIdenticalContents(
		final DiseaseMappingStudy otherDiseaseMappingStudy) {
		
		if (otherDiseaseMappingStudy == null) {
			return false;
		}
		
		AbstractStudyArea otherDiseaseMappingStudyArea
			= otherDiseaseMappingStudy.getDiseaseMappingStudyArea();
		
		if (diseaseMappingStudyArea == null) {
			if (otherDiseaseMappingStudyArea != null) {
				return false;
			}			
		}
		else {
			if (!diseaseMappingStudyArea.hasIdenticalContents(otherDiseaseMappingStudyArea)) {
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
		if (diseaseMappingStudyArea == null) {
			String diseaseMappingStudyAreaFieldName
				= SERVICE_MESSAGES.getMessage("diseaseMappingStudyArea.label");
		
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					getRecordType(),
					diseaseMappingStudyAreaFieldName);
			errorMessages.add(errorMessage);						
		}
		else {			
			try {
				diseaseMappingStudyArea.checkErrors(validationPolicy);
			}
			catch(RIFServiceException exception) {
				rifLogger.debug(this.getClass(), "DiseaseMappingStudyArea.checkErrors(): " + 
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
		if (diseaseMappingStudyArea != null) {
			diseaseMappingStudyArea.checkSecurityViolations();
		}
	}
}
