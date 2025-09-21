package systems.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import systems.project.models.Event;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Async
    CompletableFuture<List<Event>> findAllBy();


}
