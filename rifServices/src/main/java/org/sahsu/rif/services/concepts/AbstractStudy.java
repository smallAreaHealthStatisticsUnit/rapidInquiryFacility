
package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Objects;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;

/**
 * 
 * Embodies a request that the RIF uses to do a disease mapping or risk analysis
 * activity.  Note that the class {@link RIFStudySubmission}
 * encompasses both the request (study) and various information about how the results
 * should be configured.
 */
public abstract class AbstractStudy extends AbstractRIFConcept {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();

	static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	static final Messages SERVICE_MESSAGES = Messages.serviceMessages();

	AbstractStudyArea studyArea;

	/** The name. */
	private String name;
	
	/** The description. */
	private String description;
	
	/** The known issues. */
	private String otherNotes;
	
	/** The geography. */
	private Geography geography;
	
	/** The comparison area. */
	private ComparisonArea comparisonArea;
	
	/** The investigations. */
	private ArrayList<Investigation> investigations;

	private StudyType studyType;
	
	private String riskAnalysisExposureField;

	public static AbstractStudy newInstance(StudyType studyType) {

		switch (studyType) {

			case DISEASE_MAPPING:
				return DiseaseMappingStudy.newInstance();
			case RISK_ANALYSIS:
				return RiskAnalysisStudy.newInstance();
			default:
				throw new IllegalArgumentException(String.format(
						"Unknown study type '%s' in AbstractStudy.newInstance", studyType.type()));
		}
	}

    /**
     * Instantiates a new abstract study.
     * @param studyType the type of study
     */
	public AbstractStudy(StudyType studyType) {
    	
		name = "";
		description = "";
		otherNotes = "";
		riskAnalysisExposureField = null;
		this.studyType = studyType;
		geography = Geography.newInstance();
		comparisonArea = ComparisonArea.newInstance();
		investigations = new ArrayList<>();
		studyArea = AbstractStudyArea.newInstance(studyType);
	}

	public boolean isDiseaseMapping() {

		return studyType == StudyType.DISEASE_MAPPING;
	}

