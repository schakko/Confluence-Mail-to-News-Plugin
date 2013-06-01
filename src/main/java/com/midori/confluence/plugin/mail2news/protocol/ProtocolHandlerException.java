package com.midori.confluence.plugin.mail2news.protocol;

public class ProtocolHandlerException extends Exception {
	private static final long serialVersionUID = 6581876754898514161L;

	public ProtocolHandlerException(String message) {
		super(message);
	}

	public ProtocolHandlerException(String message, Throwable t) {
		super(message, t);
	}
}
