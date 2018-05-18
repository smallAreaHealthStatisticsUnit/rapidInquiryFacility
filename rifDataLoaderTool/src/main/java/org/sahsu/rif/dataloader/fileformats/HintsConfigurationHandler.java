package org.sahsu.rif.dataloader.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.ConfigurationHints;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

final class HintsConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private RIFDataTypeFactory dataTypeFactory;
	private ConfigurationHints configurationHints;
	
	private DataSetConfigurationHandler dataSetConfigurationHintHandler;
	private DataSetFieldConfigurationHandler dataSetFieldConfigurationHintHandler;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public HintsConfigurationHandler() {
		//dataTypeFactory = RIFDataTypeFactory.newInstance();
		//dataTypeFactory.populateFactoryWithBuiltInTypes();
		configurationHints = new ConfigurationHints();
		
		setSingularRecordName("configuration_hints");		
		String configurationHintsComment
			= RIFDataLoaderToolMessages.getMessage("configurationHints.toolTipText");
		setComment(
			"configuration_hints", 
			configurationHintsComment);		

		dataSetConfigurationHintHandler
			= new DataSetConfigurationHandler();
		dataSetConfigurationHintHandler.setIsSerialisingHints(true);
		dataSetFieldConfigurationHintHandler
			= new DataSetFieldConfigurationHandler();
		dataSetFieldConfigurationHintHandler.setIsSerialisingHints(true);
		
	}
	
	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
		dataSetConfigurationHintHandler.initialise(outputStream, commentInjector);
		dataSetFieldConfigurationHintHandler.initialise(outputStream, commentInjector);
	}	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public ConfigurationHints getConfigurationHints() {
		return configurationHints;
	}
	
	public void writeXML(
		final ConfigurationHints configurationHints)
		throws IOException {
		
		XMLUtility xmlUtility = getXMLUtility();
		
		xmlUtility.writeRecordStartTag(getSingularRecordName());		
		ArrayList<DataSetConfiguration> dataSetConfigurationHints
			= configurationHints.getDataSetConfigurationHints();
		dataSetConfigurationHintHandler.writeXML(dataSetConfigurationHints);				
		ArrayList<DataSetFieldConfiguration> dataSetFieldConfigurationHints
			= configurationHints.getDataSetFieldConfigurationHints();
		dataSetFieldConfigurationHintHandler.writeXML(dataSetFieldConfigurationHints);		
		xmlUtility.writeRecordEndTag(getSingularRecordName());
	}

	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isSingularRecordName(qualifiedName) == true) {
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
			if (dataSetConfigurationHintHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(dataSetConfigurationHintHandler);				
			}
			else if (dataSetFieldConfigurationHintHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(dataSetFieldConfigurationHintHandler);						
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

				if (currentDelegatedHandler == dataSetConfigurationHintHandler) {
					ArrayList<DataSetConfiguration> dataSetConfigurationHints
						= dataSetConfigurationHintHandler.getDataSetConfigurations();
					configurationHints.setDataSetConfigurationHints(dataSetConfigurationHints);
				}
				else if (currentDelegatedHandler == dataSetFieldConfigurationHintHandler) {
					ArrayList<DataSetFieldConfiguration> dataSetFieldConfigurationHints
						= dataSetFieldConfigurationHintHandler.getDataSetFieldConfigurations();
					configurationHints.setDataSetFieldConfigurationHints(dataSetFieldConfigurationHints);
				}
				else {
					assert false;
				}
				//handler just finished				
				unassignDelegatedHandler();	
			}
		}
		else {
			assert false;
		}		
	}
	
	public void setDataTypeFactory(final RIFDataTypeFactory dataTypeFactory) {
		this.dataTypeFactory = dataTypeFactory;
		dataSetConfigurationHintHandler.setDataTypeFactory(dataTypeFactory);
		dataSetFieldConfigurationHintHandler.setDataTypeFactory(dataTypeFactory);
	}
	
	public void setConfigurationHints(final ConfigurationHints configurationHints) {
		this.configurationHints = configurationHints;
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


