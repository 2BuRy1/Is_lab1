package systems.project.services;

import org.springframework.stereotype.Service;
import systems.project.models.Venue;
import systems.project.repositories.VenueRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }


    public CompletableFuture<Map<String, List<Venue>>> getVenues(){
        return CompletableFuture.supplyAsync(() -> venueRepository.findAll())
                .thenApply(venues -> Map.of("venues", venues))
                    .exceptionally(exc -> Map.of("status", null));
    }


    public CompletableFuture<Map<String, Boolean>> addVenue(Venue venue){
        return CompletableFuture.supplyAsync(() -> venueRepository.save(venue))
                .thenApply(ven -> Map.of("status", true))
                    .exceptionally(exc -> Map.of("status", false));
    }
}
