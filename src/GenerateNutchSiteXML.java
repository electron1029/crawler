import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Class to generate the nutch-site.xml file in the nutch
 * configuration folder based on user preferences.
 */
public class GenerateNutchSiteXML 
{

	/**
	 * Constructor
	 * @param threadsPerHost
	 * @param threadsPerFetch
	 * @param threadsPerQueue
	 */
	public GenerateNutchSiteXML(int threadsPerHost, int threadsPerFetch,
			int threadsPerQueue) 
	{
		try 
		{
			// generate file based on input
			generateNutchSite(threadsPerHost, threadsPerFetch, threadsPerQueue);
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * function to generate the new nutch-site.xml file and write it to disk.
	 * @param threadsPerHost
	 * @param threadsPerFetch
	 * @param threadsPerQueue
	 * @throws IOException
	 */
	private void generateNutchSite(int threadsPerHost, int threadsPerFetch,
			int threadsPerQueue) throws IOException 
	{
		// create the file in the correct location
		FileOutputStream os = new FileOutputStream(new File("nutchconf/nutch-site.xml"), false);

		/*
		 * For descriptions of all nutch-site.xml properties, please view
		 * nutch-default.xml
		 */
		// string containing text to write to file
		String xmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<configuration>\n" + "<property>\n"
				+ "<name>http.agent.name</name>\n"
				+ "<value>My Nutch Spider</value>\n" + "</property>\n"

				+ "<property>\n" + "<name>http.robots.agents</name>\n"
				+ "<value>My Nutch Spider,*</value>\n" + "</property>\n"

				+ "<property>\n"
				+ "<name>parser.character.encoding.default</name>\n"
				+ "<value>utf-8</value>\n" + "</property>\n"

				+ "<property>\n" + "<name>storage.data.store.class</name>\n"
				+ "<value>org.apache.gora.sql.store.SqlStore</value>\n"
				+ "</property>\n"

				+ "<property>\n" + "<name>db.ignore.external.links</name>\n"
				+ "<value>true</value>\n" + "</property>\n"

				+ "<property>\n" + "<name>fetcher.parse</name>\n"
				+ "<value>true</value>\n" + "</property>\n"

				+ "<property>\n" + "<name>fetcher.store.content</name>\n"
				+ "<value>true</value>\n" + "</property>\n"

				+ "<property>\n" + "<name>generate.max.count</name>\n"
				+ "<value>250</value>\n" + "</property>\n"

				+ "<property>\n" + "<name>fetcher.threads.per.host</name>\n"
				+ "<value>"
				+ threadsPerHost
				+ "</value>\n"
				+ "</property>\n"

				+ "<property>\n"
				+ "<name>fetcher.threads.fetch</name>\n"
				+ "<value>"
				+ threadsPerFetch
				+ "</value>\n"
				+ "</property>\n"

				+ "<property>\n"
				+ "<name>fetcher.threads.per.queue</name>\n"
				+ "<value>"
				+ threadsPerQueue
				+ "</value>\n"
				+ "</property>\n"

				+ "<property>\n"
				+ "<name>plugin.folders</name>\n"
				+ "<value>/home/seed/workspace/Crawler/plugins</value>\n"
				+ "</property>\n"

				+ "<property>\n"
				+ "<name>plugin.includes</name>\n"
				+ "<value>protocol-httpclient|urlfilter-regex|parse-(html|tika)|index-basic|query-(basic|site|url)|response-(json|xml)|urlnormalizer-(pass|regex|basic)</value>\n"
				+ "</property>\n"

				+ "<property>\n"
				+ "<name>urlfilter.regex.file</name>\n"
				+ "<value>regex-urlfilter.txt</value>\n"
				+ "</property>\n"

				+ "<property>\n"
				+ "<name>generate.batch.id</name>\n"
				+ "<value>"
				+ WebCrawler.batchID
				+ "</value>\n"
				+ "</property>\n"

				// refetch every 24 hours
				+ "<property>\n"
				+ "<name>db.fetch.interval.default</name>\n"
				+ "<value>86400</value>\n" + "</property>\n"

				+ "</configuration>\n";

		// convert the text to bytes for writing
		byte[] xmlStringBytes = xmlText.getBytes("UTF-8");

		// write to the file and close
		os.write(xmlStringBytes);
		os.close();
	}
}
