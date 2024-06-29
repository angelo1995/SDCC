package com.example.demo.data;

import com.example.demo.entities.Costumer;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CostumerData {
	
	private long id;
	private String title;
	
	public CostumerData(Costumer costumer) {
		this.id = costumer.getId();
		this.title = costumer.getSurname() + " " + costumer.getName();		
	}

}
