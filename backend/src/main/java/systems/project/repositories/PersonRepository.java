package systems.project.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import systems.project.models.Person;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    @Async
    CompletableFuture<List<Person>> findAllBy();

    @Async
    CompletableFuture<Optional<Person>> findById(Integer id);

}
