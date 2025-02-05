package mil.barsolcom.tor_anomalies_backend.service.anomaly.ad4j;

import mil.barsolcom.tor_anomalies_backend.common.Interval;
import mil.barsolcom.tor_anomalies_backend.model.Anomaly;
import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;
import mil.barsolcom.tor_anomalies_backend.service.anomaly.AnomalyDetector;
import org.algorithmtools.ad4j.engine.AnomalyDetectionEngine;
import org.algorithmtools.ad4j.enumtype.AnomalyDictType;
import org.algorithmtools.ad4j.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class Ad4jAnomalyDetector implements AnomalyDetector {
    private boolean positiveOnly = true;
    private int anomaliesGap = 1;
    private int anomaliesOffset = 7;
    private final AnomalyDetectionEngine engine;

    @Autowired
    public Ad4jAnomalyDetector(AnomalyDetectionEngine engine) {
        this.engine = engine;
    }

    @Override
    public List<Anomaly> detect(List<UserMetrics> metrics) {
        List<Anomaly> anomalies = new ArrayList<>();
        List<IndicatorSeries> series = mapToSeries(metrics);
        IndicatorInfo info = new IndicatorInfo("metrics", "tor", series);
        AnomalyDetectionResult result = engine.detect(info);
        if (result.getIndicatorEvaluateInfo(AnomalyDictType.TYPE_OUTLIERS_VALUE) == null) {
            return anomalies;
        }
        return result.getIndicatorEvaluateInfo(AnomalyDictType.TYPE_OUTLIERS_VALUE).stream()
                .filter(evaluateInfo -> evaluateInfo.getAnomalyDetectionModel() == AnomalyDictType.MODEL_ADM_ZScore)
                .findFirst()
                .map(IndicatorEvaluateInfo::getAnomalySeriesList)
                .map(this::mapToAnomalies)
                .orElse(Collections.emptyList());
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
