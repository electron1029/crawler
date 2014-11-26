package webcrawler;

import java.util.Random;
import org.apache.gora.store.DataStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.DbUpdaterJob;
import org.apache.nutch.crawl.GeneratorJob;
import org.apache.nutch.crawl.InjectorJob;
import org.apache.nutch.fetcher.FetcherJob;
import org.apache.nutch.parse.ParserJob;
import org.apache.nutch.storage.StorageUtils;
import org.apache.nutch.storage.WebPage;

/**
 * Main class of the Web Crawler. Coordinates all functionality and
 * performs the bulk of the function calls to the Nutch API.
 */
public class WebCrawler extends Thread
{
	// variables needed for setting up the configuration used by Nutch jobs
	private Configuration config;
	private FileSystem fs;
	private DataStore<String, WebPage> webPageStore;

	/******************************************************************
	 * Important crawl values. Default values may not work for all users
	 * but are provided to make testing easier.
	 ******************************************************************/
	public static String batchID;
	private String mysqlUsername = "root";
	private String mysqlPassword = "";
	private String mysqlServerLocation = "mysql://localhost:3306";
	private String urls = "http://www.mayoclinic.org,http://www.webmd.com";
	private int numUrls = 0;
	private int threadsPerHost = 1;
	private int threadsPerFetch = 2;
	private int threadsPerQueue = 1;
	private int maxPagesToCrawl = 2000;
	private long startingTime;
	private String elapsedTime;

	// AnalyzeFetchingStatus will figure out data about the status
	// of the crawl job, calculate any necessary values, and feed them
	// to the variables listed here. It will run in a separate thread
	// since it needs to constantly work while other tasks are running.
	private AnalyzeFetchingStatus thread;
	private volatile int totalPagesCrawled = 0;
	private float percentageCrawled;
	private float crawlSpeedPPM;
	
	// The main handler for all sqlQueries
	private SQLQueries sqlQueries;

	/**
	 * Constructor
	 * @param mysqlUsername
	 * @param mysqlPassword
	 * @param mysqlServerLocation
	 * @param urls
	 * @param threadsPerHost
	 * @param threadsPerFetch
	 * @param threadsPerQueue
	 * @param maxPagesToCrawl
	 * @throws Exception
	 */
	public WebCrawler(String mysqlUsername, String mysqlPassword,
			String mysqlServerLocation, String urls, String threadsPerHost,
			String threadsPerFetch, String threadsPerQueue,
			String maxPagesToCrawl) throws Exception 
	{
		// check all user input to see if they used a default value.
		// if they didn't assign what they entered to the variable.
		
		if (!mysqlUsername.equals("-1")) 
		{
			setMysqlUsername(mysqlUsername);
		}

		if (!mysqlPassword.equals("-1")) 
		{
			setMysqlPassword(mysqlPassword);
		}

		if (!mysqlServerLocation.equals("-1")) 
		{
			setMysqlServerLocation(mysqlServerLocation);
		}

		if (urls.equals("-1")) 
		{
			numUrls = setUrls(this.urls);
		} else 
		{
			numUrls = setUrls(urls);
		}

		if (!threadsPerHost.equals("-1")) 
		{
			setThreadsPerHost(threadsPerHost);
		}

		if (!threadsPerFetch.equals("-1")) 
		{
			setThreadsPerFetch(threadsPerFetch);
		}

		if (!threadsPerQueue.equals("-1")) 
		{
			setThreadsPerQueue(threadsPerQueue);
		}

		if (!maxPagesToCrawl.equals("-1")) 
		{
			setMaxPagesToCrawl(maxPagesToCrawl);
		}

		// generate a unique ID for this crawl. This will be a stamp in the database.
		// Used by various Nutch jobs to determine groupings etc.
		setBatchID();

		try 
		{
			// set up the values that Nutch needs
			config = setUp();
			fs = FileSystem.get(config);
			webPageStore = StorageUtils.createWebStore(config, String.class,
					WebPage.class);
			
			// connect to the database for any queries.
			sqlQueries = new SQLQueries(mysqlUsername, mysqlPassword, mysqlServerLocation);
		} catch (Exception e) 
		{
			return;
		}
	}

