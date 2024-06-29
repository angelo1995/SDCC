package com.example.demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.data.CostumerData;
import com.example.demo.data.CostumerListData;
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
	
	@Transactional(readOnly = true)
	public List<CostumerListData> getAllCostumers(Jwt jwt) {
		String email = jwt.getClaimAsString("email");
		//FIXME l'ultimo costumer non viene restituito e non c'è il controllo sul non restituire se stesso
		List<CostumerListData> result_list = new ArrayList<>();
		CostumerListData item_list = new CostumerListData();
		char letter = 'a';
		char item_letter;
		List<CostumerData> costumers = new ArrayList<>();
		List<CostumerData> list = costumerRepository.findAllByOrderBySurnameAsc().stream()
				.filter((c)->!c.getEmail().equals(email)).map(CostumerData::new).toList();
		if(list.isEmpty()) {
			return List.of();
		}
		item_letter = Character.toLowerCase(list.get(0).getTitle().charAt(0));
		while(item_letter != letter) {
			letter++;
			letter = (char)letter;
		}
		for(CostumerData item: list) {
			item_letter = Character.toLowerCase(item.getTitle().charAt(0));
			if(item_letter == letter) {
				costumers.add(item);
			} else {
				item_list = new CostumerListData();
				item_list.setLetter("" + letter);
				item_list.setMeetings(costumers);
				result_list.add(item_list);
				
				costumers = new ArrayList<>();
				while(item_letter != letter) {
					letter += 1;
					letter = (char)letter;
				}
				costumers.add(item);
			}
		}
		item_list = new CostumerListData();
		item_list.setLetter("" + letter);
		item_list.setMeetings(costumers);
		result_list.add(item_list);
		return result_list;
	}

}
