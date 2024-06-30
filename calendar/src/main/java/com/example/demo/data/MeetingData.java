package com.example.demo.data;

import com.example.demo.entities.Meeting;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MeetingData {
	
	private long id;
    
    private String title;
    
    public MeetingData(Meeting meeting) {
    	this.id = meeting.getId();
    	this.title = meeting.getTitle();    	
    }

}
