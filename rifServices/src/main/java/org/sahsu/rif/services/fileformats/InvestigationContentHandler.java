
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
import org.sahsu.rif.services.concepts.AgeBand;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.concepts.HealthTheme;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.concepts.Sex;
import org.sahsu.rif.services.concepts.YearInterval;
import org.sahsu.rif.services.concepts.YearRange;
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


final class InvestigationContentHandler 
	extends AbstractXMLContentHandler {


// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	/** The current investigations. */
	private ArrayList<Investigation> currentInvestigations;
	
	/** The current investigation. */
	private Investigation currentInvestigation;	
	
	/** The age group content handler. */
	private AgeBandContentHandler ageBandContentHandler;
	
	private HealthThemeContentHandler healthThemeContentHandler;
	
	/** The nd pair content handler. */
	private NumeratorDenominatorPairContentHandler ndPairContentHandler;
	
	/** The year range content handler. */
	private YearRangeContentHandler yearRangeContentHandler;
	
	/** The year interval content handler. */
	private YearIntervalContentHandler yearIntervalContentHandler;
	
	/** The covariate content handler. */
	private CovariateContentHandler covariateContentHandler;
	
	/** The health code content handler. */
	private HealthCodeContentHandler healthCodeContentHandler;
		
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new investigation content handler.
     */
	public InvestigationContentHandler() {
		
 		setSingularRecordName("investigation");
		setPluralRecordName("investigations");

		currentInvestigations = new ArrayList<Investigation>();
		
		ageBandContentHandler
			= new AgeBandContentHandler();
		
		healthThemeContentHandler
			= new HealthThemeContentHandler();
		ndPairContentHandler
			= new NumeratorDenominatorPairContentHandler();
		yearIntervalContentHandler
			= new YearIntervalContentHandler();
		yearRangeContentHandler
			= new YearRangeContentHandler();		
		covariateContentHandler
			= new CovariateContentHandler();
		healthCodeContentHandler
			= new HealthCodeContentHandler();
    }
	
	
// ==========================================
// Section Accessors and Mutators
// ==========================================

    /**
     * Gets the investigations.
     *
     * @return the investigations
     */
	public ArrayList<Investigation> getInvestigations() {
		
    	return currentInvestigations;
    }
    
	/**
	 * Gets the investigation.
	 *
	 * @return the investigation
	 */
	public Investigation getInvestigation() {
		
		return currentInvestigation;
	}
	
	/**
	 * Write xml.
	 *
	 * @param investigations the investigations
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final ArrayList<Investigation> investigations) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();
	
		xmlUtility.writeRecordStartTag(getPluralRecordName());
		for (Investigation investigation : investigations) {
			writeXML(investigation);
		}

		xmlUtility.writeRecordEndTag(getPluralRecordName());	
	}
	
	/**
	 * Write xml.
	 *
	 * @param investigation the investigation
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final Investigation investigation) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();
		
		String recordName = getSingularRecordName();
		
		xmlUtility.writeRecordStartTag(recordName, "id", investigation.getIdentifier());		
		xmlUtility.writeField(recordName, "title", investigation.getTitle());
		
		HealthTheme healthTheme = investigation.getHealthTheme();
		healthThemeContentHandler.writeXML(healthTheme);
				
		NumeratorDenominatorPair ndPair = investigation.getNdPair();
		ndPairContentHandler.writeXML(ndPair);

		ArrayList<AgeBand> ageBands = investigation.getAgeBands();
		ageBandContentHandler.writeXML(ageBands);		
		
		ArrayList<HealthCode> healthCodes
			= investigation.getHealthCodes();
		healthCodeContentHandler.writeXML(healthCodes);
		
		YearRange yearRange = investigation.getYearRange();
		yearRangeContentHandler.writeXML(yearRange);
		
		ArrayList<YearInterval> yearIntervals
			= investigation.getYearIntervals();
		yearIntervalContentHandler.writeXML(yearIntervals);
		

		
		xmlUtility.writeField(recordName, "years_per_interval", investigation.getInterval());
		
		Sex sex = investigation.getSex();
		xmlUtility.writeField(recordName, "sex", sex.getName());
		
		ArrayList<AbstractCovariate> covariates = investigation.getCovariates();
		covariateContentHandler.writeXML(covariates);
		
		xmlUtility.writeRecordEndTag(recordName);		
	}

	/**
	 * Write html.
	 *
	 * @param headerLevel the header level
	 * @param investigations the investigations
	 * @param isFragmentWithinLargerReport the is fragment within larger report
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeHTML(
		final int headerLevel,
		final ArrayList<Investigation> investigations, 
		final boolean isFragmentWithinLargerReport) 
		throws IOException {

		HTMLUtility htmlUtility = getHTMLUtility();		
		if (isFragmentWithinLargerReport == false) {
			htmlUtility.beginDocument();
		}
		
		String investigationsTitle
			= RIFServiceMessages.getMessage("investigation.plural.label");
		htmlUtility.writeHeader(headerLevel, investigationsTitle);
		
		for (Investigation investigation : investigations) {
			writeHTML(headerLevel + 1, investigation);
		}		
		
		if (isFragmentWithinLargerReport == false) {
			htmlUtility.endDocument();
		}
	}
	
	/**
	 * Write html.
	 *
	 * @param headerLevel the header level
	 * @param investigation the investigation
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeHTML(
		final int headerLevel, 
		final Investigation investigation) 
		throws IOException {

		HTMLUtility htmlUtility = getHTMLUtility();
		
		htmlUtility.writeHeader(headerLevel, investigation.getDisplayName());
		htmlUtility.beginTable();
		
		htmlUtility.beginRow();
		String identifierFieldLabelText
			= RIFServiceMessages.getMessage("abstractRIFConcept.id.label");
		htmlUtility.writeBoldColumnValue(identifierFieldLabelText);
		htmlUtility.writeColumnValue(investigation.getIdentifier());
		htmlUtility.endRow();

		htmlUtility.beginRow();
		String titleFieldLabelText
			= RIFServiceMessages.getMessage("investigation.title.label");
		htmlUtility.writeBoldColumnValue(titleFieldLabelText);
		htmlUtility.writeColumnValue(investigation.getTitle());
		htmlUtility.endRow();

		htmlUtility.beginRow();
		String healthThemeFieldLabelText
			= RIFServiceMessages.getMessage("healthTheme.label");
		htmlUtility.writeBoldColumnValue(healthThemeFieldLabelText);
		htmlUtility.writeColumnValue(investigation.getHealthTheme().getName());
		htmlUtility.endRow();
				
		htmlUtility.beginRow();
		String sexFieldLabelText
			= RIFServiceMessages.getMessage("sex.label");
		htmlUtility.writeBoldColumnValue(sexFieldLabelText);
		Sex sex = investigation.getSex();
		htmlUtility.writeColumnValue(sex.getName());
		htmlUtility.endRow();
				
		htmlUtility.beginRow();
		String healthCodesFieldLabelText
			= RIFServiceMessages.getMessage("healthCode.plural.label");
		htmlUtility.writeBoldColumnValue(healthCodesFieldLabelText);
		htmlUtility.beginColumnValue();
		htmlUtility.beginBulletedList();
		ArrayList<HealthCode> healthCodes = investigation.getHealthCodes();
		for (HealthCode healthCode : healthCodes) {
			htmlUtility.writeBulletedItem(healthCode.getDisplayName());
		}
		htmlUtility.endBulletedList();
		htmlUtility.endColumnValue();
		htmlUtility.endRow();

		htmlUtility.beginRow();
		String yearRangeFieldLabelText
			= RIFServiceMessages.getMessage("yearRange.label");
		htmlUtility.writeBoldColumnValue(yearRangeFieldLabelText);
		YearRange yearRange = investigation.getYearRange();
		htmlUtility.writeColumnValue(yearRange.getDisplayName());
		htmlUtility.endRow();

		htmlUtility.beginRow();
		String ageBandFieldLabelText
			= RIFServiceMessages.getMessage("ageBand.plural.label");
		htmlUtility.writeBoldColumnValue(ageBandFieldLabelText);

		htmlUtility.beginColumnValue();
		htmlUtility.beginBulletedList();
		ArrayList<AgeBand> ageBands
			= investigation.getAgeBands();
		for (AgeBand ageBand : ageBands) {
			htmlUtility.writeBulletedItem(ageBand.getDisplayName());			
		}		
		htmlUtility.endBulletedList();
		htmlUtility.endColumnValue();
		htmlUtility.endRow();
				
		htmlUtility.beginRow();
		String yearsPerIntervalLabelText
			= RIFServiceMessages.getMessage("investigation.interval.label");
		htmlUtility.writeBoldColumnValue(yearsPerIntervalLabelText);
		htmlUtility.writeColumnValue(investigation.getInterval());
		htmlUtility.endRow();
				
		htmlUtility.beginRow();
		String yearIntervalsFieldLabelText
			= RIFServiceMessages.getMessage("yearInterval.plural.label");
		htmlUtility.writeBoldColumnValue(yearIntervalsFieldLabelText);
		htmlUtility.beginColumnValue();
		ArrayList<YearInterval> yearIntervals
			= investigation.getYearIntervals();
		yearIntervalContentHandler.writeHTML(yearIntervals);
		htmlUtility.endColumnValue();		
		htmlUtility.endRow();
		
		String covariatesFieldLabelText
			= RIFServiceMessages.getMessage("covariate.plural.label");
		htmlUtility.writeBoldColumnValue(covariatesFieldLabelText);
		htmlUtility.beginBulletedList();
		ArrayList<AbstractCovariate> covariates = investigation.getCovariates();
		for (AbstractCovariate covariate : covariates) {
			htmlUtility.writeBulletedItem(covariate.getDisplayName());
		}
		htmlUtility.endBulletedList();
		htmlUtility.endColumnValue();
		htmlUtility.endRow();
		
		NumeratorDenominatorPair ndPair = investigation.getNdPair();
		
		htmlUtility.beginRow();
		String numeratorFieldName
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.numerator.label");
		htmlUtility.writeBoldColumnValue(numeratorFieldName);
		htmlUtility.writeColumnValue(ndPair.getNumeratorTableDescription());
		htmlUtility.endRow();

		htmlUtility.beginRow();
		String denominatorFieldName
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.denominator.label");
		htmlUtility.writeBoldColumnValue(denominatorFieldName);
		htmlUtility.writeColumnValue(ndPair.getDenominatorTableDescription());
		htmlUtility.endRow();
				
		htmlUtility.endTable();		
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
		healthThemeContentHandler.initialise(outputStream, commentInjector);
		ndPairContentHandler.initialise(outputStream, commentInjector);
		ageBandContentHandler.initialise(outputStream, commentInjector);
		yearIntervalContentHandler.initialise(outputStream, commentInjector);
		yearRangeContentHandler.initialise(outputStream, commentInjector);
		covariateContentHandler.initialise(outputStream, commentInjector);		
		healthCodeContentHandler.initialise(outputStream, commentInjector);				
	}
	

	@Override
	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
				
		healthThemeContentHandler.initialise(outputStream);
		ndPairContentHandler.initialise(outputStream);
		ageBandContentHandler.initialise(outputStream);
		yearIntervalContentHandler.initialise(outputStream);
		yearRangeContentHandler.initialise(outputStream);
		covariateContentHandler.initialise(outputStream);		
		healthCodeContentHandler.initialise(outputStream);				
	}	
	

	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {
				
		if (isPluralRecordName(qualifiedName)) {
			currentInvestigations.clear();
			activate();
		}
		else if (isSingularRecordName(qualifiedName)) {
			currentInvestigation = Investigation.newInstance();
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
			//determine if a delegated handler can be assigned
			if (healthThemeContentHandler.isSingularRecordTypeApplicable(qualifiedName) == true) {
				assignDelegatedHandler(healthThemeContentHandler);				
			}
			else if (ndPairContentHandler.isSingularRecordTypeApplicable(qualifiedName) == true) {
				assignDelegatedHandler(ndPairContentHandler);
			}	
			else if (ageBandContentHandler.isSingularRecordTypeApplicable(qualifiedName) == true) {
				assignDelegatedHandler(ageBandContentHandler);
			}
			else if (yearIntervalContentHandler.isPluralRecordTypeApplicable(qualifiedName) == true) {
				assignDelegatedHandler(yearIntervalContentHandler);
			}
			else if (covariateContentHandler.isPluralRecordTypeApplicable(qualifiedName) == true) {
				assignDelegatedHandler(covariateContentHandler);
			}
			else if (healthCodeContentHandler.isPluralRecordTypeApplicable(qualifiedName) == true) {
				assignDelegatedHandler(healthCodeContentHandler);
			}
			else if (yearRangeContentHandler.isSingularRecordTypeApplicable(qualifiedName) == true) {
				assignDelegatedHandler(yearRangeContentHandler);
			}
		
			//either delegate or scan for field name tags relevant to this handler
			if (isDelegatedHandlerAssigned() == true) {
				//delegate handler was found so delegate
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
		}
		else if (isSingularRecordName(qualifiedName)) {
			currentInvestigations.add(currentInvestigation);
		}
		else if (isDelegatedHandlerAssigned() == true) {
			AbstractXMLContentHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
			if (currentDelegatedHandler.isActive() == false) {
				//current handler just finished
				
				if (currentDelegatedHandler == healthThemeContentHandler) {
					HealthTheme healthTheme
						= healthThemeContentHandler.getHealthTheme();
					currentInvestigation.setHealthTheme(healthTheme);
				}
				else if (currentDelegatedHandler == ndPairContentHandler) {
					NumeratorDenominatorPair ndPair
						= ndPairContentHandler.getNumeratorDenominatorPair();
					currentInvestigation.setNdPair(ndPair);				
				}
				else if (currentDelegatedHandler == ageBandContentHandler) {
					AgeBand ageBand = ageBandContentHandler.getAgeBand();
					currentInvestigation.addAgeBand(ageBand);
				}
				else if (currentDelegatedHandler == yearIntervalContentHandler) {
					ArrayList<YearInterval> yearIntervals
						= yearIntervalContentHandler.getYearIntervals();
					currentInvestigation.setYearIntervals(yearIntervals);
				}
				else if (currentDelegatedHandler == yearRangeContentHandler) {
					YearRange yearRange
						= yearRangeContentHandler.getYearRange();
					currentInvestigation.setYearRange(yearRange);
				}
				else if (currentDelegatedHandler == covariateContentHandler) {
					ArrayList<AbstractCovariate> covariates
						= covariateContentHandler.getCovariates();
					currentInvestigation.setCovariates(covariates);				
				}
				else if (currentDelegatedHandler == healthCodeContentHandler) {
					ArrayList<HealthCode> healthCodes
						= healthCodeContentHandler.getHealthCodes();
					currentInvestigation.setHealthCodes(healthCodes);			
				}
				else {
					assert false;
				}
				
				unassignDelegatedHandler();
			}
		}
		else if (equalsFieldName("sex", qualifiedName) == true) {
			Sex sex = Sex.getSexFromName(getCurrentFieldValue());
			currentInvestigation.setSex(sex);
		}
		//examine other fields
		else if (equalsFieldName(qualifiedName, "id")) {
			currentInvestigation.setIdentifier(getCurrentFieldValue());
		}
		else if (equalsFieldName(qualifiedName, "title")) {
			currentInvestigation.setTitle(getCurrentFieldValue());
		}
		else if (equalsFieldName(qualifiedName, "years_per_interval")) {
			currentInvestigation.setInterval(getCurrentFieldValue());
		}
		else {
			assert false;
		}
	}

}
