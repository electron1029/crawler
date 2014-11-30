package javagui.resources;

import webcrawler.WebCrawler;

import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Main extends JFrame implements ActionListener, PropertyChangeListener 
{
	// GUI specific variables
	private JPanel contentPane;
	private JTextField textFieldSeeds;
	private JTextField textFieldNumPagesToCrawl;
	private JTextField textFieldMysqlUsername;
	private JTextField textFieldMysqlPassword;
	private JTextField textFieldMysqlServer;
	private JTextField textFieldThreadsPerHost;
	private JTextField textFieldTotalThreads;
	private JButton crawlButton;
	private JLabel lblPagesCrawled;
	private JLabel lblPPM;
	private JProgressBar crawlProgressBar;
	private JLabel lblElapsedTime;
	private JScrollPane urlSuccessScrollBox;
	private JList crawledUrls;
	private JLabel lblCrawlErrors;
	private JScrollPane urlUnsuccessfulScrollBox;
	private JList unsuccessfulUrls;
	private Task task;

	private WebCrawler wc;
	public static ByteArrayOutputStream b;	
	private int done;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		try 
		{
			// redirects System.out to a byte array stream
			// this causes Nutches informative output that we have configured to provide the data
			// we need to display to go to this stream. This is how we can get the information
			// since the actually methods in the Nutch API to directly calculate the data are
			// private.
			b = new ByteArrayOutputStream();
			PrintStream p = new PrintStream(b);
			System.setOut(p);

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) 
		{
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					Main frame = new Main();
					frame.setVisible(true);
				} catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Main() 
	{
		// create the main window
		setTitle("Smart Web Crawler");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 720, 720);

		// add a content pane so we can attach gui elements
		contentPane = new JPanel();
		contentPane.setBackground(new Color(240, 255, 255));
		contentPane.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		setContentPane(contentPane);

		// title of application
		JLabel lblTitle = new JLabel("Smart Web Crawler");

		// label and textbox for mysql username input
		JLabel lblMysqlUsername = new JLabel("MySQL Username");
		textFieldMysqlUsername = new JTextField();
		textFieldMysqlUsername.setColumns(20);

		// lable and textbox for mysql password input
		JLabel lblMysqlPassword = new JLabel("MySQL Password");
		textFieldMysqlPassword = new JTextField();
		textFieldMysqlPassword.setColumns(20);

		// label and textbox for mysql server input
		JLabel lblMysqlServer = new JLabel("MySQL Server");
		textFieldMysqlServer = new JTextField();
		textFieldMysqlServer.setColumns(20);

		// label and textbook for seed urls input
		JLabel lblSeed = new JLabel("Seed Urls");
		textFieldSeeds = new JTextField();
		textFieldSeeds.setColumns(20);

		// label and textbox for # parallel requests per domain input
		JLabel lblthreadsPerHost = new JLabel("Parallel Requests Per Domain");
		textFieldThreadsPerHost = new JTextField();
		textFieldThreadsPerHost.setColumns(20);

		// label and textbox for total # of threads input
		JLabel lblTotalThreads = new JLabel("Max simultaneous parallel requests");
		textFieldTotalThreads = new JTextField();
		textFieldTotalThreads.setColumns(20);

		// label and textbox for total # of pages to crawl input
		JLabel lblNoofPagesTo = new JLabel("Number of pages to crawl");
		textFieldNumPagesToCrawl = new JTextField();
		textFieldNumPagesToCrawl.setColumns(20);

		// button to press to start a crawl
		crawlButton = new JButton("Crawl");
		crawlButton.addActionListener(this);

		// create progress bar
		JLabel lblProgress = new JLabel("Overall Progress");
		crawlProgressBar = new JProgressBar(0, 100);
		crawlProgressBar.setValue(0);
		crawlProgressBar.setStringPainted(true);
		crawlProgressBar.addPropertyChangeListener("progress", this);

		// display elapsed time
		JLabel labelTime = new JLabel("ElapsedTime");
		lblElapsedTime = new JLabel("0 hours, 0 minutes, 0 seconds");

		// display crawl speed
		JLabel lblCrawlSpeed = new JLabel("Pages crawled per min");
		lblPPM = new JLabel("0");

		// display pages crawled so far
		JLabel lblPagesToCrawl = new JLabel("Number of pages crawled");
		lblPagesCrawled = new JLabel("0");

		// display the urls attempted to crawl so far
		JLabel lblUrlsCrawledSoFar = new JLabel("Urls crawled");
		String[] initializeCrawled = new String[] {"none"};
		crawledUrls = new JList(initializeCrawled);
		crawledUrls.setVisibleRowCount(5);
		urlSuccessScrollBox = new JScrollPane(crawledUrls, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		// display number of errors
		JLabel lblPagesWithErrors = new JLabel("Crawl errors");
		lblCrawlErrors = new JLabel("0");
		
		// display urls that had errors so far
		JLabel lblUrlsErrorsSoFar = new JLabel("Urls with errors");
		String[] initializeUncrawled = new String[] {"none"};
		unsuccessfulUrls = new JList(initializeUncrawled);
		unsuccessfulUrls.setVisibleRowCount(5);
		urlUnsuccessfulScrollBox = new JScrollPane(unsuccessfulUrls, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	
		// add all above content to the content pane
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
				gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
										.addGap(80)
										.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
												.addGroup(gl_contentPane.createSequentialGroup()
														.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																.addComponent(lblMysqlUsername)
																.addComponent(lblMysqlPassword)
																.addComponent(lblMysqlServer)
																.addComponent(lblSeed)
																.addComponent(lblthreadsPerHost)
																.addComponent(lblTotalThreads)
																.addComponent(lblNoofPagesTo))
																.addGap(20)
																.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																		.addComponent(textFieldMysqlUsername, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(textFieldMysqlPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(textFieldMysqlServer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(textFieldThreadsPerHost, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(textFieldTotalThreads, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(textFieldSeeds, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(textFieldNumPagesToCrawl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
																		.addGroup(gl_contentPane.createSequentialGroup()
																				.addGap(200)
																				.addComponent(lblTitle, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE))))
																				.addGroup(gl_contentPane.createSequentialGroup()
																						.addGap(262)
																						.addComponent(crawlButton)))
																						.addContainerGap(312, Short.MAX_VALUE))
																						.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
																								.addGap(50)
																								.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
																										.addComponent(lblPagesToCrawl)
																										.addGroup(gl_contentPane.createSequentialGroup()
																												.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																														.addComponent(lblProgress)
																														.addComponent(lblCrawlSpeed)
																														.addComponent(labelTime)
																														.addComponent(lblUrlsCrawledSoFar)
																														.addComponent(lblPagesWithErrors)
																														.addComponent(lblUrlsErrorsSoFar))
																														.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																												.addGroup(gl_contentPane.createSequentialGroup()
																														.addGap(40)
																														.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																																.addComponent(crawlProgressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																																.addComponent(lblPagesCrawled, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																																.addComponent(lblElapsedTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																																.addComponent(lblPPM, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																																.addComponent(urlSuccessScrollBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																																.addComponent(lblCrawlErrors, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																																.addComponent(urlUnsuccessfulScrollBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
																																.addContainerGap(0, Short.MAX_VALUE)))));
		gl_contentPane.setVerticalGroup(
				gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addGap(10)
						.addComponent(lblTitle)
						.addGap(10)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblMysqlUsername)
								.addComponent(textFieldMysqlUsername, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(10)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
										.addComponent(lblMysqlPassword)
										.addComponent(textFieldMysqlPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addGap(10)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
												.addComponent(lblMysqlServer)
												.addComponent(textFieldMysqlServer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addGap(10)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
														.addComponent(lblthreadsPerHost)
														.addComponent(textFieldThreadsPerHost, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
														.addGap(10)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																.addComponent(lblTotalThreads)
																.addComponent(textFieldTotalThreads, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																.addGap(10)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																		.addComponent(lblSeed)
																		.addComponent(textFieldSeeds, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																		.addGap(10)
																		.addPreferredGap(ComponentPlacement.RELATED)
																		.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																				.addComponent(lblNoofPagesTo)
																				.addComponent(textFieldNumPagesToCrawl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																				.addGap(10)
																				.addComponent(crawlButton)
																				.addGap(10)
																				.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																						.addComponent(lblProgress)
																						.addComponent(crawlProgressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																						.addGap(10)
																						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																								.addComponent(labelTime)
																								.addComponent(lblElapsedTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																								.addGap(10)
																								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																										.addComponent(lblCrawlSpeed)
																										.addComponent(lblPPM, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																										.addGap(10)
																										.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																												.addComponent(lblPagesToCrawl)
																												.addComponent(lblPagesCrawled, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																												.addGap(10)
																												.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																														.addComponent(lblUrlsCrawledSoFar)
																														.addComponent(urlSuccessScrollBox, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE))
																														.addGap(10)
																														.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																																.addComponent(lblPagesWithErrors)
																																.addComponent(lblCrawlErrors, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																																.addGap(10)
																																.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																																		.addComponent(lblUrlsErrorsSoFar)
																																		.addComponent(urlUnsuccessfulScrollBox, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE))));
		contentPane.setLayout(gl_contentPane);
	}

	/**
	 * Called when the crawl button is pressed
	 */
	public void actionPerformed(ActionEvent e) 
	{
		// get text fields
		String mysqlUsername = textFieldMysqlUsername.getText();
		String mysqlPassword = textFieldMysqlPassword.getText();
		String mysqlServer = textFieldMysqlServer.getText();
		String urlSeeds = textFieldSeeds.getText();
		String threadsPerHost = textFieldThreadsPerHost.getText();
		String totalThreads = textFieldTotalThreads.getText();
		String pagesToCrawl = textFieldNumPagesToCrawl.getText();

		try 
		{
			// create the web crawler instance with the user parameters
			wc = new WebCrawler(mysqlUsername, mysqlPassword, mysqlServer, urlSeeds, 
					threadsPerHost, totalThreads, "-1", pagesToCrawl);
			wc.start();

			// start the task to pull crawl data into the UI
			task = new Task();                
			task.start();

		} catch (Exception e1) 
		{
			showErrorDialog(new JFrame(), e1);
		}

	}

	public void propertyChange(PropertyChangeEvent event) 
	{
		crawlProgressBar.setValue(done);

	}

	/**
	 * Crawled when the program throws an exception
	 * @param parent
	 * @param e
	 */
	public static void showErrorDialog(JFrame parent, Exception e) 
	{
		final JTextArea textArea = new JTextArea();
		textArea.setEditable(false);

		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		textArea.setText(writer.toString());

		JScrollPane scrollPane = new JScrollPane(textArea);		
		scrollPane.setPreferredSize(new Dimension(350, 150));

		JOptionPane.showMessageDialog(parent, scrollPane, "An Error Has Occurred", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Class that updates the data in the UI in a different thread.
	 * @author seed
	 *
	 */
	private class Task extends Thread 
	{    
		public Task(){}

		public void run()
		{
			do
			{
				SwingUtilities.invokeLater(new Runnable() 
				{
					public void run() 
					{
						// update data on UI
						crawlProgressBar.setValue((int)(wc.getPercentageCrawled() * 100));
						lblElapsedTime.setText("" + wc.getElapsedTime());
						lblPPM.setText("" + wc.getCrawlSpeedPPM());
						lblPagesCrawled.setText("" + wc.getTotalPagesCrawled());
						crawledUrls.setListData(wc.getCrawledUrls());
						unsuccessfulUrls.setListData(wc.getUnsuccessfulUrls());
					}
				});
				try 
				{
					Thread.sleep(100);
				} catch (InterruptedException e) 
				{
					return;
				}
			} while(crawlProgressBar.getValue() != 100);
		}
	}   
}
