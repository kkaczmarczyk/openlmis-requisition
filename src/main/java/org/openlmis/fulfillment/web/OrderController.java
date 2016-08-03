package org.openlmis.fulfillment.web;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.service.OrderService;
import org.openlmis.hierarchyandsupervision.domain.User;
import org.openlmis.requisition.domain.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;


@RepositoryRestController
public class OrderController {
  Logger logger = LoggerFactory.getLogger(OrderController.class);

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private OrderService orderService;

  /**
   * Allows finalizing orders.
   *
   * @param orderId The UUID of the order to finalize
   * @return ResponseEntity with the "#200 OK" HTTP response status on success
  or ResponseEntity containing the error description and "#400 Bad Request" status
   */

  @RequestMapping(value = "/orders/{id}/finalize", method = RequestMethod.PUT)
  public ResponseEntity<?> finalize(@PathVariable("id") UUID orderId) {

    Order order = orderRepository.findOne(orderId);

    if (order == null || order.getStatus() != OrderStatus.ORDERED) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    logger.debug("Finalizing the order");

    order.setStatus(OrderStatus.SHIPPED);
    orderRepository.save(order);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Returns csv or pdf of defined object in response.
   *
   * @param orderId UUID of order to print
   * @param format String describing return format (pdf or csv)
   * @param response HttpServletResponse object
   */
  @RequestMapping(value = "/orders/{id}/print", method = RequestMethod.GET)
  @ResponseBody
  public void printOrder(@PathVariable("id") UUID orderId,
                         @RequestParam("format") String format,
                         HttpServletResponse response) {
    Order order = orderRepository.findOne(orderId);
    if (order == null) {
      try {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Order does not exist.");
      } catch (IOException ex) {
        logger.info("Error sending error message to client.", ex);
      }
    }
    String[] columns = {"productName", "filledQuantity", "orderedQuantity"};
    if (format.equals("pdf")) {
      response.setContentType("application/pdf");
      response.addHeader("Content-Disposition",
              "attachment; filename=order-" + order.getOrderCode() + ".pdf");
      try {
        orderService.orderToPdf(order, columns, response.getOutputStream());
      } catch (IOException ex) {
        logger.debug("Error getting response output stream.", ex);
      }
    } else {
      response.setContentType("text/csv");
      response.addHeader("Content-Disposition",
              "attachment; filename=order" + order.getOrderCode() + ".csv");
      String csvContent = orderService.orderToCsv(order, columns);
      try {
        InputStream input = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        IOUtils.copy(input, response.getOutputStream());
        response.flushBuffer();
      } catch (IOException ex) {
        logger.debug("Error writing csv file to output stream.", ex);
      }
    }
  }

  /**
   * Converting Requisition list to orders.
   *
   * @param requisitionList List of Requisitions that will be converted to Orders
   * @return ResponseEntity with the "#200 OK" HTTP response status on success
   */
  @RequestMapping(value = "/orders/requisitions", method = RequestMethod.POST)
  public ResponseEntity<?> convertToOrder(@RequestBody List<Requisition> requisitionList,
                                          OAuth2Authentication auth) {
    UUID userId = null;
    if (auth != null && auth.getPrincipal() != null) {
      userId = ((User) auth.getPrincipal()).getId();
    }
    orderService.convertToOrder(requisitionList, userId);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
