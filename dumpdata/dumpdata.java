
import java.lang.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

import rif40_db.RifDBLogin;

public class dumpdata {
	public static void main(String[] argv) {

	Connection connection = null;
		String str=null;
		long starttime=0;
		long endtime=0;
	
//
// Expecting table name to dump
//
		if (argv.length == 0) {
			System.out.println("Usage: dumpdata <table or view name>");
			System.exit(1);
		}

		connection=RifDBLogin.rif_dbconnect();
		if (connection == null) {
			System.out.println("Failed to make connection!");
			System.exit(1);
		}

		try {
// Run rif40_dmp_pkg.csv_dump()
			PreparedStatement stmt=null;
			ResultSet rs=null;
			SQLWarning warn=null;
			String csv_sql="SELECT rif40_dmp_pkg.csv_dump(LOWER(?)::VARCHAR) AS csv_dump";
			String csv_file_name=null;
			FileWriter file=null;
			BufferedWriter bf=null;
			int i=0;
			int j=0;
			double elapsed_time=0;
			double rate=0;

			stmt=connection.prepareStatement(csv_sql);
			stmt.setFetchSize(1000); // big
			stmt.setString(1, argv[0]);
			rs=stmt.executeQuery();
			warn=stmt.getWarnings();
// Extract warnings (INFO/NOTICE/WARNINGS)
			while (warn != null) {
				System.out.println(warn.getMessage());
				warn=warn.getNextWarning();
			}
// Set up output file with a big buffer
			csv_file_name=argv[0] + ".csv";
			System.setProperty("line.separator", "\r\n");
			file = new FileWriter(csv_file_name);
			bf=new BufferedWriter(file, 4096*1024);
// Extract CSV data
			while (rs.next()) {
				i++;
				str=rs.getString("csv_dump");
				if (str != null) {
					j+=str.split("\r\n").length;
					bf.write(str);
					bf.newLine();
				}
			}
			bf.close();
			connection.close(); 
			endtime=System.nanoTime();
			elapsed_time=(endtime-starttime)/(double)(1000*1000*1000);
			rate=(double)j/elapsed_time;
			System.out.println("Wrote " + i + " fetches  " + j + " rows to: " + csv_file_name + 
				", in: " + String.format("%.3g", elapsed_time) + "s at " + String.format("%,.1f", rate) + " records/s");
		} 
		catch (SQLException e) {
 
			System.out.println("Extraction Failed! Check SQL error");
			e.printStackTrace();
			return;
 
		}
		catch (IOException i) {
 
			System.out.println("Extraction Failed! Check IO error");
			i.printStackTrace();
			return;
 
		}
	}
}
