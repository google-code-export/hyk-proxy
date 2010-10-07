/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: EMailUtil.java 
 *
 * @author yinqiwen [ 2010-8-30 | ÏÂÎç10:59:45 ]
 *
 */
package com.hyk.proxy.server.gae.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.server.gae.config.XmlConfig;

/**
 *
 */
public class EMailUtil
{
	protected static Logger				logger		= LoggerFactory.getLogger(EMailUtil.class);
	private static final String ADMIN  = "yinqiwen@gmail.com";
	public static void sendMail(String toAddress, String subject, String content)
	{
		try
		{
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
			Message msg = new MimeMessage(session);
			StringBuffer buffer = new StringBuffer();
			buffer.append("Hi, ").append("\r\n\r\n");
			buffer.append(content);

			buffer.append("Thanks again. admin@" + XmlConfig.getInstance().getAppId() + ".appspot.com");
			String msgBody = buffer.toString();
			msg.setSubject(subject);
			msg.setFrom(new InternetAddress("admin@" + XmlConfig.getInstance().getAppId() + ".appspotmail.com"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress, "Mr/Ms. User"));
			msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(ADMIN, "Mr. Admin"));
			// msg.set
			msg.setText(msgBody);
			Transport.send(msg);
		}
		catch(Exception e)
		{
			logger.error("Failed to send mail to user:" + toAddress, e);
		}
	}
}
