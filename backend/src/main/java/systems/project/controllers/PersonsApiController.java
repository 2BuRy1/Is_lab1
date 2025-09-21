package systems.project.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import systems.project.controllers.api.PersonsApi;
import systems.project.models.Person;
import systems.project.models.api.AbstractResponse;
import systems.project.models.envelopes.PersonEnvelope;
import systems.project.services.PersonService;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"*"})
public class PersonsApiController implements PersonsApi {

    private final PersonService personService;

    public PersonsApiController(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public ResponseEntity<AbstractResponse> addPerson(Person person) {
        try {
            Map<String, Boolean> res = personService.addPerson(person).join();
            boolean ok = Boolean.TRUE.equals(res.get("status"));
            if (ok) {
                return ResponseEntity.ok(
                        AbstractResponse.builder()
                                .status("ok")
                                .title("Успех")
                                .message("Человек создан")
                                .build()
                );
            }
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Ошибка")
                            .message("Ошибка при создании человека")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<AbstractResponse<PersonEnvelope>> getPersons() {
        try {
            Map<String, List<Person>> map = personService.getPersons().join();
            List<Person> persons = map.get("persons");
            PersonEnvelope envelope = new PersonEnvelope();
            envelope.setPersonList(persons);

            return ResponseEntity.ok(
                    AbstractResponse.<PersonEnvelope>builder()
                            .status("ok")
                            .title("Успех")
                            .message("Список людей")
                            .data(envelope)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AbstractResponse.<PersonEnvelope>builder()
                            .status("error")
                            .title("Ошибка")
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }
}