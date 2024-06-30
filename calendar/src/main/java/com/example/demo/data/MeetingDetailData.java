package com.example.demo.data;

import java.sql.Timestamp;
import java.util.List;

import com.example.demo.entities.Meeting;
import com.example.demo.entities.Slot;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MeetingDetailData {
	
    private long id;
    
    private String title;

    private String description;
    
    private Timestamp date;
    
    private String priority;
    
    private String owner;
    
    private int duration;
    
	private List<String> guests;
	
	public MeetingDetailData(Meeting meeting) {
		this.id = meeting.getId();
		this.title = meeting.getTitle();
		this.description = meeting.getDescription();
		this.guests = meeting.getMeetings().stream()
				.map((m)-> m.getCostumer().getName()+" "+ m.getCostumer().getSurname())
				.toList();
		this.priority = meeting.getPriorityAsString();
		this.owner = meeting.getUser().getName() + " " + meeting.getUser().getSurname();
		
		List<Slot> slots = meeting.getSlots();
		if(slots.size() <= 0) {
			throw new IllegalStateException("reservation with no slot");
		}
		Timestamp min = slots.get(0).getDate();
		for(Slot slot: slots) {
			if(slot.getDate().compareTo(min) < 0) {
				min = slot.getDate();
			}
		}
		this.date = min;
		this.duration = slots.size();
	}

}
