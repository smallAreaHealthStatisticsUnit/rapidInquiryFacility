package org.sahsu.rif.services.util;

import org.sahsu.rif.generic.util.RIFLogger;

import java.util.Arrays;
import java.util.List;

/**
 * Converts a String value to its boolean equivalent, allowing for number of
 * common values.
 */
public class BooleanFromString {

	private RIFLogger rifLogger = RIFLogger.getLogger();
	private static final List<String> TRUE_VALUES = Arrays.asList("y", "yes", "1");

	private final String value;

	public BooleanFromString(final String value) {

		this.value = value;
	}

	public boolean toBoolean() {

		// Boolean.valueOf handles "true" in any case.
		boolean what = Boolean.valueOf(value) || TRUE_VALUES.contains(value.toLowerCase());
		rifLogger.debug(getClass(), "returning boolean " + what  + " for " + value);
		return what;
	}

	// INCOMPLETE: not sure about this.
	public String loggingTruth(String message) {

		return String.format(message , (value == null ? "NOT FOUND" : toBoolean()));
	}
}
