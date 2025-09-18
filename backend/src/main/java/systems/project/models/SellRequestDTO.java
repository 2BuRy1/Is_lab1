package systems.project.models;

import lombok.Data;

@Data
public class SellRequestDTO {
    public Integer ticketId;
    public Integer personId;
    public float amount;
}
