package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.RVector;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.InsertQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

import lombok.Builder;

public class TempTableCreator extends BaseSQLManager {

	// private final String[] columnNames;
	private final RList data;
	private final User user;
	private String tempTable;

	@Builder
	private TempTableCreator(final RIFServiceStartupOptions rifServiceStartupOptions,
			final RList data, final User user, final int studyId) {

		super(rifServiceStartupOptions);

		this.data = data;
		this.user = user;
		tempTable = user.getUserID() + ".tmp_s" + studyId + "_map";
	}

	public void create() throws RIFServiceException, SQLException {

		makeTempMapTable();
		populateTempMapTable();
	}

	private void makeTempMapTable() throws RIFServiceException, SQLException {

		SQLGeneralQueryFormatter formatter = new SQLGeneralQueryFormatter();
		formatter.addQueryLine(0, "create table " + tempTable + " (");
		formatter.finishLine();

		addColumnDetails(formatter);

		formatter.addQueryPhrase(")");

		Connection connection = assignPooledReadConnection(user);
		try (PreparedStatement createTempTable = SQLQueryUtility.createPreparedStatement(
				connection, formatter.generateQuery())) {

			ResultSet resultSet = createTempTable.executeQuery();

			} finally {

			reclaimPooledReadConnection(user, connection);
		}
	}

	private void populateTempMapTable() throws RIFServiceException, SQLException {

		InsertQueryFormatter formatter = InsertQueryFormatter.getInstance(
				rifDatabaseProperties.getDatabaseType());
		formatter.setIntoTable(tempTable);

		for (String column : data.keys()) {

			formatter.addInsertField(column);
		}

		Connection connection = assignPooledReadConnection(user);
		try (PreparedStatement populateTempTable = SQLQueryUtility.createPreparedStatement(
				connection, formatter.generateQuery())) {

			for (int i = 1; i <= data.keys().length; i++) {

				populateTempTable.setObject(i, data.at(i));
			}

			ResultSet resultSet = populateTempTable.executeQuery();

		} finally {

			reclaimPooledReadConnection(user, connection);
		}

	}

	private void addColumnDetails(final SQLGeneralQueryFormatter formatter) {


		int i = 0;
		for (String column: data.keys()) {

			REXP colData = data.at(column);

			String type = dataType(colData);
			formatter.addQueryLine(1, column + " " + type);

			if (i < data.keys().length - 1) {
				formatter.addQueryPhrase(", ");
			}
			i++;

			formatter.finishLine();
		}
	}

	private String dataType(REXP dataItemFromR) {

		switch (dataItemFromR.getType()) {

			case REXP.XT_INT:
			case REXP.INTSXP:
			case REXP.XT_ARRAY_INT:
				return "int";
			case REXP.XT_DOUBLE:
			case REXP.XT_ARRAY_DOUBLE:
				return "real";
			case REXP.XT_STR:
			case REXP.STRSXP:
			case REXP.XT_ARRAY_STR:
				return "varchar(255)";
		}
		return "varchar(255)"; // Hopeful fallback.
	}
}
