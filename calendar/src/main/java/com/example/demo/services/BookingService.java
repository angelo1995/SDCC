package com.example.demo.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.data.DayofWeek;
import com.example.demo.data.ReservationPayload;
import com.example.demo.data.ReservationResponse;
import com.example.demo.data.WindowWeekResponse;
import com.example.demo.entities.Costumer;
import com.example.demo.entities.Meeting;
import com.example.demo.entities.Slot;
import com.example.demo.exceptions.BookingException;
import com.example.demo.repositories.CostumerRepository;
import com.example.demo.repositories.ReservationRepository;
import com.example.demo.repositories.SlotRepository;

@Service
public class BookingService {

	private static final int MIN_DURATION = 1;
	private static final int MAX_DURATION = 4;
	private static final int OPEN_HOUR = 8;
	private static final int CLOSED_HOUR = 19;
	private static final long ONE_HOUR_IN_MILLIS = 60L*60L*1_000L;

	@Autowired
	private CostumerRepository userRepository;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private SlotRepository slotRepository;
	
	private void checkBookingReservation(ReservationPayload reservation) {
		if(reservation == null) {
			throw new BookingException("prenotazione non effettuabile");
		}
		Calendar now = new GregorianCalendar();
		Calendar reservation_date = new GregorianCalendar();
		reservation_date.setTimeInMillis(reservation.getDate().getTime());
		final int RESERVATION_HOUR = reservation_date.get(Calendar.HOUR_OF_DAY);
		final int SLOT_LENGHT = reservation.getDuration();
		if(reservation_date.compareTo(now) < 0) {
			throw new BookingException("impossibile prenotare uno slot passato");
		}
		if(SLOT_LENGHT < MIN_DURATION || SLOT_LENGHT > MAX_DURATION) {
			throw new BookingException("prenotazione non effettuabile");
		}
		if(RESERVATION_HOUR < OPEN_HOUR || RESERVATION_HOUR + SLOT_LENGHT > CLOSED_HOUR) {
			throw new BookingException("orario non accettabile");
		}
		if(Calendar.SUNDAY == reservation_date.get(Calendar.DAY_OF_WEEK)) {
			throw new BookingException("non è prenotabile di domenica");
		}
	}	

	@Transactional(readOnly = false)
	public ReservationResponse bookingReservation(ReservationPayload reservation) {
		checkBookingReservation(reservation);
		String email = "test";
		Optional<Costumer> costumer_optional = userRepository.findByEmail(email);
		Optional<Slot> slot_optional = null;
		Slot[] slots = null;
		Slot slot = null;
		Costumer costumer = null;
		Timestamp date = null;
		Meeting reservationDB = null;
		final int SLOT_LENGHT = reservation.getDuration();
		Calendar reservation_date = new GregorianCalendar();
		reservation_date.setTimeInMillis(reservation.getDate().getTime());
		slots = new Slot[SLOT_LENGHT];
		date = reservation.getDate();
		costumer = costumer_optional.get();
		
		for(int i=0; i < SLOT_LENGHT; ++i) {
			slot_optional = slotRepository.findByDate(date);
			if(slot_optional.isEmpty()) {
				slot = new Slot();
				slot.setDate(date);
				slot.setOccupied(true);
				slots[i] = slot;
			} else {
				slot = slot_optional.get();
				if(slot.isOccupied()){
					throw new BookingException("prenotazione non effettuabile");
				} else {
					slot.setOccupied(true);
					slots[i] = slot;
				}
			}
			date = new Timestamp(date.getTime() + ONE_HOUR_IN_MILLIS);
		}
		
		reservationDB = new Meeting();
		reservationDB.setDescription(reservation.getDescription());
		reservationDB.setSlots(List.of(slots));
		reservationDB.setUser(costumer);
		costumer.getReservations().add(reservationDB);

		for(int i=0; i < SLOT_LENGHT; ++i) {
			slotRepository.save(slots[i]);
		}
		userRepository.save(costumer);
		reservationRepository.save(reservationDB);
		return new ReservationResponse(true, "prenotazione effettuata");
	}

