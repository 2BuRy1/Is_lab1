package systems.project.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import systems.project.models.Venue;
import systems.project.repositories.VenueRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }


    @Async
    public CompletableFuture<Map<String, List<Venue>>> getVenues() {
        return venueRepository.findAllBy()
                .thenApply(venues -> Map.of("venues", venues))
                    .exceptionally(exc -> Map.of("status", null));
    }


    public CompletableFuture<Map<String, Boolean>> addVenue(Venue venue) {
        try {
            venueRepository.save(venue);
            return completedFuture(Map.of("status", true));
        } catch (Exception e) {
            return completedFuture(Map.of("status", false));
        }
    }
}
