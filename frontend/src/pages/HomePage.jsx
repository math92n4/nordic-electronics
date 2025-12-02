import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { productsAPI } from '../api';
import { ProductRow } from '../components/ProductRow';
import { ProductModal } from '../components/ProductModal';
import { useState } from 'react';

export function HomePage() {
    const navigate = useNavigate();
    const [selectedProductId, setSelectedProductId] = useState(null);

    const { data: bestSelling = [], isLoading: bestSellingLoading, error: bestSellingError } = useQuery({
        queryKey: ['best-selling'],
        queryFn: () => productsAPI.getBestSelling(),
    });

    const { data: bestReviewed = [], isLoading: bestReviewedLoading, error: bestReviewedError } = useQuery({
        queryKey: ['best-reviewed'],
        queryFn: () => productsAPI.getBestReviewed(),
    });

    // Log errors for debugging
    if (bestSellingError) {
        console.error('Error fetching best selling products:', bestSellingError);
    }
    if (bestReviewedError) {
        console.error('Error fetching best reviewed products:', bestReviewedError);
    }

    return (
        <>
            <section className="hero">
                <div className="hero-container">
                    <h1>Welcome to Nordic Electronics</h1>
                    <p>Discover the latest in electronic gadgets and technology</p>
                    <button 
                        className="cta-button" 
                        onClick={() => navigate('/products')}
                    >
                        Shop Now <i className="fas fa-arrow-right"></i>
                    </button>
                </div>
            </section>

            <ProductRow
                title="Best Selling Products"
                products={bestSelling}
                isLoading={bestSellingLoading}
                icon="fas fa-fire"
            />

            <ProductRow
                title="Best Reviewed Products"
                products={bestReviewed}
                isLoading={bestReviewedLoading}
                icon="fas fa-star"
            />

            <ProductModal
                isOpen={!!selectedProductId}
                onClose={() => setSelectedProductId(null)}
                productId={selectedProductId}
            />
        </>
    );
}

