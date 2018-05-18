
package org.sahsu.rif.dataloader.fileformats;

import org.sahsu.rif.dataloader.concepts.ConfigurationHints;
import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DatabaseConnectionsConfiguration;
import org.sahsu.rif.dataloader.concepts.Geography;
import org.sahsu.rif.dataloader.concepts.GeographyMetaData;
import org.sahsu.rif.dataloader.concepts.HealthTheme;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;

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

		rifDataTypeFactory = RIFDataTypeFactory.newInstance();
		rifDataTypeFactory.populateFactoryWithBuiltInTypes();
		
		databaseConnectionConfigurationHandler
			= new DatabaseConnectionConfigurationHandler();
		geographyMetaDataConfigurationHandler
			= new GeographyMetaDataConfigurationHandler();
		healthThemesConfigurationHandler
			= new HealthThemesConfigurationHandler();
		rifDataTypeConfigurationHandler 
			= new DataTypeConfigurationHandler();
		rifDataTypeConfigurationHandler.setDataTypeFactory(rifDataTypeFactory);
		
		configurationHints = new ConfigurationHints();
		configurationHintsHandler = new HintsConfigurationHandler();
		configurationHintsHandler.setConfigurationHints(configurationHints);
		configurationHintsHandler = new HintsConfigurationHandler();
		configurationHintsHandler.setDataTypeFactory(rifDataTypeFactory);		

		dataSetConfigurationHandler
			= new DataSetConfigurationHandler();
		dataSetConfigurationHandler.setDataTypeFactory(rifDataTypeFactory);

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
		
		ArrayList<HealthTheme> healthThemes
			= dataLoaderToolConfiguration.getHealthThemes();
		healthThemesConfigurationHandler.writeXML(healthThemes);		
		rifDataTypeConfigurationHandler.writeXML(
			dataLoaderToolConfiguration.getRIFDataTypeFactory());
		configurationHintsHandler.writeXML(
			dataLoaderToolConfiguration.getConfigurationHints());
		dataSetConfigurationHandler.writeXML(
			dataLoaderToolConfiguration.getAllDataSetConfigurations());
		xmlUtility.writeRecordEndTag(recordType);	
		
	}	
	
	private void resolveDependencies() {
		//We need to go through all the data set configurations and
		//figure out which geography and health theme to associate
		//with each of them.
		
		ArrayList<DataSetConfiguration> dataSetConfigurations
			= dataSetConfigurationHandler.getDataSetConfigurations();
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {
			
			String geographyName
				= dataSetConfigurationHandler.getGeographyName(dataSetConfiguration);
			Geography geography
				= geographyMetaDataConfigurationHandler.getGeography(geographyName);
			dataSetConfiguration.setGeography(geography);
			
			String healthThemeName
				= dataSetConfigurationHandler.getHealthThemeName(dataSetConfiguration);
			HealthTheme healthTheme
				= healthThemesConfigurationHandler.getHealthTheme(healthThemeName);
			dataSetConfiguration.setHealthTheme(healthTheme);
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
			else if (geographyMetaDataConfigurationHandler.isSingularRecordName(qualifiedName)) {
				assignDelegatedHandler(geographyMetaDataConfigurationHandler);				
			}
			else if (healthThemesConfigurationHandler.isPluralRecordName(qualifiedName)) {
				assignDelegatedHandler(healthThemesConfigurationHandler);				
			}
			else if (rifDataTypeConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(rifDataTypeConfigurationHandler);
			}
			else if (configurationHintsHandler.isSingularRecordTypeApplicable(qualifiedName)) {
				configurationHintsHandler.setDataTypeFactory(
					rifDataTypeConfigurationHandler.getDataTypeFactory());
				assignDelegatedHandler(configurationHintsHandler);
			}
			else if (dataSetConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(dataSetConfigurationHandler);
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
			resolveDependencies();
			
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
					DatabaseConnectionsConfiguration databaseConnectionsConfiguration
						= databaseConnectionConfigurationHandler.getDatabaseConnectionParameters();
					dataLoaderToolConfiguration.setDatabaseConnectionConfiguration(databaseConnectionsConfiguration);
				}
				else if (currentDelegatedHandler == geographyMetaDataConfigurationHandler) {
					GeographyMetaData geographyMetaData
						= geographyMetaDataConfigurationHandler.getGeographyMetaData();
					dataLoaderToolConfiguration.setGeographyMetaData(geographyMetaData);
				}
				
				else if (currentDelegatedHandler == healthThemesConfigurationHandler) {
					ArrayList<HealthTheme> healthThemes
						= healthThemesConfigurationHandler.getHealthThemes();
					dataLoaderToolConfiguration.setHealthThemes(healthThemes);
				}			
				else if (currentDelegatedHandler == rifDataTypeConfigurationHandler) {
					dataLoaderToolConfiguration.setRIFDataTypeFactory(
						rifDataTypeConfigurationHandler.getDataTypeFactory());
				}
				else if (currentDelegatedHandler == configurationHintsHandler) {
					ConfigurationHints hints = configurationHintsHandler.getConfigurationHints();				
					dataLoaderToolConfiguration.setConfigurationHints(hints);
				}
				else if (currentDelegatedHandler == dataSetConfigurationHandler) {
					ArrayList<DataSetConfiguration> denominatorDataSets
						= dataSetConfigurationHandler.getDataSetConfigurations(
							RIFSchemaArea.POPULATION_DENOMINATOR_DATA);
					dataLoaderToolConfiguration.setDenominatorDataSetConfigurations(denominatorDataSets);
					ArrayList<DataSetConfiguration> numeratorDataSets
						= dataSetConfigurationHandler.getDataSetConfigurations(
							RIFSchemaArea.HEALTH_NUMERATOR_DATA);
					dataLoaderToolConfiguration.setNumeratorDataSetConfigurations(numeratorDataSets);
					
					ArrayList<DataSetConfiguration> covariateDataSets
						= dataSetConfigurationHandler.getDataSetConfigurations(
						RIFSchemaArea.COVARIATE_DATA);
					dataLoaderToolConfiguration.setCovariateDataSetConfigurations(covariateDataSets);
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
