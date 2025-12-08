package com.example.nordicelectronics.service.mongodb;

import com.example.nordicelectronics.entity.mongodb.ProductDocument;
import com.example.nordicelectronics.repositories.mongodb.ProductMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ProductMongoService {

    private final ProductMongoRepository productMongoRepository;

    public List<ProductDocument> getAll() {
        return productMongoRepository.findAll();
    }

    public ProductDocument getByProductId(UUID productId) {
        return productMongoRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
    }

    public ProductDocument getBySku(String sku) {
        return productMongoRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
    }

    public List<ProductDocument> getByBrandId(UUID brandId) {
        return productMongoRepository.findByBrandId(brandId);
    }

    public List<ProductDocument> getByCategoryId(UUID categoryId) {
        return productMongoRepository.findByCategoryId(categoryId);
    }

    public ProductDocument save(ProductDocument productDocument) {
        if (productDocument.getProductId() == null) {
            productDocument.setProductId(UUID.randomUUID());
        }
        return productMongoRepository.save(productDocument);
    }

    public ProductDocument update(UUID productId, ProductDocument productDocument) {
        ProductDocument existing = getByProductId(productId);
        
        existing.setSku(productDocument.getSku());
        existing.setName(productDocument.getName());
        existing.setDescription(productDocument.getDescription());
        existing.setPrice(productDocument.getPrice());
        existing.setWeight(productDocument.getWeight());
        existing.setBrand(productDocument.getBrand());
        existing.setCategories(productDocument.getCategories());
        existing.setWarranty(productDocument.getWarranty());
        existing.setReviews(productDocument.getReviews());
        existing.setWarehouseStock(productDocument.getWarehouseStock());

        return productMongoRepository.save(existing);
    }

    public void deleteByProductId(UUID productId) {
        productMongoRepository.deleteByProductId(productId);
    }
}
