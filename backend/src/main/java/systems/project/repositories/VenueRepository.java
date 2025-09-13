package systems.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import systems.project.models.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {

}
