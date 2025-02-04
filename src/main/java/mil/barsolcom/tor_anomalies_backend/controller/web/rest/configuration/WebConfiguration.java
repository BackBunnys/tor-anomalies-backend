package mil.barsolcom.tor_anomalies_backend.controller.web.rest.configuration;

import org.algorithmtools.ad4j.engine.AnomalyDetectionEngine;
import org.algorithmtools.ad4j.enumtype.AnomalyDictType;
import org.algorithmtools.ad4j.pojo.AnomalyDetectionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
public class WebConfiguration {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Collections.singletonList("*")); // Allows all origins
        corsConfig.setAllowedMethods(Collections.singletonList("*")); // Allows all HTTP methods
        corsConfig.setAllowedHeaders(Collections.singletonList("*")); // Allows all headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    @Bean
    public AnomalyDetectionEngine detectionEngine() {
        return new AnomalyDetectionEngine();
    }
}
