package com.example.demo.entities;

public enum MeetingStatus {
	
	INVITATION(0), ACCEPTED(1), REFUSED(2), CANCELLED(3);
	
	private Integer code;

    private MeetingStatus(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

}
