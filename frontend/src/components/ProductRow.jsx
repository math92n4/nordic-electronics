import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../hooks/useCart';
import { showAlert } from '../utils/alerts';
import { ProductModal } from './ProductModal';

function resolveProductId(product) {
    return product.productId || product.product_id || product.id || product._id || product.sku;
}

export function ProductRow({ title, products, isLoading, icon }) {
    const navigate = useNavigate();
    const { addToCart } = useCart();
    const [selectedProductId, setSelectedProductId] = useState(null);
    const scrollContainerRef = useRef(null);
    const [canScrollLeft, setCanScrollLeft] = useState(false);
    const [canScrollRight, setCanScrollRight] = useState(true);

    const checkScrollButtons = () => {
        if (scrollContainerRef.current) {
            const { scrollLeft, scrollWidth, clientWidth } = scrollContainerRef.current;
            setCanScrollLeft(scrollLeft > 0);
            setCanScrollRight(scrollLeft < scrollWidth - clientWidth - 10);
        }
    };

    useEffect(() => {
        checkScrollButtons();
        const container = scrollContainerRef.current;
        if (container) {
            container.addEventListener('scroll', checkScrollButtons);
            window.addEventListener('resize', checkScrollButtons);
            return () => {
                container.removeEventListener('scroll', checkScrollButtons);
                window.removeEventListener('resize', checkScrollButtons);
            };
        }
    }, [products]);

    const scroll = (direction) => {
        if (scrollContainerRef.current) {
            const scrollAmount = 240; // card width + gap
            scrollContainerRef.current.scrollBy({
                left: direction === 'left' ? -scrollAmount : scrollAmount,
                behavior: 'smooth'
            });
        }
    };

    const handleAddToCart = (e, product) => {
        e.stopPropagation();
        // Extract price using the same logic as display to ensure consistency
        // Views return product_price (snake_case), regular API returns price (camelCase)
        const extractedPrice = product.price || product.product_price || product.productPrice || product.list_price || product.listPrice || 0;
        
        // Debug: log the product structure if price is missing
        if (process.env.NODE_ENV === 'development' && extractedPrice === 0) {
            console.warn('Product has no price field:', {
                product,
                availableFields: Object.keys(product || {}),
                priceFields: {
                    price: product?.price,
                    product_price: product?.product_price,
                    productPrice: product?.productPrice,
                    list_price: product?.list_price,
                    listPrice: product?.listPrice
                }
            });
        }
        
        // Create cart product with explicitly extracted price to ensure it's not lost
        const cartProduct = {
            ...product, // Include all product fields
            price: extractedPrice, // Explicitly set the extracted price
            // Also set common aliases to ensure CartContext can find it
            product_price: extractedPrice,
            productPrice: extractedPrice,
        };
        addToCart(cartProduct);
        const productName = product.name || product.product_name || product.productName || 'Item';
        showAlert(`${productName} added to cart`, 'success');
    };

    const handleCardClick = (product) => {
        const productId = product.productId || product.product_id || resolveProductId(product);
        if (productId) {
            setSelectedProductId(productId);
        }
    };

    if (isLoading) {
        return (
            <div className="section">
                <div className="container">
                    <h2 className="section-title">
                        {icon && <i className={icon}></i>} {title}
                    </h2>
                    <p className="loading">Loading...</p>
                </div>
            </div>
        );
    }

    // Show empty state if no products, but still render the section
    if (!products || products.length === 0) {
        return (
            <div className="section">
                <div className="container">
                    <h2 className="section-title">
                        {icon && <i className={icon}></i>} {title}
                    </h2>
                    <p className="no-results">No products available at the moment.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="section">
            <div className="container">
                <div className="product-row-header">
                    <h2 className="section-title">
                        {icon && <i className={icon}></i>} {title}
                    </h2>
                    <button 
                        className="btn btn-secondary"
                        onClick={() => navigate('/products')}
                    >
                        View All <i className="fas fa-arrow-right"></i>
                    </button>
                </div>
                <div className="product-row-wrapper">
                    <button
                        className="product-row-scroll-btn left"
                        onClick={() => scroll('left')}
                        disabled={!canScrollLeft}
                        aria-label="Scroll left"
                    >
                        <i className="fas fa-chevron-left"></i>
                    </button>
                    <div className="product-row" ref={scrollContainerRef}>
                        {products.map((product, idx) => {
                        const pid = resolveProductId(product) || `__idx_${idx}`;
                        const productId = product.productId || product.product_id || pid;
                        const name = product.name || product.product_name || 'Unnamed Product';
                        // Views don't include description, so we'll use a placeholder
                        const description = product.description || 'Check out this popular product!';
                        // Extract price - views use product_price (snake_case), regular API uses price (camelCase)
                        const price = product.price || product.productPrice || product.product_price || product.listPrice || product.list_price || 0;
                        const averageRating = product.average_rating || product.averageRating;
                        const totalSold = product.total_units_sold || product.totalUnitsSold;
                        const numberOfReviews = product.number_of_reviews || product.numberOfReviews;
                        
                        return (
                            <div
                                key={pid}
                                className="product-card clickable"
                                onClick={() => handleCardClick(product)}
                            >
                                <div className="product-image">
                                    <i className="fas fa-mobile-alt"></i>
                                </div>
                                <h3 className="product-title">{name}</h3>
                                <p className="product-description">{description}</p>
                                <div className="product-price">${parseFloat(price).toFixed(2)}</div>
                                {averageRating && (
                                    <div className="product-rating">
                                        <i className="fas fa-star"></i> {parseFloat(averageRating).toFixed(1)}
                                        {numberOfReviews && (
                                            <span className="rating-count"> ({numberOfReviews})</span>
                                        )}
                                    </div>
                                )}
                                {totalSold && (
                                    <div className="product-sold">
                                        <i className="fas fa-shopping-bag"></i> {totalSold} sold
                                    </div>
                                )}
                                <div className="product-actions" onClick={(e) => e.stopPropagation()}>
                                    <button
                                        className="btn btn-primary"
                                        onClick={(e) => handleAddToCart(e, product)}
                                    >
                                        <i className="fas fa-cart-plus"></i> Add to Cart
                                    </button>
                                    <button
                                        className="btn btn-secondary"
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            handleCardClick(product);
                                        }}
                                    >
                                        <i className="fas fa-eye"></i> View
                                    </button>
                                </div>
                            </div>
                        );
                    })}
                    </div>
                    <button
                        className="product-row-scroll-btn right"
                        onClick={() => scroll('right')}
                        disabled={!canScrollRight}
                        aria-label="Scroll right"
                    >
                        <i className="fas fa-chevron-right"></i>
                    </button>
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

