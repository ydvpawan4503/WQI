package in.py.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartComparisonDto {
    
    // Meta Data
    private String villageId;
    private String villageName;
    
    // Actual Recorded Values from the Sensor
    private double actualPh;
    private double actualTds;
    private double actualTurbidity;
    private double currentWqi;
    
    // Standard Thresholds (BIS IS 10500) for the Chart Baseline
    private double standardPh;
    private double standardTds;
    private double standardTurbidity;
}
