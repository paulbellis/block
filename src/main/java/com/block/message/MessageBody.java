package com.block.message;

public class MessageBody {

	private Object body;

	public MessageBody(Object body) {
		super();
		this.body = body;
	}

	public Object getBody() {
		return body;
	}

	public static MessageBody create(Object body) {
		return new MessageBody(body);
	}
}
