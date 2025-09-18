package systems.project.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import systems.project.models.Venue;
import systems.project.services.VenueService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin(origins = {"*"})
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping("/get_venues")
    public CompletableFuture<ResponseEntity<Map<String, List<Venue>>>> getVenues() {
        return venueService.getVenues()
                .thenApply(ResponseEntity::ok);
    }


    @PostMapping("/add_venue")
    public CompletableFuture<ResponseEntity<Map<String, Boolean>>> addVenue(@RequestBody Venue venue) {
        return venueService.addVenue(venue)
                .thenApply(res -> {
                    if (res.get("status")) return ResponseEntity.ok(res);
                    return ResponseEntity.badRequest().body(Map.of("status", false));
                });
    }
}
