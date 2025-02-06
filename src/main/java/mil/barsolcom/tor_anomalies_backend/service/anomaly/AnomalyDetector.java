package mil.barsolcom.tor_anomalies_backend.service.anomaly;

import mil.barsolcom.tor_anomalies_backend.model.Anomaly;
import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;

import java.time.Duration;
import java.util.List;

public interface AnomalyDetector {
    List<Anomaly> detect(List<UserMetrics> metrics);

    List<Anomaly> detect(List<UserMetrics> metrics, Duration duration);
}
