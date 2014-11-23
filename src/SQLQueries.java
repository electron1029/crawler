import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class that handles execution of sql queries.
 */
public class SQLQueries 
{
	// variables necessary
	// default values are provided but may not work for every set up
	// user can choose them at their own risk.
	private Connection mySQLConn;
	private String mysqlUsername = "root";
	private String mysqlPassword = "";
	private String mysqlServer = "mysql://localhost:3306";

	/**
	 * Constructor without arguments
	 */
	public SQLQueries()
	{
		
	}
	
	/**
	 * Constructor with arguments. 
	 * @param mysqlUsername
	 * @param mysqlPassword
	 * @param mysqlServer
	 */
	public SQLQueries(String mysqlUsername, String mysqlPassword, String mysqlServer) 
	{
		// check if user wanted to use any default values.
		// if they did not assign the value to the necessary variable
		
		if (!mysqlUsername.equals("-1"))
		{
			setMysqlUsername(mysqlUsername);
		}
		if (!mysqlPassword.equals("-1"))
		{
			setMysqlPassword(mysqlPassword);
		}
		if (!mysqlServer.equals("-1"))
		{
			setMysqlServer(mysqlServer);
		}

		// attempt to connect using the provided credentials
		connect(this.mysqlUsername, this.mysqlPassword, this.mysqlServer);
	}

	/**
	 * Set and get methods are in this section.
	 * Methods are self-explanatory.
	 * If anything is not self-explanatory an additional comment is 
	 * provided in the method.
	 */
	
	public void setMysqlUsername(String mysqlUsername)
	{
		this.mysqlUsername = mysqlUsername;
	}

	public void setMysqlPassword(String mysqlPassword)
	{
		this.mysqlPassword = mysqlPassword;
	}

	public void setMysqlServer(String mysqlServer)
	{
		this.mysqlServer = mysqlServer;
	}

	/**
	 * Function to connect after setting the variables manually
	 */
	public void connect()
	{
		connect(this.mysqlUsername, this.mysqlPassword, this.mysqlServer);
	}
	
	/**
	 * Function that attempts to connect to the mysql database with the given
	 * credentials.
	 * @param mysqlUsername
	 * @param mysqlPassword
	 * @param mysqlServer
	 */
	public void connect(String mysqlUsername, String mysqlPassword, String mysqlServer) 
	{
		try 
		{
			// try to create the connection
			// if it fails throw an exception and give potential error information
			mySQLConn = DriverManager.getConnection("jdbc:" + mysqlServer + "?"
					+ "user=" + mysqlUsername + "&password=" + mysqlPassword);
		} catch (SQLException e) 
		{
			System.err.println(("Error finding and/or creating MySQL database.\r\n"
					+ "Troubleshooting Tips:\r\n"
					+ "1. Check your username and password.\r\n"
					+ "2. Please ensure the Mysql server location provided is correct. You provided: \r\n\t"
					+ mysqlServer + "\r\n"
					+ "3. Please ensure MySql is setup to use InnoDB.\r\n"
					+ "4. If the Mysql account you are logging in with does not have creation priviledges, the " 
					+ "nutch.webpage database will need to be created manually.\r\n\r\n"
					+ "Exiting..."));
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Function that queries for the status of a specific url.
	 * @param url
	 * @return
	 */
	public String getUrlStatus(String url) 
	{
		String results = "";

		try 
		{
			// try to look up the fetchTime, protocolstatus, and baseUrl of the desired Url
			PreparedStatement p = mySQLConn.prepareStatement("SELECT baseUrl, fetchTime, "
					+ "protocolStatus FROM nutch.webpage WHERE baseUrl=\""+ url + "\";");
			ResultSet r = p.executeQuery();

			// iterate over the results to parse out the actual data
			while (r.next()) 
			{
				String baseUrl = r.getString("baseUrl");
				long fetchTime = r.getLong("fetchTime");
				Blob blob = r.getBlob("protocolStatus");
				
				InputStream is = blob.getBinaryStream();
				BufferedReader b = new BufferedReader(new InputStreamReader(is));
				String blobText = "";
				String line;
				while((line = b.readLine()) != null)
				{
					blobText += line;
				}
				
				// put the data into a string and return it to the function caller
				results += baseUrl + " " + fetchTime + " " + blobText + "\n";
			}
			
			// if we did not get any results tell the user and give some potential
			// information as to why the query may have failed
			if (results.equals(""))
			{
				results = url + " not found in the database nutch.webpage.\r\n"
						+ "\tPlease check the url for errors. This could also mean this webpage\r\n"
						+ "has not yet been crawled.";
			}
		} catch (SQLException e) 
		{
			e.printStackTrace();
		} catch (IOException e) 
		{
			System.err.println("Error parsing database information.\n");
		}

		return results;
	}

	/**
	 * A demonstration of getting a repository stat.
	 * This just looks up total urls fetched.
	 * @return
	 */
	public String getNumCrawledUrls() 
	{
		String results = "";

		try 
		{
			// attempt to look up the total urls in the database
			PreparedStatement p1 = mySQLConn.prepareStatement("SELECT COUNT(*) AS total FROM nutch.webpage;");
			ResultSet r = p1.executeQuery();
			
			// parse the results and return it to the function caller
			if (r.next())
			{
				results += r.getInt("total");
			}
		} catch (SQLException e) 
		{
			e.printStackTrace();
		}

		return results;
	}

	/**
	 * Attempt to look up the urls crawled for a certain domain
	 * @param domain
	 * @return
	 */
	public String getNumCrawledUrlsForADomain(String domain) 
	{
		String results = "";

		try 
		{
			// try to execute the query
			PreparedStatement p1 = mySQLConn
					.prepareStatement("SELECT COUNT(*) AS total FROM"
							+ "(SELECT * FROM nutch.webpage WHERE id LIKE '" + domain
							+ "%');");
			ResultSet r = p1.executeQuery();
			
			// return the results to the method caller
			results += r.getInt("total");
		} catch (SQLException e) 
		{
			e.printStackTrace();
		}

		return results;
	}

	/**
	 * Function to do some database cleanup by deleting all rows that contain
	 * pages that have not actually been crawled.
	 */
	public void deleteUnparsedRows() 
	{
		try 
		{
			// attempt to delete the rows.
			PreparedStatement p1 = mySQLConn
					.prepareStatement("DELETE FROM nutch.webpage WHERE status!=2;");
			p1.executeUpdate();
		} catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
}
