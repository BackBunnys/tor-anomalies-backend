package mil.barsolcom.tor_anomalies_backend.service.anomaly;

import mil.barsolcom.tor_anomalies_backend.model.Anomaly;
import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;
import mil.barsolcom.tor_anomalies_backend.service.anomaly.model.AnalyzeInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnomalyServiceImpl implements AnomalyService {
    private final List<AnomalyDetector> detectors;

    @Autowired
    public AnomalyServiceImpl(List<AnomalyDetector> detectors) {
        this.detectors = detectors;
    }

    @Override
    public List<String> getAlgorithms() {
        return detectors.stream()
                .map(AnomalyDetector::getAlgorithmName)
                .toList();
    }

    @Override
    public List<Anomaly> analyze(AnalyzeInput input) {
        return detectors.stream()
                .filter(detector -> detector.getAlgorithmName().equals(input.getAlgorithmName()))
                .findFirst()
                .map(detector -> detector.detect(input.getUserMetricsList(), input.getParameters()))
                .orElseThrow(() -> new IllegalArgumentException("Algorithm '%s' not supported!".formatted(input.getAlgorithmName())));
    }
}
