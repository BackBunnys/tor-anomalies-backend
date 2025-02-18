package mil.barsolcom.tor_anomalies_backend.service.anomaly.ad4j;

import org.algorithmtools.ad4j.engine.AnomalyDetectionEngine;
import org.algorithmtools.ad4j.enumtype.AnomalyDictType;
import org.springframework.stereotype.Service;

@Service
public class ZScoreAnomalyDetector extends Ad4jAnomalyDetector {
    public ZScoreAnomalyDetector(AnomalyDetectionEngine engine) {
        super(engine, "Z-Score", AnomalyDictType.MODEL_ADM_ZScore);
    }
}
