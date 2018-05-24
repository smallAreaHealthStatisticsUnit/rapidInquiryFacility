package org.sahsu.rif.services.test.services.ms;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.ontologyservices.ICD10ClaMLTaxonomyProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public class ICD10ClaMLTaxonomyProviderTest {
	private ICD10ClaMLTaxonomyProvider icd10provider;

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public ICD10ClaMLTaxonomyProviderTest() {
		icd10provider = new ICD10ClaMLTaxonomyProvider();
	}
   
	@Before
	public void setUp() throws RIFServiceException{
		
		ArrayList<Parameter> parameters = new ArrayList<Parameter>();
		Parameter inputFileParameter = Parameter.newInstance();
		inputFileParameter.setName("icd10_ClaML_file");
		//Need a better way to set the file location
		inputFileParameter.setValue("src/main/resources/ExampleClaMLICD10Codes.xml");
		
		//System.out.println(this.getClass().getResource("../../../../../main/resources/ExampleClaMLICD10Codes.xml").getPath());		
		//inputFileParameter.setValue("../../../../../main/resources/ExampleClaMLICD10Codes.xml");
		parameters.add(inputFileParameter);
		
		icd10provider.initialise(parameters);
	}
	
	/**
	 * Test
	 *     {@link ICD10ClaMLTaxonomyProvider#getTopLevelCodes()}.
	 * @throws RIFServiceException
	 */
	@Test
	public void testGetTopLevelCodes() throws RIFServiceException{
		//We know saushland_cancer only use ONE chapter, which is chapter II.
		HealthCode whoChapterII = HealthCode.newInstance();
		whoChapterII.setCode("II");
		whoChapterII.setDescription("Neoplasms");
		whoChapterII.setNameSpace("icd10");
		whoChapterII.setTopLevelTerm(true);
		//Only ONE chapter is used.
		whoChapterII.setNumberOfSubTerms(1);
		
		HealthCode expectedTopLevelCodeInSampleDataset = icd10provider.getTopLevelCodes().get(0);
		
		assertEquals(expectedTopLevelCodeInSampleDataset.getCode(), whoChapterII.getCode());
		assertEquals(expectedTopLevelCodeInSampleDataset.getNumberOfSubTerms(), whoChapterII.getNumberOfSubTerms());
		assertEquals(expectedTopLevelCodeInSampleDataset.getDescription(), whoChapterII.getDescription());
		assertEquals(expectedTopLevelCodeInSampleDataset.getNameSpace(), whoChapterII.getNameSpace());
		assertTrue(expectedTopLevelCodeInSampleDataset.isTopLevelTerm());
		assertEquals(expectedTopLevelCodeInSampleDataset.getDisplayName(), whoChapterII.getDisplayName());
		assertEquals(expectedTopLevelCodeInSampleDataset.getIdentifier(), whoChapterII.getIdentifier());
		assertEquals(expectedTopLevelCodeInSampleDataset.getRecordType(), whoChapterII.getRecordType());
		
		//Code below won't work as HealthCode not override equals and hashCode methods.
		
		//assertEquals(whoChapterII, icd10provider.getTopLevelCodes().get(0));
	}
	
	/**
	 * Test
	 *     {@link ICD10ClaMLTaxonomyProvider#getHealthCodes()}.
	 * @throws RIFServiceException
	 */
	@Test
	public void testGetHealthCodes() throws RIFServiceException{
		//If try to find "null" in the sample data, it returns null.
		assertNull(icd10provider.getHealthCodes(null, true));
		assertNull(icd10provider.getHealthCodes(null, false));
		
		//The sample data doesn't contain codes starting from 'D'.
		//Then the search should return null.
		assertNull(icd10provider.getHealthCodes("D2", true));
		
		//The sample data doesn't contain codes like 'Zec'.
		assertNull(icd10provider.getHealthCodes("Zec", false));
		
		//The following code labels are used in saushland_cancer. Other 
		//details such as description are ignored.
		String C220 = "C220";
		String C221 = "C221";
		String C223 =  "C223";
		String C229 =  "C229";
		
		//The above four codes are children of the code below:
		String C22 =  "C22";
		
		//The code of C22 is one children of the code below:
		String C15_C26 = "C15-C26";
	
		HashSet<String> codesInSaushland =new HashSet<String>();
		codesInSaushland.add(C220);
		codesInSaushland.add(C221);
		codesInSaushland.add(C223);
		codesInSaushland.add(C229);
		codesInSaushland.add(C22);
		codesInSaushland.add(C15_C26);
		
		//Test if it returns the right answer when searching the code with "C2".
		assertEquals(icd10provider.getHealthCodes("C2", false).size(), codesInSaushland.size());
		for(HealthCode code :icd10provider.getHealthCodes("C2", false)){
			String label =code.getCode();
			assertTrue(codesInSaushland.contains(label));
			codesInSaushland.remove(label);
		}
		
		codesInSaushland.add(C220);
		codesInSaushland.add(C221);
		codesInSaushland.add(C223);
		codesInSaushland.add(C229);
		codesInSaushland.add(C22);
		//Test if it returns the right answer when searching the code with "C22".
		assertEquals(icd10provider.getHealthCodes("C22", false).size(), codesInSaushland.size());
		for(HealthCode code :icd10provider.getHealthCodes("C22", false)){
			String label =code.getCode();
			assertTrue(codesInSaushland.contains(label));
			codesInSaushland.remove(label);
		}
		
		//Case sensitive test
		assertNull(icd10provider.getHealthCodes("c2", true));
		
		String C710 = "C710";
		assertEquals(icd10provider.getHealthCodes("C710", false).get(0).getCode(), C710);
		
		String c710 = "c710";
		assertNull(icd10provider.getHealthCodes("c710", true));
		
		assertEquals(icd10provider.getHealthCodes("c710", false).get(0).getCode(), c710.toUpperCase());
		
		String chapterTwo ="II";
		assertEquals(icd10provider.getHealthCodes("II", true).get(0).getCode(), chapterTwo);		
	}
	
	/**
	 * Test
	 *     {@link ICD10ClaMLTaxonomyProvider#getHealthCode()}.
	 * @throws RIFServiceException
	 */
	@Test
	public void testGetHealthCode() throws RIFServiceException{
		String C22 =  "C22";
		String nameSpace ="icd9";
		
		//If the taxonomy provider doesn't contain the code a user is interested,
		//it should return null.
		assertNull(icd10provider.getHealthCode("10010", nameSpace));
		assertNull(icd10provider.getHealthCode(C22, nameSpace));
		assertNull(icd10provider.getHealthCode(C22, null));
		assertNull(icd10provider.getHealthCode(null, "icd10"));
		assertNull(icd10provider.getHealthCode("10010", "icd10"));
				
		assertEquals(icd10provider.getHealthCode(C22, "icd10").getCode(), C22);
		assertEquals(icd10provider.getHealthCode("C710", "icd10").getCode(), "C710");
		assertNull(icd10provider.getHealthCode("c710", "icd10"));
	}
	
	/**
	 * Test
	 *     {@link ICD10ClaMLTaxonomyProvider#getImmediateSubterms(HealthCode)}.
	 * @throws RIFServiceException
	 */
	@Test
	public void testGetImmediateSubterms() throws RIFServiceException{
		assertNull(icd10provider.getImmediateSubterms(null));
		HealthCode whoChapterII = HealthCode.newInstance();
		whoChapterII.setCode("II");
		whoChapterII.setNameSpace("icd10");
		whoChapterII.setNumberOfSubTerms(1);
		
		HealthCode C00_C97 = HealthCode.newInstance();
		C00_C97.setCode("C00-C97");
		
		//Chapter two has only one child in the sample data.
		assertEquals(icd10provider.getImmediateSubterms(whoChapterII).size(), 1);
		assertEquals(icd10provider.getImmediateSubterms(whoChapterII).get(0).getCode(), C00_C97.getCode());
		
		HealthCode C22= HealthCode.newInstance();
		C22.setCode("C22");
		C22.setNameSpace("icd10");
		C22.setNumberOfSubTerms(4);
		
		String C220= "C220";
		String C221= "C221";
		String C223= "C223";
		String C229= "C229";

		HashSet<String> codes =new HashSet<String>();
		codes.add(C220);
		codes.add(C221);
		codes.add(C223);
		codes.add(C229);
		assertEquals(icd10provider.getImmediateSubterms(C22).size(), codes.size());
		for(HealthCode code : icd10provider.getImmediateSubterms(C22)){
			assertTrue(codes.contains(code.getCode()));
		}
		
		HealthCode healthcode = HealthCode.newInstance();
		healthcode.setCode(C221);
		healthcode.setNameSpace("icd10");
		//Leaf code has no child
		assertNull(icd10provider.getImmediateSubterms(healthcode));	
		healthcode.setCode(C229);
		assertNull(icd10provider.getImmediateSubterms(healthcode));
		
		//A code doesn't exist in the sample data, return null.
		healthcode.setCode("C15");
		assertNull(icd10provider.getImmediateSubterms(healthcode));		
	}
	
	/**
	 * Test
	 *     {@link ICD10ClaMLTaxonomyProvider#getParentHealthCode(HealthCode)}.
	 *     
	 * @throws RIFServiceException
	 */
	@Test
	public void testGetParentHealthCode() throws RIFServiceException{
		HealthCode whoChapterII = HealthCode.newInstance();
		whoChapterII.setCode("II");
		whoChapterII.setNameSpace("icd10");
		whoChapterII.setNumberOfSubTerms(1);
		//whoChapterII.setTopLevelTerm(true);

		//A top level code has no parents.
		assertNull(icd10provider.getParentHealthCode(whoChapterII));
		assertNull(icd10provider.getParentHealthCode(null));
		
		HealthCode healthcode= HealthCode.newInstance();
		healthcode.setCode("C229");
		healthcode.setNameSpace("icd10");
		healthcode.setNumberOfSubTerms(0);
		assertEquals(icd10provider.getParentHealthCode(healthcode).getCode(), "C22");
		
		healthcode.setCode("C15-C26");
		assertEquals(icd10provider.getParentHealthCode(healthcode).getCode(), "C00-C75");
		
		healthcode.setCode("C00-C75");
		assertEquals(icd10provider.getParentHealthCode(healthcode).getCode(), "C00-C97");
		
		healthcode.setCode("D11");
		assertNull(icd10provider.getParentHealthCode(healthcode));
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
