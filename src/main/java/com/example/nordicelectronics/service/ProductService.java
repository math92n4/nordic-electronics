package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warranty;
import com.example.nordicelectronics.entity.dto.product.ProductRequestDTO;
import com.example.nordicelectronics.entity.dto.product.ProductResponseDTO;
import com.example.nordicelectronics.entity.mapper.ProductMapper;
import com.example.nordicelectronics.repositories.sql.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getBestSellingProducts() {
        String sql = "SELECT * FROM mv_best_selling_products ORDER BY total_units_sold DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getBestReviewedProducts() {
        String sql = "SELECT * FROM mv_best_reviewed_products ORDER BY average_rating DESC, number_of_reviews DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public void refreshAnalyticsViews() {
        jdbcTemplate.execute("SELECT fn_refresh_materialized_views()");
        System.out.println("Analytics views refreshed successfully");
    }

    public List<ProductResponseDTO> getAll() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO getById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return ProductMapper.toResponseDTO(product);
    }

    /**
     * Gets Product entity by ID. Used internally by other services that need the entity.
     * For API responses, use getById() which returns ProductResponseDTO.
     */
    public Product getEntityById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    @Transactional
    public ProductResponseDTO save(ProductRequestDTO dto) {
        Product product = ProductMapper.toEntity(dto);

        Brand brand = brandService.getById(dto.getBrandId());
        Warranty warranty = warrantyService.getById(dto.getWarrantyId());

        Set<Category> categories = dto.getCategoryIds().stream()
                .map(categoryService::getById)
                .collect(Collectors.toSet());

        product.setBrand(brand);
        product.setWarranty(warranty);
        product.setCategories(categories);

        Product saved = productRepository.save(product);
        return ProductMapper.toResponseDTO(saved);
    }

    @Transactional
    public ProductResponseDTO update(UUID id, ProductRequestDTO dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        existing.setSku(dto.getSku());
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setWeight(dto.getWeight());

        // Update brand if provided
        if (dto.getBrandId() != null) {
            Brand brand = brandService.getById(dto.getBrandId());
            existing.setBrand(brand);
        }

        // Update warranty if provided
        if (dto.getWarrantyId() != null) {
            Warranty warranty = warrantyService.getById(dto.getWarrantyId());
            existing.setWarranty(warranty);
        }

        // Update categories if provided
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            Set<Category> categories = dto.getCategoryIds().stream()
                    .map(categoryService::getById)
                    .collect(Collectors.toSet());
            existing.setCategories(categories);
        }

        Product saved = productRepository.save(existing);
        return ProductMapper.toResponseDTO(saved);
    }

    public void deleteById(UUID id) {
        productRepository.deleteById(id);
    }
}
