package mil.barsolcom.tor_anomalies_backend.controller.web.rest.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MappingConfiguration {
    @Bean
    @Primary
    public ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // Optional: to use ISO format
        return mapper;
    }

    @Bean
    public CsvMapper csvMapper() {
        CsvMapper mapper = new CsvMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // Optional: to use ISO format
        mapper.enable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS); // Optional: to control date/time precision
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
