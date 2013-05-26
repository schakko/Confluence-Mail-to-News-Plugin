package com.midori.confluence.plugin.mail2news.protocol;

import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.log4j.Logger;

import com.midori.confluence.plugin.mail2news.MailConfiguration;
import com.sun.mail.imap.IMAPFolder;

/**
 * Base class for IMAP(S) and POP3(S)
 * 
 * @author ckl
 */
public abstract class AbstractProtocolHandler implements ProtocolHandler {
	/**
	 * The log to which we will be logging infos and errors.
	 */
	private final static Logger log = Logger.getLogger(AbstractProtocolHandler.class);

	protected MailConfiguration configuration;

	/* The mailstore object used to connect to the server */
	protected Store store = null;

	/* create the properties for the session */
	protected Properties sessionProperties = new Properties();

	protected Folder folderInbox = null;

	public AbstractProtocolHandler(MailConfiguration configuration) {
		this.configuration = configuration;
	}

	public void connect() throws ProtocolHandlerException {
		String protocol = configuration.getProtocol().toLowerCase()
				.concat(configuration.getSecure() ? "s" : "");
		/* assemble the property prefix for this protocol */
		String propertyPrefix = "mail.";
		propertyPrefix = propertyPrefix.concat(protocol).concat(".");

		/*
		 * get the server port from the configuration and add it to the
		 * properties, but only if it is actually set. If port = 0 this means we
		 * use the standard port for the chosen protocol
		 */
		int port = configuration.getPort();

		if (port != 0) {
			sessionProperties.setProperty(propertyPrefix.concat("port"), ""
					+ port);
		}

		/* set connection timeout (10 seconds) */
		sessionProperties.setProperty(
				propertyPrefix.concat("connectiontimeout"), "10000");

		/* get the session for connecting to the mail server */
		Session session = Session.getInstance(sessionProperties, null);

		/* get the mail store, using the desired protocol */

		try {
			store = session.getStore(protocol);
		} catch (Exception e) {
			throw new ProtocolHandlerException(
					"Failed to get store for protocol '" + protocol + "': "
							+ e.getMessage());
		}

		/*
		 * get the host and credentials for the mail server from the
		 * configuration
		 */
		String host = configuration.getServer();
		String username = configuration.getUsername();
		String password = configuration.getPassword();

		/* sanity check */
		if (host == null || username == null || password == null) {
			throw new ProtocolHandlerException(
					"Incomplete mail configuration settings (at least one setting is null).");
		}

		/* connect to the mailstore */
		try {
			store.connect(host, username, password);
		} catch (AuthenticationFailedException afe) {
			throw new ProtocolHandlerException(
					"Authentication for mail store failed: " + afe.getMessage(),
					afe);
		} catch (MessagingException me) {
			throw new ProtocolHandlerException(
					"Connecting to mail store failed: " + me.getMessage(), me);
		} catch (IllegalStateException ise) {
			throw new ProtocolHandlerException(
					"Connecting to mail store failed, already connected: "
							+ ise.getMessage(), ise);
		} catch (Exception e) {
			throw new ProtocolHandlerException(
					"Connecting to mail store failed, general exception: "
							+ e.getMessage(), e);
		}

		/***
		 * Open the INBOX
		 ***/

		try {
			folderInbox = store.getFolder("INBOX");
		} catch (MessagingException e) {
			throw new ProtocolHandlerException("Failed to get INBOX folder: "
					+ e.getMessage(), e);
		}

		/*
		 * we need to open it READ_WRITE, because we want to move messages we
		 * already handled
		 */
		try {
			folderInbox.open(Folder.READ_WRITE);
		} catch (FolderNotFoundException fnfe) {
			throw new ProtocolHandlerException("Could not find INBOX folder: "
					+ fnfe.getMessage(), fnfe);
		} catch (Exception e) {
			throw new ProtocolHandlerException("Could not open INBOX folder: "
					+ e.getMessage(), e);
		}
	}

	public void close() throws ProtocolHandlerException {
		try {
			store.close();
		} catch (Exception e) {
			throw new ProtocolHandlerException("Failed to close store: "
					+ e.getMessage(), e);
		}
	}

	public Message[] getMessages() throws ProtocolHandlerException {
		try {
			return folderInbox.getMessages();
		} catch (Exception e) {
			throw new ProtocolHandlerException(
					"Failed to get messages from inbox: " + e.getMessage(), e);
		}
	}
}
