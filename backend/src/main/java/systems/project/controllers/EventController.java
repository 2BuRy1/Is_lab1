package systems.project.controllers;


import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public CompletableFuture<ResponseEntity<Map<String, List<Event>>>> getEvents(){
        return service.getEvents()
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/add_event")
    public CompletableFuture<ResponseEntity<Map<String, String>>> add_event(@RequestBody Event event){

        return service.addEvent(event)
                .thenApply(ResponseEntity::ok);
    }


}
