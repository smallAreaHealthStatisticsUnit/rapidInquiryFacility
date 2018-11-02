
package org.sahsu.taxonomyservices;

import org.sahsu.rif.generic.concepts.Parameter;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.ParameterContentHandler;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Reads an XML fragment containing taxonomy service descriptions.  It is invoked
 * by {@link TaxonomyServiceConfigurationXMLReader}
 * and generates a collection of 
 * {@link TaxonomyServiceConfiguration} objects
 * that are used to instantiate services.
 */

final public class TaxonomyServiceContentHandler
		extends AbstractXMLContentHandler {

	private ArrayList<TaxonomyServiceConfiguration> taxonomyServiceConfigurations;
	private TaxonomyServiceConfiguration currentTaxonomyServiceConfiguration;
	private ParameterContentHandler parameterContentHandler;

    /**
     * Instantiates a new RIF job submission content handler.
     */
    TaxonomyServiceContentHandler() {
		
    	setPluralRecordName("taxonomy_services");
    	setSingularRecordName("taxonomy_service");
    	
    	taxonomyServiceConfigurations = new ArrayList<>();
    	
    	parameterContentHandler = new ParameterContentHandler();
    }

	/**
	 * Gets the RIF job submission.
	 *
	 * @return the RIF job submission
	 */
	ArrayList<TaxonomyServiceConfiguration> getTaxonomyServiceConfigurations() {
		return taxonomyServiceConfigurations;
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
	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
		parameterContentHandler.initialise(outputStream);
	}
	
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
			currentTaxonomyServiceConfiguration = TaxonomyServiceConfiguration.newInstance();
		}
		else if (isDelegatedHandlerAssigned()) {
			AbstractXMLContentHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.startElement(
				nameSpaceURI, 
				localName, 
				qualifiedName, 
				attributes);
		}
		else {				
			//determine if a delegate handler can be assigned to do future processing
			if (parameterContentHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(parameterContentHandler);
			}
		
			//either delegate or scan for field tags releated to this handler
			if (isDelegatedHandlerAssigned()) {
				//one of the above cases results in an active delegated handler.  Now delegate
				AbstractXMLContentHandler currentDelegatedHandler
					= getCurrentDelegatedHandler();
				currentDelegatedHandler.startElement(
					nameSpaceURI, 
					localName, 
					qualifiedName, 
					attributes);
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
		} else if (isSingularRecordName(qualifiedName)) {
			taxonomyServiceConfigurations.add(currentTaxonomyServiceConfiguration);
		} else if (isDelegatedHandlerAssigned()) {
			AbstractXMLContentHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
			
			if (!currentDelegatedHandler.isActive()) {
				//current handler has finished.  Therefore, cast delegator and obtain data
				if (currentDelegatedHandler == parameterContentHandler) {
					List<Parameter> parameters = parameterContentHandler.getParameters();
					currentTaxonomyServiceConfiguration.setParameters(parameters);
				} else {
					assert false;
				}
				
				unassignDelegatedHandler();
			}
		} else if (equalsFieldName(qualifiedName, "identifier")) {
			currentTaxonomyServiceConfiguration.setServiceIdentifier(getCurrentFieldValue());
		} else if (equalsFieldName(qualifiedName, "name")) {
			currentTaxonomyServiceConfiguration.setName(getCurrentFieldValue());
		} else if (equalsFieldName(qualifiedName, "description")) {
			currentTaxonomyServiceConfiguration.setDescription(getCurrentFieldValue());
		} else if (equalsFieldName(qualifiedName, "version")) {
			currentTaxonomyServiceConfiguration.setVersion(getCurrentFieldValue());
		} else if (equalsFieldName(qualifiedName, "ontology_service_class_name")) {
			currentTaxonomyServiceConfiguration.setOntologyServiceClassName(getCurrentFieldValue());
		} else {
			assert false;
		}
	}
}
