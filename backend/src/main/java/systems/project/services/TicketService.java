package systems.project.services;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import systems.project.models.Coordinates;
import systems.project.models.Ticket;
import systems.project.models.TicketType;
import systems.project.repositories.PersonRepository;
import systems.project.repositories.TicketRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final PersonRepository personRepository;

    public TicketService(TicketRepository ticketRepository, PersonRepository personRepository) {
        this.ticketRepository = ticketRepository;
        this.personRepository = personRepository;
    }

    public CompletableFuture<Map<String, List<Ticket>>> getTickets() {
        return ticketRepository.findAllBy()
                .thenApply(list -> Map.of("tickets", list))
                .exceptionally(exc -> Map.of("tickets", null));
    }

    @Async
    public CompletableFuture<Map<String, Boolean>> addTicket(Ticket ticket) {
        try {
            ticketRepository.save(ticket);
            return completedFuture(Map.of("status", true));
        } catch (Exception e) {
            return completedFuture(Map.of("status", false));
        }
    }

    public CompletableFuture<Ticket> getTicket(Integer id) {
        return ticketRepository.findById(id)
                .thenApply(res -> res.orElse(null))
                .exceptionally(exc -> null);
    }

    @Async
    public CompletableFuture<Boolean> updateTicket(Integer id, Ticket ticket) {
        return ticketRepository.existsById(id)
                .thenCompose(exists -> {
                    if (!exists) return completedFuture(false);
                    try {
                        ticket.setId(id);
                        ticketRepository.save(ticket);
                        return completedFuture(true);
                    } catch (Exception e) {
                        return completedFuture(false);
                    }
                })
                .exceptionally(exc -> false);
    }

    @Async
    @Transactional
    public CompletableFuture<Boolean> removeTicket(Integer id) {
        return ticketRepository.existsById(id)
                .thenCompose(exists -> {
                    if (!exists) return completedFuture(false);
                    try {
                        ticketRepository.deleteById(id);
                        return completedFuture(true);
                    } catch (Exception e) {
                        return completedFuture(false);
                    }
                })
                .exceptionally(exc -> false);
    }

    @Async
    @Transactional
    public CompletableFuture<Boolean> deleteAllByComment(String comment) {
        String c = comment == null ? "" : comment.trim();
        if (c.isEmpty()) return completedFuture(false);
        return ticketRepository.deleteByComment(c)
                .thenApply(removed -> removed != null && removed > 0)
                .exceptionally(exc -> false);
    }

    @Async
    public CompletableFuture<Ticket> getWithMinEvent() {
        return ticketRepository.findFirstByEventIsNotNullOrderByEventIdAsc()
                .thenApply(res -> res.orElse(null))
                .exceptionally(exc -> null);
    }

    @Async
    public CompletableFuture<Map<String, Long>> countByCommentLess(String comment) {
        return ticketRepository.countByCommentLessThan(comment)
                .thenApply(res -> Map.of("count", res == null ? 0L : res))
                .exceptionally(ex -> Map.of("count", 0L));
    }

    @Async
    public CompletableFuture<Boolean> sellTicket(Integer ticketId, Integer personId, float amount) {
        if (amount <= 0f) return completedFuture(false);

        return ticketRepository.findById(ticketId)
                .thenCompose(tOpt ->
                        tOpt.map(ticket ->
                                personRepository.findById(personId)
                                        .thenCompose(pOpt -> {
                                            if (pOpt.isEmpty()) return completedFuture(false);
                                            try {
                                                ticket.setPrice(amount);
                                                ticket.setPerson(pOpt.get());
                                                ticketRepository.save(ticket);
                                                return completedFuture(true);
                                            } catch (Exception e) {
                                                return completedFuture(false);
                                            }
                                        })
                        ).orElseGet(() -> completedFuture(false))
                )
                .exceptionally(exc -> false);
    }

    @Async
    public CompletableFuture<Ticket> cloneVip(Integer ticketId) {
        return ticketRepository.findById(ticketId)
                .thenCompose(srcOpt -> {
                    if (srcOpt.isEmpty()) return completedFuture(null);

                    var src = srcOpt.get();
                    var copy = new Ticket();
                    copy.setId(null);
                    copy.setName(src.getName());
                    copy.setCreationDate(LocalDateTime.now());
                    copy.setPerson(src.getPerson());
                    copy.setEvent(src.getEvent());
                    copy.setVenue(src.getVenue());
                    copy.setComment(src.getComment());
                    copy.setNumber(src.getNumber());
                    copy.setDiscount(src.getDiscount());

                    if (src.getCoordinates() != null) {
                        var c0 = src.getCoordinates();
                        var c1 = new Coordinates();
                        c1.setX(c0.getX());
                        c1.setY(c0.getY());
                        copy.setCoordinates(c1);
                    }

                    copy.setType(TicketType.VIP);
                    copy.setPrice(src.getPrice() * 2.0f);

                    try {
                        var saved = ticketRepository.save(copy);
                        return completedFuture(saved);
                    } catch (Exception e) {
                        return completedFuture(null);
                    }
                })
                .exceptionally(ex -> null);
    }
}