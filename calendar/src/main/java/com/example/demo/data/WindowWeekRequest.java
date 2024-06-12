package com.example.demo.data;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class WindowWeekRequest {
	
	private Timestamp start;
	
	private Timestamp end;	

}
