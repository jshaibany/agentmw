package com.onecashye.web.security.exception;

public class FailedToCreateSqlServerRecord extends Exception{

	private static final long serialVersionUID = 1L;

	public FailedToCreateSqlServerRecord(String errorMessage) {
        super(errorMessage);
    }
}
