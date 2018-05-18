package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.datastorage.DisplayableItemSorter;
import org.sahsu.rif.generic.util.FieldValidationUtility;


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

public class CovariateUtility {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new covariate utility.
	 */
	public CovariateUtility() {

	}

	// ==========================================
	// Section Accessors and Mutators
	/**
	 * Copy covariates.
	 *
	 * @param originalCovariates the original covariates
	 * @return the array list
	 */
	public static ArrayList<AbstractCovariate> copyCovariates(
		final ArrayList<AbstractCovariate> originalCovariates) {

		ArrayList<AbstractCovariate> cloneCovariates 
			= new ArrayList<AbstractCovariate>();

		for (AbstractCovariate originalCovariate : originalCovariates) {
			if (originalCovariate instanceof AdjustableCovariate) {
				AdjustableCovariate originalAdjustableCovariate
					= (AdjustableCovariate) originalCovariate;
				cloneCovariates.add(originalAdjustableCovariate);	
			}
			else {
				//it's an exposure covariate.  Note that this is a potentially
				//vulnerable point in code if new classes of covariates are 
				//developed.
				ExposureCovariate originalExposureCovariate
					= (ExposureCovariate) originalCovariate;
				cloneCovariates.add(originalExposureCovariate);			
			}
		}
	
		return cloneCovariates;
	}

	
	/**
	 * Checks for identical contents.
	 *
	 * @param covariateListA the covariate list a
	 * @param covariateListB the covariate list b
	 * @return true, if successful
	 */
	public static boolean hasIdenticalContents(
		final ArrayList<AbstractCovariate> covariateListA,
		final ArrayList<AbstractCovariate> covariateListB) {
	
		if (FieldValidationUtility.hasDifferentNullity(
			covariateListA, 
			covariateListB)) {
			return false;
		}
		
		if (covariateListA.size() != covariateListB.size() ) {
			return false;
		}
		
		ArrayList<AbstractCovariate> covariatesA	
			= sortCovariates(covariateListA);
		ArrayList<AbstractCovariate> covariatesB	
			= sortCovariates(covariateListB);
		
		int numberOfCovariates = covariatesA.size();
		for (int i = 0; i < numberOfCovariates; i++) {
			AbstractCovariate genericCovariateA
				= covariatesA.get(i);
			if (genericCovariateA instanceof AdjustableCovariate) {
				AdjustableCovariate adjustableCovariateA
					= (AdjustableCovariate) genericCovariateA;
				AdjustableCovariate adjustableCovariateB
					= (AdjustableCovariate) covariatesB.get(i);
				if (adjustableCovariateA.hasIdenticalContents(adjustableCovariateB) == false) {
					return false;
				}
			}
			else {
				//assume it's exposure covariate
				ExposureCovariate exposureCovariateA
					= (ExposureCovariate) genericCovariateA;
				ExposureCovariate exposureCovariateB
					= (ExposureCovariate) covariatesB.get(i);
				if (exposureCovariateA.hasIdenticalContents(exposureCovariateB) == false) {
					return false;
				}				
			}
		}
	
		return true;		
	}
	
	/**
	 * Sort covariates.
	 *
	 * @param covariates the covariates
	 * @return the array list
	 */
	private static ArrayList<AbstractCovariate> sortCovariates(
		final ArrayList<AbstractCovariate> covariates) {
		DisplayableItemSorter sorter = new DisplayableItemSorter();
			
		for (AbstractCovariate covariate : covariates) {
			sorter.addDisplayableListItem(covariate);
		}
			
		ArrayList<AbstractCovariate> results = new ArrayList<AbstractCovariate>();
		ArrayList<String> identifiers = sorter.sortIdentifiersList();
		for (String identifier : identifiers) {
			AbstractCovariate sortedCovariate 
				= (AbstractCovariate) sorter.getItemFromIdentifier(identifier);
			results.add(sortedCovariate);
		}
			
		return results;
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
