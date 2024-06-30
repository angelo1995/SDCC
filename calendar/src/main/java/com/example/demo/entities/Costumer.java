package com.example.demo.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
public class Costumer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Basic
	@Column(nullable = true, length = 255)
	private String email;

	@Basic
	@Column(nullable = true, length = 255)
	private String name;

	@Basic
	@Column(nullable = true, length = 255)
	private String surname;

	@Basic
	@Column(nullable = true, length = 255)
	private String username;
	
	@OneToMany(mappedBy = "user")
	@ToString.Exclude
	@JsonIgnore
	private List<Meeting> reservations = new ArrayList<>();
	
	@OneToMany(mappedBy = "costumer")
	@ToString.Exclude
	@JsonIgnore
    private List<MeetingRelation> meetings = new ArrayList<>();
	
	@Setter(value=AccessLevel.NONE)
	@Getter(value=AccessLevel.NONE)
	@Version
    @Column(name = "version", nullable = false)
    @JsonIgnore
    private long version;

	public Costumer(String first_name, String last_name) {
		this.name = first_name;
		this.surname = last_name;
	}
	
}
