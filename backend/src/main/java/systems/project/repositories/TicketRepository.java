package systems.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import systems.project.models.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {



    @Async
    CompletableFuture<List<Ticket>> findAllBy();

    @Async
    CompletableFuture<Optional<Ticket>> findById(Integer id);

    @Async
    CompletableFuture<Boolean> existsById(Integer id);

    @Async
    CompletableFuture<Void> deleteById(Integer id);

    @Async
    CompletableFuture<Long> deleteByComment(String comment);

    @Async
    CompletableFuture<Optional<Ticket>>  findFirstByEventIsNotNullOrderByEventIdAsc();

    @Async
    CompletableFuture<Long>countByCommentLessThan(String comment);
}
