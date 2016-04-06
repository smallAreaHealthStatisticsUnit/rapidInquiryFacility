package rifDataLoaderTool.businessConceptLayer;


import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.util.FieldValidationUtility;
import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


/**
 * The base class for classes which implement the {@link rifDataLoaderTool.businessConceptLayer.RIFDataTypeInterface} 
 * interface. The RIF Data Loader Tool recognises a hierarchy of Data Types it associates with table columns 
 * in the data sources (see @link rifDataLoaderTool.businessConceptLayer.dataSet}).  By associating a RIF 
 * data type with each column, the Data Loader Tool is able to apply default cleaning behaviour and default 
 * validation checks.  
 * 
 * <p>
 * We have a number of design challenges in creating a collection of types.  First, the collection of types 
 * must be necessary and sufficient to cope with the needs of both SAHSU and of its external partners.  Although our
 * needs are fairly clear, we are still trying to anticipate the data management needs of our partners.  
 * 
 * <p>
 * Second, we have to be wary of how we minimise replicated code.  Many designers favour the use of aggregation 
 * over inheritance to reduce code duplication in a way that flexibly accommodates future changes.  
 * Code used for inheritance is often simpler to implement than code used to support a more complex interaction 
 * of aggregated classes.  However, the simplicity comes at the cost of developing methods which are not equally
 * relevant to sub classes.  It is the prospect that sub-classes may not 'fit' the provisions of the hierarchy that
 * tend to make inheritance more brittle in response to future changes.  
 * 
 * <p>
 * We have chosen to rely on inheritance rather than aggregation for creating data type classes.  
 * Our reasons include:
 * <ul>
 * <li>
 * the properties of data types are derived from long-running use cases that have exhibited 
 * little change over the years 
 * </li>
 * <li>
 * There is a significant number of features that all data types we've identified would tend to need
 * </li>
 * <li>
 * The simplicity of maintaining an inheritance hierarchy would help lower the skill level 
 * required to maintain the code
 * </li>
 * </ul>
 * 
 * <p>
 * The RIF data types will be advertised in the Data Loader Tool as a set of templates that can be 
 * parameterised (eg: setting two digit years associated with the 20th century or 21st century) or 
 * copied (eg: using properties of a RIFIntegerType to make a new type used for some kind of numeric
 * code).  
 * </p>
 * 
 * <p>
 * All RIF Data Types will have the following properties:
 * <ul>
 * <li>
 * <b>identifier</b>: uniquely identifies the type in a database table describing RIF data types 
 * (eg: rif_sex, ons_sex, hes_sex)
 * </li>
 * <li>
 * <b>name</b>: a human-readable name that will be displayed to RIF managers (eg: "RIF Sex")
 * </li>
 * <li>
 * <b>description</b>: a description that tells RIF manager the properties of the data type
 * </li>
 * <li>
 * <b>fieldValidationPolicy</b>: informs the code generator classes how to write SQL code that validates
 * field values.  The three policies are: do no validation; rely on a collection of regular expressions
 * that describe valid values; or delegate validation to a database function 
 * (eg: is_valid_uk_postal_code(fieldValue, areBlanksAllowed) )
 * </li>
 * <li>
 * <b>validationExpressions</b>: a collection of regular expressions which can be used to validate
 * a field value
 * </li>
 * <li>
 * <b>validationFunctionName</b>: the name of a function that can be used to validate the field.
 * (eg: is_valid_date(fieldValue, dateFormat)  )
 * </li>
 * <li>
 * <b>fieldCleaningPolicy</b>: the policy used to clean a field.  The options are: don't clean at all;
 * rely on a collection of cleaning rules (see: {@link rifDataLoaderTool.dataStorageLayer.CleaningRule};
 * rely on a cleaning function (eg: clean_uk_postal_code(fieldValue, allowBlankValues)     )
 * </li>
 * </ul>
 * 
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
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

public final class RIFDataType 
	implements DisplayableListItemInterface {

	// ==========================================
	// Section Constants
	// ==========================================
	public static final RIFDataType EMPTY_RIF_DATA_TYPE = new RIFDataType(false);
	
	// ==========================================
	// Section Properties
	// ==========================================
	private String identifier;
	private String name;
	private String description;
	
	private RIFFieldActionPolicy fieldValidationPolicy;
	private ArrayList<ValidationRule> validationRules;
	private String validationFunctionName;
	private String validationFunctionParameterPhrase;

	private RIFFieldActionPolicy fieldCleaningPolicy;	
	private String cleaningFunctionName;
	private String cleaningFunctionParameterPhrase;
	private ArrayList<CleaningRule> cleaningRules;

	private boolean isReservedDataType;
	// ==========================================
	// Section Construction
	// ==========================================

	private RIFDataType() {
		this.isReservedDataType = false;
		
		init(
			"", 
			"", 
			"");		
	}

	private RIFDataType(final boolean isMutable) {
		
		this.isReservedDataType = isMutable;
		
		init(
			"", 
			"", 
			"");		
	}
	
	private RIFDataType(
		final String identifier,
		final String name,
		final String description,
		final boolean isMutable) {
		
		this.isReservedDataType = isMutable;
		
		init(
			identifier, 
			name, 
			description);
	}

	private void init(
		final String identifier,
		final String name, 
		final String description) {
				
		
		this.identifier = identifier;
		
		this.name = name;
		this.description = description;

		fieldValidationPolicy = RIFFieldActionPolicy.DO_NOTHING;
		validationRules = new ArrayList<ValidationRule>();
		validationFunctionName = "";
		
		fieldCleaningPolicy = RIFFieldActionPolicy.DO_NOTHING;
		cleaningRules = new ArrayList<CleaningRule>();	
		cleaningFunctionName = "";
	}
	
	public static RIFDataType newInstance() {
		RIFDataType rifDataType 
			= new RIFDataType(
				"", 
				"", 
				"",
				false);		
			
		return rifDataType;
	}	
	
	public static RIFDataType newInstance(
		final String identifier,
		final String name,
		final String description,
		final boolean isMutable) {
		RIFDataType rifDataType 
			= new RIFDataType(
				identifier, 
				name, 
				description,
				isMutable);		
		
		return rifDataType;
	}
	
	public static RIFDataType createCopy(final RIFDataType originalDataType) {
		
		RIFDataType cloneType = new RIFDataType(originalDataType.isReservedDataType());
		return copyInto(originalDataType, cloneType);
	}
	
	public static RIFDataType copyInto(
		final RIFDataType source,
		final RIFDataType destination) {
				
		destination.setIdentifier(source.getIdentifier());
		destination.setName(source.getName());
		destination.setDescription(source.getDescription());

		destination.setFieldValidationPolicy(source.getFieldValidationPolicy());
		ArrayList<ValidationRule> sourceValidationRules
			= source.getValidationRules();
		ArrayList<ValidationRule> cloneValidationRules
			= new ArrayList<ValidationRule>();
		for (ValidationRule sourceValidationRule : sourceValidationRules) {
			ValidationRule cloneValidationRule
				= ValidationRule.createCopy(sourceValidationRule);
			cloneValidationRules.add(cloneValidationRule);
		}
		destination.setValidationRules(cloneValidationRules);
		destination.setValidationFunctionName(source.getValidationFunctionName());
		
		RIFFieldActionPolicy originalFieldCleaningPolicy
			= source.getFieldCleaningPolicy();
		destination.setFieldCleaningPolicy(originalFieldCleaningPolicy);
		
		ArrayList<CleaningRule> originalCleaningRules
			= source.getCleaningRules();
		ArrayList<CleaningRule> cloneCleaningRules
			= new ArrayList<CleaningRule>();
		for (CleaningRule originalCleaningRule : originalCleaningRules) {
			CleaningRule cloneCleaningRule
				= CleaningRule.createCopy(originalCleaningRule);
			cloneCleaningRules.add(cloneCleaningRule);
		}
		destination.setCleaningRules(cloneCleaningRules);
		destination.setCleaningFunctionName(source.getCleaningFunctionName());
	
		return destination;
	}

	public static final boolean hasIdenticalContents(
		final ArrayList<RIFDataType> rifDataTypeListA,
		final ArrayList<RIFDataType> rifDataTypeListB) {
		
		if (rifDataTypeListA == rifDataTypeListB) {
			return true;
		}
		
		if ((rifDataTypeListA == null) && (rifDataTypeListB != null) ||
			(rifDataTypeListA != null) && (rifDataTypeListB == null)) {
			
			return false;
		}
		
		if (rifDataTypeListA.size() != rifDataTypeListB.size()) {
			return false;
		}
			
		System.out.println("RIFDataType hasIdenticalContents 1");
		HashMap<String, RIFDataType> dataTypeFromNameA 
			= new HashMap<String, RIFDataType>();
		for (RIFDataType rifDataTypeA : rifDataTypeListA) {
			dataTypeFromNameA.put(rifDataTypeA.getDisplayName(), rifDataTypeA);
		}		
		
		HashMap<String, RIFDataType> dataTypeFromNameB
			= new HashMap<String, RIFDataType>();
		for (RIFDataType rifDataTypeB : rifDataTypeListB) {
			dataTypeFromNameA.put(rifDataTypeB.getDisplayName(), rifDataTypeB);
		}
		
		ArrayList<String> keysListA = new ArrayList<String>();
		keysListA.addAll(dataTypeFromNameA.keySet());
		ArrayList<String> keysListB = new ArrayList<String>();
		keysListB.addAll(dataTypeFromNameB.keySet());
		
		System.out.println("=========================================================");
		for (String keysListItemA : keysListA) {
			System.out.println("RIFDataType A=="+keysListItemA+"==");
		}
		System.out.println("=========================================================");
		for (String keysListItemB : keysListB) {
			System.out.println("RIFDataType B=="+keysListItemB+"==");
		}

		
		for (String keyA : keysListA) {
			RIFDataType rifDataTypeB
				= dataTypeFromNameB.get(keyA);
			if (rifDataTypeB == null) {
				System.out.println("RIFDataType hasIdenticalContents 2");

				return false;
			}
			
			RIFDataType rifDataTypeA
				= dataTypeFromNameA.get(keyA);
			if (rifDataTypeA.hasIdenticalContents(rifDataTypeB) == false) {
				System.out.println("RIFDataType hasIdenticalContents 3");
				return false;
			}
		}
		
		for (String keyB : keysListB) {
			RIFDataType rifDataTypeA
				= dataTypeFromNameA.get(keyB);
			if (rifDataTypeA == null) {
				System.out.println("RIFDataType hasIdenticalContents 4");
				return false;
			}
			
			RIFDataType rifDataTypeB
				= dataTypeFromNameB.get(keyB);
			if (rifDataTypeA.hasIdenticalContents(rifDataTypeB) == false) {
				System.out.println("RIFDataType hasIdenticalContents 5");

				return false;
			}
		}
		
		return true;
		
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(
		final String identifier) {

		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(
		final String name) {

		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public String getCleaningFunctionParameterPhrase() {
		return cleaningFunctionParameterPhrase;
	}
	
	public void setCleaningFunctionParameterPhrase(
		final String cleaningFunctionParameterPhrase) {

		this.cleaningFunctionParameterPhrase = cleaningFunctionParameterPhrase;
	}

	public String getValidationFunctionParameterPhrase() {
		return validationFunctionParameterPhrase;
	}
	
	public void setValidationFunctionParameterPhrase(
		final String validationFunctionParameterPhrase) {

		this.validationFunctionParameterPhrase = validationFunctionParameterPhrase;
	}

	public void setDescription(
		final String description) {

		this.description = description;
	}

	public RIFFieldActionPolicy getFieldValidationPolicy() {
		return fieldValidationPolicy;
	}

	public void setFieldValidationPolicy(
		final RIFFieldActionPolicy fieldValidationPolicy) {

		this.fieldValidationPolicy = fieldValidationPolicy;
	}
	
	public ArrayList<ValidationRule> getValidationRules() {
		return validationRules;
	}

	public void addValidationRule(
		final ValidationRule validationRule) {
		
		validationRules.add(validationRule);
	}
	
	public void setValidationRules(
		final ArrayList<ValidationRule> validationRules) {

		this.validationRules = validationRules;
	}

	public void clearValidationRules() {
		validationRules.clear();
	}
	
	public String getValidationFunctionName() {
		return validationFunctionName;
	}

	public void setValidationFunctionName(
		final String validationFunctionName) {

		this.validationFunctionName = validationFunctionName;
	}

	public RIFFieldActionPolicy getFieldCleaningPolicy() {
		return fieldCleaningPolicy;
	}

	public void setFieldCleaningPolicy(
		final RIFFieldActionPolicy fieldCleaningPolicy) {
		
		this.fieldCleaningPolicy = fieldCleaningPolicy;
	}

	public String getCleaningFunctionName() {
		return cleaningFunctionName;
	}

	public void setCleaningFunctionName(
		final String cleaningFunctionName) {

		this.cleaningFunctionName = cleaningFunctionName;
	}
	
	public void addCleaningRule(
		final CleaningRule cleaningRule) {

		cleaningRules.add(cleaningRule);
	}
	
	public void setCleaningRules(
		final ArrayList<CleaningRule> cleaningRules) {
		
		this.cleaningRules = cleaningRules;
	}

	public void clearCleaningRules() {
		
		cleaningRules.clear();
	}
	
	public ArrayList<CleaningRule> getCleaningRules() {

		return cleaningRules;
	}
	
	
	public String getCleaningFunctionParameterValues() {
		return "";
	}
	
	public String getValidationFunctionParameterValues() {
		return "";
	}

	public String getMainValidationValue() {
		if (validationRules.isEmpty()) {
			return null;
		}
		else {
			return validationRules.get(0).getValidValue();
		}		
	}
	

	public boolean hasIdenticalContents(
		final RIFDataType otherRIFDataType) {
		
		String otherIdentifier 
			= otherRIFDataType.getIdentifier();
		String otherName 
			= otherRIFDataType.getName();
		String otherDescription 
			= otherRIFDataType.getDescription();
		
		if (Objects.deepEquals(identifier, otherIdentifier) == false) {
			System.out.println("RDT - hasIdenticalContents 1");
			return false;
		}
		if (Objects.deepEquals(name, otherName) == false) {
			System.out.println("RDT - hasIdenticalContents 2");
			return false;
		}
		if (Objects.deepEquals(description, otherDescription) == false) {
			System.out.println("RDT - hasIdenticalContents 3");
			return false;
		}		
	 		
		RIFFieldActionPolicy otherFieldCleaningPolicy
			= otherRIFDataType.getFieldCleaningPolicy();
		if (fieldCleaningPolicy != otherFieldCleaningPolicy) {
			System.out.println("RDT - hasIdenticalContents 4");
			
			return false;
		}
		
		ArrayList<CleaningRule> otherCleaningRules
			= otherRIFDataType.getCleaningRules();
		System.out.println("RDT original=="+this.getDisplayName()+"==size=="+cleaningRules.size()+"==other=="+otherRIFDataType.getDisplayName()+"==size=="+otherRIFDataType.getDisplayName()+"==");
		if (CleaningRule.cleaningRulesAreEqual(
			cleaningRules, 
			otherCleaningRules) == false) {
			System.out.println("RDT - hasIdenticalContents 5 for ==" + otherRIFDataType.getDisplayName()+"==");
			
			return false;
		}
		

		
		String otherCleaningFunctionName
			= otherRIFDataType.getCleaningFunctionName();
		if (Objects.deepEquals(
			cleaningFunctionName, 
			otherCleaningFunctionName) == false) {
			
			System.out.println("RDT - hasIdenticalContents 6");

			return false;
		}		
		
		RIFFieldActionPolicy otherFieldValidationPolicy
			= otherRIFDataType.getFieldValidationPolicy();
		if (fieldValidationPolicy != otherFieldValidationPolicy) {
			
			System.out.println("RDT - hasIdenticalContents 7");

			return false;
		}
			
		ArrayList<ValidationRule> otherValidationRules
			= otherRIFDataType.getValidationRules();
		if (ValidationRule.validationRulesAreEqual(
			validationRules, 
			otherValidationRules) == false) {
			System.out.println("RDT - hasIdenticalContents 8");
		
			return false;
		}
		
		String otherValidationFunctionName
			= otherRIFDataType.getValidationFunctionName();
		if (Objects.deepEquals(
				validationFunctionName, 
				otherValidationFunctionName) == false) {
			System.out.println("RDT - hasIdenticalContents 9 validationFunc=="+validationFunctionName+"==other validation=="+otherValidationFunctionName+"==");

			return false;
		}		
				
		return true;		
	}
	
	public boolean isReservedDataType() {
		return isReservedDataType;
	}

	public void setReservedDataType(final boolean isReservedDataType) {
		this.isReservedDataType = isReservedDataType;
	}
	
	public void printFields() {
		System.out.println("RIFDataType Name:==" + name+"== Identifier=="+identifier+"==");
		System.out.println("Description:=="+ description+"==");
		System.out.println("Cleaning Policy:=="+fieldCleaningPolicy.getName()+"==");
		System.out.println("Cleaning function name:=="+validationFunctionName+"==");
		for (CleaningRule cleaningRule : cleaningRules) {
			System.out.println("Cleaning Rule id=="+cleaningRule.getIdentifier()+"==");
			System.out.println("Cleaning Rule name=="+cleaningRule.getName()+"==");
			System.out.println("Cleaning Rule description=="+cleaningRule.getDescription()+"==");
			System.out.println("Cleaning Rule search value=="+cleaningRule.getSearchValue()+"==");
			System.out.println("Cleaning Rule replace value=="+cleaningRule.getReplaceValue()+"==");
		}
		System.out.println("Validation Policy:=="+fieldCleaningPolicy.getName()+"==");
		System.out.println("Validation function name:=="+validationFunctionName+"==");
		for (ValidationRule validationRule : validationRules) {
			System.out.println("Validation Rule id=="+validationRule.getIdentifier()+"==");
			System.out.println("Validation Rule name=="+validationRule.getName()+"==");
			System.out.println("Validation Rule description=="+validationRule.getDescription()+"==");
			System.out.println("Validation Rule validation value=="+validationRule.getValidValue()+"==");
		}
	
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	public void checkErrors() {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		String recordType
			= RIFDataLoaderToolMessages.getMessage("abstractRIFDataType.name.label");
		
		String identifierFieldName
			= RIFDataLoaderToolMessages.getMessage("rifDataType.identifier.label");
		String nameFieldName
			= RIFDataLoaderToolMessages.getMessage("rifDataType.name.label");
		String descriptionFieldName
			= RIFDataLoaderToolMessages.getMessage("rifDataType.description.label");
				
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();		
		if (fieldValidationUtility.isEmpty(identifier)) {		

			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					identifierFieldName);
			errorMessages.add(errorMessage);
		}
		else if (fieldValidationUtility.isEmpty(name)) {
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					nameFieldName);
			errorMessages.add(errorMessage);			
		}
		else if (fieldValidationUtility.isEmpty(description)) {
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					descriptionFieldName);
			errorMessages.add(errorMessage);			
		}

	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: DisplayableListItem
	public String getDisplayName() {
		return name;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


