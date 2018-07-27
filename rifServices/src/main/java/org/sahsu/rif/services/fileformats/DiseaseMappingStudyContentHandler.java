
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.util.ArrayList;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.services.concepts.AbstractStudy;
import org.sahsu.rif.services.concepts.AbstractStudyArea;
import org.sahsu.rif.services.concepts.ComparisonArea;
import org.sahsu.rif.services.concepts.DiseaseMappingStudy;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.Investigation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

final class DiseaseMappingStudyContentHandler extends AbstractStudyContentHandler {

	/**
     * Instantiates a new disease mapping study content handler.
     */
    DiseaseMappingStudyContentHandler() {

    	super();
	    setSingularRecordName("disease_mapping_study");
    }

}
