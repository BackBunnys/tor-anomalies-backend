package mil.barsolcom.tor_anomalies_backend.common;

import lombok.Data;

@Data
public class Interval<T> {
    private T start;
    private T end;

    public Interval(T start, T end) {
        this.start = start;
        this.end = end;
    }

    public Interval() {
    }

    public T getStart() {
        return start;
    }

    public void setStart(T start) {
        this.start = start;
    }

    public T getEnd() {
        return end;
    }

    public void setEnd(T end) {
        this.end = end;
    }
}
