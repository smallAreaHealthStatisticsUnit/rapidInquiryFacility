package org.sahsu.rif.generic.fileformats;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.sahsu.rif.generic.system.RIFServiceException;

public class FilePath {

	private final Path filePath;

	public FilePath(final String fileName) throws RIFServiceException {

		if (StringUtils.isEmpty(fileName)) {

			throw new RIFServiceException("Received empty file name");
		}

		URL url = getClass().getClassLoader().getResource(fileName);
		if (url == null) {

			throw new RIFServiceException("Couldn't get URL for file " + fileName);
		} else {

			try {

				filePath = Paths.get(url.toURI());
			} catch (URISyntaxException e) {

				throw new RIFServiceException(e, "Problem with URI of file %s", fileName);
			}

			if (!filePath.toFile().exists()) {

				String msg = String.format("ICD9 file %s not found", filePath.toString());
				throw new RIFServiceException(msg);
			}
		}
	}

	public Path getPath() {

		return filePath;
	}
}
