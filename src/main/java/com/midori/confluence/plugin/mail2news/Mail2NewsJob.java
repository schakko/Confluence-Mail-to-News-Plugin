/**
 * The mail2news confluence plugin, job module.
 * This is the job which is periodically executed to
 * check for new email messages in a specified account
 * and add them to the news of a space.
 *
 * This software is licensed under the BSD license.
 *
 * Copyright (c) 2008, Liip AG
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of Liip AG nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author   Roman Schlegel <roman@liip.ch>
 * @version  $Id$
 * @package  com.midori.confluence.plugin.mail2news.mail2news
 */

package com.midori.confluence.plugin.mail2news;

import javax.mail.Message;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.atlassian.confluence.plugins.sharepage.api.SharePageService;
import com.atlassian.quartz.jobs.AbstractJob;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.spring.container.ContainerManager;
import com.midori.confluence.plugin.mail2news.message.MessageProcessor;
import com.midori.confluence.plugin.mail2news.protocol.ProtocolHandler;
import com.midori.confluence.plugin.mail2news.protocol.ProtocolHandlerFactory;

public class Mail2NewsJob extends AbstractJob {

	/**
	 * The log to which we will be logging infos and errors.
	 */
	private final static Logger log = Logger.getLogger(Mail2NewsJob.class);

	/**
	 * The configuration manager of this plugin which contains the settings of
	 * this plugin (e.g. login credentials for the email account which is
	 * monitored).
	 */
	private ConfigurationManager configurationManager;

	private SharePageService sharePageService;

	/**
	 * The default constructor. Autowires this component and creates a new
	 * configuration manager.
	 */
	public Mail2NewsJob(SharePageService sharePageService) {
		// bind manually b/c of OSGi plug-in
		this.sharePageService = sharePageService;

		/*
		 * autowire this component (this means that the space and page manager
		 * are automatically set by confluence
		 */
		ContainerManager.autowireComponent(this);

		/* create the configuration manager */
		this.configurationManager = new ConfigurationManager();

	}

	/**
	 * The main method of this job. Called by confluence every time the
	 * mail2news trigger fires.
	 * 
	 * @see com.atlassian.quartz.jobs.AbstractJob#doExecute(org.quartz.JobExecutionContext)
	 */
	public void doExecute(JobExecutionContext arg0)
			throws JobExecutionException {

		if (null == this.getSharePageService()) {
			throw new JobExecutionException("SharePageService was not bound");
		}

		/* The mailstore object used to connect to the server */
		ProtocolHandler protocolHandler = null;
		MessageProcessor messageProcessor = null;

		try {
			log.info("Executing mail2news plugin.");

			/* get the mail configuration from the manager */
			MailConfiguration config = configurationManager
					.getMailConfiguration();

			if (config == null) {
				throw new Exception("Null MailConfiguration instance.");
			}

			ProtocolHandlerFactory factory = new ProtocolHandlerFactory();
			protocolHandler = factory.create(config);

			protocolHandler.connect();

			Message[] messages = protocolHandler.getMessages();

			for (Message message : messages) {
				messageProcessor = protocolHandler
						.createMessageProcessor(message);
				try {
					if (!messageProcessor.preProcess()) {
						// processing failed, check next message
						continue;
					}

					MailToBlogPostPublisher publisher = new MailToBlogPostPublisher(
							message);
					// OSGi, Autowiring does not work
					publisher.setSharePageService(getSharePageService());

					publisher.publish(config);

					messageProcessor.postProcess();
				} catch (Exception e) {
					messageProcessor.fail(e.getMessage(), e);
				}
			}

			log.info("Closing mail inbox connection");
			protocolHandler.close();

		} catch (Exception e) {
			/* catch any exception which was not handled so far */
			log.error("Error while executing mail2news job: " + e.getMessage(),
					e);
			JobExecutionException jee = new JobExecutionException(
					"Error while executing mail2news job: " + e.getMessage(),
					e, false);
			throw jee;
		}
	}

	public SharePageService getSharePageService() {
		return sharePageService;
	}

	public void setSharePageService(SharePageService sharePageService) {
		this.sharePageService = sharePageService;
	}
}
