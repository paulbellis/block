package com.block.message;

public class MessageHeader {
	private MessageType type;

	private MessageHeader(MessageType type) {
		super();
		this.type = type;
	}

	public MessageType getType() {
		return type;
	}
	
	public static MessageHeader create(MessageType type) {
		return new MessageHeader(type);
	}
}
