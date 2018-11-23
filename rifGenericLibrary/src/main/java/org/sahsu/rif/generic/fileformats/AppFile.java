package org.sahsu.rif.generic.fileformats;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;

import org.sahsu.rif.generic.fileformats.tomcat.TomcatBase;
import org.sahsu.rif.generic.fileformats.tomcat.TomcatFile;

/**
 * Represents a file on a filesystem, supporting a number of possible ways to load the underlying
 * file.
 */
public interface AppFile {

	String FRONT_END_PARAMETERS_FILE = "frontEndParameters.json5";

	/**
	 * Returns an AppFile for the specified file, assuming we're in the rifServices environment.
	 * @param fileName the name of the file
	 * @return a representation of the specified file name
	 */
	static AppFile getServicesInstance(String fileName) {

		// Only Tomcat versions for now
		return getInstance(fileName, TomcatFile.ServiceType.RIF);
	}

	/**
	 * Returns an AppFile for the specified file, assuming we're in the taxonomyServices
	 * environment.
	 * @param fileName the name of the file
	 * @return a representation of the specified file name
	 */
	static AppFile getTaxonomyInstance(String fileName) {

		return getInstance(fileName, TomcatFile.ServiceType.TAXONOMY);
	}

	/**
	 * Returns an AppFile for the specified file, assuming we're in the statistics service
	 * environment.
	 * @param fileName the name of the file
	 * @return a representation of the specified file name
	 */
	static AppFile getStatisticsInstance(String fileName) {

		return getInstance(fileName, TomcatFile.ServiceType.STATS);
	}

	/**
	 * Returns an AppFile for the specified file, allowing for selection between the various
	 * environments.
	 * @param fileName the name of the file
	 * @param type the approprate {@code ServiceType}
	 * @return a representation of the specified file name
	 */
	static AppFile getInstance(String fileName, TomcatFile.ServiceType type) {

		return new TomcatFile(new TomcatBase(), fileName, type);
	}

	static AppFile getFrontEndParametersInstance() {

		return getServicesInstance(FRONT_END_PARAMETERS_FILE);
	}

	/**
	 * Returns the {@code java.nio.file.Path} that refers to this object.
	 * @return the Path
	 */
	Path path();

	/**
	 * Returns the path to the directory where the classes are stored. This is a bit specific to
	 * running in an app server, which is fine at present. But it should have meaning in other
	 * contexts.
	 * @return the path to the directory containing the classes
	 */
	Path pathToClassesDirectory();

	/**
	 * Returns the path to the directory that contains third-party libraries. In an app server
	 * context this is usually called lib.
	 * @return the path to the directory containing the libraries
	 */
	Path pathToLibDirectory();

	BufferedReader reader() throws IOException;

	File asFile();

	String asString();

	URL asUrl() throws MalformedURLException;

	Properties properties() throws IOException;
}
