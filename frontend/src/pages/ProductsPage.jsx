import { useState, useMemo, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { productsAPI, categoriesAPI, brandsAPI } from '../api';
import { useCart } from '../hooks/useCart';
import { showAlert } from '../utils/alerts';
import { ProductModal } from '../components/ProductModal';

function resolveProductId(product) {
    // ProductResponseDTO uses productId field
    return product.productId || product.id || product._id || product.sku;
}

export function ProductsPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const navigate = useNavigate();
    
    // Initialize state from URL params
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedCategory, setSelectedCategory] = useState('');
    const [selectedBrand, setSelectedBrand] = useState('');
    const [selectedProductId, setSelectedProductId] = useState(null);
    const { addToCart } = useCart();

    // Sync state with URL params on mount and when URL changes
    useEffect(() => {
        const categoryParam = searchParams.get('category') || '';
        const brandParam = searchParams.get('brand') || '';
        const searchParam = searchParams.get('search') || '';
        
        if (categoryParam !== selectedCategory) {
            setSelectedCategory(categoryParam);
        }
        if (brandParam !== selectedBrand) {
            setSelectedBrand(brandParam);
        }
        if (searchParam !== searchTerm) {
            setSearchTerm(searchParam);
        }
    }, [searchParams]);

    const { data: products = [], isLoading: productsLoading } = useQuery({
        queryKey: ['products'],
        queryFn: () => productsAPI.getAll(),
    });

    const { data: categories = [], isLoading: categoriesLoading } = useQuery({
        queryKey: ['categories'],
        queryFn: () => categoriesAPI.getAll(),
    });

    const { data: brands = [], isLoading: brandsLoading } = useQuery({
        queryKey: ['brands'],
        queryFn: () => brandsAPI.getAll(),
    });

    // Update URL params when filters change
    useEffect(() => {
        const params = new URLSearchParams();
        if (searchTerm) params.set('search', searchTerm);
        if (selectedCategory) params.set('category', selectedCategory);
        if (selectedBrand) params.set('brand', selectedBrand);
        
        // Only update if params actually changed to avoid infinite loops
        const currentParams = searchParams.toString();
        if (params.toString() !== currentParams) {
            setSearchParams(params, { replace: true });
        }
    }, [searchTerm, selectedCategory, selectedBrand, searchParams, setSearchParams]);

    // Listen for filter events from CategoriesPage and BrandsPage
    useEffect(() => {
        const handleCategoryFilter = (e) => {
            const categoryId = e.detail.categoryId;
            setSelectedCategory(categoryId);
            // Update URL immediately
            const params = new URLSearchParams(searchParams);
            if (categoryId) {
                params.set('category', categoryId);
            } else {
                params.delete('category');
            }
            setSearchParams(params, { replace: true });
        };
        
        const handleBrandFilter = (e) => {
            const brandId = e.detail.brandId;
            setSelectedBrand(brandId);
            // Update URL immediately
            const params = new URLSearchParams(searchParams);
            if (brandId) {
                params.set('brand', brandId);
            } else {
                params.delete('brand');
            }
            setSearchParams(params, { replace: true });
        };

        window.addEventListener('filterByCategory', handleCategoryFilter);
        window.addEventListener('filterByBrand', handleBrandFilter);

        return () => {
            window.removeEventListener('filterByCategory', handleCategoryFilter);
            window.removeEventListener('filterByBrand', handleBrandFilter);
        };
    }, [searchParams, setSearchParams]);

    const filteredProducts = useMemo(() => {
        if (!products || products.length === 0) return [];
        
        return products.filter(product => {
            // Search filter
            const matchesSearch = !searchTerm ||
                (product.name && product.name.toLowerCase().includes(searchTerm.toLowerCase())) ||
                (product.description && product.description.toLowerCase().includes(searchTerm.toLowerCase()));

            // Category filter - ProductResponseDTO has categoryIds as array
            let matchesCategory = true;
            if (selectedCategory) {
                const selectedCategoryStr = String(selectedCategory).trim();
                if (product.categoryIds && Array.isArray(product.categoryIds) && product.categoryIds.length > 0) {
                    matchesCategory = product.categoryIds.some(catId => {
                        // Convert both to strings and compare (UUIDs are case-insensitive but we normalize)
                        const catIdStr = String(catId).trim();
                        return catIdStr === selectedCategoryStr;
                    });
                } else {
                    matchesCategory = false;
                }
            }

            // Brand filter - ProductResponseDTO has brandId as UUID
            let matchesBrand = true;
            if (selectedBrand) {
                const selectedBrandStr = String(selectedBrand).trim();
                if (product.brandId) {
                    const brandIdStr = String(product.brandId).trim();
                    matchesBrand = brandIdStr === selectedBrandStr;
                } else {
                    matchesBrand = false;
                }
            }

            return matchesSearch && matchesCategory && matchesBrand;
        });
    }, [products, searchTerm, selectedCategory, selectedBrand]);

    const handleAddToCart = (product) => {
        addToCart(product);
        // Show success alert
        const productName = product.name || 'Item';
        showAlert(`${productName} added to cart`, 'success');
    };

    if (productsLoading) {
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
                <div className="filters">
                    <select 
                        id="category-filter" 
                        className="filter-select"
                        value={selectedCategory}
                        onChange={(e) => {
                            setSelectedCategory(e.target.value);
                            // Update URL
                            const params = new URLSearchParams(searchParams);
                            if (e.target.value) {
                                params.set('category', e.target.value);
                            } else {
                                params.delete('category');
                            }
                            setSearchParams(params, { replace: true });
                        }}
                    >
                        <option value="">All Categories</option>
                        {categories.map((cat, index) => {
                            const catId = cat.categoryId || cat.id;
                            // Ensure we convert to string for consistent comparison
                            const catIdValue = catId ? String(catId) : '';
                            return (
                                <option key={catIdValue || `cat-${index}`} value={catIdValue}>
                                    {cat.name || 'Unnamed Category'}
                                </option>
                            );
                        })}
                    </select>
                    <select 
                        id="brand-filter" 
                        className="filter-select"
                        value={selectedBrand}
                        onChange={(e) => {
                            setSelectedBrand(e.target.value);
                            // Update URL
                            const params = new URLSearchParams(searchParams);
                            if (e.target.value) {
                                params.set('brand', e.target.value);
                            } else {
                                params.delete('brand');
                            }
                            setSearchParams(params, { replace: true });
                        }}
                    >
                        <option value="">All Brands</option>
                        {brands.map((brand, index) => {
                            const brandId = brand.brandId || brand.id;
                            // Ensure we convert to string for consistent comparison
                            const brandIdValue = brandId ? String(brandId) : '';
                            return (
                                <option key={brandIdValue || `brand-${index}`} value={brandIdValue}>
                                    {brand.name || 'Unnamed Brand'}
                                </option>
                            );
                        })}
                    </select>
                    <input 
                        type="text" 
                        id="search-input" 
                        className="search-input" 
                        placeholder="Search products..."
                        value={searchTerm}
                        onChange={(e) => {
                            setSearchTerm(e.target.value);
                            // Update URL with debounce effect
                            const params = new URLSearchParams(searchParams);
                            if (e.target.value) {
                                params.set('search', e.target.value);
                            } else {
                                params.delete('search');
                            }
                            setSearchParams(params, { replace: true });
                        }}
                    />
                </div>
                <div id="products-grid" className="products-grid">
                    {filteredProducts.length === 0 ? (
                        <p className="no-results">No products found</p>
                    ) : (
                        filteredProducts.map((product, idx) => {
                            const pid = resolveProductId(product) || `__idx_${idx}`;
                            const safeName = (product.name || 'Unnamed Product').replace(/"/g, '&quot;');
                            const priceVal = product.price !== undefined && product.price !== null ? product.price : 0;
                            // Use productId for the modal (needed for API call)
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
                                    <h3 className="product-title">{product.name || 'Unnamed Product'}</h3>
                                    <p className="product-description">{product.description || 'No description available'}</p>
                                    <div className="product-price">${priceVal}</div>
                                    <div className="product-actions" onClick={(e) => e.stopPropagation()}>
                                        <button 
                                            className="btn btn-primary" 
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleAddToCart(product);
                                            }}
                                        >
                                            <i className="fas fa-cart-plus"></i> Add to Cart
                                        </button>
                                        <button 
                                            className="btn btn-secondary" 
                                            onClick={(e) => {
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
            </div>
            
            <ProductModal 
                isOpen={!!selectedProductId}
                onClose={() => setSelectedProductId(null)}
                productId={selectedProductId}
            />
        </div>
    );
}

