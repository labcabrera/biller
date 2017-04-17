
package com.luckia.biller.core.common;

@SuppressWarnings("serial")
public class BillerException extends RuntimeException {

	public BillerException() {
		super();
	}

	public BillerException(String message) {
		super(message);
	}

	public BillerException(Throwable cause) {
		super(cause);
	}

	public BillerException(String message, Throwable cause) {
		super(message, cause);
	}

}
