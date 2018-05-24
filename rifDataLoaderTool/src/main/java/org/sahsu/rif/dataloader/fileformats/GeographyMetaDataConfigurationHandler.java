package org.sahsu.rif.dataloader.fileformats;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.GeographicalResolutionLevel;
import org.sahsu.rif.dataloader.concepts.Geography;
import org.sahsu.rif.dataloader.concepts.GeographyMetaData;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.generic.presentation.HTMLUtility;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceExceptionFactory;
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

public class GeographyMetaDataConfigurationHandler
	extends AbstractDataLoaderConfigurationHandler {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private GeographyMetaData geographyMetaData;
	private Geography currentGeography;
	private GeographicalResolutionLevel currentGeographicalResolution;
	
	private String geographicalResolutionLevelTag;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GeographyMetaDataConfigurationHandler() {
		geographyMetaData = new GeographyMetaData();
		
		setSingularRecordName("geography_meta_data");
		
		geographicalResolutionLevelTag = "geographical_resolution_level";
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public GeographyMetaData getGeographyMetaData() {
		return geographyMetaData;
	}
	
	public Geography getGeography(final String geographyName) {
		return geographyMetaData.getGeography(geographyName);
	}
	
	public String getHTML( 
		final GeographyMetaData geographyMetaData) 
		throws RIFServiceException {

		String htmlText  = "";
		
		try {
			
			ArrayList<Geography> geographies
				= geographyMetaData.getGeographies();

			ByteArrayOutputStream outputStream
				= new ByteArrayOutputStream();

			HTMLUtility htmlUtility = new HTMLUtility();
			htmlUtility.initialise(outputStream, "UTF-8");
		
			htmlUtility.beginDocument();
			htmlUtility.beginBody();
		
			String geographyMetaDataHeaderText
				= RIFDataLoaderToolMessages.getMessage("dlGeographyMetaData.label");
			htmlUtility.writeHeader(1, geographyMetaDataHeaderText);
	
			String geographicalResolutionsHeaderText
				= RIFDataLoaderToolMessages.getMessage("dlGeographicalResolution.plural.label");
	
			String levelOrderHeaderText
				= RIFDataLoaderToolMessages.getMessage("dlGeographicalResolution.order.label");

			String levelDisplayNameHeaderText
				= RIFDataLoaderToolMessages.getMessage("dlGeographicalResolution.displayName.label");
			String levelDatabaseNameHeaderText
				= RIFDataLoaderToolMessages.getMessage("dlGeographicalResolution.databaseFieldName.label");

			for (Geography geography : geographies) {
				htmlUtility.writeHeader(2, geography.getDisplayName());
				htmlUtility.writeHeader(3, geographicalResolutionsHeaderText);
							
				htmlUtility.beginTable();

				
				ArrayList<GeographicalResolutionLevel> levels
					= geography.getLevels();

				htmlUtility.beginRow();				
				htmlUtility.writeBoldColumnValue(levelOrderHeaderText);
				htmlUtility.writeBoldColumnValue(levelDisplayNameHeaderText);
				htmlUtility.writeBoldColumnValue(levelDatabaseNameHeaderText);
				htmlUtility.endRow();
				
				for (GeographicalResolutionLevel level : levels) {
					htmlUtility.beginRow();
					htmlUtility.writeColumnValue(String.valueOf(level.getOrder()));
					htmlUtility.writeBoldColumnValue(level.getDisplayName());
					htmlUtility.writeColumnValue(level.getDatabaseFieldName());
					htmlUtility.endRow();
				}					
				htmlUtility.endTable();					
			}
		
			htmlUtility.endBody();
			htmlUtility.endDocument();
			htmlText
				= new String(outputStream.toByteArray(), "UTF-8");	
			outputStream.close();
		}
    	catch(Exception exception) {
    		//in the event of an exception, just write instruction
    		//text as normal
    		RIFServiceExceptionFactory factory
    			= new RIFServiceExceptionFactory();
    		factory.createProblemCreatingHTML();
    	}		
		return htmlText;
	}
	
	public void writeXML(final GeographyMetaData geographyMetaData) 
		throws IOException {
		
		XMLUtility xmlUtility = getXMLUtility();
				
		String rifGeographyMetaDataTag = getSingularRecordName();		
		xmlUtility.writeRecordStartTag(rifGeographyMetaDataTag);
		xmlUtility.writeField(
				rifGeographyMetaDataTag, 
				"file_path", 
				geographyMetaData.getFilePath());
	
		xmlUtility.writeRecordStartTag("geographies");
		ArrayList<Geography> geographies = geographyMetaData.getGeographies();
		for (Geography geography : geographies) {
			xmlUtility.writeRecordStartTag("geography");
					
			xmlUtility.writeField("geography", "name", geography.getName());
			
			ArrayList<GeographicalResolutionLevel> levels
				= geography.getLevels();
			for (GeographicalResolutionLevel level : levels) {
				xmlUtility.writeRecordStartTag(geographicalResolutionLevelTag);

				xmlUtility.writeField(
					geographicalResolutionLevelTag, 
					"order", 
					String.valueOf(level.getOrder()));

				xmlUtility.writeField(
					geographicalResolutionLevelTag, 
					"display_name", 
					level.getDisplayName());

				xmlUtility.writeField(
					geographicalResolutionLevelTag, 
					"database_field_name", 
					level.getDatabaseFieldName());
				
				xmlUtility.writeRecordEndTag(geographicalResolutionLevelTag);
			}
			xmlUtility.writeRecordEndTag("geography");			
		}
		
		xmlUtility.writeRecordEndTag("geographies");
		
		xmlUtility.writeRecordEndTag(rifGeographyMetaDataTag);		
	}
	
	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (equalsFieldName(getSingularRecordName(), qualifiedName) == true) {
			activate();
		}
		if (equalsFieldName("geographies", qualifiedName) == true) {
			geographyMetaData.clearGeographies();
		}
		if (equalsFieldName("geography", qualifiedName) == true) {
			currentGeography = Geography.newInstance();
		}
		else if (equalsFieldName("geographical_resolution_level", qualifiedName) == true) {
			currentGeographicalResolution = GeographicalResolutionLevel.newInstance();
		}
	}
	
	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {

		if (equalsFieldName(getSingularRecordName(), qualifiedName) == true) {
			deactivate();
		}
		if (equalsFieldName("geography", qualifiedName) == true) {
			geographyMetaData.addGeography(currentGeography);
		}		
		if (equalsFieldName("file_path", qualifiedName) == true) {
			geographyMetaData.setFilePath(getCurrentFieldValue());
		}		
		else if (equalsFieldName("name", qualifiedName) == true) {
			currentGeography.setName(getCurrentFieldValue());
		}	
		else if (equalsFieldName(geographicalResolutionLevelTag, qualifiedName) == true) {
			currentGeography.addLevel(currentGeographicalResolution);
		}		
		else if (equalsFieldName("order", qualifiedName) == true) {
			if (currentGeographicalResolution == null) {				
				System.out.println("GMDCH endElement order 2 resolution null currentValue=="+getCurrentFieldValue()+"==");
			}
			currentGeographicalResolution.setOrder(Integer.valueOf(getCurrentFieldValue()));
		}
		else if (equalsFieldName("display_name", qualifiedName) == true) {
			currentGeographicalResolution.setDisplayName(getCurrentFieldValue());
		}
		else if (equalsFieldName("database_field_name", qualifiedName) == true) {
			currentGeographicalResolution.setDatabaseFieldName(getCurrentFieldValue());
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

}


