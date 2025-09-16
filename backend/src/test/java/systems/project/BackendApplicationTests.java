package systems.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import systems.project.models.*;
import systems.project.repositories.*;
import systems.project.services.EventService;
import systems.project.services.PersonService;
import systems.project.services.TicketService;
import systems.project.services.VenueService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackendApplicationTests {

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
        when(ticketRepository.findAll()).thenReturn(tickets);
        var res = ticketService.getTickets().get();


        //Then
        assertNotNull(res.get("tickets"));
        verify(ticketRepository).findAll();

    }

    @Test
    void testGetAllEvents() throws ExecutionException, InterruptedException {
        //Given
        List<Event> events = mock();

        //When
        when(eventRepository.findAll()).thenReturn(events);
        var res = eventService.getEvents().get();

        //Then
        assertNotNull(res.get("events"));
        verify(eventRepository).findAll();

    }

    @Test
    void testGetAllPersons() throws ExecutionException, InterruptedException {
        //Given
        List<Person> persons = mock();

        //When
        when(personRepository.findAll()).thenReturn(persons);
        var res = personService.getPersons().get();


        //Then
        assertNotNull(res.get("persons"));
    }

    @Test
    void testGetAllVenues() throws ExecutionException, InterruptedException {
        //Given
        List<Venue> venues = mock();

        //When
        when(venueRepository.findAll()).thenReturn(venues);
        var res = venueService.getVenues().get();

        //Then
        assertNotNull(res.get("venues"));
        verify(venueRepository).findAll();



    }


    @Test
    void testAddTicket() throws ExecutionException, InterruptedException {
        // Given
        Ticket ticket = mock();

        // When
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        var res = ticketService.addTicket(ticket).get();



        // Then
        assertTrue(res.get("status"));
        verify(ticketRepository).save(same(ticket));
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

        when(locationRepository.save(any(Location.class))).thenReturn(loc);
        when(personRepository.save(any(Person.class))).thenReturn(person);

        // When
        var res = personService.addPerson(person).get();

        // Then
        assertTrue(res.get("status"));
        verify(locationRepository).save(same(loc));
        verify(personRepository, times(2)).save(same(person));
    }






}
