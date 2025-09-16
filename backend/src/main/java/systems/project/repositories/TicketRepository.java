package systems.project.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import systems.project.models.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findById(Integer id);
    Boolean existsById(Integer id);
    void deleteById(Long id);
    @Transactional
    long deleteByComment(String comment);
    Optional<Ticket> findFirstByEventIsNotNullOrderByEvent_IdAsc();
    long countByCommentLessThan(String comment);
}
