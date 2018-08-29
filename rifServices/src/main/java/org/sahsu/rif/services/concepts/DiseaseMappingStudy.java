
package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.RIFLogger;

public final class DiseaseMappingStudy extends AbstractStudy {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();

	private DiseaseMappingStudy() {

		super(StudyType.DISEASE_MAPPING);
	}
	
	static public DiseaseMappingStudy newInstance() {

		return new DiseaseMappingStudy();
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
