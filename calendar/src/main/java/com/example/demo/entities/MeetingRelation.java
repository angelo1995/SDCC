package com.example.demo.entities;

import com.example.demo.enumerations.MeetingStatus;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "meeting_relation")
public class MeetingRelation {

    @EmbeddedId
    private MeetingKey id = new MeetingKey();

    @ManyToOne
    @MapsId("costumerId")
    @JoinColumn(name = "costumer_id")
    private Costumer costumer;

    @ManyToOne
    @MapsId("meetingId")
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;
    
    private MeetingStatus status;

	public MeetingRelation(Costumer costumer, Meeting meeting, MeetingStatus status) {
		this.costumer = costumer;
		this.meeting = meeting;
		this.status = status;
	}
    
}
