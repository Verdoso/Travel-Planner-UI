package org.greeneyed.tplanner.services;

import java.util.List;
import java.util.stream.Collectors;

import org.greeneyed.tplanner.db.ZoneRepository;
import org.greeneyed.tplanner.mappers.MapperService;
import org.greeneyed.tplanner.model.api.APIZone;
import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class ZoneService {

    private final ZoneRepository zoneRepository;

    private final MapperService mapperService;

    public List<APIZone> getAll() {
        return zoneRepository.findAll().stream().map(mapperService::from).collect(Collectors.toList());
    }
}
