package com.midori.confluence.plugin.mail2news.message;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.log4j.Logger;

import com.atlassian.confluence.pages.Attachment;

public class MessageContent {
	private final static Logger log = Logger.getLogger(MessageContent.class);

	/**
	 * The content of a message, this will be the content of the news entry
	 */
	private String content = "";

	/**
	 * A flag indicating whether the current post contains an image
	 */
	private boolean containsImage = false;

	/**
	 * Identified attachments inside {@link #message}
	 */
	Map<Attachment, ByteArrayInputStream> attachments = new HashMap<Attachment, ByteArrayInputStream>();

	/**
	 * Message object which contains the content
	 */
	private Message message;

	/**
	 * Whether the parsing of {@link #message} was successful or not
	 */
	private boolean isValid = false;

	/**
	 * After constructing this object, the message will be parsed
	 * 
	 * @param message
	 */
	public MessageContent(Message message) {
		this.message = message;
		parse();
	}

	protected void parse() {
		try {
			Object content = message.getContent();
			if (content instanceof Multipart) {
				handleMultipart((Multipart) content);
			} else {
				handlePart(message);
			}

			isValid = true;
		} catch (Exception e) {
			log.error("Failed to parse the content of the message: "
					+ e.getMessage());
		}
	}

	/**
	 * Returns whether the message content was parsed successfully or not
	 * 
	 * @return
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * Is any attachment an image?
	 * 
	 * @return
	 */
	public boolean containsImages() {
		return containsImage;
	}

	/**
	 * Retrieve any attachments and the corresponding input stream
	 * 
	 * @return
	 */
	public Map<Attachment, ByteArrayInputStream> getAttachments() {
		return attachments;
	}

	/**
	 * Returns the textutal representation of the message body
	 * 
	 * @return
	 */
	public String getText() {
		return content;
	}

	/**
	 * Handle a multipart of a email message. May recursively call
	 * handleMultipart or handlePart.
	 * 
	 * @param multipart
	 *            The multipart to handle.
	 * @throws MessagingException
	 * @throws IOException
	 */
	private void handleMultipart(Multipart multipart)
			throws MessagingException, IOException {
		for (int i = 0, n = multipart.getCount(); i < n; i++) {
			Part p = multipart.getBodyPart(i);
			if (p instanceof Multipart) {
				handleMultipart((Multipart) p);
			} else {
				handlePart(multipart.getBodyPart(i));
			}
		}
	}

	/**
	 * Handle a part of a email message. This is either displayable text or some
	 * MIME attachment.
	 * 
	 * @param part
	 *            The part to handle.
	 * @throws MessagingException
	 * @throws IOException
	 */
	private void handlePart(Part part) throws MessagingException, IOException {
		/* get the content type of this part */
		String contentType = part.getContentType();

		if (part.getContent() instanceof Multipart) {
			handleMultipart((Multipart) part.getContent());
			return;
		}

		log.debug("Content-Type: " + contentType);

		/* check if the content is printable */
		if (contentType.toLowerCase().startsWith("text/plain")
				&& content == null) {
			/* get the charset */
			Charset charset = getCharsetFromHeader(contentType);
			/* set the blog entry content to this content */
			content = "";
			InputStream is = part.getInputStream();
			BufferedReader br = null;
			if (charset != null) {
				br = new BufferedReader(new InputStreamReader(is, charset));
			} else {
				br = new BufferedReader(new InputStreamReader(is));
			}
			String currentLine = null;

			while ((currentLine = br.readLine()) != null) {
				content = content.concat(currentLine).concat("\r\n");
			}
		} else {
			/*
			 * the content is not text, so we assume it is some sort of MIME
			 * attachment
			 */

			try {
				/* get the filename */
				String fileName = part.getFileName();

				/* no filename, ignore this part */
				if (fileName == null) {
					this.log.warn("Attachment with no filename. Ignoring.");
					return;
				}

				/* retrieve an input stream to the attachment */
				InputStream is = part.getInputStream();

				/*
				 * clean-up the content type (only the part before the first ';'
				 * is relevant)
				 */
				if (contentType.indexOf(';') != -1) {
					contentType = contentType.substring(0,
							contentType.indexOf(';'));
				}

				if (contentType.toLowerCase().indexOf("image") != -1) {
					/*
					 * this post contains an image as attachment, add the
					 * gallery macro to the blog post
					 */
					containsImage = true;
				}

				ByteArrayInputStream bais = null;
				byte[] attachment = null;
				/* put the attachment into a byte array */
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte buf[] = new byte[1024];
					int numBytes;
					while (true) {
						numBytes = is.read(buf);
						if (numBytes > 0) {
							baos.write(buf, 0, numBytes);
						} else {
							/* end of stream reached */
							break;
						}
					}
					/* create a new input stream */
					attachment = baos.toByteArray();
					bais = new ByteArrayInputStream(attachment);
					// this.log.info("Attachment size: " +
					// attachment.length);
				} catch (Exception e) {
					this.log.error(
							"Could not load attachment:" + e.getMessage(), e);
					/* skip this attachment */
					throw e;
				}
				/* create a new attachment */
				Attachment a = new Attachment(fileName, contentType,
						attachment.length, "Attachment added by mail2news");
				Date d = new Date();
				a.setCreationDate(d);
				a.setLastModificationDate(d);

				/*
				 * add the attachment and the input stream to the attachment to
				 * the list of attachments of the current blog entry
				 */
				attachments.put(a, bais);
			} catch (Exception e) {
				this.log.error(
						"Error while saving attachment: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Get the charset listed in a "Content-Type" header.
	 * 
	 * @param contentType
	 *            The "Content-Type" header.
	 * @return Returns the used charset or null if no information is found.
	 */
	private Charset getCharsetFromHeader(String contentType) {

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
						log.warn("Unsupported charset in email content ("
								+ charsetString
								+ "). Some characters may be wrong.");
						return null;
					}
				}
			}
		}

		return null;
	}
}