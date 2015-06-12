package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.DataSet;


import rifServices.fileFormats.XMLUtility;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.io.IOException;

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

public final class DataSetConfigurationHandler 
	extends AbstractWorkflowConfigurationHandler {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	ArrayList<DataSet> dataSets;
	private DataSet currentDataSet;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetConfigurationHandler() {
		setPluralRecordName("data_sources");
		setSingularRecordName("data_source");
		
		dataSets = new ArrayList<DataSet>();
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
			dataSets.clear();
			activate();
		}
		else if (isSingularRecordName(qualifiedName) == true) {
			currentDataSet = DataSet.newInstance();
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
			dataSets.add(currentDataSet);
		}
		else if (equalsFieldName("name", qualifiedName) == true) {
			currentDataSet.setCoreDataSetName(getCurrentFieldValue());
		}
		else if (equalsFieldName("file", qualifiedName) == true) {
			currentDataSet.setSourceName(getCurrentFieldValue());					
		}
	}
	
	
	public ArrayList<DataSet> getdataSets() {
		return dataSets;		
	}
	
	public String getHTML(
		final DataSet dataSet) {
			
		return "";
	}	
	
	
	public String getHTML(
		final ArrayList<DataSet> dataSets) {
			
		return "";
	}	
	
	public void writeXML(final ArrayList<DataSet> dataSets) 
		throws IOException {
		
		this.dataSets = dataSets;
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordListStartTag("data_sources");
		for (DataSet dataSet : dataSets) {
			xmlUtility.writeRecordStartTag("data_source");
			xmlUtility.writeField(
				"data_source", 
				"name", 
				dataSet.getCoreDataSetName());
			xmlUtility.writeField(
				"data_source", 
				"file", 
				dataSet.getSourceName());
			xmlUtility.writeRecordEndTag("data_source");
		}
		
		xmlUtility.writeRecordListEndTag("data_sources");
		
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


