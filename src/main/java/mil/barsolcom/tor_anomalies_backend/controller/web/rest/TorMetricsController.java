package mil.barsolcom.tor_anomalies_backend.controller.web.rest;

import mil.barsolcom.tor_anomalies_backend.common.Interval;
import mil.barsolcom.tor_anomalies_backend.controller.web.rest.model.UserMetricsListRequest;
import mil.barsolcom.tor_anomalies_backend.controller.web.rest.model.UserMetricsListResponse;
import mil.barsolcom.tor_anomalies_backend.service.anomaly.AnomalyDetector;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.UserMetricsService;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.model.UserMetricsSearch;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.model.UserMetricsType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;

import static mil.barsolcom.tor_anomalies_backend.service.user_metrics.Utils.groupByDate;

@RestController
@RequestMapping("v1/metrics")
public class TorMetricsController {
    private final UserMetricsService userMetricsService;
    private final AnomalyDetector anomalyDetector;

    public TorMetricsController(UserMetricsService userMetricsService, AnomalyDetector anomalyDetector) {
        this.userMetricsService = userMetricsService;
        this.anomalyDetector = anomalyDetector;
    }

    @GetMapping("/relays")
    Mono<UserMetricsListResponse> relays(@ModelAttribute UserMetricsListRequest request) {
        return userMetricsService
                .searchMetrics(new UserMetricsSearch(
                    request.getCountries(),
                    new Interval<>(request.getFrom(), request.getTo()),
                    Collections.singleton(UserMetricsType.RELAY)
                ))
                .collectList()
                .map(metrics -> new UserMetricsListResponse(metrics, anomalyDetector.detect(groupByDate(metrics), Duration.ofDays(request.getSensitivity().getWindowDays()))));
    }



    @GetMapping("/bridges")
    Mono<UserMetricsListResponse> bridges(@ModelAttribute UserMetricsListRequest request) {
        return userMetricsService
                .searchMetrics(new UserMetricsSearch(
                        request.getCountries(),
                        new Interval<>(request.getFrom(), request.getTo()),
                        Collections.singleton(UserMetricsType.BRIDGE)
                ))
                .collectList()
                .map(metrics -> new UserMetricsListResponse(metrics, anomalyDetector.detect(groupByDate(metrics), Duration.ofDays(request.getSensitivity().getWindowDays()))));
    }

    @GetMapping("/all")
    Mono<UserMetricsListResponse> all(@ModelAttribute UserMetricsListRequest request) {
        return userMetricsService
                .searchMetrics(new UserMetricsSearch(
                        request.getCountries(),
                        new Interval<>(request.getFrom(), request.getTo()),
                        UserMetricsType.ALL
                ))
                .collectList()
                .map(metrics -> new UserMetricsListResponse(metrics, anomalyDetector.detect(groupByDate(metrics), Duration.ofDays(request.getSensitivity().getWindowDays()))));
    }
}
