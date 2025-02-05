package mil.barsolcom.tor_anomalies_backend.service.user_metrics.model;

import mil.barsolcom.tor_anomalies_backend.common.Interval;

import java.time.LocalDate;
import java.util.Set;

public class UserMetricsSearch {
    private Set<String> countryCodeList;
    private Interval<LocalDate> dateInterval;
    private Set<UserMetricsType> types;

    public UserMetricsSearch(Set<String> countryCodeList, Interval<LocalDate> dateInterval, Set<UserMetricsType> types) {
        this.countryCodeList = countryCodeList;
        this.dateInterval = dateInterval;
        this.types = types;
    }

    public UserMetricsSearch() {
    }

    public Set<String> getCountryCodeList() {
        return countryCodeList;
    }

    public void setCountryCodeList(Set<String> countryCodeList) {
        this.countryCodeList = countryCodeList;
    }

    public Interval<LocalDate> getDateInterval() {
        return dateInterval;
    }

    public void setDateInterval(Interval<LocalDate> dateInterval) {
        this.dateInterval = dateInterval;
    }

    public Set<UserMetricsType> getTypes() {
        return types;
    }

    public void setTypes(Set<UserMetricsType> types) {
        this.types = types;
    }
}
