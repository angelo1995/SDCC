package com.example.demo.data;

import lombok.Data;

@Data
public class MeetingResponse {
	
	private boolean success;
	private String message;
	private MeetingData meeting;
	
	public MeetingResponse(boolean success, String message, MeetingData meeting) {
		this.success = success;
		this.message = message;
		this.meeting = meeting;
	}
	
}
