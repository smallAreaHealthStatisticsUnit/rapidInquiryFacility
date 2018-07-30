package org.sahsu.rif.services.fileformats;

import org.sahsu.rif.services.concepts.AbstractStudy;
import org.sahsu.rif.services.concepts.StudyType;

class RiskAnalysisStudyContentHandler extends AbstractStudyContentHandler {

	RiskAnalysisStudyContentHandler() {

		super();
		setSingularRecordName(StudyType.RISK_ANALYSIS.type());
		areaContentHandler = new StudyAreaContentHandler(StudyType.RISK_ANALYSIS.area());
	}
}
