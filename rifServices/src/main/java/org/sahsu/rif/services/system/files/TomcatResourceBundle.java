package org.sahsu.rif.services.system.files;

import org.sahsu.rif.generic.util.RIFLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * A wrapper for a {@link TomcatFile}, accessing it as a {@link ResourceBundle}.
 */
public class TomcatResourceBundle {

	private static RIFLogger rifLogger = RIFLogger.getLogger();
	private ResourceBundle bundle;
	private TomcatFile file;

	public TomcatResourceBundle(TomcatFile file) {

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

	public TomcatFile tomcatFile() {

		return file;
	}
}