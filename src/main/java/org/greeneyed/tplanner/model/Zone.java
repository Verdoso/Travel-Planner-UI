package org.greeneyed.tplanner.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonInclude(Include.NON_NULL)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TRVL_ZONE")
public class Zone {

  @Id
  @EqualsAndHashCode.Include
  @Column(name = "ZON_ID")
  private String id;

  @Column(name = "ZON_NAME", length = 255)
  private String name;

  @Column(name = "ZON_DESCRIPTION", length =1024)
  private String description;
}
