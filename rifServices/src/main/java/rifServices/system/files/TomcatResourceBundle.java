package rifServices.system.files;

import rifGenericLibrary.util.RIFLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class TomcatResourceBundle {

	private static RIFLogger rifLogger = RIFLogger.getLogger();
	private ResourceBundle bundle;

	public TomcatResourceBundle(TomcatFile file) {

		try (BufferedReader reader = file.reader()) {

			bundle = new PropertyResourceBundle(reader);
			rifLogger.info(getClass().getName(),
					"RIFServiceStartupProperties: using: " +
							file.path().getFileName());
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
}
