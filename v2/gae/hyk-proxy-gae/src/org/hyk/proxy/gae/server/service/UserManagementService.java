/**
 * 
 */
package org.hyk.proxy.gae.server.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.arch.buffer.Buffer;
import org.hyk.proxy.gae.common.auth.Group;
import org.hyk.proxy.gae.common.auth.User;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * @author qiyingwang
 * 
 */
public class UserManagementService
{
	private static CapabilitiesService capabilities = CapabilitiesServiceFactory
	        .getCapabilitiesService();
	private static DatastoreService datastore = DatastoreServiceFactory
	        .getDatastoreService();
	private static AsyncDatastoreService asyncdatastore = DatastoreServiceFactory
	        .getAsyncDatastoreService();
	protected static AsyncMemcacheService asyncCache = MemcacheServiceFactory
	        .getAsyncMemcacheService();
	protected static MemcacheService cache = MemcacheServiceFactory
	        .getMemcacheService();
	protected static Map<String, Object> localMemCache = new ConcurrentHashMap<String, Object>();

	public static void saveUser(User user)
	{
		Entity usrEntity = new Entity("User", user.getEmail());
		usrEntity.setProperty("name", user.getEmail());
		usrEntity.setProperty("passwd", user.getPasswd());
		usrEntity.setProperty("group", user.getGroup());
		usrEntity.setProperty("authtoken", user.getAuthToken());
		datastore.put(usrEntity);
		Buffer buffer = new Buffer(128);
		user.encode(buffer);
		byte[] content = buffer.toArray();
		asyncCache.put("User:" + user.getAuthToken(), content);
		asyncCache.put("User:" + user.getEmail(), content);
		localMemCache.put("User:" + user.getAuthToken(), user);
		localMemCache.put("User:" + user.getEmail(), user);
	}

	public static String verifyUser(String name, String passwd)
	{
		return null;
	}

	public static Group getGroup(String groupName)
	{
		String key = "Group:" + groupName;
		Group grp = (Group) localMemCache.get(key);
		if (null != grp)
		{
			return grp;
		}
		byte[] content = (byte[]) cache.get(key);
		if (null != content)
		{
			Buffer buf = Buffer.wrapReadableContent(content);
			grp = new Group();
			grp.decode(buf);
			localMemCache.put(key, grp);
			return grp;
		}
		Query q = new Query("Group");
		q.addFilter("name", Query.FilterOperator.EQUAL, groupName);
		// q.addFilter("height", Query.FilterOperator.LESS_THAN,
		// maxHeightParam);

		// PreparedQuery contains the methods for fetching query results
		// from the datastore
		PreparedQuery pq = datastore.prepare(q);

		for (Entity result : pq.asIterable())
		{
			grp = new Group();
			grp.setName((String) result.getKey().getName());
			Buffer buffer = new Buffer(128);
			grp.encode(buffer);
			content = buffer.toArray();
			asyncCache.put(key, content);
			localMemCache.put(key, grp);
		}
		return grp;
	}

	public static void saveGroup(Group group)
	{
		Entity usrEntity = new Entity("Group", group.getName());
		usrEntity.setProperty("blacklist", "");
		datastore.put(usrEntity);
		//datastore.
		Buffer buffer = new Buffer(128);
		group.encode(buffer);
		byte[] content = buffer.toArray();
		String key = "Group:" + group.getName();
		asyncCache.put(key, content);
		localMemCache.put(key, group);
	}

	private static User getUser(String name)
	{
		String key = "User:" + name;
		User user = (User) localMemCache.get(key);
		if (null != user)
		{
			return user;
		}
		byte[] content = (byte[]) cache.get(key);
		if (null != content)
		{
			Buffer buf = Buffer.wrapReadableContent(content);
			user = new User();
			user.decode(buf);
			localMemCache.put(key, user);
			return user;
		}
		Query q = new Query("User");
		q.addFilter("name", Query.FilterOperator.EQUAL, name);
		// q.addFilter("height", Query.FilterOperator.LESS_THAN,
		// maxHeightParam);

		// PreparedQuery contains the methods for fetching query results
		// from the datastore
		PreparedQuery pq = datastore.prepare(q);

		for (Entity result : pq.asIterable())
		{
			user = new User();
			user.setEmail((String) result.getKey().getName());
			Buffer buffer = new Buffer(128);
			user.encode(buffer);
			content = buffer.toArray();
			asyncCache.put(key, content);
			localMemCache.put(key, user);
		}
		return user;
	}
	
	public static User getUserWithName(String email)
    {
		return getUser(email);
    }
	public static User getUserWithToken(String token)
    {
		return getUser(token);
    }

	public static List<Group> getAllGroups()
    {
		List<Group> grps = new LinkedList<Group>();
		Query q = new Query("Group");
		PreparedQuery pq = datastore.prepare(q);

		for (Entity result : pq.asIterable())
		{
			Group grp = new Group();
			grp.setName((String) result.getKey().getName());
			Buffer buffer = new Buffer(128);
			grp.encode(buffer);
			byte[] content = buffer.toArray();
			String key = "Group:" + grp.getName();
			asyncCache.put(key, content);
			localMemCache.put(key, grp);
			grps.add(grp);
		}
		return grps;
    }

	public static List<User> getAllUsers()
    {
		List<User> users = new LinkedList<User>();
		Query q = new Query("User");
		PreparedQuery pq = datastore.prepare(q);

		for (Entity result : pq.asIterable())
		{
			User user = new User();
			user.setEmail((String) result.getKey().getName());
			Buffer buffer = new Buffer(128);
			user.encode(buffer);
			byte[] content = buffer.toArray();
			String key = "User:" + user.getEmail();
			String key1 = "User:" + user.getAuthToken();
			asyncCache.put(key, content);
			asyncCache.put(key1, content);
			localMemCache.put(key, user);
			localMemCache.put(key1, user);
			users.add(user);
		}
		return users;
    }

	public static void deleteGroup(Group g)
    {
		datastore.delete(KeyFactory.createKey("Group", g.getName()));
		String key = "Group:"+g.getName();
		localMemCache.remove(key);
		asyncCache.delete(key);
    }

	public static void deleteUser(User u)
    {
		datastore.delete(KeyFactory.createKey("User", u.getEmail()));
		String key = "User:"+u.getEmail();
		String key1 = "User:"+u.getAuthToken();
		localMemCache.remove(key);
		asyncCache.delete(key);
		localMemCache.remove(key1);
		asyncCache.delete(key1);
    }
	
	public static boolean userAuthServiceAvailable(String token)
	{
		String key = "User:" + token;
		if(localMemCache.containsKey(key))
		{
			return true;
		}
		CapabilityStatus status = capabilities.getStatus(Capability.MEMCACHE).getStatus();
		if(status == CapabilityStatus.DISABLED)
		{
			return false;
		}
		
		status = capabilities.getStatus(Capability.DATASTORE).getStatus();
		if(status == CapabilityStatus.DISABLED)
		{
			return false;
		}
		return true;
	}
}
