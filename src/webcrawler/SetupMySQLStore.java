package webcrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Class to check if MySql is setup and fix it if it's not
 */
public class SetupMySQLStore 
{
	/**
	 * Constructor
	 * @param mysqlUsername
	 * @param mysqlPassword
	 * @param mysqlServer
	 */
	public SetupMySQLStore(String mysqlUsername, String mysqlPassword, String mysqlServer) 
	{
		try 
		{
			// check if the webpage table exists, if not create it
			checkSQLTableExists(mysqlUsername, mysqlPassword, mysqlServer);
			
			// fix the gora properties file for proper database storage
			generateGoraProperties(mysqlUsername, mysqlPassword, mysqlServer + "/nutch");
			
			// fix gora sql mapping file for proper database storage
			generateGoraSQLMapping();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * Function that checks if the webpage table exists and creates it if it does not
	 * @param mysqlUsername
	 * @param mysqlPassword
	 * @param mysqlServer
	 */
	private void checkSQLTableExists(String mysqlUsername, String mysqlPassword, String mysqlServer) 
	{
		Connection mySQLConn = null;

		try 
		{
			// try to connect to the database
			mySQLConn = DriverManager.getConnection("jdbc:" + mysqlServer + "?"
					+ "user=" + mysqlUsername + "&password=" + mysqlPassword);

			// create the nutch database if it does not exist
			PreparedStatement p1 = mySQLConn
					.prepareStatement("CREATE DATABASE IF NOT EXISTS nutch DEFAULT CHARACTER "
							+ "SET utf8 DEFAULT COLLATE utf8_general_ci;");
			p1.executeUpdate();

			// close and reopen the connection
			mySQLConn.close();

			mySQLConn = DriverManager.getConnection("jdbc:" + mysqlServer
					+ "/nutch" + "?" + "user=" + mysqlUsername + "&password="
					+ mysqlPassword);

			// create the nutch.webpage table if it does not exist
			PreparedStatement p2 = mySQLConn
					.prepareStatement("CREATE TABLE IF NOT EXISTS webpage ("
							+ "id varchar(255) NOT NULL,"
							+ "headers blob,"
							+ "text longtext DEFAULT NULL,"
							+ "status int(11) DEFAULT NULL,"
							+ "markers blob,"
							+ "parseStatus blob,"
							+ "modifiedTime bigint(20) DEFAULT NULL,"
							+ "prevModifiedTime bigint(20) DEFAULT NULL,"
							+ "score float DEFAULT NULL,"
							+ "typ varchar(32) CHARACTER SET latin1 DEFAULT NULL,"
							+ "batchId varchar(32) CHARACTER SET latin1 DEFAULT NULL,"
							+ "baseUrl varchar(767) DEFAULT NULL,"
							+ "content longblob,"
							+ "title varchar(2048) DEFAULT NULL,"
							+ "reprUrl varchar(767) DEFAULT NULL,"
							+ "fetchInterval int(11) DEFAULT NULL,"
							+ "prevFetchTime bigint(20) DEFAULT NULL,"
							+ "inlinks mediumblob," + "prevSignature blob,"
							+ "outlinks mediumblob,"
							+ "fetchTime bigint(20) DEFAULT NULL,"
							+ "retriesSinceFetch int(11) DEFAULT NULL,"
							+ "protocolStatus blob," + "signature blob,"
							+ "metadata blob," + "PRIMARY KEY (id)"
							+ ") ENGINE=InnoDB " + "ROW_FORMAT=COMPRESSED "
							+ "DEFAULT CHARSET=utf8;");
			p2.executeUpdate();

		} catch (SQLException e) 
		{
			// if there is an error quit and print a message with possible reasons
			System.err.println(("Error finding and/or creating MySQL database.\r\n"
							+ "Troubleshooting Tips:\r\n"
							+ "1. Check your username and password.\r\n"
							+ "2. Please ensure the Mysql server location provided is correct. You provided: \r\n\t"
							+ mysqlServer + "\r\n"
							+ "3. Please ensure MySql is setup to use InnoDB.\r\n"
							+ "4. If the Mysql account you are logging in with does not have creation priviledges, the " 
							+ "nutch.webpage database will need to be created manually."));
		}
	}

	/**
	 * Function to generate the gora properties file in the nutch configuration
	 * folder to use the mysql settings
	 * @param mysqlUsername
	 * @param mysqlPassword
	 * @param mysqlServer
	 * @throws IOException
	 */
	private void generateGoraProperties(String mysqlUsername,
			String mysqlPassword, String mysqlServer) throws IOException 
	{
		// create the file in the correct location
		FileOutputStream os = new FileOutputStream(new File("nutchconf/gora.properties"), false);

		// set the text to write in the file
		String propertiesText = "################################\n"
				+ "# MySQL Properties\n" + "################################\n"
				+ "gora.sqlstore.jdbc.driver=com.mysql.jdbc.Driver\n"
				+ "gora.sqlstore.jdbc.url=jdbc:" + mysqlServer + "\n"
				+ "gora.sqlstore.jdbc.user=" + mysqlUsername + "\n"
				+ "gora.sqlstore.jdbc.password=" + mysqlPassword + "\n";

		// convert the text to write to bytes for proper writing
		byte[] propertiesTextBytes = propertiesText.getBytes("UTF-8");

		// write the file and close
		os.write(propertiesTextBytes);
		os.close();
	}

	/**
	 * Function to create the correct gora-sql-mapping.xml file in the
	 * nutch configuration folder.
	 * @throws IOException
	 */
	private void generateGoraSQLMapping() throws IOException 
	{
		// create the file in the proper location
		FileOutputStream os = new FileOutputStream(new File(
				"nutchconf/gora-sql-mapping.xml"), false);

		// the text to write to the file
		String goraSqlMappingXMLText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<gora-orm>\n"
				+ "<class name=\"org.apache.nutch.storage.WebPage\" keyClass=\"java.lang.String\" table=\"webpage\">\n"
				+ "  <primarykey column=\"id\" length=\"255\"/>\n"
				+ "    <field name=\"baseUrl\" column=\"baseUrl\" length=\"512\"/>\n"
				+ "    <field name=\"status\" column=\"status\"/>\n"
				+ "    <field name=\"prevFetchTime\" column=\"prevFetchTime\"/>\n"
				+ "    <field name=\"fetchTime\" column=\"fetchTime\"/>\n"
				+ "    <field name=\"fetchInterval\" column=\"fetchInterval\"/>\n"
				+ "    <field name=\"retriesSinceFetch\" column=\"retriesSinceFetch\"/>\n"
				+ "    <field name=\"reprUrl\" column=\"reprUrl\" length=\"512\"/>\n"
				+ "    <field name=\"content\" column=\"content\" length=\"65536\"/>\n"
				+ "    <field name=\"contentType\" column=\"typ\" length=\"32\"/>\n"
				+ "    <field name=\"protocolStatus\" column=\"protocolStatus\"/>\n"
				+ "    <field name=\"modifiedTime\" column=\"modifiedTime\"/>\n"
				+ "    <field name=\"prevModifiedTime\" column=\"prevModifiedTime\"/>\n"
				+ "    <field name=\"batchId\" column=\"batchId\" length=\"32\"/>\n\n"
				+ "    <!-- parse fields                     -->\n"
				+ "    <field name=\"title\" column=\"title\" length=\"512\"/>\n"
				+ "    <field name=\"text\" column=\"text\" length=\"32000\"/>\n"
				+ "    <field name=\"parseStatus\" column=\"parseStatus\"/>\n"
				+ "    <field name=\"signature\" column=\"signature\"/>\n"
				+ "    <field name=\"prevSignature\" column=\"prevSignature\"/>\n\n"
				+ "    <!-- score fields                     -->\n"
				+ "    <field name=\"score\" column=\"score\"/>\n"
				+ "    <field name=\"headers\" column=\"headers\"/>\n"
				+ "    <field name=\"inlinks\" column=\"inlinks\"/>\n"
				+ "    <field name=\"outlinks\" column=\"outlinks\"/>\n"
				+ "    <field name=\"metadata\" column=\"metadata\"/>\n"
				+ "    <field name=\"markers\" column=\"markers\"/>\n"
				+ "</class>\n\n"
				+ "<class name=\"org.apache.nutch.storage.Host\" keyClass=\"java.lang.String\"\n"
				+ "table=\"host\">\n"
				+ "  <primarykey column=\"id\" length=\"255\"/>\n"
				+ "  <field name=\"metadata\" column=\"metadata\"/>\n"
				+ "  <field name=\"inlinks\" column=\"inlinks\"/>\n"
				+ "  <field name=\"outlinks\" column=\"outlinks\"/>\n"
				+ "</class>\n" + "</gora-orm>";

		// convert the text to bytes for writing
		byte[] goraSqlMappingXMLTextBytes = goraSqlMappingXMLText.getBytes("UTF-8");

		// actually write the data and close the file
		os.write(goraSqlMappingXMLTextBytes);
		os.close();
	}
}
