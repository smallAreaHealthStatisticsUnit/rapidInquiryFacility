package org.sahsu.rif.services.fileformats;

import org.sahsu.rif.services.concepts.AbstractStudy;

public class RiskAnalysisStudyContentHandler extends AbstractStudyContentHandler {

	public RiskAnalysisStudyContentHandler() {

		super();
		setSingularRecordName(AbstractStudy.RISK_ANALYSIS_STUDY);
	}
}
