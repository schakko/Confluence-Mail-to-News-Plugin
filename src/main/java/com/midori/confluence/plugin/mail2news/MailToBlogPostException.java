package com.midori.confluence.plugin.mail2news;

public class MailToBlogPostException extends Exception {
	private static final long serialVersionUID = -6637948895751637455L;

	public MailToBlogPostException(String message) {
		super(message);
	}

	public MailToBlogPostException(String message, Throwable t) {
		super(message, t);
	}
}
