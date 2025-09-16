package systems.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import systems.project.models.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

}
