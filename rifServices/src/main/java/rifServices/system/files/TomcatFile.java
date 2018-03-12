package rifServices.system.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class TomcatFile {

	private Path file;

	public TomcatFile(TomcatBase base, String fileName) {

		this(base.resolve(), fileName);
	}

	public TomcatFile(Path baseDir, String fileName) {

		Path dir1 = baseDir.resolve("conf");
		Path dir2 = baseDir.resolve("webapps").resolve("rifServices")
				.resolve("WEB-INF").resolve("classes");
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

}
