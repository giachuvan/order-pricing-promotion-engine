package pricing.infrastructure.persistence;

import pricing.application.port.ProductCatalogPort;
import pricing.domain.model.Product;
import pricing.infrastructure.persistence.entity.ProductEntity;
import pricing.infrastructure.persistence.repository.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductCatalogAdapter implements ProductCatalogPort {

    private final ProductRepository productRepository;

    public ProductCatalogAdapter(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll().stream()
                .map(ProductCatalogAdapter::toDomain)
                .toList();
    }

    private static Product toDomain(ProductEntity entity) {
        return new Product(entity.getSku(), entity.getName(), entity.getPrice());
    }
}
