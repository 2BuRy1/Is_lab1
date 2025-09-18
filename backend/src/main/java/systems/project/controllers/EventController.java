package systems.project.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import systems.project.models.Event;
import systems.project.services.EventService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin(origins = {"*"})
public class EventController {


    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @GetMapping("/get_events")
    public CompletableFuture<ResponseEntity<Map<String, List<Event>>>> getEvents() {
        return service.getEvents()
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/add_event")
    public CompletableFuture<ResponseEntity<Map<String, Boolean>>> addEvent(@RequestBody Event event) {

        return service.addEvent(event)
                .thenApply(res -> {
                    if (res.get("status")) return ResponseEntity.ok(res);
                    return ResponseEntity.badRequest().body(Map.of("status", false));
                });
    }


}
