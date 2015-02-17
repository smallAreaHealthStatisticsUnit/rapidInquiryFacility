
package rifJobSubmissionTool.desktop.interactive;

import rifServices.businessConceptLayer.AbstractCovariate;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.AbstractListModel;


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


final class CovariatesListModel 
	extends AbstractListModel {

// ==========================================
// Section Constants
// ==========================================
	private static final long serialVersionUID = -1660315780084386811L;

// ==========================================
// Section Properties
// ==========================================
		

	//Data
	/** The covariates. */
	private ArrayList<AbstractCovariate> covariates;	
	/** The covariate from display name. */
	private HashMap<String, AbstractCovariate> covariateFromDisplayName;
    
	//GUI Components
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new covariates list model.
     */
	public CovariatesListModel() {
		
		covariates = new ArrayList<AbstractCovariate>();
		covariateFromDisplayName = new HashMap<String, AbstractCovariate>();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    /**
     * Clear list.
     */
	public void clearList() {
	
    	covariates.clear();
    	covariateFromDisplayName.clear();
    }
    
    /**
     * Gets the covariates.
     *
     * @return the covariates
     */
    public ArrayList<AbstractCovariate> getCovariates() {
    	
    	ArrayList<AbstractCovariate> results 
    		= new ArrayList<AbstractCovariate>();
    	results.addAll(covariates);
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

	/**
	 * Adds the covariates.
	 *
	 * @param covariatesToAdd the covariates to add
	 */
    public void addCovariates(
    	ArrayList<AbstractCovariate> covariatesToAdd) {

    	for (AbstractCovariate covariateToAdd : covariatesToAdd) {
			String displayName
				= covariateToAdd.getDisplayName();
			if (covariateFromDisplayName.containsKey(displayName) == false) {
				covariateFromDisplayName.put(displayName, covariateToAdd);
				covariates.add(covariateToAdd);
				int currentIndex = covariates.size() - 1;
				fireIntervalAdded(this, currentIndex, currentIndex);
			}			
		}		
	}
	
	/**
	 * Gets the covariate.
	 *
	 * @param index the index
	 * @return the covariate
	 */
	public AbstractCovariate getCovariate(
		int index) {

		AbstractCovariate covariate = covariates.get(index);
		return covariate;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(
		int index) {		

		AbstractCovariate covariate = covariates.get(index);
		return covariate.getDisplayName();
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {
		
		return covariates.size();
	}
	
	/**
	 * Delete covariates.
	 *
	 * @param indicesToDelete the indices to delete
	 */
	public void deleteCovariates(
		int[] indicesToDelete) {
		
		for (int i = 0; i < indicesToDelete.length; i++) {
			AbstractCovariate covariateToDelete
				= covariates.get(indicesToDelete[i]);
			covariates.remove(indicesToDelete[i]);
			covariateFromDisplayName.remove(covariateToDelete.getDisplayName());
		}
		
		//fire events to update the list
		for (int i = 0; i < indicesToDelete.length; i++) {
			fireIntervalRemoved(this, indicesToDelete[i], indicesToDelete[i]);
		}
	}	
}
