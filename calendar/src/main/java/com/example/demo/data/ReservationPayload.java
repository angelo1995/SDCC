package com.example.demo.data;

import java.sql.Timestamp;
import java.util.List;

import com.example.demo.entities.Meeting;
import com.example.demo.entities.MeetingRelation;
import com.example.demo.entities.Slot;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ReservationPayload {
	
	private long id;
	
	private String title;
	
	private String description;
	
	private Timestamp date;
	
	private int duration;
	
	private String priority;
	
	private List<Long> guests;
	
	public ReservationPayload(Meeting reservation) {
		this.id = reservation.getId();
		this.title = reservation.getTitle();
		this.description = reservation.getDescription();
		
		List<Slot> slots = reservation.getSlots();
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
	
	public ReservationPayload(MeetingRelation reservation) {
		this.id = reservation.getMeeting().getId();
		this.title = reservation.getMeeting().getTitle();
		this.description = reservation.getMeeting().getDescription();
		
		List<Slot> slots = reservation.getMeeting().getSlots();
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
