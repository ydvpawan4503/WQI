package in.py.main.service;

import in.py.main.dto.ChartComparisonDto;
import in.py.main.dto.VillageDataDto;

public interface WaterQualityAnalyticsService {
	public ChartComparisonDto getChartComparison(String villageId);
}
