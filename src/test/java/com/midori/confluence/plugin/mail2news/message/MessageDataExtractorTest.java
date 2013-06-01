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
public class MessageDataExtractorTest extends TestCase {
	public MessageDataExtractorTest() {
	}

	@Test
	public void textIsConvertedInConfluenceStorageFormat() throws Exception {
		Properties prop = new Properties();
		Session session = Session.getDefaultInstance(prop, null);

		String msgBody = "URL http://www.google.de\r\nNew line\r\n";

		try {
			Message msg = new MimeMessage(session);
			msg.setHeader("Content-Type",
					" text/plain; charset=ISO-8859-15; format=flowed");
			msg.setText(msgBody);

			MessageDataExtractor sut = new MessageDataExtractor(msg);

			assertEquals(
					"URL <a href=\"http://www.google.de\">http://www.google.de</a><br />New line",
					sut.getBodyInStorageFormat());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}