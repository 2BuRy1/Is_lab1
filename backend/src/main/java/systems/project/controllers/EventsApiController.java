package systems.project.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import systems.project.controllers.api.EventsApi;
import systems.project.models.Event;
import systems.project.models.api.AbstractResponse;
import systems.project.models.envelopes.EventsEnvelope;
import systems.project.services.EventService;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"*"})
public class EventsApiController implements EventsApi {

    private final EventService service;

    public EventsApiController(EventService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<AbstractResponse> addEvent(Event event) {
        try {
            Map<String, Boolean> res = service.addEvent(event).join();
            boolean ok = Boolean.TRUE.equals(res.get("status"));
            if (ok) {
                return ResponseEntity.ok(
                        AbstractResponse.builder()
                                .status("ok")
                                .title("Успех")
                                .message("Событие создано")
                                .build()
                );
            }
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Ошибка")
                            .message("Ошибка при создании события")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<AbstractResponse<EventsEnvelope>> getEvents() {
        try {
            Map<String, List<Event>> map = service.getEvents().join();
            List<Event> events = map.get("events");
            EventsEnvelope envelope = new EventsEnvelope();
            envelope.setEventList(events);

            return ResponseEntity.ok(
                    AbstractResponse.<EventsEnvelope>builder()
                            .status("ok")
                            .title("Успех")
                            .message("Список событий")
                            .data(envelope)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.<EventsEnvelope>builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }
}