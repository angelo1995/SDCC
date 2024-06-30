package com.example.demo.entities;

import java.io.Serializable;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@Embeddable
@Data
@NoArgsConstructor
public class MeetingKey implements Serializable {

	@Basic
    @Column(name = "costumer_id")
    private Long costumerId;

	@Basic
    @Column(name = "meeting_id")
    private Long meetingId;

}
