package org.sahsu.rif.generic.fileformats;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.sahsu.rif.generic.system.RIFServiceException;

public class ClasspathFilePath {

	private final Path filePath;

	/**
	 * A utility class that gets hold of a file given a file name. It makes use of the
	 * {@code java.nio.file} features to provide a {@code Path} object, and it uses the classpath
	 * to load the file, so it is not tied to a specific filesystem.
	 *
	 * @param fileName the name of the file we're interested in
	 * @throws RIFServiceException if the name is not provided or the file is not found.
	 */
	public ClasspathFilePath(final String fileName) throws RIFServiceException {

		if (StringUtils.isEmpty(fileName)) {

			throw new RIFServiceException("Received empty file name");
		}

		URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
		if (url == null) {

			throw new RIFServiceException("Couldn't get URL for file " + fileName);
		} else {

			try {

				filePath = Paths.get(url.toURI());
			} catch (URISyntaxException e) {

				throw new RIFServiceException(e, "Problem with URI of file %s", fileName);
			}

			if (!filePath.toFile().exists()) {

				String msg = String.format("File %s not found", filePath.toString());
				throw new RIFServiceException(msg);
			}
		}
	}

	public Path getPath() {

		return filePath;
	}
}
