package org.greeneyed.tplanner.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonInclude(Include.NON_NULL)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "TPRM_STAT")
public class EstadisticaPremis {

  @Id
  @EqualsAndHashCode.Include
  @Column(name = "STA_ANY")
  private String any;

  @Column(name = "STA_PREMIS")
  private Integer premis;
}
