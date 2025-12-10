import { useState, useEffect, useCallback } from "react";
import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { productsAPI, categoriesAPI, brandsAPI } from "../api";
import { useCart } from "../hooks/useCart";
import { showAlert } from "../utils/alerts";
import { ProductModal } from "../components/ProductModal";

function resolveProductId(product) {
  // ProductResponseDTO uses productId field
  return product.productId || product.id || product._id || product.sku;
}

// Debounce hook for search input
function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

const PAGE_SIZE = 24;

export function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  // Pagination state
  const [currentPage, setCurrentPage] = useState(
    parseInt(searchParams.get("page")) || 0
  );

  // Filter state
  const [searchTerm, setSearchTerm] = useState(
    searchParams.get("search") || ""
  );
  const [selectedCategory, setSelectedCategory] = useState(
    searchParams.get("category") || ""
  );
  const [selectedBrand, setSelectedBrand] = useState(
    searchParams.get("brand") || ""
  );
  // Combined sort option (e.g., "name-asc", "price-desc")
  const [sortOption, setSortOption] = useState(() => {
    const sortBy = searchParams.get("sortBy") || "name";
    const sortDir = searchParams.get("sortDirection") || "asc";
    return `${sortBy}-${sortDir}`;
  });

  const [selectedProductId, setSelectedProductId] = useState(null);
  const { addToCart } = useCart();

  // Debounce search term to avoid too many API calls
  const debouncedSearchTerm = useDebounce(searchTerm, 300);

  // Parse sort option into sortBy and sortDirection
  const [sortBy, sortDirection] = sortOption.split("-");

  // Sync state with URL params on mount
  useEffect(() => {
    const pageParam = parseInt(searchParams.get("page")) || 0;
    const categoryParam = searchParams.get("category") || "";
    const brandParam = searchParams.get("brand") || "";
    const searchParam = searchParams.get("search") || "";
    const sortByParam = searchParams.get("sortBy") || "name";
    const sortDirParam = searchParams.get("sortDirection") || "asc";
    const newSortOption = `${sortByParam}-${sortDirParam}`;

    if (pageParam !== currentPage) setCurrentPage(pageParam);
    if (categoryParam !== selectedCategory) setSelectedCategory(categoryParam);
    if (brandParam !== selectedBrand) setSelectedBrand(brandParam);
    if (searchParam !== searchTerm) setSearchTerm(searchParam);
    if (newSortOption !== sortOption) setSortOption(newSortOption);
  }, []);

  // Update URL when filters change
  const updateUrlParams = useCallback(
    (updates = {}) => {
      const params = new URLSearchParams();

      const page = updates.page !== undefined ? updates.page : currentPage;
      const search =
        updates.search !== undefined ? updates.search : debouncedSearchTerm;
      const category =
        updates.category !== undefined ? updates.category : selectedCategory;
      const brand = updates.brand !== undefined ? updates.brand : selectedBrand;

      // Parse sortOption for URL params
      const currentSortOption =
        updates.sortOption !== undefined ? updates.sortOption : sortOption;
      const [sort, dir] = currentSortOption.split("-");

      if (page > 0) params.set("page", page);
      if (search) params.set("search", search);
      if (category) params.set("category", category);
      if (brand) params.set("brand", brand);
      if (sort !== "name") params.set("sortBy", sort);
      if (dir !== "asc") params.set("sortDirection", dir);

      setSearchParams(params, { replace: true });
    },
    [
      currentPage,
      debouncedSearchTerm,
      selectedCategory,
      selectedBrand,
      sortOption,
      setSearchParams,
    ]
  );

  // Fetch paginated products
  const {
    data: productsData,
    isLoading: productsLoading,
    isFetching,
  } = useQuery({
    queryKey: [
      "products",
      "paginated",
      currentPage,
      PAGE_SIZE,
      debouncedSearchTerm,
      selectedCategory,
      selectedBrand,
      sortOption,
    ],
    queryFn: () =>
      productsAPI.getPaginated({
        page: currentPage,
        size: PAGE_SIZE,
        search: debouncedSearchTerm || undefined,
        categoryId: selectedCategory || undefined,
        brandId: selectedBrand || undefined,
        sortBy: sortBy,
        sortDirection: sortDirection,
      }),
    keepPreviousData: true,
  });

  const { data: categories = [], isLoading: categoriesLoading } = useQuery({
    queryKey: ["categories"],
    queryFn: () => categoriesAPI.getAll(),
  });

  const { data: brands = [], isLoading: brandsLoading } = useQuery({
    queryKey: ["brands"],
    queryFn: () => brandsAPI.getAll(),
  });

  // Listen for filter events from CategoriesPage and BrandsPage
  useEffect(() => {
    const handleCategoryFilter = e => {
      const categoryId = e.detail.categoryId;
      setSelectedCategory(categoryId);
      setCurrentPage(0);
      updateUrlParams({ category: categoryId, page: 0 });
    };

    const handleBrandFilter = e => {
      const brandId = e.detail.brandId;
      setSelectedBrand(brandId);
      setCurrentPage(0);
      updateUrlParams({ brand: brandId, page: 0 });
    };

    window.addEventListener("filterByCategory", handleCategoryFilter);
    window.addEventListener("filterByBrand", handleBrandFilter);

    return () => {
      window.removeEventListener("filterByCategory", handleCategoryFilter);
      window.removeEventListener("filterByBrand", handleBrandFilter);
    };
  }, [updateUrlParams]);

  // Update URL when debounced search term changes
  useEffect(() => {
    updateUrlParams({ search: debouncedSearchTerm, page: 0 });
    if (debouncedSearchTerm !== searchParams.get("search")) {
      setCurrentPage(0);
    }
  }, [debouncedSearchTerm]);

  const handlePageChange = newPage => {
    setCurrentPage(newPage);
    updateUrlParams({ page: newPage });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleCategoryChange = categoryId => {
    setSelectedCategory(categoryId);
    setCurrentPage(0);
    updateUrlParams({ category: categoryId, page: 0 });
  };

  const handleBrandChange = brandId => {
    setSelectedBrand(brandId);
    setCurrentPage(0);
    updateUrlParams({ brand: brandId, page: 0 });
  };

  const handleSortOptionChange = newSortOption => {
    setSortOption(newSortOption);
    setCurrentPage(0);
    updateUrlParams({ sortOption: newSortOption, page: 0 });
  };

  const handleAddToCart = product => {
    addToCart(product);
    const productName = product.name || "Item";
    showAlert(`${productName} added to cart`, "success");
  };

  // Extract data from response
  const products = productsData?.content || [];
  const totalPages = productsData?.totalPages || 0;
  const totalElements = productsData?.totalElements || 0;
  const hasNext = productsData?.hasNext || false;
  const hasPrevious = productsData?.hasPrevious || false;

  // Generate page numbers for pagination
  const getPageNumbers = () => {
    const pages = [];
    const maxVisiblePages = 5;

    if (totalPages <= maxVisiblePages) {
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Always show first page
      pages.push(0);

      let start = Math.max(1, currentPage - 1);
      let end = Math.min(totalPages - 2, currentPage + 1);

      if (currentPage <= 2) {
        end = 3;
      } else if (currentPage >= totalPages - 3) {
        start = totalPages - 4;
      }

      if (start > 1) {
        pages.push("...");
      }

      for (let i = start; i <= end; i++) {
        pages.push(i);
      }

      if (end < totalPages - 2) {
        pages.push("...");
      }

      // Always show last page
      if (totalPages > 1) {
        pages.push(totalPages - 1);
      }
    }

    return pages;
  };

  if (productsLoading && !productsData) {
    return (
      <div className="section">
        <div className="container">
          <h2 className="section-title">Our Products</h2>
          <p className="loading">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="section">
      <div className="container">
        <h2 className="section-title">Our Products</h2>

        {/* Filters */}
        <div className="filters">
          <div className="filter-group">
            <label htmlFor="category-filter" className="visually-hidden">
              Filter by category
            </label>
            <select
              id="category-filter"
              className="filter-select"
              value={selectedCategory}
              onChange={e => handleCategoryChange(e.target.value)}
              aria-label="Filter by category"
            >
              <option value="">All Categories</option>
              {categories.map((cat, index) => {
                const catId = cat.categoryId || cat.id;
                const catIdValue = catId ? String(catId) : "";
                return (
                  <option key={catIdValue || `cat-${index}`} value={catIdValue}>
                    {cat.name || "Unnamed Category"}
                  </option>
                );
              })}
            </select>
          </div>

          <div className="filter-group">
            <label htmlFor="brand-filter" className="visually-hidden">
              Filter by brand
            </label>
            <select
              id="brand-filter"
              className="filter-select"
              value={selectedBrand}
              onChange={e => handleBrandChange(e.target.value)}
              aria-label="Filter by brand"
            >
              <option value="">All Brands</option>
              {brands.map((brand, index) => {
                const brandId = brand.brandId || brand.id;
                const brandIdValue = brandId ? String(brandId) : "";
                return (
                  <option
                    key={brandIdValue || `brand-${index}`}
                    value={brandIdValue}
                  >
                    {brand.name || "Unnamed Brand"}
                  </option>
                );
              })}
            </select>
          </div>

          <div className="filter-group">
            <label htmlFor="search-input" className="visually-hidden">
              Search products
            </label>
            <input
              type="text"
              id="search-input"
              className="search-input"
              placeholder="Search products..."
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              aria-label="Search products"
            />
          </div>

          <div className="filter-group">
            <label htmlFor="sort-filter" className="visually-hidden">
              Sort products
            </label>
            <select
              id="sort-filter"
              className="filter-select"
              value={sortOption}
              onChange={e => handleSortOptionChange(e.target.value)}
              aria-label="Sort products"
            >
              <option value="name-asc">Name: A to Z</option>
              <option value="name-desc">Name: Z to A</option>
              <option value="price-asc">Price: Low to High</option>
              <option value="price-desc">Price: High to Low</option>
            </select>
          </div>
        </div>

        {/* Results info */}
        <div
          className="results-info"
          style={{
            marginBottom: "1rem",
            color: "#666",
          }}
        >
          {totalElements > 0 ? (
            <>
              Showing {currentPage * PAGE_SIZE + 1}-
              {Math.min((currentPage + 1) * PAGE_SIZE, totalElements)} of{" "}
              {totalElements} products
            </>
          ) : (
            "No products found"
          )}
          {isFetching && (
            <span style={{ marginLeft: "10px", opacity: 0.7 }}>
              (Loading...)
            </span>
          )}
        </div>

        {/* Products Grid */}
        <div id="products-grid" className="products-grid">
          {products.length === 0 ? (
            <p className="no-results">No products found</p>
          ) : (
            products.map((product, idx) => {
              const pid = resolveProductId(product) || `__idx_${idx}`;
              const safeName = (product.name || "Unnamed Product").replace(
                /"/g,
                "&quot;"
              );
              const priceVal =
                product.price !== undefined && product.price !== null
                  ? product.price
                  : 0;
              const productIdForModal = product.productId || pid;

              return (
                <div
                  key={pid}
                  className="product-card clickable"
                  data-product-id={pid}
                  data-product-name={safeName}
                  data-product-price={priceVal}
                  onClick={() => setSelectedProductId(productIdForModal)}
                >
                  <div className="product-image">
                    <i className="fas fa-mobile-alt"></i>
                  </div>
                  <h3 className="product-title">
                    {product.name || "Unnamed Product"}
                  </h3>
                  <p className="product-description">
                    {product.description || "No description available"}
                  </p>
                  <div className="product-price">${priceVal}</div>
                  <div
                    className="product-actions"
                    onClick={e => e.stopPropagation()}
                  >
                    <button
                      className="btn btn-primary"
                      onClick={e => {
                        e.stopPropagation();
                        handleAddToCart(product);
                      }}
                    >
                      <i className="fas fa-cart-plus"></i> Add to Cart
                    </button>
                    <button
                      className="btn btn-secondary"
                      onClick={e => {
                        e.stopPropagation();
                        setSelectedProductId(productIdForModal);
                      }}
                    >
                      <i className="fas fa-eye"></i> View
                    </button>
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div
            className="pagination"
            style={{
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              gap: "0.5rem",
              marginTop: "2rem",
              flexWrap: "wrap",
            }}
          >
            <button
              className="btn btn-secondary"
              onClick={() => handlePageChange(0)}
              disabled={!hasPrevious}
              style={{ padding: "0.5rem 1rem" }}
              aria-label="Go to first page"
            >
              <i className="fas fa-angle-double-left" aria-hidden="true"></i>
            </button>
            <button
              className="btn btn-secondary"
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={!hasPrevious}
              style={{ padding: "0.5rem 1rem" }}
              aria-label="Go to previous page"
            >
              <i className="fas fa-angle-left" aria-hidden="true"></i> Prev
            </button>

            {getPageNumbers().map((page, index) =>
              page === "..." ? (
                <span key={`ellipsis-${index}`} style={{ padding: "0.5rem" }}>
                  ...
                </span>
              ) : (
                <button
                  key={page}
                  className={`btn ${
                    page === currentPage ? "btn-primary" : "btn-secondary"
                  }`}
                  onClick={() => handlePageChange(page)}
                  style={{
                    padding: "0.5rem 1rem",
                    minWidth: "40px",
                  }}
                >
                  {page + 1}
                </button>
              )
            )}

            <button
              className="btn btn-secondary"
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={!hasNext}
              style={{ padding: "0.5rem 1rem" }}
              aria-label="Go to next page"
            >
              Next <i className="fas fa-angle-right" aria-hidden="true"></i>
            </button>
            <button
              className="btn btn-secondary"
              onClick={() => handlePageChange(totalPages - 1)}
              disabled={!hasNext}
              style={{ padding: "0.5rem 1rem" }}
              aria-label="Go to last page"
            >
              <i className="fas fa-angle-double-right" aria-hidden="true"></i>
            </button>
          </div>
        )}
      </div>

      <ProductModal
        isOpen={!!selectedProductId}
        onClose={() => setSelectedProductId(null)}
        productId={selectedProductId}
      />
    </div>
  );
}
