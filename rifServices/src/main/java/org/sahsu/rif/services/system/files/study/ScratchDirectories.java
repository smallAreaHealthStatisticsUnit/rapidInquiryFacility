package org.sahsu.rif.services.system.files.study;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.Builder;

public class ScratchDirectories {

	private final Path scratchDir;

	@Builder
	private ScratchDirectories(final int studyId, final String directory) throws IOException {

		// We split the directories into blocks of 100 to stop any one from having too many
		// entries.
		int centile = studyId / 100;
		String numberDir  = "d" + (centile * 100 + 1) + "-" + (centile + 1) * 100;

		scratchDir = Paths.get(
				directory,
				"scratchSpace",
				numberDir,
				"s" + studyId);

		// Make sure the directory tree exists
		Files.createDirectories(scratchDir);
	}

	public Path dataDir() throws IOException {

		Path path = scratchDir.resolve("data");
		Files.createDirectories(path);
		return path;
	}

	public Path geographyDir() throws IOException {

		Path path = scratchDir.resolve("geography");
		Files.createDirectories(path);
		return path;
	}

	public Path reportsDir() throws IOException {

		Path path = scratchDir.resolve("reports");
		Files.createDirectories(path);
		return path;
	}

}
