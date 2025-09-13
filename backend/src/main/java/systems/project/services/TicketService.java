package systems.project.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import systems.project.models.Ticket;
import systems.project.repositories.TicketRepository;

import java.sql.SQLDataException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;


    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }


    public CompletableFuture<Map<String, List<Ticket>>> getTickets(){
        return CompletableFuture.supplyAsync(ticketRepository::findAll)
                .thenApply(ticket -> Map.of("tickets", ticket))
                .exceptionally(ex -> Map.of("tickets", null));
    }


    public CompletableFuture<Map<String, Boolean>> addTicket(Ticket ticket){
        return CompletableFuture.supplyAsync(() -> ticketRepository.save(ticket))
                .thenApply(ticket1 -> Map.of("status", true))
                .exceptionally(ex -> Map.of("status", false));

    }


}
