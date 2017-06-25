package com.crawler.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @author Pantina Shrikant
 */
public class CrawlerUtility {
	
	private static Logger LOGGER = Logger.getLogger(CrawlerUtility.class);
	
	public static Elements getElementFromURL(String URL){
		
		Document doc = null;
		Elements links = null;
		try {
			doc = Jsoup.connect(URL).ignoreContentType(true).get();
			links = doc.select("a[href]");
			
		} catch (IOException ex) {
			LOGGER.error("IO Excpetion : " + ex.getMessage());
		} catch (Exception ex) {
			LOGGER.error("Excpetion : " + ex.getMessage());
		}
		return links;
		
	}
	
	public static boolean crawlCheck(String urlToCrawl , String year) {
        if(!(urlToCrawl.indexOf(year) != -1)){
            return false;
        }
        if(urlToCrawl.startsWith("javascript:"))  { 
        	return false; 
        }
        if(urlToCrawl.startsWith("#")) {
        	return false; 
        }
        
        return true;
    }

	public static String returnMessageId(String queryHrefLink) {

		String messageId = null;
		try {
			messageId = URLDecoder.decode(queryHrefLink, "UTF-8");
			Pattern pattern = Pattern.compile("\\<(.*?)\\>");
			Matcher macther = pattern.matcher(messageId);
			while (macther.find()) {
				messageId = macther.group(1);
			}
		} catch (UnsupportedEncodingException e) {
		}
		return messageId;
	}

}
