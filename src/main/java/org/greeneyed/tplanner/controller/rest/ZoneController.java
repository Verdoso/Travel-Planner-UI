package org.greeneyed.tplanner.controller.rest;

import java.util.List;

import org.greeneyed.tplanner.controller.BasicController;
import org.greeneyed.tplanner.model.api.APIZone;
import org.greeneyed.tplanner.services.ZoneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

@RestController
@Data
@RequestMapping(value = BasicController.TRAVEL_PLANNER_BASIC_PATH + "/api/zone/")
public class ZoneController {

    private final ZoneService zoneService;

    @GetMapping
    public ResponseEntity<List<APIZone>> all() {
        return ResponseEntity.ok(zoneService.getAll());
    }
}