package org.sahsu.rif.dataloader.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.LinearWorkflow;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.concepts.WorkflowState;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

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


final class LinearWorkflowConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	private ArrayList<LinearWorkflow> linearWorkflows;
	private LinearWorkflow currentLinearWorkflow;
	private DataSetConfigurationHandler dataSetConfigurationHandler;
		
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study content handler.
     */
	public LinearWorkflowConfigurationHandler() {
		setPluralRecordName("linear_workflows");	
		setSingularRecordName("linear_workflow");
		currentLinearWorkflow = LinearWorkflow.newInstance();	
		dataSetConfigurationHandler = new DataSetConfigurationHandler();
		linearWorkflows = new ArrayList<LinearWorkflow>();
		
	}


	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
		dataSetConfigurationHandler.initialise(outputStream, commentInjector);
	}

	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
		dataSetConfigurationHandler.initialise(outputStream);
	}
	
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
    	
	/**
	 * Gets the disease mapping study.
	 *
	 * @return the disease mapping study
	 */
	public ArrayList<LinearWorkflow> getLinearWorkflows() {
		return linearWorkflows;
	}

	public void writeXML(
		final ArrayList<LinearWorkflow> linearWorkflows) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();

		String recordType = getPluralRecordName();
		xmlUtility.writeRecordStartTag(recordType);		
		for (LinearWorkflow linearWorkflow : linearWorkflows) {
			writeXML(linearWorkflow, false);
		}	
		xmlUtility.writeRecordEndTag(recordType);		
	}
			
	public void writeXML(
		final LinearWorkflow linearWorkflow,
		final boolean isIndependentDocument)
		throws IOException {
			
		XMLUtility xmlUtility = getXMLUtility();
		if (isIndependentDocument == true) {
			xmlUtility.writeStartXML();
		}

		String recordType = getSingularRecordName();
		xmlUtility.writeRecordStartTag(recordType);

		xmlUtility.writeField(
			recordType, 
			"start_workflow_state",
			linearWorkflow.getStartWorkflowState().getCode());

		xmlUtility.writeField(
			recordType, 
			"stop_workflow_state", 
			linearWorkflow.getStopWorkflowState().getCode());				
		ArrayList<DataSetConfiguration> dataSetConfigurations
			= linearWorkflow.getDataSetConfigurations();
		dataSetConfigurationHandler.writeXML(dataSetConfigurations);
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
	public void setRIFDataTypeFactory(final RIFDataTypeFactory rifDataTypeFactory) {
		dataSetConfigurationHandler.setDataTypeFactory(rifDataTypeFactory);		
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
			if (dataSetConfigurationHandler.isPluralRecordTypeApplicable(qualifiedName)) {
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
		
		if (isPluralRecordName(qualifiedName)) {
			deactivate();
		}
		else if (isSingularRecordName(qualifiedName)) {
			linearWorkflows.add(currentLinearWorkflow);
			currentLinearWorkflow.setNewRecord(false);
		}
		else if (isDelegatedHandlerAssigned()) {
			AbstractDataLoaderConfigurationHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
						
			if (currentDelegatedHandler.isActive() == false) {
				if (currentDelegatedHandler == dataSetConfigurationHandler) {
					ArrayList<DataSetConfiguration> dataSetConfigurations
						= dataSetConfigurationHandler.getDataSetConfigurations();
					currentLinearWorkflow.setDataSetConfigurations(dataSetConfigurations);
				}
				else {
					assert false;
				}				
				
				//handler just finished				
				unassignDelegatedHandler();				
			}
		}
		else if (equalsFieldName("start_workflow_state", qualifiedName)) {
			WorkflowState startWorkflowState
				= WorkflowState.getWorkflowStateFromCode(getCurrentFieldValue());
			currentLinearWorkflow.setStartWorkflowState(startWorkflowState);
		}			
		else if (equalsFieldName("stop_workflow_state", qualifiedName)) {
			WorkflowState stopWorkflowState
				= WorkflowState.getWorkflowStateFromCode(getCurrentFieldValue());
			currentLinearWorkflow.setStopWorkflowState(stopWorkflowState);
		}			
		else {
			assert false;
		}
	}
}
