package com.example.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.data.ReservationPayload;
import com.example.demo.data.ReservationResponse;
import com.example.demo.data.WindowWeekRequest;
import com.example.demo.data.WindowWeekResponse;
import com.example.demo.services.BookingService;

@RestController
@RequestMapping("/book")
@CrossOrigin(origins="*", maxAge=3600)
public class BookingController {
	
	@Autowired
	private BookingService service;
	
	@PostMapping("/meeting")
	public ReservationResponse bookingReservation(@RequestBody ReservationPayload reservation) {
		return service.bookingReservation(reservation);
	}
	
	@PostMapping("/view/meeting")
	public WindowWeekResponse viewWindowWeek(@RequestBody WindowWeekRequest week) {
		return service.getWindowWeek(week.getStart(), week.getEnd());
	}
	
	@GetMapping("/view/meeting")
	public List<ReservationPayload> viewAllBooking() {
		return service.getAllReservation();
	}

}
