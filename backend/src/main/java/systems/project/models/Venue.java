package systems.project.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Check;

import java.util.List;

@Entity
@Data
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически

    @Column(nullable = false)
    @Check(constraints = "char_length(name) > 0")
    private String name; //Поле не может быть null, Строка не может быть пустой

    @Check(constraints = "capacity > 0")
    private int capacity; //Значение поля должно быть больше 0

    @Enumerated(EnumType.STRING)
    private VenueType type; //Поле может быть null

    @OneToMany(mappedBy = "venue")
    @JsonIgnore
    private List<Ticket> tickets;
}
