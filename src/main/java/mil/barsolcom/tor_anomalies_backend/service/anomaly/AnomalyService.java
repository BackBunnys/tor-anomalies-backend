package mil.barsolcom.tor_anomalies_backend.service.anomaly;

import mil.barsolcom.tor_anomalies_backend.model.Anomaly;
import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;
import mil.barsolcom.tor_anomalies_backend.service.anomaly.model.AnalyzeInput;

import java.util.List;

public interface AnomalyService {
    List<String> getAlgorithms();

    List<Anomaly> analyze(AnalyzeInput input);
}
