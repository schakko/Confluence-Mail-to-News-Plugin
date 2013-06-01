package com.midori.confluence.plugin.mail2news.protocol.imap;

import javax.mail.Message;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.midori.confluence.plugin.mail2news.config.MailConfiguration;
import com.midori.confluence.plugin.mail2news.protocol.ProtocolHandler;
import com.midori.confluence.plugin.mail2news.protocol.ProtocolHandlerFactory;

@RunWith(MockitoJUnitRunner.class)
public class ImapProtocolHandlerTest extends TestCase {

	@Test
	public void mailsFromImapInboxesCanBeFetched() throws Exception
	{
		GreenMail server = new GreenMail();
		server.start();
		server.setUser("test@localhost", "user", "password");
		
		MailConfiguration config = new MailConfiguration();
		config.setServer("localhost");
		config.setUsername("user");
		config.setPassword("password");
		config.setProtocol("imap");
		config.setPort(server.getImap().getPort());
		ProtocolHandlerFactory factory = new ProtocolHandlerFactory();
		ProtocolHandler handler = factory.create(config);
		GreenMailUtil.sendTextEmailTest("test@localhost", "bla@localhost", "wiki+SPACEKEY@domain.tld", "Check this link out..");
		assertTrue(handler instanceof ImapProtocolHandler);
		
		try {
			handler.connect();
			Message[] m = handler.getMessages();
			assertEquals(1, m.length);
			//assertEquals("Check this link out..", m[0].getContent());
			handler.close();
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
		finally
		{
			handler.close();
			server.stop();
		}
	}
}
