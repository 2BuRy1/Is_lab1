package systems.project.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import systems.project.models.Coordinates;
import systems.project.models.Ticket;
import systems.project.models.TicketType;
import systems.project.repositories.PersonRepository;
import systems.project.repositories.TicketRepository;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    private final PersonRepository personRepository;

    public TicketService(TicketRepository ticketRepository, PersonRepository personRepository) {
        this.ticketRepository = ticketRepository;
        this.personRepository = personRepository;
    }


    public CompletableFuture<Map<String, List<Ticket>>> getTickets() {
        return CompletableFuture.supplyAsync(ticketRepository::findAll)
                .thenApply(ticket -> Map.of("tickets", ticket))
                .exceptionally(ex -> Map.of("tickets", null));
    }


    public CompletableFuture<Map<String, Boolean>> addTicket(Ticket ticket) {
        return CompletableFuture.supplyAsync(() -> ticketRepository.save(ticket))
                .thenApply(ticket1 -> Map.of("status", true))
                .exceptionally(ex -> Map.of("status", false));

    }

    public CompletableFuture<Ticket> getTicket(Integer id) {
        return CompletableFuture.supplyAsync(() -> {
            var optional = ticketRepository.findById(id);
            return optional.orElse(null);
        }).exceptionally(exc -> null);

    }

    public CompletableFuture<Boolean> updateTicket(Integer id, Ticket ticket) {
        return CompletableFuture.supplyAsync(() -> {
            if (ticketRepository.existsById(id)) {
                ticket.setId(id);
                ticketRepository.save(ticket);
                return true;
            }
            return false;
        })
                .exceptionally(exc -> false);
    }

    public CompletableFuture<Boolean> removeTicket(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            if (ticketRepository.existsById(id)) {
                ticketRepository.deleteById(id);
                return true;
            }
            return false;
        })
                .exceptionally(exc -> false);

    }


    public CompletableFuture<Boolean> deleteAllByComment(String comment) {
        return CompletableFuture.supplyAsync(() -> {
            String c = comment == null ? "" : comment.trim();
            if (c.isEmpty()) return false;
            long removed = ticketRepository.deleteByComment(c);
            return removed > 0;
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return false;
        });
    }

    public CompletableFuture<Ticket> getWithMinEvent() {
        return CompletableFuture.supplyAsync(() ->
                ticketRepository.findFirstByEventIsNotNullOrderByEvent_IdAsc().orElse(null)
        ).exceptionally(ex -> null);
    }


    public CompletableFuture<Map<String, Long>> countByCommentLess(String comment) {
        return CompletableFuture.supplyAsync(() ->
                Map.of("count", ticketRepository.countByCommentLessThan(comment))
        ).exceptionally(ex -> Map.of("count", 0L));
    }

    public CompletableFuture<Boolean> sellTicket(Integer ticketId, Integer personId, float amount) {
        return CompletableFuture.supplyAsync(() -> {
            var ticket = ticketRepository.findById(ticketId);
            var person = personRepository.findById(Long.valueOf(personId));
            if (ticket.isEmpty() || person.isEmpty() || amount <= 0f) return false;

            var t = ticket.get();
            t.setPrice(amount);
            t.setPerson(person.get());
            ticketRepository.save(t);
            return true;
        }).exceptionally(ex -> false);
    }

    public CompletableFuture<Ticket> cloneVip(Integer ticketId) {
        return CompletableFuture.supplyAsync(() -> {
            var src = ticketRepository.findById(ticketId).orElse(null);
            if (src == null) return null;

            var copy = new Ticket();
            copy.setId(null); // новый
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

            return ticketRepository.save(copy);
        }).exceptionally(ex -> null);
    }
}
