package rifServices.test.services;

import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;

import rifGenericLibrary.util.FieldValidationUtility;

/**
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

public abstract class AbstractHealthCodeProviderTestCase 
	extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	/** The icd10 health code taxonomy. */
	private HealthCodeTaxonomy icd10HealthCodeTaxonomy;
		
	/** The master health code. */
	private HealthCode masterHealthCode;
	
	private HealthCode masterChapter02HealthCode;
	private HealthCode masterC34HealthCode;
	private HealthCode masterC342HealthCode;
	
	private HealthCode validHealthCode;
	private HealthCode emptyHealthCode;
	private HealthCode nonExistentHealthCode;
	private HealthCode maliciousHealthCode;
	
	private String validNameSpace;
	private String emptyNameSpace;
	private String nonExistentNameSpace;
	private String maliciousNameSpace;
	
	private String validSearchText;
	private String emptySearchText;
	private String maliciousSearchText;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractHealthCodeProviderTestCase() {

		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();

		icd10HealthCodeTaxonomy
			= HealthCodeTaxonomy.newInstance(
				"ICD 10", 
				"ICD 10 terms",
				"icd10",
				"1.0");
		
		masterChapter02HealthCode = HealthCode.newInstance();
		masterChapter02HealthCode.setCode("Chapter 02");
		masterChapter02HealthCode.setDescription("Chapter 02; Neoplasms");
		masterChapter02HealthCode.setNameSpace("icd10");
		masterChapter02HealthCode.setTopLevelTerm(true);

		masterC34HealthCode = HealthCode.newInstance();
		masterC34HealthCode.setCode("C34");
		masterC34HealthCode.setDescription("malignant neoplasm of bronchus and lung");
		masterC34HealthCode.setNameSpace("icd10");
	
		masterC342HealthCode = HealthCode.newInstance();
		masterC342HealthCode.setCode("C342");
		masterC342HealthCode.setDescription("middle lobe, bronchus or lung");
		masterC342HealthCode.setNameSpace("icd10");

		validHealthCode = HealthCode.newInstance();
		validHealthCode.setCode("C34");
		validHealthCode.setDescription("malignant neoplasm of bronchus and lung");
		validHealthCode.setNameSpace("icd10");
		
		emptyHealthCode = HealthCode.newInstance();
		emptyHealthCode.setCode("");
		emptyHealthCode.setDescription("malignant neoplasm of bronchus and lung");
		emptyHealthCode.setNameSpace("icd10");

		nonExistentHealthCode = HealthCode.newInstance();
		nonExistentHealthCode
			= HealthCode.createCopy(masterC342HealthCode);
		nonExistentHealthCode.setCode("XYZ");
		nonExistentHealthCode.setNameSpace("icd10");
		
		maliciousHealthCode = HealthCode.newInstance();
		maliciousHealthCode.setCode(
			fieldValidationUtility.getTestMaliciousFieldValue());
		maliciousHealthCode.setDescription("malignant neoplasm of bronchus and lung");
		maliciousHealthCode.setNameSpace("icd10");

		validNameSpace = "icd10";
		emptyNameSpace = "";
		nonExistentNameSpace = "icxxy345";
		maliciousNameSpace = fieldValidationUtility.getTestMaliciousFieldValue();

		validSearchText = "main bronchus";
		emptySearchText = "";
		maliciousSearchText 
			= fieldValidationUtility.getTestMaliciousFieldValue();
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	protected HealthCodeTaxonomy cloneValidHealthCodeTaxonomy() {
		return HealthCodeTaxonomy.createCopy(icd10HealthCodeTaxonomy);
	}
	
	protected HealthCodeTaxonomy cloneEmptyHealthCodeTaxonomy() {
		HealthCodeTaxonomy healthCodeTaxonomy
			= HealthCodeTaxonomy.createCopy(icd10HealthCodeTaxonomy);
		healthCodeTaxonomy.setName("");
		return healthCodeTaxonomy;
	}
	
	protected HealthCodeTaxonomy cloneNonExistentHealthCodeTaxonomy() {		
		return HealthCodeTaxonomy.newInstance(
			"Pretend HealthCode Taxonomy",
			"Pretend stuff",
			"sahsu:pretendHealthCodeTaxonomy",
			"1.0");
	}
	
	protected HealthCodeTaxonomy cloneMaliciousHealthCodeTaxonomy() {
		HealthCodeTaxonomy healthCodeTaxonomy
			= HealthCodeTaxonomy.createCopy(icd10HealthCodeTaxonomy);
		healthCodeTaxonomy.setName(getTestMaliciousValue());
		return healthCodeTaxonomy;
	}
	
	protected HealthCode cloneValidHealthCode() {
		return HealthCode.createCopy(validHealthCode);
	}
	
	protected HealthCode cloneEmptyHealthCode() {
		return HealthCode.createCopy(emptyHealthCode);
	}
	
	protected HealthCode cloneNonExistentHealthCode() {
		return HealthCode.createCopy(nonExistentHealthCode);
	}

	protected HealthCode cloneMaliciousHealthCode() {
		return HealthCode.createCopy(maliciousHealthCode);
	}
	
	protected String getValidNameSpace() {
		return validNameSpace;
	}
	
	protected String getEmptyNameSpace() {
		return emptyNameSpace;
	}
	
	protected String getNonExistentNameSpace() {
		return nonExistentNameSpace;
	}
	
	protected String getMaliciousNameSpace() {
		return maliciousSearchText;
	}
	
	protected String getValidSearchText() {
		return validSearchText;
	}
	
	protected String getEmptySearchText() {
		return emptySearchText;
	}
	
	protected String getMaliciousSearchText() {
		return maliciousSearchText;
	}

	protected HealthCode cloneChapter02HealthCode() {
		return HealthCode.createCopy(masterChapter02HealthCode);
	}
	
	protected HealthCode cloneC34HealthCode() {
		return HealthCode.createCopy(masterC34HealthCode);
	}
	
	protected HealthCode cloneC342HealthCode() {
		return HealthCode.createCopy(masterC342HealthCode);
	}
	
	protected HealthCodeTaxonomy cloneICD10HealthTaxonomy() {
		return HealthCodeTaxonomy.createCopy(icd10HealthCodeTaxonomy);
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
