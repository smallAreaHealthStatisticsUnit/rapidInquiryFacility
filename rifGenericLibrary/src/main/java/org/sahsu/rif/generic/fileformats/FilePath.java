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
public interface FilePath {

	String FRONT_END_PARAMETERS_FILE = "frontEndParameters.json5";

	static FilePath getInstance(String fileName) {

		// Only Tomcat versions for now
		return new TomcatFile(new TomcatBase(), fileName);
	}

	static FilePath getInstance(String fileName, boolean taxonomy) {

		return new TomcatFile(new TomcatBase(), fileName, taxonomy);
	}

	/**
	 * Returns the {@code java.nio.file.Path} that refers to this object.
	 * @return the Path
	 */
	Path path();

	BufferedReader reader() throws IOException;

	String absolutePath();

	URL asUrl() throws MalformedURLException;

	Properties properties() throws IOException;
}
