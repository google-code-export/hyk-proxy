/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AccountServiceImpl.java 
 *
 * @author yinqiwen [ 2010-4-8 | ÏÂÎç07:51:42 ]
 *
 */
package com.hyk.proxy.server.gae.rpc.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.common.Constants;
import com.hyk.proxy.common.gae.auth.Group;
import com.hyk.proxy.common.gae.auth.Operation;
import com.hyk.proxy.common.gae.auth.User;
import com.hyk.proxy.common.rpc.service.AccountService;
import com.hyk.proxy.common.rpc.service.AuthRuntimeException;
import com.hyk.proxy.server.gae.config.XmlConfig;
import com.hyk.proxy.server.gae.rpc.remote.Reloadable;
import com.hyk.proxy.server.gae.util.ServerUtils;
import com.hyk.util.random.RandomUtil;

/**
 *
 */
public class AccountServiceImpl implements AccountService, Serializable, Reloadable
{
	protected transient Logger	logger;

	Group						group;
	User						user;

	public AccountServiceImpl(Group group, User user)
	{
		setUserAndGroup(group, user);
		init();
	}

	public void init()
	{
		logger = LoggerFactory.getLogger(getClass());
	}

	public void setUserAndGroup(Group group, User user)
	{
		this.group = group;
		this.user = user;
	}

	protected String assertRootAuth()
	{
		if(!user.getEmail().equals(Constants.ROOT_NAME))
		{
			return Constants.AUTH_FAILED;
		}
		return null;
	}

	protected void sendAccountMail(String to, String passwd, boolean isCreate)
	{
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		try
		{
			Message msg = new MimeMessage(session);
			StringBuffer buffer = new StringBuffer();
			buffer.append("Hi, ").append(to).append("\r\n\r\n");
			if(isCreate)
			{
				buffer.append("You account on ").append(XmlConfig.getInstance().getAppId() + ".appspot.com").append(" has been created.").append(
						"\r\n");
				buffer.append("    Username:").append(to).append("\r\n");
				buffer.append("    Password:").append(passwd).append("\r\n");
				msg.setSubject("Your account has been activated");
			}
			else
			{
				msg.setSubject("Your account has been deleted.");
				buffer.append("You account on ").append(XmlConfig.getInstance().getAppId() + ".appspot.com").append(" has been deleted.").append(
						"\r\n");
			}

			buffer.append("Thanks again for registering, admin@" + XmlConfig.getInstance().getAppId() + ".appspot.com");
			String msgBody = buffer.toString();

			msg.setFrom(new InternetAddress("admin@" + XmlConfig.getInstance().getAppId() + ".appspotmail.com"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to, "Mr. User"));

			// msg.set
			msg.setText(msgBody);
			Transport.send(msg);
		}
		catch(Exception e)
		{
			logger.error("Failed to send mail to user:" + to, e);
		}
	}

	protected static boolean createGroupIfNotExist(String groupName)
	{
		Group group = ServerUtils.getGroup(groupName);
		if(null == group)
		{
			group = new Group();
			group.setName(groupName);
			ServerUtils.storeObject(group);
			return false;
		}
		return true;
	}

	protected static boolean createUserIfNotExist(String email, String groupName)
	{
		User user = ServerUtils.getUser(email);
		if(null == user)
		{
			user = new User();
			user.setEmail(email);
			user.setGroup(groupName);
			if(email.equals(Constants.ANONYMOUSE_NAME))
			{
				user.setPasswd(Constants.ANONYMOUSE_NAME);
			}
			else
			{
				user.setPasswd(RandomUtil.generateRandomString(8));
			}
			ServerUtils.storeObject(user);
		}
		return true;
	}

	/**
	 * Create it if it not exist
	 */
	public static void checkDefaultAccount()
	{
		createGroupIfNotExist(Constants.ROOT_GROUP_NAME);
		createUserIfNotExist(Constants.ROOT_NAME, Constants.ROOT_GROUP_NAME);
		createGroupIfNotExist(Constants.PUBLIC_GROUP_NAME);
		createGroupIfNotExist(Constants.ANONYMOUSE_NAME);
		createUserIfNotExist(Constants.ANONYMOUSE_NAME, Constants.ANONYMOUSE_GROUP_NAME);

	}

	public static User getRootUser()
	{
		return ServerUtils.getUser(Constants.ROOT_NAME);
	}

	@Override
	public String createGroup(String groupname)
	{
		if(assertRootAuth() != null)
		{
			return assertRootAuth();
		}
		Group g = ServerUtils.getGroup(groupname);
		if(null != g)
		{
			return Constants.GRP_EXIST;
		}
		g = new Group();
		g.setName(groupname);
		ServerUtils.storeObject(g);
		// ServerUtils.cacheGroup(g);
		return null;
	}

	@Override
	public String createUser(String username, String groupname, String passwd)
	{
		if(assertRootAuth() != null)
		{
			return assertRootAuth();
		}
		try
		{
			Group g = ServerUtils.getGroup(groupname);
			if(null == g)
			{
				return Constants.GRP_NOTFOUND;
			}
			User u = ServerUtils.getUser(username);
			if(null != u)
			{
				return Constants.USER_EXIST;
			}
			Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
			Matcher m = p.matcher(username);
			boolean matchFound = m.matches();
			if(!matchFound)
			{
				return "Username MUST be a email address!";
			}
			u = new User();
			u.setEmail(username);
			u.setGroup(groupname);
			u.setPasswd(passwd);
			ServerUtils.storeObject(u);
			// ServerUtils.cacheUser(u);
			sendAccountMail(username, passwd, true);
		}
		catch(Throwable e)
		{
			logger.error("Failed to create user.", e);
			return "Failed to create user." + e.getMessage();
		}

		return null;
	}

