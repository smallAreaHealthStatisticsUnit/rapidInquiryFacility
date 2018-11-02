
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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

final class CalculationMethodContentHandler extends AbstractXMLContentHandler {

	/** The calculation methods. */
	private List<CalculationMethod> calculationMethods;
	
	/** The current calculation method. */
	private CalculationMethod currentCalculationMethod;
    
	/** The parameter content handler. */
	private ParameterContentHandler parameterContentHandler;
	
	/**
	 * Instantiates a new calculation method content handler.
	 */
    CalculationMethodContentHandler() {
		
		setSingularRecordName("calculation_method");
		setPluralRecordName("calculation_methods");
		
		parameterContentHandler = new ParameterContentHandler();
		calculationMethods = new ArrayList<>();
    }

	/**
	 * Gets the calculation methods.
	 *
	 * @return the calculation methods
	 */
	public List<CalculationMethod> getCalculationMethods() {
		
		return calculationMethods;
	}

	/**
	 * Write xml.
	 *
	 * @param calculationMethods the calculation methods
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(final List<CalculationMethod> calculationMethods) throws IOException {

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

		List<Parameter> parameters = calculationMethod.getParameters();
		parameterContentHandler.writeXML(parameters);
		
		xmlUtility.writeRecordEndTag(recordName);
	}

	/**
	 * Write html calculation method.
	 *
	 * @param calculationMethod the calculation method
	 */
	private void writeHTMLCalculationMethod(
		final CalculationMethod calculationMethod) {
		
		HTMLUtility htmlUtility = getHTMLUtility();
		
		//make the header appear smaller than the "Calculation Methods" section title
		int recordHeaderLevel = getRecordHeaderLevel() + 1;
		
		htmlUtility.writeHeader(recordHeaderLevel, createCalculationMethodHeader(calculationMethod));
		htmlUtility.writeParagraph(calculationMethod.getDescription());
		
		List<Parameter> parameters = calculationMethod.getParameters();
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
		else if (isDelegatedHandlerAssigned()) {
			getCurrentDelegatedHandler().startElement(
				nameSpaceURI, 
				localName, 
				qualifiedName, 
				attributes);
		}
		else {
			//Check if a delegate can be assigned
			if (parameterContentHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(parameterContentHandler);
			}
			
			//delegate or inspect field tags for this element
			if (isDelegatedHandlerAssigned()) {
				getCurrentDelegatedHandler().startElement(
					nameSpaceURI, 
					localName, 
					qualifiedName, 
					attributes);
			}
			else if (!isIgnoredStartTag(qualifiedName)) {
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
		
		if (isPluralRecordName(qualifiedName)) {
			deactivate();
		}
		else if (isSingularRecordName(qualifiedName)) {
			calculationMethods.add(currentCalculationMethod);
		}
		else if (isDelegatedHandlerAssigned()) {
			AbstractXMLContentHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();

			currentDelegatedHandler.endElement(nameSpaceURI, localName, qualifiedName);
			if (!currentDelegatedHandler.isActive()) {
				//current handler is finished. Inspect which handler it was and do appropriate action
				if (currentDelegatedHandler == parameterContentHandler) {
					currentCalculationMethod.setParameters(parameterContentHandler.getParameters());				
				}				
				unassignDelegatedHandler();
			}
		}
		else if (equalsFieldName("name", qualifiedName)) {
			currentCalculationMethod.setName(getCurrentFieldValue());
		}
		else if (equalsFieldName("code_routine_name", qualifiedName)) {
			currentCalculationMethod.setCodeRoutineName(getCurrentFieldValue());						
		}
		else if (equalsFieldName("description", qualifiedName)) {
			currentCalculationMethod.setDescription(getCurrentFieldValue());
		}
		else if (!isIgnoredEndTag(qualifiedName)) {
			//illegal field name
			assert false;
		}
	}
}
