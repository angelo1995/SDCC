package com.example.demo.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@PropertySource("classpath:constant_values.properties")
@Getter
public class CostantProvider {

	@Value("${MIN_DURATION_MEETING:1}")
    private int minDuration;
	
	@Value("${MAX_DURATION_MEETING:4}")
    private int maxDuration;
	
	@Value("${OPEN_HOUR_MEETING:8}")
    private int openHour;
	
	@Value("${CLOSED_HOUR_MEETING:19}")
    private int closedHour;

}
