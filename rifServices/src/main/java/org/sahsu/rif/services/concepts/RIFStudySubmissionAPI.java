package org.sahsu.rif.services.concepts;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.io.*;



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

public interface RIFStudySubmissionAPI extends RIFStudyServiceAPI {
	
	/**
	 * Gets the RIF service information.
	 *
	 * @param user the user
	 * @return the RIF service information
	 * @throws RIFServiceException the RIF service exception
	 */
	public RIFServiceInformation getRIFServiceInformation(
		final User user) 
		throws RIFServiceException;
	
	/**
	 * Gets the available calculation methods.
	 *
	 * @param user the user
	 * @return the available calculation methods
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<CalculationMethod> getAvailableCalculationMethods(
		final User user) 
		throws RIFServiceException;
	
	/**
	 * Gets the health themes.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @return the health themes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthTheme> getHealthThemes(
		final User user,
		final Geography geography)
		throws RIFServiceException;	
	
	/**
	 * Gets the numerator denominator pairs.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param healthTheme the health theme
	 * @return the numerator denominator pairs
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<NumeratorDenominatorPair> getNumeratorDenominatorPairs(
		final User user,
		final Geography geography,
		final HealthTheme healthTheme) 
		throws RIFServiceException;
	
	
	public NumeratorDenominatorPair getNumeratorDenominatorPairFromNumeratorTable(
		final User user,
		final Geography geography,
		final String numeratorTableName) 
		throws RIFServiceException;
	
	//Features for Age, Sex and Study Years
	/**
	 * Gets the age groups.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param ndPair the nd pair
	 * @param sortingOrder the sorting order
	 * @return the age groups
	 * @throws RIFServiceException the RIF service exception
	 */
	List<AgeGroup> getAgeGroups(
		final User user,
		final Geography geography,
		final NumeratorDenominatorPair ndPair,
		final AgeGroupSortingOption sortingOrder) 
		throws RIFServiceException;
	
	/**
	 * Gets the sexes.
	 *
	 * @param user the user
	 * @return the sexes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<Sex> getSexes(
		final User user)
		throws RIFServiceException;
		
	//Features for Health Codes

	//Features for Covariates
	/**
	 * Gets the covariates.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelToMap the geo level to map
	 * @return the covariates
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<AbstractCovariate> getCovariates(
		final User user,
		final Geography geography,
		final GeoLevelToMap geoLevelToMap)
		throws RIFServiceException;
	
	/**
	 * Gets the year range.
	 *
	 * @param user the user
	 * @param geography the geography
	 * @param ndPair the nd pair
	 * @return the year range
	 * @throws RIFServiceException the RIF service exception
	 */
	public YearRange getYearRange(
		final User user,
		final Geography geography,
		final NumeratorDenominatorPair ndPair) 
		throws RIFServiceException;
			
	/**
	 * Gets the projects.
	 *
	 * @param user the user
	 * @return the projects
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<Project> getProjects(
		final User user) 
		throws RIFServiceException;
	

	/**
	 * Submit study.
	 *
	 * @param user the user
	 * @param rifStudySubmission the rif job submission
	 * @param outputFile the output file
	 * @param url the URL of the originating request
	 * @return the studyID for the study that has just been submitted
	 * @throws RIFServiceException the RIF service exception
	 */
	String submitStudy(final User user, final RIFStudySubmission rifStudySubmission,
			final File outputFile, final String url) throws RIFServiceException;
	
	public void createStudyExtract(
		final User user,
		final String studyID,
		final String zoomLevel,
		final Locale locale,
		final String url)
		throws RIFServiceException;	
	public FileInputStream getStudyExtract(
		final User user,
		final String studyID,
		final String zoomLevel)
		throws RIFServiceException;		
	public String getStudyExtractFIleName(
		final User user,
		final String studyID)
		throws RIFServiceException;	
	public String getExtractStatus(
		final User user,
		final String studyID)
		throws RIFServiceException;
	public String getJsonFile(
		final User user,
		final String studyID,
		final Locale locale,
		final String url)
		throws RIFServiceException;
	public String getFrontEndParameters(
		final User user);
	
	void test(final User user, final String url) throws RIFServiceException;
	
}
