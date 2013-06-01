package com.midori.confluence.plugin.mail2news.util;

import java.nio.charset.Charset;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.SMTPMailServer;

public class MailUtil {
	protected final static Logger log = Logger.getLogger(MailUtil.class);

	/**
	 * Get the charset listed in a "Content-Type" header.
	 * 
	 * @param contentType
	 *            The "Content-Type" header.
	 * @return Returns the used charset or null if no information is found.
	 */
	public static Charset getCharsetFromHeader(String contentType) {

		StringTokenizer tok = new StringTokenizer(contentType, ";");

		while (tok.hasMoreTokens()) {
			String token = tok.nextToken().trim();
			if (token.toLowerCase().startsWith("charset")) {
				if (token.indexOf('=') != -1) {
					String charsetString = token
							.substring(token.indexOf('=') + 1);
					try {
						Charset characterSet = Charset.forName(charsetString);
						return characterSet;
					} catch (Exception e) {
						return null;
					}
				}
			}
		}

		return null;
	}

	public static String getEmailAddressFromMessage(Message m)
			throws MessagingException {
		Address[] sender = m.getFrom();
		String creatorEmail = "";
		if (sender.length > 0) {
			if (sender[0] instanceof InternetAddress) {
				creatorEmail = ((InternetAddress) sender[0]).getAddress();
			} else {
				try {
					InternetAddress ia[] = InternetAddress.parse(sender[0]
							.toString());
					if (ia.length > 0) {
						creatorEmail = ia[0].getAddress();
					}
				} catch (AddressException ae) {
				}
			}
		}

		return creatorEmail;
	}

	/**
	 * Send an mail containing the error message back to the user which sent the
	 * given message.
	 * 
	 * @param m
	 *            The message which produced an error while handling it.
	 * @param error
	 *            The error string.
	 */
	public static void sendErrorMessage(Message m, String error)
			throws Exception // FIXME this method should use the higher level
								// email sending facilities in confluence
								// instead of this low level approach
	{
		SMTPMailServer smtpMailServer = MailFactory.getServerManager()
				.getDefaultSMTPMailServer();
		if (smtpMailServer == null) {
			log.warn("Failed to send error message as no SMTP server is configured");
			return;
		}

		if (smtpMailServer.getHostname() == null) {
			log.warn("Failed to send error message as JNDI bound SMTP servers are not supported (JNDI location:<"
					+ smtpMailServer.getJndiLocation() + ">)");
			return;
		}

		/* get system properties */
		Properties props = System.getProperties();

		/* Setup mail server */
		props.put("mail.smtp.host", smtpMailServer.getHostname());

		/* get a session */
		Session session = Session.getDefaultInstance(props, null);
		/* create the message */
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(smtpMailServer.getDefaultFrom()));
		String senderEmail = getEmailAddressFromMessage(m);
		if (senderEmail == "") {
			throw new Exception("Unknown sender of email.");
		}
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(
				senderEmail));
		message.setSubject("[mail2news] Error while handling message ("
				+ m.getSubject() + ")");
		message.setText("An error occurred while handling your message:\n\n  "
				+ error
				+ "\n\nPlease contact the administrator to solve the problem.\n");

		/* send the message */
		Transport tr = session.getTransport("smtp");
		if (StringUtils.isBlank(smtpMailServer.getPort())) {
			tr.connect(smtpMailServer.getHostname(),
					smtpMailServer.getUsername(), smtpMailServer.getPassword());
		} else {
			int smtpPort = Integer.parseInt(smtpMailServer.getPort());
			tr.connect(smtpMailServer.getHostname(), smtpPort,
					smtpMailServer.getUsername(), smtpMailServer.getPassword());
		}
		message.saveChanges();
		tr.sendMessage(message, message.getAllRecipients());
		tr.close();
	}
}
