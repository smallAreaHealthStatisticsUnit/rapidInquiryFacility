
package rifServices.fileFormats;

import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.DiseaseMappingStudyArea;
import rifServices.businessConceptLayer.ComparisonArea;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.Project;
import rifServices.businessConceptLayer.Geography;
import rifGenericLibrary.fileFormats.AbstractXMLContentHandler;
import rifGenericLibrary.fileFormats.XMLCommentInjector;
import rifGenericLibrary.fileFormats.XMLUtility;
import rifGenericLibrary.presentationLayer.HTMLUtility;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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


final class DiseaseMappingStudyContentHandler 
	extends AbstractXMLContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================

	private GeographyContentHandler geographyContentHandler;

	
	/** The current disease mapping study. */
	private DiseaseMappingStudy currentDiseaseMappingStudy;
	
	
	/** The disease mapping study area content handler. */
	private DiseaseMappingStudyAreaContentHandler diseaseMappingStudyAreaContentHandler;
	
	/** The comparison area content handler. */
	private ComparisonAreaContentHandler comparisonAreaContentHandler;
	
	/** The investigation content handler. */
	private InvestigationContentHandler investigationContentHandler;
	
	private RIFOutputOptionContentHandler rifOutputOptionContentHandler;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study content handler.
     */
	public DiseaseMappingStudyContentHandler() {
		
		setSingularRecordName("disease_mapping_study");
		geographyContentHandler = new GeographyContentHandler();
		diseaseMappingStudyAreaContentHandler = new DiseaseMappingStudyAreaContentHandler();
		comparisonAreaContentHandler = new ComparisonAreaContentHandler();
		investigationContentHandler = new InvestigationContentHandler();		
		rifOutputOptionContentHandler = new RIFOutputOptionContentHandler();
	}


	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
		
		geographyContentHandler.initialise(outputStream, commentInjector);
		diseaseMappingStudyAreaContentHandler.initialise(outputStream, commentInjector);
		comparisonAreaContentHandler.initialise(outputStream, commentInjector);
		investigationContentHandler.initialise(outputStream, commentInjector);	
		rifOutputOptionContentHandler.initialise(outputStream, commentInjector);
	}

	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
		geographyContentHandler.initialise(outputStream);
		diseaseMappingStudyAreaContentHandler.initialise(outputStream);
		comparisonAreaContentHandler.initialise(outputStream);
		investigationContentHandler.initialise(outputStream);	
		rifOutputOptionContentHandler.initialise(outputStream);		
	}
	
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
    	
	/**
	 * Gets the disease mapping study.
	 *
	 * @return the disease mapping study
	 */
	public DiseaseMappingStudy getDiseaseMappingStudy() {
	
		ComparisonArea comparisonArea
			= comparisonAreaContentHandler.getComparisonArea();
		DiseaseMappingStudyArea diseaseMappingStudyArea
			= diseaseMappingStudyAreaContentHandler.getDiseaseMappingStudyArea();
	
		ArrayList<Investigation> investigations
			= investigationContentHandler.getInvestigations();
		currentDiseaseMappingStudy.setComparisonArea(comparisonArea);
		currentDiseaseMappingStudy.setDiseaseMappingStudyArea(diseaseMappingStudyArea);
		currentDiseaseMappingStudy.setInvestigations(investigations);
		
		return currentDiseaseMappingStudy;
	}
	
	/**
	 * Write xml.
	 *
	 * @param diseaseMappingStudy the disease mapping study
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final DiseaseMappingStudy diseaseMappingStudy) 
		throws IOException {

		String recordName = getSingularRecordName();
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(recordName);
		xmlUtility.writeField(recordName, "name", diseaseMappingStudy.getName());
		xmlUtility.writeField(recordName, "description", diseaseMappingStudy.getDescription());
		
		Geography geography = diseaseMappingStudy.getGeography();
		geographyContentHandler.writeXML(geography);
				
		DiseaseMappingStudyArea studyArea = diseaseMappingStudy.getDiseaseMappingStudyArea();
		diseaseMappingStudyAreaContentHandler.writeXML(studyArea);
		
		ComparisonArea comparisonArea = diseaseMappingStudy.getComparisonArea();
		comparisonAreaContentHandler.writeXML(comparisonArea);
		
		ArrayList<Investigation> investigations
			= diseaseMappingStudy.getInvestigations();
		investigationContentHandler.writeXML(investigations);
		xmlUtility.writeRecordEndTag(recordName);
	}
	
	/**
	 * Write html.
	 *
	 * @param headerLevel the header level
	 * @param diseaseMappingStudy the disease mapping study
	 * @param project the project
	 * @param isFragmentWithinLargerReport the is fragment within larger report
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeHTML(
		final int headerLevel,
		final DiseaseMappingStudy diseaseMappingStudy,
		final Project project,
		final boolean isFragmentWithinLargerReport) 
		throws IOException {

		HTMLUtility htmlUtility = getHTMLUtility();		
		if (isFragmentWithinLargerReport == false) {
			htmlUtility.beginDocument();			
		}
		
		htmlUtility.writeHeader(1, diseaseMappingStudy.getDisplayName());
		htmlUtility.writeParagraph(diseaseMappingStudy.getDescription());
		
		Geography geography = diseaseMappingStudy.getGeography();
		geographyContentHandler.writeHTML(
			2,
			geography,
			isFragmentWithinLargerReport);
		
		DiseaseMappingStudyArea studyArea = diseaseMappingStudy.getDiseaseMappingStudyArea();
		diseaseMappingStudyAreaContentHandler.writeHTML(
			2,
			studyArea, 
			isFragmentWithinLargerReport);		
		ComparisonArea comparisonArea = diseaseMappingStudy.getComparisonArea();
		comparisonAreaContentHandler.writeHTML(
			2,
			comparisonArea, 
			isFragmentWithinLargerReport);

		ArrayList<Investigation> investigations
			= diseaseMappingStudy.getInvestigations();
		investigationContentHandler.writeHTML(
			2,
			investigations, 
			isFragmentWithinLargerReport);
		
		if (isFragmentWithinLargerReport == false) {
			htmlUtility.endDocument();
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
			

	@Override
    public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isSingularRecordName(qualifiedName)) {
			currentDiseaseMappingStudy = DiseaseMappingStudy.newInstance();
			activate();
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
			AbstractXMLContentHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			
			//check to see if handlers could be assigned to delegate parsing
			if (geographyContentHandler.isSingularRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(geographyContentHandler);
			}
			else if (diseaseMappingStudyAreaContentHandler.isSingularRecordTypeApplicable(qualifiedName)) {
				assignDelegatedHandler(diseaseMappingStudyAreaContentHandler);
			}
			else if (comparisonAreaContentHandler.isSingularRecordTypeApplicable(qualifiedName) == true) {
				assignDelegatedHandler(comparisonAreaContentHandler);
			}
			else if (investigationContentHandler.isPluralRecordTypeApplicable(qualifiedName) == true) {
				assignDelegatedHandler(investigationContentHandler);
			}
			

			//delegate to a handler.  If not, then scan for fields relating to this handler
			if (isDelegatedHandlerAssigned()) {
				
				currentDelegatedHandler
					= getCurrentDelegatedHandler();
				currentDelegatedHandler.startElement(
					nameSpaceURI, 
					localName, 
					qualifiedName, 
					attributes);
			}
			else if (isSingularRecordName(qualifiedName) == true) {
				currentDiseaseMappingStudy = DiseaseMappingStudy.newInstance();
				activate();
			}
			else {
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
		
		if (isSingularRecordName(qualifiedName)) {
			deactivate();
		}
		else if (isDelegatedHandlerAssigned()) {
			AbstractXMLContentHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
			if (currentDelegatedHandler.isActive() == false) {
				if (currentDelegatedHandler == geographyContentHandler) {
					Geography geography = geographyContentHandler.getGeography();
					currentDiseaseMappingStudy.setGeography(geography);
				}
				else if (currentDelegatedHandler == diseaseMappingStudyAreaContentHandler) {
					DiseaseMappingStudyArea diseaseMappingStudyArea
						= diseaseMappingStudyAreaContentHandler.getDiseaseMappingStudyArea();
					currentDiseaseMappingStudy.setDiseaseMappingStudyArea(diseaseMappingStudyArea);					
				}
				else if (currentDelegatedHandler == comparisonAreaContentHandler) {
					ComparisonArea comparisonArea
						= comparisonAreaContentHandler.getComparisonArea();
					currentDiseaseMappingStudy.setComparisonArea(comparisonArea);
				}
				else if (currentDelegatedHandler == investigationContentHandler) {
					ArrayList<Investigation> investigations
						= investigationContentHandler.getInvestigations();
					currentDiseaseMappingStudy.setInvestigations(investigations);					
				}
				else {
					assert false;
				}				
				
				//handler just finished				
				unassignDelegatedHandler();				
			}
		}
		else if (equalsFieldName("name", qualifiedName) == true) {
			currentDiseaseMappingStudy.setName(getCurrentFieldValue());
		}
		else if (equalsFieldName("description", qualifiedName) == true) {
			currentDiseaseMappingStudy.setDescription(getCurrentFieldValue());
		}
		else {
			assert false;
		}
	}
}
