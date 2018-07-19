
package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.system.RIFServiceError;

public final class DiseaseMappingStudyArea extends AbstractStudyArea {

	private static final Messages SERVICE_MESSAGES = Messages.serviceMessages();

	/**
	 * New instance.
	 *
	 * @return the disease mapping study area
	 */
	static public DiseaseMappingStudyArea newInstance() {

		return new DiseaseMappingStudyArea();
	}
	
	/**
	 * Copy.
	 *
	 * @param originalDiseaseMappingStudyArea the original disease mapping study area
	 * @return the disease mapping study area
	 */
	public static DiseaseMappingStudyArea copy(
		final DiseaseMappingStudyArea originalDiseaseMappingStudyArea) {

		if (originalDiseaseMappingStudyArea == null) {
			return null;
		}
				
		DiseaseMappingStudyArea cloneDiseaseMappingStudyArea
			= new DiseaseMappingStudyArea();
		cloneDiseaseMappingStudyArea.setIdentifier(originalDiseaseMappingStudyArea.getIdentifier());
		ArrayList<MapArea> cloneMapAreas 
			= MapArea.createCopy(originalDiseaseMappingStudyArea.getMapAreas());
		cloneDiseaseMappingStudyArea.setMapAreas(cloneMapAreas);

		GeoLevelView cloneGeoLevelView
			= GeoLevelView.createCopy(originalDiseaseMappingStudyArea.getGeoLevelView());		
		cloneDiseaseMappingStudyArea.setGeoLevelView(cloneGeoLevelView);
		GeoLevelArea cloneGeoLevelArea
			= GeoLevelArea.createCopy(originalDiseaseMappingStudyArea.getGeoLevelArea());		
		cloneDiseaseMappingStudyArea.setGeoLevelArea(cloneGeoLevelArea);
		GeoLevelSelect cloneGeoLevelSelect
			= GeoLevelSelect.createCopy(originalDiseaseMappingStudyArea.getGeoLevelSelect());		
		cloneDiseaseMappingStudyArea.setGeoLevelSelect(cloneGeoLevelSelect);
		GeoLevelToMap cloneGeoLevelToMap
			= GeoLevelToMap.createCopy(originalDiseaseMappingStudyArea.getGeoLevelToMap());		
		cloneDiseaseMappingStudyArea.setGeoLevelToMap(cloneGeoLevelToMap);

		return cloneDiseaseMappingStudyArea;		
	}

	public void identifyDifferences(
		final DiseaseMappingStudyArea anotherDiseaseMappingStudyArea,
		final ArrayList<String> differences) {
	
		super.identifyDifferences(
			anotherDiseaseMappingStudyArea, 
			differences);
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherDiseaseMappingStudyArea the other disease mapping study area
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final DiseaseMappingStudyArea otherDiseaseMappingStudyArea) {

		if (otherDiseaseMappingStudyArea == null) {
			return false;
		}
		
		return super.hasIdenticalContents(otherDiseaseMappingStudyArea);		
	}
	
	@Override
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		super.checkSecurityViolations();		
	}
	
	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {	
		
		//do security checks on String area identifiers
		ArrayList<String> errorMessages = new ArrayList<>();
		super.checkErrors(
			validationPolicy,
			errorMessages);
		countErrors(
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA,
				errorMessages);
	}

	@Override
	public String getRecordType() {

		return SERVICE_MESSAGES.getMessage("diseaseMappingStudyArea.label");
	}
}
