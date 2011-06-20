/**
 * 
 */
package com.hyk.rpc.core.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hyk.rpc.core.annotation.Remote;

/**
 * @author qiying.wang
 * 
 */
public class RemoteUtil {
	private static Map<Method, Integer> methodIDCache = new HashMap<Method, Integer>();

	public static Class[] getRemoteInterfaces(Class clazz) {
		Class[] interfaces = clazz.getInterfaces();
		List<Class> collect = new ArrayList<Class>();
		for (Class cls : interfaces) {
			if (cls.isAnnotationPresent(Remote.class)) {
				collect.add(cls);
			}
		}
		Class[] ret = new Class[collect.size()];
		return collect.toArray(ret);
	}

	public static Method getMethod(String methodID, Object obj) {
		Class[] inters = getRemoteInterfaces(obj.getClass());
		List<Method> methods = new ArrayList<Method>();
		for (int i = 0; i < inters.length; i++) {
			methods.addAll(Arrays.asList(inters[i].getMethods()));
		}
		for (Method m : methods) {
			if (m.getName().equals(methodID)) {
				return m;
			}
		}
		return null;
	}

	public static int getMethodID(Method method, Object obj) {
		if (methodIDCache.containsKey(method)) {
			return methodIDCache.get(method);
		}

		Class[] inters = getRemoteInterfaces(obj.getClass());
		List<Method> methods = new ArrayList<Method>();

		for (int i = 0; i < inters.length; i++) {
			methods.addAll(Arrays.asList(inters[i].getMethods()));
		}

		for (int i = 0; i < methods.size(); i++) {
			if (methods.get(i).equals(method)) {
				methodIDCache.put(method, i);
				return i;
			}
		}
		return -1;
	}
}
