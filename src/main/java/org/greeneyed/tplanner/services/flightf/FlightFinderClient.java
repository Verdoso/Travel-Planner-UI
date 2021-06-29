package org.greeneyed.tplanner.services.flightf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.Resource;

import org.greeneyed.summer.monitoring.Measured;
import org.greeneyed.tplanner.config.CommunicationsConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Data
@Slf4j
public class FlightFinderClient {

    @Resource(name = CommunicationsConfiguration.FLIGHT_FINDER_REST_TEMPLATE)
    private RestTemplate restTemplate;

    @Measured
    public List<APIAvailableFlight> getFlights(LocalDate outboundDate, LocalDate inboundDate, String country,
            String originId, String destinationId, String currency, String locale) {
        List<APIAvailableFlight> availableFlights = null;
        try {
            availableFlights = List.of(restTemplate.getForEntity(
                    "/search/{country}/{currency}/{locale}/{originId}/{destinationId}/{outboundDate}/{inboundDate}",
                    APIAvailableFlight[].class, country, currency, locale, originId, destinationId,
                    outboundDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    inboundDate.format(DateTimeFormatter.ISO_LOCAL_DATE)).getBody());
        } catch (ResourceAccessException e) {
            log.error("Error reading Flight search response: {}", e.getMessage());
        } catch (HttpClientErrorException e) {
            log.error("Http Error Flight search response {}: {}", e.getStatusCode(), e.getStatusText());
        }
        return availableFlights;
    }

}