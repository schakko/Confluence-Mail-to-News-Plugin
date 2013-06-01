package com.midori.confluence.plugin.mail2news.protocol;

import com.midori.confluence.plugin.mail2news.config.MailConfiguration;
import com.midori.confluence.plugin.mail2news.protocol.imap.ImapProtocolHandler;
import com.midori.confluence.plugin.mail2news.protocol.pop3.Pop3ProtocolHandler;

public class ProtocolHandlerFactory {
	public ProtocolHandler create(MailConfiguration configuration)
			throws ProtocolHandlerException {
		String name = configuration.getProtocol().toLowerCase();

		if (null == name) {
			throw new ProtocolHandlerException(
					"No protocol defined in configuration");
		}

		if ("pop3".equals(name)) {
			return new Pop3ProtocolHandler();
		} else if ("imap".equals(name)) {
			return new ImapProtocolHandler(configuration);
		}

		throw new ProtocolHandlerException("The defined protocol handler '"
				+ name + "' is not defined");
	}
}
