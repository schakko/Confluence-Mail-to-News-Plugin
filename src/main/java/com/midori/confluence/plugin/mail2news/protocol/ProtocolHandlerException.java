package com.midori.confluence.plugin.mail2news.protocol;

public class ProtocolHandlerException extends Exception {
	public ProtocolHandlerException(String message) {
		super(message);
	}

	public ProtocolHandlerException(String message, Throwable t) {
		super(message, t);
	}
}
