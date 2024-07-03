package com.example.demo.repositories;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Meeting;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long>{
	
	public Optional<Meeting> findById(long id);
	
	//TODO
	public List<Meeting> findBySlots_Date(Timestamp date);
	
	//https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
	//meeting.slots.date == 1?

}
