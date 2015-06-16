package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.CheckWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.RIFSchemaArea;
import rifServices.fileFormats.XMLUtility;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.ArrayList;

/**
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

public final class CheckWorkflowConfigurationHandler 
	extends AbstractWorkflowConfigurationHandler {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private CheckWorkflowConfiguration checkWorkflowConfiguration;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CheckWorkflowConfigurationHandler() {
		checkWorkflowConfiguration 
			= CheckWorkflowConfiguration.newInstance();
		setPluralRecordName("check");
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public CheckWorkflowConfiguration getCheckWorkflowConfiguration() {
		return checkWorkflowConfiguration;		
	}

	public void writeXML(
		final CheckWorkflowConfiguration checkWorkflowConfiguration) 
		throws IOException {
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag("check");
		
		ArrayList<String> duplicateRowCheckFields
			= checkWorkflowConfiguration.getDuplicateRowCheckFields();
		for (String duplicateRowCheckField : duplicateRowCheckFields) {			
			System.out.println("========================CWCH -- writeXML dup=="+duplicateRowCheckField+"==");
			xmlUtility.writeField(
				"check", 
				"duplicate_row_field", 
				duplicateRowCheckField);		
		}

		ArrayList<String> cleanedRowCheckFields
			= checkWorkflowConfiguration.getCleanedRowCheckFields();
		for (String cleanedRowCheckField : cleanedRowCheckFields) {
			xmlUtility.writeField(
				"check", 
				"cleaned_row_field", 
				cleanedRowCheckField);				
		}
		
		xmlUtility.writeRecordEndTag("check");				
	}
	
	
	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		System.out.println("CWCH - startElement zzzzero=="+qualifiedName+"==");
		
		if (isPluralRecordName(qualifiedName) == true) {
			System.out.println("CWCH - startElement 1=="+qualifiedName+"==");
			activate();
		}

	}
	
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {

		if (isPluralRecordName(qualifiedName)) {
			System.out.println("CWCH - endElement 1=="+qualifiedName+"==");
			deactivate();
		}
		else if (equalsFieldName(
			"duplicate_row_field", 
			qualifiedName)) {		
			
			System.out.println("CWCH - endElement 2=="+qualifiedName+"==");

			checkWorkflowConfiguration.addDuplicateRowCheckField(getCurrentFieldValue());				
		}
		else if (equalsFieldName(
			"cleaned_row_field", 
			qualifiedName)) {				

			System.out.println("CWCH - endElement 3=="+qualifiedName+"==");
			
			checkWorkflowConfiguration.addCleanedRowCheckField(getCurrentFieldValue());				
		}	
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


