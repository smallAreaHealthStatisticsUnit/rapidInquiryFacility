package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.services.concepts.AbstractStudy;
import org.sahsu.rif.services.concepts.AbstractStudyArea;
import org.sahsu.rif.services.concepts.ComparisonArea;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.concepts.StudyType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class AbstractStudyContentHandler extends AbstractXMLContentHandler {

	private GeographyContentHandler geographyContentHandler;
	/** The disease mapping study area content handler. */
	AbstractGeographicalAreaContentHandler areaContentHandler;
	/** The comparison area content handler. */
	private ComparisonAreaContentHandler comparisonAreaContentHandler;
	/** The investigation content handler. */
	private InvestigationContentHandler investigationContentHandler;

	private RIFOutputOptionContentHandler rifOutputOptionContentHandler;
	/** The current disease mapping study. */
	private AbstractStudy currentStudy;

	AbstractStudyContentHandler() {
		comparisonAreaContentHandler = new ComparisonAreaContentHandler();
		rifOutputOptionContentHandler = new RIFOutputOptionContentHandler();
		geographyContentHandler = new GeographyContentHandler();
		investigationContentHandler = new InvestigationContentHandler();
	}

	@Override
	public void initialise(final OutputStream outputStream, final XMLCommentInjector commentInjector)
			throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);

		geographyContentHandler.initialise(outputStream, commentInjector);
		areaContentHandler.initialise(outputStream, commentInjector);
		comparisonAreaContentHandler.initialise(outputStream, commentInjector);
		investigationContentHandler.initialise(outputStream, commentInjector);
		rifOutputOptionContentHandler.initialise(outputStream, commentInjector);
	}

	public void initialise(final OutputStream outputStream) throws UnsupportedEncodingException {

		super.initialise(outputStream);
		geographyContentHandler.initialise(outputStream);
		areaContentHandler.initialise(outputStream);
		comparisonAreaContentHandler.initialise(outputStream);
		investigationContentHandler.initialise(outputStream);
		rifOutputOptionContentHandler.initialise(outputStream);
	}

	/**
	 * Gets the study.
	 *
	 * @return the study
	 */
	public AbstractStudy getStudy() {

		ComparisonArea comparisonArea = comparisonAreaContentHandler.getComparisonArea();
		AbstractStudyArea studyArea = areaContentHandler.getStudyArea();
		ArrayList<Investigation> investigations = investigationContentHandler.getInvestigations();
		currentStudy.setComparisonArea(comparisonArea);
		currentStudy.setStudyArea(studyArea);
		currentStudy.setInvestigations(investigations);

		return currentStudy;
	}

	/**
	 * Write xml.
	 *
	 * @param study the study
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(final AbstractStudy study) throws IOException {

		String recordName = getSingularRecordName();

		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(recordName);
		xmlUtility.writeField(recordName, "name", study.getName());
		xmlUtility.writeField(recordName, "description", study.getDescription());
		xmlUtility.writeField(recordName, "riskAnalysisExposureField", study.getRiskAnalysisExposureField());

		Geography geography = study.getGeography();
		geographyContentHandler.writeXML(geography);

		AbstractStudyArea studyArea = study.getStudyArea();
		areaContentHandler.writeXML(studyArea);

		ComparisonArea comparisonArea = study.getComparisonArea();
		comparisonAreaContentHandler.writeXML(comparisonArea);

		ArrayList<Investigation> investigations
			= study.getInvestigations();
		investigationContentHandler.writeXML(investigations);
		xmlUtility.writeRecordEndTag(recordName);
	}

	@Override
    public void startElement(final String nameSpaceURI, final String localName,
			final String qualifiedName, final Attributes attributes) throws SAXException {

		StudyType type = StudyType.fromTypeString(qualifiedName);

		if (isSingularRecordName(qualifiedName)) {

			currentStudy = AbstractStudy.newInstance(type);
			activate();
		} else if (isDelegatedHandlerAssigned()) {

			AbstractXMLContentHandler currentDelegatedHandler = getCurrentDelegatedHandler();
			currentDelegatedHandler.startElement(nameSpaceURI, localName, qualifiedName, attributes);
		} else {

			AbstractXMLContentHandler currentDelegatedHandler;

			//check to see if handlers could be assigned to delegate parsing
			if (geographyContentHandler.isSingularRecordTypeApplicable(qualifiedName)) {

				assignDelegatedHandler(geographyContentHandler);
			} else if (areaContentHandler.isSingularRecordTypeApplicable(qualifiedName)) {

				assignDelegatedHandler(areaContentHandler);
			} else if (comparisonAreaContentHandler.isSingularRecordTypeApplicable(qualifiedName)) {

				assignDelegatedHandler(comparisonAreaContentHandler);
			} else if (investigationContentHandler.isPluralRecordTypeApplicable(qualifiedName)) {

				assignDelegatedHandler(investigationContentHandler);
			}


			//delegate to a handler.  If not, then scan for fields relating to this handler
			if (isDelegatedHandlerAssigned()) {

				currentDelegatedHandler = getCurrentDelegatedHandler();
				currentDelegatedHandler.startElement(
					nameSpaceURI,
					localName,
					qualifiedName,
					attributes);
			} else if (isSingularRecordName(qualifiedName)) {

				currentStudy = AbstractStudy.newInstance(type);
				activate();
			}
		}
	}

	@Override
	public void endElement(final String nameSpaceURI, final String localName,
			final String qualifiedName) throws SAXException {

		if (isSingularRecordName(qualifiedName)) {
			deactivate();
		} else if (isDelegatedHandlerAssigned()) {
			AbstractXMLContentHandler currentDelegatedHandler
				= getCurrentDelegatedHandler();
			currentDelegatedHandler.endElement(
				nameSpaceURI,
				localName,
				qualifiedName);

			if (!currentDelegatedHandler.isActive()) {

				if (currentDelegatedHandler == geographyContentHandler) {
					Geography geography = geographyContentHandler.getGeography();
					currentStudy.setGeography(geography);
				} else if (currentDelegatedHandler == areaContentHandler) {
					AbstractStudyArea studyArea = areaContentHandler.getStudyArea();
					currentStudy.setStudyArea(studyArea);
				} else if (currentDelegatedHandler == comparisonAreaContentHandler) {
					ComparisonArea comparisonArea
						= comparisonAreaContentHandler.getComparisonArea();
					currentStudy.setComparisonArea(comparisonArea);
				} else if (currentDelegatedHandler == investigationContentHandler) {
					ArrayList<Investigation> investigations
						= investigationContentHandler.getInvestigations();
					currentStudy.setInvestigations(investigations);
				}

				//handler just finished
				unassignDelegatedHandler();
			}
		}
		else if (equalsFieldName("name", qualifiedName)) {
			currentStudy.setName(getCurrentFieldValue());
		}
		else if (equalsFieldName("description", qualifiedName)) {
			currentStudy.setDescription(getCurrentFieldValue());
		}
		else if (equalsFieldName("riskAnalysisExposureField", qualifiedName)) {
			currentStudy.setRiskAnalysisExposureField(getCurrentFieldValue());
		}
		else {
			assert false;
		}
	}
}
