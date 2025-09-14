package systems.project.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import systems.project.exceptions.InvalidDataException;
import systems.project.models.CloneRequest;
import systems.project.models.SellRequestDTO;
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
    public CompletableFuture<ResponseEntity<Map<String, List<Ticket>>>> getTickets() {
        return ticketService.getTickets().thenApply(ResponseEntity::ok);

    }

    @PostMapping("/add_ticket")
    public CompletableFuture<ResponseEntity<Map<String, Boolean>>> addTicket(@RequestBody Ticket ticket) {

        return ticketService.addTicket(ticket)
                .thenApply(res -> {
                    if (res.get("status")) return ResponseEntity.ok(res);

                    return ResponseEntity.badRequest().body(Map.of("status", false));
                });


    }


    @GetMapping("get_ticket/{id}")
    public CompletableFuture<ResponseEntity<Ticket>> getTicket(@PathVariable Integer id) {
        return ticketService.getTicket(id).thenApply(res -> {
            if (res != null) return ResponseEntity.ok(res);
            return ResponseEntity.badRequest().body(null);
        });
    }

    @PostMapping("/update_ticket/{id}")
    public CompletableFuture<ResponseEntity<Void>> updateTicket(@PathVariable Integer id, @RequestBody Ticket ticket) {

        return ticketService.updateTicket(id, ticket)
                .thenApply(res -> res ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build()
                );
    }

    @DeleteMapping("/delete_ticket/{id}")
    public CompletableFuture<ResponseEntity<String>> deleteTicket(@PathVariable Long id) {
        return ticketService.removeTicket(id)
                .thenApply(res -> res ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Ошибка при удалении объекта, возможно его не существует")
                );


    }

    @DeleteMapping("/delete_by_comment")
    public CompletableFuture<ResponseEntity<String>> delete_by_comment(@RequestParam String commentEq){
        return ticketService.deleteAllByComment(commentEq)
                .thenApply(res -> res ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Возможно не было найдено объектов с таким же Comment"));
    }

    @GetMapping("/min_event_ticket")
    public CompletableFuture<ResponseEntity<Ticket>> minEventTicket() {
        return ticketService.getWithMinEvent()
                .thenApply(t -> t != null ? ResponseEntity.ok(t)
                        : ResponseEntity.badRequest().body(null));
    }

    @GetMapping("/count_comment_less")
    public CompletableFuture<ResponseEntity<Map<String, Long>>> countCommentLess(@RequestParam String comment) {
        return ticketService.countByCommentLess(comment).thenApply(ResponseEntity::ok);
    }

    @PostMapping("/sell_ticket")
    public CompletableFuture<ResponseEntity<Map<String, Boolean>>> sellTicket(@RequestBody SellRequestDTO req) {
        return ticketService.sellTicket(req.ticketId, req.personId, req.amount)
                .thenApply(ok -> ok ? ResponseEntity.ok(Map.of("status", true))
                        : ResponseEntity.badRequest().body(Map.of("status", false)));
    }

    @PostMapping("/clone_vip")
    public CompletableFuture<ResponseEntity<Ticket>> cloneVip(@RequestBody CloneRequest req) {
        return ticketService.cloneVip(req.ticketId)
                .thenApply(copy -> copy != null ? ResponseEntity.ok(copy)
                        : ResponseEntity.badRequest().body(null));
    }



}
