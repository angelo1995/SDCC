package com.example.demo.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.annotations.FoundbleUser;
import com.example.demo.data.DayofWeek;
import com.example.demo.data.MeetingData;
import com.example.demo.data.MeetingDetailData;
import com.example.demo.data.ReservationPayload;
import com.example.demo.data.ReservationResponse;
import com.example.demo.data.WindowWeekResponse;
import com.example.demo.entities.Costumer;
import com.example.demo.entities.Meeting;
import com.example.demo.entities.MeetingRelation;
import com.example.demo.entities.Slot;
import com.example.demo.exceptions.BookingException;
import com.example.demo.repositories.CostumerRepository;
import com.example.demo.repositories.MeetingRelationRepository;
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
	private MeetingRelationRepository meetingRelationalRepository;

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
		if(reservation.getTitle() == null || reservation.getTitle().isBlank()) {
			throw new BookingException("campo obbligatorio omesso");
		}
		if(reservation.getGuests().isEmpty()) {
			return;
		}
		List<Long> list = reservation.getGuests().stream().sorted().toList();
		long previous = -1;		
		long guest;
		for(int i=0; i < list.size(); ++i) {
			guest = list.get(i);
			if(guest < 0) {
				throw new BookingException("id non valido");
			}
			if(this.userRepository.findById(guest).isEmpty()) {
				throw new BookingException("costumer non presente");
			}
			if(i != 0 && previous == guest) {
				throw new BookingException("duplicato");
			}
			previous = guest;
		}
	}

	@Transactional(readOnly = false)
	@FoundbleUser
	public ReservationResponse bookingMeeting(Jwt jwt, ReservationPayload reservation) {
		checkBookingReservation(reservation);
		String email = jwt.getClaimAsString("email");
		Optional<Costumer> costumer_optional = userRepository.findByEmail(email);
		Slot[] slots = null;
		Slot slot = null;
		Costumer costumer = null;
		Timestamp date = null;
		final int SLOT_LENGHT = reservation.getDuration();
		Calendar reservation_date = new GregorianCalendar();
		reservation_date.setTimeInMillis(reservation.getDate().getTime());
		slots = new Slot[SLOT_LENGHT];
		date = reservation.getDate();
		costumer = costumer_optional.get();
		boolean overlap = false;

		for(int i=0; i < SLOT_LENGHT; ++i) {
			slot = new Slot();
			slot.setDate(date);
			slot.setOccupied(true);
			slots[i] = slot;
			date = new Timestamp(date.getTime() + ONE_HOUR_IN_MILLIS);
		}
		List<Timestamp> list_slot = List.of(slots).stream().map((s)-> s.getDate()).toList();
		for(Meeting meeting : costumer.getReservations()) {
			overlap = meeting.getSlots().stream().map((s)-> s.getDate()).anyMatch((t)-> list_slot.contains(t));
			if(overlap) {
				throw new BookingException("prenotazione non effettuabile");
			}
		}

		List<Costumer> list_costumer = reservation.getGuests().stream().map((id)-> userRepository.findById(id).get()).toList();
		
		Meeting reservationDB = new Meeting();
		reservationDB.setTitle(reservation.getTitle());
		reservationDB.setDescription(reservation.getDescription());
		reservationDB.setSlots(List.of(slots));
		reservationDB.setUser(costumer);

		costumer.getReservations().add(reservationDB);

		for(int i=0; i < SLOT_LENGHT; ++i) {
			slotRepository.save(slots[i]);
		}
		
		List<MeetingRelation> list = list_costumer.stream().map((guest)-> new MeetingRelation(guest, reservationDB, 0)).toList();
		reservationDB.setMeetings(list);
		userRepository.save(costumer);
		reservationRepository.save(reservationDB);
		list.forEach((mr) -> meetingRelationalRepository.save(mr));
		MeetingData meetingData = new MeetingData(reservationDB);
		return new ReservationResponse(true, "prenotazione effettuata", meetingData);
	}

	@Transactional(readOnly = true)
	@FoundbleUser
	public List<ReservationPayload> getAllReservation(Jwt jwt){
		String email = jwt.getClaimAsString("email");
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

	@Transactional(readOnly = true)
	public MeetingDetailData getMeetingDetail(long id) {
		Optional<Meeting> optional_meeting = reservationRepository.findById(id);
		MeetingDetailData meetingDetail = optional_meeting.map(MeetingDetailData::new).orElseThrow(() -> new BookingException("meeting non presente"));
		return meetingDetail;
	}

	@Transactional(readOnly = true)
	public List<MeetingData> getMeetingsOnSlot(Timestamp date) {
		List<Meeting> meetings = reservationRepository.findBySlots_Date(date);
		return meetings.stream()
				.map(MeetingData::new)
				.toList();
	}

	@Transactional(readOnly = true)
	@FoundbleUser
	public List<ReservationPayload> getAllInvitations(Jwt jwt, int status) {
		String email = jwt.getClaimAsString("email");
		Costumer costumer = userRepository.findByEmail(email).get();
		List<ReservationPayload> list = costumer.getMeetings().stream()
				.filter((m)-> m.getStatus() == status)
				.map(ReservationPayload::new)
				.toList();
		return list;
	}
	
	@Transactional(readOnly = false)
	@FoundbleUser
	public boolean setInvitationStatus(Jwt jwt, long id_meeting, int status) {
		String email = jwt.getClaimAsString("email");
		Costumer costumer = userRepository.findByEmail(email).get();
		MeetingRelation meeting = null;
		List<MeetingRelation> list = costumer.getMeetings().stream().filter((m)-> m.getMeeting().getId() == id_meeting).toList();
		if(list.size() == 0) {
			throw new BookingException("meeting non trovato");
		}
		if(list.size() > 1) {
			throw new RuntimeException();
		}
		meeting = list.get(0);
		if(meeting.getStatus() != 0) {
			throw new BookingException("meeting non modificabile");
		}
		if(status < 0 || status > 2) {
			throw new BookingException("staus non accettabile");
		}
		meeting.setStatus(status);
		meetingRelationalRepository.save(meeting);
		return true;
	}

}
