package com.block.message;

public class Message {
	private MessageHeader header;
	private MessageBody body;

	public Message(MessageHeader header, MessageBody body) {
		super();
		this.header = header;
		this.body = body;
	}

	public MessageHeader getHeader() {
		return header;
	}

	public MessageBody getBody() {
		return body;
	}

	public static Message create(MessageHeader header, MessageBody body) {
		return new Message(header, body);
	}
}
