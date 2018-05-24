
package org.sahsu.rif.dataloader.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.CleaningRule;
import org.sahsu.rif.dataloader.concepts.FieldActionPolicy;
import org.sahsu.rif.dataloader.concepts.RIFDataType;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.concepts.ValidationRule;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.generic.system.RIFServiceException;
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


final public class DataTypeConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	private ArrayList<String> errorMessages;
	private RIFDataTypeFactory rifDataTypeFactory;
	private RIFDataType currentRIFDataType;
	private FieldCleaningPolicyConfigurationHandler cleaningPolicyConfigurationHandler;
	private FieldValidatingPolicyConfigurationHandler validatingPolicyConfigurationHandler;
	
	private String currentIdentifier;
	private String currentName;
	private String currentDescription;
	private String currentTimeStampPhrase;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study content handler.
     */
	public DataTypeConfigurationHandler() {
		errorMessages = new ArrayList<String>();
		setPluralRecordName("rif_data_types");
		setSingularRecordName("rif_data_type");
		rifDataTypeFactory = RIFDataTypeFactory.newInstance();	
		rifDataTypeFactory.populateFactoryWithBuiltInTypes();
		cleaningPolicyConfigurationHandler
			= new FieldCleaningPolicyConfigurationHandler();
		validatingPolicyConfigurationHandler
			= new FieldValidatingPolicyConfigurationHandler();

		String rifDataTypeComment
			= RIFDataLoaderToolMessages.getMessage("abstractRIFDataType.toolTipText");
		setComment(
			"rif_data_type", 
			rifDataTypeComment);
	}


	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
		cleaningPolicyConfigurationHandler.initialise(
			outputStream, 
			commentInjector);
		validatingPolicyConfigurationHandler.initialise(
			outputStream, 
			commentInjector);
		
	}

	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
		
		cleaningPolicyConfigurationHandler.initialise(
			outputStream);
		validatingPolicyConfigurationHandler.initialise(
			outputStream);
		
	}
	
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
    	
	/**
	 * Gets the disease mapping study.
	 *
	 * @return the disease mapping study
	 */
	public RIFDataTypeFactory getDataTypeFactory() {
		return rifDataTypeFactory;
	}
	
	public void setDataTypeFactory(final RIFDataTypeFactory rifDataTypeFactory) {
		this.rifDataTypeFactory = rifDataTypeFactory;
	}
	
	public ArrayList<String> getErrorMessages() {
		return errorMessages;
	}
	
	public void writeXML(
		final RIFDataTypeFactory rifDataTypeFactory)
		throws IOException {
			
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(getPluralRecordName());
		ArrayList<RIFDataType> rifDataTypes
			= rifDataTypeFactory.getRegisteredDataTypes();
		for (RIFDataType rifDataType : rifDataTypes) {
			writeRIFDataType(rifDataType);
		}
		xmlUtility.writeRecordEndTag(getPluralRecordName());		
	}	

	private void writeRIFDataType(final RIFDataType rifDataType) 
		throws IOException {
		
		String recordTag = getSingularRecordName();
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(recordTag);
		xmlUtility.writeField(
			recordTag, 
			"identifier", 
			rifDataType.getIdentifier());

		xmlUtility.writeField(
			recordTag, 
			"name", 
			rifDataType.getName());
		
		xmlUtility.writeField(
			recordTag, 
			"description", 
			rifDataType.getDescription());

		xmlUtility.writeField(
			recordTag, 
			"last_modified", 
			getLastModifiedTimeStampPhrase(rifDataType.getLastModifiedTime()));		
		
		cleaningPolicyConfigurationHandler.writeXML(rifDataType);			
		validatingPolicyConfigurationHandler.writeXML(rifDataType);
		xmlUtility.writeRecordEndTag(recordTag);		
		
	}
	
	public void setRIFDataTypeFactory(final RIFDataTypeFactory rifDataTypeFactory) {
		this.rifDataTypeFactory = rifDataTypeFactory;
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
		
		if (isPluralRecordName(qualifiedName)) {
			activate();
		}
		else if (isSingularRecordName(qualifiedName)) {
			currentRIFDataType = RIFDataType.newInstance();
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
			if (cleaningPolicyConfigurationHandler.isSingularRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(cleaningPolicyConfigurationHandler);
			}
			else if (validatingPolicyConfigurationHandler.isSingularRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(validatingPolicyConfigurationHandler);
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
			try {
				RIFDataType existingRIFDataType
					= rifDataTypeFactory.getDataTypeFromName(currentName);
				if (existingRIFDataType == null) {
					//This must be new
					RIFDataType rifDataType
						= RIFDataType.newInstance();
					rifDataType.setIdentifier(currentIdentifier);
					rifDataType.setName(currentName);
					rifDataType.setDescription(currentDescription);
					rifDataType.setLastModifiedTime(getLastModifiedTimeStamp(currentTimeStampPhrase));
					rifDataTypeFactory.registerCustomDataType(
						currentRIFDataType, 
						false);					
				}
			}
			catch(RIFServiceException rifServiceException) {
				errorMessages.addAll(rifServiceException.getErrorMessages());
			}
		}
		else if (isDelegatedHandlerAssigned()) {
			AbstractDataLoaderConfigurationHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
						
			if (currentDelegatedHandler.isActive() == false) {
				if (currentDelegatedHandler == cleaningPolicyConfigurationHandler) {
					FieldActionPolicy fieldCleaningPolicy
						= cleaningPolicyConfigurationHandler.getFieldCleaningPolicy();
					currentRIFDataType.setFieldCleaningPolicy(fieldCleaningPolicy);
					ArrayList<CleaningRule> cleaningRules
						= cleaningPolicyConfigurationHandler.getCleaningRules();
					currentRIFDataType.setCleaningRules(cleaningRules);
					String cleaningFunctionName
						= cleaningPolicyConfigurationHandler.getCleaningFunctionName();
					currentRIFDataType.setCleaningFunctionName(cleaningFunctionName);			
					cleaningPolicyConfigurationHandler.resetPolicyAttributes();
				}
				else if (currentDelegatedHandler == validatingPolicyConfigurationHandler) {
					FieldActionPolicy fieldValidationPolicy
						= validatingPolicyConfigurationHandler.getFieldValidationPolicy();
					currentRIFDataType.setFieldValidationPolicy(fieldValidationPolicy);
					ArrayList<ValidationRule> validationRules
						= validatingPolicyConfigurationHandler.getValidationRules();
					currentRIFDataType.setValidationRules(validationRules);
					String validationFunctionName
						= validatingPolicyConfigurationHandler.getValidationFunctionName();
					currentRIFDataType.setValidationFunctionName(validationFunctionName);
					validatingPolicyConfigurationHandler.resetPolicyAttributes();
				}			
				else {
					assert false;
				}				
				
				//handler just finished				
				unassignDelegatedHandler();				
			}
		}
		else if (equalsFieldName("identifier", qualifiedName)) {
			currentIdentifier = getCurrentFieldValue();
			//currentRIFDataType.setIdentifier(getCurrentFieldValue());
		}
		else if (equalsFieldName("name", qualifiedName)) {
			currentName = getCurrentFieldValue();
			//currentRIFDataType.setName(getCurrentFieldValue());
		}
		else if (equalsFieldName("description", qualifiedName)) {
			currentDescription = getCurrentFieldValue();
			//currentRIFDataType.setDescription(getCurrentFieldValue());
		}
		else if (equalsFieldName("last_modified", qualifiedName)) {
			currentTimeStampPhrase = getCurrentFieldValue();
			//currentRIFDataType.setLastModifiedTime(getLastModifiedTimeStamp(timeStampPhrase));
		}		
		else {
			assert false;
		}		
	}
}
