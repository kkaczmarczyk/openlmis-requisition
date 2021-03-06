package org.openlmis.product.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.product.domain.ProductCategory;
import org.openlmis.product.repository.ProductCategoryRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProductCategoryServiceTest {

  @Mock
  private ProductCategoryRepository productCategoryRepository;

  @InjectMocks
  private ProductCategoryService productCategoryService;

  private Integer currentInstanceNumber;
  private List<ProductCategory> productCategories;

  @Before
  public void setUp() {
    productCategories = new ArrayList<>();
    currentInstanceNumber = 0;
    productCategoryService = new ProductCategoryService();
    generateInstances();
    initMocks(this);
  }

  @Test
  public void shouldFindProductCategoryIfMatchedCode() {
    when(productCategoryRepository
            .searchProductCategories(productCategories.get(0).getCode()))
            .thenReturn(Arrays.asList(productCategories.get(0)));

    List<ProductCategory> receivedProductCategories =
            productCategoryService.searchProductCategories(productCategories.get(0).getCode());

    assertEquals(1, receivedProductCategories.size());
    assertEquals(
            receivedProductCategories.get(0).getCode(),
            productCategories.get(0).getCode());
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }

  private void generateInstances() {
    for (int instancesCount = 0; instancesCount < 5; instancesCount++) {
      productCategories.add(generateProductCategory());
    }
  }

  private ProductCategory generateProductCategory() {
    ProductCategory productCategory = new ProductCategory();
    Integer instanceNumber = generateInstanceNumber();
    productCategory.setName("productCategoryName" + instanceNumber);
    productCategory.setCode("productCategoryCode" + instanceNumber);
    productCategory.setDisplayOrder(instanceNumber);
    return productCategory;
  }
}
