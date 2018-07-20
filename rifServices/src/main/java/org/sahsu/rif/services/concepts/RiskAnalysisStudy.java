package org.sahsu.rif.services.concepts;

import org.sahsu.rif.generic.system.RIFServiceException;

public class RiskAnalysisStudy extends AbstractStudy {

	@Override
	public String getRecordType() {

		return SERVICE_MESSAGES.getMessage("riskAnalysisStudy.label");
	}

	@Override
	public void checkErrors(final ValidationPolicy validationPolicy) throws RIFServiceException {

	}
}
