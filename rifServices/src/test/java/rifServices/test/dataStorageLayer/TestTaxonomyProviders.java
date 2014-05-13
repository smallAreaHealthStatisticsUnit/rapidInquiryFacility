package rifServices.test.dataStorageLayer;

import rifServices.ProductionRIFJobSubmissionService;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.Parameter;
import rifServices.businessConceptLayer.DisplayableListItem;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.taxonomyServices.HealthCodeProvider;
import rifServices.taxonomyServices.RIFXMLTaxonomyProvider;
import rifServices.util.DisplayableItemSorter;
import rifServices.test.AbstractRIFTestCase;
import static org.junit.Assert.*;

import org.junit.Test;

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

public class TestTaxonomyProviders extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The admin user. */
	private User adminUser;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test taxonomy providers.
	 */
	public TestTaxonomyProviders() {
		adminUser = User.newInstance("keving", "11.111.11.228");
	}

	/**
	 * Creates the test service.
	 *
	 * @return the test service
	 * @throws RIFServiceException the RIF service exception
	 */
	private TestService createTestService() throws RIFServiceException {
		TestService service = new TestService();
		service.login("keving", new String("a").toCharArray());			
				
		RIFXMLTaxonomyProvider icdTaxonomyProvider = new RIFXMLTaxonomyProvider();
		ArrayList<Parameter> parameters = new ArrayList<Parameter>();
		Parameter inputFileParameter = Parameter.newInstance();
		inputFileParameter.setName("input_file");
		inputFileParameter.setValue("C://icd10_taxonomy//ExampleICD10Codes.xml");
		parameters.add(inputFileParameter);
		icdTaxonomyProvider.initialise(parameters);

		service.clearHealthCodeProviders(adminUser);
		service.addHealthCodeProvider(adminUser, icdTaxonomyProvider);	
		
		return service;
	}
	
	/**
	 * Reject health code provider with no input file.
	 */
	@Test
	public void rejectHealthCodeProviderWithNoInputFile() {
		
		try {
			TestService service = new TestService();
			service.login("keving", new String("a").toCharArray());			
					
			RIFXMLTaxonomyProvider icdTaxonomyProvider = new RIFXMLTaxonomyProvider();
			ArrayList<Parameter> parameters = new ArrayList<Parameter>();
			Parameter inputFileParameter = Parameter.newInstance();
			parameters.add(inputFileParameter);
			icdTaxonomyProvider.initialise(parameters);
			fail();		
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.XML_TAXONOMY_READER_NO_INPUT_FILE_SPECIFIED, 
				1);
		}
	}

	
	/**
	 * Obtain taxonomy information.
	 */
	public void obtainTaxonomyInformation() {
		try {
			
			TestService testService = createTestService();
			try {
				
			ArrayList<HealthCodeTaxonomy> taxonomies
				= testService.getHealthCodeTaxonomies(adminUser);
			
			HealthCodeTaxonomy icd10HealthCodeTaxonomy
				= taxonomies.get(0);
			assertEquals("OurICD10Provider", icd10HealthCodeTaxonomy.getName());
			assertEquals("Provides a few ICD10 codes.", icd10HealthCodeTaxonomy.getDescription());
			assertEquals("icd10", icd10HealthCodeTaxonomy.getNameSpace());
			assertEquals("1.0", icd10HealthCodeTaxonomy.getVersion());
			}
			catch(RIFServiceException rifServiceException) {
				fail();
			}

		}
		catch(RIFServiceException rifServiceException) {
			printErrors("obtainTopLevelCodes", rifServiceException);
			fail();			
		}				
	}
	
	/**
	 * Obtain top level codes.
	 */
	@Test 
	public void obtainTopLevelCodes() {
		try {
			
			TestService testService = createTestService();
						
			ArrayList<HealthCodeTaxonomy> healthCodeTaxonomies
				= testService.getHealthCodeTaxonomies(adminUser);
			HealthCodeTaxonomy icd10Taxonomy = healthCodeTaxonomies.get(0);
			
			ArrayList<DisplayableListItem> results
				= sort(testService.getTopLevelCodes(adminUser, icd10Taxonomy));
			
			int actualNumberTopLevelCodes = results.size();
			assertEquals(2, actualNumberTopLevelCodes);
			
			HealthCode chapter02HealthCode = (HealthCode) results.get(0);
			assertEquals("Chapter 02", chapter02HealthCode.getCode());

			HealthCode chapter10HealthCode = (HealthCode) results.get(1);
			assertEquals("Chapter 10", chapter10HealthCode.getCode());

		}
		catch(RIFServiceException rifServiceException) {
			printErrors("obtainTopLevelCodes", rifServiceException);
			fail();			
		}		
	}
	
	/**
	 * Sort.
	 *
	 * @param healthCodes the health codes
	 * @return the array list
	 */
	private ArrayList<DisplayableListItem> sort(ArrayList<HealthCode> healthCodes) {
		DisplayableItemSorter sorter = new DisplayableItemSorter();
		for (HealthCode healthCode : healthCodes) {
			sorter.addDisplayableListItem(healthCode);
		}
		return sorter.sortList();		
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


class TestService extends ProductionRIFJobSubmissionService {
	public void addHealthCodeProvider(
		final User adminUser,
		final HealthCodeProvider healthCodeProvider) 
		throws RIFServiceException {
		super.addHealthCodeProvider(adminUser, healthCodeProvider);
	}
	
	public void clearHealthCodeProviders(
		final User adminUser) 
		throws RIFServiceException {
		super.clearHealthCodeProviders(adminUser);
	}
	
	public void getHealthCodeProvider (
		final User adminUser,
		final String nameSpace) 
		throws RIFServiceException {
		super.clearHealthCodeProviders(adminUser);
	}
	
	
}

