package com.happytails.adoption.config;

import com.happytails.adoption.service.GeocodingService;
import com.happytails.adoption.service.MinIntervalLimiter;
import com.happytails.adoption.service.NominatimGeocodingService;
import java.net.http.HttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GeocodingProperties.class)
public class GeocodingConfig {

  /** The real geocoding service; created only when {@code geocoding.provider=nominatim}. */
  @Bean
  @ConditionalOnProperty(name = "geocoding.provider", havingValue = "nominatim")
  public GeocodingService nominatimGeocodingService(GeocodingProperties properties) {
    HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
    JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(properties.timeout());

    RestClient restClient =
        NominatimGeocodingService.createClient(
            properties, RestClient.builder().requestFactory(requestFactory));
    return new NominatimGeocodingService(
        restClient, new MinIntervalLimiter(properties.minInterval()));
  }
}
