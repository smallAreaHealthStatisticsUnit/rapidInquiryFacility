
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

public class Login {

    /**
     * To run:
     *		javac Login.java
     *          jar -cvf Login.jar Login.class
     *		java -cp Login.jar:postgresql-9.2-1002.jdbc4.jar Login
     * or:
     * 		javac Login.java ; jar -cvf Login.jar Login.class ; java -cp `pwd`:postgresql-9.2-1002.jdbc4.jar:Login.jar Login
     *
     * Windows:	java -cp P:\src\SAHSU\projects\rif\V4.0\create\install;postgresql-9.2-1002.jdbc4.jar;Login.jar Login
     */
 
	public static void main(String[] argv) {
 
		Connection connection = null;
		Properties props = new Properties();

   	     	// set up new properties object from file "RIFProperties.txt"
		FileInputStream propFile;
		PrintWriter jaasFile;
		String url;
		String driver;

		String os_name=System.getProperty("os.name");

		try {
        		propFile = new FileInputStream( "RIFProperties.txt");
		} catch (FileNotFoundException e) {

			System.out.println("Cannot find: RIFProperties.txt");
			e.printStackTrace();
			return;
		}

		try {
        		Properties rif_props = new Properties(System.getProperties());
        		rif_props.load(propFile);

		        // set the system properties
        		System.setProperties(rif_props);
		} catch (IOException e) {

			System.out.println("Cannot load: RIFProperties.txt");
			e.printStackTrace();
			return;
		}

		System.out.println("-------- JDBC Connection Testing ------------");
 
		if (System.getProperty("rif.logon.driver") == null) {
			driver = "org.postgresql.Driver";			// Default
		}
		else {
			driver=System.getProperty("rif.logon.driver");
		}
		// Load Postgres driver
		try {
			Class.forName(driver);
 
		} catch (ClassNotFoundException e) {
 
			System.out.println("Where is your " + driver + " JDBC Driver? "
					+ "Include driver in your library path!");
			e.printStackTrace();
			return;
 
		}
		System.out.println(driver + " JDBC Driver Registered!");

		// JDBC connection url. Use properties file to alter host and database
		if (System.getProperty("rif.logon.url") == null) {
			url = "jdbc:postgresql://localhost/sahsuland";			// Default
		}
		else {
			url=System.getProperty("rif.logon.url");
		}
		// User (name). Default: OS USER
		if (System.getProperty("rif.logon.user") == null) {
			props.setProperty("user", System.getProperty("user.name"));	// Default
		}
		else {
			props.setProperty("user", System.getProperty("rif.logon.user"));
		}
		// Password. No default (assumed using GSSAPI/SSPI)
		if (System.getProperty("rif.logon.password") != null) {
			props.setProperty("password", System.getProperty("rif.logon.password"));
		}
		// JDBC deriver log level - 0: None, 1: Not much, 2: Loads. Default NONE	
		if (System.getProperty("rif.logon.loglevel") != null) {
			props.setProperty("loglevel", System.getProperty("rif.logon.loglevel"));
		}
		// Needs to be a service ticket; can be modified is the user is so inclined
		if (System.getProperty("rif.logon.KerberosServerName") != null) {
			props.setProperty("KerberosServerName", System.getProperty("rif.logon.KerberosServerName"));
		}
		else {
			props.setProperty("KerberosServerName", "postgres");	
		}
		// Where to get GSSAPI settings
		if (System.getProperty("rif.logon.jaasApplicationName") != null) {
			props.setProperty("jaasApplicationName", System.getProperty("rif.logon.jaasApplicationName"));
		}
		else if (System.getProperty("java.security.auth.login.config") != null) {
			props.setProperty("jaasApplicationName","rif");		
		}
		// Sanity check
		if ((props.getProperty("jaasApplicationName") != null)&&	
		    (props.getProperty("KerberosServerName")  != null)&&
		    (System.getProperty("java.security.auth.login.config")  != null)) {
			System.out.println("Connecting to " + url + " using GSSAPI and JAAS");
		}
		else if ((props.getProperty("jaasApplicationName") == null)&&	
		    (props.getProperty("KerberosServerName")  == null)&&
		    (props.getProperty("password")  != null)&&
		    (System.getProperty("java.security.auth.login.config")  == null)) {
			System.out.println("Connecting to " + url + " using MD5");
		}
		else {
			System.out.println("Connection Failed! Cannot tell if JASS/MD5 connection required." + 
				" rif.logon.jaasApplicationName/password/KerberosServerName or java.security.auth.login.config not set");
			return;
		}

// Location of krb5.conf (assumed /etc/krb5.conf on Linux)
		if ((System.getProperty("rif.logon.windows.krb5.conf") != null)&&
		    (!os_name.equals("Linux"))) {
			String krb5_conf=System.getProperty("user.dir") + "\\" + System.getProperty("rif.logon.windows.krb5.conf");
			System.setProperty("java.security.krb5.conf", krb5_conf);
			System.out.println("Sett java.security.krb5.conf to: " + System.getProperty("java.security.krb5.conf"));
		}

// Auto generate JAAS login configuration file
/*
//
// Default JASS login configuration file - Linux systems
//
// login.config.url.1=file:${user.home}/.java.login.config
//
// Or via -Dcom.sun.security.auth.login.ConfigFile=<location>
//
// This is set in RIFProperties.txt by:
//
// login.configuration.provider=com.sun.security.auth.login.ConfigFile
//
rif {
	com.sun.security.auth.module.Krb5LoginModule required 
		debug=true 
		useKeyTab=true 
		keyTab="FILE:/etc/krb5.keytab" 
		useTicketCache=true 
		ticketCache="/tmp/krb5cc_31110_jNaiq11817";
};
//
// Eof

 Windows notes: /etc/krb5.conf is not present; Oracle uses: C:\krb5\krb5.conf 

 Java is expecting: C:\Documents and Settings\peterh\WINDOWS\krb5.ini
 */

		if ((props.getProperty("jaasApplicationName") != null)&&	
		    (props.getProperty("KerberosServerName")  != null)&&
		    (System.getProperty("java.security.auth.login.config")  != null)) {
			String jgss_debug=null;
			if (System.getProperty("sun.security.jgss.debug") != null) {
				jgss_debug=new String(System.getProperty("sun.security.jgss.debug"));
			}
			if ((jgss_debug != null)&&(jgss_debug.equals("true"))) {
				System.out.println("Creating JASS login configuration file for " + os_name +
					" (with debug): " + System.getProperty("java.security.auth.login.config") +
					"; Kerberos ticket cache: " + System.getenv("KRB5CCNAME"));
			}
			else {
				System.out.println("Creating JASS login configuration file for " + os_name + 
					": " + System.getProperty("java.security.auth.login.config") +
					"; Kerberos ticket cache: " + System.getenv("KRB5CCNAME"));
			}
			try {
        			jaasFile = new PrintWriter(System.getProperty("java.security.auth.login.config"));
				jaasFile.println("//");
				jaasFile.println("// Default JASS login configuration file - " + os_name + " [AUTOGENERATED - DO NOT EDIT]");
				jaasFile.println("//");
				jaasFile.println("// login.config.url.1=file:${user.home}/.java.login.config");
				jaasFile.println("//");
				jaasFile.println("// Or via -Dcom.sun.security.auth.login.ConfigFile=<location>");
				jaasFile.println("//");
				jaasFile.println("// This is set in RIFProperties.txt by:");
				jaasFile.println("//");
				jaasFile.println("// login.configuration.provider=com.sun.security.auth.login.ConfigFile");
				jaasFile.println("//");
				jaasFile.println("rif {");
				jaasFile.println("	com.sun.security.auth.module.Krb5LoginModule required");
				jaasFile.println("		refreshKrb5Config=true");
				if ((jgss_debug != null)&&(jgss_debug.equals("true"))) {
					jaasFile.println("		debug=true");
				}
				if (os_name.equals("Linux")) {
					jaasFile.println("		useKeyTab=true");
					jaasFile.println("		keyTab=\"FILE:/etc/krb5.keytab\"");
					jaasFile.println("		useTicketCache=true");
					jaasFile.println("		ticketCache=\"" + System.getenv("KRB5CCNAME") + "\";");
				}
				else { // Assume Windows
// krb5.ini location?
// Oracle keytab location
					jaasFile.println("		useKeyTab=false");
					jaasFile.println("		useTicketCache=true;");
				}
				jaasFile.println("};");
				jaasFile.println("//");
				jaasFile.println("// Eof");
				jaasFile.close();

			} catch (FileNotFoundException e) {

				System.out.println("Cannot open: " + System.getProperty("java.security.auth.login.config"));
				e.printStackTrace();
				return;
			}
		}

/*
 * SSL
 *
 * Convert existing server certificate to DER format; import to RIF keystore
 *
 * Linux: in $PGDATA directory
 *
 * openssl x509 -in server.crt -out server.crt.der -outform der
 *
 * /usr/java/jre1.6.0_17/bin/keytool -import -keystore /usr/java/jre1.6.0_17/lib/security/rif -alias postgresql -file server.crt.der -storepass rifrif
 * 
 * or:
 *
 * "c:\Program Files\Java\jre6\bin\keytool" -import -keystore c:\Program Files\Java\jre6\lib\security\rif -alias postgresql -file server.crt.der -storepass rifrif
 *
 * YOU MUST USE THE JAVA KEYTOOL 
 *
 * You can test the CA using:
 *
 * $JAVA_HOME/bin/keytool -list -keystore $JAVA_HOME/lib/security/rif -alias postgresql
 * /usr/java/jre1.6.0_17/bin/keytool -list -keystore /usr/java/jre1.6.0_17/lib/security/rif -alias postgresql -storepass rifrif
 *
 * Fix permissions: chgrp G_SAHSU /usr/java/jre1.6.0_17/lib/security/rif
 *
 * javax.net.ssl.trustStore must be set to /usr/java/jre1.6.0_17/lib/security/rif
 * javax.net.ssl.trustStorePassword must be set is a password was set by keytool
 */
		if (System.getProperty("rif.logon.ssl") != null) {
			props.setProperty("ssl", System.getProperty("rif.logon.ssl"));
		}
// Disable certificate verifcation (DO NOT DO THIS)
		if (System.getProperty("rif.logon.sslfactory") != null) {
			props.setProperty("sslfactory", System.getProperty("rif.logon.sslfactory"));
		}
	
//
// Expecting tablr name to dump
//
		if (argv.length == 0) {
			System.out.println("Usage: java program <table or view name>");
			System.exit(1);
		}
//
// Do conenction
//
		String str=null;
		String current_database=null;
		long starttime=0;
		long endtime=0;
		try {
			starttime=System.nanoTime();
			connection = DriverManager.getConnection(url, props);
// Run rif40_startup()
			Statement stmt=null;
			ResultSet rs=null;
			SQLWarning warn=null;
			int i=0;

			stmt=connection.createStatement();
			rs=stmt.executeQuery("SELECT rif40_startup() AS rif40_startup, ssl_version() AS ssl_version, current_database() AS current_database");
			warn=stmt.getWarnings();
// Extract warnings (INFO/NOTICE/WARNINGS)
			while (warn != null) {
				System.out.println(warn.getMessage());
				warn=warn.getNextWarning();
			}
// Extract ssl_version() from DB. Should only be one row [THIS SHOULD BE CHECKED]
			while (rs.next()) {
				i++;
				str=rs.getString("ssl_version");
				current_database=rs.getString("current_database");
			}
		} catch (SQLException e) {
 
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
 
		}
 
		if (connection != null) {
			if (str.length() > 0) {
				System.out.println("Connected securely to " + current_database + " using " + str);
			}
			else {
				System.out.println("Connected insecurely to " + current_database);
			}
		} else {
			System.out.println("Failed to make connection!");
			System.exit(1);
		}

		try {
// Run rif40_startup()
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
				j+=str.split("\r\n").length;
				bf.write(str);
				bf.newLine();
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
	

