package com.crawler;

import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jsoup.select.Elements;

import com.crawler.mailDownload.DownloadMailService;
import com.crawler.service.CrawlingService;
import com.crawler.util.CrawlerUtility;

/**
 * @author Pantina Shrikant
 */
public class CrawlerMain implements Runnable {

	private static Logger LOGGER = Logger.getLogger(CrawlerMain.class);

	private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

	// should be configured in a properties file later for better maintnance
	static final String pageURL = "http://mail-archives.apache.org/mod_mbox/maven-users/";
	static final String mailDowloadDirectory = "C:\\Users\\HOME\\Downloads\\WebCrawler\\files";
	static final String defaultYear = "2015";

	public static void main(String[] args) {
		
		CrawlerMain crawlMain = new CrawlerMain();
		String userInputYear = crawlMain.takeUserInput();
		final String year = (userInputYear != null && !"".equalsIgnoreCase(userInputYear)) ? userInputYear : defaultYear;
		crawlMain.startCrawl(crawlMain.queue, pageURL, year);
	}

	/**
	 * This method takes following parameters to initiate page crawl and
	 * starts a thread to listen to blocking queue to parallel process crawling and
	 * downloading the mail message for a year
	 * 
	 * @param queue
	 *            - A linkedBlocking implementation
	 * @param pageURL
	 * @param year
	 */
	public void startCrawl(BlockingQueue<String> queue, String pageURL, String year) {

		long startTime = System.currentTimeMillis();
		HashMap<String, String> crawlMap = new HashMap<String, String>();
		
		Elements links = CrawlerUtility.getElementFromURL(pageURL);
		Thread mailDownloadThread = new Thread(this);
		mailDownloadThread.start();

		LOGGER.info("****** crawling started for year :: "+ year +" ******");
		CrawlingService crawlPage = new CrawlingService();
		crawlPage.crawlPages(queue, crawlMap, links, pageURL, year);
		LOGGER.info("****** crawling ended for year :: "+ year +" ******");

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		LOGGER.info("***********************************************************************************");
		LOGGER.info("URL's crawled: " + crawlMap.size() + " in " + totalTime + " ms (avg: "+ totalTime / crawlMap.size() + ")");
		LOGGER.info("***********************************************************************************");
	}

	public void run() {

		LOGGER.info("****** Thread for downloading mails started ******");
		DownloadMailService downloadMail = new DownloadMailService();
		while (true) {
			try {
				String mailHrefLink = queue.poll(5, TimeUnit.SECONDS);
				if (mailHrefLink != null) {
					downloadMail.downloadMailToFile(mailHrefLink, mailDowloadDirectory);
				} else {
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	public String takeUserInput(){
		Scanner scanner = new Scanner(System.in);
		System.out.print("Please enter the year to crawl ::  ");
		String userInputYear = scanner.next();
		scanner.close();
		return userInputYear;
	}
}