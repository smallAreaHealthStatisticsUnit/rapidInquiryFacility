
package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.RIFWorkflowConfiguration;


import rifDataLoaderTool.businessConceptLayer.RIFWorkflowCollection;
import rifDataLoaderTool.businessConceptLayer.DataSet;

import rifDataLoaderTool.businessConceptLayer.CleanWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.ConvertWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.OptimiseWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.CheckWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.PublishWorkflowConfiguration;

import rifServices.fileFormats.XMLCommentInjector;
import rifServices.fileFormats.XMLUtility;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


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


final class RIFWorkflowConfigurationHandler 
	extends AbstractWorkflowConfigurationHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	private RIFWorkflowCollection rifWorkflowCollection;
	
	private CleanWorkflowConfigurationHandler cleanWorkflowConfigurationHandler;
	private ConvertWorkflowConfigurationHandler convertWorkflowConfigurationHandler;
	private OptimiseWorkflowConfigurationHandler optimiseWorkflowConfigurationHandler;
	private CheckWorkflowConfigurationHandler checkWorkflowConfigurationHandler;
	private PublishWorkflowConfigurationHandler publishWorkflowConfigurationHandler;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study content handler.
     */
	public RIFWorkflowConfigurationHandler() {
		
		setSingularRecordName("rif_workflow");
			
		rifWorkflowCollection = RIFWorkflowCollection.newInstance();
		cleanWorkflowConfigurationHandler = new CleanWorkflowConfigurationHandler();		
		convertWorkflowConfigurationHandler = new ConvertWorkflowConfigurationHandler();
		optimiseWorkflowConfigurationHandler = new OptimiseWorkflowConfigurationHandler();
		checkWorkflowConfigurationHandler = new CheckWorkflowConfigurationHandler();
		publishWorkflowConfigurationHandler = new PublishWorkflowConfigurationHandler();
		
	}


	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
		
		cleanWorkflowConfigurationHandler.initialise(outputStream, commentInjector);
		convertWorkflowConfigurationHandler.initialise(outputStream, commentInjector);
		optimiseWorkflowConfigurationHandler.initialise(outputStream, commentInjector);
		checkWorkflowConfigurationHandler.initialise(outputStream, commentInjector);
		publishWorkflowConfigurationHandler.initialise(outputStream, commentInjector);		
	}

	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
		cleanWorkflowConfigurationHandler.initialise(outputStream);
		convertWorkflowConfigurationHandler.initialise(outputStream);
		optimiseWorkflowConfigurationHandler.initialise(outputStream);
		checkWorkflowConfigurationHandler.initialise(outputStream);
		publishWorkflowConfigurationHandler.initialise(outputStream);		
	}
	
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
    	
	/**
	 * Gets the disease mapping study.
	 *
	 * @return the disease mapping study
	 */
	public RIFWorkflowCollection getRIFWorkflowCollection() {
		return rifWorkflowCollection;
	}

	public void writeXML(
		final RIFWorkflowCollection rifWorkflowCollection)
		throws IOException {
			
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeStartXML();		

		xmlUtility.writeRecordStartTag("rif_data_loader_submission");

		xmlUtility.writeRecordStartTag("data_sets");
		ArrayList<DataSet> dataSets
			= rifWorkflowCollection.getDataSets();
		for (DataSet dataSet : dataSets) {
			xmlUtility.writeRecordStartTag("data_set");
			xmlUtility.writeField(
				"data_set", 
				"name", 
				dataSet.getSourceName());		
			xmlUtility.writeRecordEndTag("data_set");			
		}				
		xmlUtility.writeRecordEndTag("data_sets");
		
		xmlUtility.writeRecordStartTag("rif_workflow");
	
		RIFWorkflowConfiguration rifWorkflowConfiguration
			= rifWorkflowCollection.getRIFWorkflowConfiguration();
				
		CleanWorkflowConfiguration cleanWorkflowConfiguration
			= rifWorkflowConfiguration.getCleanWorkflowConfiguration();
		cleanWorkflowConfigurationHandler.writeXML(cleanWorkflowConfiguration);
			
		ConvertWorkflowConfiguration convertWorkflowConfiguration
			= rifWorkflowConfiguration.getConvertWorkflowConfiguration();
		convertWorkflowConfigurationHandler.writeXML(convertWorkflowConfiguration);

		OptimiseWorkflowConfiguration optimiseWorkflowConfiguration
			= rifWorkflowConfiguration.getOptimiseWorkflowConfiguration();
		optimiseWorkflowConfigurationHandler.writeXML(optimiseWorkflowConfiguration);
				
		CheckWorkflowConfiguration checkWorkflowConfiguration
			= rifWorkflowConfiguration.getCheckWorkflowConfiguration();
		checkWorkflowConfigurationHandler.writeXML(checkWorkflowConfiguration);
		
		PublishWorkflowConfiguration publishWorkflowConfiguration
			= rifWorkflowConfiguration.getPublishWorkflowConfiguration();
		publishWorkflowConfigurationHandler.writeXML(publishWorkflowConfiguration);

		xmlUtility.writeRecordEndTag("rif_workflow");		
		xmlUtility.writeRecordEndTag("rif_data_loader_submission");		
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
			AbstractWorkflowConfigurationHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.startElement(
				nameSpaceURI, 
				localName, 
				qualifiedName, 
				attributes);
		}
		else {
			
			//check to see if handlers could be assigned to delegate parsing			
			if (cleanWorkflowConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(cleanWorkflowConfigurationHandler);
			}
			else if (convertWorkflowConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(convertWorkflowConfigurationHandler);
			}
			else if (optimiseWorkflowConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(optimiseWorkflowConfigurationHandler);
			}
			else if (checkWorkflowConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(checkWorkflowConfigurationHandler);
			}
			else if (publishWorkflowConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(publishWorkflowConfigurationHandler);
			}
			
			
			//delegate to a handler.  If not, then scan for fields relating to this handler
			if (isDelegatedHandlerAssigned()) {

				AbstractWorkflowConfigurationHandler currentDelegatedHandler
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
			AbstractWorkflowConfigurationHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
			
			RIFWorkflowConfiguration rifWorkflowConfiguration
				= rifWorkflowCollection.getRIFWorkflowConfiguration();
			
			if (currentDelegatedHandler.isActive() == false) {
				if (currentDelegatedHandler == cleanWorkflowConfigurationHandler) {
					CleanWorkflowConfiguration cleanWorkflowConfiguration
						= cleanWorkflowConfigurationHandler.getCleanWorkflowConfiguration();
					rifWorkflowConfiguration.setCleaningWorkflowConfiguration(cleanWorkflowConfiguration);			
				}
				else if (currentDelegatedHandler == convertWorkflowConfigurationHandler) {

					ConvertWorkflowConfiguration convertWorkflowConfiguration
						= convertWorkflowConfigurationHandler.getConvertWorkflowConfiguration();
					rifWorkflowConfiguration.setConvertWorkflowConfiguration(convertWorkflowConfiguration);					
				}
				else if (currentDelegatedHandler == optimiseWorkflowConfigurationHandler) {

					OptimiseWorkflowConfiguration optimiseWorkflowConfiguration
						= optimiseWorkflowConfigurationHandler.getOptimiseWorkflowConfiguration();
					rifWorkflowConfiguration.setOptimiseWorkflowConfiguration(optimiseWorkflowConfiguration);				
				}
				else if (currentDelegatedHandler == checkWorkflowConfigurationHandler) {

					CheckWorkflowConfiguration checkWorkflowConfiguration
						= checkWorkflowConfigurationHandler.getCheckWorkflowConfiguration();
					rifWorkflowConfiguration.setCheckWorkflowConfiguration(checkWorkflowConfiguration);				
				}					
				else if (currentDelegatedHandler == publishWorkflowConfigurationHandler) {

					PublishWorkflowConfiguration publishWorkflowConfiguration
						= publishWorkflowConfigurationHandler.getPublishWorkflowConfiguration();
					rifWorkflowConfiguration.setPublishWorkflowConfiguration(publishWorkflowConfiguration);				
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
}
