package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warranty;
import com.example.nordicelectronics.repositories.sql.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final WarrantyService warrantyService;

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product getById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    @Transactional
    public Product save(Product product) {

        Brand brand = brandService.getById(product.getBrand().getBrandId());
        Warranty warranty = warrantyService.getById(product.getWarranty().getWarrantyId());

        Set<Category> categories = product.getCategories().stream()
                .map(c -> categoryService.getById(c.getCategoryId()))
                .collect(Collectors.toSet());

        product.setBrand(brand);
        product.setWarranty(warranty);
        product.setCategories(categories);

        return productRepository.save(product);
    }

    @Transactional
    public Product update(UUID id, Product product) {
        Product existing = getById(id);

        existing.setSku(product.getSku());
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setWeight(product.getWeight());

        // Update brand if provided
        if (product.getBrand() != null && product.getBrand().getBrandId() != null) {
            Brand brand = brandService.getById(product.getBrand().getBrandId());
            existing.setBrand(brand);
        }

        // Update warranty if provided
        if (product.getWarranty() != null && product.getWarranty().getWarrantyId() != null) {
            Warranty warranty = warrantyService.getById(product.getWarranty().getWarrantyId());
            existing.setWarranty(warranty);
        }

        // Update categories if provided
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            Set<Category> categories = product.getCategories().stream()
                    .map(c -> categoryService.getById(c.getCategoryId()))
                    .collect(Collectors.toSet());
            existing.setCategories(categories);
        }

        return productRepository.save(existing);
    }

    public void deleteById(UUID id) {
        productRepository.deleteById(id);
    }

    public String getSomething() {
        return "Something";
    }
}
