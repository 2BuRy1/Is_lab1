package systems.project.services;


import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import systems.project.models.Event;
import systems.project.repositories.EventRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Async
    public CompletableFuture<Map<String, List<Event>>> getEvents() {
        return eventRepository.findAllBy().thenApply(res -> Map.of("events", res))
                .exceptionally(exc -> Map.of("events", null));
    }

    @Async
    public CompletableFuture<Map<String, Boolean>> addEvent(Event event) {
        try {
            eventRepository.save(event);
            return completedFuture(Map.of("status", true));
        } catch (Exception e) {
            return completedFuture(Map.of("status", false));
        }
    }


}
