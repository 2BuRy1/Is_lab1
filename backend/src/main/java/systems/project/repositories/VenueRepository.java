package systems.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import systems.project.models.Venue;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

}
