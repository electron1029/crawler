package webcrawler;

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
		int i = 0;
		int pagesCrawledSinceUpdate = 0;
		int errorsSinceUpdate = 0;

		// continuously loop here
		// this is where all nutch data parsing and calculations are done
		while (true) 
		{
			try 
			{
				// look in the nutch output data for the line we are interested in.
				// this line contains all data we need for the calculations required.
				String output = ((i = Main.b.toString().indexOf(
						"spinwaiting/active")) != -1) ? Main.b.toString()
						.substring(i) : "";

				// if we found the data then parse it and make calculations
				if (!output.equals("")) 
				{
					// split the string we found
					String[] tokens = output.split(",*\\s+");

					// calculations from parsed data
					pagesCrawled = Integer.parseInt(tokens[1]);
					pagesCrawledSinceUpdate = pagesCrawled - lastUpdatePagesCrawled;
					crawlerErrors = Integer.parseInt(tokens[3]);
					errorsSinceUpdate = crawlerErrors - lastUpdateErrors;
					crawlSpeedPPM = Float.parseFloat(tokens[5]) * 60;
				
					// set the calculated data for access in the actual webcrawler thread
					w.setCrawlSpeedPPM(crawlSpeedPPM);
					w.incrementTotalPagesCrawled(pagesCrawledSinceUpdate - errorsSinceUpdate);
				
					// output to the console the progress and crawl speed
					// we have to output using err since we are using System.out to 
					// hold nutch logging output for parsing
					System.err.print("Progress: " + w.getPercentageCrawled());
					System.err.print(" Speed: " + w.getCrawlSpeedPPM() + " pages/min");
					System.err.println(" Elapsed time: " + w.getElapsedTime());
					
					// update the status variables for next time
					lastUpdatePagesCrawled = pagesCrawled;
					lastUpdateErrors = crawlerErrors;
				}

				// reset the output thread and sleep for a bit to help efficiency
				Main.b.reset();
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