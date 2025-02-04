package mil.barsolcom.tor_anomalies_backend.controller.web.rest.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Anomaly {
    private Interval<LocalDate> interval;

    public Anomaly(Interval<LocalDate> interval) {
        this.interval = interval;
    }

    public Interval<LocalDate> getInterval() {
        return interval;
    }

    public void setInterval(Interval<LocalDate> interval) {
        this.interval = interval;
    }
}
