package mil.barsolcom.tor_anomalies_backend.service.anomaly.model;

import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;

import java.util.List;
import java.util.Map;

public class AnalyzeInput {
    private String algorithmName;
    private List<UserMetrics> userMetricsList;
    private Map<String, Object> parameters;

    public AnalyzeInput(String algorithmName, List<UserMetrics> userMetricsList, Map<String, Object> parameters) {
        this.algorithmName = algorithmName;
        this.userMetricsList = userMetricsList;
        this.parameters = parameters;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public List<UserMetrics> getUserMetricsList() {
        return userMetricsList;
    }

    public void setUserMetricsList(List<UserMetrics> userMetricsList) {
        this.userMetricsList = userMetricsList;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
