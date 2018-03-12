package rifServices.system.files;

import org.apache.commons.lang.SystemUtils;
import rifGenericLibrary.util.RIFLogger;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TomcatBase {

	private static RIFLogger rifLogger = RIFLogger.getLogger();

	private Path baseDir;

	public TomcatBase() {

		Path catalinaBaseDir;
		String catalinaHome = System.getenv().get("CATALINA_HOME");
		if (System.getenv().get("CATALINA_HOME") != null) {

			catalinaBaseDir = FileSystems.getDefault().getPath(catalinaHome);
		} else {

			rifLogger.warning("rifServices.system.RIFServiceStartupProperties",
					"RIFServiceStartupProperties: CATALINA_HOME not set in environment." +
							"Trying Windows defaults");

			if (SystemUtils.IS_OS_WINDOWS) {
				catalinaBaseDir = Paths.get("C:", "Program Files",
						"Apache Software Foundation", "Tomcat 8.5");
			} else {

				// Add defaults for other platforms here, but until then...
				throw new RuntimeException("CATALINA_HOME must be set.");
			}
		}
	}

	public Path resolve() {

		return baseDir;
	}
}
