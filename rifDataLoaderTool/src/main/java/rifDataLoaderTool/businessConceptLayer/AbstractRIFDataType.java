package rifDataLoaderTool.businessConceptLayer;

import java.util.ArrayList;


/**
 * The base class for classes which implement the {@link rifDataLoaderTool.businessConceptLayer.RIFDataTypeInterface} 
 * interface. The RIF Data Loader Tool recognises a hierarchy of Data Types it associates with table columns 
 * in the data sources (see @link rifDataLoaderTool.businessConceptLayer.DataSource}).  By associating a RIF 
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

public abstract class AbstractRIFDataType implements RIFDataTypeInterface {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String identifier;
	private String name;
	private String description;
	
	private RIFFieldValidationPolicy fieldValidationPolicy;
	private ArrayList<String> validationExpressions;
	private String validationFunctionName;

	
	private RIFFieldCleaningPolicy fieldCleaningPolicy;	
	private String cleaningFunctionName;
	private ArrayList<CleaningRule> cleaningRules;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractRIFDataType(
		final String identifier,
		final String name,
		final String description) {
		
		this.identifier = identifier;
		this.name = name;
		this.description = description;
		
		fieldValidationPolicy = RIFFieldValidationPolicy.NO_VALIDATION;
		validationExpressions = new ArrayList<String>();
		fieldCleaningPolicy = RIFFieldCleaningPolicy.NO_CLEANING;

		cleaningRules = new ArrayList<CleaningRule>();
	}

	public void copyAttributes(
		final AbstractRIFDataType cloneType) {
		
		cloneType.setIdentifier(getIdentifier());
		cloneType.setName(getName());

		cloneType.setFieldValidationPolicy(getFieldValidationPolicy());
		ArrayList<String> validationExpressions
			= getValidationExpressions();
		ArrayList<String> cloneValidationExpressions = new ArrayList<String>();
		cloneValidationExpressions.addAll(validationExpressions);
		cloneType.setValidationFunctionName(getValidationFunctionName());
		
		cloneType.setFieldCleaningPolicy(fieldCleaningPolicy);
		cloneType.setCleaningFunctionName(getCleaningFunctionName());
		
		ArrayList<CleaningRule> cloneCleaningRules
			= new ArrayList<CleaningRule>();
		for (CleaningRule cleaningRule : cleaningRules) {
			CleaningRule cloneCleaningRule
				= CleaningRule.createCopy(cleaningRule);
			cloneCleaningRules.add(cloneCleaningRule);
		}
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

	public void setDescription(
		final String description) {

		this.description = description;
	}

	public RIFFieldValidationPolicy getFieldValidationPolicy() {
		return fieldValidationPolicy;
	}

	public void setFieldValidationPolicy(
		final RIFFieldValidationPolicy fieldValidationPolicy) {

		this.fieldValidationPolicy = fieldValidationPolicy;
	}

	public String getMainValidationExpression() {
		if (validationExpressions.isEmpty()) {
			return null;
		}
		else {
			return validationExpressions.get(0);
		}
	}
	
	public ArrayList<String> getValidationExpressions() {
		return validationExpressions;
	}

	public void addValidationExpression(
		final String validationExpression) {
		
		validationExpressions.add(validationExpression);
	}
	
	public void setValidationExpressions(
		final ArrayList<String> validationExpressions) {

		this.validationExpressions = validationExpressions;
	}

	public String getValidationFunctionName() {
		return validationFunctionName;
	}

	public void setValidationFunctionName(
		final String validationFunctionName) {

		this.validationFunctionName = validationFunctionName;
	}

	public RIFFieldCleaningPolicy getFieldCleaningPolicy() {
		return fieldCleaningPolicy;
	}

	public void setFieldCleaningPolicy(
		final RIFFieldCleaningPolicy fieldCleaningPolicy) {
		
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

	public ArrayList<CleaningRule> getCleaningRules() {

		return cleaningRules;
	}
	
	
	public String getCleaningFunctionParameterValues() {
		return "";
	}
	
	public String getValidationFunctionParameterValues() {
		return "";
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

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


