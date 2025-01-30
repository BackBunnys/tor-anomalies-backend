package mil.barsolcom.tor_anomalies_backend.controller.web.rest;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mil.barsolcom.tor_anomalies_backend.controller.web.rest.model.UserMetricsListRequest;
import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("v1/metrics")
public class TorMetricsController {
    private static final String TOR_METRICS_URL = "https://metrics.torproject.org/";
    private static final String TOR_RELAY_METRICS_PATH = "userstats-relay-country.csv";
    private static final String TOR_BRIDGE_METRICS_PATH = "userstats-bridge-country.csv";
    private final WebClient webClient;
    private final CsvMapper csvMapper;
    private final CsvSchema schema = CsvSchema.emptySchema()
            .withHeader()
            .withComments();

    public TorMetricsController(WebClient.Builder builder, CsvMapper csvMapper) {
        this.webClient = builder.baseUrl(TOR_METRICS_URL).build();
        this.csvMapper = csvMapper;
    }

    @GetMapping("/relays")
    Mono<String> relays(@ModelAttribute UserMetricsListRequest request) {
        System.out.println(request.getCountries());
        return webClient.get()
                .uri(uriBuilder -> {
                           URI uri = uriBuilder
                                    .path(TOR_RELAY_METRICS_PATH)
                                    .queryParam("start", request.getDateInterval().getStart())
                                    .queryParam("end", request.getDateInterval().getEnd())
                                    .queryParam("country", request.getCountries())
                                    .build();
                           System.out.println(uri);
                           return uri;
                        }
                )
                .retrieve()
                .bodyToFlux(String.class)
                .collectList()
                .map(data -> String.join("\n", data));
//                .collectList()
//                .map(data -> String.join("\n", data))
//                .flatMapMany(this::parseCsv);
    }

    @GetMapping("/bridges")
    Mono<String> bridges(@ModelAttribute UserMetricsListRequest request) {
        System.out.println(request.getCountries());
        return webClient.get()
                .uri(uriBuilder -> {
                            URI uri = uriBuilder
                                    .path(TOR_BRIDGE_METRICS_PATH)
                                    .queryParam("start", request.getDateInterval().getStart())
                                    .queryParam("end", request.getDateInterval().getEnd())
                                    .queryParam("country", request.getCountries())
                                    .build();
                            System.out.println(uri);
                            return uri;
                        }
                )
                .retrieve()
                .bodyToFlux(String.class)
                .collectList()
                .map(data -> String.join("\n", data));
//                .collectList()
//                .map(data -> String.join("\n", data))
//                .flatMapMany(this::parseCsv);
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
