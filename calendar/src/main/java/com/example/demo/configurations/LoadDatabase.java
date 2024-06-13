package com.example.demo.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.entities.Slot;
import com.example.demo.repositories.SlotRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
class LoadDatabase {
	
	@Autowired
	private SlotRepository slotRepository;

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			
			if(slotRepository.findAll().size() == 0) {
				for(int i=0; i < 6; ++i) {
					for(int j=0; j < 11; j++) {
						log.info("Preloading " + slotRepository.save(new Slot("2024-06-" + (20+i) + " " + (j+8) + ":00:00", false)));
					}
				}
			}
		};
	}
}