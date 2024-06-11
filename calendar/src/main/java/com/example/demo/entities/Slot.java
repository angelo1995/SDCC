package com.example.demo.entities;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@NoArgsConstructor
public class Slot {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	@Basic
    @Column(nullable = false, unique = true)
    private Timestamp date;
	
	@Basic
    @Column(nullable = false)
    private boolean occupied;
	
	@Setter(value=AccessLevel.NONE)
	@Getter(value=AccessLevel.NONE)
	@Version
    @Column(name = "version", nullable = false)
    @JsonIgnore
    private long version;

	public Slot(String date, boolean occupied) {
		this.date = Timestamp.valueOf(date);
		this.occupied = occupied;
	}

}
