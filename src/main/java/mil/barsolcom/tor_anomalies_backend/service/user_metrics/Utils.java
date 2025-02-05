package mil.barsolcom.tor_anomalies_backend.service.user_metrics;

import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Utils {
    public static List<UserMetrics> groupByDate(List<UserMetrics> metrics) {
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
}
