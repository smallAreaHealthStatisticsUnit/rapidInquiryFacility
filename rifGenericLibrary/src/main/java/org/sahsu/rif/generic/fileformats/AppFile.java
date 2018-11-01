package org.sahsu.rif.generic.fileformats;

import java.io.BufferedReader;
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
	 * Returns a AppFile for the specified file, assuming we're in the rifServices environment.
	 * @param fileName the name of the file
	 * @return a representation of the specified file name
	 */
	static AppFile getInstance(String fileName) {

		// Only Tomcat versions for now
		return getInstance(fileName, false);
	}

	/**
	 * Returns a AppFile for the specified file, allowing for selection between the rifServices
	 * and taxonomyServices environments.
	 * @param fileName the name of the file
	 * @param taxonomy true if the taxonomyServices enviromnent is required, false for rifServices.
	 * @return a representation of the specified file name
	 */
	static AppFile getInstance(String fileName, boolean taxonomy) {

		return new TomcatFile(new TomcatBase(), fileName, taxonomy);
	}

	static AppFile getFrontEndParametersInstance() {

		return getInstance(FRONT_END_PARAMETERS_FILE);
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

	String absolutePath();

	URL asUrl() throws MalformedURLException;

	Properties properties() throws IOException;
}
