package in.py.main.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.py.main.dto.ChartComparisonDto;
import in.py.main.dto.VillageDataDto;
import in.py.main.service.WaterQualityAnalyticsService;
import in.py.main.service.WaterQualityFilterService;
import in.py.main.service.WaterQualityService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/water")
@RequiredArgsConstructor
public class WaterQualityController {

    private final WaterQualityFilterService filterService;
    private final WaterQualityAnalyticsService analyticsService;
    
    // Injecting the main service to get the live snapshot
    private final WaterQualityService waterQualityService;

    // --- NEW ENDPOINTS FOR LIVE DASHBOARD ---

    // Snapshot endpoint for live.html when page opens
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> getLiveSnapshot() {
        return ResponseEntity.ok(waterQualityService.getLastBroadcastedPayload());
    }

    // Manual refresh endpoint (You can visit http://localhost:8080/api/water/refresh to force an update)
    @GetMapping("/refresh")
    public ResponseEntity<String> refreshData() {
        waterQualityService.fetchAndBroadcastData();
        return ResponseEntity.ok("Data refreshed successfully via Google Sheets.");
    }

    // --- EXISTING ENDPOINTS ---

    // API for the Paginated Table with Filters
    @GetMapping("/filter")
    public ResponseEntity<List<VillageDataDto>> getFilteredData(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Double minTds,
            @RequestParam(required = false) Double maxTds,
            @RequestParam(required = false) Double minPh,
            @RequestParam(required = false) Double maxPh,
            @RequestParam(required = false) Double minTurb,
            @RequestParam(required = false) Double maxTurb,
            @RequestParam(required = false) Double minWqi,
            @RequestParam(required = false) Double maxWqi,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<VillageDataDto> results = filterService.getFilteredVillages(
                searchKeyword, minTds, maxTds, minPh, maxPh, minTurb, maxTurb, minWqi, maxWqi, page, size);
        
        return ResponseEntity.ok(results);
    }

    // API for the "View Details" Graph Comparison
    @GetMapping("/analytics/{villageId}")
    public ResponseEntity<ChartComparisonDto> getVillageAnalytics(@PathVariable String villageId) {
        ChartComparisonDto comparison = analyticsService.getChartComparison(villageId);
        
        if (comparison == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(comparison);
    }
}