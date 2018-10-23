package org.sahsu.rif.generic.fileformats.tomcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Retrieves files from standard Tomcat locations, and makes their contents
 * available in various forms.
 */
public class TomcatFile {

	public static final String FRONT_END_PARAMETERS_FILE = "frontEndParameters.json5";

	private static final String CONF_DIRECTORY = "conf";
	private static final String WEBAPPS_DIRECTORY = "webapps";
	private static final String RIF_SERVICES_DIRECTORY = "rifServices";
	private static final String WEB_INF_DIRECTORY = "WEB-INF";
	private static final String CLASSES_DIRECTORY = "classes";
	private static final String LIB_DIRECTORY = "lib";

	private final Path file;
	private final Path classesPath;
	private final Path libPath;
	private Properties props;
	private BufferedReader reader;

	public TomcatFile(final TomcatBase base, final String fileName) {

		this(base.resolve(), fileName);
	}

	private TomcatFile(final Path baseDir, final String fileName) {

		Path confPath = baseDir.resolve(CONF_DIRECTORY);
		classesPath = baseDir.resolve(WEBAPPS_DIRECTORY).resolve(RIF_SERVICES_DIRECTORY)
				.resolve(WEB_INF_DIRECTORY).resolve(CLASSES_DIRECTORY);
		libPath = baseDir.resolve(WEBAPPS_DIRECTORY).resolve(RIF_SERVICES_DIRECTORY)
				          .resolve(WEB_INF_DIRECTORY).resolve(LIB_DIRECTORY);

		Path tempPath = confPath.resolve(fileName);
		if (!tempPath.toFile().exists()) {

			tempPath = classesPath.resolve(fileName);
		}

		file = tempPath;
	}

	public Path path() {

		return file;
	}

	public Path pathToClassesDirectory() {

		return classesPath;
	}

	public Path pathToLibDirectory() {

		return libPath;
	}

	public BufferedReader reader() throws IOException {

		if (reader == null) {
			reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
		}
		return reader;
	}

	public Properties properties() throws IOException {

		if (props == null) {
			props = new Properties();
			props.load(reader());
		}
		return props;
	}

	public String absolutePath() {

		return file.toFile().getAbsolutePath();
	}

	public URL asUrl() throws MalformedURLException {

		return file.toFile().toURI().toURL();
	}
}
