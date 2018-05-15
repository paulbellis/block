package com.block.crypto;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Optional;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RSA implements Keys {

	private static Logger log = LogManager.getLogger(RSA.class);
	private PrivateKey priv;
	private PublicKey pub;

	@Override
	public boolean verify(String data, byte[] sig) {
		try {
			Signature publicSignature = Signature.getInstance("SHA256withRSA");
			publicSignature.initVerify(pub);
			publicSignature.update(data.getBytes(StandardCharsets.UTF_8));
			return publicSignature.verify(sig);
		} catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
			return false;
		}
	}

	@Override
	public byte[] sign(String data) {
        Signature privateSignature;
		try {
			privateSignature = Signature.getInstance("SHA256withRSA");
	        privateSignature.initSign(priv);
	        privateSignature.update(data.getBytes(StandardCharsets.UTF_8));
	        return privateSignature.sign();
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			return new byte[0];
		}
	}

	@Override
	public PrivateKey getPriv() {
		return priv;
	}

	@Override
	public PublicKey getPub() {
		return pub;
	}

	private void generateNewKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(512);
		KeyPair pair = keyGen.generateKeyPair();
		priv = pair.getPrivate();
		pub = pair.getPublic();
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
			pemString = pemString.replace("-----BEGIN RSA PRIVATE KEY-----", "");
			pemString = pemString.replace("-----END RSA PRIVATE KEY-----", "");

			byte[] decoded = Base64.decodeBase64(pemString);

			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
			KeyFactory kf = KeyFactory.getInstance("RSA");

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
					writePemFile(priv, "RSA PRIVATE KEY", "id_rsa");
					writePemFile(pub, "RSA PUBLIC KEY", "id_rsa.pub");
				}
			}
			catch (IOException | NoSuchAlgorithmException e1) {
				log.error(e1.getMessage());
			}
		} 
	}

	private void writePemFile(Key key, String description, String filename) throws FileNotFoundException, IOException {
		PemFile pemFile = new PemFile(key, description);
		pemFile.write(filename);
	}

	@Override
	public PublicKey getPubFromPriv() throws NoSuchAlgorithmException, InvalidKeySpecException {
		return pub;
	}

	public static RSA valueOf() {
		return new RSA();
	}
	
	@Override
	public PublicKey recreatePubKeyFromAddress(String hexEncodedBase64DecodedPemPublicKeyText)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, DecoderException {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		RSA myRSA = new RSA();
		myRSA.init("paul");
		String data = "hello world";
		byte[] sig = myRSA.sign(data);
		System.out.println(myRSA.verify(data, sig));
		

	}

	@Override
	public String getPublicKeyDecodedHexAddress() {
		// TODO Auto-generated method stub
		return null;
	}


}
