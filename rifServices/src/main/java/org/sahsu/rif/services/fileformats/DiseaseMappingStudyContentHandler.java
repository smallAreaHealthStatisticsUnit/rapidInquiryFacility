
package org.sahsu.rif.services.fileformats;

import org.sahsu.rif.services.concepts.AbstractStudy;
import org.sahsu.rif.services.concepts.StudyType;

final class DiseaseMappingStudyContentHandler extends AbstractStudyContentHandler {

	/**
     * Instantiates a new disease mapping study content handler.
     */
    DiseaseMappingStudyContentHandler() {

    	super();
	    setSingularRecordName(StudyType.DISEASE_MAPPING.type());
	    areaContentHandler = new StudyAreaContentHandler(StudyType.DISEASE_MAPPING.area());
    }

}
