
package org.sahsu.rif.dataloader.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.ShapeFile;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.generic.system.Messages;
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


final class ShapeFileConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

// ==========================================
// Section Constants
// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
// Section Properties
// ==========================================
	private ArrayList<ShapeFile> shapeFiles;
	private ShapeFile currentShapeFile;
	private String unknownPhrase;
		
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study content handler.
     */
	public ShapeFileConfigurationHandler() {
		setPluralRecordName("shape_files");
		setSingularRecordName("shape_file");
		
		shapeFiles = new ArrayList<ShapeFile>();

		String shapeFileComment
			= RIFDataLoaderToolMessages.getMessage("shapeFile.toolTipText");
		setComment(
			"shape_file", 
			shapeFileComment);
		String shapeFileTotalAreaIdentifiersComment
			= RIFDataLoaderToolMessages.getMessage("shapeFile.totalAreaIdentifiers.toolTipText");
		setComment(
			"shape_file",
			"shape_file_total_area_identifiers",
			shapeFileTotalAreaIdentifiersComment);
		String shapeFileNameFieldComment
			= RIFDataLoaderToolMessages.getMessage("shapeFile.nameFieldName.toolTipText");	
		setComment(
			"shape_file",
			"shape_file_name_field",
			shapeFileNameFieldComment);	
		
		unknownPhrase
			= RIFDataLoaderToolMessages.getMessage("general.label.unknown");

	}

	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
	}

	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
	}
	
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
    	
	/**
	 * Gets the disease mapping study.
	 *
	 * @return the disease mapping study
	 */
	public ArrayList<ShapeFile> getShapeFiles() {
		return shapeFiles;
	}

	public void writeXML(
		final ArrayList<ShapeFile> shapeFiles)
		throws IOException {
			
		XMLUtility xmlUtility = getXMLUtility();
		
		xmlUtility.writeRecordStartTag(getPluralRecordName());
		String recordType = getSingularRecordName();		
		
		for (ShapeFile shapeFile : shapeFiles) {
			
			xmlUtility.writeRecordStartTag(recordType);
			
			xmlUtility.writeField(
				recordType, 
				"shape_file_description", 
				shapeFile.getShapeFileDescription());			

			ArrayList<String> shapeFileComponentPaths
				= shapeFile.getShapeFileComponentPaths();
			for (String shapeFileComponentPath : shapeFileComponentPaths) {
				xmlUtility.writeField(
					recordType, 
					"shape_file_component_file_path", 
					shapeFileComponentPath);				
			}

			int totalAreaIdentifiers
				= shapeFile.getTotalAreaIdentifiers();
			if (totalAreaIdentifiers == ShapeFile.UNKNOWN_TOTAL_AREA_IDENTIFIERS) {

				xmlUtility.writeField(
					recordType, 
					"shape_file_total_area_identifiers", 
					unknownPhrase);				
			}
			else {
				xmlUtility.writeField(
					recordType, 
					"shape_file_total_area_identifiers", 
					String.valueOf(totalAreaIdentifiers));					
			}
			
			xmlUtility.writeField(
				recordType, 
				"shape_file_area_identifier_field", 
				shapeFile.getAreaIdentifierFieldName());				

			xmlUtility.writeField(
				recordType, 
				"shape_file_name_field", 
				shapeFile.getNameFieldName());				


			xmlUtility.writeField(
				recordType, 
				"shape_file_projection", 
				shapeFile.getProjection());				
			
			ArrayList<String> shapeFileFieldNames
				= shapeFile.getShapeFileFieldNames();
			for (String shapeFileFieldName : shapeFileFieldNames) {
				xmlUtility.writeField(
					recordType, 
					"shape_file_field_name", 
					shapeFileFieldName);				
			}
			xmlUtility.writeRecordEndTag(recordType);
		}
		xmlUtility.writeRecordEndTag(getPluralRecordName());
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
			currentShapeFile = ShapeFile.newInstance();
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
			shapeFiles.add(currentShapeFile);
		}
		else if (equalsFieldName("shape_file_description", qualifiedName)) {
			currentShapeFile.setShapeFileDescription(getCurrentFieldValue());
		}		
		else if (equalsFieldName("shape_file_component_file_path", qualifiedName)) {
			currentShapeFile.addShapeFileComponentPath(getCurrentFieldValue());
		}
		else if (equalsFieldName("shape_file_field_name", qualifiedName)) {
			currentShapeFile.addShapeFileFieldName(getCurrentFieldValue());
		}
		else if (equalsFieldName("shape_file_area_identifier_field", qualifiedName)) {
			currentShapeFile.setAreaIdentifierFieldName(getCurrentFieldValue());
		}
		else if (equalsFieldName("shape_file_name_field", qualifiedName)) {
			currentShapeFile.setNameFieldName(getCurrentFieldValue());
		}	
		else if (equalsFieldName("shape_file_field_name", qualifiedName)) {
			currentShapeFile.addShapeFileFieldName(getCurrentFieldValue());
		}
		else if (equalsFieldName("shape_file_projection", qualifiedName)) {
			currentShapeFile.setProjection(getCurrentFieldValue());
		}
		else if (equalsFieldName("shape_file_total_area_identifiers", qualifiedName)) {
			String totalAreaIdentifiersPhrase
				= getCurrentFieldValue();
			Collator collator
				= GENERIC_MESSAGES.getCollator();
			if (collator.equals(
				totalAreaIdentifiersPhrase, 
				unknownPhrase)) {
				currentShapeFile.setTotalAreaIdentifiers(ShapeFile.UNKNOWN_TOTAL_AREA_IDENTIFIERS);
			}
			else {
				Integer totalAreaIdentifiers
					= Integer.valueOf(totalAreaIdentifiersPhrase);
				currentShapeFile.setTotalAreaIdentifiers(totalAreaIdentifiers);
			}
		}
		else {
			assert false;
		}
	}
}
