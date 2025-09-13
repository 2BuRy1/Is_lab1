package systems.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import systems.project.models.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
