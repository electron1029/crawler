package webcrawler;

import java.util.ArrayList;

import javagui.resources.Main;

/**
 * Class that calculates fetching progress based on
 * Nutch output information.
 */
public class AnalyzeFetchingStatus extends Thread 
{
	// reference to the webcrawler to update data
	private WebCrawler w;

	// variables to hold calculations
	private volatile int pagesCrawled;
	private volatile int crawlerErrors;
	private volatile float crawlSpeedPPM;
	private volatile int remainingPagesToCrawl;
	private volatile int totalPages;

	// for help determining progress between updates
	private int lastUpdatePagesCrawled;
	private int lastUpdateErrors;

	/** 
	 * Constructor
	 * @param w
	 */
	public AnalyzeFetchingStatus(WebCrawler w) 
	{
		this.w = w;
	}

	/**
	 * This section contains sets and gets.
	 * Function is self-explanatory.
	 * If anything is not self-explanatory there is a comment in the
	 * method elaborating
	 */
	public synchronized int getPagesCrawled() 
	{
		return pagesCrawled;
	}

	public synchronized int getCrawlerErrors() 
	{
		return crawlerErrors;
	}

	public synchronized int getRemainingPagesToCrawl() 
	{
		return remainingPagesToCrawl;
	}

	public synchronized int getTotalPages() 
	{
		return totalPages;
	}

	/**
	 * Called when the thread starts
	 */
	public void run() 
	{
		// variables
		int i = 0, j = 0;
		int pagesCrawledSinceUpdate = 0;
		int errorsSinceUpdate = 0;

		// continuously loop here
		// this is where all nutch data parsing and calculations are done
		while (true) 
		{
			try 
			{
				// look in the nutch output data for the line swe are interested in.
				
				// this line contains all data we need for the calculations required.
				String spinWaitingOutput = ((i = Main.b.toString().indexOf(
						"spinwaiting/active")) != -1) ? Main.b.toString()
						.substring(i) : "";
					
				// this line contains the url being fetched		
				String fetchingOutput = ((i = Main.b.toString().indexOf(
								"fetching")) != -1) ? Main.b.toString()
								.substring(i) : "";
								
				// this line contains the url with an error
				String fetchingErrorOutput = ((i = Main.b.toString().indexOf(
						"failed with")) != -1) ? Main.b.toString()
						.substring(i) : "";
	
				// reset the output
				Main.b.reset();		
						
				// if we found the data then parse it and make calculations
				if (!spinWaitingOutput.equals("")) 
				{
					// split the string we found
					String[] tokens = spinWaitingOutput.split(",*\\s+");

					// calculations from parsed data
					pagesCrawled = Integer.parseInt(tokens[1]);
					pagesCrawledSinceUpdate = pagesCrawled - lastUpdatePagesCrawled;
					crawlerErrors = Integer.parseInt(tokens[3]);
					errorsSinceUpdate = crawlerErrors - lastUpdateErrors;
					crawlSpeedPPM = Float.parseFloat(tokens[5]) * 60;
				
					// set the calculated data for access in the actual webcrawler thread
					w.setCrawlSpeedPPM(crawlSpeedPPM);
					w.incrementTotalPagesCrawled(pagesCrawledSinceUpdate - errorsSinceUpdate);
					
					// update the status variables for next time
					lastUpdatePagesCrawled = pagesCrawled;
					lastUpdateErrors = crawlerErrors;
				}
				
				// if we see a url being fetched then save the data
				if (!fetchingOutput.equals(""))
				{
					String[] tokens = fetchingOutput.split(",*\\s+");
					
					w.setCrawledUrls(tokens[1]);
				}
				
				// if we see a fetching error save the data
				if (!fetchingErrorOutput.equals("")) 
				{
					String[] tokens = fetchingOutput.split(",*\\s+");
					
					w.setUnsuccessfulUrls(tokens[2]);
				}

				// sleep for a bit to help efficiency
				Thread.sleep(5);
			} catch (InterruptedException e) 
			{
				// if we are interrupted we just return
				return;
			} catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
}