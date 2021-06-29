package org.greeneyed.tplanner.controller.rest;

import org.greeneyed.tplanner.controller.BasicController;
import org.greeneyed.tplanner.services.airportf.APIAirportsResponse;
import org.greeneyed.tplanner.services.airportf.AirportFinderClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

@RestController
@Data
@RequestMapping(value = BasicController.TRAVEL_PLANNER_BASIC_PATH + "/api/airport/")
public class AirportFinderController {

    private static final ResponseEntity<APIAirportsResponse> BAD_REQUEST = ResponseEntity.badRequest().build();
    private final AirportFinderClient airportFinderClient;

    @GetMapping(path = "by_ip/")
    public ResponseEntity<APIAirportsResponse> findClosestAirports(@RequestParam(name = "ip") String ip) {
        return airportFinderClient.getClosestAirports(ip).map(ResponseEntity::ok).orElse(BAD_REQUEST);
    }

    @GetMapping(path = "by_position/")
    public ResponseEntity<APIAirportsResponse> findClosestDestinationAirports(
            @RequestParam(name = "latitude") String latitude, @RequestParam(name = "longitude") String longitude) {
        return airportFinderClient.getClosestAirports(latitude, longitude).map(ResponseEntity::ok).orElse(BAD_REQUEST);
    }
}