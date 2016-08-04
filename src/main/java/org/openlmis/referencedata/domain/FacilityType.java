package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "facility_types", schema = "referencedata")
@NoArgsConstructor
public class FacilityType extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String description;

  @Getter
  @Setter
  private Integer displayOrder;

  @Getter
  @Setter
  private Boolean active;

  /**
   * Returns a created facility type with only the required parameters set to dummy values.
   *
   * @return a mock facility type
   */
  public static FacilityType getTestFacilityType() {
    FacilityType ft = new FacilityType();

    ft.setCode("FT1");

    return ft;
  }
}
