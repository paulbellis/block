package com.block.crypto;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

public class ECDSA3 implements Cryptographies {

	private static Logger log = LogManager.getLogger(RSA.class);
	private KeyPair pair = null;
	private PrivateKey priv = null;
	private PublicKey pub = null;
	private String user;

	public ECDSA3() {

	}

	@Override
	public boolean verify(String data, byte[] sig) {
		boolean verified = false;
		Signature dsa;
		try {
			dsa = Signature.getInstance("SHA1withECDSA");
			dsa.initVerify(pub);
			dsa.update(data.getBytes());
			verified = dsa.verify(sig);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return verified;
	}

	@Override
	public byte[] sign(String data) {

		Signature dsa;
		// ECPrivateKeySpec pubKeySpec = new ECPrivateKeySpec();
		PKCS8EncodedKeySpec pubKeySpec = new PKCS8EncodedKeySpec(priv.getEncoded());
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance("EC");
			PrivateKey privateKey = keyFactory.generatePrivate(pubKeySpec);
			dsa = Signature.getInstance("SHA1withECDSA");
			dsa.initSign(privateKey);

			byte[] strByte = data.getBytes("UTF-8");
			dsa.update(strByte);

			/*
			 * Now that all the data to be signed has been read in, generate a signature for
			 * it
			 */

			byte[] realSig = dsa.sign();
			return realSig;
			// return(new BigInteger(1, realSig).toString(16));
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public PublicKey getPubFromPriv() throws NoSuchAlgorithmException, InvalidKeySpecException {
		// KeyFactory factory = KeyFactory.getInstance("EC"); // using SpongyCastle
		// provider
		// ECPrivateKeySpec privSpec = factory.getKeySpec(priv, ECPrivateKeySpec.class);
		// ECParameterSpec params = privSpec.getParams();
		//
		// ECPoint q = params..getG().multiply(privSpec.getD());
		// ECPublicKeySpec pubSpec = new ECPublicKeySpec(q, params);
		// PublicKey publicKey = factory.generatePublic(pubSpec);
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

	private void writePemFile(Key key, String description, String filename) throws FileNotFoundException, IOException {
		PemFile pemFile = new PemFile(key, description);
		pemFile.write(filename);
	}

	// static void SO22963581BCPEMPrivateEC () throws Exception {
	// Security.addProvider(news
	// org.bouncycastle.jce.provider.BouncyCastleProvider());
	// Reader rdr = new StringReader ("-----BEGIN EC PRIVATE KEY-----\n"
	// +"MHQCAQEEIDzESrZFmTaOozu2NyiS8LMZGqkHfpSOoI/qA9Lw+d4NoAcGBSuBBAAK\n"
	// +"oUQDQgAE7kIqoSQzC/UUXdFdQ9Xvu1Lri7pFfd7xDbQWhSqHaDtj+XY36Z1Cznun\n"
	// +"GDxlA0AavdVDuoGXxNQPIed3FxPE3Q==\n"+"-----END EC PRIVATE KEY-----\n");
	// new PemObjectParser()
	// Object parsed = new org.bouncycastle.openssl.PEMParser(rdr).readObject();
	// KeyPair pair = new
	// org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter().getKeyPair((org.bouncycastle.openssl.PEMKeyPair)parsed);
	// System.out.println (pair.getPrivate().getAlgorithm());
	// }
	//
	private KeyPair keyPairFromPEM(String pem) throws IllegalArgumentException, IOException {

		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

		StringReader strReader = new StringReader("paulb/id_rsa");
		PEMParser pemParser = new PEMParser(strReader);
		Object keyObject = pemParser.readObject();
		pemParser.close();

		KeyPair keys = converter.getKeyPair((PEMKeyPair) keyObject);
		return keys;

	}

	private boolean generatePublicKeyFromPrivate() throws InvalidKeySpecException, NoSuchAlgorithmException {
		RSAPrivateCrtKey privk = (RSAPrivateCrtKey) priv;

		RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privk.getModulus(),
				privk.getPublicExponent());

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		pub = keyFactory.generatePublic(publicKeySpec);
		return true;
	}

	private void readPemRsaPrivateKey(String pemFilename) throws java.io.IOException,
			java.security.NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException {

		Optional<String> o = Files.readAllLines(Paths.get(pemFilename))
				.stream()
				.reduce((s1, s2) -> s1 + s2);
		if (o.isPresent()) {
			String pemString = o.get();
			
			KeyPair x = keyPairFromPEM(pemString);
			PrivateKey y = x.getPrivate();
			PublicKey z = x.getPublic();
			System.out.println(y.getAlgorithm());
			System.out.println(z.getAlgorithm());

			pemString = pemString.replace("-----BEGIN EC PRIVATE KEY-----", "");
			pemString = pemString.replace("-----END EC PRIVATE KEY-----", "");


			byte[] decoded = Base64.decodeBase64(pemString);

			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
			KeyFactory kf = KeyFactory.getInstance("EC");

			priv = kf.generatePrivate(keySpec);
			generatePublicKeyFromPrivate();
		}
	}

	@Override
	public void init(String user) {
		try {
			readPemRsaPrivateKey(user + "/id_rsa");
		} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			try {
				if (priv == null || pub == null) {
					Path dir = Paths.get(user);
					if (!Files.exists(dir)) {
						Files.createDirectory(dir);
					}
					generateNewKeys();
					writePemFile(priv, "EC PRIVATE KEY", user + "/id_rsa");
					writePemFile(pub, "EC PUBLIC KEY", user + "/id_rsa.pub");
				}
			} catch (IOException e1) {
				log.error(e1.getMessage());
			}
		}
	}

	private void generateNewKeys() {
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance("EC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			keyGen.initialize(256, random);
			pair = keyGen.generateKeyPair();
			priv = pair.getPrivate();
			pub = pair.getPublic();
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		}

	}

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
		Cryptographies paulbKeys = new ECDSA3();
		paulbKeys.init("paulb");
		String data = "hello world";
		byte[] sig = paulbKeys.sign(data);
		System.out.println(paulbKeys.getPriv()
				.getFormat());
		System.out.println(Hex.encodeHexString(paulbKeys.getPriv()
				.getEncoded()));
		System.out.println(paulbKeys.getPub()
				.getFormat());
		System.out.println(Hex.encodeHexString(paulbKeys.getPub()
				.getEncoded()));
		System.out.println(Hex.encodeHexString(paulbKeys.getPubFromPriv()
				.getEncoded()));
		System.out.println(Hex.encodeHexString(sig) + " : " + paulbKeys.verify(data, sig));

	}
}
