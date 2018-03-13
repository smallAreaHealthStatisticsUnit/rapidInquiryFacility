package rifServices.system.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class TomcatFile {

	public static final String CONF_DIRECTORY = "conf";
	public static final String WEBAPPS_DIRECTORY = "webapps";
	public static final String RIF_SERVICES_DIRECTORY = "rifServices";
	public static final String WEB_INF_DIRECTORY = "WEB-INF";
	public static final String CLASSES_DIRECTORY = "classes";
	public static final String FRONT_END_PARAMETERS_FILE = "frontEndParameters.json5";

	private Path file;

	public TomcatFile(TomcatBase base, String fileName) {

		this(base.resolve(), fileName);
	}

	public TomcatFile(Path baseDir, String fileName) {

		Path dir1 = baseDir.resolve(CONF_DIRECTORY);
		Path dir2 = baseDir.resolve(WEBAPPS_DIRECTORY).resolve(RIF_SERVICES_DIRECTORY)
				.resolve(WEB_INF_DIRECTORY).resolve(CLASSES_DIRECTORY);
		file = dir1.resolve(fileName);
		if (!file.toFile().exists()) {

			file = dir2.resolve(fileName);
		}
	}

	public Path path() {

		return file;
	}

	public BufferedReader reader() throws IOException {

		return Files.newBufferedReader(file, StandardCharsets.UTF_8);
	}

	public Properties properties() throws IOException {

		Properties props = new Properties();
		props.load(reader());
		return props;
	}

	public String absolutePath() {

		return file.toFile().getAbsolutePath();
	}

}
