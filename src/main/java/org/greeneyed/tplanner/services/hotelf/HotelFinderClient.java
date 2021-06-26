package org.greeneyed.tplanner.services.hotelf;

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
    public List<APIAvailableHotel> getAvailability(LocalDate checkIn, LocalDate checkOut, String destinationId,
            Integer numAdults, Integer numChildren) {
        List<APIAvailableHotel> availableHotels = null;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/availability")
                    .queryParam("destinationId", destinationId)
                    .queryParam("numAdults", numAdults)
                    .queryParam("numChildren", numChildren)
                    .queryParam("checkIn", checkIn.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .queryParam("checkOut", checkOut.format(DateTimeFormatter.ISO_LOCAL_DATE));
            availableHotels = List
                    .of(restTemplate.getForEntity(builder.toUriString(), APIAvailableHotel[].class).getBody());
        } catch (ResourceAccessException e) {
            log.error("Error reading Hotel availability response: {}", e.getMessage());
        } catch (HttpClientErrorException e) {
            log.error("Http Error Hotel availability response {}: {}", e.getStatusCode(), e.getStatusText());
        }
        return availableHotels;
    }

    @Measured
    public List<APIDestination> getDestinations(final String term) {
        List<APIDestination> destinations = null;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/zone").queryParam("query", term);
            destinations = List
                    .of(restTemplate.getForEntity(builder.toUriString(), APIDestination[].class).getBody());
        } catch (ResourceAccessException e) {
            log.error("Error reading Hotel zones response: {}", e.getMessage());
        } catch (HttpClientErrorException e) {
            log.error("Http Error Hotel zones response {}: {}", e.getStatusCode(), e.getStatusText());
        }
        return destinations;
    }
}