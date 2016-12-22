package rifDataLoaderTool.fileFormats;


import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration;
import rifDataLoaderTool.businessConceptLayer.RIFDataTypeFactory;
import rifDataLoaderTool.businessConceptLayer.RIFSchemaArea;
import rifDataLoaderTool.businessConceptLayer.WorkflowState;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.fileFormats.XMLCommentInjector;
import rifGenericLibrary.fileFormats.XMLUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 *
 * <hr>
 * Copyright 2016 Imperial College London, developed by the Small Area
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

final class DataSetConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DataSetFieldConfigurationHandler dataSetFieldConfigurationHandler;
	
	private ArrayList<DataSetConfiguration> dataSetConfigurations;
	
	private DataSetConfiguration currentDataSetConfiguration;
	
	private boolean isSerialisingHints;
	
	private HashMap<String, DataSetConfiguration> dataSetFromDependencyName;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetConfigurationHandler() {
		isSerialisingHints = false;
		
		dataSetFieldConfigurationHandler
			= new DataSetFieldConfigurationHandler();
		
		dataSetConfigurations = new ArrayList<DataSetConfiguration>();

		setPluralRecordName("data_set_configurations");		
		setSingularRecordName("data_set_configuration");
		
		String dataSetConfigurationComment
			= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.toolTipText");
		setComment(
			"data_set_configuration", 
			dataSetConfigurationComment);
		
		String configurationVersionComment
			= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.version.toolTip");
		setComment(
			"data_set_configuration",
			"version",
			configurationVersionComment);
		
		String filePathComment
			= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.version.toolTip");
		setComment(
			"data_set_configuration",
			"file_path",
			filePathComment);
		
		dataSetFromDependencyName = new HashMap<String, DataSetConfiguration>();
	}
	
	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
		dataSetFieldConfigurationHandler.initialise(outputStream, commentInjector);
	}	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void setIsSerialisingHints(final boolean isSerialisingHints) {
		this.isSerialisingHints = isSerialisingHints;
	}


	
	public ArrayList<DataSetConfiguration> getDataSetConfigurations() {
		return dataSetConfigurations;
	}

	
	public ArrayList<DataSetConfiguration> getDataSetConfigurations(
		final RIFSchemaArea targetRIFSchemaArea) {
	
		ArrayList<DataSetConfiguration> results = new ArrayList<DataSetConfiguration>();
		if (targetRIFSchemaArea == null) {
			return results;
		}
		
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {
			RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
			if (rifSchemaArea == targetRIFSchemaArea) {
				results.add(dataSetConfiguration);
			}
		}
		
		return results;
	}
	
	
	private void resolveDependencies() {
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {
			String displayName = dataSetConfiguration.getDisplayName();
			DataSetConfiguration dependentDataSetConfiguration
				= dataSetFromDependencyName.get(displayName);
			if (dependentDataSetConfiguration != null) {
				//This data set dependends on the current data set
				dependentDataSetConfiguration.setDependencyDataSetConfiguration(dataSetConfiguration);
			}
		}
	}

	public void writeXML(
		final ArrayList<DataSetConfiguration> dataSetConfigurations) 
		throws IOException {
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(getPluralRecordName());
		
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {
			xmlUtility.writeRecordStartTag(getSingularRecordName());
			
			xmlUtility.writeField(
				getSingularRecordName(), 
				"name", 
				dataSetConfiguration.getName());

			xmlUtility.writeField(
				getSingularRecordName(), 
				"version", 
				dataSetConfiguration.getVersion());

			xmlUtility.writeField(
				getSingularRecordName(), 
				"file_path", 
				dataSetConfiguration.getFilePath());
			
			xmlUtility.writeField(
				getSingularRecordName(), 
				"description", 
				dataSetConfiguration.getDescription());
			
			xmlUtility.writeField(
				getSingularRecordName(), 
				"file_has_field_names_defined", 
				String.valueOf(dataSetConfiguration.fileHasFieldNamesDefined()));
			
			RIFSchemaArea rifSchemaArea
				= dataSetConfiguration.getRIFSchemaArea();
			if (rifSchemaArea != null) {				
				xmlUtility.writeField(
					getSingularRecordName(), 
					"rif_schema_area", 
					dataSetConfiguration.getRIFSchemaArea().getCode());
			}
			
			xmlUtility.writeField(
				getSingularRecordName(), 
				"current_workflow_state", 
				dataSetConfiguration.getCurrentWorkflowState().getCode());
			
			ArrayList<DataSetFieldConfiguration> fieldConfigurations
				= dataSetConfiguration.getFieldConfigurations();
			dataSetFieldConfigurationHandler.writeXML(fieldConfigurations);
			
			xmlUtility.writeRecordEndTag(getSingularRecordName());
			
			DataSetConfiguration dependencyDataSetConfiguration
				= dataSetConfiguration.getDependencyDataSetConfiguration();
			
			if (dependencyDataSetConfiguration != null) {
				xmlUtility.writeField(
					getSingularRecordName(), 
					"dependency", 
					dependencyDataSetConfiguration.getDisplayName());
			}
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

		if (isPluralRecordName(qualifiedName) == true) {
			activate();
		}
		else if (isSingularRecordName(qualifiedName) == true) {
			currentDataSetConfiguration
				= DataSetConfiguration.newInstance();
			currentDataSetConfiguration.setNewRecord(false);
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
			if (dataSetFieldConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(dataSetFieldConfigurationHandler);				
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
			resolveDependencies();		
			deactivate();
		}
		else if (isSingularRecordName(qualifiedName) == true) {
			currentDataSetConfiguration.setIsHint(isSerialisingHints);
			dataSetConfigurations.add(currentDataSetConfiguration);
		}		
		else if (isDelegatedHandlerAssigned()) {
			AbstractDataLoaderConfigurationHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
			
			if (currentDelegatedHandler.isActive() == false) {
				if (currentDelegatedHandler == dataSetFieldConfigurationHandler) {
					ArrayList<DataSetFieldConfiguration> fieldConfigurations
						=  dataSetFieldConfigurationHandler.getDataSetFieldConfigurations();
					currentDataSetConfiguration.setFieldConfigurations(fieldConfigurations);
				}				
				else {
					assert false;
				}
				//handler just finished				
				unassignDelegatedHandler();	
			}
		}
		else if (equalsFieldName("name", qualifiedName)) {
			currentDataSetConfiguration.setName(getCurrentFieldValue());
		}
		else if (equalsFieldName("version", qualifiedName)) {
			currentDataSetConfiguration.setVersion(getCurrentFieldValue());
		}
		else if (equalsFieldName("file_path", qualifiedName)) {
			currentDataSetConfiguration.setFilePath(getCurrentFieldValue());
		}
		else if (equalsFieldName("description", qualifiedName)) {
			currentDataSetConfiguration.setDescription(getCurrentFieldValue());
		}
		else if (equalsFieldName("file_has_field_names_defined", qualifiedName)) {
			Boolean fileHasFieldNamesDefined
				= Boolean.valueOf(getCurrentFieldValue());
			currentDataSetConfiguration.setFileHasFieldNamesDefined(fileHasFieldNamesDefined);
		}
		else if (equalsFieldName("rif_schema_area", qualifiedName)) {
			RIFSchemaArea rifSchemaArea = RIFSchemaArea.getSchemaAreaFromCode(getCurrentFieldValue());
			currentDataSetConfiguration.setRIFSchemaArea(rifSchemaArea);
		}
		else if (equalsFieldName("current_workflow_state", qualifiedName)) {
			WorkflowState workflowState = WorkflowState.getWorkflowStateFromCode(getCurrentFieldValue());
			currentDataSetConfiguration.setCurrentWorkflowState(workflowState);
		}		
		else if (equalsFieldName("dependency", qualifiedName)) {
			dataSetFromDependencyName.put(getCurrentFieldValue(), currentDataSetConfiguration);
		}		
		else {
			assert false;
		}		
	}
	
	public void setRIFDataTypeFactory(final RIFDataTypeFactory rifDataTypeFactory) {
		dataSetFieldConfigurationHandler.setRIFDataTypeFactory(rifDataTypeFactory);		
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


