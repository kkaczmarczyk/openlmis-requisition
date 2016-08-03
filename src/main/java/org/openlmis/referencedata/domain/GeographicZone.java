package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "geographic_zones", schema = "referencedata")
@NoArgsConstructor
public class GeographicZone extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "levelid", nullable = false)
  @Getter
  @Setter
  private GeographicLevel level;

  //@ManyToOne
  //@JoinColumn(name = "parentid")
  //@Getter
  //@Setter
  //private GeographicZone parent;

  @Getter
  @Setter
  private Integer catchmentPopulation;

  @Column(columnDefinition = "numeric(8,5)")
  @Getter
  @Setter
  private Double latitude;

  @Column(columnDefinition = "numeric(8,5)")
  @Getter
  @Setter
  private Double longitude;
  
  public static GeographicZone getMockGeographicZone() {
    GeographicZone gz = new GeographicZone();

    gz.setCode("GZ1");
    gz.setLevel(GeographicLevel.getMockGeographicLevel());
    
    return gz;
  }
}
