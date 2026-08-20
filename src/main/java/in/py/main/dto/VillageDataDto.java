package in.py.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VillageDataDto {
    private String villageId;    // Col A
    private String date;         // Col B
    private String time;         // Col C
    private String state;        // Col D
    private String district;     // Col E
    private String villageName;  // Col F
    private double temperature;  // Col G
    private double ph;           // Col H
    private double tds;          // Col I
    private double turbidity;    // Col J
    private double wqi;          // Calculated locally
}
