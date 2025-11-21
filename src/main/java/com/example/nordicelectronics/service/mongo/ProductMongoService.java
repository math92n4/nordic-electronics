package com.example.nordicelectronics.service.mongo;

import com.example.nordicelectronics.document.ProductDocument;
import com.example.nordicelectronics.repositories.mongodb.ProductMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductMongoService {

    private final ProductMongoRepository productMongoRepository;

    public List<ProductDocument> getAll() {
        return productMongoRepository.findAll();
    }

    public ProductDocument getById(String id) {
        return productMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public ProductDocument save(ProductDocument product) {
        return productMongoRepository.save(product);
    }

    public ProductDocument update(String id, ProductDocument product) {
        ProductDocument existing = getById(id);
        existing.setSku(product.getSku());
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setWeight(product.getWeight());
        
        if (product.getBrandId() != null) {
            existing.setBrandId(product.getBrandId());
            existing.setBrandName(product.getBrandName());
        }
        
        if (product.getWarrantyId() != null) {
            existing.setWarrantyId(product.getWarrantyId());
            existing.setWarranty(product.getWarranty());
        }
        
        if (product.getCategoryIds() != null) {
            existing.setCategoryIds(product.getCategoryIds());
            existing.setCategories(product.getCategories());
        }
        
        return productMongoRepository.save(existing);
    }

    public void deleteById(String id) {
        productMongoRepository.deleteById(id);
    }

    public List<ProductDocument> getByBrand(String brandId) {
        return productMongoRepository.findByBrandId(brandId);
    }

    public List<ProductDocument> getByCategory(String categoryId) {
        return productMongoRepository.findByCategoryIdsContaining(categoryId);
    }
}

