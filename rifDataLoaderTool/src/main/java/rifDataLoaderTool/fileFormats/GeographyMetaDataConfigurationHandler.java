package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.DLGeographyMetaData;
import rifDataLoaderTool.businessConceptLayer.DLGeography;
import rifDataLoaderTool.businessConceptLayer.DLGeographicalResolutionLevel;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.system.RIFServiceException;

import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.presentationLayer.HTMLUtility;
import rifGenericLibrary.fileFormats.XMLUtility;

import java.util.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.io.*;


/**
 *
 *
 * <hr>
 * Copyright 2016 Imperial College London, developed by the Small Area
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
	private DLGeographyMetaData geographyMetaData;
	private DLGeography currentGeography;
	private DLGeographicalResolutionLevel currentGeographicalResolution;
	
	private String geographicalResolutionTag;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GeographyMetaDataConfigurationHandler() {
		geographyMetaData = new DLGeographyMetaData();
		
		setSingularRecordName("rifGeographyMetaDataTag");
		
		this.geographicalResolutionTag = "geographical_resolution_level";
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public DLGeographyMetaData getGeographyMetaData() {
		return geographyMetaData;
	}
	
	public String getHTML( 
		final DLGeographyMetaData geographyMetaData) 
		throws RIFServiceException {

		String htmlText  = "";
		
		try {
			
			ArrayList<DLGeography> geographies
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

			for (DLGeography geography : geographies) {
				htmlUtility.writeHeader(2, geography.getDisplayName());
				htmlUtility.writeHeader(3, geographicalResolutionsHeaderText);
							
				htmlUtility.beginTable();

				
				ArrayList<DLGeographicalResolutionLevel> levels
					= geography.getLevels();

				htmlUtility.beginRow();				
				htmlUtility.writeBoldColumnValue(levelOrderHeaderText);
				htmlUtility.writeBoldColumnValue(levelDisplayNameHeaderText);
				htmlUtility.writeBoldColumnValue(levelDatabaseNameHeaderText);
				htmlUtility.endRow();
				
				System.out.println("Geography Levels");
				for (DLGeographicalResolutionLevel level : levels) {
					htmlUtility.beginRow();
					htmlUtility.writeColumnValue(String.valueOf(level.getOrder()));
					htmlUtility.writeBoldColumnValue(level.getDisplayName());
					htmlUtility.writeColumnValue(level.getDatabaseFieldName());
					htmlUtility.endRow();
				}					
			}
			htmlUtility.endTable();					
		
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
	
	public void writeXML(final DLGeographyMetaData geographyMetaData) 
		throws IOException {
		
		XMLUtility xmlUtility = new XMLUtility();
		
		xmlUtility.writeStartXML();
		
		String rifGeographyMetaDataTag = "rif_geography_meta_data";		
		xmlUtility.writeRecordStartTag(rifGeographyMetaDataTag);
		xmlUtility.writeField(
				rifGeographyMetaDataTag, 
				"file_path", 
				geographyMetaData.getFilePath());
	
		xmlUtility.writeRecordStartTag("geographies");
		ArrayList<DLGeography> geographies = geographyMetaData.getGeographies();
		for (DLGeography geography : geographies) {
			xmlUtility.writeRecordStartTag("geography");
			
			
			xmlUtility.writeField("geography", "name", geography.getName());
			
			ArrayList<DLGeographicalResolutionLevel> levels
				= geography.getLevels();
			for (DLGeographicalResolutionLevel level : levels) {
				xmlUtility.writeRecordStartTag(geographicalResolutionTag);

				xmlUtility.writeField(
					geographicalResolutionTag, 
					"order", 
					String.valueOf(level.getOrder()));

				xmlUtility.writeField(
					geographicalResolutionTag, 
					"display_name", 
					level.getDisplayName());

				xmlUtility.writeField(
					geographicalResolutionTag, 
					"database_field_name", 
					level.getDatabaseFieldName());
				
				xmlUtility.writeRecordEndTag("geographical_resolution_level");
			}
			xmlUtility.writeRecordEndTag("geography");			
		}
		
		xmlUtility.writeRecordEndTag("geographies");
		
		xmlUtility.writeRecordEndTag("rif_geography_meta_data");		
	}
	
	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		System.out.println("GeoMetaData startElement tag==" + qualifiedName + "==");
		if (equalsFieldName("geography", qualifiedName) == true) {
			currentGeography = DLGeography.newInstance();
		}
		else if (equalsFieldName("geographical_resolutions", qualifiedName) == true) {
			currentGeography.clearLevels();
		}
		else if (equalsFieldName("geographical_resolution", qualifiedName) == true) {
			currentGeographicalResolution = DLGeographicalResolutionLevel.newInstance();
		}
	}
	
	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {

		if (equalsFieldName("geography", qualifiedName) == true) {
			geographyMetaData.addGeography(currentGeography);
		}		
		else if (equalsFieldName("name", qualifiedName) == true) {
			currentGeography.setName(getCurrentFieldValue());
		}	
		else if (equalsFieldName("geographical_resolution", qualifiedName) == true) {
			currentGeography.addLevel(currentGeographicalResolution);
		}		
		else if (equalsFieldName("order", qualifiedName) == true) {
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


