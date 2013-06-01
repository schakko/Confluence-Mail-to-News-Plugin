package com.midori.confluence.plugin.mail2news.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;

public class MessageDataExtractor {
	private final static Logger log = Logger
			.getLogger(MessageDataExtractor.class);

	protected Message message = null;

	private MessageContent messageContent;

	public final static String SHARE_WITH_HEADER = "Share-With";

	public MessageDataExtractor(Message message) {
		this.message = message;
	}

	public MessageContent getContent() {
		if (messageContent == null) {
			messageContent = new MessageContent(message);
			messageContent.parse();
		}

		return messageContent;
	}

	/**
	 * Returns a list with identified space names which might be used The
	 * following order is used:
	 * <ul>
	 * <li>Header <i>To</i>: "some+$SPACENAME@domain.tld"; the recipient
	 * delimiter is not supported in Microsoft Exchange</li>
	 * <li>Header <i>To</i>: "$SPACENAME@domain.tld"; the recipient delimiter is
	 * not supported in Microsoft Exchange</li>
	 * <li>Header <i>Cc</i>: "some+$SPACENAME@domain.tld"; the recipient
	 * delimiter is not supported in Microsoft Exchange</li>
	 * <li>Header <i>Cc</i>: "$SPACENAME@domain.tld"; the recipient delimiter is
	 * not supported in Microsoft Exchange</li>
	 * <li>Header <i>Subject</i> "$SPACENAME: subject line"
	 * <li></li>
	 * </ul>
	 * 
	 * @return
	 */
	public List<String> getSpaceNames() {
		List<String> r = new ArrayList<String>();

		r.addAll(getSpaceNamesFromRecipients());
		r.addAll(getSpaceNameFromSubject());

		return r;
	}

	/**
	 * Extracts the space name from subject line. The subject line must be in
	 * format "$SPACEKEY: " or "[$SPACEKEY]"
	 * 
	 * @return
	 */
	public List<String> getSpaceNameFromSubject() {
		return new ArrayList<String>(getSpaceKeyAndTitleFromSubject().keySet());
	}

	/**
	 * Identifies a space key and the title. Matches "$SPACEKEY: $TITLE",
	 * "$SPACEKEY : $TITLE", "[$SPACEKEY] $TITLE"
	 * 
	 * @return
	 */
	public Map<String, String> getSpaceKeyAndTitleFromSubject() {
		List<Pattern> patterns = new ArrayList<Pattern>();
		Map<String, String> r = new HashMap<String, String>();

		String subject = null;

		try {
			subject = message.getSubject();
		} catch (Exception e) {
			log.error("Failed to extract subject from message: "
					+ e.getMessage());
		}

		if (null != subject) {
			subject = subject.trim();

			// "[$SPACEKEY] $TITLE"
			patterns.add(Pattern.compile("^\\[(\\w+)\\]\\s*(.*)"));
			// "$SPACEKEY:$TITLE" or "$SPACEKEY : $TITLE"
			patterns.add(Pattern.compile("^(\\w+)\\s*:\\s*(.*)"));

			for (Pattern pattern : patterns) {
				Matcher matcher = pattern.matcher(subject);

				if (matcher.matches()) {
					r.put(matcher.group(1).toUpperCase(), matcher.group(2));
					break;
				}
			}
		}

		return r;
	}

