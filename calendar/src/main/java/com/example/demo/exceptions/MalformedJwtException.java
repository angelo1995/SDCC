package com.example.demo.exceptions;

@SuppressWarnings("serial")
public class MalformedJwtException extends RuntimeException{

	public MalformedJwtException() {
		super();
	}

	public MalformedJwtException(String message, Throwable cause) {
		super(message, cause);
	}

	public MalformedJwtException(String message) {
		super(message);
	}

	public MalformedJwtException(Throwable cause) {
		super(cause);
	}

}
