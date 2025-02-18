package mil.barsolcom.tor_anomalies_backend.controller.web.rest.model;

import lombok.Data;
import org.algorithmtools.ad4j.enumtype.AnomalyDictType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserMetricsListRequest {
    public enum Type {
        RELAY,
        BRIDGE,
        ALL
    }

    public enum Sensitivity {
        LOW(365),
        MEDIUM(91),
        HIGH(30);

        private final int windowDays;

        Sensitivity(int windowDays) {
            this.windowDays = windowDays;
        }

        public int getWindowDays() {
            return windowDays;
        }
    }

    private Type type;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDate from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDate to;
    private Set<String> countries;
    private Sensitivity sensitivity;
    private String algorithm;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public Set<String> getCountries() {
        return countries;
    }

    public void setCountries(Set<String> countries) {
        this.countries = countries;
    }

    public Sensitivity getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(Sensitivity sensitivity) {
        this.sensitivity = sensitivity;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
