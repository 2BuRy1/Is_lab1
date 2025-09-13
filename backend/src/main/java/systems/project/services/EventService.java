package systems.project.services;


import org.springframework.stereotype.Service;
import systems.project.models.Event;
import systems.project.repositories.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }


    public CompletableFuture<Map<String, List<Event>>> getEvents(){
        return CompletableFuture.supplyAsync(() -> eventRepository.findAll())
                .thenApply(events -> Map.of("events", events))
                    .exceptionally(exc -> Map.of("events", null));
    }

    public CompletableFuture<Map<String, String>> addEvent(Event event) {
        return CompletableFuture.supplyAsync(() -> eventRepository.save(event))
                .thenApply(evnt -> Map.of("status", "added"))
                    .exceptionally(exc -> Map.of("status", "failed" + exc.getMessage()));
    }


}
