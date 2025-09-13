package systems.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import systems.project.models.Ticket;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {


}
