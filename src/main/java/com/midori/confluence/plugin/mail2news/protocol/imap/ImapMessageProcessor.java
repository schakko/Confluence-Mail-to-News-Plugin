package com.midori.confluence.plugin.mail2news.protocol.imap;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import com.midori.confluence.plugin.mail2news.MailUtil;
import com.midori.confluence.plugin.mail2news.message.MessageProcessor;
import com.midori.confluence.plugin.mail2news.message.MessageProcessorException;
import com.midori.confluence.plugin.mail2news.protocol.imap.ImapProtocolHandler.ImapRootFolderStructure;

public class ImapMessageProcessor implements MessageProcessor {
	protected final Logger log = Logger.getLogger(ImapMessageProcessor.class);

	protected ImapRootFolderStructure rootFolderStructure;
	protected Message message;

	public ImapMessageProcessor(Message message, ImapRootFolderStructure rootFolderStructure) {
		this.message = message;
		this.rootFolderStructure = rootFolderStructure;
	}

	public void preProcess() throws MessageProcessorException {
		try {
			boolean isSet = message.isSet(Flags.Flag.SEEN);

			if (isSet) {
				throw new MessageProcessorException(
						"This message has already been flagged as seen before being handled and was thus ignored.");
			}
		} catch (Exception e) {
			throw new MessageProcessorException("Failed to check SEEN status: "
					+ e.getMessage(), e);
		}
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
		try {
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
		moveMessage(message, rootFolderStructure.folderInbox,
				rootFolderStructure.folderProcessed);
	}

	public void fail(String failMessage) {
		/* this message has been seen, should not happen */
		/* send email to the sender */
		try {
			MailUtil.sendErrorMessage(message, failMessage);
		} catch (Exception e) {
			log.error("Failed to send error message: " + e.getMessage()
					+ "; original message was: " + failMessage);
		}

		/* move this message to the invalid folder */
		moveMessage(message, rootFolderStructure.folderInbox,
				rootFolderStructure.folderInvalid);
		/* skip this message */
	}
}
