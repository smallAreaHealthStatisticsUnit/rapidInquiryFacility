
package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.RIFDataTypeFactory;
import rifDataLoaderTool.businessConceptLayer.RIFDatabaseConnectionParameters;
import rifDataLoaderTool.businessConceptLayer.DataLoaderToolGeography;
import rifDataLoaderTool.businessConceptLayer.DataLoaderToolSettings;
import rifDataLoaderTool.businessConceptLayer.LinearWorkflow;
import rifDataLoaderTool.businessConceptLayer.RIFDataType;


import rifGenericLibrary.fileFormats.XMLCommentInjector;
import rifGenericLibrary.fileFormats.XMLUtility;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

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
 * Copyright 2014 Imperial College London, developed by the Small Area
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


final class RIFDataLoaderConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	private DataLoaderToolSettings dataLoaderToolSettings;
	
	private DatabaseConnectionConfigurationHandler databaseConnectionConfigurationHandler;
	private GeographyConfigurationHandler geographyConfigurationHandler;
	private RIFDataTypeConfigurationHandler rifDataTypeConfigurationHandler;
	private LinearWorkflowConfigurationHandler linearWorkflowConfigurationHandler;
		
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study content handler.
     */
	public RIFDataLoaderConfigurationHandler() {
		dataLoaderToolSettings = new DataLoaderToolSettings();
		
		setSingularRecordName("rif_data_loader_settings");
		rifDataTypeConfigurationHandler = new RIFDataTypeConfigurationHandler();
		
		databaseConnectionConfigurationHandler
			= new DatabaseConnectionConfigurationHandler();

		geographyConfigurationHandler
			= new GeographyConfigurationHandler();
		linearWorkflowConfigurationHandler
			= new LinearWorkflowConfigurationHandler();
		//geographicalResolutionLevels
		//	= new ArrayList<GeographicalResolutionLevel>();
	}


	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(
			outputStream, 
			commentInjector);

		databaseConnectionConfigurationHandler.initialise(
			outputStream, 
			commentInjector);	
		rifDataTypeConfigurationHandler.initialise(
			outputStream, 
			commentInjector);		
		geographyConfigurationHandler.initialise(
			outputStream, 
			commentInjector);
		linearWorkflowConfigurationHandler.initialise(
			outputStream, 
			commentInjector);	
				
	}

	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
		databaseConnectionConfigurationHandler.initialise(outputStream);
		geographyConfigurationHandler.initialise(outputStream);
		rifDataTypeConfigurationHandler.initialise(outputStream);
		linearWorkflowConfigurationHandler.initialise(outputStream);
	}
		
// ==========================================
// Section Accessors and Mutators
// ==========================================
    	
	public DataLoaderToolSettings getDataLoaderToolSettings() {
		return dataLoaderToolSettings;
	}
	
	/**
	 * Gets the disease mapping study.
	 *
	 * @return the disease mapping study
	 */

	public void writeXML(final DataLoaderToolSettings dataLoaderToolSettings)
		throws IOException {
			
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeStartXML();		

		String recordType = getSingularRecordName();
		xmlUtility.writeRecordStartTag(recordType);
		databaseConnectionConfigurationHandler.writeXML(
			dataLoaderToolSettings.getDatabaseConnectionParameters());

		geographyConfigurationHandler.writeXML(
			dataLoaderToolSettings.getGeographies());
		rifDataTypeConfigurationHandler.writeXML(
			dataLoaderToolSettings.getRIFDataTypeFactory());
		
		//@TODO In future, we will have the Data Loader Tool
		//support multiple work flows
		ArrayList<LinearWorkflow> workFlows
			= dataLoaderToolSettings.getWorkflows();
		linearWorkflowConfigurationHandler.writeXML(workFlows);			
		xmlUtility.writeRecordEndTag(recordType);	
		
		System.out.println("RDLConfigHandler writeXML 4");

		
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
			if (databaseConnectionConfigurationHandler.isSingularRecordName(qualifiedName)) {
				assignDelegatedHandler(databaseConnectionConfigurationHandler);				
			}
			else if (geographyConfigurationHandler.isPluralRecordName(qualifiedName)) {
				assignDelegatedHandler(geographyConfigurationHandler);				
			}
			else if (rifDataTypeConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(rifDataTypeConfigurationHandler);
			}
			else if (linearWorkflowConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(linearWorkflowConfigurationHandler);
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
				if (currentDelegatedHandler == databaseConnectionConfigurationHandler) {
					RIFDatabaseConnectionParameters databaseConnectionParameters
						= databaseConnectionConfigurationHandler.getDatabaseConnectionParameters();
					dataLoaderToolSettings.setDatabaseConnectionParameters(databaseConnectionParameters);
				}				
				else if (currentDelegatedHandler == geographyConfigurationHandler) {
					ArrayList<DataLoaderToolGeography> geographies
						= geographyConfigurationHandler.getGeographies();
					dataLoaderToolSettings.setGeographies(geographies);
				}
				else if (currentDelegatedHandler == rifDataTypeConfigurationHandler) {
					RIFDataTypeFactory rifDataTypeFactory
						= rifDataTypeConfigurationHandler.getRIFDataTypeFactory();
					dataLoaderToolSettings.setRIFDataTypeFactory(rifDataTypeFactory);
				}
				else if (currentDelegatedHandler == linearWorkflowConfigurationHandler) {
					ArrayList<LinearWorkflow> linearWorkflows
						= linearWorkflowConfigurationHandler.getLinearWorkflows();
					dataLoaderToolSettings.setWorkflows(linearWorkflows);
				}
				else {
					assert false;
				}				
				
				//handler just finished				
				unassignDelegatedHandler();				
			}
			else {
				assert false;				
			}
		}
	}
}
