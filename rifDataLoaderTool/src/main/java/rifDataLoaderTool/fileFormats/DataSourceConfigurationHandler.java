package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.DataSource;
import rifDataLoaderTool.businessConceptLayer.RIFDataTypeInterface;
import rifDataLoaderTool.businessConceptLayer.CleaningRule;



import rifGenericLibrary.presentationLayer.HTMLUtility;
import rifServices.businessConceptLayer.HealthCode;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public final class DataSourceConfigurationHandler 
	extends AbstractWorkflowConfigurationHandler {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	ArrayList<DataSource> dataSources;
	private DataSource currentDataSource;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSourceConfigurationHandler() {
		setPluralRecordName("data_sources");
		setSingularRecordName("data_source");
		
		dataSources = new ArrayList<DataSource>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	

	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isPluralRecordName(qualifiedName) == true) {
			dataSources.clear();
			activate();
		}
		else if (isSingularRecordName(qualifiedName) == true) {
			currentDataSource = DataSource.newInstance();
		}
	}
	
	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {

		if (isPluralRecordName(qualifiedName) == true) {
			deactivate();
		}
		else if (isSingularRecordName(qualifiedName) == true) {
			dataSources.add(currentDataSource);
		}
		else if (equalsFieldName("name", qualifiedName) == true) {
			currentDataSource.setSourceName(getCurrentFieldValue());
		}
		else if (equalsFieldName("file", qualifiedName) == true) {
			currentDataSource.setSourceName(getCurrentFieldValue());					
		}
	}
	
	
	public ArrayList<DataSource> getDataSources() {
		return dataSources;		
	}
	
	public String getHTML(
		final DataSource dataSource) {
			
		return "";
	}	
	
	
	public String getHTML(
		final ArrayList<DataSource> dataSources) {
			
		return "";
	}	
	
	public void writeXML(final ArrayList<DataSource> dataSources) {
		this.dataSources = dataSources;
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