	@Transactional(readOnly = true)
	public List<ReservationPayload> getAllReservation(){
		String email = "test";
		Costumer costumer = userRepository.findByEmail(email).get();
		List<ReservationPayload> list = costumer.getReservations().stream().map(ReservationPayload::new).toList();
		return list;
	}

	@Transactional(readOnly = true)
	public WindowWeekResponse getWindowWeek(Timestamp start, Timestamp end) {
		if(start == null || end == null) {
			throw new NullPointerException();
		}
		int index_week = 0;
		List<List<DayofWeek>> weeks = new ArrayList<>();
		List<Slot> slots = slotRepository.findAllByOrderByDateAsc();		
		List<DayofWeek> week = new ArrayList<>();
		WindowWeekResponse response = new WindowWeekResponse();
		Calendar item = new GregorianCalendar();
		Calendar startWeek = new GregorianCalendar();
		Calendar endWeek = new GregorianCalendar();
		Calendar currentWeek = new GregorianCalendar();
		Calendar subStart = new GregorianCalendar();
		Calendar subEnd = new GregorianCalendar();
		
		startWeek.setTimeInMillis(start.getTime());
		startWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		startWeek.set(Calendar.HOUR_OF_DAY, OPEN_HOUR);
		startWeek.set(Calendar.MINUTE, 0);
		startWeek.set(Calendar.SECOND, 0);
		startWeek.set(Calendar.MILLISECOND, 0);
		
		endWeek.setTimeInMillis(end.getTime());
		endWeek.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		endWeek.set(Calendar.HOUR_OF_DAY, CLOSED_HOUR);
		endWeek.set(Calendar.MINUTE, 0);
		endWeek.set(Calendar.SECOND, 0);
		endWeek.set(Calendar.MILLISECOND, 0);
		
		subStart.setTimeInMillis(startWeek.getTimeInMillis());
		subEnd.setTimeInMillis(subStart.getTimeInMillis());
		subEnd.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		subEnd.set(Calendar.HOUR_OF_DAY, CLOSED_HOUR);
		
		currentWeek.setTimeInMillis(startWeek.getTimeInMillis());
		while(currentWeek.compareTo(endWeek) < 0) {
			week = newWeek();
			weeks.add(week);
			currentWeek.add(Calendar.DAY_OF_MONTH, 7);
		}
		week = weeks.get(0);
		for(Slot slot: slots) {
			item.setTimeInMillis(slot.getDate().getTime());
			if(item.compareTo(startWeek) < 0) {
				continue;
			}
			if(item.compareTo(endWeek) > 0) {
				break;
			}
			if(!slot.isOccupied()) {
				continue;
			}
			if(item.compareTo(subStart) < 0) {
				throw new RuntimeException();
			}
			while(item.compareTo(subEnd) > 0) {
				index_week++;
				week = weeks.get(index_week);
				subStart.add(Calendar.DAY_OF_MONTH, 7);
				subEnd.add(Calendar.DAY_OF_MONTH, 7);
			}
			setOccuped(week, item);
		}
		response.setWeeks(weeks);
		return response;
	}
	
	private ArrayList<DayofWeek> newWeek(){
		ArrayList<DayofWeek> week = new ArrayList<>();
		for(int i=0; i < CLOSED_HOUR - OPEN_HOUR; ++i) {
			week.add(new DayofWeek());
		}
		return week;
	}

	private void setOccuped(List<DayofWeek> week, Calendar item) {
		int day = item.get(Calendar.DAY_OF_WEEK);
		int hour = item.get(Calendar.HOUR_OF_DAY);
		int indexHour = hour - OPEN_HOUR;
		switch(day) {
		case Calendar.MONDAY:
			week.get(indexHour).setMonday("occupied");
			break;
		case Calendar.TUESDAY:
			week.get(indexHour).setTuesday("occupied");
			break;
		case Calendar.WEDNESDAY:
			week.get(indexHour).setWednesday("occupied");
			break;
		case Calendar.THURSDAY:
			week.get(indexHour).setThursday("occupied");
			break;
		case Calendar.FRIDAY:
			week.get(indexHour).setFriday("occupied");
			break;
		case Calendar.SATURDAY:
			week.get(indexHour).setSaturday("occupied");
			break;
		default:
			throw new RuntimeException();
		}
	}

}
