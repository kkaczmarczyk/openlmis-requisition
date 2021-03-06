package org.openlmis.referencedata.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.product.domain.Product;
import org.openlmis.product.domain.ProductCategory;
import org.openlmis.referencedata.domain.Stock;
import org.openlmis.referencedata.repository.StockRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class StockServiceTest {

  private List<Stock> stocks;
  private Integer currentInstanceNumber;

  @Mock
  private StockRepository stockRepository;

  @InjectMocks
  private StockService stockService;

  @Before
  public void setUp() {
    stocks = new ArrayList<>();
    currentInstanceNumber = 0;
    generateInstances();
    initMocks(this);
  }

  @Test
  public void shouldFindStockIfMatchedProduct() {
    when(stockRepository
            .searchStocks(stocks.get(0).getProduct()))
            .thenReturn(Arrays.asList(stocks.get(0)));
    List<Stock> receivedStocks = stockService.searchStocks(
        stocks.get(0).getProduct());

    assertEquals(1, receivedStocks.size());
    assertEquals(
        stocks.get(0).getProduct().getId(),
        receivedStocks.get(0).getProduct().getId());
    assertEquals(
        stocks.get(0).getId(),
        receivedStocks.get(0).getId());
  }

  private void generateInstances() {
    for (int instancesCount = 0; instancesCount < 5; instancesCount++) {
      stocks.add(generateStock());
    }
  }

  private Stock generateStock() {
    ProductCategory productCategory = generateProductCategory();
    Product product = generateProduct(productCategory);
    Stock stock = new Stock();
    stock.setId(UUID.randomUUID());
    stock.setProduct(product);
    return stock;
  }

  private ProductCategory generateProductCategory() {
    Integer instanceNumber = generateInstanceNumber();
    ProductCategory productCategory = new ProductCategory();
    productCategory.setId(UUID.randomUUID());
    productCategory.setCode("code" + instanceNumber);
    productCategory.setName("vaccine" + instanceNumber);
    productCategory.setDisplayOrder(1);
    return productCategory;
  }

  private Product generateProduct(ProductCategory productCategory) {
    Integer instanceNumber = generateInstanceNumber();
    Product product = new Product();
    product.setId(UUID.randomUUID());
    product.setCode("code" + instanceNumber);
    product.setPrimaryName("product" + instanceNumber);
    product.setDispensingUnit("unit" + instanceNumber);
    product.setDosesPerDispensingUnit(10);
    product.setPackSize(1);
    product.setPackRoundingThreshold(0);
    product.setRoundToZero(false);
    product.setActive(true);
    product.setFullSupply(true);
    product.setTracer(false);
    product.setProductCategory(productCategory);
    return product;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}
