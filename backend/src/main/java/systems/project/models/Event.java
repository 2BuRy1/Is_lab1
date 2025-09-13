package systems.project.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Check;

import java.util.List;

@Entity
@Data
public class Event {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически

    @Column(nullable = false)
    @Check(constraints = "char_length(name) > 0")
    private String name; //Поле не может быть null, Строка не может быть пустой

    @Column(nullable = false)
    @Check(constraints = "tickets_count > 0")
    private Integer ticketsCount; //Поле не может быть null, Значение поля должно быть больше 0

    @Enumerated(EnumType.STRING)
    private EventType eventType; //Поле может быть null

    @OneToMany
    @JsonIgnore
    private List<Ticket> tickets;
}
