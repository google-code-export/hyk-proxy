/**
 * 
 */
package com.hyk.proxy.plugin.phptunnel.event;

import java.net.URL;
import java.util.concurrent.ExecutorService;

import com.hyk.proxy.framework.common.Misc;
import com.hyk.proxy.framework.event.HttpProxyEventService;
import com.hyk.proxy.framework.event.HttpProxyEventServiceFactory;
import com.hyk.proxy.framework.util.ListSelector;
import com.hyk.proxy.plugin.phptunnel.config.PhpTunnelApplicationConfig;


/**
 * @author wqy
 *
 */
public class PhpTunnelClientProxyEventServiceFactory implements HttpProxyEventServiceFactory
{
	public static final String NAME = "PHPTunnel";
	
	private int localServerPort = -1;
	private ExecutorService executor;
	private ListSelector<URL>	selector;
	
	public PhpTunnelClientProxyEventServiceFactory(ExecutorService executor, int localServerPort)
    {
	    this.executor = executor;
	    this.localServerPort = localServerPort;
	    this.selector = new ListSelector<URL>(PhpTunnelApplicationConfig.getPhpTunnelServerAddress());
    }
	

	@Override
    public HttpProxyEventService createHttpProxyEventService()
    {
	    return new PhpTunnelClientProxyEventService(executor, localServerPort, selector);
    }

	@Override
    public String getName()
    {
	    return NAME;
    }

	@Override
    public void init() throws Exception
    {
		Misc.getTrace().info(NAME + " is working now!");
	    
    }

	@Override
    public void destroy() throws Exception
    {
	    // TODO Auto-generated method stub
	    
    }

}