	/**
	 * Returns possible space names from the recipients. Possible space names
	 * are returned in this order:
	 * <ul>
	 * <li>Header <i>To</i>: "some+$SPACENAME@domain.tld"; the recipient
	 * delimiter is not supported in Microsoft Exchange</li>
	 * <li>Header <i>To</i>: "$SPACENAME@domain.tld"; the recipient delimiter is
	 * not supported in Microsoft Exchange</li>
	 * <li>Header <i>Cc</i>: "some+$SPACENAME@domain.tld"; the recipient
	 * delimiter is not supported in Microsoft Exchange</li>
	 * <li>Header <i>Cc</i>: "$SPACENAME@domain.tld"; the recipient delimiter is
	 * not supported in Microsoft Exchange</li>
	 * </ul>
	 * 
	 * @return
	 */
	public List<String> getSpaceNamesFromRecipients() {
		List<String> r = new ArrayList<String>();
		List<Address> allRecipients = new ArrayList<Address>();

		try {
			RecipientType[] types = new RecipientType[] { RecipientType.TO,
					RecipientType.CC };
			Address[] recipients = null;

			for (RecipientType type : types) {
				recipients = message.getRecipients(type);

				if (null != recipients) {
					Collections.addAll(allRecipients, recipients);
				}
			}
		} catch (Exception e) {
			log.error("Failed to extract recipients: " + e.getMessage());
		}

		String emailAddress = null, spaceKey = null;

		for (Address address : allRecipients) {
			if (address instanceof InternetAddress) {
				emailAddress = ((InternetAddress) address).getAddress();
			} else {
				emailAddress = address.toString();
			}

			/* extract the wiki space name */
			Pattern pattern = Pattern
					.compile("(.+?)([a-zA-Z0-9]+\\+[a-zA-Z0-9]+)@(.+?)");
			Matcher matcher = pattern.matcher(emailAddress);

			if (matcher.matches()) {
				String tmp = matcher.group(2);
				spaceKey = tmp.substring(tmp.indexOf('+') + 1);
			} else {
				/*
				 * the email address is not in the form "aaaa+wikispace@bbb" /*
				 * fallback: test if there exists a space with a spacekey equal
				 * to the local part of the email address.
				 */
				spaceKey = emailAddress.substring(0, emailAddress.indexOf('@'));
			}

			if (!r.contains(spaceKey.toUpperCase())) {
				r.add(spaceKey.toUpperCase());
			}
		}

		return r;
	}

	/**
	 * Returns the e-mail addresses of the sender of the message.
	 * 
	 * @return
	 */
	public List<String> getSenders() {
		List<String> r = new ArrayList<String>();

		try {
			Address[] addresses = message.getFrom();

			for (Address address : addresses) {
				if (!r.contains(address.toString().toLowerCase())) {
					r.add(address.toString().toLowerCase());
				}
			}
		} catch (Exception e) {
			log.error("Failed to extract sender addresses: " + e.getMessage());
		}

		return r;
	}

	/**
	 * This method extracts the users who will receive a Confluence sharing
	 * mail. It <strong>won't</strong> detect the users to share by CC or BCC
	 * because they would already have received a mail, delivered by the local
	 * mail server. Instead of this, the special header
	 * {@value #SHARE_WITH_HEADER} is used, which contains the e-mail addresses
	 * of the recipient. Please use the Generic Exchange Transport Agent for
	 * Exchange Server to change the mail.
	 * 
	 * @return
	 */
	public List<String> getUsersForSharing() {
		List<String> r = new ArrayList<String>();
		StringBuffer sbHeader = new StringBuffer();
		String address = null;

		try {
			String[] headers = message.getHeader(SHARE_WITH_HEADER);

			for (String headerPart : headers) {
				sbHeader.append(headerPart);

			}
		} catch (Exception e) {
			log.info("No header " + SHARE_WITH_HEADER
					+ " defined; not sharing e-mail with anyone");
		}

		if (null != sbHeader) {
			String header = sbHeader.toString().trim();
			String[] addresses = header.split("\\;|\\,");
			for (String s : addresses) {
				address = s.trim().toLowerCase();

				if (null != address && address.length() > 0
						&& !r.contains(address)) {
					r.add(address);
				}
			}
		}

		return r;
	}

	/**
	 * Returns the title of this message
	 * 
	 * @param usedSpaceKey
	 * @return
	 */
	public String getTitle(String usedSpaceKey) {
		if (getSpaceKeyAndTitleFromSubject().containsKey(usedSpaceKey)) {
			return getSpaceKeyAndTitleFromSubject().get(usedSpaceKey);
		}

		String subject = "";

		try {
			subject = message.getSubject();
		} catch (Exception e) {
			log.error("Failed to get subject from message: " + e.getMessage());
		}

		return subject;
	}

	/**
	 * Replaces all linebreaks with <br />
	 * and converts URLs to clickable links. Other HTML code is not removed.
	 * 
	 * @return
	 */
	public String getBodyInStorageFormat() {
		// non-null
		String text = getContent().getText();

		// replace line breaks
		text = text.replaceAll("\r\n", "<br />");

		// Replace URLs with links; fixed version from
		// http://stackoverflow.com/questions/1909534/java-replacing-text-url-with-clickable-html-link
		text = text.replaceAll("(\\w+://[^\\s^<^>]*)",
				"<a href=\"$1\">$1</a>");

		return text;
	}
}
