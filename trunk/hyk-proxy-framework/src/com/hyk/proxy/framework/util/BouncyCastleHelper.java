/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: BouncyCastleHelper.java 
 *
 * @author yinqiwen [ 2011-5-8 | ÉÏÎç11:27:39 ]
 *
 */
package com.hyk.proxy.framework.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.crypto.CipherOutputStream;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

import com.hyk.proxy.framework.appdata.AppData;

/**
 *
 */
public class BouncyCastleHelper
{
	static
	{
		Security.addProvider(new BouncyCastleProvider());
	}

	private static KeyPair createRSAKeyPair() throws NoSuchAlgorithmException,
	        NoSuchProviderException
	{
		KeyPairGenerator caKeyPairGen = KeyPairGenerator.getInstance("RSA",
		        "BC");
		caKeyPairGen.initialize(1024, new SecureRandom());
		KeyPair keypair = caKeyPairGen.genKeyPair();

		return keypair;

	}

	public static X509Certificate createCACert(KeyPair pair)
	        throws InvalidKeyException, IllegalStateException,
	        NoSuchProviderException, NoSuchAlgorithmException,
	        SignatureException, CertificateException
	{
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		certGen.setIssuerDN(new X500Principal("CN=hyk-proxy Certificate"));
		certGen.setNotBefore(new Date(System.currentTimeMillis() - 10000));
		certGen.setNotAfter(new Date(Long.MAX_VALUE));
		certGen.setSubjectDN(new X500Principal("CN=hyk-proxy Certificate"));
		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		// Is a CA
		certGen.addExtension(X509Extensions.BasicConstraints, true,
		        new BasicConstraints(true));

		certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
		        new SubjectKeyIdentifierStructure(pair.getPublic()));

		X509Certificate cert = certGen.generateX509Certificate(
		        pair.getPrivate(), "BC");
		cert.checkValidity(new Date());
		cert.verify(pair.getPublic());
		return cert;
	}

	public static X509Certificate createClientCert(PublicKey pubKey,
	        PrivateKey caPrivKey, PublicKey caPubKey, String host)
	        throws Exception
	{
		X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
		//
		// issuer
		//
		String issuer = "CN=hyk-proxy CA, OU=hyk-proxy CA, O=hyk-proxy, L=hyk-proxy, ST=hyk-proxy, C=CN";

		//
		// subjects name table.
		//
		Hashtable attrs = new Hashtable();
		Vector order = new Vector();

		attrs.put(X509Principal.C, "CN");
		attrs.put(X509Principal.O, "hyk-proxy");
		attrs.put(X509Principal.OU, "hyk-proxy");
		attrs.put(X509Principal.CN, host);

		order.addElement(X509Principal.C);
		order.addElement(X509Principal.O);
		order.addElement(X509Principal.OU);
		order.addElement(X509Principal.CN);

		//
		// create the certificate - version 3
		//
		v3CertGen.reset();

		v3CertGen
		        .setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		v3CertGen.setIssuerDN(new X509Principal(issuer));
		v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 10000));
		v3CertGen.setNotAfter(new Date(Long.MAX_VALUE));
		v3CertGen.setSubjectDN(new X509Principal(order, attrs));
		v3CertGen.setPublicKey(pubKey);
		v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		X509Certificate cert = v3CertGen.generateX509Certificate(caPrivKey);

		cert.checkValidity(new Date());

		cert.verify(caPubKey);
		// cert.getEncoded();

		return cert;
	}

	public static class FakeKeyManager extends X509ExtendedKeyManager 
	{
		private String entryname;
		private String port;
		private static X509Certificate caCert;
		private static KeyPair caKeyPair;
		private X509Certificate clientCert;
		private PrivateKey privatekey;

		static
		{
			if (null == caCert)
			{
				try
				{
					File certFile = new File(AppData.GetFakeSSLCertHome(),
					        "_ROOT_CA_obj.dump");
					if (certFile.exists())
					{
						FileInputStream caCertFis = new FileInputStream(
						        certFile);
						ObjectInputStream ois = new ObjectInputStream(caCertFis);
						caCert = (X509Certificate) ois.readObject();
						caKeyPair = (KeyPair) ois.readObject();
						ois.close();
						caCertFis.close();

					}
					else
					{
						FileOutputStream fos = new FileOutputStream(certFile);
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						caKeyPair = createRSAKeyPair();
						caCert = createCACert(caKeyPair);
						oos.writeObject(caCert);
						oos.writeObject(caKeyPair);
						oos.close();
						fos.close();
						File certf = new File(AppData.GetFakeSSLCertHome(),
						        "_ROOT_CA_.cert");
						fos = new FileOutputStream(certf);
						fos.write(caCert.getEncoded());
						fos.close();
					}
				}
				catch (Exception e)
				{
					// TODO: handle exception
				}

			}
		}

		public FakeKeyManager(String host, String port)
		{
			this.port = port;
			this.entryname = host;
			try
			{
				String certFileName = host + "_" + port + ".cert.obj.dump";
				File certFile = new File(AppData.GetFakeSSLCertHome(),
				        certFileName);
				if (certFile.exists())
				{
					FileInputStream caCertFis = new FileInputStream(certFile);
					ObjectInputStream oos = new ObjectInputStream(caCertFis);
					privatekey = (PrivateKey) oos.readObject();
					clientCert = (X509Certificate) oos.readObject();
					oos.close();
					caCertFis.close();
				}
				else
				{
					KeyPair pair = createRSAKeyPair();
					clientCert = createClientCert(pair.getPublic(),
					        caKeyPair.getPrivate(), caKeyPair.getPublic(), host);
					privatekey = pair.getPrivate();
					FileOutputStream fos = new FileOutputStream(certFile);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(privatekey);
					oos.writeObject(clientCert);
					oos.close();
					fos.close();
				}

			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

		}

		public String[] getClientAliases(String string, Principal[] prncpls)
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String chooseClientAlias(String[] strings, Principal[] prncpls,
		        Socket socket)
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String[] getServerAliases(String string, Principal[] prncpls)
		{
			return (new String[] { entryname });
		}

		public String chooseServerAlias(String string, Principal[] prncpls,
		        Socket socket)
		{
			return entryname;
		}

		public X509Certificate[] getCertificateChain(String string)
		{
			X509Certificate x509certificates[] = new X509Certificate[2];

			x509certificates[0] = clientCert;
			x509certificates[1] = caCert;

			return x509certificates;
		}

		public PrivateKey getPrivateKey(String string)
		{
			return this.privatekey;
		}

	}

	public static void main(String[] args) throws Exception
	{
		Security.addProvider(new BouncyCastleProvider());
		//SSLContext sslContext = SSLContext.getInstance("SSL", new BouncyCastleProvider());
		System.out.println(new BouncyCastleProvider().getServices());
		X509V3CertificateGenerator c = new X509V3CertificateGenerator();

		X509Certificate cert = createCACert(createRSAKeyPair());
		byte[] bytes = cert.getEncoded();
		// byte[] bytes = cert.getSignature();
		new FileOutputStream("x.cert").write(bytes);
		// ks.store(stream, password);
	}

}
