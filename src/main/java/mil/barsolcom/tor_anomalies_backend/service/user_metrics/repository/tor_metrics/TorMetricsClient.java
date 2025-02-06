package mil.barsolcom.tor_anomalies_backend.service.user_metrics.repository.tor_metrics;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.Utils;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.model.UserMetricsType;
import mil.barsolcom.tor_anomalies_backend.service.user_metrics.repository.UserMetricsRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.Set;
import java.util.function.Function;

@Repository
public class TorMetricsClient implements UserMetricsRepository {
    private static final String TOR_METRICS_URL = "https://metrics.torproject.org/";
    private static final String TOR_RELAY_METRICS_PATH = "userstats-relay-country.csv";
    private static final String TOR_BRIDGE_METRICS_PATH = "userstats-bridge-country.csv";

    private final WebClient webClient;
    private final CsvMapper csvMapper;
    private final CsvSchema schema = CsvSchema.emptySchema()
            .withHeader()
            .withComments();

    public TorMetricsClient(WebClient.Builder builder, CsvMapper csvMapper) {
        this.webClient = builder.baseUrl(TOR_METRICS_URL).build();
        this.csvMapper = csvMapper;
    }

    @Override
    public Flux<UserMetrics> findAll(Set<String> countries, Set<UserMetricsType> types, LocalDate from, LocalDate to) {
        return Flux.fromIterable(countries)
                    .flatMap(
                        country -> Flux.fromIterable(types)
                                .flatMap(type -> fetch(builder -> buildUri(builder, getPath(type), from, to, country)))
                                .collectList()
                                .map(Utils::groupByDate)
                    )
                    .flatMap(Flux::fromIterable);
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

    private String getPath(UserMetricsType type) {
        switch (type) {
            case RELAY -> {
                return TOR_RELAY_METRICS_PATH;
            }
            case BRIDGE -> {
                return TOR_BRIDGE_METRICS_PATH;
            }
            default -> {
                throw new IllegalArgumentException("UserMetricsType '%s' not supported!".formatted(type));
            }
        }
    }

    private URI buildUri(UriBuilder builder, String path, LocalDate from, LocalDate to, String country) {
        return builder
                .path(path)
                .queryParam("start", from)
                .queryParam("end", to)
                .queryParam("country", country)
                .build();
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