	public boolean isRiskAnalysis() {

		return studyType == StudyType.RISK_ANALYSIS;
	}


	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(
		final String name) {
		
		this.name = name;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(
		final String description) {
		
		this.description = description;
	}

	/**
	 * Gets the known issues.
	 *
	 * @return the other notes
	 */
	private String getOtherNotes() {
		
		return otherNotes;
	}

	/**
	 * Sets the other notes.
	 *
	 */
	public void setOtherNotes(
		final String otherNotes) {
		
		this.otherNotes = otherNotes;
	}
	
	/**
	 * Gets the geography.
	 *
	 * @return the geography
	 */
	public Geography getGeography() {
		
		return geography;
	}
	
	/**
	 * Sets the geography.
	 *
	 * @param geography the new geography
	 */
	public void setGeography(
		final Geography geography) {
		
		this.geography = geography;
	}
	
	/**
	 * Gets the comparison area.
	 *
	 * @return the comparison area
	 */
	public ComparisonArea getComparisonArea() {
		
		return comparisonArea;
	}

	/**
	 * Sets the comparison area.
	 *
	 * @param comparisonArea the new comparison area
	 */
	public void setComparisonArea(
		final ComparisonArea comparisonArea) {
		
		this.comparisonArea = comparisonArea;
	}
	
	/**
	 * Gets the investigations.
	 *
	 * @return the investigations
	 */
	public ArrayList<Investigation> getInvestigations() {
		
		return investigations;
	}
	
	/**
	 * Adds the investigation.
	 *
	 * @param investigation the investigation
	 */
	public void addInvestigation(
		final Investigation investigation) {
		
		investigations.add(investigation);
	}
	
	/**
	 * Sets the investigations.
	 *
	 * @param investigations the new investigations
	 */
	public void setInvestigations(
		final ArrayList<Investigation> investigations) {
		
		this.investigations = investigations;
	}
	
	/**
	 * Clear investigations.
	 */
	public void clearInvestigations() {
		
		investigations.clear();
	}
	
	public void identifyDifferences(
		final AbstractStudy anotherAbstractStudy,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherAbstractStudy, 
			differences);
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherStudy the other study
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final AbstractStudy otherStudy) {
		
		String otherName = otherStudy.getName();
		String otherDescription = otherStudy.getDescription();
		String otherKnownIssues = otherStudy.getOtherNotes();

		Collator collator = Collator.getInstance();
		
		ComparisonArea otherComparisonArea = otherStudy.getComparisonArea();		
		ArrayList<Investigation> otherInvestigations
			= otherStudy.getInvestigations();
	
		if (FieldValidationUtility.hasDifferentNullity(name, otherName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (name != null) {
			//they must both be non-null
			if (!collator.equals(name, otherName)) {
				return false;
			}
		}
		
		if (FieldValidationUtility.hasDifferentNullity(description, otherDescription)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (description != null) {
			//they must both be non-null
			if (!collator.equals(description, otherDescription)) {
				return false;
			}
		}
		
		if (FieldValidationUtility.hasDifferentNullity(otherNotes, otherKnownIssues)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (otherNotes != null) {
			//they must both be non-null
			if (!collator.equals(otherNotes, otherKnownIssues)) {
				return false;
			}
		}
				
		if (FieldValidationUtility.hasDifferentNullity(
			comparisonArea, 
			otherComparisonArea)) {
			
			//reject if one is null and the other is non-null
			return false;
		}
		else if (comparisonArea != null) {
			//they must both be non-null
			if (!comparisonArea.hasIdenticalContents(otherComparisonArea)) {
				return false;
			}
		}
		
		if (!Investigation.hasIdenticalContents(investigations, otherInvestigations)) {
			return false;
		}
		
		return super.hasIdenticalContents(otherStudy);
	}

	/**
	 * Gets the disease mapping study area.
	 *
	 * @return the disease mapping study area
	 */
		public AbstractStudyArea getStudyArea() {

			return studyArea;
		}

	/**
		* Sets the disease mapping study area.
		*
		* @param studyArea the new disease mapping study area
		*/
		public void setStudyArea(final AbstractStudyArea studyArea) {

			this.studyArea = studyArea;
		}

	public void checkErrors(final ValidationPolicy validationPolicy)
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<>();
		checkErrors(
			validationPolicy,
			errorMessages);
		if (errorMessages.size() > 0) {
			rifLogger.debug(this.getClass(), "AbstractStudy.checkErrors(): "
			                                 + errorMessages.size());
		}

		//add any errors inherent in the study area object
		if (studyArea == null) {
			// TODO: NPE waiting to happen!!!
			// String diseaseMappingStudyAreaFieldName
			// 	= studyArea.getRecordType();

			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					getRecordType(),
					"diseaseMappingStudyAreaFieldName"); // Quoted for now: see to-do above.
			errorMessages.add(errorMessage);
		}
		else {
			try {
				studyArea.checkErrors(validationPolicy);
			}
			catch(RIFServiceException exception) {
				rifLogger.debug(this.getClass(), "AbstractStudyArea.checkErrors(): " +
				                                                     exception.getErrorMessages()
						                                                     .size());
				errorMessages.addAll(exception.getErrorMessages());
			}
		}

		countErrors(RIFServiceError.INVALID_DISEASE_MAPPING_STUDY, errorMessages);
	}

	/**
	 * Creates the copy.
	 *
	 * @param originalDiseaseMappingStudy the original disease mapping study
	 * @return the disease mapping study
	 */
	static public AbstractStudy createCopy(
		final AbstractStudy originalDiseaseMappingStudy) {

		if (originalDiseaseMappingStudy == null) {
			return null;
		}

		AbstractStudy cloneDiseaseMappingStudy = AbstractStudy.newInstance(
				originalDiseaseMappingStudy.type());
		Geography originalGeography
			= originalDiseaseMappingStudy.getGeography();
		Geography cloneGeography = Geography.createCopy(originalGeography);
		cloneDiseaseMappingStudy.setGeography(cloneGeography);

		cloneDiseaseMappingStudy.setIdentifier(originalDiseaseMappingStudy.getIdentifier());
		cloneDiseaseMappingStudy.setName(originalDiseaseMappingStudy.getName());
		cloneDiseaseMappingStudy.setDescription(originalDiseaseMappingStudy.getDescription());
		cloneDiseaseMappingStudy.setRiskAnalysisExposureField(originalDiseaseMappingStudy.getRiskAnalysisExposureField());
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

	@Override
	public void checkSecurityViolations()
		throws RIFServiceSecurityException {

		super.checkSecurityViolations();
		
		String recordType = getRecordType();
		
		//Extract field names
		String nameFieldLabel
			= SERVICE_MESSAGES.getMessage("abstractStudy.name.label");
		String descriptionFieldLabel
			= SERVICE_MESSAGES.getMessage("abstractStudy.description.label");
		String otherNotesFieldLabel
			= SERVICE_MESSAGES.getMessage("abstractStudy.otherNotes.label");
				
		//Extract field values
		
		//Check for security problems.  Ensure EVERY text field is checked
		//These checks will throw a security exception and stop further validation
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			nameFieldLabel, 
			name);
		
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			descriptionFieldLabel, 
			description);

		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			otherNotesFieldLabel, 
			otherNotes);

		
		geography.checkSecurityViolations();
		comparisonArea.checkSecurityViolations();
		for (Investigation investigation : investigations) {
			investigation.checkSecurityViolations();
		}
	}
	
