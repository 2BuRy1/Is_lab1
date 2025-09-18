package systems.project.controllers;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;







import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import systems.project.models.CloneRequest;
import systems.project.models.ErrorResponseDto;
import systems.project.models.SellRequestDTO;
import systems.project.models.Ticket;
import systems.project.services.TicketEventService;
import systems.project.services.TicketService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin(origins = {"*"})
public class TicketController {


    private final TicketEventService events;
    private final TicketService ticketService;

    public TicketController(TicketEventService events, TicketService ticketService) {
        this.events = events;
        this.ticketService = ticketService;
    }


    @GetMapping(path = "/tickets/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        System.out.println("meow");
        return events.subscribe();
    }

    @GetMapping("/get_tickets")
    public CompletableFuture<ResponseEntity<Map<String, List<Ticket>>>> getTickets() {
        return ticketService.getTickets().thenApply(ResponseEntity::ok);

    }


    @PostMapping("/add_ticket")
    public CompletableFuture<ResponseEntity<Map<String, Boolean>>> addTicket(@RequestBody Ticket ticket) {
        return ticketService.addTicket(ticket)
                .thenApply(res -> {
                    if (res.get("status")) {
                        events.publishChange("add", null);
                        return ResponseEntity.ok(res);
                    }
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
                .thenApply(ok -> {
                    if (ok) {
                        events.publishChange("update", id);
                        return ResponseEntity.ok().build();
                    }
                    return ResponseEntity.badRequest().build();
                });
    }

    @DeleteMapping("/delete_ticket/{id}")
    public CompletableFuture<ResponseEntity<ErrorResponseDto>> deleteTicket(@PathVariable Integer id) {
        return ticketService.removeTicket(id)
                .thenApply(ok -> {
                    if (ok) {
                        events.publishChange("delete", id.intValue());
                        return ResponseEntity.ok().build();
                    }
                    return ResponseEntity.badRequest().
                            body(ErrorResponseDto.builder()
                                    .title("Возникла ошибка")
                                    .errorMessage("Ошибка при удалении объекта, возможно его не существует")
                                    .build()
                            );
                });
    }

    @DeleteMapping("/delete_by_comment")
    public CompletableFuture<ResponseEntity<ErrorResponseDto>> deleteByComment(@RequestParam String commentEq) {
        return ticketService.deleteAllByComment(commentEq)
                .thenApply(ok -> {
                    if (ok) {
                        events.publishChange("bulk-delete", null);
                        return ResponseEntity.ok().build();
                    }
                    return ResponseEntity.badRequest().
                            body(ErrorResponseDto.builder()
                                    .title("Возникла ошибка")
                                    .errorMessage("Возможно не было найдено объектов с таким же Comment")
                                    .build()
                            );
                });
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
                .thenApply(ok -> {

                    if (ok) {
                        events.publishChange("ticket-sell", null);
                        return ResponseEntity.ok(Map.of("status", true));


                    }
                    return ResponseEntity.badRequest().body(Map.of("status", false));
                });
    }

    @PostMapping("/clone_vip")
    public CompletableFuture<ResponseEntity<Ticket>> cloneVip(@RequestBody CloneRequest req) {
        return ticketService.cloneVip(req.ticketId)
                .thenApply(copy -> {
                    if (copy != null ) {
                        events.publishChange("vip-clone", null);
                        return ResponseEntity.ok(copy);
                    }

                    return ResponseEntity.badRequest().body(null);

                });
    }



}
