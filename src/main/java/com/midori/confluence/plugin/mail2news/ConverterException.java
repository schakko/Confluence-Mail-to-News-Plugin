package com.midori.confluence.plugin.mail2news;

public class ConverterException extends Exception {
	public ConverterException(String message) {
		super(message);
	}

	public ConverterException(String message, Throwable t) {
		super(message, t);
	}
}
