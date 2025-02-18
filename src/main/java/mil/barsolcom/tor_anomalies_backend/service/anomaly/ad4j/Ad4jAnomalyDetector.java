package mil.barsolcom.tor_anomalies_backend.service.anomaly.ad4j;

import mil.barsolcom.tor_anomalies_backend.common.Interval;
import mil.barsolcom.tor_anomalies_backend.model.Anomaly;
import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;
import mil.barsolcom.tor_anomalies_backend.service.anomaly.AnomalyDetector;
import org.algorithmtools.ad4j.engine.AnomalyDetectionEngine;
import org.algorithmtools.ad4j.enumtype.AnomalyDictType;
import org.algorithmtools.ad4j.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

public abstract class Ad4jAnomalyDetector extends AnomalyDetector {
    private boolean positiveOnly = false;
    private int anomaliesGap = 1;
    private int anomaliesOffset = 1;
    private final AnomalyDetectionEngine engine;
    private final AnomalyDictType algorithm;

    @Autowired
    public Ad4jAnomalyDetector(AnomalyDetectionEngine engine, String algorithmName, AnomalyDictType algorithm) {
        super(algorithmName);
        this.engine = engine;
        this.algorithm = algorithm;
    }

    public List<Anomaly> detect(List<UserMetrics> metrics) {
        List<Anomaly> anomalies = new ArrayList<>();
        List<IndicatorSeries> series = mapToSeries(metrics);
        IndicatorInfo info = new IndicatorInfo("metrics", "tor", series);
        AnomalyDetectionResult result = engine.detect(info);
        if (result.getIndicatorEvaluateInfo(AnomalyDictType.TYPE_OUTLIERS_VALUE) == null) {
            return anomalies;
        }
        return result.getIndicatorEvaluateInfo(AnomalyDictType.TYPE_OUTLIERS_VALUE).stream()
                .filter(evaluateInfo -> evaluateInfo.getAnomalyDetectionModel() == algorithm)
                .findFirst()
                .map(IndicatorEvaluateInfo::getAnomalySeriesList)
                .map(this::mapToAnomalies)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Anomaly> detect(List<UserMetrics> metrics, Map<String, Object> parameters) {
        int windowSize;

        if (!(parameters.get("window") instanceof Duration duration)) {
            throw new IllegalArgumentException("window parameter must be instance of Duration");
        } else {
            windowSize = (int) duration.toDays();
        }

        if (windowSize > metrics.size()) {
            return detect(metrics);
        }

        return mergeIntervals(IntStream.rangeClosed(0, metrics.size() - windowSize - 1)
                .mapToObj(i -> metrics.subList(i, i + windowSize))
                .map(this::detect)
                .flatMap(Collection::stream)
                .toList()
        );
    }

    public List<Anomaly> mergeIntervals(List<Anomaly> anomalies) {
        if (anomalies.isEmpty()) {
            return List.of();
        }

        List<Interval<LocalDate>> intervals = new ArrayList<>(anomalies.stream().map(Anomaly::getInterval).toList());

        // Sort intervals by start date
        intervals.sort(Comparator.comparing(Interval::getStart));

        List<Interval<LocalDate>> merged = new ArrayList<>();
        Interval<LocalDate> current = intervals.getFirst();

        for (int i = 1; i < intervals.size(); i++) {
            Interval<LocalDate> next = intervals.get(i);

            if (!next.getStart().isAfter(current.getEnd())) {
                // Intervals overlap, merge them
                current.setEnd(current.getEnd().isAfter(next.getEnd()) ? current.getEnd() : next.getEnd());
            } else {
                // No overlap, add the current interval and move to the next
                merged.add(current);
                current = next;
            }
        }

        // Add the last interval
        merged.add(current);
        return merged.stream().map(Anomaly::new).toList();
    }

    private List<IndicatorSeries> mapToSeries(List<UserMetrics> metrics) {
        return metrics.stream()
                .map(metric -> new IndicatorSeries(metric.getDate().toEpochDay(), metric.getUsers().intValue(), "index"))
                .toList();
    }

    private List<Anomaly> mapToAnomalies(List<AnomalyIndicatorSeries> anomalySeries) {
        List<Anomaly> anomalies = new ArrayList<>();

        if (positiveOnly) {
            anomalySeries = anomalySeries.stream().filter(s -> s.getAnomalyInfluence() == AnomalyDictType.INFLUENCE_POSITIVE).toList();
        }

        if (anomalySeries.isEmpty()) {
            return anomalies;
        }

        List<Interval<LocalDate>> anomaliesIntervals = new ArrayList<>();

        long firstDate = anomalySeries.getFirst().getIndicatorSeries().getTime();
        long lastDate = anomalySeries.getFirst().getIndicatorSeries().getTime();

        for (int i = 1; i < anomalySeries.size(); ++i) {
            var currentValue = anomalySeries.get(i).getIndicatorSeries().getTime();
            var previousValue = anomalySeries.get(i - 1).getIndicatorSeries().getTime();
            if (currentValue - previousValue > anomaliesOffset) {
                anomaliesIntervals.add(new Interval<>(LocalDate.ofEpochDay(firstDate - 1), LocalDate.ofEpochDay(lastDate + 1)));
                firstDate = currentValue;
            }
            lastDate = currentValue;
        }

        anomaliesIntervals.add(new Interval<>(LocalDate.ofEpochDay(firstDate - 1), LocalDate.ofEpochDay(lastDate + 1)));
        anomaliesIntervals.forEach(interval -> anomalies.add(new Anomaly(interval)));

        return anomalies;
    }

    public boolean isPositiveOnly() {
        return positiveOnly;
    }

    public void setPositiveOnly(boolean positiveOnly) {
        this.positiveOnly = positiveOnly;
    }

    public int getAnomaliesGap() {
        return anomaliesGap;
    }

    public void setAnomaliesGap(int anomaliesGap) {
        this.anomaliesGap = anomaliesGap;
    }

    public int getAnomaliesOffset() {
        return anomaliesOffset;
    }

    public void setAnomaliesOffset(int anomaliesOffset) {
        this.anomaliesOffset = anomaliesOffset;
    }
}
