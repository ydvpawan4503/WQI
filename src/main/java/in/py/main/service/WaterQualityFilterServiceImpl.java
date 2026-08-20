package in.py.main.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import in.py.main.dto.VillageDataDto;

@Service
public class WaterQualityFilterServiceImpl implements WaterQualityFilterService {

    private final WaterQualityService waterQualityService;

    public WaterQualityFilterServiceImpl(WaterQualityService waterQualityService) {
        this.waterQualityService = waterQualityService;
    }

    @Override
    public List<VillageDataDto> getFilteredVillages(
            String searchKeyword, // Searches ID, State, District, Village Name
            Double minTds, Double maxTds,
            Double minPh, Double maxPh,
            Double minTurbidity, Double maxTurbidity,
            Double minWqi, Double maxWqi,
            int page, int size) {

        List<VillageDataDto> allVillages = new ArrayList<>(waterQualityService.getVillageStore().values());

        if (allVillages.isEmpty()) return Collections.emptyList();

        // Apply Filters
        List<VillageDataDto> filtered = allVillages.stream().filter(v -> {
            
            // 1. Text Search Filter (Case Insensitive)
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String keyword = searchKeyword.toLowerCase();
                boolean matchesText = v.getVillageId().toLowerCase().contains(keyword) ||
                                      v.getState().toLowerCase().contains(keyword) ||
                                      v.getDistrict().toLowerCase().contains(keyword) ||
                                      v.getVillageName().toLowerCase().contains(keyword);
                if (!matchesText) return false;
            }

            // 2. Numerical Range Filters
            if (minTds != null && v.getTds() < minTds) return false;
            if (maxTds != null && v.getTds() > maxTds) return false;
            
            if (minPh != null && v.getPh() < minPh) return false;
            if (maxPh != null && v.getPh() > maxPh) return false;
            
            if (minTurbidity != null && v.getTurbidity() < minTurbidity) return false;
            if (maxTurbidity != null && v.getTurbidity() > maxTurbidity) return false;
            
            if (minWqi != null && v.getWqi() < minWqi) return false;
            if (maxWqi != null && v.getWqi() > maxWqi) return false;

            return true;
        }).collect(Collectors.toList());

        // Apply Pagination
        int start = page * size;
        if (start >= filtered.size()) return Collections.emptyList();
        int end = Math.min(start + size, filtered.size());

        return filtered.subList(start, end);
    }
}
