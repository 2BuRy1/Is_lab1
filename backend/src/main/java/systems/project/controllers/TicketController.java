package systems.project.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import systems.project.exceptions.InvalidDataException;
import systems.project.models.Ticket;
import systems.project.repositories.TicketRepository;
import systems.project.services.TicketService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin(origins = {"*"})
public class TicketController {


    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }


    @GetMapping("/get_tickets")
    public CompletableFuture<ResponseEntity<Map<String, List<Ticket>>>> getTickets(){
        return ticketService.getTickets().thenApply(ResponseEntity::ok);

    }

    @PostMapping("/add_ticket")
    public CompletableFuture<ResponseEntity<Map<String, Boolean>>> addTicket(@RequestBody Ticket ticket){
        return ticketService.addTicket(ticket)
                .thenApply(res -> {
                    if(res.get("status")) return ResponseEntity.ok(res);

                    return ResponseEntity.badRequest().body(Map.of("status", false));
                });

    }



}
