package com.example.demo.exceptions;

@SuppressWarnings("serial")
public class BookingException extends RuntimeException{

	public BookingException() {
		super();
	}

	public BookingException(String message, Throwable cause) {
		super(message, cause);
	}

	public BookingException(String message) {
		super(message);
	}

	public BookingException(Throwable cause) {
		super(cause);
	}

}
