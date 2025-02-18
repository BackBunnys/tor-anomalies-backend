package mil.barsolcom.tor_anomalies_backend.controller.web.rest;

import mil.barsolcom.tor_anomalies_backend.common.Interval;
import mil.barsolcom.tor_anomalies_backend.controller.web.rest.model.UserMetricsListRequest;
import mil.barsolcom.tor_anomalies_backend.controller.web.rest.model.UserMetricsListResponse;
import mil.barsolcom.tor_anomalies_backend.service.anomaly.AnomalyService;
import mil.barsolcom.tor_anomalies_backend.service.anomaly.model.AnalyzeInput;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.UserMetricsService;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.model.UserMetricsSearch;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.model.UserMetricsType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static mil.barsolcom.tor_anomalies_backend.service.user_metrics.Utils.groupByDate;

@RestController
@RequestMapping("v1/metrics")
public class TorMetricsController {
    private final UserMetricsService userMetricsService;
    private final AnomalyService anomalyService;

    public TorMetricsController(UserMetricsService userMetricsService, AnomalyService anomalyService) {
        this.userMetricsService = userMetricsService;
        this.anomalyService = anomalyService;
    }

    @GetMapping("/algorithms")
    Mono<List<String>> algorithms() {
        return Mono.just(anomalyService.getAlgorithms());
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
                .map(metrics -> new UserMetricsListResponse(metrics,
                        anomalyService.analyze(
                            new AnalyzeInput(
                                request.getAlgorithm(),
                                groupByDate(metrics),
                                Map.of("window", Duration.ofDays(request.getSensitivity().getWindowDays()))
                            )
                        )
                    )
                );
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
                .map(metrics -> new UserMetricsListResponse(metrics,
                                anomalyService.analyze(
                                        new AnalyzeInput(
                                                request.getAlgorithm(),
                                                groupByDate(metrics),
                                                Map.of("window", Duration.ofDays(request.getSensitivity().getWindowDays()))
                                        )
                                )
                        )
                );
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
                .map(metrics -> new UserMetricsListResponse(metrics,
                                anomalyService.analyze(
                                        new AnalyzeInput(
                                                request.getAlgorithm(),
                                                groupByDate(metrics),
                                                Map.of("window", Duration.ofDays(request.getSensitivity().getWindowDays()))
                                        )
                                )
                        )
                );
    }
}
