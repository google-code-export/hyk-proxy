/**
 * 
 */
package com.hyk.proxy.gae.client.https;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
/**
 * @author Administrator
 *
 */
public class Main2 {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		SSLContext sslContext = SSLContext.getDefault();
		final ExecutorService  serverice = new ScheduledThreadPoolExecutor(10);
		HttpsServer server  =HttpsServer.create(new InetSocketAddress(48103), 100);
		server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
		     public void configure (HttpsParameters params) {		 
		         // get the remote address if needed
		         InetSocketAddress remote = params.getClientAddress();
		 
		         SSLContext c = getSSLContext();
		 
		         SSLParameters sslparams = c.getDefaultSSLParameters();
		         System.out.println("###" + remote);
		 
		         params.setSSLParameters(sslparams);
		     }
		 }); 
		server.setExecutor(new Executor() {
			
			@Override
			public void execute(Runnable command) {
				// TODO Auto-generated method stub
				serverice.submit(command);
			}
		});
		server.createContext("/*", new HttpHandler() {		
			@Override
			public void handle(HttpExchange arg0) throws IOException {
				System.out.println(arg0.getRequestMethod());
				System.out.println(arg0.getProtocol());
				System.out.println(arg0.getRequestHeaders());
				System.out.println(arg0.getRemoteAddress());
				System.out.println(arg0.getPrincipal().getRealm());
				System.out.println(arg0.getHttpContext().getPath());
				System.out.println(arg0.getRequestURI());	
				System.out.println("#########");		
			}
		});
		
		server.start();
	}

}
