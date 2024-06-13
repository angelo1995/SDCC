package com.example.demo.handlers;

import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.example.demo.entities.Costumer;
import com.example.demo.exceptions.MalformedJwtException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.repositories.CostumerRepository;

@Aspect
@Component
public class CheckCostumer {
	
	private static final String EMAIL = "email";
	
	@Autowired
	private CostumerRepository costumerRepository;
	
	@Before("@annotation(com.example.demo.annotations.FoundbleUser)")
	public void signupUser(JoinPoint joinPoint) {
		Jwt jwt = getJwt(joinPoint.getArgs());
		checkJwt(jwt);
	}
	
	private Jwt getJwt(Object[] objects) {
		for(Object object : objects) {
			if(object instanceof Jwt) {
				return (Jwt) object;
			}
		}
		throw new RuntimeException("exception");
	}
	
	private void checkJwt(Jwt jwt) {
		if(jwt == null) {
			throw new NullPointerException("jwt is null");
		}
		String email = jwt.getClaimAsString(EMAIL);
		if(email == null) {
			throw new MalformedJwtException("jwt is malformed");
		}
		Optional<Costumer> optional = costumerRepository.findByEmail(email);
		if(optional.isEmpty()) {
			throw new UserNotFoundException();
		}
	}

}
