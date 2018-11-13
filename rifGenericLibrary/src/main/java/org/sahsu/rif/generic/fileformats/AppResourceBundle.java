package org.sahsu.rif.generic.fileformats;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.sahsu.rif.generic.util.RIFLogger;

/**
 * A wrapper for an {@link AppFile}, accessing it as a {@link ResourceBundle}.
 */
public class AppResourceBundle {

	private static RIFLogger rifLogger = RIFLogger.getLogger();
	private ResourceBundle bundle;
	private AppFile file;

	public AppResourceBundle(AppFile file) {

		this.file = file;

		try (BufferedReader reader = file.reader()) {

			bundle = new PropertyResourceBundle(reader);
			rifLogger.info(getClass().getName(),
					"Loading Resource Bundle from file: " +
							file.path().toString());
		} catch (IOException ioException) {

			rifLogger.error(getClass().getName(),
					"RIFServiceStartupProperties error for files " +
							file.path().getFileName(),
					ioException);
		}
	}

	public ResourceBundle bundle() {

		return bundle;
	}

	public AppFile appFile() {

		return file;
	}
}
