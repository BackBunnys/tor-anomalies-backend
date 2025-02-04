package mil.barsolcom.tor_anomalies_backend.controller.web.rest.model;

import lombok.Data;
import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;

import java.util.List;

@Data
public class UserMetricsListResponse {
    private List<UserMetrics> metrics;
    private List<Anomaly> anomalies;

    public UserMetricsListResponse(List<UserMetrics> metrics, List<Anomaly> anomalies) {
        this.metrics = metrics;
        this.anomalies = anomalies;
    }

    public List<UserMetrics> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<UserMetrics> metrics) {
        this.metrics = metrics;
    }

    public List<Anomaly> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<Anomaly> anomalies) {
        this.anomalies = anomalies;
    }
}
