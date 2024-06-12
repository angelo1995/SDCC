package com.example.demo.data;

import lombok.Data;

@Data
public class ReservationResponse {
	
	private boolean success;
	private String message;
	
	public ReservationResponse(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	
}
