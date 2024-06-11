package com.example.demo.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Meeting {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Basic
    @Column(nullable = true)
    private String description;
    
    @Basic
    @Column(nullable = true)
    private String title;
    
    @Basic
    @Column(nullable = true)
    private int priority;
    
    @Basic
    @Column(nullable = true)
    private boolean open;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private Costumer user;
    
    @OneToMany(cascade = CascadeType.REMOVE)
	@ToString.Exclude
	@JsonIgnore
	private List<Slot> slots = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.REMOVE)
	@ToString.Exclude
	@JsonIgnore
	private List<Costumer> guests = new ArrayList<>();
    
    @Version
    @Column(name = "version", nullable = false)
    @JsonIgnore
    @Setter(value=AccessLevel.NONE)
	@Getter(value=AccessLevel.NONE)
    private long version;

	public Meeting(String description, Costumer user) {
		this.description = description;
		this.user = user;
	}
	
}
