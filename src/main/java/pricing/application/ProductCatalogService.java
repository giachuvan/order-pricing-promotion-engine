package pricing.application;

import pricing.application.port.ProductCatalogPort;
import pricing.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductCatalogService implements ProductCatalogUseCase {

    private final ProductCatalogPort productCatalogPort;

    public ProductCatalogService(ProductCatalogPort productCatalogPort) {
        this.productCatalogPort = productCatalogPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> listProducts() {
        return productCatalogPort.findAll();
    }
}
