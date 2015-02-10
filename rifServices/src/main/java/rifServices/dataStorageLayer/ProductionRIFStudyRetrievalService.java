package rifServices.dataStorageLayer;


/**
 * The purpose of this service is to provide a secure implementation of 
 * {@link rifServices.dataStorageLayer.RIFStudyResultRetrievalAPI} which
 * is final - meaning that it cannot be overridden by the client.  
 * 
 * <p>
 * The main reason for having this class is to help 'close off' any protected 
 * method that might generate a security concern or might only serve a testing 
 * purpose.
 * </p>
 * 
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
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

//TOUR_CONCURRENCY
/**
 * This class is the version of the retrieval service that is used to support
 * production rather than testing purposes.  It is a final class to ensure that
 * it cannot be sub-classed in ways that would create concurrency problems.
 */
//TOUR_SECURITY
/**
 * <p>
 * The class is declared final to prevent a malicious client application to submit a subclass
 * that overrides methods to produce harmful effects.  It should be visible only within this 
 * package.  It is made available through {@link rifServices.dataStorageLayer.ProductionRIFStudyServiceBundle},
 * but that class only retrieves it as an instance of {@link rifServices.businessConceptLayer.RIFStudyResultRetrievalAPI}.
 * Client code cannot cast the RIFStudyResultRetrievalAPI to a ProductionRIFStudySubmissionService because
 * this class is not visible outside the package.  Therefore, the client would not be able to use
 * casting as a way of getting access to more of a class's methods than are advertised through the 
 * interface it implements.
 * </p>
 *  
 */
final class ProductionRIFStudyRetrievalService 
	extends AbstractRIFStudyRetrievalService {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public ProductionRIFStudyRetrievalService() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
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
