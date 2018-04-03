package com.block.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class Serializer {

	public static byte[] convertToBytes(Object object) {
	    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        ObjectOutput out = new ObjectOutputStream(bos)) {
	        out.writeObject(object);
	        return bos.toByteArray();
	    } catch (IOException e) {
			return null; 
		}
	}
	
}