	/**
	 * Sets and gets are in this second section.
	 * Function is self-explanatory.
	 * If it is not a furthur description will be commented
	 * inside the specific method.
	 */
	
	public void setMysqlUsername(String mysqlUsername) 
	{
		this.mysqlUsername = mysqlUsername;
	}

	public String getMysqlUsername() 
	{
		return mysqlUsername;
	}

	public void setMysqlPassword(String mysqlPassword) 
	{
		this.mysqlPassword = mysqlPassword;
	}

	public String getMysqlPassword() 
	{
		return mysqlPassword;
	}

	public void setMysqlServerLocation(String mysqlServerLocation) 
	{
		this.mysqlServerLocation = mysqlServerLocation;
	}

	public String getMysqlServerLocation() 
	{
		return mysqlServerLocation;
	}

	public int setUrls(String urls) throws Exception 
	{
		// the urls come in a raw format and need to be parsed
		// and stored to the seed file that Nutch expects.
		CreateSeedTxt c = new CreateSeedTxt(urls);
		return c.getNumUrls();
	}

	public void setThreadsPerHost(String threadsPerHost) 
	{
		this.threadsPerHost = Integer.parseInt(threadsPerHost);
	}

	public int getThreadsPerHost() 
	{
		return threadsPerHost;
	}

	public void setThreadsPerFetch(String threadsPerFetch) 
	{
		this.threadsPerFetch = Integer.parseInt(threadsPerFetch);
	}

	public int getThreadsPerFetch() 
	{
		return threadsPerFetch;
	}

	public void setThreadsPerQueue(String threadsPerQueue) 
	{
		this.threadsPerQueue = Integer.parseInt(threadsPerQueue);
	}

	public int getThreadsPerQueue() 
	{
		return threadsPerQueue;
	}

	public void setMaxPagesToCrawl(String maxPagesToCrawl) 
	{
		this.maxPagesToCrawl = Integer.parseInt(maxPagesToCrawl);
	}

	public int getMaxPagesToCrawl() 
	{
		return maxPagesToCrawl;
	}

	public synchronized void incrementTotalPagesCrawled(int n) 
	{
		// when we update the current number of pages crawled we also need
		// to call the function to update the percentage of the job progress
		totalPagesCrawled += n;
		setPercentageCrawled();
	}

	public int getTotalPagesCrawled() 
	{
		return totalPagesCrawled;
	}

	private synchronized void setPercentageCrawled() 
	{
		percentageCrawled = (float)totalPagesCrawled / (float)maxPagesToCrawl;
	}
	
	public synchronized float getPercentageCrawled()
	{
		return percentageCrawled;
	}
	
	public synchronized void setCrawlSpeedPPM(float crawlSpeedPPM)
	{
		this.crawlSpeedPPM = crawlSpeedPPM;
	}
	
	public synchronized float getCrawlSpeedPPM()
	{
		return crawlSpeedPPM;
	}
	
	public String getElapsedTime()
	{
		long hours;
		long minutes;
		long seconds;
		
		// calculate elapsed time in milliseconds
		long elapsedTime = System.currentTimeMillis() - startingTime;
		
		// convert milliseconds to hours, minutes, seconds
		hours = elapsedTime / (60*60*1000);
		elapsedTime = elapsedTime - hours*60*60*1000;
		minutes = elapsedTime / (60*1000);
		elapsedTime = elapsedTime - minutes*60*1000;
		seconds = elapsedTime / 1000;
		
		return hours + " hours, " + minutes + " minutes, " + seconds + " seconds";
	}

