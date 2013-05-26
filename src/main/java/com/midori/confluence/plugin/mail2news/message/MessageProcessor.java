package com.midori.confluence.plugin.mail2news.message;

/**
 * Processes a message
 * 
 * @author ckl
 */
public interface MessageProcessor {
	/**
	 * Will be executed before a new blog post message is loaded and processed
	 * 
	 * @throws MessageProcessorException
	 *             if any protocol exception occurs
	 * @return true if further processing is allowed
	 */
	boolean preProcess() throws MessageProcessorException;

	/**
	 * Will be executed afther the blog post message is processed and can now be
	 * deleted
	 * 
	 * @throws MessageProcessorException
	 *             if any protocol exception occurs
	 */
	void postProcess() throws MessageProcessorException;

	/**
	 * Marks the message as failed. Implementation details are specific to the
	 * protocol (moving to Trash, sending an error e-mail)
	 * 
	 * @param error
	 * @param exception
	 */
	void fail(String error, Exception e);
}
