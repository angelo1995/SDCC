package com.example.demo.handlers;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.example.demo.entities.Costumer;
import com.example.demo.exceptions.MalformedJwtException;
import com.example.demo.services.CostumerService;

@Aspect
@Component
//@Slf4j
public class RegistrationAspect {
	
	@Autowired
	private CostumerService service;
    
	@Before("@annotation(com.example.demo.annotations.AutoSignup)")
	public void signupUser(JoinPoint joinPoint) {
		Jwt jwt = null;
		Costumer newUser = null;
		
		//log.info("auto sign up");
		jwt = getJwt(joinPoint.getArgs());
		checkMap(jwt);
		if(service.getByEmail(jwt).isPresent()) {
			//log.info("user already exist");
			return;
		}
	    newUser = map(jwt);
	    service.createUser(newUser);
	    //log.info("jwt token: " + jwt.getTokenValue());
	    //log.info("new user was created: " + newUser.toString());
	}
	
	private Jwt getJwt(Object[] objects) {
		for(Object object : objects) {
			if(object instanceof Jwt) {
				return (Jwt) object;
			}
		}
		throw new RuntimeException("jwt not found. Problably you have to add @AuthenticationPrincipal Jwt jwt");
	}
	
	private void checkMap(Jwt jwt) {
		if(jwt.hasClaim(EMAIL) && jwt.hasClaim(NAME) && jwt.hasClaim(SURNAME) && jwt.hasClaim(USERNAME)) {
			return;
		}
		throw new MalformedJwtException("user registration failed");
	}
	
	private Costumer map(Jwt jwt) {
		checkMap(jwt);
		Costumer newUser = new Costumer();
	    newUser.setEmail(jwt.getClaimAsString(EMAIL));
	    newUser.setName(jwt.getClaimAsString(NAME));
	    newUser.setSurname(jwt.getClaimAsString(SURNAME));
	    newUser.setUsername(jwt.getClaimAsString(USERNAME));
		return newUser;
	}
	
	private static final String EMAIL = "email";
	private static final String NAME = "given_name";
	private static final String SURNAME = "family_name";
	private static final String USERNAME = "preferred_username";

}
