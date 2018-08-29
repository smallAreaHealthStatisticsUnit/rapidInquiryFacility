package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.system.RIFServiceError;

/**
 * Contains the main properties of a study area. Currently this is a kind of
 * marker class, but it may become deprecated or developed depending on how
 * the concept of a study area differs between a risk analysis and disease mapping
 * studies.
 */
public abstract class AbstractStudyArea extends AbstractGeographicalArea {

	protected static final Messages SERVICE_MESSAGES = Messages.serviceMessages();

	/**
	 * New instance.
	 *
	 * @return the disease mapping study area
	 */
	static public AbstractStudyArea newInstance(StudyType studyType) {

		switch (studyType) {
			case DISEASE_MAPPING:
				return new DiseaseMappingStudyArea();
			case RISK_ANALYSIS:
				return new RiskAnalysisStudyArea();
			default:
				throw new IllegalStateException(
						String.format("Unknown study type '%s' in "
						              + "AbstractStudyArea.newInstance", studyType));
		}
	}

	/**
	 * Copy.
	 *
	 * @param originalStudyArea the original disease mapping study area
	 * @return the disease mapping study area
	 */
	public static AbstractStudyArea copy(final AbstractStudyArea originalStudyArea) {

		if (originalStudyArea == null) {
			return null;
		}

		DiseaseMappingStudyArea cloneDiseaseMappingStudyArea
			= new DiseaseMappingStudyArea();
		cloneDiseaseMappingStudyArea.setIdentifier(originalStudyArea.getIdentifier());
		ArrayList<MapArea> cloneMapAreas
			= MapArea.createCopy(originalStudyArea.getMapAreas());
		cloneDiseaseMappingStudyArea.setMapAreas(cloneMapAreas);

		GeoLevelView cloneGeoLevelView
			= GeoLevelView.createCopy(originalStudyArea.getGeoLevelView());
		cloneDiseaseMappingStudyArea.setGeoLevelView(cloneGeoLevelView);
		GeoLevelArea cloneGeoLevelArea
			= GeoLevelArea.createCopy(originalStudyArea.getGeoLevelArea());
		cloneDiseaseMappingStudyArea.setGeoLevelArea(cloneGeoLevelArea);
		GeoLevelSelect cloneGeoLevelSelect
			= GeoLevelSelect.createCopy(originalStudyArea.getGeoLevelSelect());
		cloneDiseaseMappingStudyArea.setGeoLevelSelect(cloneGeoLevelSelect);
		GeoLevelToMap cloneGeoLevelToMap
			= GeoLevelToMap.createCopy(originalStudyArea.getGeoLevelToMap());
		cloneDiseaseMappingStudyArea.setGeoLevelToMap(cloneGeoLevelToMap);

		return cloneDiseaseMappingStudyArea;
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

		return hasIdenticalContents(otherDiseaseMappingStudyArea);
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
		checkErrors(
			validationPolicy,
			errorMessages);
		countErrors(
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA,
				errorMessages);
	}
}
