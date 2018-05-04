package com.example.micrometerexample;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.MeterFilter;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MicrometerExampleApplication {

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        SpringApplication.run(MicrometerExampleApplication.class, args);
    }


    @Bean
    MeterRegistryCustomizer<MeterRegistry> customizer(@Value("${REGION:us-west}") String region) {
        return registry -> registry.config().commonTags("region", region);
    }

    @Bean
    MeterFilter meterFilter() {
        return MeterFilter.denyNameStartsWith("jvm");
    }

    @Bean
    ApplicationRunner runner(MeterRegistry mr) {
        return args -> {
            executorService.scheduleWithFixedDelay(() -> {
                Timer
                    .builder("transform-photo-task")
                    .sla(Duration.ofMillis(1), Duration.ofSeconds(10))
                    .publishPercentileHistogram()
                    .tag("format", Math.random() > .5 ? "png" : "jpg")
                    .register(mr)
                    .record(Duration.ofMillis((long) (Math.random() * 1000L)));
            }, 500L, 500L, TimeUnit.MILLISECONDS);
        };
    }
}
