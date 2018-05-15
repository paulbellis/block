package com.block.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.block.crypto.ECDSA;
import com.block.crypto.Keys;

public class KeyService {

	private Keys nodekey;
	private Map<String, Keys> keys;

	public KeyService() {
		this.keys = new ConcurrentHashMap<>();
	}

	public void init() throws IOException {
		Path p = Paths.get("./src/main/resources");
		Files.find(p, 2, (path, basicFileAttributes) -> path.toAbsolutePath()
				.toString()
				.endsWith("ec-priv.pem"))
				.forEach((Path p1) -> 
					{
						String sep = File.separator;
						String[] dirs = p1.toAbsolutePath().toString().split("\\"+sep);
						ECDSA ec = new ECDSA();
						ec.init(dirs[dirs.length-2]);
						addKey(ec.getPublicKeyDecodedHexAddress(), ec);
					});
					
	}

	public void addNodeKey(String nodename) {
		Keys nodenameKey = ECDSA.valueOf();
		nodenameKey.init(nodename);
		this.nodekey = nodenameKey;
		addKey(nodenameKey.getPublicKeyDecodedHexAddress(), nodenameKey);
	}

	public String getNodePublicKey() {
		return nodekey.getPublicKeyDecodedHexAddress();
	}

	public void addKey(String user, Keys cryptokey) {
		keys.put(user, cryptokey);
	}

	public Keys getKey(String from) {
		return keys.get(from);
	}
}
