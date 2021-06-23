package org.greeneyed.tplanner.services.hotelf;

import java.util.List;

import javax.annotation.Resource;

import org.greeneyed.summer.monitoring.Measured;
import org.greeneyed.tplanner.config.CommunicationsConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Data
@Slf4j
public class HotelFinderClient {

    @Resource(name = CommunicationsConfiguration.HOTEL_FINDER_REST_TEMPLATE)
    private RestTemplate restTemplate;

    @Measured
    public List<APIDestination> getDestinations(final String term) {
        List<APIDestination> destinations = null;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/zone").queryParam("query", term);
            destinations = List.of(restTemplate.getForEntity(builder.toUriString(), APIDestination[].class, term).getBody());
        } catch (ResourceAccessException e) {
            log.error("Error reading Hotel Finder response: {}", e.getMessage());
        } catch (HttpClientErrorException e) {
            log.error("Http Error Hotel Finder response {}: {}", e.getStatusCode(), e.getStatusText());
        }
        return destinations;
    }
}