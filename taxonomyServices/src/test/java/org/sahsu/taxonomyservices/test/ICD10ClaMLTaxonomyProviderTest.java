package org.sahsu.taxonomyservices.test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;
import org.sahsu.taxonomyservices.ICD10TaxonomyTermParser;
import org.sahsu.taxonomyservices.TaxonomyTermManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;



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

public class ICD10ClaMLTaxonomyProviderTest {

	private File inputFile;
	private ICD10TaxonomyTermParser icd10TaxonomyFileReader;
	private TaxonomyTermManager taxonomyTermManager;
	
	public ICD10ClaMLTaxonomyProviderTest() {
		icd10TaxonomyFileReader = new ICD10TaxonomyTermParser();
	}
   
	@Before
	public void setUp() throws RIFServiceException, URISyntaxException {

		ClassLoader loader = getClass().getClassLoader();
		URL url = Objects.requireNonNull(loader.getResource("ExampleClaMLICD10Codes.xml"));
		inputFile = Paths.get(url.toURI()).toFile();

		icd10TaxonomyFileReader.readFile(inputFile);
		taxonomyTermManager = icd10TaxonomyFileReader.getTaxonomyTermManager();
	}

	/**
	 * Test
	 * @throws RIFServiceException
	 */
	@Test
	public void testGetTopLevelCodes_COMMON() throws RIFServiceException{
		//We know saushland_cancer only use ONE chapter, which is chapter II.
		
		TaxonomyTerm actualRootTerm 
			= taxonomyTermManager.getRootTerms().get(0);	
		
		assertEquals(
			actualRootTerm.getLabel(), 
			"II");

		assertEquals("Neoplasms", actualRootTerm.getDescription().trim());
	}
	
	/**
	 * Test
	 * @throws RIFServiceException
	 */
	@Test
	public void getMatchingTerms_COMMON() throws RIFServiceException{
		
		//Null should return an empty set of terms
		assertEquals(
			taxonomyTermManager.getMatchingTerms(null, true).size(),
			0);
		assertEquals(
			taxonomyTermManager.getMatchingTerms(null, false).size(),
			0);
		
		//Check that search string specifying non-existent 
		//The sample data doesn't contain codes starting from 'D'.
		//Then the search should return null.
		assertEquals(
			taxonomyTermManager.getMatchingTerms("D2", true).size(),
			0);
		
		//The sample data doesn't contain codes like 'Zec'.
		assertEquals(
			taxonomyTermManager.getMatchingTerms("Zec", false).size(),
			0);
		
		List<TaxonomyTerm> c22Terms
			= taxonomyTermManager.getMatchingTerms("C22", true);
				
		//Test if it returns the right answer when searching the code with "C2".
		assertEquals(
			c22Terms.size(), 
			5);

		assertEquals(
			TaxonomyTerm.hasTermMatchingLabel(c22Terms, "C22"),
			true);

		assertEquals(
			TaxonomyTerm.hasTermMatchingLabel(c22Terms, "C220"), 
			true);


		assertEquals(
			TaxonomyTerm.hasTermMatchingLabel(c22Terms, "C221"), 
			true);

		assertEquals(
			TaxonomyTerm.hasTermMatchingLabel(c22Terms, "C223"), 
			true);
		
		assertEquals(
			TaxonomyTerm.hasTermMatchingLabel(c22Terms, "C229"), 
			true);
		

		//check case sensitivity -- there should be no terms whose
		//labels use a lower case
		c22Terms
			= taxonomyTermManager.getMatchingTerms("c22", true);
		assertEquals(
			c22Terms.size(), 
			0);
		
		//now turn sensitivity off, we should get five terms again
		c22Terms
			= taxonomyTermManager.getMatchingTerms("c22", false);
		assertEquals(
			c22Terms.size(), 
			5);
		
	}
	
