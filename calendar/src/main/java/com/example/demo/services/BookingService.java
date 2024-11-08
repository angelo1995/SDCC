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
import com.example.demo.data.MeetingDetailData;
import com.example.demo.data.MeetingPayload;
import com.example.demo.data.MeetingResponse;
import com.example.demo.data.WindowWeekResponse;
import com.example.demo.entities.Costumer;
import com.example.demo.entities.Meeting;
import com.example.demo.entities.MeetingRelation;
import com.example.demo.entities.Slot;
import com.example.demo.enumerations.MeetingPriority;
import com.example.demo.enumerations.MeetingStatus;
import com.example.demo.exceptions.BookingException;
import com.example.demo.repositories.CostumerRepository;
import com.example.demo.repositories.MeetingRelationRepository;
import com.example.demo.repositories.MeetingRepository;
import com.example.demo.repositories.SlotRepository;
import com.example.demo.utils.CostantProvider;

@Service
public class BookingService {
	
	private final int MIN_DURATION;
	private final int MAX_DURATION;
	private final int OPEN_HOUR;
	private final int CLOSED_HOUR;
	private final long ONE_HOUR_IN_MILLIS = 60L*60L*1_000L;

	@Autowired
	private CostumerRepository userRepository;

	@Autowired
	private MeetingRepository meetingRepository;

	@Autowired
	private MeetingRelationRepository meetingRelationalRepository;

	@Autowired
	private SlotRepository slotRepository;
	
	public BookingService(CostantProvider costantProvider) {
		MIN_DURATION = costantProvider.getMinDuration();
		MAX_DURATION = costantProvider.getMaxDuration();
		OPEN_HOUR = costantProvider.getOpenHour();
		CLOSED_HOUR = costantProvider.getClosedHour();
	}

	private void checkBookingReservation(MeetingPayload meeting) {
		//log.info("" + meeting);
		if(meeting == null) {
			throw new BookingException("prenotazione non effettuabile");
		}
		Calendar now = new GregorianCalendar();
		Calendar reservation_date = new GregorianCalendar();
		reservation_date.setTimeInMillis(meeting.getDate().getTime());
		final int RESERVATION_HOUR = reservation_date.get(Calendar.HOUR_OF_DAY);
		final int SLOT_LENGHT = meeting.getDuration();
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
		if(meeting.getTitle() == null || meeting.getTitle().isBlank()) {
			throw new BookingException("campo obbligatorio omesso");
		}
		if(meeting.getGuests().isEmpty()) {
			return;
		}
		List<Long> list = meeting.getGuests().stream().sorted().toList();
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
	public MeetingResponse bookingMeeting(Jwt jwt, MeetingPayload meetingPayload) {
		checkBookingReservation(meetingPayload);
		String email = jwt.getClaimAsString("email");
		Optional<Costumer> costumer_optional = userRepository.findByEmail(email);
		Slot[] slots = null;
		Slot slot = null;
		Costumer costumer = null;
		Timestamp date = null;
		final int SLOT_LENGHT = meetingPayload.getDuration();
		Calendar reservation_date = new GregorianCalendar();
		reservation_date.setTimeInMillis(meetingPayload.getDate().getTime());
		slots = new Slot[SLOT_LENGHT];
		date = meetingPayload.getDate();
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
			overlap = meeting.getSlots()
					.stream()
					.filter((s)-> s.isOccupied())
					.map((s)-> s.getDate())
					.anyMatch((t)-> list_slot.contains(t));
			if(overlap) {
				throw new BookingException("prenotazione non effettuabile");
			}
		}

		List<Costumer> list_costumer = meetingPayload.getGuests().stream()
				.map((id)-> userRepository.findById(id).get())
				.toList();

		Meeting reservationDB = new Meeting();
		reservationDB.setTitle(meetingPayload.getTitle());
		reservationDB.setDescription(meetingPayload.getDescription());
		reservationDB.setSlots(List.of(slots));
		reservationDB.setUser(costumer);
		if(meetingPayload.getPriority() != null) {
			reservationDB.setPriority(MeetingPriority.getPriorityFromString(meetingPayload.getPriority()));
		}

		costumer.getReservations().add(reservationDB);

		for(int i=0; i < SLOT_LENGHT; ++i) {
			slotRepository.save(slots[i]);
		}

		List<MeetingRelation> list = list_costumer.stream()
				.map((guest)-> new MeetingRelation(guest, reservationDB, MeetingStatus.INVITATION))
				.toList();
		reservationDB.setMeetings(list);
		userRepository.save(costumer);
		meetingRepository.save(reservationDB);
		list.forEach((mr) -> meetingRelationalRepository.save(mr));
		MeetingDetailData meetingData = new MeetingDetailData(reservationDB);
		return new MeetingResponse(true, "prenotazione effettuata", meetingData);
	}

