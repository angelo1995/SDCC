package com.example.demo.enumerations;

import lombok.Getter;

@Getter
public enum MeetingStatus {
	
	INVITATION(0, ""), ACCEPTED(1, "(presente)"), REFUSED(2, "(assente)");
	
	private Integer code;
	private String message;

    private MeetingStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
