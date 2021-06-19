package org.greeneyed.tplanner.db;

import org.greeneyed.tplanner.model.Zone;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface ZoneRepository extends JpaRepositoryImplementation<Zone, String> {
}
