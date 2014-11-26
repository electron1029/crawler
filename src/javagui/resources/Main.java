package javagui.resources;

import webcrawler.WebCrawler;

import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Toolkit;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
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

public class Main extends JFrame implements ActionListener, PropertyChangeListener {

	private JPanel contentPane;
	private JTextField textFieldSeeds;
	private JTextField textFieldNumPagesToCrawl;
	private JTextField textFieldMysqlUsername;
	private JTextField textFieldMysqlPassword;
	private JTextField textFieldMysqlServer;
	private JTextField textFieldThreadsPerHost;
	private JTextField textFieldTotalThreads;
	private JLabel lblPagesCrawled;
	private JLabel lblPPM;
	private JProgressBar crawlProgressBar;
	private JLabel elapsedTime;
	private JButton crawlButton;
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
		setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/javagui/resources/Tulips.jpg")));
		setTitle("My java GUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 720, 600);

		contentPane = new JPanel();
		contentPane.setBackground(new Color(240, 255, 255));
		contentPane.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		setContentPane(contentPane);

		JLabel lblTitle = new JLabel("Smart Web Crawler");

		JLabel lblMysqlUsername = new JLabel("MySQL Username");
		textFieldMysqlUsername = new JTextField();
		textFieldMysqlUsername.setColumns(20);

		JLabel lblMysqlPassword = new JLabel("MySQL Password");
		textFieldMysqlPassword = new JTextField();
		textFieldMysqlPassword.setColumns(20);

		JLabel lblMysqlServer = new JLabel("MySQL Server");
		textFieldMysqlServer = new JTextField();
		textFieldMysqlServer.setColumns(20);

		JLabel lblSeed = new JLabel("Seed Urls");
		textFieldSeeds = new JTextField();
		textFieldSeeds.setColumns(20);

		JLabel lblthreadsPerHost = new JLabel("Parallel Requests Per Domain");
		textFieldThreadsPerHost = new JTextField();
		textFieldThreadsPerHost.setColumns(20);

		JLabel lblTotalThreads = new JLabel("Max simultaneous parallel requests");
		textFieldTotalThreads = new JTextField();
		textFieldTotalThreads.setColumns(20);

		JLabel lblNoofPagesTo = new JLabel("Number of pages to crawl");
		textFieldNumPagesToCrawl = new JTextField();
		textFieldNumPagesToCrawl.setColumns(20);

		crawlButton = new JButton("Crawl");
		crawlButton.addActionListener(this);

		JLabel lblProgress = new JLabel("Overall Progress");
		crawlProgressBar = new JProgressBar(0, 100);
		crawlProgressBar.setValue(0);
		crawlProgressBar.setStringPainted(true);
		crawlProgressBar.addPropertyChangeListener("progress", this);

		JLabel labelTime = new JLabel("ElapsedTime");
		elapsedTime = new JLabel("0 hours, 0 minutes, 0 seconds");

		JLabel lblCrawlSpeed = new JLabel("Pages crawled per min:");
		lblPPM = new JLabel("0");

		JLabel lblPagesToCrawl = new JLabel("Number of pages crawled");
		lblPagesCrawled = new JLabel("0");

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
																.addGap(40)
																.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																		.addComponent(textFieldMysqlUsername, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(textFieldMysqlPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(textFieldMysqlServer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(textFieldThreadsPerHost, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(textFieldTotalThreads, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(textFieldSeeds, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(textFieldNumPagesToCrawl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
																		.addGroup(gl_contentPane.createSequentialGroup()
																				.addGap(94)
																				.addComponent(lblTitle, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE))))
																				.addGroup(gl_contentPane.createSequentialGroup()
																						.addGap(262)
																						.addComponent(crawlButton)))
																						.addContainerGap(312, Short.MAX_VALUE))
																						.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
																								.addGap(133)
																								.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
																										.addComponent(lblPagesToCrawl)
																										.addGroup(gl_contentPane.createSequentialGroup()
																												.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																														.addComponent(lblProgress)
																														.addComponent(lblCrawlSpeed)
																														.addComponent(labelTime))
																														.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																																.addGroup(gl_contentPane.createSequentialGroup()
																																		.addGap(10)
																																		.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																																				.addComponent(crawlProgressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																																				.addComponent(lblPagesCrawled, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																																				.addComponent(elapsedTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																																				.addGap(10))
																																				.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()))
																																						.addComponent(lblPPM, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
																																						.addContainerGap(0, Short.MAX_VALUE))
				);
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
																								.addComponent(elapsedTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																								.addGap(10)
																								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																										.addComponent(lblPagesToCrawl)
																										.addComponent(lblPagesCrawled, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																										.addGap(10)
																										.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
																												.addComponent(lblCrawlSpeed)
																												.addComponent(lblPPM, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))));
		contentPane.setLayout(gl_contentPane);
	}

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

			wc = new WebCrawler(mysqlUsername, mysqlPassword, mysqlServer, urlSeeds, 
					threadsPerHost, totalThreads, "-1", pagesToCrawl);
			wc.start();


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

	private class Task extends Thread 
	{    
		public Task(){}

		public void run()
		{
			while(true)
			{
				SwingUtilities.invokeLater(new Runnable() 
				{
					public void run() 
					{
						crawlProgressBar.setValue((int)(wc.getPercentageCrawled() * 100));
						elapsedTime.setText("" + wc.getElapsedTime());
						lblPPM.setText("" + wc.getCrawlSpeedPPM());
						lblPagesCrawled.setText("" + wc.getTotalPagesCrawled());
						if (wc.getPercentageCrawled() == 1)
						{
							return;
						}
					}
				});
				try 
				{
					Thread.sleep(100);
				} catch (InterruptedException e) 
				{

				}
			}
		}
	}   
}
