import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class generates the regex-urlfilter.txt with the necessary settings.
 * This needs to be generated as we change the default settings for our 
 * crawler based on the project needs.
 * The websites webmd and mayoclinic contain many urls with question marks
 * and equal signs in them. By default, Nutch does not index these urls.
 * We create a new regex-urlfilter.txt that allows Nutch to not ignore these
 * URLs so we can properly crawl the required sites.
 */
public class GenerateURLRegexTXT 
{

	/**
	 * Constructor
	 */
	public GenerateURLRegexTXT() 
	{
		try 
		{
			// the constructor just generates the file
			generateURLRegexFilter();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * Function that actually generates the file and writes it to the proper location on disk.
	 * @throws IOException
	 */
	private void generateURLRegexFilter() throws IOException 
	{
		// set the file output location
		FileOutputStream os = new FileOutputStream(new File("nutchconf/regex-urlfilter.txt"), false);

		// the text to write in the file
		String txtText = "# skip file: ftp: and mailto: urls\n"
				+ "-^(file|ftp|mailto):\n"
				+ "# skip image and other suffixes we can't yet parse\n"
				+ "# for a more extensive coverage use the urlfilter-suffix plugin\n"
				+ "-\\.(gif|GIF|jpg|JPG|png|PNG|ico|ICO|css|CSS|sit|SIT|eps|EPS|wmf|"
				+ "WMF|zip|ZIP|ppt|PPT|mpg|MPG|xls|XLS|gz|GZ|rpm|RPM|tgz|TGZ|mov|MOV|exe|EXE|jpeg|JPEG|bmp|BMP|js|JS)$\n"
				+ "# skip URLs containing certain characters as probable queries, etc.\n"
				+ "-[*!@]\n"
				+ "# skip URLs with slash-delimited segment that repeats 3+ times, to break loops\n"
				+ "-.*(/[^/]+)/[^/]+\1/[^/]+\1/\n" + "# accept anything else\n"
				+ "+.{10,199}";

		// in order to write the data we must convert it to bytes first
		byte[] txtTextBytes = txtText.getBytes();

		// actually write the data and close the file
		os.write(txtTextBytes);
		os.close();
	}
}
