package com.example.demo.data;

import lombok.Data;

@Data
public class ReservationResponse {
	
	private boolean success;
	private String message;
	private MeetingData meeting;
	
	public ReservationResponse(boolean success, String message, MeetingData meeting) {
		this.success = success;
		this.message = message;
		this.meeting = meeting;
	}
	
}
