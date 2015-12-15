package com.luckia.biller.core.common;

@SuppressWarnings("serial")
public class ConcurrentModificationException extends RuntimeException {

	public ConcurrentModificationException(String message) {
		super(message);
	}

	public ConcurrentModificationException(Throwable cause) {
		super(cause);
	}

	public ConcurrentModificationException(String message, Throwable cause) {
		super(message, cause);
	}
}
