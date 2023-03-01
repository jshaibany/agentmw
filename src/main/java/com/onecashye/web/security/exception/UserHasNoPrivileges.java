package com.onecashye.web.security.exception;

public class UserHasNoPrivileges extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserHasNoPrivileges(String errorMessage) {
        super(errorMessage);
    }
}
