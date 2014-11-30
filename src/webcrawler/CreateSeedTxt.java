package webcrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class takes the urls input by the user and creates
 * the required seed file for Nutch.
 */
public class CreateSeedTxt 
{
	private String rawInput;		// the raw user input
	private String[] parsedUrls;	// after we process the input we store it here

	/**
	 * Constructor.
	 * Requires a string containing the URLs to use as seeds.
	 * @param urls
	 * @throws Exception
	 */
	public CreateSeedTxt(String urls) throws Exception 
	{
		rawInput = urls;	// store the urls to the raw input variable
		parseUrls();		// attempt to parse them
		createSeedFile();	// if they were successfully parsed, create the actual seed file
	}

	/**
	 * Can be called to return how many URLs the user wants for the
	 * seed file. Requires that the input is successfully parsed to 
	 * work.
	 * @return
	 */
	public int getNumUrls() 
	{
		return parsedUrls.length;
	}

	/**
	 * Function to parse the urls.
	 * Will attempt to make sure the urls are in the correct format of a URL
	 * using pattern matching.
	 * If they are then the URLs will be parsed.
	 * @throws Exception
	 */
	private void parseUrls() throws Exception 
	{
		boolean invalidUrls = false;		// used as a marker to indicate if we found a likely invalid url
		parsedUrls = rawInput.split(",");	// if the input is in the correct format, we should just split on comma

		// the pattern that attempts to detect a link
		// found on stackoverflow.com
		Pattern p = Pattern
				.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");

		// loop over the parsed Urls, checking each for validity.
		// if any invalid urls are found, mark the boolean and quit parsing.
		for (int i = 0; i < parsedUrls.length; i++) 
		{
			Matcher m = p.matcher(parsedUrls[i]);
			if (!m.matches()) 
			{
				invalidUrls = true;
				break;
			}
		}

		// if no urls were provided or we found an invalid url we tell the user by throwing an
		// exception and exiting.
		if (parsedUrls.length == 0 || invalidUrls) 
		{
			throw new Exception(
					"Error: Default urls argument not used yet no valid urls were provided to the program. "
							+ "Please check your input to the program and try again. Url format is: \n"
							+ "http://example.com,http://example2.com -> note there are no spaces allowed!");
		}
	}

	/**
	 * Function that creates the seed file if we had valid input and writes 
	 * it to disk.
	 * @throws IOException
	 */
	private void createSeedFile() throws IOException 
	{
		// create the file in the specified location
		FileOutputStream os = new FileOutputStream(new File("urls/seed.txt"), false);
		String writeUrls = "";

		// create the text to write to the file
		for (int i = 0; i < parsedUrls.length; i++) 
		{
			writeUrls += parsedUrls[i]; 
				if (i != parsedUrls.length - 1)
				{
					writeUrls += "\n";
				}
		}

		// convert the text to write to bytes for writing
		byte[] writeUrlsBytes = writeUrls.getBytes("UTF-8");

		// actually write the data and close the file
		os.write(writeUrlsBytes);
		os.close();
	}
}
