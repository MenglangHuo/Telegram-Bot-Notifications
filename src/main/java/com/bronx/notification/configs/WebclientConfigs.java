package com.bronx.notification.configs;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class WebclientConfigs {
    // Slack API settings
    @Value("${slack.connect-timeout:3000}")
    private int slackConnectTimeout;
    @Value("${slack.read-timeout:5000}")
    private int slackReadTimeout;
    @Value("${slack.max-retries:2}")
    private int slackMaxRetries;
    // Connection pool settings
    @Value("${webclient.pool.max-connections:500}")
    private int maxConnections;
    @Value("${webclient.pool.max-idle-time:20}")
    private int maxIdleTimeSeconds;
    @Value("${webclient.pool.max-life-time:60}")
    private int maxLifeTimeSeconds;
    @Value("${webclient.pool.pending-acquire-timeout:45}")
    private int pendingAcquireTimeoutSeconds;

    @Bean("slackWebClient")
    public WebClient slackWebClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("slack-pool")
                .maxConnections(maxConnections)
                .maxIdleTime(Duration.ofSeconds(maxIdleTimeSeconds))
                .maxLifeTime(Duration.ofSeconds(maxLifeTimeSeconds))
                .pendingAcquireTimeout(Duration.ofSeconds(pendingAcquireTimeoutSeconds))
                .evictInBackground(Duration.ofSeconds(30))
                .build();
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, slackConnectTimeout)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(slackReadTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(slackConnectTimeout, TimeUnit.MILLISECONDS)))
                .responseTimeout(Duration.ofMillis(slackReadTimeout))
                .compress(true);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(retryFilter(slackMaxRetries, "Slack"))
                .build();
    }

    /**
     * Retry filter with exponential backoff.
     */
    private ExchangeFilterFunction retryFilter(int maxRetries, String serviceName) {
        return (request, next) -> next.exchange(request)
                .flatMap(response -> {
                    if (response.statusCode().is5xxServerError()) {
                        return Mono.error(new RuntimeException(
                                serviceName + " server error: " + response.statusCode()));
                    }
                    return Mono.just(response);
                })
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(500))
                        .maxBackoff(Duration.ofSeconds(5))
                        .jitter(0.5)
                        .filter(throwable -> isRetryable(throwable))
                        .doBeforeRetry(signal -> log.warn("Retrying {} request, attempt {}: {}",
                                serviceName, signal.totalRetries() + 1, signal.failure().getMessage()))
                        .onRetryExhaustedThrow((spec, signal) -> {
                            log.error("{} request failed after {} retries", serviceName, maxRetries);
                            return signal.failure();
                        }));

    }
    /**
     * Determine if an exception is retryable.
     */
    private boolean isRetryable(Throwable throwable) {
        // Retry on connection errors and 5xx server errors
        return throwable instanceof java.net.ConnectException
                || throwable instanceof java.net.SocketTimeoutException
                || throwable instanceof io.netty.channel.ConnectTimeoutException
                || throwable.getMessage() != null && throwable.getMessage().contains("server error");
    }


}
