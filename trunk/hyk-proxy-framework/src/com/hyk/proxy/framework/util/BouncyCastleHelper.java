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
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.CipherOutputStream;
import javax.net.ssl.KeyManagerFactory;
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
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyk.proxy.framework.appdata.AppData;

/**
 *
 */
public class BouncyCastleHelper {
	protected static Logger logger = LoggerFactory
			.getLogger(BouncyCastleHelper.class);
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	static PrivateKey caPriKey;
	static X509Certificate caCert;
	static Map<String, KeyStore> kstCache = new ConcurrentHashMap<String, KeyStore>();
	public static final String KS_PASS = "hyk-proxy";
	public static final String CA_ALIAS = "RootCAPriKey";
	public static final String CLIENT_CERT_ALIAS = "FakeCertForClient";
	public static final String CA_FILE = "RootKeyStore.kst";

	public static PrivateKey getFakeRootCAPrivateKey() {
		loadFakeRootCA();
		return caPriKey;
	}

	public static X509Certificate getFakeRootCAX509Certificate() {
		loadFakeRootCA();
		return caCert;
	}

	private static boolean loadFakeRootCA() {
		if (null == caPriKey || null == caCert) {
			try {
				KeyStore ks = KeyStore.getInstance("JKS");
				FileInputStream fis = new FileInputStream(new File(
						AppData.GetFakeSSLCertHome(), "RootKeyStore.kst"));
				ks.load(fis, KS_PASS.toCharArray());
				caCert = (X509Certificate) ks.getCertificate(CA_ALIAS);
				caPriKey = (PrivateKey) ks.getKey(CA_ALIAS,
						KS_PASS.toCharArray());
				fis.close();
			} catch (Exception e) {
				logger.error("Failed to load fake CA key store.", e);
				return false;
			}

		}
		return true;
	}

	private static KeyPair createRSAKeyPair() throws NoSuchAlgorithmException,
			NoSuchProviderException {
		KeyPairGenerator caKeyPairGen = KeyPairGenerator.getInstance("RSA",
				"BC");
		caKeyPairGen.initialize(1024, new SecureRandom());
		KeyPair keypair = caKeyPairGen.genKeyPair();

		return keypair;

	}

	public static X509Certificate createCACert(KeyPair pair)
			throws InvalidKeyException, IllegalStateException,
			NoSuchProviderException, NoSuchAlgorithmException,
			SignatureException, CertificateException {
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		// signers name
		//
		String issuer = "CN=hyk-proxy Fake CA Certificate,OU=Certification Division,O=hyk-proxy Inc,L=hyk-proxy,S=hyk-proxy,C=hyk-proxy";

		//
		// subjects name - the same as we are self signed.
		//
		String subject = "CN=hyk-proxy Fake CA Certificate,OU=Certification Division,O=hyk-proxy Inc,L=hyk-proxy,S=hyk-proxy,C=hyk-proxy";

		certGen.setSerialNumber(BigInteger.valueOf(1));
		certGen.setIssuerDN(new X500Principal(issuer));
		certGen.setNotBefore(new Date(System.currentTimeMillis() - 1000));
		long hundredyear = 100L * 365 * 24 * 60 * 60 * 1000;
		certGen.setNotAfter(new Date(System.currentTimeMillis() + hundredyear));
		certGen.setSubjectDN(new X500Principal(subject));
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

	public static X509Certificate createClientCert(String host, PublicKey pubKey)
			throws Exception {
		if (!loadFakeRootCA()) {
			return null;
		}
		X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
		//
		// issuer
		//
		// String issuer =
		// "CN=hyk-proxy Fake CA Certificate,OU=Certification Division,O=hyk-proxy Inc,L=hyk-proxy,S=hyk-proxy,C=hyk-proxy";
		String issuer = "CN=hyk-proxy Fake CA Certificate,OU=Certification Division,O=hyk-proxy Inc,C=hyk-proxy";
		//
		// subjects name table.
		//
		Hashtable attrs = new Hashtable();
		Vector order = new Vector();
		attrs.put(X509Principal.C, "hyk-proxy");
		attrs.put(X509Principal.O, "hyk-proxy Inc");
		attrs.put(X509Principal.OU, "Certification Division");
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
		// v3CertGen.setIssuerDN(new X509Principal(issuer));
		v3CertGen.setIssuerDN(new X509Principal(issuer));
		v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 10000));
		long hundredyear = 90L * 365 * 24 * 60 * 60 * 1000;
		v3CertGen
				.setNotAfter(new Date(System.currentTimeMillis() + hundredyear));
		v3CertGen.setSubjectDN(new X509Principal(order, attrs));
		// v3CertGen.setSubjectDN(new X500Principal(
		// "CN=hyk-proxy Fake CA Certificate"));
		v3CertGen.setPublicKey(pubKey);
		v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		//
		// add the extensions
		//
		v3CertGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
				new SubjectKeyIdentifierStructure(pubKey));

		v3CertGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false,
				new AuthorityKeyIdentifierStructure(caCert.getPublicKey()));
		X509Certificate cert = v3CertGen.generate(caPriKey);

		cert.checkValidity(new Date());

		// cert.verify(caCert.getPublicKey());
		// cert.getEncoded();

		return cert;
	}

	public static KeyStore getClientKeyStore(String host) throws Exception {
		if (kstCache.containsKey(host)) {
			return kstCache.get(host);
		}
		KeyStore ks = KeyStore.getInstance("JKS");
		File kst_file = new File(AppData.GetFakeSSLCertHome(), host + ".kst");
		if (kst_file.exists()) {
			ks.load(new FileInputStream(kst_file), KS_PASS.toCharArray());
			kstCache.put(host, ks);
			return ks;
		} else {
			KeyPair pair = createRSAKeyPair();
			X509Certificate cert = createClientCert(host, pair.getPublic());
			ks.load(null, null);
			ks.setKeyEntry(CLIENT_CERT_ALIAS, caPriKey, KS_PASS.toCharArray(),
					new Certificate[] { cert, caCert });
			ks.store(new FileOutputStream(kst_file), KS_PASS.toCharArray());
			kstCache.put(host, ks);
			return ks;
		}
	}

	public static void main(String[] args) throws Exception {
		X509V3CertificateGenerator c = new X509V3CertificateGenerator();
		KeyPair pair = createRSAKeyPair();
		X509Certificate cert = createCACert(pair);
		FileOutputStream fos = new FileOutputStream(CA_FILE);
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(null, null);
		ks.setKeyEntry(CA_ALIAS, pair.getPrivate(), KS_PASS.toCharArray(),
				new Certificate[] { cert });
		ks.store(fos, KS_PASS.toCharArray());
		fos.close();

	}

}
