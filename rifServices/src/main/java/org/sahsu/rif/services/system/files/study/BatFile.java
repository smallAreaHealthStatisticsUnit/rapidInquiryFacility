package org.sahsu.rif.services.system.files.study;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

import lombok.Builder;

@Builder
public class BatFile {

	private String directory;
	private int studyId;
	private String userId;
	private String dbName;
	private String dbHost;
	private String dbPort;
	private String dbDriverPrefix;
	private String dbDriverClassName;
	private String investigationName;
	private String studyName;
	private int investigationId;
	private String odbcDataSource;
	private String model;
	private String covariateName;

	public void createEnvScript() throws IOException {

		StringBuilder scriptText = new StringBuilder();
		scriptText.append("SET USERID=").append(userId).append(System.lineSeparator())
				.append("SET DB_NAME=").append(dbName).append(System.lineSeparator())
				.append("SET DB_HOST=").append(dbHost).append(System.lineSeparator())
				.append("SET DB_PORT=").append(dbPort).append(System.lineSeparator())
				.append("SET DB_DRIVER_PREFIX=").append(dbDriverPrefix)
					.append(System.lineSeparator())
				.append("SET DB_DRIVER_CLASS_NAME=").append(dbDriverClassName)
					.append(System.lineSeparator())
				.append("SET STUDYID=").append(studyId).append(System.lineSeparator())
				.append("SET INVESTIGATIONNAME=").append(investigationName)
					.append(System.lineSeparator())
				.append("SET STUDYNAME=").append(studyName).append(System.lineSeparator())
				.append("SET INVESTIGATIONID=").append(investigationId)
					.append(System.lineSeparator())
				.append("SET ODBCDATASOURCE=").append(odbcDataSource).append(System.lineSeparator())
				.append("SET MODEL=").append(model).append(System.lineSeparator())
				.append("SET COVARIATENAME=").append(covariateName).append(System.lineSeparator());

		Path dataDir = ScratchDirectories.builder().directory(directory).studyId(studyId)
				               .build()
				               .dataDir();
		 try(Writer writer = new FileWriter(dataDir.resolve("rif40_run_R_env.bat").toFile())) {

		 	writer.write(scriptText.toString());
		 }
	}
}
