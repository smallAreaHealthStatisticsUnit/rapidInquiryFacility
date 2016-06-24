package rifServices.ontologyServices;

import rifGenericLibrary.businessConceptLayer.Parameter;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;

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

public interface HealthCodeProviderInterface {
	
	/**
	 * Initialise.
	 *
	 * @param parameters the parameters
	 * @throws RIFServiceException the RIF service exception
	 */
	public void initialise(
		final ArrayList<Parameter> parameters) 
		throws RIFServiceException;
	
	/**
	 * Gets the health code taxonomy.
	 *
	 * @return the health code taxonomy
	 */
	public HealthCodeTaxonomy getHealthCodeTaxonomy();
	
	/**
	 * Supports taxonomy.
	 *
	 * @param healthCodeTaxonomy the health code taxonomy
	 * @return true, if successful
	 */
	public boolean supportsTaxonomy(
		final HealthCodeTaxonomy healthCodeTaxonomy);
	
	/**
	 * Supports taxonomy.
	 *
	 * @param healthCode the health code
	 * @return true, if successful
	 */
	public boolean supportsTaxonomy(
		final HealthCode healthCode);

	
	
	/**
	 * Gets the health codes.
	 *
	 * @param connection the connection
	 * @param searchText the search text
	 * @return the health codes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCode> getHealthCodes(
		final String searchText,
		final boolean isCaseSensitive)
		throws RIFServiceException;
	
	/**
	 * Gets the top level codes.
	 *
	 * @param connection the connection
	 * @return the top level codes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCode> getTopLevelCodes() 
		throws RIFServiceException;
	
	/**
	 * Gets the immediate subterms.
	 *
	 * @param connection the connection
	 * @param parentHealthCode the parent health code
	 * @return the immediate subterms
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<HealthCode> getImmediateSubterms(
		final HealthCode parentHealthCode) 
		throws RIFServiceException;

	/**
	 * Gets the parent health code.
	 *
	 * @param connection the connection
	 * @param childHealthCode the child health code
	 * @return the parent health code
	 * @throws RIFServiceException the RIF service exception
	 */
	public HealthCode getParentHealthCode(
		final HealthCode childHealthCode) 
		throws RIFServiceException;
	
	public HealthCode getHealthCode(
		final String code,
		final String nameSpace) 
		throws RIFServiceException;
	
	public boolean healthCodeExists(
		final HealthCode healthCode)
		throws RIFServiceException;
}
