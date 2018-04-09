package com.block.crypto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public class ECDSA implements Keys {

	private static Logger log = LogManager.getLogger(RSA.class);
	private KeyPair pair = null;
	private PrivateKey priv = null;
	private PublicKey pub = null;
	private String publicKeyDecodedHexAddress = null;

	private static String KEY_ALGORITHM = "ECDSA";
	private static String PROVIDER = "BC";
	private static String SIGNATURE = "SHA1withECDSA";
	private static String PRIV_PEM_FILENAME = "ec-priv.pem";
	private static String PUB_PEM_FILENAME = "ec-pub.pem";

	@Override
	public boolean verify(String data, byte[] sig) {
		return verify(pub, data, sig);
	}

	public static boolean verify(PublicKey pubKey, String data, byte[] sig) {
		boolean verified = false;
		Signature dsa;
		try {
			dsa = Signature.getInstance(SIGNATURE);
			dsa.initVerify(pubKey);
			dsa.update(data.getBytes());
			verified = dsa.verify(sig);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			log.error(e.getMessage());
		}
		return verified;
	}

	@Override
	public byte[] sign(String data) {

		Signature dsa;
		try {
			dsa = Signature.getInstance(SIGNATURE);
			dsa.initSign(priv);

			byte[] strByte = data.getBytes("UTF-8");
			dsa.update(strByte);
			return dsa.sign();
		} catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	@Override
	public PublicKey getPubFromPriv() throws NoSuchAlgorithmException, InvalidKeySpecException {
		return pub;
	}

	@Override
	public PrivateKey getPriv() {
		return priv;
	}

	@Override
	public PublicKey getPub() {
		return pub;
	}

	private void writePrivatePemFile(Key key, String description, String filename)
			throws IOException {
		PrivateKeyInfo i = PrivateKeyInfo.getInstance(ASN1Sequence.getInstance(key.getEncoded()));
		if (!i.getPrivateKeyAlgorithm().getAlgorithm().equals(X9ObjectIdentifiers.id_ecPublicKey)) {
			log.error("not an EC key");
		} else {
			Writer x = new FileWriter(new File(filename));
			PemWriter w = new PemWriter(x);
			ASN1Object o = (ASN1Object) i.parsePrivateKey();
			w.writeObject(new PemObject(description, o.getEncoded("DER")));
			// DER may already be the default but safer to (re)specify it
			w.close();
		}
	}

	private KeyPair keyPairFromPEM(String pem) throws IllegalArgumentException, IOException {

		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		Reader rdr = new FileReader(new File(pem));
		org.bouncycastle.openssl.PEMParser parser = new org.bouncycastle.openssl.PEMParser(rdr);
		Object parsed = parser.readObject();
		pair = new org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter()
				.getKeyPair((org.bouncycastle.openssl.PEMKeyPair) parsed);
		rdr.close();
		parser.close();
		return pair;
	}

	private void readPemRsaPrivateKey(String pemFilename) throws IllegalArgumentException, IOException {
		KeyPair x = keyPairFromPEM(pemFilename);
		setPriv(x.getPrivate());
		setPub(x.getPublic());
	}

	private void writePublicKeyDecodedHexAddress(String user) throws IOException {
		Path p = Paths.get(user + "/" + "pub.txt");
		Files.write(p, this.publicKeyDecodedHexAddress.getBytes());
	}
	
	@Override
	public void init(String user) {
		try {
			readPemRsaPrivateKey(user + "/" + PRIV_PEM_FILENAME);
		} catch (IOException e) {
			try {
				if (priv == null || pub == null) {
					Path dir = Paths.get(user);
					if (!dir.toFile().exists()) {
						Files.createDirectory(dir);
					}
					generateNewKeys();
					writePrivatePemFile(priv, "EC PRIVATE KEY", user + "/" + PRIV_PEM_FILENAME);
					writePemFile(pub, "EC PUBLIC KEY", user + "/" + PUB_PEM_FILENAME);
					writePublicKeyDecodedHexAddress(user);
				}
			} catch (IOException e1) {
				log.error(e1.getMessage());
			}
		}
	}

	@Override
	public PublicKey recreatePubKeyFromAddress(String hexEncodedBase64DecodedPemPublicKeyText) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, DecoderException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		KeyFactory fact = KeyFactory.getInstance(KEY_ALGORITHM, PROVIDER);
		return fact.generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(Base64.encodeBase64String(Hex.decodeHex(hexEncodedBase64DecodedPemPublicKeyText.toCharArray())))));
	}

	public static PublicKey getPubKeyFromAddress(String hexEncodedBase64DecodedPemPublicKeyText) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, DecoderException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		KeyFactory fact = KeyFactory.getInstance(KEY_ALGORITHM, PROVIDER);
		return fact.generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(Base64.encodeBase64String(Hex.decodeHex(hexEncodedBase64DecodedPemPublicKeyText.toCharArray())))));
	}


	private void writePemFile(Key key, String description, String filename) throws FileNotFoundException, IOException {
		PemFile pemFile = new PemFile(key, description);
		pemFile.write(filename);
	}

	private void generateNewKeys() {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			keyGen.initialize(256, random);
			pair = keyGen.generateKeyPair();
			try {
				KeyFactory fact = KeyFactory.getInstance(KEY_ALGORITHM, PROVIDER);
				setPriv(fact.generatePrivate(new PKCS8EncodedKeySpec(pair.getPrivate().getEncoded())));
				setPub(fact.generatePublic(new X509EncodedKeySpec(pair.getPublic().getEncoded())));
			} catch (NoSuchProviderException | InvalidKeySpecException e) {
				log.error(e.getMessage());
			}

		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage());
		}

	}

	public static Keys valueOf() {
		return new ECDSA();
	}

	private void setPriv(PrivateKey priv) {
		this.priv = priv;
	}

	@Override
	public String getPublicKeyDecodedHexAddress() {
		return publicKeyDecodedHexAddress;
	}

	private void setPublicKeyDecodedHexAddress(String publicKeyDecodedHexAddress) {
		this.publicKeyDecodedHexAddress = publicKeyDecodedHexAddress;
	}

	private void setPub(PublicKey pub) {
		this.pub = pub;
		System.out.println(pub.getEncoded());
		System.out.println(Hex.encodeHex(pub.getEncoded()));
		System.out.println(Hex.encodeHexString(pub.getEncoded()));
		setPublicKeyDecodedHexAddress(Hex.encodeHexString(pub.getEncoded()));
	}

	public static void main(String[] args)
			throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, DecoderException {
		ECDSA ecdsa = new ECDSA();
		ecdsa.init("localhost4567");
		String data = "hello world";
		byte[] sig = ecdsa.sign(data);
		System.out.println("G-" + ((ECPrivateKey) ecdsa.getPriv()).getParameters().getG());
		System.out.println("x-"+((ECPublicKey) ecdsa.getPub()).getQ().getXCoord().toString());
		System.out.println("y-"+((ECPublicKey) ecdsa.getPub()).getQ().getYCoord().toString());
		System.out.println(Hex.encodeHexString(sig) + " : " + ecdsa.verify(data, sig));

		System.out.println(ECDSA.verify(ecdsa.recreatePubKeyFromAddress(ecdsa.getPublicKeyDecodedHexAddress()), data, sig));
		System.out.println(ECDSA.verify(ecdsa.recreatePubKeyFromAddress(ecdsa.getPublicKeyDecodedHexAddress()), data + "x", sig));

	}

}
