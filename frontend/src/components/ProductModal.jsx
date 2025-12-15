import { useEffect, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { productsAPI, categoriesAPI, reviewsAPI } from '../api';
import { useCart } from '../hooks/useCart';
import { showAlert } from '../utils/alerts';

function resolveProductId(product) {
    return product.productId || product.id || product._id || product.sku;
}

function StarRating({ rating }) {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    
    return (
        <div className="star-rating">
            {[...Array(5)].map((_, i) => {
                if (i < fullStars) {
                    return <i key={i} className="fas fa-star"></i>;
                } else if (i === fullStars && hasHalfStar) {
                    return <i key={i} className="fas fa-star-half-alt"></i>;
                } else {
                    return <i key={i} className="far fa-star"></i>;
                }
            })}
            <span className="rating-value">{rating.toFixed(1)}</span>
        </div>
    );
}

export function ProductModal({ isOpen, onClose, productId }) {
    const { addToCart } = useCart();

    const { data: product, isLoading: productLoading } = useQuery({
        queryKey: ['product', productId],
        queryFn: () => productsAPI.getById(productId),
        enabled: !!productId && isOpen,
    });

    const { data: categories = [] } = useQuery({
        queryKey: ['categories'],
        queryFn: () => categoriesAPI.getAll(),
        enabled: isOpen,
    });

    const { data: reviews = [], isLoading: reviewsLoading } = useQuery({
        queryKey: ['reviews', productId],
        queryFn: () => reviewsAPI.getByProductId(productId),
        enabled: !!productId && isOpen,
    });

    // Get category names for this product
    const productCategories = useMemo(() => {
        if (!product?.categoryIds || !categories.length) return [];
        return categories.filter(cat => 
            product.categoryIds?.some(catId => 
                String(cat.categoryId || cat.id) === String(catId)
            )
        );
    }, [product, categories]);

    // Calculate average rating
    const averageRating = useMemo(() => {
        if (!reviews.length) return 0;
        const sum = reviews.reduce((acc, review) => acc + (review.reviewValue || 0), 0);
        return sum / reviews.length;
    }, [reviews]);

    // Handle body overflow when modal is open
    useEffect(() => {
        if (isOpen) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'auto';
        }
        return () => {
            document.body.style.overflow = 'auto';
        };
    }, [isOpen]);

    // Handle escape key
    useEffect(() => {
        if (!isOpen) return;
        
        const handleEscape = (e) => {
            if (e.key === 'Escape') {
                onClose();
            }
        };
        
        window.addEventListener('keydown', handleEscape);
        return () => window.removeEventListener('keydown', handleEscape);
    }, [isOpen, onClose]);

    if (!isOpen || !productId) return null;

    const handleAddToCart = () => {
        if (product) {
            addToCart(product);
            const productName = product.name || 'Item';
            showAlert(`${productName} added to cart`, 'success');
        }
    };

    const priceVal = product?.price !== undefined && product?.price !== null 
        ? parseFloat(product.price) 
        : 0;

    return (
        <div 
            id="product-modal" 
            className="modal"
            data-cy="product-modal"
            style={{ display: isOpen ? 'flex' : 'none' }}
            onClick={(e) => e.target.id === 'product-modal' && onClose()}
        >
            <div className="modal-content product-modal-content">
                <span className="close" onClick={onClose}>&times;</span>
                
                {productLoading ? (
                    <div className="product-modal-loading">
                        <p className="loading">Loading product details...</p>
                    </div>
                ) : product ? (
                    <div className="product-modal-body">
                        <div className="product-modal-image" data-cy="product-image">
                            <i className="fas fa-mobile-alt"></i>
                        </div>
                        
                        <div className="product-modal-info">
                            <h2 className="product-modal-title" data-cy="product-title">{product.name || 'Unnamed Product'}</h2>
                            
                            <div className="product-modal-price" data-cy="product-price">
                                ${priceVal.toFixed(2)}
                            </div>

                            {/* Average Rating */}
                            {averageRating > 0 && (
                                <div className="product-modal-rating">
                                    <StarRating rating={averageRating} />
                                    <span className="review-count" data-cy="product-review-count">({reviews.length} {reviews.length === 1 ? 'review' : 'reviews'})</span>
                                </div>
                            )}
                            
                            {/* Categories */}
                            {productCategories.length > 0 && (
                                <div className="product-modal-categories">
                                    <span className="categories-label">Categories:</span>
                                    <div className="categories-tags">
                                        {productCategories.map((cat, idx) => (
                                            <span key={cat.categoryId || cat.id || idx} className="category-tag" data-cy="product-category">
                                                {cat.name}
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            )}
                            
                            <div className="product-modal-descriptions" data-cy="product-description">
                                <h3>Description</h3>
                                <p>{product.description || 'No description available'}</p>
                            </div>
                            
                            <div className="product-modal-details">
                                <div className="product-detail-item">
                                    <span className="detail-label">SKU:</span>
                                    <span className="detail-value" data-cy="product-SKU">{product.sku || 'N/A'}</span>
                                </div>
                                {product.weight && (
                                    <div className="product-detail-item">
                                        <span className="detail-label">Weight:</span>
                                        <span className="detail-value" data-cy="product-weight">{parseFloat(product.weight).toFixed(2)} kg</span>
                                    </div>
                                )}
                            </div>

                            {/* Reviews Section */}
                            <div className="product-modal-reviews" data-cy="product-review-section">
                                <h3>Customer Reviews</h3>
                                {reviewsLoading ? (
                                    <p className="loading">Loading reviews...</p>
                                ) : reviews.length === 0 ? (
                                    <p className="no-reviews">No reviews yet. Be the first to review this product!</p>
                                ) : (
                                    <div className="reviews-list">
                                        {reviews.map((review) => (
                                            <div key={review.reviewId || review.id} className="review-item">
                                                <div className="review-header">
                                                    <div className="review-rating">
                                                        <StarRating rating={review.reviewValue || 0} />
                                                    </div>
                                                    {review.isVerifiedPurchase && (
                                                        <span className="verified-badge">
                                                            <i className="fas fa-check-circle"></i> Verified Purchase
                                                        </span>
                                                    )}
                                                </div>
                                                {review.title && (
                                                    <h4 className="review-title">{review.title}</h4>
                                                )}
                                                <p className="review-comment">{review.comment || ''}</p>
                                                {review.user && (
                                                    <div className="review-author">
                                                        <span className="author-name">
                                                            {review.user.firstName || ''} {review.user.lastName || ''}
                                                        </span>
                                                        {review.createdAt && (
                                                            <span className="review-date">
                                                                {new Date(review.createdAt).toLocaleDateString()}
                                                            </span>
                                                        )}
                                                    </div>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                            
                            <div className="product-modal-actions">
                                <button 
                                    className="btn btn-primary" 
                                    onClick={handleAddToCart}
                                >
                                    <i className="fas fa-cart-plus"></i> Add to Cart
                                </button>
                                <button 
                                    className="btn btn-secondary" 
                                    onClick={onClose}
                                >
                                    Close
                                </button>
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className="product-modal-error">
                        <p>Product not found</p>
                        <button className="btn btn-secondary" onClick={onClose}>
                            Close
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}
