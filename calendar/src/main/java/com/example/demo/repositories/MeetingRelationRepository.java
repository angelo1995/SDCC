package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.MeetingKey;
import com.example.demo.entities.MeetingRelation;

@Repository
public interface MeetingRelationRepository extends JpaRepository<MeetingRelation, MeetingKey>{
	
	public List<MeetingRelation> findByMeeting_Id(long id);

}
