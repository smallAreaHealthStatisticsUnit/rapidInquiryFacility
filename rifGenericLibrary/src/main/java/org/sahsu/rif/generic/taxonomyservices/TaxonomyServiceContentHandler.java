
package org.sahsu.rif.generic.taxonomyservices;

import org.sahsu.rif.generic.concepts.Parameter;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.ParameterContentHandler;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


/**
 *
 * Reads an XML fragment containing taxonomy service descriptions.  It is invoked
 * by {@link TaxonomyServiceConfigurationXMLReader}
 * and generates a collection of 
 * {@link TaxonomyServiceConfiguration} objects
 * that are used to instantiate services.
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


final public class TaxonomyServiceContentHandler
		extends AbstractXMLContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================

	private ArrayList<TaxonomyServiceConfiguration> taxonomyServiceConfigurations;
	private TaxonomyServiceConfiguration currentTaxonomyServiceConfiguration;
	private ParameterContentHandler parameterContentHandler;
		
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new RIF job submission content handler.
     */
	public TaxonomyServiceContentHandler() {
		
    	setPluralRecordName("taxonomy_services");
    	setSingularRecordName("taxonomy_service");
    	
    	taxonomyServiceConfigurations = new ArrayList<TaxonomyServiceConfiguration>();
    	
    	parameterContentHandler = new ParameterContentHandler();
    	
    }

	
// ==========================================
// Section Accessors and Mutators
// ==========================================
	
	/**
	 * Gets the RIF job submission.
	 *
	 * @return the RIF job submission
	 */
	public ArrayList<TaxonomyServiceConfiguration> getTaxonomyServiceConfigurations() {
		return taxonomyServiceConfigurations;
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
		else if (isDelegatedHandlerAssigned() == true) {
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
			if (isDelegatedHandlerAssigned() == true) {
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
					ArrayList<Parameter> parameters
						= parameterContentHandler.getParameters();
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
