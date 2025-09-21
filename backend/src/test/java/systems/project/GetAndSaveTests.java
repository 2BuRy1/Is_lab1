package systems.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import systems.project.models.Event;
import systems.project.models.Ticket;
import systems.project.models.Person;
import systems.project.models.Location;
import systems.project.models.Venue;
import systems.project.repositories.PersonRepository;
import systems.project.repositories.TicketRepository;
import systems.project.repositories.EventRepository;
import systems.project.repositories.VenueRepository;
import systems.project.repositories.LocationRepository;


import systems.project.services.EventService;
import systems.project.services.PersonService;
import systems.project.services.TicketService;
import systems.project.services.VenueService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verifyNoInteractions;


@ExtendWith(MockitoExtension.class)
class GetAndSaveTests {

    @InjectMocks
    EventService eventService;


    @InjectMocks
    TicketService ticketService;

    @InjectMocks
    PersonService personService;

    @InjectMocks
    VenueService venueService;

    @Mock
    TicketRepository ticketRepository;

    @Mock
    PersonRepository personRepository;

    @Mock
    EventRepository eventRepository;

    @Mock
    VenueRepository venueRepository;

    @Mock
    LocationRepository locationRepository;



    @Test void testGetAllTickets() throws ExecutionException, InterruptedException {
        //Given
        List<Ticket> tickets = mock();

        //When
        when(ticketRepository.findAllBy()).thenReturn(CompletableFuture.completedFuture(tickets));
        var res = ticketService.getTickets().get();


        //Then
        assertNotNull(res.get("tickets"));
        verify(ticketRepository).findAllBy();

    }

    @Test
    void testGetAllEvents() throws ExecutionException, InterruptedException {
        //Given
        List<Event> events = mock();

        //When
        when(eventRepository.findAllBy()).thenReturn(CompletableFuture.completedFuture(events));
        var res = eventService.getEvents().get();

        //Then
        assertNotNull(res.get("events"));
        verify(eventRepository).findAllBy();

    }

    @Test
    void testGetAllPersons() throws ExecutionException, InterruptedException {
        //Given
        List<Person> persons = mock();

        //When
        when(personRepository.findAllBy()).thenReturn(CompletableFuture.completedFuture(List.of(new Person())));
        var res = personService.getPersons().get();


        //Then
        assertNotNull(res.get("persons"));
    }

    @Test
    void testGetAllVenues() throws ExecutionException, InterruptedException {
        //Given
        List<Venue> venues = mock();

        //When
        when(venueRepository.findAllBy()).thenReturn(CompletableFuture.completedFuture(venues));
        var res = venueService.getVenues().get();

        //Then
        assertNotNull(res.get("venues"));
        verify(venueRepository).findAllBy();



    }


    @Test
    void testAddTicket() throws ExecutionException, InterruptedException {
        // Given
        Ticket ticket = mock();

        // When
        when(ticketRepository.save(any(Ticket.class))).
                thenReturn(ticket);
        var res = ticketService.addTicket(ticket).get();



        // Then
        assertTrue(res.get("status"));
        verify(ticketRepository).save(same(ticket));
    }



    @Test
    void testAddEvent() throws ExecutionException, InterruptedException {
        //Given
        Event event = mock();

        //When
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        var res = eventService.addEvent(event).get();

        //Then
        assertTrue(res.get("status"));
        verify(eventRepository).save(same(event));
    }

    @Test
    void testAddPerson() throws Exception {
        // Given
        Location loc = new Location();
        Person person = new Person();
        person.setLocation(loc);

        when(locationRepository.save(any(Location.class))).
                thenReturn(loc);
        when(personRepository.save(any(Person.class))).
                thenReturn(person);

        // When
        var res = personService.addPerson(person).get();

        // Then
        assertTrue(res.get("status"));
        verify(locationRepository).save(same(loc));
        verify(personRepository).save(same(person));
    }

    @Test
    void testAddVenue() throws ExecutionException, InterruptedException {
        //Given
        Venue venue = mock();

        //When
        when(venueRepository.save(any(Venue.class))).thenReturn(venue);
        var res = venueService.addVenue(venue).get();

        //Then
        assertTrue(res.get("status"));
        verify(venueRepository).save(same(venue));
    }

    @Test
    void testFailAddTicket() throws ExecutionException, InterruptedException {
        // Given
        Ticket ticket = mock();

        // When
        when(ticketRepository.save(any(Ticket.class))).thenThrow(new RuntimeException("db down"));
        var res = ticketService.addTicket(ticket).get();


        // Then
        assertFalse(res.get("status"));
        verify(ticketRepository).save(same(ticket));
    }

    @Test
    void testFailAddEvent() throws ExecutionException, InterruptedException {
        //Given
        Event event = mock();

        //When
        when(eventRepository.save(any(Event.class))).thenThrow(new RuntimeException("failed to add"));
        var res = eventService.addEvent(event).get();

        assertFalse(res.get("status"));
        verify(eventRepository).save(same(event));
    }

    @Test
    void testFailAddPersonOnLocationFailure() throws Exception {
        // Given
        Person person = new Person();
        Location location = new Location();
        person.setLocation(location);

        // When
        when(locationRepository.save(any(Location.class))).thenThrow(new RuntimeException("failed to add"));

        var res = personService.addPerson(person).get();

        // Then
        assertFalse(res.get("status"));
        verify(locationRepository).save(same(location));
        verifyNoInteractions(personRepository);
    }

    @Test
    void testFailAddPersonOnPersonFailure() throws Exception {
        // Given
        Person person = new Person();
        Location location = new Location();
        person.setLocation(location);

        // When
        when(locationRepository.save(any(Location.class))).
                thenReturn(location);
        when(personRepository.save(any(Person.class))).thenThrow(new RuntimeException("failed to add"));

        var res = personService.addPerson(person).get();

        // Then
        assertFalse(res.get("status"));
        verify(locationRepository).save(same(location));
        verify(personRepository).save(same(person));
    }

    @Test
    void testFailAddVenue() throws ExecutionException, InterruptedException {
        //Given
        Venue venue = mock();

        //When
        when(venueRepository.save(any(Venue.class))).thenThrow(new RuntimeException("failed to add"));
        var res = venueService.addVenue(venue).get();

        assertFalse(res.get("status"));
        verify(venueRepository).save(same(venue));
    }










}
