package gg.amy;

import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import lombok.Getter;

/**
 * @author amy
 * @since 12/28/17.
 */
public class Metrics {
    @Getter
    private final StatsDClient client;
    
    public Metrics() {
        final String metricsEnabled = System.getenv("METRICS_ENABLED");
        if(metricsEnabled == null) {
            client = new NoOpStatsDClient();
        } else {
            boolean canStats = Boolean.parseBoolean(metricsEnabled);
            if(canStats) {
                client = new NonBlockingStatsDClient(System.getenv("METRICS_PREFIX"),
                        System.getenv("METRICS_HOST"), 8125, "");
            } else {
                client = new NoOpStatsDClient();
            }
        }
    }
}
