package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Costumer;

@Repository
public interface CostumerRepository extends JpaRepository<Costumer, Long>{
	
	public Optional<Costumer> findByEmail(String email);
	
	public List<Costumer> findAllByOrderBySurnameAsc();

}
