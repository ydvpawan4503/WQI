package in.py.main.service;

import in.py.main.dto.VillageDataDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaterQualityService {

    @Value("${wqi.sheet.url}")
    private String SHEET_URL;

    private final SimpMessagingTemplate messagingTemplate;

    // ADDED: This stores the latest snapshot for when you first open the live page
    private Map<String, Object> lastBroadcastedPayload = new HashMap<>();

    // This map acts as your Database for the other services
    private Map<String, VillageDataDto> villageStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadDataOnStartup() {
        fetchAndBroadcastData();
    }

    @Scheduled(fixedRate = 20000)
    public void fetchAndBroadcastData() {
        Map<String, VillageDataDto> tempStore = new ConcurrentHashMap<>();

        try {
            URL url = new URL(SHEET_URL);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            boolean isFirstRow = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstRow) { isFirstRow = false; continue; }

                String[] columns = line.split(",");
                if (columns.length >= 10) {
                    try {
                        String villageId = columns[0].trim();
                        double temp = Double.parseDouble(columns[6].trim());
                        double ph = Double.parseDouble(columns[7].trim());
                        double tds = Double.parseDouble(columns[8].trim());
                        double turb = Double.parseDouble(columns[9].trim());

                        if (isValidSensorData(temp, ph, tds, turb)) {
                            VillageDataDto dto = new VillageDataDto(
                                    villageId, columns[1].trim(), columns[2].trim(),
                                    columns[3].trim(), columns[4].trim(), columns[5].trim(),
                                    temp, ph, tds, turb, 0.0
                            );
                            dto.setWqi(calculateWQI(dto));
                            tempStore.put(villageId, dto); // Store only the latest record
                        }
                    } catch (NumberFormatException ignored) {
                    	log.error("Rejected row {}: {}", columns[0], ignored.getMessage());
                    }
                }
            }
            reader.close();
            this.villageStore = tempStore;

            // --- LIVE PAGE LOGIC ONLY ---
            List<VillageDataDto> topBest = villageStore.values().stream()
                    .filter(v -> v.getWqi() <= 50.0)
                    .sorted(Comparator.comparingDouble(VillageDataDto::getWqi))
                    .limit(10).collect(Collectors.toList());

            List<VillageDataDto> topCritical = villageStore.values().stream()
                    .filter(v -> v.getWqi() > 100.0)
                    .sorted(Comparator.comparingDouble(VillageDataDto::getWqi).reversed())
                    .limit(10).collect(Collectors.toList());

            Map<String, Object> payload = new HashMap<>();
            payload.put("topBest", topBest);
            payload.put("topCritical", topCritical);
            payload.put("totalMonitored", villageStore.size());

            // ADDED: Save the payload right before broadcasting it
            this.lastBroadcastedPayload = payload;

            messagingTemplate.convertAndSend("/topic/waterquality", (Object) payload);
            
        } catch (Exception e) {
            log.error("Failed to fetch data: " + e.getMessage());
        }
    }

    // ADDED: The method the Controller needs to fetch the initial data
    public Map<String, Object> getLastBroadcastedPayload() {
        return lastBroadcastedPayload;
    }

    // Expose the store so Filter and Analytics services can read it
    public Map<String, VillageDataDto> getVillageStore() {
        return villageStore;
    }

    private boolean isValidSensorData(double temp, double ph, double tds, double turb) {
        return (temp >= 0.0 && temp <= 60.0) && (ph >= 0.0 && ph <= 14.0) &&
               (tds >= 0.0 && tds <= 5000.0) && (turb >= 0.0 && turb <= 1000.0);
    }

    private double calculateWQI(VillageDataDto v) {
        double phWeight = Math.abs(7.0 - v.getPh()) * 10;
        double tdsWeight = v.getTds() * 0.1;
        double turbWeight = v.getTurbidity() * 5;
        double tempFactor = (v.getTemperature() > 30.0) ? 1.05 : 1.0; 
        return (phWeight + tdsWeight + turbWeight) * tempFactor;
    }
}