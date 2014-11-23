import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Temporary driver class to use while there is no real UI.
 * @author seed
 *
 */
public class MyCrawler 
{
	// an output stream we will use to redirect some nutch stats for analysis
	public static ByteArrayOutputStream b;

	/**
	 * Main method.
	 * Creates a makeshift console text-based UI.
	 * @param args
	 */
	public static void main(String[] args)
	{
		// get a Scanner for user input
		Scanner input = new Scanner(System.in);

		// main menu printed on program start
		// asks for a menu option and then gets the user input
		System.out.println(
				"*******************************************************************************\r\n"
				+ "\tEnter 1 to do a web crawl.\r\n"
				+ "\tEnter 2 to lookup repository statistics.\r\n"
				+ "\tEnter 3 to lookup the status of a specific url.\r\n"
				+ "\tEnter 4 to exit.\r\n"
				+ "*******************************************************************************");
		int choice = input.nextInt();
		input.nextLine();

		// go to the correct submenu based on user input
		switch(choice)
		{
		case 1:	// web crawl
			{
				// system message to provide guidance for what the user should provide for a crawl
				System.out.println(
						"Please enter crawl parameters in the form:\r\n"
						+ "\tMYSQL username, MYSQL password, MYSQL servernlocation, urls in form\r\n"
						+ "\thttp://example1.com,http://example2.com, threadsperhost, totalthreads,\r\n"
						+ "\tthreadsperqueue, approxMaxPagesToCrawlPerHost\r\n"
						+ "Enter -1 to use a default value in any field. Be warned, default values\r\n"
						+ "may not work for every system setup. See README for more info.");
	
				// parse the input
				String inputText = input.nextLine();
				String[] parameters = inputText.split(" ");
	
				try 
				{
					// throw an error if the user did not provide enough arguments
					if (parameters.length != 8) 
					{
						throw new Exception();
					}
	
				} catch (Exception e) 
				{
					System.out.println("Error: Invalid argument number.\n"
							+ "Usage: mysqlUsername mysqlPassword mysqlServerAddress urls threadsperhost "
							+ "totalthreads threadsperqueue approxMaxPagesToCrawlPerHost\n\n"
							+ "Enter -1 in any field to use default values."
							+ "Exiting...");
					System.exit(0);
				}
	
				// redirects System.out to a byte array stream
				// this causes Nutches informative output that we have configured to provide the data
				// we need to display to go to this stream. This is how we can get the information
				// since the actually methods in the Nutch API to directly calculate the data are
				// private.
				b = new ByteArrayOutputStream();
				PrintStream p = new PrintStream(b);
				System.setOut(p);
		
				try 
				{
					// attempt to actually perform a crawl based on user parameters
					WebCrawler crawler;
					crawler = new WebCrawler(parameters[0], parameters[1], parameters[2], parameters[3],
							parameters[4], parameters[5], parameters[6], parameters[7]);
					crawler.crawl();
					
				} catch (Exception e) 
				{
					// if something goes wrong print the error and exit
					System.out.println("Error performing crawl.\r\n"
							+ "Exiting...\r\n\r\n");
					e.printStackTrace();
					System.exit(0);
				}	
	
				break;
			}
		case 2:	// display repository stats
			{
				// guidance for what input user should provide to use this feature
				System.out.println(
						"Please enter your MySQL server parameters in the form:\r\n"
						+ "\tMYSQL username, MYSQL password, MYSQL server location");
				
				// parse the user input
				String inputText = input.nextLine();
				String[] parameters = inputText.split(" ");
				
				try 
				{
					// throw an error if the user did not enter enough parameters
					if (parameters.length != 3) 
					{
						throw new Exception();
					}
	
				} catch (Exception e) 
				{
					System.out.println("Error: Invalid argument number.\r\n"
							+ "Usage: mysqlUsername mysqlPassword mysqlServerAddress\r\n"
							+ "Enter -1 in any field to use default values.\r\n"
							+ "Exiting...");
					System.exit(0);
				}
				
				// attempt to connect to the database
				SQLQueries sq = new SQLQueries(parameters[0], parameters[1], parameters[2]);
				
				// outputs some random stats for the database
				System.out.println("*******************************************************************************\r\n"
						+ "\tPrinting stats for database \"nutch.webpage\":\r\n\r\n");
				System.out.println("Total pages crawled: " + sq.getNumCrawledUrls());
				System.out.println("\r\n*******************************************************************************");
				
				break;
			}
		case 3:	// lookup a specific url in the database
			{
				// provide guidance for what user should provide to use this function
				System.out.println(
						"Please enter your MySQL server parameters in the form:\r\n"
						+ "\tMYSQL username, MYSQL password, MYSQL server location, url");
				
				// parse user input
				String inputText = input.nextLine();
				String[] parameters = inputText.split(" ");
				
				try 
				{
					// throw an error if the user did not provide enough parameters
					if (parameters.length != 4) 
					{
						throw new Exception();
					}
	
				} catch (Exception e) 
				{
					System.out.println("Error: Invalid argument number.\r\n"
							+ "Usage: mysqlUsername mysqlPassword mysqlServerAddress url\r\n"
							+ "Enter -1 in any field to use default values.\r\n"
							+ "Exiting...");
					System.exit(0);
				}
				
				// attempt to connect to the database
				SQLQueries sq = new SQLQueries(parameters[0], parameters[1], parameters[2]);
				
				// print out random stats for a URL or a message if it's not in the databse
				System.out.println("*******************************************************************************\r\n"
						+ "\tPrinting stats for " + parameters[3] + ":\r\n\r\n");
				System.out.println("Results: " + sq.getUrlStatus(parameters[3]));
				System.out.println("\r\n*******************************************************************************");
				
				break;
			}
		case 4:	// user just wants to exit
			{
				// simply exit
				System.out.println("Exiting...\r\n");
				System.exit(0);
			}
		default:	// if user gives the wrong input
			{
				// tell user the error and just exit
				System.out.println("Invalid choice.\r\n"
						+ "Exiting...");
				System.exit(0);
			}
		}
	}
}
