package org.sahsu.rif.services.concepts;

import java.util.Optional;

/**
 * Enumeration of the available types of study in the RIF.
 */
public enum StudyType {

	DISEASE_MAPPING("disease_mapping_study"),
	RISK_ANALYSIS("risk_analysis_study"),
	NO_VALUE("no_value");

	private final String theType;
	StudyType(String type) {

		this.theType = type;
	}

	public String type() {

		return theType;
	}

	public String area() {

		return theType + "_area";
	}

	public static boolean hasValue(String s) {

		for (StudyType t: values()) {

			if (t.type().equals(s)) {
				return true;
			}
		}
		return false;
	}

	public static StudyType fromAreaString(String s) {

		for (StudyType t: values()) {

			if (t.area().equals(s)) {
				return t;
			}
		}
		return NO_VALUE;
	}

	public static StudyType fromTypeString(String s) {

		for (StudyType t: values()) {

			if (t.type().equals(s)) {
				return t;
			}
		}
		return NO_VALUE;
	}

}
