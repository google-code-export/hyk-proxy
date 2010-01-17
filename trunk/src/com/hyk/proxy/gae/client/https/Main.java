package com.hyk.proxy.gae.client.https;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

public class Main {

	public static void main(String args[]) throws Exception {
		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory
				.getDefault();
		final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };

		SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket(48103);
		ss.setEnabledCipherSuites(enabledCipherSuites);
		//ss.s
		while (true) {
			// ss.s
			SSLSocket s = (SSLSocket) ss.accept();
			// s.startHandshake();

			OutputStream out = s.getOutputStream();
			InputStream in = s.getInputStream();
			// BufferedReader in = new BufferedReader(new InputStreamReader(s
			// .getInputStream()));

			byte[] bbuffer = new byte[4096];
			int len = in.read(bbuffer);
			String line = new String(bbuffer, 0, len);
			System.out.println("####" + line);
			// while (((line = in.readLine()) != null) && (!("".equals(line))))
			// {
			// System.out.println(line);
			// }
			StringBuffer buffer = new StringBuffer();
			buffer.append("<HTML><HEAD><TITLE>HTTPS Server</TITLE></HEAD>\n");
			buffer.append("<BODY>\n<H1>Success!</H1></BODY></HTML>\n");

			String string = buffer.toString();
			byte[] data = string.getBytes();
			out.write("HTTP/1.0 200 OK\n".getBytes());
			out.write(new String("Content-Length: " + data.length + "\n")
					.getBytes());
			out.write("Content-Type: text/html\n\n".getBytes());
			out.write(data);
			out.flush();

			out.close();
			in.close();
			s.close();
		}
	}
}
