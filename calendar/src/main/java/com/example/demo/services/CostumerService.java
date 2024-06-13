package com.example.demo.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.Costumer;
import com.example.demo.repositories.CostumerRepository;

@Service
public class CostumerService {
	
	@Autowired
	private CostumerRepository costumerRepository;
	
	@Transactional(readOnly = false)
	public Optional<Costumer> createUser(Costumer user) {
		if(costumerRepository.findByEmail(user.getEmail()).isPresent()) {
			return Optional.empty();
		}
		return Optional.of(costumerRepository.save(user));		
	}
	
	@Transactional(readOnly = true)
	public Optional<Costumer> getByEmail(Jwt jwt) {
		String email = jwt.getClaimAsString("email");
		return costumerRepository.findByEmail(email);
	}

}
