package org.openlmis.product.repository;

import org.openlmis.product.domain.ProductCategory;
import org.openlmis.product.repository.custom.ProductCategoryRepositoryCustom;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface ProductCategoryRepository extends
        PagingAndSortingRepository<ProductCategory, UUID>,
        ProductCategoryRepositoryCustom {

}
