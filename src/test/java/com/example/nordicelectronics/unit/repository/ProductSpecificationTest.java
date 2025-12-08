package com.example.nordicelectronics.unit.repository;

import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.repositories.sql.ProductSpecification;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSpecification Tests")
@SuppressWarnings({"unchecked", "rawtypes"})
class ProductSpecificationTest {

    @Mock
    private Root<Product> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<String> stringPath;

    @Mock
    private Path<Object> objectPath;

    @Mock
    private Join<Product, Category> categoryJoin;

    @Mock
    private Expression<String> lowerExpression;

    @Mock
    private Predicate predicate;

    @Mock
    private Predicate orPredicate;

    @Nested
    @DisplayName("searchByNameOrDescription Tests")
    class SearchByNameOrDescriptionTests {

        @Test
        @DisplayName("Should return null predicate when search is null")
        void shouldReturnNullWhenSearchIsNull() {
            // Arrange
            Specification<Product> spec = ProductSpecification.searchByNameOrDescription(null);

            // Act
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            // Assert
            assertThat(result).isNull();
            verifyNoInteractions(root, query, criteriaBuilder);
        }

        @Test
        @DisplayName("Should return null predicate when search is empty")
        void shouldReturnNullWhenSearchIsEmpty() {
            // Arrange
            Specification<Product> spec = ProductSpecification.searchByNameOrDescription("");

            // Act
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            // Assert
            assertThat(result).isNull();
            verifyNoInteractions(root, query, criteriaBuilder);
        }

        @Test
        @DisplayName("Should return null predicate when search is whitespace only")
        void shouldReturnNullWhenSearchIsWhitespace() {
            // Arrange
            Specification<Product> spec = ProductSpecification.searchByNameOrDescription("   ");

            // Act
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            // Assert
            assertThat(result).isNull();
            verifyNoInteractions(root, query, criteriaBuilder);
        }

        @Test
        @DisplayName("Should create OR predicate for name and description search")
        void shouldCreateOrPredicateForValidSearch() {
            // Arrange
            String searchTerm = "test";
            String expectedPattern = "%test%";

            when(root.get(anyString())).thenReturn((Path) stringPath);
            when(criteriaBuilder.lower(any(Expression.class))).thenReturn(lowerExpression);
            when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(predicate);
            when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(orPredicate);

            Specification<Product> spec = ProductSpecification.searchByNameOrDescription(searchTerm);

            // Act
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            // Assert
            assertThat(result).isEqualTo(orPredicate);
            verify(root).get("name");
            verify(root).get("description");
            verify(criteriaBuilder, times(2)).lower(any(Expression.class));
            verify(criteriaBuilder, times(2)).like(any(Expression.class), eq(expectedPattern));
            verify(criteriaBuilder).or(any(Predicate.class), any(Predicate.class));
        }

        @Test
        @DisplayName("Should trim and lowercase search term")
        void shouldTrimAndLowercaseSearchTerm() {
            // Arrange
            String searchTerm = "  TEST  ";
            String expectedPattern = "%test%";

            when(root.get(anyString())).thenReturn((Path) stringPath);
            when(criteriaBuilder.lower(any(Expression.class))).thenReturn(lowerExpression);
            when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(predicate);
            when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(orPredicate);

            Specification<Product> spec = ProductSpecification.searchByNameOrDescription(searchTerm);

            // Act
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            // Assert
            assertThat(result).isEqualTo(orPredicate);
            verify(criteriaBuilder, times(2)).like(any(Expression.class), eq(expectedPattern));
        }
    }

    @Nested
    @DisplayName("filterByBrand Tests")
    class FilterByBrandTests {

        @Test
        @DisplayName("Should return null predicate when brandId is null")
        void shouldReturnNullWhenBrandIdIsNull() {
            // Arrange
            Specification<Product> spec = ProductSpecification.filterByBrand(null);

            // Act
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            // Assert
            assertThat(result).isNull();
            verifyNoInteractions(root, query, criteriaBuilder);
        }

        @Test
        @DisplayName("Should create equal predicate for valid brandId")
        void shouldCreateEqualPredicateForValidBrandId() {
            // Arrange
            UUID brandId = UUID.randomUUID();

            when(root.get("brand")).thenReturn((Path) objectPath);
            when(objectPath.get("brandId")).thenReturn(objectPath);
            when(criteriaBuilder.equal(any(Path.class), any(UUID.class))).thenReturn(predicate);

            Specification<Product> spec = ProductSpecification.filterByBrand(brandId);

            // Act
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            // Assert
            assertThat(result).isEqualTo(predicate);
            verify(root).get("brand");
            verify(objectPath).get("brandId");
            verify(criteriaBuilder).equal(objectPath, brandId);
        }
    }

    @Nested
    @DisplayName("filterByCategory Tests")
    class FilterByCategoryTests {

        @Test
        @DisplayName("Should return null predicate when categoryId is null")
        void shouldReturnNullWhenCategoryIdIsNull() {
            // Arrange
            Specification<Product> spec = ProductSpecification.filterByCategory(null);

            // Act
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            // Assert
            assertThat(result).isNull();
            verifyNoInteractions(root, query, criteriaBuilder);
        }

