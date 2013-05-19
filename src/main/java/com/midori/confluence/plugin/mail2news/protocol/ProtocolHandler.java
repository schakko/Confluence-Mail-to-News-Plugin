package com.midori.confluence.plugin.mail2news.protocol;

import javax.mail.Message;

import com.midori.confluence.plugin.mail2news.message.MessageProcessor;

public interface ProtocolHandler {
	void connect() throws ProtocolHandlerException;
	void close() throws ProtocolHandlerException;
	Message[] getMessages() throws ProtocolHandlerException;
	MessageProcessor createMessageProcessor(Message message);
}
