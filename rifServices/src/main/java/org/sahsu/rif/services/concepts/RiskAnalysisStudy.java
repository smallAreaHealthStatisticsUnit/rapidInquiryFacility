package org.sahsu.rif.services.concepts;

public class RiskAnalysisStudy extends AbstractStudy {

	public static RiskAnalysisStudy newInstance() {

		return new RiskAnalysisStudy();
	}

	private RiskAnalysisStudy() {

		super(StudyType.RISK_ANALYSIS);
	}

	@Override
	public String getRecordType() {

		return SERVICE_MESSAGES.getMessage("riskAnalysisStudy.label");
	}
}
