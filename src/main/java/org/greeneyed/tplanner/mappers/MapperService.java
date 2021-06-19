package org.greeneyed.tplanner.mappers;

import org.greeneyed.tplanner.model.Zone;
import org.greeneyed.tplanner.model.api.APIZone;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class MapperService {

  public abstract APIZone from(Zone zone);
}
