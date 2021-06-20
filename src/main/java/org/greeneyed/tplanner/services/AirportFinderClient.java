package org.greeneyed.tplanner.services;

import java.util.Optional;

import javax.annotation.Resource;

import org.greeneyed.summer.monitoring.Measured;
import org.greeneyed.tplanner.config.CommunicationsConfiguration;
import org.greeneyed.tplanner.services.airportf.APIAirportsResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Data
@Slf4j
public class AirportFinderClient {

    @Resource(name = CommunicationsConfiguration.AIRPORT_FINDER_REST_TEMPLATE)
    private RestTemplate restTemplate;

    @Measured
    public Optional<APIAirportsResponse> getClosestAirports(final String ip) {
        APIAirportsResponse airportsResponse = null;
        try {
            log.debug("Obtaining closest airport for IP: {}", ip);
            airportsResponse = restTemplate
                    .getForEntity("/locate_by_ip/{ip}/", APIAirportsResponse.class, ip)
                    .getBody();
        } catch (ResourceAccessException e) {
            log.error("Error reading Airport Finder response: {}", e.getMessage());
        } catch (HttpClientErrorException e) {
            log.error("Http Error Airport Finder response {}: {}", e.getStatusCode(), e.getStatusText());
        }
        return Optional.ofNullable(airportsResponse);
    }
}