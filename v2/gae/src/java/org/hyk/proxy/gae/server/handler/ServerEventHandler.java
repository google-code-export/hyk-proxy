/**
 * 
 */
package org.hyk.proxy.gae.server.handler;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventDispatcher;
import org.arch.event.EventHandler;
import org.arch.event.EventHeader;
import org.arch.event.EventSegment;
import org.arch.event.TypeVersion;
import org.arch.event.http.HTTPEventContants;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.http.HTTPResponseEvent;
import org.hyk.proxy.gae.common.CacheService;
import org.hyk.proxy.gae.common.CompressorType;
import org.hyk.proxy.gae.common.EventHeaderTags;
import org.hyk.proxy.gae.common.GAEConstants;
import org.hyk.proxy.gae.common.GAEEventHelper;
import org.hyk.proxy.gae.common.config.GAEServerConfiguration;
import org.hyk.proxy.gae.common.event.AdminResponseEvent;
import org.hyk.proxy.gae.common.event.CompressEvent;
import org.hyk.proxy.gae.common.event.EncryptEvent;
import org.hyk.proxy.gae.common.event.ServerConfigEvent;
import org.hyk.proxy.gae.server.service.EventSendService;
import org.hyk.proxy.gae.server.service.ServerConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

/**
 * @author qiyingwang
 * 
 */
public class ServerEventHandler implements EventHandler
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected URLFetchService urlFetchService = URLFetchServiceFactory
	        .getURLFetchService();

	protected AsyncMemcacheService asyncCache = MemcacheServiceFactory
	        .getAsyncMemcacheService();
	protected MemcacheService cache = MemcacheServiceFactory
	        .getMemcacheService();
	protected AccountServiceHandler accountHandler = new AccountServiceHandler();
	protected FetchServiceHandler fetchHandler = new FetchServiceHandler();

	private Event handleRecvEvent(EventHeaderTags tags, Event event)
	{
		Event response = null;
		TypeVersion tv = Event.getTypeVersion(event.getClass());
		if (null == tv)
		{
			logger.error("Failed to find registry type&version for class:"
			        + event.getClass().getName());
		}
		int type = tv.type;
		switch (type)
		{
			case GAEConstants.AUTH_REQUEST_EVENT_TYPE:
			case GAEConstants.USER_OPERATION_EVENT_TYPE:
			case GAEConstants.GROUP_OPERATION_EVENT_TYPE:
			case GAEConstants.USER_LIST_REQUEST_EVENT_TYPE:
			case GAEConstants.GROUOP_LIST_REQUEST_EVENT_TYPE:
			case GAEConstants.USER_LIST_RESPONSE_EVENT_TYPE:
			case GAEConstants.GROUOP_LIST_RESPONSE_EVENT_TYPE:
			case GAEConstants.BLACKLIST_OPERATION_EVENT_TYPE:
			{
				response = accountHandler.handleEvent(type, event);
				break;
			}
			case HTTPEventContants.HTTP_REQUEST_EVENT_TYPE:
			{
				response = fetchHandler.fetch((HTTPRequestEvent) event);
				break;
			}
			case Event.RESERVED_SEGMENT_EVENT_TYPE:
			{
				EventSegment segment = (EventSegment) event;
				Buffer content = GAEEventHelper.mergeEventSegment(segment,
				        new CacheService()
				        {
					        @Override
					        public Object put(Object key, Object value)
					        {
						        // TODO Auto-generated method stub
						        return asyncCache.put("Segment:" + key, value,
						                Expiration.byDeltaSeconds(60));
					        }

					        @Override
					        public Object get(Object key)
					        {
						        return cache.get("Segment:" + key);
					        }
				        });
				if (null != content)
				{
					Event ev;
					try
					{
						ev = EventDispatcher.getSingletonInstance().parse(
						        content);
						response = handleRecvEvent(tags, ev);
					}
					catch (Exception e)
					{
						logger.error("Failed to handle segment event.", e);
					}
				}
				break;
			}
			case GAEConstants.COMPRESS_EVENT_TYPE:
			{
				((CompressEvent) event).ev.setAttachment(event.getAttachment());
				response = handleRecvEvent(tags, ((CompressEvent) event).ev);
				break;
			}
			case GAEConstants.ENCRYPT_EVENT_TYPE:
			{
				((EncryptEvent) event).ev.setAttachment(event.getAttachment());
				response = handleRecvEvent(tags, ((EncryptEvent) event).ev);
				break;
			}
			case GAEConstants.SERVER_CONFIG_EVENT_TYPE:
			{
				ServerConfigEvent ev = (ServerConfigEvent) event;
				response = ServerConfigurationService.handleServerConfig(tags,ev);
				break;
			}
			default:
			{
				logger.error("Unsupported event type:" + type);
				break;
			}
		}
		return response;
	}

	@Override
	public void onEvent(EventHeader header, Event event)
	{
		Object[] attach = (Object[]) event.getAttachment();
		EventHeaderTags tags = (EventHeaderTags) attach[0];
		EventSendService sendService = (EventSendService) attach[1];
		Event response = handleRecvEvent(tags, event);
		if (null != response)
		{
			response.setHash(event.getHash());
			GAEServerConfiguration cfg = ServerConfigurationService
			        .getServerConfig();
			CompressorType compressType = cfg.getCompressor();
			if (response instanceof HTTPResponseEvent)
			{
				HTTPResponseEvent httpRes = (HTTPResponseEvent) response;
				String contentType = httpRes.getHeader("content-type");
				if (null != contentType
				        && cfg.isContentTypeInCompressFilter(contentType))
				{
					compressType = CompressorType.NONE;
				}
			}
			CompressEvent compress = new CompressEvent(compressType, response);
			compress.setHash(event.getHash());
			EncryptEvent enc = new EncryptEvent(cfg.getEncrypter(), compress);
			enc.setHash(event.getHash());
			Buffer buf = GAEEventHelper.encodeEvent(tags, enc);
			if (sendService.getMaxDataPackageSize() > 0
			        && buf.readableBytes() > sendService
			                .getMaxDataPackageSize())
			{
				Buffer[] bufs = GAEEventHelper.splitEventBuffer(buf,
				        event.getHash(), sendService.getMaxDataPackageSize(),
				        tags);
				for (Buffer buffer : bufs)
				{
					sendService.send(buffer);
				}
			}
			else
			{
				sendService.send(buf);
			}
		}
		else
		{
			logger.error("Failed to handle event[" + header.toString()+"]");
		}
	}

}
