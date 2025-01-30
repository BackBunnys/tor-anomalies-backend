package mil.barsolcom.tor_anomalies_backend.controller.web.rest.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserMetricsListRequest {
    public enum Type {
        RELAY,
        BRIDGE,
        ALL
    }

    private Type type;
    private Interval<LocalDate> dateInterval;
    private Set<String> countries;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Interval<LocalDate> getDateInterval() {
        return dateInterval;
    }

    public void setDateInterval(Interval<LocalDate> dateInterval) {
        this.dateInterval = dateInterval;
    }

    public Set<String> getCountries() {
        return countries;
    }

    public void setCountries(Set<String> countries) {
        this.countries = countries;
    }
}