	/**
	 * Check errors.
	 *
	 * @param errorMessages the error messages
	 * @throws RIFServiceException the RIF service exception
	 */
	protected void checkErrors(
		final ValidationPolicy validationPolicy,
		final ArrayList<String> errorMessages) 
		throws RIFServiceException {

		//Extract field names
		String nameFieldLabel = SERVICE_MESSAGES.getMessage("abstractStudy.name.label");
		String descriptionFieldLabel = SERVICE_MESSAGES.getMessage(
				"abstractStudy.description.label");
				
		//Extract field values
		String recordType = getRecordType();
		
		FieldValidationUtility fieldValidationUtility = new FieldValidationUtility();		
		if (fieldValidationUtility.isEmpty(name)) {
			String errorMessage 
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					nameFieldLabel);
			errorMessages.add(errorMessage);
		}
    
		if (fieldValidationUtility.isEmpty(getDescription())) {
			String errorMessage 
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					descriptionFieldLabel);
			errorMessages.add(errorMessage);
		}   
		
		if (geography == null) {
			String comparisonAreaLabel
				= RIFServiceMessages.getMessage("geography.label");
			String errorMessage 
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					comparisonAreaLabel);
			errorMessages.add(errorMessage);			
		}
		else {
			try {
				geography.checkErrors(validationPolicy);
			}
			catch(RIFServiceException exception) {
				rifLogger.debug(this.getClass(), "Geography.checkErrors(): " + 
					exception.getErrorMessages().size());
				errorMessages.addAll(exception.getErrorMessages());
			}			
		}
		
		if (comparisonArea == null) {
			String comparisonAreaLabel
				= RIFServiceMessages.getMessage("comparisonArea.label");
			String errorMessage 
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					comparisonAreaLabel);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				comparisonArea.checkErrors(validationPolicy);
			}
			catch(RIFServiceException exception) {
				rifLogger.debug(this.getClass(), "ComparisonArea.checkErrors(): " + 
					exception.getErrorMessages().size());
				errorMessages.addAll(exception.getErrorMessages());
			}			
		}
		
		if (investigations == null) {
			String investigationsFieldName
				= RIFServiceMessages.getMessage("investigation.plural.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					investigationsFieldName);
			errorMessages.add(errorMessage);			
		}
		else if (investigations.isEmpty()) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"abstractStudy.error.noInvestigationSpecified",
					recordType,
					getDisplayName());
			errorMessages.add(errorMessage);
		}
		else {
			for (Investigation investigation : investigations) {
				if (investigation == null) {
					String covariateRecordType =
							SERVICE_MESSAGES.getMessage("investigation.label");
					String errorMessage
						= SERVICE_MESSAGES.getMessage(
							"general.validation.nullListItem",
							getRecordType(),
							covariateRecordType);
					errorMessages.add(errorMessage);
				}
				else {
					try {
						investigation.checkErrors(validationPolicy);
					}
					catch(RIFServiceException exception) {
						rifLogger.debug(this.getClass(), "Investigation.checkErrors(): " + 
							exception.getErrorMessages().size());
						errorMessages.addAll(exception.getErrorMessages());			
					}
				}
			}
			
			//go through investigations again to ensure there are none with duplicate titles
			HashSet<String> uniqueInvestigationTitles = new HashSet<String>();
			for (Investigation investigation : investigations) {
				String currentInvestigationTitle
					= investigation.getTitle();
				if (uniqueInvestigationTitles.contains(currentInvestigationTitle)) {
					String errorMessage
						= SERVICE_MESSAGES.getMessage(
							"abstractStudy.error.duplicateInvestigation",
							recordType,
							getDisplayName(),
							currentInvestigationTitle);
					errorMessages.add(errorMessage);
				}
				else {
					uniqueInvestigationTitles.add(currentInvestigationTitle);
				}
			}
			
			//go through investigations
			String differentDenominatorsResult
				= identifyDifferentDenominators(investigations);
			if (differentDenominatorsResult != null) {
				errorMessages.add(differentDenominatorsResult);
			}
			
			//ensure that the set of covariates used in each investigation is the
			//same
			int numberOfInvestigations = investigations.size();
			if (numberOfInvestigations > 1) {
				Investigation firstInvestigation
					= investigations.get(0);
				ArrayList<String> differencesInCovariateCollections
					= new ArrayList<>();
				for (int i = 1; i < numberOfInvestigations; i++) {
					Investigation currentInvestigation
						= investigations.get(i);
					ArrayList<String> differences
						= firstInvestigation.getDifferencesInCovariates(currentInvestigation);
					differencesInCovariateCollections.addAll(differences);
				}
				
				int totalNumberOfDifferences = differencesInCovariateCollections.size();
				if (totalNumberOfDifferences > 0) {
					//differences in covariate collections were detected
					//amongst the investigations
					StringBuilder listOfDifferences = new StringBuilder();
					for (int i = 0; i < totalNumberOfDifferences; i++) {
						if (i != 0) {
							listOfDifferences.append(",");
						}
						listOfDifferences.append(differencesInCovariateCollections.get(i));
					}
					
					String errorMessage
						= RIFServiceMessages.getMessage(
							"abstractStudy.error.inconsistentCovariates",
							listOfDifferences.toString());
					errorMessages.add(errorMessage);
				}				
			}
			
			
		}		
	}

	/**
	 * Assumes that investigations have a valid ND pair
	 * @param investigations
	 * @return
	 */
	private static String identifyDifferentDenominators(
			final ArrayList<Investigation> investigations) {
		
		if (investigations.size() < 2) {
			return null;
		}
		
		//all of the investigations should have the same 
		Hashtable<String, ArrayList<Investigation>> investigationsForDenominatorTableNames =
				new Hashtable<>();
		for (Investigation investigation : investigations) {
			NumeratorDenominatorPair ndPair = investigation.getNdPair();
			Objects.requireNonNull(ndPair);
			String denominatorTableName = ndPair.getDenominatorTableName();
			ArrayList<Investigation> investigationsForDenominator =
					investigationsForDenominatorTableNames.get(denominatorTableName);
			if (investigationsForDenominator == null) {
				investigationsForDenominator = new ArrayList<>();
				investigationsForDenominatorTableNames.put(denominatorTableName,
				                                           investigationsForDenominator);
			}

			investigationsForDenominator.add(investigation);
		}
		
		//we should only have one denominator table name
		ArrayList<String> denominatorTableNameKeys = new ArrayList<>(
				investigationsForDenominatorTableNames.keySet());
		if (denominatorTableNameKeys.size() > 1) {
			return SERVICE_MESSAGES.getMessage("abstractStudy.error.inconsistentDenominatorTables");
		} else {
			return null;
		}			
	}

	@Override
	public String getDisplayName() {
		
		return name;
	}

	public void setRiskAnalysisExposureField(String riskAnalysisExposureField) {	
		this.riskAnalysisExposureField = riskAnalysisExposureField;
	}	
	
	public String getRiskAnalysisExposureField() {	
		return riskAnalysisExposureField;
	}

	public StudyType type() {

		return studyType;
	}
}
