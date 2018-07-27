package org.sahsu.rif.services.concepts;

import org.sahsu.rif.generic.system.RIFServiceException;

public class RiskAnalysisStudy extends AbstractStudy {

	public static RiskAnalysisStudy newInstance() {

		return new RiskAnalysisStudy();
	}

	@Override
	public String getRecordType() {

		return SERVICE_MESSAGES.getMessage("riskAnalysisStudy.label");
	}

	@Override
	public void checkErrors(final ValidationPolicy validationPolicy) throws RIFServiceException {

	}
}
