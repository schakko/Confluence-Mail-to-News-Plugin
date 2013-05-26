package com.midori.confluence.plugin.mail2news.protocol.imap;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import com.midori.confluence.plugin.mail2news.MailUtil;
import com.midori.confluence.plugin.mail2news.message.MessageProcessor;
import com.midori.confluence.plugin.mail2news.message.MessageProcessorException;
import com.midori.confluence.plugin.mail2news.protocol.imap.ImapProtocolHandler.FolderType;
import com.midori.confluence.plugin.mail2news.protocol.imap.ImapProtocolHandler.ImapRootFolderStructure;

public class ImapMessageProcessor implements MessageProcessor {
	protected final Logger log = Logger.getLogger(ImapMessageProcessor.class);

	protected ImapRootFolderStructure rootFolderStructure;
	protected Message message;

	public ImapMessageProcessor(Message message,
			ImapRootFolderStructure rootFolderStructure) {
		this.message = message;
		this.rootFolderStructure = rootFolderStructure;
	}

	public boolean preProcess() throws MessageProcessorException {
		try {
			boolean isSet = message.isSet(Flags.Flag.SEEN);

			if (isSet) {
				log.error("Message \"" + message.getSubject() + "\" has already been flagged as seen before. Moving to processed");
				moveMessage(message,
						rootFolderStructure.getFolder(FolderType.DEFAULT),
						rootFolderStructure.getFolder(FolderType.PROCESSED));
				return false;
			}
		} catch (Exception e) {
			throw new MessageProcessorException("Failed to check SEEN status: "
					+ e.getMessage(), e);
		}

		return true;
	}

	/**
	 * Move a given message from one IMAP folder to another. It will be flagged
	 * as DELETED in the originating folder and thus be deleted the next time
	 * EXPUNGE is called.
	 * 
	 * @param m
	 *            The message to be moved.
	 * @param from
	 *            The folder from which the message has to be moved.
	 * @param to
	 *            The folder to where to move the message.
	 */
	private void moveMessage(Message m, Folder from, Folder to) {
		if (null == m || null == from || null == to) {
			log.fatal("Failed to moveMessage, one or more parameters are null (message: "
					+ m + ", folder.from: " + from + ", folder.to: " + to);
			return;
		}

		try {
			if (!from.isOpen()) {
				from.open(Folder.READ_WRITE);
			}

			if (!to.isOpen()) {
				to.open(Folder.READ_WRITE);
			}

			/* copy the message to the destination folder */
			from.copyMessages(new Message[] { m }, to);
			/* delete the message from the originating folder */
			/*
			 * this sets the DELETED flag, the message will be deleted when
			 * expunging the folder
			 */
			m.setFlag(Flags.Flag.DELETED, true);
		} catch (Exception e) {
			this.log.error("Could not copy message: " + e.getMessage(), e);
			try {
				/*
				 * cannot move the message. mark it read so we will not look at
				 * it again
				 */
				m.setFlag(Flags.Flag.SEEN, true);
			} catch (MessagingException me) {
				/* could not set SEEN on the message */
				this.log.error("Could not set SEEN on message.", me);
			}
		}
	}

	public void postProcess() throws MessageProcessorException {
		/* move the message to the processed folder */
		moveMessage(message, rootFolderStructure.getFolder(FolderType.DEFAULT),
				rootFolderStructure.getFolder(FolderType.PROCESSED));
	}

	public void fail(String failMessage, Exception ex) {
		/* this message has been seen, should not happen */
		/* send email to the sender */
		try {
			log.error("Current message failed :" + failMessage);

			if (null != ex) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				log.error("\nStacktrace:\n" + sw.toString());
			}

			MailUtil.sendErrorMessage(message, failMessage);
		} catch (Exception e) {
			log.error("Failed to send error message: " + e.getMessage());
		}

		/* move this message to the invalid folder */
		moveMessage(message, rootFolderStructure.getFolder(FolderType.DEFAULT),
				rootFolderStructure.getFolder(FolderType.INVALID));
		/* skip this message */
	}
}
