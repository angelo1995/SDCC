package com.example.demo.data;

import java.util.List;

import lombok.Data;

@Data
public class WindowWeekResponse {
	
	private List<List<DayofWeek>> weeks;

}
