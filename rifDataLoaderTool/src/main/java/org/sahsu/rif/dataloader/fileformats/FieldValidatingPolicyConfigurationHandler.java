
package org.sahsu.rif.dataloader.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.FieldActionPolicy;
import org.sahsu.rif.dataloader.concepts.RIFDataType;
import org.sahsu.rif.dataloader.concepts.ValidationRule;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 *
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
 * Copyright 2017 Imperial College London, developed by the Small Area
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


final class FieldValidatingPolicyConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	private FieldActionPolicy policy;
	private String functionName;
	private ArrayList<ValidationRule> rules;
	private ValidationRuleConfigurationHandler rulesConfigurationHandler;
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study content handler.
     */
	public FieldValidatingPolicyConfigurationHandler() {
		setSingularRecordName("field_validating_policy");
		
		rulesConfigurationHandler
			 = new ValidationRuleConfigurationHandler();
		functionName = "";
		rules = new ArrayList<ValidationRule>();
	}

	public void resetPolicyAttributes() {
		rules.clear();
		functionName = "";
		policy = FieldActionPolicy.DO_NOTHING;
	}

	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(
			outputStream, 
			commentInjector);
		
		rulesConfigurationHandler.initialise(
			outputStream, 
			commentInjector);		
	}

	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
		rulesConfigurationHandler.initialise(
			outputStream);		
	}
	
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
    	
	/**
	 * Gets the disease mapping study.
	 *
	 * @return the disease mapping study
	 */
	public FieldActionPolicy getFieldValidationPolicy() {
		return policy;
	}
	
	public ArrayList<ValidationRule> getValidationRules() {
		ArrayList<ValidationRule> results
			= new ArrayList<ValidationRule>();
		results.addAll(rules);
		return results;
	}
	
	public String getValidationFunctionName() {
		return functionName;
	}
	
	public void writeXML(
		final RIFDataType rifDataType)
		throws IOException {
			
		XMLUtility xmlUtility = getXMLUtility();
		
		String recordType = getSingularRecordName();
		xmlUtility.writeRecordStartTag(recordType);
		FieldActionPolicy fieldValidationPolicy
			= rifDataType.getFieldValidationPolicy();
		xmlUtility.writeField(
			recordType, 
			"policy_type", 
			fieldValidationPolicy.getTagName());

		//write out list of cleaning rules
		ArrayList<ValidationRule> cleaningRules
			= rifDataType.getValidationRules();
		rulesConfigurationHandler.writeXML(cleaningRules);

		//write out the cleaning rule function
		xmlUtility.writeField(
			recordType, 
			"validation_function_name", 
			rifDataType.getValidationFunctionName());
		
		xmlUtility.writeRecordEndTag(recordType);		
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
			

	@Override
    public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {
		
		if (isSingularRecordName(qualifiedName)) {
			activate();
		}
		else if (isDelegatedHandlerAssigned()) {
			AbstractDataLoaderConfigurationHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.startElement(
				nameSpaceURI, 
				localName, 
				qualifiedName, 
				attributes);
		}
		else {
			
			//check to see if handlers could be assigned to delegate parsing			
			if (rulesConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(rulesConfigurationHandler);
			}
			
			//delegate to a handler.  If not, then scan for fields relating to this handler
			if (isDelegatedHandlerAssigned()) {
				AbstractDataLoaderConfigurationHandler currentDelegatedHandler
					= getCurrentDelegatedHandler();
				currentDelegatedHandler.startElement(
					nameSpaceURI, 
					localName, 
					qualifiedName, 
					attributes);
			}
			else {
				assert false;
			}

		}
	}
	
	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {
		
		if (isSingularRecordName(qualifiedName)) {
			deactivate();
		}
		else if (isDelegatedHandlerAssigned()) {
			AbstractDataLoaderConfigurationHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
						
			if (currentDelegatedHandler.isActive() == false) {
				if (currentDelegatedHandler == rulesConfigurationHandler) {
					rules
						= rulesConfigurationHandler.getValidationRules();
				}			
				else {
					assert false;
				}				
				
				//handler just finished				
				unassignDelegatedHandler();				
			}
		}
		else if (equalsFieldName("policy_type", qualifiedName)) {	
			policy = FieldActionPolicy.getPolicyFromTagName(getCurrentFieldValue());
		}		
		else if (equalsFieldName("validation_function_name", qualifiedName)) {
			functionName = getCurrentFieldValue();
		}
		else {
			assert false;
		}
	}
}
