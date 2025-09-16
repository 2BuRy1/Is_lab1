package systems.project.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import systems.project.models.Person;
import systems.project.models.Ticket;
import systems.project.services.PersonService;
import systems.project.services.TicketService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin(origins = {"*"})
public class PersonsController {

    private final PersonService personService;


    public PersonsController(PersonService personService) {
        this.personService = personService;
    }


    @GetMapping("/get_persons")
    public CompletableFuture<ResponseEntity<Map<String, List<Person>>>> getPersons(){
        return personService.getPersons().thenApply(ResponseEntity::ok);

    }

    @PostMapping("/add_person")
    public CompletableFuture<ResponseEntity<Map<String, Boolean>>> addPerson(@RequestBody Person person){
        return personService.addPerson(person)
                .thenApply(res -> {
                    if(res.get("status")) return ResponseEntity.ok(res);

                    return ResponseEntity.badRequest().body(Map.of("status", false));
                });

    }
}
