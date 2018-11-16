package org.sahsu.rif.generic.fileformats.tomcat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.sahsu.rif.generic.fileformats.AppFile;

/**
 * Retrieves files from standard Tomcat locations, and makes their contents
 * available in various forms.
 */
public class TomcatFile implements AppFile {

	private static final String CONF_DIRECTORY = "conf";
	private static final String WEBAPPS_DIRECTORY = "webapps";
	private static final String RIF_SERVICES_DIRECTORY = "rifServices";
	private static final String TAXONOMY_SERVICES_DIRECTORY = "taxonomies";
	private static final String STATS_SERVICE_DIRECTORY = "statistics";
	private static final String WEB_INF_DIRECTORY = "WEB-INF";
	private static final String CLASSES_DIRECTORY = "classes";
	private static final String LIB_DIRECTORY = "lib";

	private final Path file;
	private final Path classesPath;
	private final Path libPath;
	private Properties props;
	private BufferedReader reader;

	public enum ServiceType {

		RIF,
		TAXONOMY,
		STATS
	}

	public TomcatFile(final TomcatBase base, final String fileName, final ServiceType type) {

		Path baseDir = base.resolve();
		Path confPath = baseDir.resolve(CONF_DIRECTORY);

		String servicesDir;
		switch (type) {

			case RIF:
				servicesDir = RIF_SERVICES_DIRECTORY;
				break;
			case TAXONOMY:
				servicesDir = TAXONOMY_SERVICES_DIRECTORY;
				break;
			case STATS:
				servicesDir = STATS_SERVICE_DIRECTORY;
				break;
			default:
				servicesDir = "";
		}

		classesPath = baseDir.resolve(WEBAPPS_DIRECTORY).resolve(servicesDir)
				              .resolve(WEB_INF_DIRECTORY).resolve(CLASSES_DIRECTORY);
		libPath = baseDir.resolve(WEBAPPS_DIRECTORY).resolve(servicesDir)
				          .resolve(WEB_INF_DIRECTORY).resolve(LIB_DIRECTORY);

		Path tempPath = confPath.resolve(fileName);
		if (!tempPath.toFile().exists()) {

			tempPath = classesPath.resolve(fileName);
		}

		file = tempPath;
	}

	@Override
	public Path path() {

		return file;
	}

	@Override
	public Path pathToClassesDirectory() {

		return classesPath;
	}

	@Override
	public Path pathToLibDirectory() {

		return libPath;
	}

	@Override
	public BufferedReader reader() throws IOException {

		if (reader == null) {
			reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
		}
		return reader;
	}

	@Override
	public Properties properties() throws IOException {

		if (props == null) {
			props = new Properties();
			props.load(reader());
			reader.close();
		}
		return props;
	}

	@Override
	public String asString() {

		return file.toFile().getAbsolutePath();
	}

	@Override
	public URL asUrl() throws MalformedURLException {

		return asFile().toURI().toURL();
	}


	public File asFile() {

		return file.toFile();
	}
}
