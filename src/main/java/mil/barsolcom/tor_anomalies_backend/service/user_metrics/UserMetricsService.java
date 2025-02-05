package mil.barsolcom.tor_anomalies_backend.service.user_metrics;

import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.model.UserMetricsSearch;
import reactor.core.publisher.Flux;

public interface UserMetricsService {
    Flux<UserMetrics> searchMetrics(UserMetricsSearch search);
}
