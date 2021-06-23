package org.greeneyed.tplanner.controller.rest;

import java.util.List;

import org.greeneyed.tplanner.controller.BasicController;
import org.greeneyed.tplanner.services.airportf.APIAirportsResponse;
import org.greeneyed.tplanner.services.hotelf.APIDestination;
import org.greeneyed.tplanner.services.hotelf.HotelFinderClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

@RestController
@Data
@RequestMapping(value = BasicController.TRAVEL_PLANNER_BASIC_PATH + "/api/hotel/")
public class HotelFinderController {

    private final HotelFinderClient hotelFinderClient;

    @GetMapping()
    public ResponseEntity<List<APIDestination>> findClosestAirports(@RequestParam(name = "term") String term) {
        return ResponseEntity.ok(hotelFinderClient.getDestinations(term));
    }
}