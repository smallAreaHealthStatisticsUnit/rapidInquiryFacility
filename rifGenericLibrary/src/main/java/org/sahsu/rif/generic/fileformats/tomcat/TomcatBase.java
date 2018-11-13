package org.sahsu.rif.generic.fileformats.tomcat;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;
import org.sahsu.rif.generic.util.RIFLogger;

/**
 * Provides a reference to the base directory of a Tomcat installation, or
 * CATALINA_HOME, as it is usually set in the environment.
 */
public class TomcatBase {

	private static RIFLogger rifLogger = RIFLogger.getLogger();

	private final Path baseDir;

	public TomcatBase() {

		String catalinaHome = System.getenv().get("CATALINA_HOME");
		if (catalinaHome != null) {
			baseDir = FileSystems.getDefault().getPath(catalinaHome);
		} else {
			rifLogger.warning(getClass(),
					"RIFServiceStartupProperties: CATALINA_HOME not set in environment." +
							"Trying system defaults");

			if (SystemUtils.IS_OS_WINDOWS) {
				baseDir = Paths.get("C:", "Program Files",
								"Apache Software Foundation", "Tomcat 8.5");
				
			} else if (SystemUtils.IS_OS_MAC) {
				baseDir = Paths.get("usr", "local", "Cellar", "tomcat", "9.0.6", "libexec");
				
			} else {

				// Add defaults for other platforms here, but until then...
				throw new RuntimeException("CATALINA_HOME must be set.");
			}
		}
	}

	Path resolve() {

		return baseDir;
	}
}
