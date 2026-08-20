package in.py.main.service;

import java.util.List;

import in.py.main.dto.VillageDataDto;

public interface WaterQualityFilterService {
	
	public List<VillageDataDto> getFilteredVillages(
            String searchKeyword, // Searches ID, State, District, Village Name
            Double minTds, Double maxTds,
            Double minPh, Double maxPh,
            Double minTurbidity, Double maxTurbidity,
            Double minWqi, Double maxWqi,
            int page, int size);		

}
