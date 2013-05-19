package com.midori.confluence.plugin.mail2news.message;

public class MessageProcessorException extends Exception {
	public MessageProcessorException(String message) {
		super(message);
	}

	public MessageProcessorException(String message, Throwable t) {
		super(message, t);
	}
}
