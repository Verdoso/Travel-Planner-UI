package org.greeneyed.tplanner.controller.rest;

import java.time.LocalDate;
import java.util.List;

import org.greeneyed.tplanner.controller.BasicController;
import org.greeneyed.tplanner.services.hotelf.APIAvailableHotel;
import org.greeneyed.tplanner.services.hotelf.APIDestination;
import org.greeneyed.tplanner.services.hotelf.HotelFinderClient;
import org.springframework.format.annotation.DateTimeFormat;
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

    @GetMapping(value = "/destinations")
    public ResponseEntity<List<APIDestination>> findClosestAirports(@RequestParam(name = "term") String term) {
        return ResponseEntity.ok(hotelFinderClient.getDestinations(term));
    }

    @GetMapping(value = "/availability")
    public ResponseEntity<List<APIAvailableHotel>> availability(
            @RequestParam(name = "checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(name = "checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(name = "destinationId") String destinationId,
            @RequestParam(name = "numAdults") Integer numAdults,
            @RequestParam(name = "numChildren") Integer numChildren) {
        return ResponseEntity
                .ok(hotelFinderClient.getAvailability(checkIn, checkOut, destinationId, numAdults, numChildren));
    }
}