import { useQuery } from '@tanstack/react-query';
import { brandsAPI } from '../api';

export function BrandsSection() {
    const { data: brands = [], isLoading } = useQuery({
        queryKey: ['brands'],
        queryFn: () => brandsAPI.getAll(),
    });

    const filterByBrand = (brandId) => {
        // Dispatch a custom event that ProductsSection can listen to
        window.dispatchEvent(new CustomEvent('filterByBrand', { detail: { brandId } }));
        scrollToSection('products');
    };

    const scrollToSection = (sectionId) => {
        const section = document.getElementById(sectionId);
        if (section) {
            section.scrollIntoView({ behavior: 'smooth' });
        }
    };

    if (isLoading) {
        return (
            <section id="brands" className="section">
                <div className="container">
                    <h2 className="section-title">Our Brands</h2>
                    <p className="loading">Loading...</p>
                </div>
            </section>
        );
    }

    return (
        <section id="brands" className="section">
            <div className="container">
                <h2 className="section-title">Our Brands</h2>
                <div id="brands-grid" className="brands-grid">
                    {brands.length === 0 ? (
                        <p className="no-results">No brands found</p>
                    ) : (
                        brands.map((brand, index) => (
                            <div 
                                key={brand.id || `brand-${index}`} 
                                className="brand-card" 
                                onClick={() => filterByBrand(brand.id)}
                            >
                                <div className="brand-logo">
                                    <i className="fas fa-building"></i>
                                </div>
                                <h3 className="brand-name">{brand.name || 'Unnamed Brand'}</h3>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </section>
    );
}

