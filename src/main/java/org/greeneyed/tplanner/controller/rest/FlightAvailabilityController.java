package org.greeneyed.tplanner.controller.rest;

import java.time.LocalDate;
import java.util.List;

import org.greeneyed.tplanner.controller.BasicController;
import org.greeneyed.tplanner.services.flightf.APIAvailableFlight;
import org.greeneyed.tplanner.services.flightf.FlightFinderClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@RestController
@RequestMapping(value = BasicController.TRAVEL_PLANNER_BASIC_PATH + "/api/flight/")
public class FlightAvailabilityController {

    private final FlightFinderClient flightFinderClient;

    @GetMapping(value = "/search/{country}/{currency}/{locale}/{originId}/{destinationId}/{outboundDate}/{inboundDate}")
    public ResponseEntity<List<APIAvailableFlight>> availability(
            @PathVariable(value = "outboundDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate outboundDate,
            @PathVariable(value = "inboundDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inboundDate,
            @PathVariable(value = "country") String country,
            @PathVariable(value = "originId") String originId,
            @PathVariable(value = "destinationId") String destinationId,
            @PathVariable(value = "currency") String currency,
            @PathVariable(value = "locale") String locale) {
        final List<APIAvailableFlight> availability = flightFinderClient.getFlights(outboundDate, inboundDate, country, originId, destinationId, currency, locale);
        return ResponseEntity
                .ok(availability);
    }
}
