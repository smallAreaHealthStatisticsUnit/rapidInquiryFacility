package org.sahsu.rif.services.test.services;

import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.concepts.HealthCodeTaxonomy;

public class CommonHealthCodeProviderTestCase extends CommonRIFServiceTestCase {

	/** The icd10 health code taxonomy. */
	private HealthCodeTaxonomy icd10HealthCodeTaxonomy;

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

	private String validSearchText;
	private String emptySearchText;
	private String maliciousSearchText;
	
	public CommonHealthCodeProviderTestCase() {

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

		validSearchText = "main bronchus";
		emptySearchText = "";
		maliciousSearchText 
			= fieldValidationUtility.getTestMaliciousFieldValue();
		
	}

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
}
