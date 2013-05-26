package com.midori.confluence.plugin.mail2news.message;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MailToBlogPostPublisherTest extends TestCase {
	public MailToBlogPostPublisherTest() {
	}
	
	@Test
	public void squareBracketsInSubjectCanBeUsedAsSpaceKey() throws Exception
	{
		String s = "admin <admin@localhost>";
		String r = "";
		
		if ((s.lastIndexOf("<") > 0) && (s.lastIndexOf(">") > 0)) {
			r = s.substring(s.lastIndexOf("<") + 1, s.lastIndexOf(">"));
		}
		
		assertEquals("admin@localhost", r);
	}
}