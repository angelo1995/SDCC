package com.example.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.AutoSignup;
import com.example.demo.data.MeetingData;
import com.example.demo.data.MeetingDetailData;
import com.example.demo.data.ReservationPayload;
import com.example.demo.data.ReservationResponse;
import com.example.demo.data.SlotData;
import com.example.demo.data.WindowWeekRequest;
import com.example.demo.data.WindowWeekResponse;
import com.example.demo.entities.MeetingStatus;
import com.example.demo.services.BookingService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/book")
@CrossOrigin(origins="*", maxAge=3600)
@Slf4j
public class BookingController {
	
	@Autowired
	private BookingService service;
	
	@PostMapping("/meeting")
	@PreAuthorize("hasAuthority('user')")
	@AutoSignup
	public ReservationResponse bookingMeeting(@AuthenticationPrincipal Jwt jwt, @RequestBody ReservationPayload reservation) {
		log.info("input: " + reservation);
		return service.bookingMeeting(jwt, reservation);
	}
	
	@PostMapping("/view/meeting")
	public WindowWeekResponse viewWindowWeek(@RequestBody WindowWeekRequest week) {
		return service.getWindowWeek(week.getStart(), week.getEnd());
	}
	
	@GetMapping("/view/meeting")
	@PreAuthorize("hasAuthority('user')")
	@AutoSignup
	public List<ReservationPayload> viewAllBooking(@AuthenticationPrincipal Jwt jwt) {
		return service.getAllReservation(jwt);
	}
	
	@GetMapping("/view/meeting/detail/{id_meeting}")
	@PreAuthorize("hasAuthority('user')")
	@AutoSignup
	public MeetingDetailData viewMeetingDetail(@AuthenticationPrincipal Jwt jwt, @PathVariable long id_meeting) {
		return service.getMeetingDetail(id_meeting);
	}
	
	@PostMapping("/view/meetings/slot")
	@PreAuthorize("hasAuthority('user')")
	@AutoSignup
	public List<MeetingData> viewMeetingsSlot(@AuthenticationPrincipal Jwt jwt, @RequestBody SlotData slot) {
		return service.getMeetingsOnSlot(slot.getDate());
	}
	
	@GetMapping("/view/invitations")
	@PreAuthorize("hasAuthority('user')")
	@AutoSignup
	public List<ReservationPayload> viewInvitations(@AuthenticationPrincipal Jwt jwt) {
		return service.getAllInvitations(jwt, MeetingStatus.INVITATION);
	}
	
	@GetMapping("/view/accepted")
	@PreAuthorize("hasAuthority('user')")
	@AutoSignup
	public List<ReservationPayload> viewAccepted(@AuthenticationPrincipal Jwt jwt) {
		return service.getAllInvitations(jwt, MeetingStatus.ACCEPTED);
	}
	
	@GetMapping("/view/refused")
	@PreAuthorize("hasAuthority('user')")
	@AutoSignup
	public List<ReservationPayload> viewRefused(@AuthenticationPrincipal Jwt jwt) {
		return service.getAllInvitations(jwt, MeetingStatus.REFUSED);
	}
	
	@GetMapping("/invitation/accept/{id_meeting}")
	@PreAuthorize("hasAuthority('user')")
	@AutoSignup
	public boolean acceptInvitation(@AuthenticationPrincipal Jwt jwt, @PathVariable long id_meeting) {
		return service.setInvitationStatus(jwt, id_meeting, MeetingStatus.ACCEPTED);
	}
	
	@GetMapping("/invitation/refuse/{id_meeting}")
	@PreAuthorize("hasAuthority('user')")
	@AutoSignup
	public boolean refuseInvitation(@AuthenticationPrincipal Jwt jwt, @PathVariable long id_meeting) {
		return service.setInvitationStatus(jwt, id_meeting, MeetingStatus.REFUSED);
	}

}
