package com.example.demo.configurations;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.entities.Costumer;
import com.example.demo.entities.Slot;
import com.example.demo.repositories.CostumerRepository;
import com.example.demo.repositories.MeetingRepository;
import com.example.demo.repositories.SlotRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
class LoadDatabase {
	
	@Autowired
	private SlotRepository slotRepository;
	
	@Autowired
	private MeetingRepository meetingRepository;
	
	@Autowired
	private CostumerRepository costumerRepository;

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			
			meetingRepository.toString();
			
			if(slotRepository.findAll().size() == 0) {
				for(int i=0; i < 6; ++i) {
					for(int j=0; j < 11; j++) {
						//log.info("Preloading " + slotRepository.save(new Slot("2024-06-" + (20+i) + " " + (j+8) + ":00:00", false)));
					}
				}
			}
			
			/*Costumer ower = new Costumer("name ower 1", "surname ower 1");
			log.info("Preloading " + costumerRepository.save(ower));
			Costumer[] guests = new Costumer[10];
			for(int i=0; i < guests.length; i++) {
				guests[i] = new Costumer("name test " + i, "surname test " + i);
				log.info("Preloading " + costumerRepository.save(guests[i]));
			}
			
			guests = new Costumer[10];
			char letter = 'a';
			for(int i=0; i < guests.length; i++) {
				guests[i] = new Costumer("name test " + i, letter + "surname test " + i);
				log.info("Preloading " + costumerRepository.save(guests[i]));
				letter++;
			}*/
			
			final int DAY = 3;
			/*			
			Slot slot = new Slot("2024-06-" + (DAY) + " " + (9) + ":00:00", false);
			slotRepository.save(slot);
			Meeting meeting = new Meeting();
			meeting.setUser(ower);
			meeting.setSlots(List.of(slot));
			meeting.setPriority(1);
			meeting.setGuests(List.of(guests));
			meeting.setTitle("titolo test");
			meeting.setDescription("descrizione test");
			log.info("Preloading " + meetingRepository.save(meeting));*/
			
			Slot[] slots = new Slot[2];
			slots[0] = new Slot("2024-07-" + (DAY) + " " + (8) + ":00:00", true);
			slots[1] = new Slot("2024-07-" + (DAY) + " " + (9) + ":00:00", true);
			Optional<Costumer> optional = costumerRepository.findByEmail("test@testLeone.com");
			if(optional.isEmpty()) {
				Costumer guest = new Costumer("Davide", "Leone");
				guest.setEmail("test@testLeone.com");
				guest.setUsername("username_leone");
				guest = costumerRepository.save(guest);
				costumerRepository.delete(guest);
				log.info("Database up");
			}
			
			/*slotRepository.save(slots[0]);
			slotRepository.save(slots[1]);
			meeting = new Meeting();
			meeting.setUser(guests[0]);
			meeting.setSlots(List.of(slots));
			meeting.setPriority(1);
			meeting.setGuests(List.of(guest));
			meeting.setTitle("titolo test 2");
			meeting.setDescription("descrizione test");
			log.info("Preloading " + meetingRepository.save(meeting));
			
			slots = new Slot[2];
			slots[0] = new Slot("2024-06-" + (DAY) + " " + (10) + ":00:00", true);
			slots[1] = new Slot("2024-06-" + (DAY) + " " + (11) + ":00:00", true);
			slotRepository.save(slots[0]);
			slotRepository.save(slots[1]);
			meeting = new Meeting();
			meeting.setUser(guests[0]);
			meeting.setSlots(List.of(slots));
			meeting.setPriority(1);
			meeting.setGuests(List.of(guests));
			meeting.setTitle("titolo test 3");
			meeting.setDescription("descrizione test");
			log.info("Preloading " + meetingRepository.save(meeting));*/
		};
	}
}