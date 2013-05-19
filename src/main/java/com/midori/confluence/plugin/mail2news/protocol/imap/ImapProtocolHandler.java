package com.midori.confluence.plugin.mail2news.protocol.imap;

import java.util.HashMap;
import java.util.Map;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import com.midori.confluence.plugin.mail2news.MailConfiguration;
import com.midori.confluence.plugin.mail2news.message.MessageProcessor;
import com.midori.confluence.plugin.mail2news.protocol.AbstractProtocolHandler;
import com.midori.confluence.plugin.mail2news.protocol.ProtocolHandler;
import com.midori.confluence.plugin.mail2news.protocol.ProtocolHandlerException;

public class ImapProtocolHandler extends AbstractProtocolHandler implements
		ProtocolHandler {
	public class ImapRootFolderStructure
	{
		Folder folderDefault = null;
		Folder folderProcessed = null;
		Folder folderInvalid = null;
		Folder folderInbox = null;
	}

	protected final Logger log = Logger.getLogger(this.getClass());

	protected ImapRootFolderStructure imapRootFolderStructure;
	
	public ImapProtocolHandler(MailConfiguration configuration) {
		super(configuration);
		imapRootFolderStructure = new ImapRootFolderStructure();
	}
	
	@Override
	public void connect() throws ProtocolHandlerException {
		super.connect();

		/***
		 * Open the default folder, under which will be the processed and the
		 * invalid folder.
		 */
		try {
			imapRootFolderStructure.folderDefault = store.getDefaultFolder();
		} catch (MessagingException me) {
			throw new ProtocolHandlerException("Could not get default folder: "
					+ me.getMessage(), me);
		}
		/* sanity check */
		try {
			if (!imapRootFolderStructure.folderDefault.exists()) {
				throw new ProtocolHandlerException(
						"Default folder does not exist. Cannot continue. This might indicate that this software does not like the given IMAP server. If you think you know what the problem is contact the author.");
			}
		} catch (MessagingException me) {
			throw new ProtocolHandlerException(
					"Could not test existence of the default folder: "
							+ me.getMessage(), me);
		}

		/**
		 * This is kind of a fallback mechanism. For some reasons it can happen
		 * that the default folder has an empty name and exists() returns true,
		 * but when trying to create a subfolder it generates an error message.
		 * So what we do here is if the name of the default folder is empty, we
		 * look for the "INBOX" folder, which has to exist and then create the
		 * subfolders under this folder.
		 */
		if (imapRootFolderStructure.folderDefault.getName().equals("")) {
			this.log.warn("Default folder has empty name. Looking for 'INBOX' folder as root folder.");
			try {

				imapRootFolderStructure.folderDefault = store.getFolder("INBOX");
				if (!imapRootFolderStructure.folderDefault.exists()) {
					throw new ProtocolHandlerException(
							"Could not find default folder and could not find 'INBOX' folder. Cannot continue. This might indicate that this software does not like the given IMAP server. If you think you know what the problem is contact the author.");
				}
			} catch (Exception e) {
				throw new ProtocolHandlerException(
						"Failed to get default folder INBOX:" + e.getMessage(),
						e);
			}
		}

		/***
		 * Open the folder for processed messages
		 ***/
		Map<String, Folder> folders = new HashMap<String, Folder>();
		folders.put("Processed", imapRootFolderStructure.folderProcessed);
		folders.put("Invalid", imapRootFolderStructure.folderInvalid);

		Folder folderReference = null;

		for (String folderName : folders.keySet()) {
			folderReference = folders.get(folderName);

			try {
				folderReference = imapRootFolderStructure.folderDefault.getFolder(folderName);
				
				if (!folderReference.exists()) {
					/* does not exist, create it */
					if (!folderReference.create(Folder.HOLDS_MESSAGES)) {
						throw new ProtocolHandlerException("Creating '"
								+ folderName + "' folder failed.");
					}
				}

				/*
				 * we need to open it READ_WRITE, because we want to move
				 * messages we already handled to this folder
				 */
				folderReference.open(Folder.READ_WRITE);
			} catch (Exception e) {
				throw new ProtocolHandlerException(
						"Failed to initialize folder '" + folderName + "': "
								+ e.getMessage(), e);
			}
		}
	}

	public MessageProcessor createMessageProcessor(Message message) {
		return new ImapMessageProcessor(message, imapRootFolderStructure);
	}
}
