package com.luckia.biller.core.model.common;

import java.util.ArrayList;
import java.util.List;

public class Message<I> {

	public static final String CODE_SUCCESS = "200";
	public static final String CODE_GENERIC_ERROR = "500";	

	private String code;
	private String message;
	private List<String> errors;

	private I payload;

	public Message() {
	}

	public Message(String code, String message, I payload) {
		this.code = code;
		this.message = message;
		this.payload = payload;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public I getPayload() {
		return payload;
	}

	public void setPayload(I payload) {
		this.payload = payload;
	}

	public boolean hasErrors() {
		return errors != null && !errors.isEmpty();
	}

	public void addError(String value) {
		synchronized (this) {
			if (errors == null) {
				errors = new ArrayList<String>();
			}
		}
		errors.add(value);
	}
}
