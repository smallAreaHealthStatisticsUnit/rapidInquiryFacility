package rifJobSubmissionTool.system;

import rifGenericUILibrary.UserInterfaceFactory;
import rifServices.businessConceptLayer.AbstractGeographicalArea;
import rifServices.businessConceptLayer.CalculationMethod;
import rifServices.businessConceptLayer.ComparisonArea;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.DiseaseMappingStudyArea;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.RIFOutputOption;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.businessConceptLayer.User;
import rifServices.dataStorageLayer.ProductionRIFStudyServiceBundle;

import java.util.ArrayList;
import java.io.File;


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

public class RIFStudySubmissionToolSession {
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private ProductionRIFStudyServiceBundle rifStudyServiceBundle;
	
	
	/** The ui factory. */
	private UserInterfaceFactory uiFactory;
	
	/** The working directory. */
	private File workingDirectory;
	
	/** The user. */
	private User user;
	
	/** The current rif job submission. */
	private RIFStudySubmission currentRIFJobSubmission;
	
	/** The currently edited investigation. */
	private Investigation currentlyEditedInvestigation;

	/** The current geographical area. */
	private AbstractGeographicalArea currentGeographicalArea;
	
	/** The committed activity steps. */
	private ArrayList<RIFStudySubmissionActivityStep> committedActivitySteps;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new RIF session.
	 *
	 * @param service the service
	 * @param user the user
	 */
	public RIFStudySubmissionToolSession(
		ProductionRIFStudyServiceBundle rifStudyServiceBundle,
		User user) {

		this.rifStudyServiceBundle = rifStudyServiceBundle;
		this.user = user;		
		uiFactory = new UserInterfaceFactory();
		committedActivitySteps = new ArrayList<RIFStudySubmissionActivityStep>();
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Sets the current rif submission.
	 *
	 * @param currentRIFJobSubmission the new current rif submission
	 */
	public void setCurrentRIFSubmission(
		RIFStudySubmission currentRIFJobSubmission) {

		this.currentRIFJobSubmission = currentRIFJobSubmission;
	}
	
	/**
	 * Gets the RIF job submission.
	 *
	 * @return the RIF job submission
	 */
	public RIFStudySubmission getRIFJobSubmission() {
		
		return currentRIFJobSubmission ;
	}
	
	
	/**
	 * Gets the current geography.
	 *
	 * @return the current geography
	 */
	public Geography getCurrentGeography() {
		
		if (currentRIFJobSubmission == null) {
			return null;
		}
		
		DiseaseMappingStudy diseaseMappingStudy
			= (DiseaseMappingStudy) currentRIFJobSubmission.getStudy();
		if (diseaseMappingStudy == null) {
			return null;
		}
		
		return diseaseMappingStudy.getGeography();
	}

	/**
	 * Gets the current disease mapping study.
	 *
	 * @return the current disease mapping study
	 */
	public DiseaseMappingStudy getCurrentDiseaseMappingStudy() {
		
		if (currentRIFJobSubmission == null) {
			return null;
		}
		
		DiseaseMappingStudy diseaseMappingStudy
			= (DiseaseMappingStudy) currentRIFJobSubmission.getStudy();
		return diseaseMappingStudy;		
	}
	
	/**
	 * Sets the current disease mapping study.
	 *
	 * @param diseaseMappingStudy the new current disease mapping study
	 */
	public void setCurrentDiseaseMappingStudy(
		DiseaseMappingStudy diseaseMappingStudy) {
		
		currentRIFJobSubmission.setStudy(diseaseMappingStudy);
	}	
	
	
	/**
	 * Gets the current disease mapping study area.
	 *
	 * @return the current disease mapping study area
	 */
	public DiseaseMappingStudyArea getCurrentDiseaseMappingStudyArea() {
	
		DiseaseMappingStudy diseaseMappingStudy
			= getCurrentDiseaseMappingStudy();
		if (diseaseMappingStudy == null) {
			return null;
		}
		
		return diseaseMappingStudy.getDiseaseMappingStudyArea();		
	}
	
	/**
	 * Gets the current comparison area.
	 *
	 * @return the current comparison area
	 */
	public ComparisonArea getCurrentComparisonArea() {
		
		DiseaseMappingStudy diseaseMappingStudy
			= getCurrentDiseaseMappingStudy();
		if (diseaseMappingStudy == null) {
			return null;
		}
		
		return diseaseMappingStudy.getComparisonArea();		
	}
	
	/**
	 * Sets the current comparison area.
	 *
	 * @param currentComparisonArea the new current comparison area
	 */
	public void setCurrentComparisonArea(
		ComparisonArea currentComparisonArea) {

		DiseaseMappingStudy diseaseMappingStudy
			= getCurrentDiseaseMappingStudy();
		diseaseMappingStudy.setComparisonArea(currentComparisonArea);
	}
	
	/**
	 * Sets the current investigations.
	 *
	 * @param currentInvestigations the new current investigations
	 */
	public void setCurrentInvestigations(
		ArrayList<Investigation> currentInvestigations) {

		DiseaseMappingStudy diseaseMappingStudy
			= getCurrentDiseaseMappingStudy();
		diseaseMappingStudy.setInvestigations(currentInvestigations);
	}
	
	/**
	 * Gets the current study investigations.
	 *
	 * @return the current study investigations
	 */
	public ArrayList<Investigation> getCurrentStudyInvestigations() {
		
		ArrayList<Investigation> investigations 
			= new ArrayList<Investigation>();
		DiseaseMappingStudy diseaseMappingStudy
			= getCurrentDiseaseMappingStudy();
		if (diseaseMappingStudy == null) {
			return investigations;
		}
		else {
			investigations.addAll(diseaseMappingStudy.getInvestigations());
		}
		
		return investigations;
	}
	
	/**
	 * Gets the current reporting options.
	 *
	 * @return the current reporting options
	 */
	public ArrayList<RIFOutputOption> getCurrentReportingOptions() {
		
		ArrayList<RIFOutputOption> reportingOptions 
			= new ArrayList<RIFOutputOption>();
		if (currentRIFJobSubmission != null) {
			reportingOptions.addAll(currentRIFJobSubmission.getRIFOutputOptions());
		}
		return reportingOptions;
	}
	
	/**
	 * Gets the current calculation methods.
	 *
	 * @return the current calculation methods
	 */
	public ArrayList<CalculationMethod> getCurrentCalculationMethods() {
		
		ArrayList<CalculationMethod> calculationMethods 
			= new ArrayList<CalculationMethod>();
		if (currentRIFJobSubmission != null) {
			calculationMethods.addAll(currentRIFJobSubmission.getCalculationMethods());
		}
		return calculationMethods;
	}
	
	/**
	 * Sets the current calculation methods.
	 *
	 * @param calculationMethods the new current calculation methods
	 */
	public void setCurrentCalculationMethods(
		ArrayList<CalculationMethod> calculationMethods) {

		currentRIFJobSubmission.setCalculationMethods(calculationMethods);
	}
	
	/**
	 * Sets the current geographical area.
	 *
	 * @param currentGeographicalArea the new current geographical area
	 */
	public void setCurrentGeographicalArea(
		AbstractGeographicalArea currentGeographicalArea) {

		this.currentGeographicalArea = currentGeographicalArea;
	}
	
	/**
	 * Gets the current geographical area.
	 *
	 * @return the current geographical area
	 */
	public AbstractGeographicalArea getCurrentGeographicalArea() {
		
		return currentGeographicalArea;
	}
	
	/**
	 * Gets the study geo level select.
	 *
	 * @return the study geo level select
	 */
	public GeoLevelSelect getStudyGeoLevelSelect() {
		
		DiseaseMappingStudyArea diseaseMappingStudyArea
			= getCurrentDiseaseMappingStudyArea();
		if (diseaseMappingStudyArea == null) {
			return null;
		}
		
		return diseaseMappingStudyArea.getGeoLevelSelect();		
	}
	
	/**
	 * Gets the study geo level to map.
	 *
	 * @return the study geo level to map
	 */
	public GeoLevelToMap getStudyGeoLevelToMap() {
		
		DiseaseMappingStudyArea diseaseMappingStudyArea
			= getCurrentDiseaseMappingStudyArea();
		if (diseaseMappingStudyArea == null) {
			return null;
		}
	
		return diseaseMappingStudyArea.getGeoLevelToMap();			
	}
		
	/**
	 * Gets the UI factory.
	 *
	 * @return the UI factory
	 */
	public UserInterfaceFactory getUIFactory() {
		
		return uiFactory;
	}
	
	/**
	 * Gets the working directory.
	 *
	 * @return the working directory
	 */
	public File getWorkingDirectory() {
		
		return workingDirectory;
	}

	/**
	 * Sets the working directory.
	 *
	 * @param workingDirectory the new working directory
	 */
	public void setWorkingDirectory(
		File workingDirectory) {
		
		this.workingDirectory = workingDirectory;
	}
	
	/**
	 * Gets the user.
	 *
	 * @return the user
	 */
	public User getUser() {
		
		return user;
	}
	
	public ProductionRIFStudyServiceBundle getRIFStudyServiceBundle() {
		return rifStudyServiceBundle;
	}
	
	/**
	 * Gets the service.
	 *
	 * @return the service
	 */
	public RIFStudySubmissionAPI getRIFStudySubmissionService() {		
		return rifStudyServiceBundle.getRIFStudySubmissionService();
	}
	
	/**
	 * Gets the currently edited investigation.
	 *
	 * @return the currently edited investigation
	 */
	public Investigation getCurrentlyEditedInvestigation() {
		
		return currentlyEditedInvestigation;
	}
	
	/**
	 * Sets the currently edited investigation.
	 *
	 * @param currentlyEditedInvestigation the new currently edited investigation
	 */
	public void setCurrentlyEditedInvestigation(
		Investigation currentlyEditedInvestigation) {

		this.currentlyEditedInvestigation = currentlyEditedInvestigation;
	}
	
	/**
	 * Gets the current investigation nd pair.
	 *
	 * @return the current investigation nd pair
	 */
	public NumeratorDenominatorPair getCurrentInvestigationNDPair() {
		
		if (currentlyEditedInvestigation == null) {
			return null;
		}
		
		return currentlyEditedInvestigation.getNdPair();
	}
	
	/**
	 * Adds the committed activity step.
	 *
	 * @param rifActivityStep the rif activity step
	 */
	public void addCommittedActivityStep(
		RIFStudySubmissionActivityStep rifActivityStep) {
		
		committedActivitySteps.add(rifActivityStep);
	}
	
	/**
	 * Checks if is activity step committed.
	 *
	 * @param rifActivityStep the rif activity step
	 * @return true, if is activity step committed
	 */
	public boolean isActivityStepCommitted(
		RIFStudySubmissionActivityStep rifActivityStep) {

		return committedActivitySteps.contains(rifActivityStep);
	}
	
	/**
	 * Removes the committed activity step.
	 *
	 * @param rifActivityStep the rif activity step
	 */
	public void removeCommittedActivityStep(
		RIFStudySubmissionActivityStep rifActivityStep) {

		committedActivitySteps.remove(rifActivityStep);
	}
	
	/**
	 * Removes the activity steps that follow.
	 *
	 * @param rifActivityStep the rif activity step
	 */
	public void removeActivityStepsThatFollow(
		RIFStudySubmissionActivityStep rifActivityStep) {

		int index = committedActivitySteps.indexOf(rifActivityStep);
		if (index == -1) {
			return;
		}
		
		//define the start index of steps that follow this rif activity step
		index += 1;		
		ArrayList<RIFStudySubmissionActivityStep> stepsToDelete = new ArrayList<RIFStudySubmissionActivityStep>();
		for (int i = index; i < committedActivitySteps.size(); i++) {
			stepsToDelete.add(committedActivitySteps.get(i));
		}
		committedActivitySteps.removeAll(stepsToDelete);		
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

