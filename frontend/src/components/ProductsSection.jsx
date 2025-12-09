import { useState, useMemo, useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { productsAPI, categoriesAPI, brandsAPI } from "../api";
import { useCart } from "../hooks/useCart";

function resolveProductId(product) {
  return product.id || product.productId || product._id || product.sku;
}

export function ProductsSection() {
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("");
  const [selectedBrand, setSelectedBrand] = useState("");
  const { addToCart } = useCart();

  const { data: products = [], isLoading: productsLoading } = useQuery({
    queryKey: ["products"],
    queryFn: () => productsAPI.getAll(),
  });

  const { data: categories = [], isLoading: categoriesLoading } = useQuery({
    queryKey: ["categories"],
    queryFn: () => categoriesAPI.getAll(),
  });

  const { data: brands = [], isLoading: brandsLoading } = useQuery({
    queryKey: ["brands"],
    queryFn: () => brandsAPI.getAll(),
  });

  // Listen for filter events from CategoriesSection and BrandsSection
  useEffect(() => {
    const handleCategoryFilter = e => {
      setSelectedCategory(e.detail.categoryId);
    };
    const handleBrandFilter = e => {
      setSelectedBrand(e.detail.brandId);
    };

    window.addEventListener("filterByCategory", handleCategoryFilter);
    window.addEventListener("filterByBrand", handleBrandFilter);

    return () => {
      window.removeEventListener("filterByCategory", handleCategoryFilter);
      window.removeEventListener("filterByBrand", handleBrandFilter);
    };
  }, []);

  const filteredProducts = useMemo(() => {
    return products.filter(product => {
      const matchesSearch =
        !searchTerm ||
        (product.name &&
          product.name.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (product.description &&
          product.description.toLowerCase().includes(searchTerm.toLowerCase()));

      const matchesCategory =
        !selectedCategory ||
        (product.category &&
          String(product.category.id) === String(selectedCategory));

      const matchesBrand =
        !selectedBrand ||
        (product.brand && String(product.brand.id) === String(selectedBrand));

      return matchesSearch && matchesCategory && matchesBrand;
    });
  }, [products, searchTerm, selectedCategory, selectedBrand]);

  const handleAddToCart = product => {
    addToCart(product);
  };

  const scrollToSection = sectionId => {
    const section = document.getElementById(sectionId);
    if (section) {
      section.scrollIntoView({ behavior: "smooth" });
    }
  };

  if (productsLoading) {
    return (
      <section id="products" className="section">
        <div className="container">
          <h2 className="section-title">Our Products</h2>
          <p className="loading">Loading...</p>
        </div>
      </section>
    );
  }

  return (
    <section id="products" className="section">
      <div className="container">
        <h2 className="section-title">Our Products</h2>
        <div className="filters">
          <div className="filter-group">
            <label htmlFor="category-filter" className="visually-hidden">
              Filter by category
            </label>
            <select
              id="category-filter"
              className="filter-select"
              value={selectedCategory}
              onChange={e => setSelectedCategory(e.target.value)}
              aria-label="Filter by category"
            >
              <option value="">All Categories</option>
              {categories.map((cat, index) => (
                <option key={cat.id || `cat-${index}`} value={cat.id}>
                  {cat.name || "Unnamed Category"}
                </option>
              ))}
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
              onChange={e => setSelectedBrand(e.target.value)}
              aria-label="Filter by brand"
            >
              <option value="">All Brands</option>
              {brands.map((brand, index) => (
                <option key={brand.id || `brand-${index}`} value={brand.id}>
                  {brand.name || "Unnamed Brand"}
                </option>
              ))}
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
        </div>
        <div id="products-grid" className="products-grid">
          {filteredProducts.length === 0 ? (
            <p className="no-results">No products found</p>
          ) : (
            filteredProducts.map((product, idx) => {
              const pid = resolveProductId(product) || `__idx_${idx}`;
              const safeName = (product.name || "Unnamed Product").replace(
                /"/g,
                "&quot;"
              );
              const priceVal =
                product.price !== undefined && product.price !== null
                  ? product.price
                  : 0;

              return (
                <div
                  key={pid}
                  className="product-card"
                  data-product-id={pid}
                  data-product-name={safeName}
                  data-product-price={priceVal}
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
                  <div className="product-actions">
                    <button
                      className="btn btn-primary"
                      onClick={() => handleAddToCart(product)}
                    >
                      <i className="fas fa-cart-plus"></i> Add to Cart
                    </button>
                    <button
                      className="btn btn-secondary"
                      onClick={() => {
                        /* View product details */
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
      </div>
    </section>
  );
}
