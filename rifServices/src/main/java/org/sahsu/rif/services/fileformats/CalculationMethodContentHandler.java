
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.ParameterContentHandler;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.generic.presentation.HTMLUtility;
import org.sahsu.rif.services.concepts.CalculationMethod;
import org.sahsu.rif.services.system.RIFServiceMessages;
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


final class CalculationMethodContentHandler 
	extends AbstractXMLContentHandler {


// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The calculation methods. */
	private ArrayList<CalculationMethod> calculationMethods;
	
	/** The current calculation method. */
	private CalculationMethod currentCalculationMethod;
    
	/** The parameter content handler. */
	private ParameterContentHandler parameterContentHandler;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new calculation method content handler.
     */
	public CalculationMethodContentHandler() {
		
		setSingularRecordName("calculation_method");
		setPluralRecordName("calculation_methods");
		
		parameterContentHandler = new ParameterContentHandler();
		calculationMethods = new ArrayList<CalculationMethod>();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
	/**
	 * Gets the calculation methods.
	 *
	 * @return the calculation methods
	 */
	public ArrayList<CalculationMethod> getCalculationMethods() {
		
		return calculationMethods;
	}

	/**
	 * Write xml.
	 *
	 * @param calculationMethods the calculation methods
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final ArrayList<CalculationMethod> calculationMethods) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(getPluralRecordName());
		
		for (CalculationMethod calculationMethod : calculationMethods) {
			writeXMLCalculationMethod(calculationMethod);
		}

		xmlUtility.writeRecordEndTag(getPluralRecordName());
	}
	
	/**
	 * Write xml calculation method.
	 *
	 * @param calculationMethod the calculation method
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeXMLCalculationMethod(
		final CalculationMethod calculationMethod) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();
		
		String recordName = getSingularRecordName();
		xmlUtility.writeRecordStartTag(recordName);

		xmlUtility.writeField(recordName, "name", calculationMethod.getName());
		xmlUtility.writeField(recordName, "code_routine_name", calculationMethod.getCodeRoutineName());
		xmlUtility.writeField(recordName, "description", calculationMethod.getDescription());

		ArrayList<Parameter> parameters = calculationMethod.getParameters();
		parameterContentHandler.writeXML(parameters);
		
		xmlUtility.writeRecordEndTag(recordName);
	}
	
	/**
	 * Write html.
	 *
	 * @param headerLevel the header level
	 * @param calculationMethods the calculation methods
	 * @param isFragmentWithinLargerReport the is fragment within larger report
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeHTML(
		final int headerLevel,
		final ArrayList<CalculationMethod> calculationMethods,
		final boolean isFragmentWithinLargerReport) 
		throws IOException {
		
		HTMLUtility htmlUtility = getHTMLUtility();

		if (isFragmentWithinLargerReport == false) {
			htmlUtility.beginDocument();
		}
		
		int recordHeaderLevel = getRecordHeaderLevel();
		String calculationMethodsSection
			= RIFServiceMessages.getMessage("calculationMethod.plural.label");
		htmlUtility.writeHeader(recordHeaderLevel, calculationMethodsSection);
		
		for (CalculationMethod calculationMethod: calculationMethods) {
			writeHTMLCalculationMethod(calculationMethod);
		}
		
		if (isFragmentWithinLargerReport == false) {
			htmlUtility.endDocument();
		}
	}
	
	/**
	 * Write html calculation method.
	 *
	 * @param calculationMethod the calculation method
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeHTMLCalculationMethod(
		final CalculationMethod calculationMethod) 
		throws IOException {
		
		HTMLUtility htmlUtility = getHTMLUtility();
		
		//make the header appear smaller than the "Calculation Methods" section title
		int recordHeaderLevel = getRecordHeaderLevel() + 1;
		
		htmlUtility.writeHeader(recordHeaderLevel, createCalculationMethodHeader(calculationMethod));
		htmlUtility.writeParagraph(calculationMethod.getDescription());
		
		ArrayList<Parameter> parameters = calculationMethod.getParameters();
		parameterContentHandler.writeHTML(parameters);
	}
	
	
	/**
	 * Creates the calculation method header.
	 *
	 * @param calculationMethod the calculation method
	 * @return the string
	 */
	private String createCalculationMethodHeader(
		final CalculationMethod calculationMethod) {
		
		StringBuilder buffer = new StringBuilder();
		String calculationMethodHeader
			= RIFServiceMessages.getMessage("calculationMethod.label");		
		buffer.append(calculationMethodHeader);
		buffer.append(":");
		buffer.append(calculationMethod.getName());
		return buffer.toString();
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
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
		parameterContentHandler.initialise(outputStream, commentInjector);
	}
	

	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isPluralRecordName(qualifiedName)) {
			calculationMethods.clear();
			activate();
		}
		else if (isSingularRecordName(qualifiedName)) {
			currentCalculationMethod = CalculationMethod.newInstance();
		}
		else if (isDelegatedHandlerAssigned() == true) {
			getCurrentDelegatedHandler().startElement(
				nameSpaceURI, 
				localName, 
				qualifiedName, 
				attributes);
		}
		else {
			//Check if a delegate can be assigned
			if (parameterContentHandler.isPluralRecordTypeApplicable(qualifiedName) == true) {
				assignDelegatedHandler(parameterContentHandler);
			}
			
			//delegate or inspect field tags for this element
			if (isDelegatedHandlerAssigned() == true) {
				getCurrentDelegatedHandler().startElement(
					nameSpaceURI, 
					localName, 
					qualifiedName, 
					attributes);
			}
			else if (isIgnoredStartTag(qualifiedName) == false) {
				//illegal field name
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
		
		if (isPluralRecordName(qualifiedName) == true) {
			deactivate();
		}
		else if (isSingularRecordName(qualifiedName) == true) {
			calculationMethods.add(currentCalculationMethod);
		}
		else if (isDelegatedHandlerAssigned() == true) {
			AbstractXMLContentHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();

			currentDelegatedHandler.endElement(nameSpaceURI, localName, qualifiedName);
			if (currentDelegatedHandler.isActive() == false) {			
				//current handler is finished. Inspect which handler it was and do appropriate action
				if (currentDelegatedHandler == parameterContentHandler) {
					currentCalculationMethod.setParameters(parameterContentHandler.getParameters());				
				}				
				unassignDelegatedHandler();
			}
		}
		else if ( equalsFieldName("name", qualifiedName) == true) {
			currentCalculationMethod.setName(getCurrentFieldValue());
		}
		else if (equalsFieldName("code_routine_name", qualifiedName) == true) {
			currentCalculationMethod.setCodeRoutineName(getCurrentFieldValue());						
		}
		else if (equalsFieldName("description", qualifiedName) == true) {
			currentCalculationMethod.setDescription(getCurrentFieldValue());
		}
		else if (isIgnoredEndTag(qualifiedName) == false) {
			//illegal field name
			assert false;
		}
	}
}
