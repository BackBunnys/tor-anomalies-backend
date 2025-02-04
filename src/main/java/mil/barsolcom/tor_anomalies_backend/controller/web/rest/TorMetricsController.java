package mil.barsolcom.tor_anomalies_backend.controller.web.rest;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import mil.barsolcom.tor_anomalies_backend.controller.web.rest.model.Anomaly;
import mil.barsolcom.tor_anomalies_backend.controller.web.rest.model.Interval;
import mil.barsolcom.tor_anomalies_backend.controller.web.rest.model.UserMetricsListRequest;
import mil.barsolcom.tor_anomalies_backend.controller.web.rest.model.UserMetricsListResponse;
import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;
import org.algorithmtools.ad4j.engine.AnomalyDetectionEngine;
import org.algorithmtools.ad4j.enumtype.AnomalyDictType;
import org.algorithmtools.ad4j.pojo.*;
import org.algorithmtools.ad4j.utils.IndicatorSeriesUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("v1/metrics")
public class TorMetricsController {
    private static final String TOR_METRICS_URL = "https://metrics.torproject.org/";
    private static final String TOR_RELAY_METRICS_PATH = "userstats-relay-country.csv";
    private static final String TOR_BRIDGE_METRICS_PATH = "userstats-bridge-country.csv";
    private final AnomalyDetectionEngine engine;
    private final WebClient webClient;
    private final CsvMapper csvMapper;
    private final CsvSchema schema = CsvSchema.emptySchema()
            .withHeader()
            .withComments();

    public TorMetricsController(WebClient.Builder builder, CsvMapper csvMapper, AnomalyDetectionEngine engine) {
        this.webClient = builder.baseUrl(TOR_METRICS_URL).build();
        this.csvMapper = csvMapper;
        this.engine = engine;
    }

    @GetMapping("/relays")
    Mono<UserMetricsListResponse> relays(@ModelAttribute UserMetricsListRequest request) {
        return Flux.fromIterable(request.getCountries())
                .flatMap(country -> fetch(builder -> buildUri(builder, TOR_RELAY_METRICS_PATH, request, country)))
                .collectList()
                .map(metrics -> new UserMetricsListResponse(metrics, searchAnomalies(groupByDate(metrics))));
    }

    @GetMapping("/bridges")
    Mono<UserMetricsListResponse> bridges(@ModelAttribute UserMetricsListRequest request) {
        return Flux.fromIterable(request.getCountries())
                .flatMap(country -> fetch(builder -> buildUri(builder, TOR_BRIDGE_METRICS_PATH, request, country)))
                .collectList()
                .map(metrics -> new UserMetricsListResponse(metrics, searchAnomalies(groupByDate(metrics))));
    }

    @GetMapping("/all")
    Mono<UserMetricsListResponse> all(@ModelAttribute UserMetricsListRequest request) {
        return Flux.fromIterable(request.getCountries())
                .flatMap(country -> Flux.merge(
                        fetch(builder -> buildUri(builder, TOR_RELAY_METRICS_PATH, request, country)),
                        fetch(builder -> buildUri(builder, TOR_BRIDGE_METRICS_PATH, request, country))
                    ).collectList()
                    .map(this::groupByDate)
                    .flatMapMany(Flux::fromIterable)
                )
                .collectList()
                .map(metrics -> new UserMetricsListResponse(metrics, searchAnomalies(groupByDate(metrics))));
    }

    private List<UserMetrics> groupByDate(List<UserMetrics> metrics) {
        return metrics.stream()
                .collect(Collectors.groupingBy(UserMetrics::getDate, TreeMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry ->
                        new UserMetrics(
                                entry.getKey(),
                                BigDecimal.valueOf(entry.getValue().stream().mapToLong(metric -> metric.getUsers().longValue()).sum()),
                                entry.getValue().getFirst().getCountry()
                        )
                ).toList();
    }

    private URI buildUri(UriBuilder builder, String path, UserMetricsListRequest request, String country) {
        return builder
                .path(path)
                .queryParam("start", request.getDateInterval().getStart())
                .queryParam("end", request.getDateInterval().getEnd())
                .queryParam("country", country)
                .build();
    }

    private Flux<UserMetrics> fetch(Function<UriBuilder, URI> uriFunction) {
        return webClient.get()
                .uri(uriFunction)
                .retrieve()
                .bodyToFlux(String.class)
                .collectList()
                .map(data -> String.join("\n", data))
                .flatMapMany(this::parseCsv);
    }

    private List<Anomaly> searchAnomalies(List<UserMetrics> metrics) {
        List<Anomaly> anomalies = new ArrayList<>();

        List<IndicatorSeries> series = metrics.stream()
                .map(metric -> new IndicatorSeries(metric.getDate().toEpochDay(), metric.getUsers().intValue(), "index"))
                .toList();

        IndicatorInfo info = new IndicatorInfo("who", "who name", series);
        AnomalyDetectionResult result = engine.detect(info);
        if (result.getIndicatorEvaluateInfo(AnomalyDictType.TYPE_OUTLIERS_VALUE) == null) {
            return anomalies;
        }
        result.getIndicatorEvaluateInfo(AnomalyDictType.TYPE_OUTLIERS_VALUE).stream()
                .filter(evaluateInfo -> evaluateInfo.getAnomalyDetectionModel() == AnomalyDictType.MODEL_ADM_Quantile)
                .findFirst()
                .map(IndicatorEvaluateInfo::getAnomalySeriesList)
                .ifPresent(anomalySeries -> {
                    List<Interval<LocalDate>> anomaliesIntervals = new ArrayList<>();

                    long firstDate = anomalySeries.getFirst().getIndicatorSeries().getTime();
                    long lastDate = anomalySeries.getFirst().getIndicatorSeries().getTime();

                    for (int i = 1; i < anomalySeries.size(); ++i) {
                        var currentValue = anomalySeries.get(i).getIndicatorSeries().getTime();
                        var previousValue = anomalySeries.get(i - 1).getIndicatorSeries().getTime();
                        if (currentValue - previousValue > 7) {
                            anomaliesIntervals.add(new Interval<>(LocalDate.ofEpochDay(firstDate - 1), LocalDate.ofEpochDay(lastDate + 1)));
                            firstDate = currentValue;
                        }
                        lastDate = currentValue;
                    }

                    anomaliesIntervals.add(new Interval<>(LocalDate.ofEpochDay(firstDate - 1), LocalDate.ofEpochDay(lastDate + 1)));

                    anomaliesIntervals.forEach(interval -> System.out.println(interval.getStart() + " " + interval.getEnd()));

                    anomaliesIntervals.forEach(interval -> anomalies.add(new Anomaly(interval)));
                });
        IndicatorSeriesUtil.print(result);

        return anomalies;
    }

    private Flux<UserMetrics> parseCsv(String csvData) {
        try {
            MappingIterator<UserMetrics> it = csvMapper.readerFor(UserMetrics.class)
                    .with(schema)
                    .readValues(csvData);
            return Flux.fromIterable(it.readAll());
        } catch (IOException e) {
            return Flux.error(new RuntimeException("Error parsing CSV", e));
        }
    }
}
