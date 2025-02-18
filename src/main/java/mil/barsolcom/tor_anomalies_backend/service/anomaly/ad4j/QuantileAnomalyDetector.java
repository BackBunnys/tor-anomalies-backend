package mil.barsolcom.tor_anomalies_backend.service.anomaly.ad4j;

import org.algorithmtools.ad4j.engine.AnomalyDetectionEngine;
import org.algorithmtools.ad4j.enumtype.AnomalyDictType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuantileAnomalyDetector extends Ad4jAnomalyDetector {

    @Autowired
    public QuantileAnomalyDetector(AnomalyDetectionEngine engine) {
        super(engine, "Quantile", AnomalyDictType.MODEL_ADM_Quantile);
    }
}
