package com.example.demo.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
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

    @Basic
    @Column(nullable = true)
    private int status;

	public MeetingRelation(Costumer costumer, Meeting meeting, int status) {
		this.costumer = costumer;
		this.meeting = meeting;
		this.status = status;
	}
    
}
