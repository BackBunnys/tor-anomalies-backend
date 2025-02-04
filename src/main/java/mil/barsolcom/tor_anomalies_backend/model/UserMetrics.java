package mil.barsolcom.tor_anomalies_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserMetrics {
    private LocalDate date;
    private BigDecimal users;
    private String country;

    public UserMetrics() {
    }

    public UserMetrics(LocalDate date, BigDecimal users, String country) {
        this.date = date;
        this.users = users;
        this.country = country;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getUsers() {
        return users;
    }

    public void setUsers(BigDecimal users) {
        this.users = users;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
