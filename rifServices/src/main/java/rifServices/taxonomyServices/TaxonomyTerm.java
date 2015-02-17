package rifServices.taxonomyServices;

import java.util.ArrayList;


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

final class TaxonomyTerm {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The label. */
	private String label;
	
	/** The name space. */
	private String nameSpace;
	
	/** The description. */
	private String description;
	
	/** The parent term. */
	private TaxonomyTerm parentTerm;
	
	/** The sub terms. */
	private ArrayList<TaxonomyTerm> subTerms;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new taxonomy term.
	 */
	private TaxonomyTerm() {	
		
		subTerms = new ArrayList<TaxonomyTerm>();
	}

	/**
	 * New instance.
	 *
	 * @return the taxonomy term
	 */
	public static TaxonomyTerm newInstance() {
		
		TaxonomyTerm taxonomyTerm = new TaxonomyTerm();
		return taxonomyTerm;
	}
	
	/**
	 * Creates the shallow copy.
	 *
	 * @param originalTerm the original term
	 * @return the taxonomy term
	 */
	public static TaxonomyTerm createShallowCopy(
		final TaxonomyTerm originalTerm) {

		TaxonomyTerm copyTerm = new TaxonomyTerm();
		
		copyTerm.setLabel(originalTerm.getLabel());
		copyTerm.setDescription(originalTerm.getDescription());
		copyTerm.setNameSpace(originalTerm.getNameSpace());
		
		return copyTerm;
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Adds the sub term.
	 *
	 * @param subTerm the sub term
	 */
	public void addSubTerm(
		final TaxonomyTerm subTerm) {

		subTerms.add(subTerm);
	}

	/**
	 * Adds the sub terms.
	 *
	 * @param subTerms the sub terms
	 */
	public void addSubTerms(
		final ArrayList<TaxonomyTerm> subTerms) {

		this.subTerms.addAll(subTerms);
	}
	
	/**
	 * Gets the sub terms.
	 *
	 * @return the sub terms
	 */
	public ArrayList<TaxonomyTerm> getSubTerms() {
		
		return subTerms;
	}
	
	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label the new label
	 */
	public void setLabel(
		final String label) {
		
		this.label = label;
	}

	/**
	 * Gets the name space.
	 *
	 * @return the name space
	 */
	public String getNameSpace() {
		
		return nameSpace;
	}

	/**
	 * Sets the name space.
	 *
	 * @param nameSpace the new name space
	 */
	public void setNameSpace(
		final String nameSpace) {

		this.nameSpace = nameSpace;
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
	public void setDescription(final String description) {
		
		this.description = description;
	}
	
	/**
	 * Gets the parent term.
	 *
	 * @return the parent term
	 */
	public TaxonomyTerm getParentTerm() {
		
		return parentTerm;
	}
	
	/**
	 * Sets the parent term.
	 *
	 * @param parentTerm the new parent term
	 */
	public void setParentTerm(
		final TaxonomyTerm parentTerm) {
		
		this.parentTerm = parentTerm;
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