	@Override
	public String modifyPassword(String username, String oldPass, String newPass)
	{
		if(user.getEmail().equals(Constants.ANONYMOUSE_NAME))
		{
			return "Can't modify anonymous user's password!";
		}
		if(user.getEmail().equals(Constants.ROOT_NAME) || user.getEmail().equals(username))
		{
			User modifyuser = ServerUtils.getUser(username);
			if(null == modifyuser)
			{
				return Constants.USER_NOTFOUND;
			}
			if(!user.getEmail().equals(Constants.ROOT_NAME))
			{
				if(!modifyuser.getPasswd().equals(oldPass))
				{
					return Constants.PASS_NOT_MATCH;
				}
			}
			if(null == newPass)
			{
				return "New password can't be empty!";
			}
			modifyuser.setPasswd(newPass);
			ServerUtils.storeObject(modifyuser);
			return null;
		}
		return Constants.AUTH_FAILED;
	}

	@Override
	public List<Group> getGroupsInfo()
	{
		if(assertRootAuth() != null)
		{
			throw new AuthRuntimeException(assertRootAuth());
		}
		List<Group> groups = ServerUtils.getAllGroups();
		return groups;

	}

	@Override
	public List<User> getUsersInfo()
	{
		if(assertRootAuth() != null)
		{
			throw new AuthRuntimeException(assertRootAuth());
		}

		List<User> users = ServerUtils.getAllUsers();
		return users;

	}

	@Override
	public String deleteGroup(String groupname)
	{
		if(assertRootAuth() != null)
		{
			return assertRootAuth();
		}

		if(groupname.equals(Constants.ROOT_NAME) || groupname.equals(Constants.ANONYMOUSE_NAME) || groupname.equals(Constants.PUBLIC_GROUP_NAME))
		{
			return "Deletion not allowed!";
		}

		Group g = ServerUtils.getGroup(groupname);
		if(null == g)
		{
			return Constants.GRP_NOTFOUND;
		}
		ServerUtils.deleteObject(g);

		return null;

	}

	@Override
	public String deleteUser(String username)
	{
		if(assertRootAuth() != null)
		{
			return assertRootAuth();
		}

		if(username.equals(Constants.ROOT_NAME) || username.equals(Constants.ANONYMOUSE_NAME))
		{
			return "Deletion not allowed!";
		}
		try
		{
			User u = ServerUtils.getUser(username);
			if(null == u)
			{
				return Constants.USER_NOTFOUND;
			}
			// ServerUtils.removeUserCache(u);
			sendAccountMail(u.getEmail(), u.getPasswd(), false);
			// pm.deletePersistent(u);
			ServerUtils.deleteObject(u);
			return null;
		}
		catch(Throwable e)
		{
			logger.error("Failed to delete user.", e);
			return "Failed to delete user." + e.getMessage();
		}
	}

	@Override
	public String operationOnGroupBlackList(String groupname, String host, Operation operation)
	{
		if(assertRootAuth() != null)
		{
			return assertRootAuth();
		}

		Group g = ServerUtils.getGroup(groupname);
		if(null == g)
		{
			return Constants.GRP_NOTFOUND;
		}
		Set<String> blacklist = g.getBlacklist();
		if(null == blacklist)
		{
			blacklist = new HashSet<String>();
		}
		switch(operation)
		{
			case ADD:
			{
				blacklist.add(host);
				break;
			}
			case DELETE:
			{
				blacklist.remove(host);
				break;
			}
		}
		g.setBlacklist(blacklist);
		ServerUtils.storeObject(g);
		return null;
	}

	@Override
	public String operationOnUserBlackList(String username, String host, Operation operation)
	{
		if(assertRootAuth() != null)
		{
			return assertRootAuth();
		}

		User u = ServerUtils.getUser(username);
		if(null == u)
		{
			return Constants.USER_NOTFOUND;
		}
		Set<String> blacklist = u.getBlacklist();
		if(null == blacklist)
		{
			blacklist = new HashSet<String>();
		}
		switch(operation)
		{
			case ADD:
			{
				blacklist.add(host);
				break;
			}
			case DELETE:
			{
				blacklist.remove(host);
				break;
			}
		}
		u.setBlacklist(blacklist);
		ServerUtils.storeObject(u);
		return null;

	}

	@Override
	public String operationOnUserTraffic(String username, String host, int trafficRestriction)
	{
		if(assertRootAuth() != null)
		{
			return assertRootAuth();
		}
		User u = ServerUtils.getUser(username);
		if(null == u)
		{
			return Constants.USER_NOTFOUND;
		}
		try
		{
			Map<String, Integer> restrictionTable = u.getTrafficRestrictionTable();
			if(null == restrictionTable)
			{
				restrictionTable = new HashMap<String, Integer>();
				u.setTrafficRestrictionTable(restrictionTable);
			}
			restrictionTable.put(host, trafficRestriction);
			ServerUtils.storeObject(u);
		}
		catch(Throwable e)
		{
			logger.error("Failed to traffic.", e);
			return "Failed to delete user." + e.getMessage();
		}
		return null;
	}
}
