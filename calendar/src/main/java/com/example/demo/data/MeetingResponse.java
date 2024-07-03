package com.example.demo.data;

import lombok.Data;

@Data
public class MeetingResponse {
	
	private boolean success;
	private String message;
	private MeetingDetailData meeting;
	
	public MeetingResponse(boolean success, String message, MeetingDetailData meeting) {
		this.success = success;
		this.message = message;
		this.meeting = meeting;
	}
	
}
