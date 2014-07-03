
package rifServices.io;

import rifServices.businessConceptLayer.Parameter;
import rifServices.system.RIFServiceMessages;
import rifServices.util.HTMLUtility;
import rifServices.util.XMLUtility;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
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


class ParameterContentHandler 
	extends AbstractRIFConceptContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The parameters. */
	private ArrayList<Parameter> parameters;
	
	/** The current parameter. */
	private Parameter currentParameter;
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new parameter content handler.
     */
	public ParameterContentHandler() {
		
		parameters = new ArrayList<Parameter>();
		setSingularRecordName("parameter");
		setPluralRecordName("parameters");
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    /**
     * Gets the parameters.
     *
     * @return the parameters
     */
	public ArrayList<Parameter> getParameters() {
		
		return parameters;
	}
	
	/**
	 * Write xml.
	 *
	 * @param parameters the parameters
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final ArrayList<Parameter> parameters) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag("parameters");
		for (Parameter parameter : parameters) {
			writeXMLParameter(parameter);
		}
		xmlUtility.writeRecordEndTag("parameters");
	}
	
	/**
	 * Write xml parameter.
	 *
	 * @param parameter the parameter
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXMLParameter(
		final Parameter parameter) 
		throws IOException {

		String recordName = getSingularRecordName();
		XMLUtility xmlUtility = getXMLUtility();

		xmlUtility.writeRecordStartTag(recordName);
		xmlUtility.writeField(recordName, "name", parameter.getName());
		xmlUtility.writeField(recordName, "value", parameter.getValue());
		xmlUtility.writeRecordEndTag(recordName);		
	}
	
	/**
	 * Write html.
	 *
	 * @param parameters the parameters
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeHTML(
		final ArrayList<Parameter> parameters) 
		throws IOException {

		HTMLUtility htmlUtility = getHTMLUtility();
		
		String parametersLabel
			= RIFServiceMessages.getMessage("parameters.label");
		int recordHeaderLevel = getRecordHeaderLevel();
		htmlUtility.writeHeader(recordHeaderLevel, parametersLabel);
		if (parameters.size() == 0) {
			String none
				= RIFServiceMessages.getMessage("general.emptyList.none");
			htmlUtility.writeParagraph(none);
		}
		else {
			htmlUtility.beginInvisibleTable();
			
			//write out header row
			String nameFieldLabel
				= RIFServiceMessages.getMessage("parameter.name.label");
			String valueFieldLabel
				= RIFServiceMessages.getMessage("parameter.value.label");
			htmlUtility.beginRow();
			htmlUtility.writeBoldColumnValue(nameFieldLabel);
			htmlUtility.writeBoldColumnValue(valueFieldLabel);			
			htmlUtility.endRow();
			
			//write out data rows
			for (Parameter parameter : parameters) {
				htmlUtility.beginRow();
				htmlUtility.writeColumnValue(parameter.getName());
				htmlUtility.writeColumnValue(parameter.getValue());
				htmlUtility.endRow();
			}
			htmlUtility.endTable();			
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

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isPluralRecordName(qualifiedName) == true) {
			parameters.clear();
			activate();
		}
		else if (isSingularRecordName(qualifiedName) == true) {
			currentParameter = Parameter.newInstance();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
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
			parameters.add(currentParameter);
		}
		else if (equalsFieldName("name", qualifiedName) == true) {
			currentParameter.setName(getCurrentFieldValue());
		}
		else if (equalsFieldName("value", qualifiedName) == true) {
			currentParameter.setValue(getCurrentFieldValue());						
		}
	}

}
