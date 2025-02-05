package mil.barsolcom.tor_anomalies_backend.service.user_metrics;

import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.model.UserMetricsSearch;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.repository.UserMetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class UserMetricsServiceImpl implements UserMetricsService {
    private final UserMetricsRepository repository;

    @Autowired
    public UserMetricsServiceImpl(UserMetricsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<UserMetrics> searchMetrics(UserMetricsSearch search) {
        return repository.findAll(
            search.getCountryCodeList(),
            search.getTypes(),
            search.getDateInterval().getStart(),
            search.getDateInterval().getEnd()
        );
    }
}