        @Test
        @DisplayName("Should create join and equal predicate for valid categoryId")
        void shouldCreateJoinAndEqualPredicateForValidCategoryId() {
            // Arrange
            UUID categoryId = UUID.randomUUID();

            when(root.join(eq("categories"), eq(JoinType.INNER))).thenReturn((Join) categoryJoin);
            when(categoryJoin.get("categoryId")).thenReturn((Path) objectPath);
            when(criteriaBuilder.equal(any(Path.class), any(UUID.class))).thenReturn(predicate);

            Specification<Product> spec = ProductSpecification.filterByCategory(categoryId);

            // Act
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            // Assert
            assertThat(result).isEqualTo(predicate);
            verify(root).join("categories", JoinType.INNER);
            verify(categoryJoin).get("categoryId");
            verify(criteriaBuilder).equal(objectPath, categoryId);
        }
    }

    @Nested
    @DisplayName("withFilters Tests")
    class WithFiltersTests {

        @Test
        @DisplayName("Should return base specification when all filters are null")
        void shouldReturnBaseSpecificationWhenAllFiltersNull() {
            // Arrange & Act
            Specification<Product> spec = ProductSpecification.withFilters(null, null, null);

            // Assert
            assertThat(spec).isNotNull();
            // The specification should produce null predicate when executed with null filters
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);
            // Base specification with no filters returns null
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return base specification when search is empty and IDs are null")
        void shouldReturnBaseSpecificationWhenSearchEmptyAndIdsNull() {
            // Arrange & Act
            Specification<Product> spec = ProductSpecification.withFilters("", null, null);

            // Assert
            assertThat(spec).isNotNull();
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return base specification when search is whitespace only")
        void shouldReturnBaseSpecificationWhenSearchWhitespaceOnly() {
            // Arrange & Act
            Specification<Product> spec = ProductSpecification.withFilters("   ", null, null);

            // Assert
            assertThat(spec).isNotNull();
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should include search filter when search term is provided")
        void shouldIncludeSearchFilterWhenSearchProvided() {
            // Arrange
            String searchTerm = "laptop";
            String expectedPattern = "%laptop%";

            when(root.get(anyString())).thenReturn((Path) stringPath);
            when(criteriaBuilder.lower(any(Expression.class))).thenReturn(lowerExpression);
            when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(predicate);
            when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(orPredicate);

            Specification<Product> spec = ProductSpecification.withFilters(searchTerm, null, null);

            // Act
            spec.toPredicate(root, query, criteriaBuilder);

            // Assert
            verify(criteriaBuilder, times(2)).like(any(Expression.class), eq(expectedPattern));
        }

        @Test
        @DisplayName("Should include category filter when categoryId is provided")
        void shouldIncludeCategoryFilterWhenCategoryIdProvided() {
            // Arrange
            UUID categoryId = UUID.randomUUID();

            when(root.join(eq("categories"), eq(JoinType.INNER))).thenReturn((Join) categoryJoin);
            when(categoryJoin.get("categoryId")).thenReturn((Path) objectPath);
            when(criteriaBuilder.equal(any(Path.class), any(UUID.class))).thenReturn(predicate);

            Specification<Product> spec = ProductSpecification.withFilters(null, categoryId, null);

            // Act
            spec.toPredicate(root, query, criteriaBuilder);

            // Assert
            verify(root).join("categories", JoinType.INNER);
            verify(criteriaBuilder).equal(objectPath, categoryId);
        }

        @Test
        @DisplayName("Should include brand filter when brandId is provided")
        void shouldIncludeBrandFilterWhenBrandIdProvided() {
            // Arrange
            UUID brandId = UUID.randomUUID();

            when(root.get("brand")).thenReturn((Path) objectPath);
            when(objectPath.get("brandId")).thenReturn(objectPath);
            when(criteriaBuilder.equal(any(Path.class), any(UUID.class))).thenReturn(predicate);

            Specification<Product> spec = ProductSpecification.withFilters(null, null, brandId);

            // Act
            spec.toPredicate(root, query, criteriaBuilder);

            // Assert
            verify(root).get("brand");
            verify(criteriaBuilder).equal(objectPath, brandId);
        }

        @Test
        @DisplayName("Should combine all filters when all are provided")
        void shouldCombineAllFiltersWhenAllProvided() {
            // Arrange
            String searchTerm = "phone";
            String expectedPattern = "%phone%";
            UUID categoryId = UUID.randomUUID();
            UUID brandId = UUID.randomUUID();

            // Mock for search
            when(root.get(anyString())).thenReturn((Path) stringPath);
            when(criteriaBuilder.lower(any(Expression.class))).thenReturn(lowerExpression);
            when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(predicate);
            when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(orPredicate);

            // Mock for category join
            when(root.join(eq("categories"), eq(JoinType.INNER))).thenReturn((Join) categoryJoin);
            when(categoryJoin.get("categoryId")).thenReturn((Path) objectPath);

            // Mock for brand (need separate path)
            Path brandPath = mock(Path.class);
            Path brandIdPath = mock(Path.class);
            when(root.get("brand")).thenReturn(brandPath);
            when(brandPath.get("brandId")).thenReturn(brandIdPath);

            // Mock equals
            when(criteriaBuilder.equal(any(Path.class), any(UUID.class))).thenReturn(predicate);

            Specification<Product> spec = ProductSpecification.withFilters(searchTerm, categoryId, brandId);

            // Act
            spec.toPredicate(root, query, criteriaBuilder);

            // Assert - verify all filters were applied
            verify(criteriaBuilder, times(2)).like(any(Expression.class), eq(expectedPattern));
            verify(root).join("categories", JoinType.INNER);
            verify(root).get("brand");
        }
    }
}
