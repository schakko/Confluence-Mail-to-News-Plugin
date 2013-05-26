package com.midori.confluence.plugin.mail2news;

import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.mail.Message;

import org.apache.log4j.Logger;

import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.plugins.sharepage.api.SharePageService;
import com.atlassian.confluence.plugins.sharepage.api.ShareRequest;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.core.filters.ServletContextThreadLocal;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.User;
import com.atlassian.user.search.SearchResult;
import com.midori.confluence.plugin.mail2news.message.MessageDataExtractor;

public class MailToBlogPostPublisher {
	protected final static Logger log = Logger
			.getLogger(MailToBlogPostPublisher.class);

	private Message message;
	private MessageDataExtractor extractor;

	/**
	 * The space manager of this Confluence instance
	 */
	private SpaceManager spaceManager;

	private PageManager pageManager;

	/**
	 * The user accessor of this Confluence instance, used to find users.
	 */
	private UserAccessor userAccessor;

	private SharePageService sharePageService;

	/**
	 * The attachment manager of this Confluence instance
	 */
	private AttachmentManager attachmentManager;

	public MailToBlogPostPublisher(Message message) {
		ContainerManager.autowireComponent(this);
		this.setMessage(message);
		this.setExtractor(new MessageDataExtractor(message));
	}

	public void publish(MailConfiguration configuration)
			throws ConverterException {
		Space spaceToPublish = getSpace();

		User user = mapSenderOfMessageToLocalUser(getExtractor().getSenders());

		BlogPost post = publishMessageContentAsBlogPost(getExtractor(),
				spaceToPublish, user, configuration);

		if (configuration.isShareWithOthers()) {
			sharePost(post.getId(), user, getExtractor().getUsersForSharing());
		}
	}

	/**
	 * Shares the post with the given id with users
	 * 
	 * @param id
	 * @param sender
	 * @param users
	 */
	protected void sharePost(long id, User sender, List<String> usernamesOrEmailAddresses) {
		HashSet<String> users = new HashSet<String>();
		User u = null;
		
		if (null == sender) {
			log.error("Not sharing post, could not identify the user which sends this blog post");
			return;
		}
		
		for (String usernameOrEmailAddress : usernamesOrEmailAddresses) {
			u = resolveUserFromAddress(usernameOrEmailAddress);
			
			if (null != u) {
				users.add(u.getName());
			}
		}
		
		AuthenticatedUserThreadLocal.setUser(sender);

		ShareRequest request = new ShareRequest();
		request.setEntityId(id);
		request.setUsers(users);
		// null values not allowed
		request.setEmails(new HashSet<String>());

		// using setRequest() is the only possibility to make this plug-in working. Hopfully, Atlassian won't remove the API method.
		ServletContextThreadLocal.setRequest(new MockHttpServletRequest());

		try {
			getSharePageService().share(request);
		} catch (Exception e) {
			log.error("Failed to share page: " + e.getMessage());
		}
	}

	/**
	 * Publishes the given e-mail as blog post
	 * 
	 * @param extractor
	 * @param space
	 * @param createdBy
	 * @throws ConverterException
	 *             If the referenced e-mail could not be parsed
	 * @return the recently created blog post
	 */
	protected BlogPost publishMessageContentAsBlogPost(
			MessageDataExtractor extractor, Space space, User createdBy,
			MailConfiguration configuration) throws ConverterException {
		if (!extractor.getContent().isValid()) {
			throw new ConverterException("E-Mail could not be parsed");
		}
		
		/* create the blogPost and add values */
		BlogPost blogPost = new BlogPost();
		/* set the creation date of the blog post to the current date */
		blogPost.setCreationDate(new Date());
		/* set the space where to save the blog post */
		blogPost.setSpace(space);

		String content = extractor.getContent().getText();

		/*
		 * if the gallery macro is set and the post contains an image add the
		 * macro
		 */
		if (configuration.getGallerymacro()) {
			/* gallery macro is set */
			if (extractor.getContent().containsImages()) {
				content = content.concat("{gallery}");
			}
		}

		/* set the blog post content */
		if (content != null) {
			blogPost.setBodyAsString(content);
		} else {
			blogPost.setBodyAsString("");
		}

		/* set the title of the blog post */
		String title = extractor.getTitle(space.getKey());

		/*
		 * check for illegal characters in the title and replace them with a
		 * space
		 */
		/* could be replaced with a regex */
		/* Only needed for Confluence < 4.1 */
		String version = GeneralUtil.getVersionNumber();
		// TODO really required? as page-share plug-in is required, this check
		// should be unnecessary
		if (!Pattern.matches("^4\\.[1-9]+.*$", version)) {
			char[] illegalCharacters = { ':', '@', '/', '%', '\\', '&', '!',
					'|', '#', '$', '*', ';', '~', '[', ']', '(', ')', '{', '}',
					'<', '>', '.' };
			for (int i = 0; i < illegalCharacters.length; i++) {
				if (title.indexOf(illegalCharacters[i]) != -1) {
					title = title.replace(illegalCharacters[i], ' ');
				}
			}
		}

		blogPost.setTitle(title);

		String creatorName = "Anonymous";

		if (null != createdBy) {
			creatorName = createdBy.getName();
		}

		blogPost.setCreatorName(creatorName);
		AuthenticatedUserThreadLocal.setUser(createdBy);

		/* save the blog post */
		getPageManager().saveContentEntity(blogPost, null);

		/*
		 * we have to save the blog post before we can add the attachments,
		 * because attachments need to be attached to a content.
		 */
		for (Attachment attachment : extractor.getContent().getAttachments()
				.keySet()) {
			InputStream is = (InputStream) extractor.getContent()
					.getAttachments().get(attachment);
			attachment.setCreatorName(creatorName);

			try {
				getAttachmentManager().saveAttachment(attachment, null, is);

				/* add the attachment to the blog post */
				blogPost.addAttachment(attachment);
			} catch (Exception e) {
				log.error("Failed to save/add attachment to blog post: "
						+ e.getMessage());
			}
		}

		log.info("Blog post \"" + title + "\" published in space "
				+ space.getName());

		return blogPost;
	}