	/**
	 * Generate a random batch id for the job. Have to do this manually because
	 * for some reason the automatic generate function in the Nutch API causes
	 * Hadoop to throw a mapreduce error based on a generatorjob error.
	 * 
	 * Fix for this error and the recommended code to fix the error was adapted
	 * from http://blog.csdn.net/weijonathan/article/details/9197697 translated
	 * by Google Translate in the Chrome browser on 11/4/2014
	 */
	private String setBatchID() 
	{
		int randomSeed = Math.abs(new Random().nextInt());
		batchID = (System.currentTimeMillis() / 1000) + "-" + randomSeed;

		return batchID;
	}

	/**
	 * Function to set up main configurations.
	 * @return
	 */
	private Configuration setUp() 
	{
		// create the configuration
		Configuration conf = new Configuration();
		
		// make sure the database is created in MySql and the files in the Nutch
		// configuration folder are properly set
		new SetupMySQLStore(mysqlUsername, mysqlPassword, mysqlServerLocation);
		
		// update the Nutch-site.xml file in the nutch configuration folder based
		// on the crawl parameters the user gives
		new GenerateNutchSiteXML(threadsPerHost, threadsPerFetch, threadsPerQueue);
		
		// make sure the url regex filter file is set correctly
		new GenerateURLRegexTXT();
		
		// update the variable for the server location to point to the correct databbase
		// now that we know for sure the database exists
		setMysqlServerLocation(getMysqlServerLocation() + "/nutch");

		// set the batch ID directly so it is correctly propogated through
		// nutch jobs
		conf.set(GeneratorJob.BATCH_ID, batchID);
		conf.setBoolean(FetcherJob.PARSE_KEY, true);

		// add the setting files we created to the nutch configuration
		conf.addResource("nutch-default.xml");
		conf.addResource("nutch-site.xml");
		
		// we are done
		return conf;
	}

	public void run()
	{
		crawl();
	}
	
	/**
	 * Main crawl method
	 */
	public void crawl() 
	{
		try 
		{
			startingTime = System.currentTimeMillis();
			
			// first try to inject the seed urls
			Path urlPath = new Path("urls/seed.txt");
			InjectorJob i = new InjectorJob();
			i.setConf(config);
			i.inject(urlPath);

			// keep performing incremental crawls until we have tried to crawl the user specified number
			// of pages
			// the reason this is done incrementally is to avoid java heap space errors on very large 
			// crawls. the risk of heap space errors is reduced if the crawls are kept small
			while (totalPagesCrawled < maxPagesToCrawl) 
			{
				// how many pages to fetch this round. either the remainder to fetch or 500, whatever is smallest
				int topN = (((maxPagesToCrawl - totalPagesCrawled) <= 500) ? (maxPagesToCrawl - totalPagesCrawled) : 500);

				// generate the topN urls
				GeneratorJob g = new GeneratorJob();
				g.setConf(config);
				g.generate(topN, System.currentTimeMillis(), true, false);

				// now attempt to actually crawl the topN urls generated
				// this is where we use the AnalyzeFetchingStatus thread to 
				// figure out the progress of the crawl. We start this job
				// in a new thread and interrupt the thread when the crawl
				// is complete.
				FetcherJob f = new FetcherJob();
				f.setConf(config);
				thread = new AnalyzeFetchingStatus(this);
				thread.start();
				f.fetch(batchID, threadsPerFetch, true, -1);
				thread.interrupt();

				// parse the pages fetched for new urls
				ParserJob p = new ParserJob();
				p.setConf(config);
				p.parse(batchID, true, false);

				// update the database based on fetched pages and the new urls seed
				// during parsing
				DbUpdaterJob d = new DbUpdaterJob();
				d.setConf(config);
				d.run(new String[0]);
			}
			
			// delete all unparsed rows to make the database more manageable
			// this unfortunately creates an inefficient first round on the next crawl
			// however when the mysql database is too large there are more java heap errors
			// and performance is poor. This is likely because mysql was not a good choice of
			// database for our needs.
			sqlQueries.deleteUnparsedRows();
		} catch (Exception e) 
		{
			System.err.println("Error during crawl!");
		}
	}
}
