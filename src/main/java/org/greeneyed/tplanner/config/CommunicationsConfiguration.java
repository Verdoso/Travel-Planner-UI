package org.greeneyed.tplanner.config;

import java.util.Arrays;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.greeneyed.summer.util.logging.LoggingClientHttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Data
@Slf4j
public class CommunicationsConfiguration {

    public static final String AIRPORT_FINDER_REST_TEMPLATE = "AIRPORT_FINDER_REST_TEMPLATE";

    @Value("${airport-finder.address:http://localhost:9999}")
    String airportFinderAddress;

    @Bean
    LoggingClientHttpRequestInterceptor loggingClientHttpRequestInterceptor() {
        return new LoggingClientHttpRequestInterceptor();
    }

    @Bean(name = AIRPORT_FINDER_REST_TEMPLATE)
    public RestTemplate osrRestTemplate(RestTemplateBuilder builder) {
        log.info("Configuring Airport Finder remote access to {}", airportFinderAddress);
        return builder.additionalInterceptors(Arrays.asList(loggingClientHttpRequestInterceptor()))
                .requestFactory(this::getClientHttpRequestFactory)
                .rootUri(airportFinderAddress).build();
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        //@formatter:off
            int timeout = 5000;
            RequestConfig config = RequestConfig.custom()
              .setConnectTimeout(timeout)
              .setConnectionRequestTimeout(timeout)
              .setSocketTimeout(timeout)
              .build();
            CloseableHttpClient client = HttpClientBuilder
              .create()
              .setDefaultRequestConfig(config)
              .build();
            //@formatter:on
        return new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(client));
      }
}
