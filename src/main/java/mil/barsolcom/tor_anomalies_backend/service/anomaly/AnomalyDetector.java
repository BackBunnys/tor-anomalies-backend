package mil.barsolcom.tor_anomalies_backend.service.anomaly;

import mil.barsolcom.tor_anomalies_backend.model.Anomaly;
import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;

import java.util.List;
import java.util.Map;

public abstract class AnomalyDetector {
    private final String algorithmName;

    public AnomalyDetector(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getAlgorithmName() {
        return this.algorithmName;
    }

    public List<Anomaly> detect(List<UserMetrics> metrics) {
        return detect(metrics, null);
    }

    public abstract List<Anomaly> detect(List<UserMetrics> metrics, Map<String, Object> parameters);
}
