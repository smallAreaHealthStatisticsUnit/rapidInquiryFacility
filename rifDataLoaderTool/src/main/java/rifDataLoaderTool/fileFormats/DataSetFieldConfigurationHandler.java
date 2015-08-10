package rifDataLoaderTool.fileFormats;


import rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration;
import rifDataLoaderTool.businessConceptLayer.FieldPurpose;
import rifDataLoaderTool.businessConceptLayer.RIFConversionFunction;
import rifDataLoaderTool.businessConceptLayer.RIFConversionFunctionFactory;
import rifDataLoaderTool.businessConceptLayer.FieldRequirementLevel;
import rifDataLoaderTool.businessConceptLayer.FieldChangeAuditLevel;

import rifDataLoaderTool.businessConceptLayer.RIFCheckOption;
import rifDataLoaderTool.businessConceptLayer.RIFDataTypeFactory;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.AbstractRIFDataType;
import rifDataLoaderTool.businessConceptLayer.WorkflowState;
import rifServices.fileFormats.XMLCommentInjector;
import rifServices.fileFormats.XMLUtility;

import java.util.ArrayList;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

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

public final class DataSetFieldConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private RIFCheckOptionConfigurationHandler rifCheckOptionConfigurationHandler;
	
	private RIFDataTypeFactory rifDataTypeFactory;
	private RIFConversionFunctionFactory rifConversionFunctionFactory;
	private ArrayList<DataSetFieldConfiguration> dataSetFieldConfigurations;
	private DataSetFieldConfiguration currentDataSetFieldConfiguration;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetFieldConfigurationHandler() {
		rifDataTypeFactory = RIFDataTypeFactory.newInstance();
		rifConversionFunctionFactory = RIFConversionFunctionFactory.newInstance();
		rifCheckOptionConfigurationHandler
			= new RIFCheckOptionConfigurationHandler();
		
		dataSetFieldConfigurations 
			= new ArrayList<DataSetFieldConfiguration>();

		setPluralRecordName("data_set_field_configurations");		
		setSingularRecordName("data_set_field_configuration");
	}

	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
		rifCheckOptionConfigurationHandler.initialise(outputStream, commentInjector);
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public ArrayList<DataSetFieldConfiguration> getDataSetFieldConfigurations() {
		return dataSetFieldConfigurations;
	}
	
	
	public String getHTML(
		final DataSetFieldConfiguration fieldConfiguration,
		final WorkflowState workflowState) {

		
		return "";
	}
	
	public void writeXML(
		final ArrayList<DataSetFieldConfiguration> fieldConfigurations)
		throws IOException {
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(getPluralRecordName());

		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			
			xmlUtility.writeRecordStartTag(getSingularRecordName());

			xmlUtility.writeField(
				getSingularRecordName(), 
				"core_field_name", 
				fieldConfiguration.getCoreFieldName());

			xmlUtility.writeField(
				getSingularRecordName(), 
				"core_field_description", 
				fieldConfiguration.getCoreFieldDescription());			
			
			xmlUtility.writeField(
				getSingularRecordName(), 
				"rif_data_type", 
				fieldConfiguration.getRIFDataType().getIdentifier());			
			
			xmlUtility.writeField(
				getSingularRecordName(), 
				"load_field_name", 
				fieldConfiguration.getLoadFieldName());

			xmlUtility.writeField(
				getSingularRecordName(), 
				"clean_field_name", 
				fieldConfiguration.getCleanFieldName());			
			
			xmlUtility.writeField(
				getSingularRecordName(), 
				"convert_field_name", 
				fieldConfiguration.getConvertFieldName());

			
			
			RIFConversionFunction rifConversionFunction
				= fieldConfiguration.getConvertFunction();
			if (rifConversionFunction != null) {
				xmlUtility.writeField(
					getSingularRecordName(), 
					"convert_field_function", 
					rifConversionFunction.getFunctionName());
			}
			else {
				xmlUtility.writeField(
					getSingularRecordName(), 
					"convert_field_function", 
					"");				
			}
			
			xmlUtility.writeField(
				getSingularRecordName(), 
				"optimise_using_index", 
				String.valueOf(fieldConfiguration.optimiseUsingIndex()));
			
			xmlUtility.writeField(
				getSingularRecordName(), 
				"is_duplicate_identification_field", 
				String.valueOf(fieldConfiguration.isDuplicateIdentificationField()));
			
			FieldRequirementLevel fieldRequirementLevel
				= fieldConfiguration.getFieldRequirementLevel();
			xmlUtility.writeField(
				getSingularRecordName(), 
				"field_requirement_level", 
				fieldRequirementLevel.getCode());

			FieldChangeAuditLevel fieldChangeAuditLevel
				= fieldConfiguration.getFieldChangeAuditLevel();
			xmlUtility.writeField(
				getSingularRecordName(), 
				"field_change_audit_level", 
				fieldChangeAuditLevel.getCode());
			
			
			FieldPurpose fieldPurpose
				= fieldConfiguration.getFieldPurpose();
			xmlUtility.writeField(
				getSingularRecordName(), 
				"field_purpose", 
				fieldPurpose.getCode());
			
			ArrayList<RIFCheckOption> checkOptions
				= fieldConfiguration.getCheckOptions();			
			rifCheckOptionConfigurationHandler.writeXML(checkOptions);
			
			xmlUtility.writeRecordEndTag(getSingularRecordName());			
		}
				
		xmlUtility.writeRecordEndTag(getPluralRecordName());		
	}

	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {
		
		if (isPluralRecordName(qualifiedName)) {
			activate();
		}
		else if (isSingularRecordName(qualifiedName) == true) {
			currentDataSetFieldConfiguration
				= DataSetFieldConfiguration.newInstance();
			currentDataSetFieldConfiguration.setNewRecord(false);
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
			if (rifCheckOptionConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(rifCheckOptionConfigurationHandler);
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


		if (isPluralRecordName(qualifiedName)) {
			deactivate();
		}
		else if (isSingularRecordName(qualifiedName)) {
			dataSetFieldConfigurations.add(currentDataSetFieldConfiguration);
		}		
		else if (isDelegatedHandlerAssigned()) {
			AbstractDataLoaderConfigurationHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
			
			if (currentDelegatedHandler.isActive() == false) {
				if (currentDelegatedHandler == rifCheckOptionConfigurationHandler) {
					ArrayList<RIFCheckOption> checkOptions
						= rifCheckOptionConfigurationHandler.getCheckOptions();
					currentDataSetFieldConfiguration.setCheckOptions(checkOptions);
					
					ArrayList<RIFCheckOption> mystuff
						= currentDataSetFieldConfiguration.getCheckOptions();
				}
				else {
					assert false;
				}
				
				//handler just finished				
				unassignDelegatedHandler();		
			}
	
		
		}
		else if (equalsFieldName("core_field_name", qualifiedName)) {
			currentDataSetFieldConfiguration.setCoreFieldName(
				getCurrentFieldValue());
		}
		else if (equalsFieldName("core_field_description", qualifiedName)) {
			currentDataSetFieldConfiguration.setCoreFieldDescription(
				getCurrentFieldValue());
		}
		else if (equalsFieldName("rif_data_type", qualifiedName)) {
			String dataTypeName = getCurrentFieldValue();
			AbstractRIFDataType rifDataType 
				= rifDataTypeFactory.getDataTypeFromCode(dataTypeName);
			currentDataSetFieldConfiguration.setRIFDataType(rifDataType);
		}
		else if (equalsFieldName("load_field_name", qualifiedName)) {
			currentDataSetFieldConfiguration.setLoadFieldName(
				getCurrentFieldValue());
		}		
		else if (equalsFieldName("clean_field_name", qualifiedName)) {
			currentDataSetFieldConfiguration.setCleanFieldName(
				getCurrentFieldValue());
		}		
		else if (equalsFieldName("convert_field_name", qualifiedName)) {
			currentDataSetFieldConfiguration.setConvertFieldName(
				getCurrentFieldValue());
		}	
		else if (equalsFieldName("convert_field_function", qualifiedName)) {
			String functionName = getCurrentFieldValue();	
			RIFConversionFunction rifConversionFunction
				= rifConversionFunctionFactory.getRIFConvertFunction(functionName);
			currentDataSetFieldConfiguration.setConvertFunction(rifConversionFunction);
		}	
		else if (equalsFieldName("optimise_using_index", qualifiedName)) {
			currentDataSetFieldConfiguration.setOptimiseUsingIndex(
				Boolean.valueOf(getCurrentFieldValue()));
		}
		else if (equalsFieldName("field_purpose", qualifiedName)) {
			currentDataSetFieldConfiguration.setFieldPurpose(
				FieldPurpose.getValueFromCode(getCurrentFieldValue()));
		}
		else if (equalsFieldName("field_requirement_level", qualifiedName)) {
			currentDataSetFieldConfiguration.setFieldRequirementLevel(
				FieldRequirementLevel.getValueFromCode(getCurrentFieldValue()));
		}		
		else if (equalsFieldName("field_change_audit_level", qualifiedName)) {
			currentDataSetFieldConfiguration.setFieldChangeAuditLevel(
				FieldChangeAuditLevel.getValueFromCode(getCurrentFieldValue()));
		}
		else if (equalsFieldName("is_duplicate_identification_field", qualifiedName)) {
			currentDataSetFieldConfiguration.setDuplicateIdentificationField(
				Boolean.valueOf(getCurrentFieldValue()));
		}
		else {
			assert false;
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


