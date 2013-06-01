package com.midori.confluence.plugin.mail2news.message;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessageContentTest extends TestCase {
	public MessageContentTest() {
	}
	
	@Test
	public void textPlainCanBeExtracted() throws Exception
	{
		Properties prop = new Properties();
		Session session = Session.getDefaultInstance(prop, null);
		
		String msgBody = "Hallo Welt http://www.google.de";
		
		try {
			Message msg = new MimeMessage(session);
			msg.setHeader("Content-Type", " text/plain; charset=ISO-8859-15; format=flowed");
			msg.setText(msgBody);
			
			MessageContent mc = new MessageContent(msg);
			
			assertEquals(msgBody, mc.getText());
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}
}