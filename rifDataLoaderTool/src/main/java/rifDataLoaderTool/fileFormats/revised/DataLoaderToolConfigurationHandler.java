
package rifDataLoaderTool.fileFormats.revised;

import rifDataLoaderTool.businessConceptLayer.*;

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


final class DataLoaderToolConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	private DataLoaderToolConfiguration dataLoaderToolConfiguration;
	private RIFDataTypeFactory rifDataTypeFactory;
	private ConfigurationHints configurationHints;
	
	private DatabaseConnectionConfigurationHandler databaseConnectionConfigurationHandler;
	private GeographyMetaDataConfigurationHandler geographyMetaDataConfigurationHandler;
	private HealthThemesConfigurationHandler healthThemesConfigurationHandler;
	private DataTypeConfigurationHandler rifDataTypeConfigurationHandler;
	private HintsConfigurationHandler configurationHintsHandler;
	private DataSetConfigurationHandler dataSetConfigurationHandler;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study content handler.
     */
	public DataLoaderToolConfigurationHandler() {
		dataLoaderToolConfiguration = DataLoaderToolConfiguration.newInstance();
				
		setSingularRecordName("rif_data_loader_settings");

		databaseConnectionConfigurationHandler
			= new DatabaseConnectionConfigurationHandler();
		geographyMetaDataConfigurationHandler
			= new GeographyMetaDataConfigurationHandler();
		healthThemesConfigurationHandler
			= new HealthThemesConfigurationHandler();
		rifDataTypeConfigurationHandler 
			= new DataTypeConfigurationHandler();
		
		configurationHints = new ConfigurationHints();
		configurationHintsHandler.setConfigurationHints(configurationHints);
		configurationHintsHandler = new HintsConfigurationHandler();
		configurationHintsHandler.setDataTypeFactory(rifDataTypeFactory);		

		dataSetConfigurationHandler
			= new DataSetConfigurationHandler();		

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
		geographyMetaDataConfigurationHandler.initialise(
			outputStream, 
			commentInjector);
		healthThemesConfigurationHandler.initialise(
			outputStream, 
			commentInjector);		
		rifDataTypeConfigurationHandler.initialise(
			outputStream, 
			commentInjector);	
		configurationHintsHandler.initialise(
			outputStream, 
			commentInjector);		
		dataSetConfigurationHandler.initialise(
			outputStream, 
			commentInjector);	
	}

	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
		databaseConnectionConfigurationHandler.initialise(outputStream);
		geographyMetaDataConfigurationHandler.initialise(outputStream);
		rifDataTypeConfigurationHandler.initialise(outputStream);
		configurationHintsHandler.initialise(outputStream);		
		dataSetConfigurationHandler.initialise(outputStream);
	}
		
// ==========================================
// Section Accessors and Mutators
// ==========================================
    	
	public DataLoaderToolConfiguration getDataLoaderToolConfiguration() {
		return dataLoaderToolConfiguration;
	}
	
	/**
	 * Gets the disease mapping study.
	 *
	 * @return the disease mapping study
	 */

	public void writeXML(final DataLoaderToolConfiguration dataLoaderToolConfiguration)
		throws IOException {
			
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeStartXML();		

		String recordType = getSingularRecordName();
		xmlUtility.writeRecordStartTag(recordType);

		databaseConnectionConfigurationHandler.writeXML(
			dataLoaderToolConfiguration.getDatabaseConnectionConfiguration());
		geographyMetaDataConfigurationHandler.writeXML(
			dataLoaderToolConfiguration.getGeographyMetaData());
		rifDataTypeConfigurationHandler.writeXML(
			dataLoaderToolConfiguration.getRIFDataTypeFactory());
		configurationHintsHandler.writeXML(
			dataLoaderToolConfiguration.getConfigurationHints());
		
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
			if (databaseConnectionConfigurationHandler.isSingularRecordName(qualifiedName)) {
				assignDelegatedHandler(databaseConnectionConfigurationHandler);				
			}
			else if (geographyMetaDataConfigurationHandler.isPluralRecordName(qualifiedName)) {
				assignDelegatedHandler(geographyMetaDataConfigurationHandler);				
			}
			else if (rifDataTypeConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(rifDataTypeConfigurationHandler);
			}
			else if (configurationHintsHandler.isSingularRecordTypeApplicable(qualifiedName)) {
				configurationHintsHandler.setDataTypeFactory(
					rifDataTypeConfigurationHandler.getRIFDataTypeFactory());
				assignDelegatedHandler(configurationHintsHandler);
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
					RIFDatabaseConnectionParameters databaseConnectionsConfiguration
						= databaseConnectionConfigurationHandler.getDatabaseConnectionParameters();
					dataLoaderToolConfiguration.setDatabaseConnectionConfiguration(databaseConnectionsConfiguration);
				}
				else if (currentDelegatedHandler == geographyMetaDataConfigurationHandler) {
					DLGeographyMetaData geographyMetaData
						= geographyMetaDataConfigurationHandler.getGeographyMetaData();
					dataLoaderToolConfiguration.setGeographyMetaData(geographyMetaData);
				}
				else if (currentDelegatedHandler == rifDataTypeConfigurationHandler) {
					dataLoaderToolConfiguration.setRIFDataTypeFactory(
						rifDataTypeConfigurationHandler.getRIFDataTypeFactory());
				}
				else if (currentDelegatedHandler == configurationHintsHandler) {
					dataLoaderToolConfiguration.setConfigurationHints(configurationHints);
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
