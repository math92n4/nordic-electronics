import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { brandsAPI } from '../api';

export function BrandsPage() {
    const navigate = useNavigate();
    const { data: brands = [], isLoading } = useQuery({
        queryKey: ['brands'],
        queryFn: () => brandsAPI.getAll(),
    });

    const filterByBrand = (brandId) => {
        // Navigate with query parameter
        navigate(`/products?brand=${brandId}`);
        // Also dispatch event for immediate update if already on products page
        window.dispatchEvent(new CustomEvent('filterByBrand', { detail: { brandId } }));
    };

    if (isLoading) {
        return (
            <div className="section">
                <div className="container">
                    <h2 className="section-title">Our Brands</h2>
                    <p className="loading">Loading...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="section">
            <div className="container">
                <h2 className="section-title">Our Brands</h2>
                <div id="brands-grid" className="brands-grid">
                    {brands.length === 0 ? (
                        <p className="no-results">No brands found</p>
                    ) : (
                        brands.map((brand, index) => {
                            const brandId = brand.brandId || brand.id;
                            return (
                                <div 
                                    key={brandId || `brand-${index}`} 
                                    className="brand-card" 
                                    onClick={() => filterByBrand(brandId)}
                                >
                                <div className="brand-logo">
                                    <i className="fas fa-building"></i>
                                </div>
                                <h3 className="brand-name">{brand.name || 'Unnamed Brand'}</h3>
                            </div>
                            );
                        })
                    )}
                </div>
            </div>
        </div>
    );
}

