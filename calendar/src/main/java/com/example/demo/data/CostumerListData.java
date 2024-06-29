package com.example.demo.data;

import java.util.List;

import lombok.Data;

@Data
public class CostumerListData {
	
	private String letter;
	private List<CostumerData> meetings;

}
