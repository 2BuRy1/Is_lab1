package systems.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import systems.project.models.Venue;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    @Async
    CompletableFuture<List<Venue>> findAllBy();
}
