package systems.project.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import systems.project.models.Person;
import systems.project.repositories.LocationRepository;
import systems.project.repositories.PersonRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Service
public class PersonService {


    private final PersonRepository personRepository;

    private final LocationRepository locationRepository;

    public PersonService(PersonRepository personRepository, LocationRepository locationRepository) {
        this.personRepository = personRepository;
        this.locationRepository = locationRepository;
    }

    @Async
    public CompletableFuture<Map<String, List<Person>>> getPersons() {
        return personRepository.findAllBy().thenApply(res -> Map.of("persons", res))
                .exceptionally(exc -> Map.of("persons", null));
    }

    @Async
    public CompletableFuture<Map<String, Boolean>> addPerson(Person person) {
        try {
            var saved = locationRepository.save(person.getLocation());
            person.setLocation(saved);
            personRepository.save(person);
            return completedFuture(Map.of("status", true));
        } catch (Exception e) {
            return completedFuture(Map.of("status", false));
        }
    }

}
