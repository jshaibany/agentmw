package com.onecashye.web.security.exception;

public class NullOrEmptyInputParameters extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NullOrEmptyInputParameters(String errorMessage) {
        super(errorMessage);
    }
}
