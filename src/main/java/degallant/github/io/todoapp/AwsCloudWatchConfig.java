package degallant.github.io.todoapp;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.time.Duration;
import java.util.Map;

@Configuration
public class AwsCloudWatchConfig implements CloudWatchConfig {

    private final Map<String, String> configuration = Map.of(
            "cloudwatch.namespace", "todoApp",
            "cloudwatch.step", Duration.ofMinutes(1).toString()
    );

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {

        //credentials are read from ~/.aws/credentials

        return CloudWatchAsyncClient.builder()
                .region(Region.SA_EAST_1)
                .build();

    }

    @Bean
    public MeterRegistry meterRegistry(CloudWatchAsyncClient cloudWatchAsyncClient) {

        return new CloudWatchMeterRegistry(
                this,
                Clock.SYSTEM,
                cloudWatchAsyncClient
        );

    }

    @Override
    public String get(String key) {
        return configuration.get(key);
    }

}