	/**
	 * Test
	 * @throws RIFServiceException
	 */
	@Test
	public void getTaxonomyTerm_COMMON() throws RIFServiceException{
		
		//If the taxonomy provider doesn't contain the code a user is interested,
		//it should return null.
		assertNull(taxonomyTermManager.getTerm("10010"));
		assertNull(taxonomyTermManager.getTerm(null));
			
		assertNotNull(taxonomyTermManager.getTerm("C22"));
		assertNotNull(taxonomyTermManager.getTerm("C710"));
		assertNull(taxonomyTermManager.getTerm("c710"));
	}
	
	/**
	 * Test
	 * @throws RIFServiceException
	 */
	
	@Test
	public void getImmediateSubterms_COMMON() throws RIFServiceException{
		/*
		 * II
		 * ---> C00-C97
		 *      ---> C00-C75
		 *           ---> C15-C26
		 *                C30-C39
		 *                C50-C50
		 *                C64-C68
		 *                C69-C72 
		 */
		
		//Passing null should result in no results
		assertEquals(
			taxonomyTermManager.getImmediateChildTerms(null).size(),
			0);
		
		List<TaxonomyTerm> chapter2Children
			= taxonomyTermManager.getImmediateChildTerms("II");
		assertEquals(
			chapter2Children.size(),
			1);	
		assertEquals(
			TaxonomyTerm.hasTermMatchingLabel(chapter2Children, "C00-C97"),
			true);
			
		List<TaxonomyTerm> c00c97Children
			= taxonomyTermManager.getImmediateChildTerms("C00-C97");
		assertEquals(1, c00c97Children.size());
		assertEquals(
			true,
			TaxonomyTerm.hasTermMatchingLabel(
				c00c97Children, 
				"C00-C75"));
		
		List<TaxonomyTerm> c00c75Children
			= taxonomyTermManager.getImmediateChildTerms("C00-C75");
		assertEquals(
			5,
			c00c75Children.size());
		assertEquals(
			true,
			TaxonomyTerm.hasTermMatchingLabel(
				c00c75Children, 
				"C15-C26"));

		assertEquals(
			true,
			TaxonomyTerm.hasTermMatchingLabel(
				c00c75Children, 
				"C30-C39"));
		assertEquals(
			true,
			TaxonomyTerm.hasTermMatchingLabel(
				c00c75Children, 
				"C50-C50"));
		assertEquals(
			true,
			TaxonomyTerm.hasTermMatchingLabel(
				c00c75Children, 
				"C64-C68"));
		assertEquals(
			true,
			TaxonomyTerm.hasTermMatchingLabel(
				c00c75Children, 
				"C69-C72"));

		//Now go for a leaf item that will have no children
		List<TaxonomyTerm> c717Children
			= taxonomyTermManager.getImmediateChildTerms("C717");

		for (TaxonomyTerm term : c717Children) {
			System.out.println("zzzzTerm=="+term.getLabel()+"==");
			
		}
				
		assertEquals(
			0,
			c717Children.size());		
	}
	
	/**
	 * Test
	 *     {@link ICD10TaxonomyTermParser#getParentTerm(TaxonomyTerm)}.
	 *     
	 * @throws RIFServiceException
	 */
	@Test
	public void getParentTaxonomyTerm_COMMON() throws RIFServiceException{
		
		//null value should return a null parent
		TaxonomyTerm parentTerm
			= taxonomyTermManager.getParentTerm(null);
		assertNull(parentTerm);

		//root term should return a null parent
		parentTerm
			= taxonomyTermManager.getParentTerm("II");
		assertNull(parentTerm);
		
		
		/*
		 * II
		 * ---> C00-C97
		 *      ---> C00-C75
		 *           ---> C15-C26
		 *                C30-C39
		 *                C50-C50
		 *                C64-C68
		 *                C69-C72 
		 */		
		
		parentTerm
			= taxonomyTermManager.getParentTerm("C00-C97");
		assertEquals(
			parentTerm.hasMatchingLabel("II"),
			true);
				
		parentTerm
			= taxonomyTermManager.getParentTerm("C00-C75");
		assertEquals(
			parentTerm.hasMatchingLabel("C00-C97"),
			true);

		parentTerm
			= taxonomyTermManager.getParentTerm("C15-C26");
		assertEquals(
			parentTerm.hasMatchingLabel("C00-C75"),
			true);		
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
