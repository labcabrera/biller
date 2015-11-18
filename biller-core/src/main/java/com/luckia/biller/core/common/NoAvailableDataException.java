package com.luckia.biller.core.common;

@SuppressWarnings("serial")
public class NoAvailableDataException extends RuntimeException {

	public NoAvailableDataException() {
		super();
	}

	public NoAvailableDataException(String message) {
		super(message);
	}

	public NoAvailableDataException(Throwable cause) {
		super(cause);
	}

	public NoAvailableDataException(String message, Throwable cause) {
		super(message, cause);
	}
}
