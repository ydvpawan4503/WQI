package in.py.main.service;

import in.py.main.dto.VillageDataDto;
import in.py.main.dto.ChartComparisonDto;
import org.springframework.stereotype.Service;

@Service
public class WaterQualityAnalyticsServiceImpl implements WaterQualityAnalyticsService {

    private final WaterQualityService waterQualityService;

    // Standard Acceptable Limits (BIS IS 10500)
    private static final double STANDARD_PH = 8.5; // Upper limit
    private static final double STANDARD_TDS = 500.0;
    private static final double STANDARD_TURBIDITY = 1.0;

    public WaterQualityAnalyticsServiceImpl(WaterQualityService waterQualityService) {
        this.waterQualityService = waterQualityService;
    }

    @Override
    public ChartComparisonDto getChartComparison(String villageId) {
        VillageDataDto village = waterQualityService.getVillageStore().get(villageId);
        
        if (village == null) {
            return null; // Handle 404 in controller
        }

        ChartComparisonDto dto = new ChartComparisonDto();
        dto.setVillageId(village.getVillageId());
        dto.setVillageName(village.getVillageName());
        
        // Actual Recorded Values
        dto.setActualPh(village.getPh());
        dto.setActualTds(village.getTds());
        dto.setActualTurbidity(village.getTurbidity());
        dto.setCurrentWqi(village.getWqi());

        // Standard Thresholds for Chart Baseline
        dto.setStandardPh(STANDARD_PH);
        dto.setStandardTds(STANDARD_TDS);
        dto.setStandardTurbidity(STANDARD_TURBIDITY);

        return dto;
    }
}