package org.sahsu.stats.service;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StatisticsFunctions {

	private static final String SMOOTHING_KEY = "smoothing";
	private static final String RISK_ANALYSIS_KEY = "risk";
	static final String STUDY_ID = "studyId";
	static final String STUDY_ID_URI = "/{" + STUDY_ID + "}";
	private static final String SMOOTHING_URI = "/smoothing";
	private static final String RISK_ANALYSIS_URI = "/riskanalysis";
	static final String SCRIPT_URI = "/script";

	private final Map<String, String> functions = new HashMap<>();

	private static Map<String, StatisticsFunctions> instances = new HashMap<>();

	static StatisticsFunctions getInstance(String studyId) {

		if (!instances.containsKey(studyId)) {

			instances.put(studyId, new StatisticsFunctions(studyId));
		}
		return instances.get(studyId);
	}

	private StatisticsFunctions(final String studyId) {

		functions.put(SMOOTHING_KEY, SMOOTHING_URI + "/" + studyId);
		functions.put(RISK_ANALYSIS_KEY, RISK_ANALYSIS_URI + "/" + studyId);
	}

	@SuppressWarnings("unused")
	private StatisticsFunctions() {
		// Needed for JAXB
	}


	@XmlElement
	@SuppressWarnings("unused")
	public String getSmoothingFunction() {

		return functions.get(SMOOTHING_KEY);
	}

	@XmlElement
	@SuppressWarnings("unused")
	public String getRiskAnalysisFunction() {

		return functions.get(RISK_ANALYSIS_KEY);
	}
}
