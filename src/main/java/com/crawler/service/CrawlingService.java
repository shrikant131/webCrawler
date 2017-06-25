package com.crawler.service;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.crawler.util.CrawlerUtility;

/**
 * @author Pantina Shrikant
 */
public class CrawlingService {

	private Logger LOGGER = Logger.getLogger(CrawlingService.class);
	
	/**
	 * This methods takes following parameters and adds links form pages eligible for crawling
	 * @param queue
	 * @param crawlMap
	 * @param links
	 * @param pageURL
	 * @param year
	 * @return
	 */
	public HashMap<String, String> crawlPages(BlockingQueue<String> queue, HashMap<String, String> crawlMap, Elements links, String pageURL, String year) {

		for (Element link : links) {
			String subLink = link.attr("abs:href");
			if (CrawlerUtility.crawlCheck(subLink, year)) {
				// if (subLink.indexOf(year) != -1) {

				String messageId = CrawlerUtility.returnMessageId(subLink);

				if (messageId != null) {
					if (crawlMap.get(messageId) == null) {
						crawlMap.put(messageId, subLink);
						try {
							queue.put(subLink);
						} catch (InterruptedException ignored) {
						}
						crawlSubPages(queue, crawlMap, subLink, year);
					}
				} else {
					if (crawlMap.get(subLink) == null) {
						crawlMap.put(subLink, subLink);
						try {
							queue.put(subLink);
						} catch (InterruptedException ignored) {
						}
						crawlSubPages(queue, crawlMap, subLink, year);
					}
				}
			}
		}
		return crawlMap;
	}

	/**
	 * This methods takes following parameters and recursively calls crawlSubPages for depth crawling for a given subLink
	 * @param queue
	 * @param crawlMap
	 * @param subLink
	 * @param year
	 */
	public void crawlSubPages(BlockingQueue<String> queue, HashMap<String, String> crawlMap, String subLink,	String year) {

		try {

			if (CrawlerUtility.crawlCheck(subLink, year)) {
				// if (subLink.indexOf(year) != -1) {

				LOGGER.info("****** Crawling inner link :: " + subLink + " ******");

				Elements innerSubLinks = CrawlerUtility.getElementFromURL(subLink);

				if (innerSubLinks.size() != 0) {
					/*
					 * if (crawlMap.get(subLink) == null) {
					 * crawlMap.put(subLink, subLink); }
					 */
					for (Element innerSubLink : innerSubLinks) {
						String currentLink = innerSubLink.attr("abs:href");
						String messageId = CrawlerUtility.returnMessageId(currentLink);

						if (messageId != null) {
							if (crawlMap.get(messageId) == null) {
								crawlMap.put(messageId, currentLink);
								try {
									queue.put(subLink);
								} catch (InterruptedException ignored) {
								}
								crawlSubPages(queue, crawlMap, currentLink, year);
							}
						} else {
							if (crawlMap.get(subLink) == null) {
								crawlMap.put(subLink, currentLink);
								try {
									queue.put(subLink);
								} catch (InterruptedException ignored) {
								}
								crawlSubPages(queue, crawlMap, currentLink, year);
							}
						}
					}
				}
			}

		} catch (Exception exception) {
			LOGGER.error("Exception Occurred is: " + exception.getMessage());
		}
	}
}
