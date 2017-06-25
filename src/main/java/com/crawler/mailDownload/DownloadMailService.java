package com.crawler.mailDownload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

/**
 * @author Pantina Shrikant
 */
public class DownloadMailService {

	private Logger LOGGER = Logger.getLogger(DownloadMailService.class);
	
	/**
	 * This method downloads the mail message for a given URL and saves to file system
	 * The method is generic and capable of parsing HTML,XML and RAW mail URL links
	 * @param crawledMailLink
	 * @param mailDowloadDirectory
	 */
	public void downloadMailToFile(String crawledMailLink, String mailDowloadDirectory) {

		try {
			Document mailContentDoc = Jsoup.connect(crawledMailLink).get();
			/*
			 * Document mailContentDoc = Jsoup .connect(
			 * "http://mail-archives.apache.org/mod_mbox/maven-users/201605.mbox/ajax/<CAPCjjnFM%2B%2B2_ORDWKuy4TVE_q5VWYDfyuZwxUYbGyRKxZPiS0w%40mail.gmail.com>")
			 * .get();
			 */
			/*
			 * Document mailContentDoc = Jsoup .connect(
			 * "http://mail-archives.apache.org/mod_mbox/maven-users/201605.mbox/<CAPCjjnFM%2B%2B2_ORDWKuy4TVE_q5VWYDfyuZwxUYbGyRKxZPiS0w%40mail.gmail.com>")
			 * .get();
			 */
			String mailSubject = getParsedSubject(mailContentDoc);
			
			mailSubject = mailSubject.trim().replaceAll(" ", "_");
			String dateString = getParsedDate(mailContentDoc);

			if (mailSubject == null || "".equalsIgnoreCase(mailSubject) || dateString == null	|| "".equalsIgnoreCase(dateString)) {
				return;
			}
			
			Date dateValue = new Date(dateString);
			SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yy hh.mm.ss");
			String formattedDateString = df.format(dateValue);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateValue);
			int mailYear = cal.get(Calendar.YEAR);
			String mailMonth = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
			int mailDate = cal.get(Calendar.DAY_OF_MONTH);

			File outputDirectory = new File(mailDowloadDirectory + File.separator + mailYear + File.separator + mailMonth + File.separator + mailDate);
			if (!outputDirectory.exists()) {
				outputDirectory.mkdirs();
			}
			String fileName = formattedDateString +" | "+ (mailSubject.length() <= 100 ? mailSubject : mailSubject.substring(0 , 100));
			File file = new File(outputDirectory.getPath() + File.separator + fileName.replaceAll("[\\\\/:*?\"<>|]", "").trim() + ".txt");

			if (!file.exists()) {
				boolean isMailCreated = file.createNewFile();
				if (isMailCreated) {
					String cleanMailText = cleanTagPerservingLineBreaks(mailContentDoc.text());
					BufferedWriter writer = new BufferedWriter(new FileWriter(file));
					writer.write(cleanMailText);
					writer.close();
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}
	
	public static String cleanTagPerservingLineBreaks(String html) {
		String result = "";
		if (html == null)
			return html;
		Document document = Jsoup.parse(html);
		document.outputSettings(new Document.OutputSettings().prettyPrint(false));
		// document.select("br").append("\\n");
		result = document.html().replaceAll("\\\\n", "\n");
		result = Jsoup.clean(result, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
		return result;
	}

	public static String getParsedDate(Document mailContent) {

		String dateString = null;
		if (dateString == null || "".equalsIgnoreCase(dateString)) {
			dateString = mailContent.select("date").text();
		}
		if (dateString == null || "".equalsIgnoreCase(dateString)) {
			dateString = mailContent.select(".date").text().trim().substring(mailContent.select(".date").text().indexOf("date") + 5).trim();
		}
		return dateString;
	}

	public static String getParsedSubject(Document mailContent) {

		// raw File parsing
		String subject = null;
		String[] parts = mailContent.html().split("\\r?\\n");
		for (String subjectPart : parts) {
			if (subjectPart.indexOf("Subject:") >= 0) {
				subject = subjectPart.replace("Subject:", "").trim();
				break;
			}
		}
		// xml File parsing
		if (subject == null || "".equalsIgnoreCase(subject)) {
			subject = mailContent.select("subject").text();
			subject = subject.replace("subject", "").trim();
		}
		// html File parsing
		if (subject == null || "".equalsIgnoreCase(subject)) {
			subject = mailContent.select("tr:contains(subject)").text();
			subject = subject.replace("Subject", "").trim();
		}
		return subject;
	}
	
}
