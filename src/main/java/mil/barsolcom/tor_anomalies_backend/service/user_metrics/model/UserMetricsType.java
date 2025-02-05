package mil.barsolcom.tor_anomalies_backend.service.user_metrics.model;

import java.util.Set;

public enum UserMetricsType {
    RELAY,
    BRIDGE;

    public static final Set<UserMetricsType> ALL = Set.of(RELAY, BRIDGE);
}
