package systems.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import systems.project.models.Coordinates;
import systems.project.models.Person;
import systems.project.models.Ticket;
import systems.project.models.TicketType;
import systems.project.repositories.PersonRepository;
import systems.project.repositories.TicketRepository;
import systems.project.services.TicketService;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class TicketServiceTests {

    @Mock
    TicketRepository ticketRepository;

    @Mock
    PersonRepository personRepository;

    @InjectMocks
    TicketService service;

    @Test
    void testGetTicket() throws ExecutionException, InterruptedException {
        //Given
        Ticket ticket = mock();

        //When
        when(ticketRepository.findById(any(Integer.class))).thenReturn(CompletableFuture.
                completedFuture(Optional.of(ticket)));
        var res = service.getTicket(5).get();

        //Then
        assertEquals(res, ticket);
    }

    @Test
    void testGetEmptyTicket() throws ExecutionException, InterruptedException {
        //When
        when(ticketRepository.findById(any(Integer.class))).thenReturn(CompletableFuture.
                completedFuture(Optional.empty()));
        var res = service.getTicket(5).get();

        assertNull(res);
    }

    @Test
    void testFailGetTicket() throws ExecutionException, InterruptedException {
        //When
        when(ticketRepository.findById(any(Integer.class))).
                thenReturn(CompletableFuture.failedFuture(new RuntimeException("fail")));
        var res = service.getTicket(5).get();

        //Then
        assertNull(res);
    }

    @Test
    void testUpdateTicket() throws ExecutionException, InterruptedException {
        //Given
        Ticket ticket = mock();

        //When
        when(ticketRepository.existsById(any(Integer.class))).
                thenReturn(CompletableFuture.completedFuture(true));
        var res = service.updateTicket(5, ticket).get();

        //Then
        assertTrue(res);

    }

    @Test
    void testNotFountWhileUpdateTicket() throws ExecutionException, InterruptedException {
        //Given
        Ticket ticket = mock();

        //When
        when(ticketRepository.existsById(any(Integer.class))).
                thenReturn(CompletableFuture.completedFuture(false));
        var res = service.updateTicket(5, ticket).get();

        //Then
        assertFalse(res);
    }

    @Test
    void testFailUpdateTicket() throws ExecutionException, InterruptedException {
        //Given
        Ticket ticket = mock();

        //When
        when(ticketRepository.existsById(any(Integer.class))).
                thenReturn(CompletableFuture.failedFuture(new RuntimeException("fail")));
        var res = service.updateTicket(5, ticket).get();

        //Then
        assertFalse(res);
    }

    @Test
    void testRemoveTicket() throws ExecutionException, InterruptedException {
        //When
        when(ticketRepository.existsById(anyInt())).thenReturn(CompletableFuture.completedFuture(true));
        when(ticketRepository.deleteById(anyInt())).thenReturn(CompletableFuture.completedFuture(mock()));
        var res = service.removeTicket(5).get();

        //Then
        assertTrue(res);
    }

    @Test
    void testNotFoundWhileDelete() throws ExecutionException, InterruptedException {
        //When
        when(ticketRepository.existsById(anyInt())).thenReturn(CompletableFuture.completedFuture(false));
        var res = service.removeTicket(5).get();

        //Then
        assertFalse(res);
    }

    @Test
    void testFailDelete() throws Exception {
        // When
        when(ticketRepository.existsById(anyInt()))
                .thenReturn(CompletableFuture.completedFuture(true));
        doThrow(new RuntimeException("error happened")).when(ticketRepository).deleteById(anyInt());

        var res = service.removeTicket(5).get();

        // Then
        assertFalse(res);
    }

    @Test
    void testDeleteAllByComment() throws Exception {
        // When
        when(ticketRepository.deleteByComment("abc")).
                thenReturn(CompletableFuture.completedFuture(5L));
        var res = service.deleteAllByComment("abc").get();

        // Then
        assertTrue(res);
    }

    @Test
    void testDeleteAllByCommentNone() throws Exception {
        // When
        when(ticketRepository.deleteByComment("abc")).
                thenReturn(CompletableFuture.completedFuture(0L));
        var res = service.deleteAllByComment("abc").get();

        // Then
        assertFalse(res);
    }

    @Test
    void testDeleteAllByCommentEmpty() throws Exception {
        // When
        var res = service.deleteAllByComment("   ").get();

        // Then
        assertFalse(res);
        verify(ticketRepository, never()).deleteByComment(any());
    }


    @Test
    void testGetWithMinEvent() throws Exception {
        // Given
        Ticket ticket = mock();

        // When
        when(ticketRepository.findFirstByEventIsNotNullOrderByEventIdAsc()).
                thenReturn(CompletableFuture.
                        completedFuture(Optional.of(ticket)));
        var res = service.getWithMinEvent().get();

        // Then
        assertEquals(ticket, res);
    }

    @Test
    void testGetWithMinEventEmpty() throws Exception {
        // When
        when(ticketRepository.findFirstByEventIsNotNullOrderByEventIdAsc()).
                thenReturn(CompletableFuture.
                        completedFuture(Optional.empty()));
        var res = service.getWithMinEvent().get();

        // Then
        assertNull(res);
    }


    @Test
    void testCountByCommentLess() throws Exception {
        // When
        when(ticketRepository.countByCommentLessThan("aaa")).
            thenReturn(CompletableFuture.completedFuture(5L));
        var res = service.countByCommentLess("aaa").get();

        // Then
        assertEquals(5L, res.get("count"));
    }

    @Test
    void testFailCountByCommentLess() throws Exception {
        // When
        when(ticketRepository.countByCommentLessThan(anyString())).
                thenReturn(CompletableFuture.failedFuture(new RuntimeException("fail")));
        var res = service.countByCommentLess("aaa").get();

        // Then
        assertEquals(0L, res.get("count"));
    }


    @Test
    void testSellTicket() throws Exception {
        // Given
        var ticket = new Ticket();
        var person = new Person();

        // When
        when(ticketRepository.findById(1)).
                thenReturn(CompletableFuture.
                        completedFuture(Optional.of(ticket)));
        when(personRepository.findById(1)).
                thenReturn(CompletableFuture.
                        completedFuture(Optional.of(person)));
        var res = service.sellTicket(1, 1, 100f).get();

        // Then
        assertTrue(res);
        assertEquals(100f, ticket.getPrice());
        assertEquals(person, ticket.getPerson());
    }

    @Test
    void testFailSellTicketInvalidAmount() throws Exception {
        // When
        var res = service.sellTicket(1, 1, -5f).get();

        // Then
        assertFalse(res);
    }

    @Test
    void testFailSellTicketNoTicket() throws Exception {
        // When
        when(ticketRepository.findById(1)).
                thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        var res = service.sellTicket(1, 1, 50f).get();

        // Then
        assertFalse(res);
    }

    @Test
    void testFailSellTicketNoPerson() throws Exception {
        // When
        Ticket ticket = mock();
        when(ticketRepository.findById(1)).
                thenReturn(CompletableFuture.
                        completedFuture(Optional.of(ticket)));
        when(personRepository.findById(1)).
                thenReturn(CompletableFuture.
                        completedFuture(Optional.empty()));
        var res = service.sellTicket(1, 1, 50f).get();

        // Then
        assertFalse(res);
    }


    @Test
    void testCloneVip() throws Exception {
        // Given
        var ticket = new Ticket();
        ticket.setId(1);
        ticket.setName("test");
        ticket.setPrice(10f);
        ticket.setCoordinates(new Coordinates());
        ticket.setType(TicketType.USUAL);

        // When
        when(ticketRepository.findById(1)).
                thenReturn(CompletableFuture.
                        completedFuture(Optional.of(ticket)));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        var copy = service.cloneVip(1).get();

        // Then
        assertNotNull(copy);
        assertEquals("test", copy.getName());
        assertEquals(20f, copy.getPrice());
        assertEquals(TicketType.VIP, copy.getType());
        assertNull(copy.getId());
    }

    @Test
    void testCloneVipNotFound() throws Exception {
        // When
        when(ticketRepository.findById(1)).
                thenReturn(CompletableFuture.
                        completedFuture(Optional.empty()));
        var res = service.cloneVip(1).get();

        // Then
        assertNull(res);
    }



}
