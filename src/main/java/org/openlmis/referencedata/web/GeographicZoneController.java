package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@RepositoryRestController
public class GeographicZoneController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeographicZoneController.class);

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  /**
   * Allows creating new geographicZones.
   *
   * @param geographicZone A geographicZone bound to the request body
   * @return ResponseEntity containing the created geographicZone
   */
  @RequestMapping(value = "/geographicZones", method = RequestMethod.POST)
  public ResponseEntity<?> createGeographicZone(@RequestBody GeographicZone geographicZone) {
    if (geographicZone == null) {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    } else {
      LOGGER.debug("Creating new geographicZone");
      // Ignore provided id
      geographicZone.setId(null);
      GeographicZone newGeographicZone = geographicZoneRepository.save(geographicZone);
      return new ResponseEntity<GeographicZone>(newGeographicZone, HttpStatus.CREATED);
    }
  }

  /**
   * Get all geographicZones.
   *
   * @return GeographicZones.
   */
  @RequestMapping(value = "/geographicZones", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getAllGeographicZones() {
    Iterable<GeographicZone> geographicZones = geographicZoneRepository.findAll();
    if (geographicZones == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(geographicZones, HttpStatus.OK);
    }
  }

  /**
   * Allows updating geographicZones.
   *
   * @param geographicZone A geographicZone bound to the request body
   * @param geographicZoneId UUID of geographicZone which we want to update
   * @return ResponseEntity containing the updated geographicZone
   */
  @RequestMapping(value = "/geographicZones/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateGeographicZone(@RequestBody GeographicZone geographicZone,
                                                 @PathVariable("id") UUID geographicZoneId) {
    GeographicZone geographicZoneFromDb = geographicZoneRepository.findOne(geographicZoneId);
    if (geographicZoneFromDb == null) {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    } else {
      LOGGER.debug("Updating geographicZone");
      GeographicZone updatedGeographicZone = geographicZoneRepository.save(geographicZone);
      return new ResponseEntity<GeographicZone>(updatedGeographicZone, HttpStatus.OK);
    }
  }

  /**
   * Get chosen geographicZone.
   *
   * @param geographicZoneId UUID of geographicZone which we want to get
   * @return geographicZone.
   */
  @RequestMapping(value = "/geographicZones/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getGeographicZone(@PathVariable("id") UUID geographicZoneId) {
    GeographicZone geographicZone = geographicZoneRepository.findOne(geographicZoneId);
    if (geographicZone == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(geographicZone, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting geographicZone.
   *
   * @param geographicZoneId UUID of geographicZone which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/geographicZones/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteGeographicZone(@PathVariable("id") UUID geographicZoneId) {
    GeographicZone geographicZone = geographicZoneRepository.findOne(geographicZoneId);
    if (geographicZone == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      try {
        geographicZoneRepository.delete(geographicZone);
      } catch (DataIntegrityViolationException ex) {
        LOGGER.debug("GeographicZone cannot be deleted because of existing dependencies", ex);
        return new ResponseEntity(HttpStatus.CONFLICT);
      }
      return new ResponseEntity<GeographicZone>(HttpStatus.NO_CONTENT);
    }
  }
}