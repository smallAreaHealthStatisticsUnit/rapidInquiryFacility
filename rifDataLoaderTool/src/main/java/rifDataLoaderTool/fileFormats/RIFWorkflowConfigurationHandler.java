
package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.RIFWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.CleanWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.ConvertWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataSource;
import rifServices.fileFormats.XMLCommentInjector;

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
	private RIFWorkflowConfiguration rifWorkflowConfiguration;
	
	private DataSourceConfigurationHandler dataSourceConfigurationHandler;
	private CleanWorkflowConfigurationHandler cleanWorkflowConfigurationHandler;
	private ConvertWorkflowConfigurationHandler convertWorkflowConfigurationHandler;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study content handler.
     */
	public RIFWorkflowConfigurationHandler() {
		
		setSingularRecordName("rif_workflow");
		
		dataSourceConfigurationHandler = new DataSourceConfigurationHandler();
		cleanWorkflowConfigurationHandler = new CleanWorkflowConfigurationHandler();		
		convertWorkflowConfigurationHandler = new ConvertWorkflowConfigurationHandler();
	}


	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
		
		dataSourceConfigurationHandler.initialise(outputStream, commentInjector);
		cleanWorkflowConfigurationHandler.initialise(outputStream, commentInjector);
		convertWorkflowConfigurationHandler.initialise(outputStream, commentInjector);
	}

	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
		dataSourceConfigurationHandler.initialise(outputStream);
		cleanWorkflowConfigurationHandler.initialise(outputStream);
		convertWorkflowConfigurationHandler.initialise(outputStream);
	}
	
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
    	
	/**
	 * Gets the disease mapping study.
	 *
	 * @return the disease mapping study
	 */
	public RIFWorkflowConfiguration getConfiguration() {
		return rifWorkflowConfiguration;
	}
	
	/**
	 * Write xml.
	 *
	 * @param diseaseMappingStudy the disease mapping study
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final RIFWorkflowConfiguration rifWorkflowConfiguration)
		throws IOException {
			
		ArrayList<DataSource> dataSources
			= rifWorkflowConfiguration.getDataSources();
		dataSourceConfigurationHandler.writeXML(dataSources);
		
		CleanWorkflowConfiguration cleanWorkflowConfiguration
			= cleanWorkflowConfigurationHandler.getCleanWorkflowConfiguration();
		cleanWorkflowConfigurationHandler.writeXML(cleanWorkflowConfiguration);
		
		ConvertWorkflowConfiguration convertWorkflowConfiguration
			= convertWorkflowConfigurationHandler.getConvertWorkflowConfiguration();
		convertWorkflowConfigurationHandler.writeXML(convertWorkflowConfiguration);
		
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
			if (cleanWorkflowConfigurationHandler.isSingularRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(cleanWorkflowConfigurationHandler);
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
			else if (isSingularRecordName(qualifiedName) == true) {

				activate();
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
