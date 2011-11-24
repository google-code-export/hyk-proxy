/**
 * 
 */
package org.hyk.proxy.gae.client.connection.http;

import java.util.LinkedList;
import java.util.List;

import org.arch.event.Event;

/**
 * @author qiyingwang
 *
 */
public class HTTPConnectionManager
{
	private List<Event> eventQueue = new LinkedList<Event>();
	private Object eventQueueLock = new Object();
	public void send(Event event)
	{
		synchronized (eventQueueLock)
        {
			eventQueue.add(event);
			eventQueueLock.notify();
        }
	}
}
