package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.MeetingRelation;
import com.example.demo.entities.MeetingKey;

@Repository
public interface MeetingRelationRepository extends JpaRepository<MeetingRelation, MeetingKey>{

}
