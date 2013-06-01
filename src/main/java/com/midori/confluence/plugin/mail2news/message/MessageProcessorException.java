package com.midori.confluence.plugin.mail2news.message;

public class MessageProcessorException extends Exception {
	private static final long serialVersionUID = 3723385706203845417L;

	public MessageProcessorException(String message) {
		super(message);
	}

	public MessageProcessorException(String message, Throwable t) {
		super(message, t);
	}
}
