package org.acme;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import io.quarkus.runtime.Startup;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Startup
@Named
@Singleton
public class System {

    public final ZonedDateTime started = ZonedDateTime.now();

    public Duration upTime() {
        return Duration.between(started.withZoneSameInstant(
                ZoneId.systemDefault()).toLocalDateTime(),
                LocalDateTime.now());
    }

}
