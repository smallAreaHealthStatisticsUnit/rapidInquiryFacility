package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.ConvertWorkflowConfiguration;

import rifDataLoaderTool.businessConceptLayer.DataSource;
import rifDataLoaderTool.businessConceptLayer.RIFDataTypeFactory;
import rifDataLoaderTool.businessConceptLayer.CleanWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.CleanWorkflowFieldConfiguration;
import rifDataLoaderTool.businessConceptLayer.DateRIFDataType;
import rifDataLoaderTool.businessConceptLayer.DoubleRIFDataType;
import rifDataLoaderTool.businessConceptLayer.ICDCodeRIFDataType;
import rifDataLoaderTool.businessConceptLayer.RIFDataTypeInterface;
import rifDataLoaderTool.businessConceptLayer.CleaningRule;
import rifDataLoaderTool.businessConceptLayer.AbstractRIFDataType;


import rifGenericLibrary.presentationLayer.HTMLUtility;
import rifServices.fileFormats.XMLUtility;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

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

public final class CleanWorkflowConfigurationHandler 
	extends AbstractWorkflowConfigurationHandler {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private CleanWorkflowConfiguration cleanWorkFlowConfiguration;
	private RIFDataTypeFactory rifDataTypeFactory;
	
	
	private CleanWorkflowFieldConfiguration currentCleanWorkflowFieldConfiguration;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CleanWorkflowConfigurationHandler() {
		cleanWorkFlowConfiguration = CleanWorkflowConfiguration.newInstance();
		rifDataTypeFactory = new RIFDataTypeFactory();
		
		setSingularRecordName("clean");
		setPluralRecordName("clean_field_configuration");
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public CleanWorkflowConfiguration getCleanWorkflowConfiguration() {
		return cleanWorkFlowConfiguration;
	}
	
	public String getHTML(
		final CleanWorkflowConfiguration cleanWorkflowConfiguration) {
		
		return "";
	}
	
	public String getHTML(
		final CleanWorkflowFieldConfiguration cleanWorkflowFieldConfiguration) {
			
		return "";
	}	
	
	
	public void writeXML(
		final CleanWorkflowConfiguration cleanWorkflowConfiguration) 
		throws IOException {
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag("clean");
		
		ArrayList<CleanWorkflowFieldConfiguration> fields
			= cleanWorkflowConfiguration.getAllFieldCleaningConfigurations();
		for (CleanWorkflowFieldConfiguration field : fields) {
			xmlUtility.writeRecordStartTag("clean_field_configuration");
			xmlUtility.writeField(
				"clean", 
				"load_field_name", 
				field.getLoadTableFieldName());
			xmlUtility.writeField(
				"clean", 
				"clean_field_name", 
				field.getCleanedTableFieldName());
			xmlUtility.writeField(
				"clean", 
				"description", 
				field.getDescription());
			xmlUtility.writeField(
				"clean", 
				"description", 
				field.getRifDataType().getName());
			xmlUtility.writeRecordEndTag("clean_field_configuration");
		}
				
		xmlUtility.writeRecordEndTag("clean");		
	}

	
	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isPluralRecordName(qualifiedName) == true) {

			activate();
		}
		else if (isSingularRecordName(qualifiedName) == true) {
			currentCleanWorkflowFieldConfiguration
				= CleanWorkflowFieldConfiguration.newInstance();
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
			cleanWorkFlowConfiguration.addCleanWorkflowFieldConfiguration(currentCleanWorkflowFieldConfiguration);
		}
		else if (equalsFieldName("load_field_name", qualifiedName)) {
			currentCleanWorkflowFieldConfiguration.setLoadTableFieldName(getCurrentFieldValue());
		}
		else if (equalsFieldName("clean_field_name", qualifiedName)) {
			currentCleanWorkflowFieldConfiguration.setCleanedTableFieldName(getCurrentFieldValue());
		}
		else if (equalsFieldName("description", qualifiedName)) {
			currentCleanWorkflowFieldConfiguration.setCleanedTableFieldName(getCurrentFieldValue());
		}		
		else if (equalsFieldName("data_type", qualifiedName)) {
			String dataTypeName = getCurrentFieldValue();
			AbstractRIFDataType rifDataType 
				= rifDataTypeFactory.getDataType(dataTypeName);
			currentCleanWorkflowFieldConfiguration.setRifDataType(rifDataType);
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


