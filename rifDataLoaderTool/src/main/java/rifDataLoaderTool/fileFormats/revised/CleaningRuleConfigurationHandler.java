
package rifDataLoaderTool.fileFormats.revised;


import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.fileFormats.XMLCommentInjector;
import rifGenericLibrary.fileFormats.XMLUtility;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


/**
 * Responsible for serialising {@link rifDataLoaderTool.businessConceptLayer.CleaningRule} 
 * to and from XML files.
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2016 Imperial College London, developed by the Small Area
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


final class CleaningRuleConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	private ArrayList<CleaningRule> rules;
	private CleaningRule currentRule;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study content handler.
     */
	public CleaningRuleConfigurationHandler() {
		setPluralRecordName("cleaning_rules");
		setSingularRecordName("cleaning_rule");
		
		rules = new ArrayList<CleaningRule>();
	}


	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
	}

	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
	}
	
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
    	
	/**
	 * Gets the disease mapping study.
	 *
	 * @return the disease mapping study
	 */
	public ArrayList<CleaningRule> getCleaningRules() {
		return rules;
	}

	public void writeXML(
		final ArrayList<CleaningRule> rules)
		throws IOException {
			
		XMLUtility xmlUtility = getXMLUtility();
		
		xmlUtility.writeRecordStartTag(getPluralRecordName());	
		String recordType = getSingularRecordName();
		int numberOfRules = rules.size();
		
		for (int i = 0; i < numberOfRules; i++) {
			CleaningRule rule = rules.get(i);
			
			xmlUtility.writeRecordStartTag(recordType);
			
			if (i == 0) {
				//We comment the first example of a record
				String cleaningRuleComment
					= RIFDataLoaderToolMessages.getMessage("cleaningRule.toolTipText");
				xmlUtility.writeComment(cleaningRuleComment);				
			}
			else {
				
			}
			
			xmlUtility.writeField(
				recordType, 
				"identifier", 
				rule.getIdentifier());
			xmlUtility.writeField(
				recordType, 
				"name", 
				rule.getName());
			xmlUtility.writeField(
				recordType, 
				"description", 
				rule.getDescription());
			xmlUtility.writeField(
				recordType, 
				"search_value", 
				rule.getSearchValue());
			
			xmlUtility.writeField(
				recordType, 
				"replace_value", 
				rule.getReplaceValue());
			xmlUtility.writeRecordEndTag(recordType);
		}
		xmlUtility.writeRecordEndTag(getPluralRecordName());
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
		
		if (isPluralRecordName(qualifiedName)) {
			activate();
			rules.clear();
		}
		else if (isSingularRecordName(qualifiedName)) {
			currentRule = CleaningRule.newInstance();			
		}
	}
	
	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {
		
		if (isPluralRecordName(qualifiedName)) {
			deactivate();
		}
		else if (isSingularRecordName(qualifiedName)) {
			rules.add(currentRule);
		}
		else if (equalsFieldName("identifier", qualifiedName)) {
			currentRule.setIdentifier(getCurrentFieldValue());
		}
		else if (equalsFieldName("name", qualifiedName)) {
			currentRule.setName(getCurrentFieldValue());
		}
		else if (equalsFieldName("description", qualifiedName)) {
			currentRule.setDescription(getCurrentFieldValue());
		}
		else if (equalsFieldName("search_value", qualifiedName)) {
			currentRule.setSearchValue(getCurrentFieldValue());
		}
		else if (equalsFieldName("replace_value", qualifiedName)) {
			currentRule.setReplaceValue(getCurrentFieldValue());
		}
		else {
			assert false;
		}
	}
}
