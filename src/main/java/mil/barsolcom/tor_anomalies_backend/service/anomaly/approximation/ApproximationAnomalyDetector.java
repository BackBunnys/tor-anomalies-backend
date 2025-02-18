package mil.barsolcom.tor_anomalies_backend.service.anomaly.approximation;

import mil.barsolcom.tor_anomalies_backend.common.Interval;
import mil.barsolcom.tor_anomalies_backend.model.Anomaly;
import mil.barsolcom.tor_anomalies_backend.model.UserMetrics;
import mil.barsolcom.tor_anomalies_backend.service.anomaly.AnomalyDetector;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ApproximationAnomalyDetector extends AnomalyDetector {
    public ApproximationAnomalyDetector() {
        super("3STD_DEV");
    }

    public List<Anomaly> detect(List<UserMetrics> metrics) {
        var x = metrics.stream().mapToDouble(metric -> metric.getDate().toEpochDay()).toArray();
        var y = metrics.stream().mapToDouble(metric -> metric.getUsers().doubleValue()).toArray();
        double alpha = 0.3; // Smoothing factor

        // Calculate EMA
        double[] ema = exponentialMovingAverage(y, alpha);

        // Detect anomalies
        return detectAnomalies(y, x, ema);
    }

    public static double[] exponentialMovingAverage(double[] data, double alpha) {
        double[] ema = new double[data.length];
        ema[0] = data[0]; // First value remains the same

        for (int i = 1; i < data.length; i++) {
            ema[i] = alpha * data[i] + (1 - alpha) * ema[i - 1];
        }
        return ema;
    }

    @Override
    public List<Anomaly> detect(List<UserMetrics> metrics, Map<String, Object> parameters) {
        var x = metrics.stream().mapToDouble(metric -> metric.getDate().toEpochDay()).toArray();
        var y = metrics.stream().mapToDouble(metric -> metric.getUsers().doubleValue()).toArray();
        double alpha = 2.0 / (5 + 1);

        // Calculate EMA
        double[] ema = exponentialMovingAverage(y, alpha);

        // Detect anomalies
        return detectAnomalies(y, x, ema);
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

    public PolynomialFunction fitPolynomial(double[] x, double[] y, int degree) {
        WeightedObservedPoints obs = new WeightedObservedPoints();

        for (int i = 0; i < x.length; i++) {
            obs.add(x[i], y[i]);
        }

        // Fit the polynomial curve
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
        double[] coefficients = fitter.fit(obs.toList());

        return new PolynomialFunction(coefficients);
    }

    // Anomaly Detection based on 3-sigma rule
    public List<Anomaly> detectAnomalies(double[] data, double[] timestamp, double[] ema) {
        double[] residuals = new double[data.length];
        double mean = 0;
        List<Anomaly> anomalies = new ArrayList<>();

        // Compute residuals (absolute difference)
        for (int i = 0; i < data.length; i++) {
            residuals[i] = Math.abs(data[i] - ema[i]);
            mean += residuals[i];
        }
        mean /= data.length;

        // Compute standard deviation
        double finalMean = mean;
        double stdDev = Math.sqrt(Arrays.stream(residuals)
                .map(r -> Math.pow(r - finalMean, 2))
                .sum() / data.length);

        // Threshold for anomaly detection (mean + 3*stdDev)
        double threshold = mean + 3 * stdDev;

        // Identify anomalies
        for (int i = 0; i < data.length; i++) {
            if (residuals[i] > threshold) {
                System.out.println("🚨 Anomaly detected at index " + i + ": " + data[i] +
                        " (Deviation = " + residuals[i] + ")");
                anomalies.add(new Anomaly(new Interval<>(LocalDate.ofEpochDay((long) timestamp[i] - 1), LocalDate.ofEpochDay((long) timestamp[i] + 1))));
            }
        }
        return anomalies;
    }
}
