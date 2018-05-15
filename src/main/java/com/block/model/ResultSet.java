package com.block.model;

import com.block.commons.JSON;

public class ResultSet {

	public static String ERROR = "ERROR";
	public static String SUCCESS = "SUCCESS";
	public static String ACCOUNT_NOT_EXIST = "Account does not exist";
	
	private String status;
	private Object data;
	private ResultSet(String status, Object data) {
		super();
		this.status = status;
		this.data = data;
	}

	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	public static class ResultSetBuilder {
		private String status;
		private Object data;
		private String dataAsString;

		public ResultSetBuilder setStatus(String status) {
			this.status = status;
			return this;
		}

		public ResultSetBuilder setErrorStatus() {
			this.status = ResultSet.ERROR;
			return this;
		}

		public ResultSetBuilder setOkStatus() {
			this.status = ResultSet.SUCCESS;
			return this;
		}

		public ResultSetBuilder setData(Object data) {
			this.data = data;
			this.dataAsString = JSON.toJson(data);
			return this;
		}
		
		public String build() {
			return JSON.toJson(new ResultSet(this.status, this.dataAsString));
		}
		
	}
	
}
