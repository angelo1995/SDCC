package com.example.demo.enumerations;

import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum MeetingPriority {

	LOW(0, "bassa"), MEDIUM(1, "media"), HIGH(2, "alta");

	private Integer code;
	private String message;

	private MeetingPriority(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public static boolean check(String message) {
		return Stream.of(MeetingPriority.values())
				.anyMatch(p -> p.getMessage().equalsIgnoreCase(message));
	}

	public static MeetingPriority getPriorityFromString(String message) {
		return Stream.of(MeetingPriority.values())
				.filter(p -> p.message.equalsIgnoreCase(message))
				.findFirst()
				.orElseThrow(IllegalArgumentException::new);
	}

}
