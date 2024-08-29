package com.example.demo.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.data.MessageData;

@RestController
@RequestMapping("/message")
@CrossOrigin(origins="*", maxAge=3600)
public class MessageController {
	
	@GetMapping("/home")
	public MessageData home() {
		MessageData message = new MessageData();
		String title = "Home";
		String body = "Descrizione home";
		
		message.setTitle(title);
		message.setBody(body);
		return message;
	}
	
	@GetMapping("/aboutUs")
	public MessageData aboutUs() {
		MessageData message = new MessageData();
		String title = "About us";
		String body = "Descrizione About us";
		
		message.setTitle(title);
		message.setBody(body);
		return message;
	}

}
