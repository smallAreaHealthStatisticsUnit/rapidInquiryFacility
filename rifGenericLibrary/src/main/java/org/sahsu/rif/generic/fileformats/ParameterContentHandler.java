
package org.sahsu.rif.generic.fileformats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.presentation.HTMLUtility;
import org.sahsu.rif.generic.system.Messages;
import org.xml.sax.Attributes;

/**
 * Parses XML content for {@code <parameters>} elements of the Taxonomy Services configuration file,
 * as part of SAX parsing.
 */
public final class ParameterContentHandler extends AbstractXMLContentHandler {

	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	/** The parameters. */
	private List<Parameter> parameters;
	
	/** The current parameter. */
	private Parameter currentParameter;
    
    /**
     * Instantiates a new parameter content handler.
     */
	public ParameterContentHandler() {
		
		setSingularRecordName("parameter");
		setPluralRecordName("parameters");
    }

    /**
     * Gets the parameters.
     *
     * @return the parameters
     */
	public List<Parameter> getParameters() {
		
		return parameters;
	}
	
	/**
	 * Write xml.
	 *
	 * @param parameters the parameters
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(final List<Parameter> parameters)
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
	private void writeXMLParameter(final Parameter parameter)
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
	 */
	public void writeHTML(final List<Parameter> parameters) {

		HTMLUtility htmlUtility = getHTMLUtility();
		
		String parametersLabel = GENERIC_MESSAGES.getMessage("parameters.label");
		int recordHeaderLevel = getRecordHeaderLevel();
		htmlUtility.writeHeader(recordHeaderLevel, parametersLabel);
		if (parameters.isEmpty()) {
			String none = GENERIC_MESSAGES.getMessage("general.emptyList.none");
			htmlUtility.writeParagraph(none);
		} else {
			htmlUtility.beginInvisibleTable();
			
			//write out header row
			String nameFieldLabel = GENERIC_MESSAGES.getMessage("parameter.name.label");
			String valueFieldLabel = GENERIC_MESSAGES.getMessage("parameter.value.label");
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

	@Override
	public void startElement(final String nameSpaceURI, final String localName,
			final String qualifiedName, final Attributes attributes) {

		if (isPluralRecordName(qualifiedName)) {
			parameters = new ArrayList<>();
			activate();
		} else if (isSingularRecordName(qualifiedName)) {
			currentParameter = Parameter.newInstance();
		}
	}
	
	@Override
	public void endElement(final String nameSpaceURI, final String localName,
			final String qualifiedName) {

		if (isPluralRecordName(qualifiedName)) {
			deactivate();
		} else if (isSingularRecordName(qualifiedName)) {
			parameters.add(currentParameter);
		} else if (equalsFieldName("name", qualifiedName)) {
			currentParameter.setName(getCurrentFieldValue().trim());
		} else if (equalsFieldName("value", qualifiedName)) {
			currentParameter.setValue(getCurrentFieldValue().trim());						
		}
	}
}
