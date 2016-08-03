package org.openlmis.referencedata.web;

import org.openlmis.referencedata.utils.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {

  Logger logger = LoggerFactory.getLogger(VersionController.class);

  @RequestMapping("/version")
  public Version index() {
    logger.debug("Returning version");
    return new Version();
  }
}
