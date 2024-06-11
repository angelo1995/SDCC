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
import lombok.Setter;
import lombok.ToString;

@Entity
@Data
public class Costumer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Basic
	@Column(nullable = true, length = 255)
	private String email;

	@Basic
	@Column(nullable = true, length = 255)
	private String first_name;

	@Basic
	@Column(nullable = true, length = 255)
	private String last_name;

	@Basic
	@Column(nullable = true, length = 255)
	private String username;
	
	@Setter(value=AccessLevel.NONE)
	@Getter(value=AccessLevel.NONE)
	@Version
    @Column(name = "version", nullable = false)
    @JsonIgnore
    private long version;

	@OneToMany(mappedBy = "user")
	@ToString.Exclude
	@JsonIgnore
	private List<Meeting> reservations = new ArrayList<>();

}
