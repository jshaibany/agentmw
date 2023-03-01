package com.onecashye.web.security.exception;

public class SalesOrderNotFound extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SalesOrderNotFound(String errorMessage) {
        super(errorMessage);
    }
}
