package pricing.application.port;

import pricing.domain.model.Product;

import java.util.List;

public interface ProductCatalogPort {

    List<Product> findAll();
}
