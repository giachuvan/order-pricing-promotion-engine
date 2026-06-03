package pricing.application;

import pricing.domain.model.Product;

import java.util.List;

public interface ProductCatalogUseCase {

    List<Product> listProducts();
}
