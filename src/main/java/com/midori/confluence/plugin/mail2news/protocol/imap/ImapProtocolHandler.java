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
	public enum FolderType {
		DEFAULT, PROCESSED, INVALID
	}

	public class ImapRootFolderStructure {
		Map<FolderType, Folder> folders = new HashMap<FolderType, Folder>();

		public Folder getFolder(FolderType type) {
			return folders.get(type);
		}
	}

	private final static Logger log = Logger
			.getLogger(ImapProtocolHandler.class);

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
			imapRootFolderStructure.folders.put(FolderType.DEFAULT,
					store.getDefaultFolder());
		} catch (MessagingException me) {
			throw new ProtocolHandlerException("Could not get default folder: "
					+ me.getMessage(), me);
		}
		/* sanity check */
		try {
			if (!imapRootFolderStructure.getFolder(FolderType.DEFAULT).exists()) {
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
		if (imapRootFolderStructure.getFolder(FolderType.DEFAULT).getName()
				.equals("")) {
			log.warn("Default folder has empty name. Looking for 'INBOX' folder as root folder.");

			try {
				imapRootFolderStructure.folders.put(FolderType.DEFAULT,
						store.getFolder("INBOX"));

				if (!imapRootFolderStructure.getFolder(FolderType.DEFAULT)
						.exists()) {
					throw new ProtocolHandlerException(
							"Could not find default folder and could not find 'INBOX' folder. Cannot continue. This might indicate that this software does not like the given IMAP server. If you think you know what the problem is contact the author.");
				}
			} catch (Exception e) {
				throw new ProtocolHandlerException(
						"Failed to get default folder INBOX:" + e.getMessage(),
						e);
			}
		}

		try {
			imapRootFolderStructure.getFolder(FolderType.DEFAULT).open(
					Folder.READ_WRITE);
		} catch (Exception e) {
			throw new ProtocolHandlerException(
					"Failed to open INBOX for read/write: " + e.getMessage(), e);
		}

		/***
		 * Open the folder for processed messages
		 ***/
		FolderType[] typesToResolve = new FolderType[] { FolderType.PROCESSED,
				FolderType.INVALID };

		for (FolderType folderType : typesToResolve) {
			String folderName = folderType.toString();
			Folder folderReference = null;

			log.error("Initializing folder [" + folderName + "]");

			try {
				folderReference = store.getFolder(folderName);

				if (!folderReference.exists()) {
					log.info("Folder " + folderName
							+ " does not exists, creating...");

					/* does not exist, create it */
					if (!folderReference.create(Folder.HOLDS_MESSAGES)) {
						log.error("Failed to create folder '" + folderName
								+ "'");
						throw new ProtocolHandlerException("Creating '"
								+ folderName + "' folder failed.");
					}
				}

				/*
				 * we need to open it READ_WRITE, because we want to move
				 * messages we already handled to this folder
				 */
				folderReference.open(Folder.READ_WRITE);
				imapRootFolderStructure.folders
						.put(folderType, folderReference);
			} catch (Exception e) {
				throw new ProtocolHandlerException(
						"Failed to initialize folder '" + folderName + "': "
								+ e.getMessage(), e);
			}
		}
	}

	public void close() throws ProtocolHandlerException {
		// expunge/delete all existing messages on close
		for (FolderType f : imapRootFolderStructure.folders.keySet()) {
			try {
				Folder folder = imapRootFolderStructure.getFolder(f);
				folder.close(true);
			} catch (Exception e) {
				log.error("Failed to close folder " + f.toString() + ": "
						+ e.getMessage());
			}
		}

		super.close();
	}

	public MessageProcessor createMessageProcessor(Message message) {
		return new ImapMessageProcessor(message, imapRootFolderStructure);
	}
}
