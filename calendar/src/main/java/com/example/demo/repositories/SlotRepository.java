package com.example.demo.repositories;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Slot;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long>{
	
	public List<Slot> findByDate(Timestamp date);
	
	public List<Slot> findAllByOrderByDateAsc();

}
