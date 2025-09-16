package systems.project.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Coordinates {


    @SequenceGenerator(
            name = "coordinates_seq_gen",
            sequenceName = "coordinates_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "coordinates_seq_gen")
    @Id
    private Long id;
    private int x;
    @Column(nullable = false)
    private Float y; //Поле не может быть null
}
