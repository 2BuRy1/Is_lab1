package systems.project.services;

import org.springframework.stereotype.Service;
import systems.project.models.Location;
import systems.project.models.Person;
import systems.project.models.Ticket;
import systems.project.repositories.LocationRepository;
import systems.project.repositories.PersonRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class PersonService {


    private final PersonRepository personRepository;

    private final LocationRepository locationRepository;

    public PersonService(PersonRepository personRepository, LocationRepository locationRepository) {
        this.personRepository = personRepository;
        this.locationRepository = locationRepository;
    }

    public CompletableFuture<Map<String, List<Person>>> getPersons() {
        return CompletableFuture.supplyAsync(personRepository::findAll)
                .thenApply(persons -> Map.of("persons", persons))
                .exceptionally(ex -> Map.of("persons", null));
    }


    public CompletableFuture<Map<String, Boolean>> addPerson(Person person) {
        return CompletableFuture.supplyAsync(() -> {
            var savedLoc = locationRepository.save(person.getLocation());
            person.setLocation(savedLoc);
            personRepository.save(person);
            return Map.of("status", true);
        }).exceptionally(ex -> {
            System.out.println(ex.getMessage());
            return Map.of("status", false);
        });
    }
}
