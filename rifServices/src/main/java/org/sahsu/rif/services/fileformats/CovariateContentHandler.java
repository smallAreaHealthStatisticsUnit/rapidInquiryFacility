
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.generic.presentation.HTMLUtility;
import org.sahsu.rif.services.concepts.AbstractCovariate;
import org.sahsu.rif.services.concepts.AdjustableCovariate;
import org.sahsu.rif.services.concepts.CovariateType;
import org.sahsu.rif.services.concepts.ExposureCovariate;
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


public final class CovariateContentHandler 
	extends AbstractXMLContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The covariates. */
	private ArrayList<AbstractCovariate> covariates;
	
	/** The current covariate. */
	private AbstractCovariate currentCovariate;
	
	/** The Constant adjustableCovariateName. */
	private static final String adjustableCovariateName = "adjustable_covariate";
	
	/** The Constant exposureCovariateName. */
	private static final String exposureCovariateName = "exposure_covariate";

// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new covariate content handler.
     */
	public CovariateContentHandler() {
		
    	setPluralRecordName("covariates");
		covariates = new ArrayList<AbstractCovariate>();
		
		ignoreXMLStartTag("name");
		ignoreXMLStartTag("minimum_value");
		ignoreXMLStartTag("maximum_value");		
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
	
	/**
	 * Gets the covariates.
	 *
	 * @return the covariates
	 */
	public ArrayList<AbstractCovariate> getCovariates() {
		
		return covariates;
	}
	
	/**
	 * Write xml.
	 *
	 * @param covariates the covariates
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final ArrayList<AbstractCovariate> covariates) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordListStartTag(getPluralRecordName());
		for (AbstractCovariate covariate : covariates) {
			writeXML(covariate);
		}		
		xmlUtility.writeRecordListEndTag(getPluralRecordName());
	}
	
	/**
	 * Write xml.
	 *
	 * @param covariate the covariate
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final AbstractCovariate covariate) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();

		if (covariate instanceof AdjustableCovariate) {
			xmlUtility.writeRecordStartTag(adjustableCovariateName);		
			xmlUtility.writeField(adjustableCovariateName, "name", covariate.getName());
			xmlUtility.writeField(adjustableCovariateName, "minimum_value", covariate.getMinimumValue());
			xmlUtility.writeField(adjustableCovariateName, "maximum_value", covariate.getMaximumValue());

			CovariateType covariateType = covariate.getCovariateType();
			xmlUtility.writeField(adjustableCovariateName, "covariate_type", covariateType.getName());
			xmlUtility.writeRecordEndTag(adjustableCovariateName);		
		}
		else if (covariate instanceof ExposureCovariate) {
			xmlUtility.writeRecordStartTag(exposureCovariateName);		
			xmlUtility.writeField(exposureCovariateName, "name", covariate.getName());
			xmlUtility.writeField(exposureCovariateName, "minimum_value", covariate.getMinimumValue());
			xmlUtility.writeField(exposureCovariateName, "maximum_value", covariate.getMaximumValue());

			CovariateType covariateType = covariate.getCovariateType();
			xmlUtility.writeField(exposureCovariateName, "covariate_type", covariateType.getName());						
			xmlUtility.writeRecordEndTag(exposureCovariateName);
		}
		else {
			assert false;
		}
	}
		
	/**
	 * Write html.
	 *
	 * @param covariates the covariates
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeHTML(
		final ArrayList<AbstractCovariate> covariates) 
		throws IOException {
	
		HTMLUtility htmlUtility = getHTMLUtility();
		
		htmlUtility.beginTable();
				
		htmlUtility.beginRow();
		String nameFieldName
			= RIFServiceMessages.getMessage("covariate.name.label");
		htmlUtility.writeBoldColumnValue(nameFieldName);
		String covariateTypeFieldName
			= RIFServiceMessages.getMessage("covariate.covariateType.label");
		htmlUtility.writeBoldColumnValue(covariateTypeFieldName);		
		String minimumValueFieldName
			= RIFServiceMessages.getMessage("covariate.minimum.label");
		htmlUtility.writeBoldColumnValue(minimumValueFieldName);
		String maximumValueFieldName
			= RIFServiceMessages.getMessage("covariate.maximum.label");
		htmlUtility.writeBoldColumnValue(maximumValueFieldName);
		htmlUtility.endRow();
		
		for (AbstractCovariate covariate : covariates) {
			htmlUtility.beginRow();
			htmlUtility.writeColumnValue(covariate.getName());
			CovariateType covariateType = covariate.getCovariateType();
			htmlUtility.writeColumnValue(covariateType.getName());
			htmlUtility.writeColumnValue(covariate.getMinimumValue());
			htmlUtility.writeColumnValue(covariate.getMaximumValue());
			htmlUtility.endRow();
		}
				
		htmlUtility.endTable();
		
	}
	
	/**
	 * Write html.
	 *
	 * @param covariate the covariate
	 */
	public void writeHTML(
		final AbstractCovariate covariate) {		

		HTMLUtility htmlUtility = getHTMLUtility();

		htmlUtility.beginDocument();
		
		htmlUtility.beginInvisibleTable();
		
		htmlUtility.beginTable();
		
		htmlUtility.beginRow();
		String nameFieldName
			= RIFServiceMessages.getMessage("covariate.name.label");
		htmlUtility.writeBoldColumnValue(nameFieldName);
		htmlUtility.writeColumnValue(covariate.getName());
		htmlUtility.endRow();
		
		htmlUtility.beginRow();
		String covariateTypeFieldName
			= RIFServiceMessages.getMessage("covariate.covariateType.label");
		htmlUtility.writeBoldColumnValue(covariateTypeFieldName);
		CovariateType covariateType = covariate.getCovariateType();
		htmlUtility.writeColumnValue(covariateType.getName());
		htmlUtility.endRow();
		
		htmlUtility.beginRow();
		String minimumValueFieldName
			= RIFServiceMessages.getMessage("covariate.minimum.label");
		htmlUtility.writeBoldColumnValue(minimumValueFieldName);
		htmlUtility.writeColumnValue(covariate.getMinimumValue());
		htmlUtility.endRow();

		htmlUtility.beginRow();
		String maximumValueFieldName
			= RIFServiceMessages.getMessage("covariate.maximum.label");
		htmlUtility.writeBoldColumnValue(maximumValueFieldName);
		htmlUtility.writeColumnValue(covariate.getMaximumValue());
		htmlUtility.endRow();

		htmlUtility.endTable();		
		htmlUtility.endDocument();
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
	}
	

	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isPluralRecordName(qualifiedName) == true) {
			covariates.clear();
			activate();
		}
		else if (equalsFieldName(qualifiedName, "adjustable_covariate") == true) {
			currentCovariate = AdjustableCovariate.newInstance();
		}
		else if (equalsFieldName(qualifiedName, "exposure_covariate") == true) {
			currentCovariate = ExposureCovariate.newInstance();
		}
		else if (isIgnoredStartTag(qualifiedName) == false) {
			assert false;
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
		else if (qualifiedName.equals("adjustable_covariate") == true) {
			covariates.add(currentCovariate);
		}
		else if (qualifiedName.equals("exposure_covariate") == true) {
			covariates.add(currentCovariate);
		}
		else if (equalsFieldName(qualifiedName, "name") == true) {
			currentCovariate.setName(getCurrentFieldValue());
		}
		else if (equalsFieldName(qualifiedName, "minimum_value") == true) {
			currentCovariate.setMinimumValue(getCurrentFieldValue());
		}
		else if (equalsFieldName(qualifiedName, "maximum_value") == true) {
			currentCovariate.setMaximumValue(getCurrentFieldValue());
		}		
		else if (equalsFieldName(qualifiedName, "covariate_type") == true) {
			CovariateType covariateType
				= CovariateType.getTypeFromName(getCurrentFieldValue());
			currentCovariate.setCovariateType(covariateType);
		}
		else if (isIgnoredEndTag(qualifiedName) == false) {
			assert false;
		}
	}
}
