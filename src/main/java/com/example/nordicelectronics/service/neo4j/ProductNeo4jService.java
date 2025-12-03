package com.example.nordicelectronics.service.neo4j;

import com.example.nordicelectronics.entity.neo4j.ProductNode;
import com.example.nordicelectronics.repositories.neo4j.ProductNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductNeo4jService {

    private final ProductNeo4jRepository productNeo4jRepository;

    public List<ProductNode> getAll() {
        return productNeo4jRepository.findAll();
    }

    public ProductNode getByProductId(UUID productId) {
        return productNeo4jRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
    }

    public ProductNode getBySku(String sku) {
        return productNeo4jRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
    }

    public List<ProductNode> getByBrandId(UUID brandId) {
        return productNeo4jRepository.findByBrandId(brandId);
    }

    public List<ProductNode> getByCategoryId(UUID categoryId) {
        return productNeo4jRepository.findByCategoryId(categoryId);
    }

    public ProductNode save(ProductNode productNode) {
        if (productNode.getProductId() == null) {
            productNode.setProductId(UUID.randomUUID());
        }
        return productNeo4jRepository.save(productNode);
    }

    public ProductNode update(UUID productId, ProductNode productNode) {
        ProductNode existing = getByProductId(productId);

        existing.setSku(productNode.getSku());
        existing.setName(productNode.getName());
        existing.setDescription(productNode.getDescription());
        existing.setPrice(productNode.getPrice());
        existing.setWeight(productNode.getWeight());
        existing.setWarrantyId(productNode.getWarrantyId());
        existing.setBrandId(productNode.getBrandId());

        return productNeo4jRepository.save(existing);
    }

    public void deleteByProductId(UUID productId) {
        productNeo4jRepository.deleteByProductId(productId);
    }
}

