
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.services.concepts.AbstractStudyArea;
import org.sahsu.rif.services.concepts.StudyType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

final class StudyAreaContentHandler extends AbstractGeographicalAreaContentHandler {

	/**
     * Instantiates a new disease mapping study area content handler.
     */
    StudyAreaContentHandler(String recordName) {
		
		setSingularRecordName(recordName);
	}

	/**
	 * Write xml.
	 *
	 * @param studyArea the disease mapping study area
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(final AbstractStudyArea studyArea) throws IOException {

		String recordName = getSingularRecordName();
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(recordName);
		super.writeXML(studyArea);
		xmlUtility.writeRecordEndTag(recordName);
	}

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

		if (isSingularRecordName(qualifiedName)) {
			currentStudyArea = AbstractStudyArea.newInstance(
					StudyType.fromAreaString(qualifiedName));
			activate();				
		}
		else {
			super.startElement(nameSpaceURI, localName, qualifiedName, attributes);
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
		else {
			super.endElement(nameSpaceURI, localName, qualifiedName);
		}
	}
}
