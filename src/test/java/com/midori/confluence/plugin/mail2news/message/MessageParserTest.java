package com.midori.confluence.plugin.mail2news.message;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageParserTest extends TestCase {
	public MessageParserTest() {
	}
	
	@Test
	public void squareBracketsInSubjectCanBeUsedAsSpaceKey() throws Exception
	{
		Message message = mock(Message.class);
		when(message.getSubject()).thenReturn("[spacekey] My subject line");
		MessageDataExtractor sut = new MessageDataExtractor(message);
		
		List<String> r = sut.getSpaceNameFromSubject();
		assertEquals(1, r.size());
		assertEquals("SPACEKEY", r.get(0));
	}
	
	@Test
	public void spaceKeyInSubjectLineSeparatedWithDoubleColon() throws Exception
	{
		Message message = mock(Message.class);
		when(message.getSubject()).thenReturn(" spacekey: My subject line");
		MessageDataExtractor sut = new MessageDataExtractor(message);
		
		List<String> r = sut.getSpaceNameFromSubject();
		assertEquals(1, r.size());
		assertEquals("SPACEKEY", r.get(0));
	}
	
	@Test
	public void recipientDelimiterCanBeUsedAsSpaceKey() throws Exception
	{
		Message message = mock(Message.class);
		InternetAddress[] addresses = new InternetAddress[1];
		addresses[0] = new InternetAddress("wiki+SPACEKEY@domain.tld");
		
		when(message.getRecipients(Message.RecipientType.TO)).thenReturn(addresses);
		MessageDataExtractor sut = new MessageDataExtractor(message);
		
		List<String> r = sut.getSpaceNamesFromRecipients();
		assertEquals(1, r.size());
		assertEquals("SPACEKEY", r.get(0));
	}
	
	@Test 
	public void usersToShareCanBeExtractedFromHeader() throws Exception
	{
		Message message = mock(Message.class);
		String[] shareableUsers = new String[] { "userA@domain.tld; userB@Domain.tld, ", "userC@domain.tld;", "userC@domain.tld" };
		
		when(message.getHeader(anyString())).thenReturn(shareableUsers);
		MessageDataExtractor sut = new MessageDataExtractor(message);
		
		List<String> r = sut.getUsersForSharing();
		assertEquals(3, r.size());
		assertEquals("usera@domain.tld", r.get(0));
		assertEquals("userb@domain.tld", r.get(1));
		assertEquals("userc@domain.tld", r.get(2));
	}
}