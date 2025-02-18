package mil.barsolcom.tor_anomalies_backend.service.anomaly.ad4j;

import org.algorithmtools.ad4j.engine.AnomalyDetectionEngine;
import org.algorithmtools.ad4j.enumtype.AnomalyDictType;
import org.springframework.stereotype.Service;

@Service
public class ManKendallAnomalyDetector extends Ad4jAnomalyDetector {
    public ManKendallAnomalyDetector(AnomalyDetectionEngine engine) {
        super(engine, "ManKendall", AnomalyDictType.MODEL_ADM_ManKendall);
    }
}