	/**
	 * Checks the detected spaces from the message and tries to resolve the
	 * space keys to a local space instance.
	 * 
	 * @return
	 * @throws ConverterException
	 */
	protected Space getSpace() throws ConverterException {
		List<String> spaces = getExtractor().getSpaceNames();
		Space space = null;

		// check public available spaces
		for (String spaceCandidate : spaces) {
			log.debug("Checking if space key " + spaceCandidate
					+ " is available");

			space = getSpaceManager().getSpace(spaceCandidate);

			if (space == null) {
				// fall back to look up a personal space
				space = getSpaceManager().getPersonalSpace(spaceCandidate);
			}

			if (space != null) {
				log.info("Space " + spaceCandidate
						+ " is valid; using this for publishing");
				return space;
			}
		}

		// fallback to personal spaces. The personal space is identified by the
		// the sender addresses
		User user = mapSenderOfMessageToLocalUser(getExtractor().getSenders());

		if (null != user) {
			String username = user.getName();
			log.info("Fallback to personal space, using the personal space of '"
					+ username + "' for publishing");
			space = getSpaceManager().getPersonalSpace(username);

			if (null != space) {
				log.info("Using personal space of user '" + username + "'");
				return space;
			}
		}

		throw new ConverterException(
				"None of the potential space key candidates were available nor could the personal space of the sender be identified");
	}

	/**
	 * Parses the sender(s) of the message and tries to map them to a local
	 * Confluence user account. First successful mapping result will be
	 * returned.
	 * 
	 * @return
	 */
	public User mapSenderOfMessageToLocalUser(List<String> senders) {
		User r = null;

		for (String sender : senders) {
			r = resolveUserFromAddress(sender);

			if (null != r) {
				break;
			}
		}

		if (null != r) {
			log.debug("Sender of mail resolved to " + r.getFullName());
		} else {
			log.error("Could not resolve sender of E-mail");
		}

		return r;
	}

	/**
	 * Resolves an e-mail address to a local user instance. Detects:
	 * <ul>
	 * <li>"admin <admin@localhost>" -&gt; admin</li>
	 * <li>"admin@localhost" -&gt; admin</li>
	 * <li>"admin" -&gt; admin</li>
	 * </ul>
	 * 
	 * @param given
	 *            format
	 * @return
	 */
	public User resolveUserFromAddress(String usernamesOrEmail) {
		String extractedEmailAddress = usernamesOrEmail;

		if ((extractedEmailAddress.lastIndexOf("<") > 0)
				&& (extractedEmailAddress.lastIndexOf(">") > 0)) {
			extractedEmailAddress = usernamesOrEmail.substring(
					usernamesOrEmail.lastIndexOf("<") + 1,
					usernamesOrEmail.lastIndexOf(">"));
		}

		@SuppressWarnings("unchecked")
		SearchResult<User> results = getUserAccessor().getUsersByEmail(
				extractedEmailAddress);
		List<User> firstPage = results.pager().getCurrentPage();

		if (firstPage.size() > 0) {
			return firstPage.get(0);
		}

		// fallback to username search
		extractedEmailAddress = extractedEmailAddress.substring(0,
				extractedEmailAddress.indexOf("@"));

		if (extractedEmailAddress.length() > 0) {
			User r = getUserAccessor().getUser(extractedEmailAddress);

			if (null != r) {
				return r;
			}
		}

		return null;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public MessageDataExtractor getExtractor() {
		return extractor;
	}

	public void setExtractor(MessageDataExtractor extractor) {
		this.extractor = extractor;
	}

	public SpaceManager getSpaceManager() {
		return spaceManager;
	}

	public void setSpaceManager(SpaceManager spaceManager) {
		this.spaceManager = spaceManager;
	}

	public PageManager getPageManager() {
		return pageManager;
	}

	public void setPageManager(PageManager pageManager) {
		this.pageManager = pageManager;
	}

	public UserAccessor getUserAccessor() {
		return userAccessor;
	}

	public void setUserAccessor(UserAccessor userAccessor) {
		this.userAccessor = userAccessor;
	}

	public SharePageService getSharePageService() {
		return sharePageService;
	}

	public void setSharePageService(SharePageService sharePageService) {
		this.sharePageService = sharePageService;
	}

	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
}