	@Transactional(readOnly = true)
	@FoundbleUser
	public List<MeetingPayload> getAllReservation(Jwt jwt){
		String email = jwt.getClaimAsString("email");
		Costumer costumer = userRepository.findByEmail(email).get();
		List<MeetingPayload> list = costumer.getReservations().stream().map(MeetingPayload::new).toList();
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
		Optional<Meeting> optional_meeting = meetingRepository.findById(id);
		MeetingDetailData meetingDetail = optional_meeting.map(MeetingDetailData::new).orElseThrow(() -> new BookingException("meeting non presente"));
		return meetingDetail;
	}

	@Transactional(readOnly = true)
	public List<MeetingDetailData> getMeetingsOnSlot(Timestamp date) {
		List<Meeting> meetings = meetingRepository.findBySlots_Date(date);
		return meetings.stream()
				.map(MeetingDetailData::new)
				.toList();
	}

	@Transactional(readOnly = true)
	@FoundbleUser
	public List<MeetingDetailData> getAllInvitations(Jwt jwt, MeetingStatus status) {
		String email = jwt.getClaimAsString("email");
		Costumer costumer = userRepository.findByEmail(email).get();
		Calendar now = new GregorianCalendar();
		Calendar end_meeting = new GregorianCalendar();
		List<MeetingDetailData> list = costumer.getMeetings().stream()
				.filter((m)-> {
					List<Timestamp> timestamps = m.getMeeting().getSlots().stream().map((s)-> s.getDate()).sorted().toList();
					Timestamp end = timestamps.get(timestamps.size() - 1);
					end_meeting.setTimeInMillis(end.getTime() + ONE_HOUR_IN_MILLIS);
					return m.getStatus().equals(status) && m.isVisible() && end_meeting.compareTo(now) > 0;
				})
				.map(MeetingDetailData::new)
				.toList();
		return list;
	}

	@Transactional(readOnly = false)
	@FoundbleUser
	public boolean setInvitationStatus(Jwt jwt, long id_meeting, MeetingStatus status) {
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
		if(!meeting.getStatus().equals(MeetingStatus.INVITATION)) {
			throw new BookingException("meeting non modificabile");
		}
		meeting.setStatus(status);
		meetingRelationalRepository.save(meeting);
		return true;
	}

	@Transactional(readOnly = false)
	@FoundbleUser
	public boolean invisible(Jwt jwt, long id_meeting) {
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
		meeting.setVisible(false);
		meetingRelationalRepository.save(meeting);
		return true;
	}

	@Transactional(readOnly = false)
	@FoundbleUser
	public boolean cancelMeeting(Jwt jwt, long id_meeting) {
		String email = jwt.getClaimAsString("email");
		Costumer costumer = userRepository.findByEmail(email).get();
		Meeting meeting = meetingRepository.findById(id_meeting).orElseThrow(BookingException::new);
		if(!meeting.getUser().equals(costumer)) {
			throw new BookingException("costumer not owner");
		}
		meeting.setCancelled(true);
		meetingRepository.save(meeting);
		meeting.getSlots().stream().forEach((s)-> {
			s.setOccupied(false);
			slotRepository.save(s);
		});
		return true;
	}

}
