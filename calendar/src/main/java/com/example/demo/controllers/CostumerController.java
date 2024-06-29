package com.example.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.AutoSignup;
import com.example.demo.data.CostumerListData;
import com.example.demo.services.CostumerService;

@RestController
@RequestMapping("/costumer")
@CrossOrigin(origins="*", maxAge=3600)
public class CostumerController {
	
	@Autowired
	private CostumerService service;
	
	@GetMapping("/view/all")
	@PreAuthorize("hasAuthority('user')")
	@AutoSignup
	public List<CostumerListData> viewCostumerList(@AuthenticationPrincipal Jwt jwt) {
		return service.getAllCostumers(jwt);
	}

}
