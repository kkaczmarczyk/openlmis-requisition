package org.openlmis.requisition.web;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.openlmis.hierarchyandsupervision.domain.User;
import org.openlmis.referencedata.domain.Comment;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.repository.CommentRepository;
import org.openlmis.requisition.domain.Requisition;
import org.openlmis.requisition.domain.RequisitionStatus;
import org.openlmis.requisition.exception.RequisitionException;
import org.openlmis.requisition.repository.RequisitionRepository;
import org.openlmis.requisition.service.RequisitionService;
import org.openlmis.requisition.validate.RequisitionValidator;
import org.openlmis.settings.service.ConfigurationSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.validation.Valid;

@RepositoryRestController
@SuppressWarnings("PMD.TooManyMethods")
public class RequisitionController {
  private static final Logger LOGGER = LoggerFactory.getLogger(RequisitionController.class);

  @Autowired
  private RequisitionRepository requisitionRepository;

  @Autowired
  @Qualifier("beforeSaveRequisitionValidator")
  private RequisitionValidator validator;

  @Autowired
  private RequisitionService requisitionService;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private ConfigurationSettingService configurationSettingService;

  @InitBinder("requisition")
  protected void initBinder(final WebDataBinder binder) {
    binder.addValidators(validator);
  }

  /**
   * Initiates requisition.
   * @return result
   */
  @RequestMapping(value = "/requisitions/initiate", method = POST)
  public ResponseEntity<?> initiateRequisition(@RequestBody @Valid Requisition requisitionDto,
                                               BindingResult bindingResult) {
    try {

      Requisition requisition = requisitionService.initiateRequisition(
          requisitionDto);
      ResponseEntity response = new ResponseEntity<>(requisition, HttpStatus.CREATED);
      return response;

    } catch (RequisitionException ex) {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Submits earlier initiated requisition.
   */
  @RequestMapping(value = "/requisitions/{id}/submit", method = RequestMethod.PUT)
  public ResponseEntity<?> submitRequisition(@RequestBody @Valid Requisition requisition,
                                             BindingResult bindingResult,
                                             @PathVariable("id") UUID requisitionId) {
    if (!bindingResult.hasErrors()) {
      try {
        requisition = requisitionService.submitRequisition(requisition);
      } catch (RequisitionException ex) {
        LOGGER.debug(ex.getMessage(), ex);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
      return new ResponseEntity<Object>(requisition, HttpStatus.OK);
    } else {
      return new ResponseEntity(getRequisitionErrors(bindingResult), HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Deletes requisition with the given id.
   */
  @RequestMapping(value = "/requisitions/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteRequisition(@PathVariable("id") UUID requisitionId) {
    try {
      boolean deleted = requisitionService.tryDelete(requisitionId);
      if (deleted) {
        return new ResponseEntity(HttpStatus.NO_CONTENT);
      } else {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } catch (RequisitionException ex) {
      LOGGER.debug(ex.getMessage(), ex);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Finds requisitions matching all of provided parameters.
   */
  @RequestMapping(value = "/requisitions/search", method = RequestMethod.GET)
  public ResponseEntity<?> searchRequisitions(
      @RequestParam(value = "facility", required = false) Facility facility,
      @RequestParam(value = "program", required = false) Program program,
      @RequestParam(value = "createdDateFrom", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateFrom,
      @RequestParam(value = "createdDateTo", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateTo) {

    List<Requisition> result = requisitionService.searchRequisitions(facility, program,
        createdDateFrom, createdDateTo);

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  /**
   * Skipping chosen requisition period.
   */
  @RequestMapping(value = "/requisitions/{id}/skip", method = RequestMethod.PUT)
  public ResponseEntity<?> skipRequisition(@PathVariable("id") UUID requisitionId) {
    ResponseEntity<Object> responseEntity;
    try {
      Requisition requisition = requisitionService.skip(requisitionId);
      responseEntity = new ResponseEntity<>(requisition, HttpStatus.OK);
    } catch (RequisitionException ex) {
      LOGGER.debug(ex.getMessage(), ex);
      responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    return responseEntity;
  }

  /**
   * Rejecting requisition which is waiting for approve.
   */
  @RequestMapping(value = "/requisitions/{id}/reject", method = RequestMethod.PUT)
  public ResponseEntity<?> rejectRequisition(@PathVariable("id") UUID id) {

    try {
      requisitionService.reject(id);
    } catch (RequisitionException ex) {
      LOGGER.debug(ex.getMessage(), ex);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Requisition rejectedRequisition = requisitionRepository.findOne(id);
    return new ResponseEntity<>(rejectedRequisition, HttpStatus.OK);
  }

  /**
   * Add comment to the requisition.
   */
  @PreAuthorize("isAuthenticated()")
  @RequestMapping(value = "/requisitions/{id}/comments", method = RequestMethod.POST)
  public ResponseEntity<Object> insertComment(@RequestBody Comment comment,
                                         @PathVariable("id") UUID id, OAuth2Authentication auth) {
    Requisition requisition = requisitionRepository.findOne(id);
    comment.setRequisition(requisition);

    User user = (User) auth.getPrincipal();

    comment.setAuthor(user);
    commentRepository.save(comment);

    List<Comment> comments = requisitionService.getCommentsByReqId(id);
    return new ResponseEntity<>(comments, HttpStatus.OK);
  }

  /**
   * Get all comments for specified requisition.
   */
  @RequestMapping(value = "/requisitions/{id}/comments", method = RequestMethod.GET)
  public ResponseEntity<Object> getCommentsForRequisition(@PathVariable("id") UUID id) {
    List<Comment> comments = requisitionService.getCommentsByReqId(id);
    return new ResponseEntity<Object>(comments, HttpStatus.OK);
  }

  /**
   * Approve specified by id requisition.
   */
  @PreAuthorize("isAuthenticated()")
  @RequestMapping(value = "/requisitions/{id}/approve", method = RequestMethod.PUT)
  public ResponseEntity<?> approveRequisition(@PathVariable("id") UUID requisitionId) {
    Requisition requisition = requisitionRepository.findOne(requisitionId);
    if (requisition == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
    if (requisition.getStatus() == RequisitionStatus.AUTHORIZED
        || (configurationSettingService.getBoolValue("skipAuthorization")
        && requisition.getStatus() == RequisitionStatus.SUBMITTED)) {
      requisition.setStatus(RequisitionStatus.APPROVED);
      requisitionRepository.save(requisition);
      LOGGER.debug("Requisition with id " + requisitionId + " approved");
      return new ResponseEntity<>(requisition, HttpStatus.OK);
    } else {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Get requisitions to approve for right supervisor.
   */
  @PreAuthorize("isAuthenticated()")
  @RequestMapping(value = "/requisitions-for-approval", method = RequestMethod.GET)
  public ResponseEntity<Object> listForApproval(OAuth2Authentication auth) {
    User user = (User) auth.getPrincipal();
    List<Requisition> requisitions = requisitionService.getRequisitionsForApproval(user.getId());
    return new ResponseEntity<Object>(requisitions, HttpStatus.OK);
  }

  private Map<String, String> getRequisitionErrors(BindingResult bindingResult) {
    return new HashMap<String, String>() {
      {
        for (FieldError error : bindingResult.getFieldErrors()) {
          put(error.getField(), error.getCode());
        }
      }
    };
  }

  /**
   * Get all submitted Requisitions.
   *
   * @return Submitted requisitions.
   */
  @RequestMapping(value = "/requisitions/submitted", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getSubmittedRequisitions() {

    Iterable<Requisition> submittedRequisitions =
        requisitionRepository.findByStatus(RequisitionStatus.SUBMITTED);
    if (submittedRequisitions == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(submittedRequisitions, HttpStatus.OK);
    }
  }

  /**
   * Authorize given requisition.
   *
   * @param requisitionDto Requisition object to be authorized.
   * @param bindingResult Object used for validation.
   * @param requisitionId UUID of Requisition to authorize.
   * @return ResponseEntity with authorized Requisition if authorization was successful.
   */
  @RequestMapping(value = "/requisitions/{id}/authorize", method = RequestMethod.PUT)
  public ResponseEntity<?> authorizeRequisition(@RequestBody Requisition requisitionDto,
                                                BindingResult bindingResult,
                                                @PathVariable("id") UUID requisitionId) {

    if (requisitionId == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    try {
      requisitionDto = requisitionService.authorize(requisitionId, requisitionDto,
          bindingResult.hasErrors());
      LOGGER.info("Requisition: " +  requisitionId + " authorized.");
    } catch (RequisitionException ex) {
      LOGGER.debug(ex.getMessage(), ex);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(requisitionDto, HttpStatus.OK);
  }
}
