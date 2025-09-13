package systems.project.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Coordinates {


    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private Long id;
    private int x;
    @Column(nullable = false)
    private Float y; //Поле не может быть null
}
