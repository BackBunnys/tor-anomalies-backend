package mil.barsolcom.tor_anomalies_backend.service.user_metrics.repository;

import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.model.UserMetricsType;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.Set;

public interface UserMetricsRepository {
    Flux<UserMetrics> findAll(Set<String> countries, Set<UserMetricsType> type, LocalDate from, LocalDate to);
}
